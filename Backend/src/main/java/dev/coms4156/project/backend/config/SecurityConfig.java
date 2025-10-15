package dev.coms4156.project.backend.config;

import dev.coms4156.project.backend.model.CompanyAccount;
import dev.coms4156.project.backend.service.db.CompanyAccountDbService;
import dev.coms4156.project.backend.service.db.UserDbService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

/**
 * Security configuration enabling Google OAuth2 login and securing write operations.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

  private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
  private final Set<String> adminEmails;
  private final UserDbService userDbService;
  private final CompanyAccountDbService companyAccountDbService;

  /**
   * Create a security configuration with role and account services.
   *
   * @param adminEmailList comma-separated admin email addresses
   * @param userDbService persistence service for OAuth users
   * @param companyAccountDbService persistence service for third-party accounts
   */
  public SecurityConfig(@Value("${app.admin.emails:}") final String adminEmailList,
                        final UserDbService userDbService,
                        final CompanyAccountDbService companyAccountDbService) {
    this.adminEmails = Arrays.stream(adminEmailList.split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .map(s -> s.toLowerCase(Locale.ROOT))
        .collect(Collectors.toSet());
    this.userDbService = userDbService;
    this.companyAccountDbService = companyAccountDbService;
  }

  /**
   * Configure the security filter chain for the application.
   *
   * @param http the HTTP security builder
   * @return configured security filter chain
   * @throws Exception if Spring Security cannot be configured
   */
  @Bean
  SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/", "/index").permitAll()
            .requestMatchers("/login", "/error", "/oauth2/**").permitAll()
            .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").authenticated()
            .requestMatchers("/h2-console/**").authenticated()
            .requestMatchers("/v1/**").authenticated()
            .anyRequest().authenticated())
        .oauth2Login(oauth -> oauth
            .userInfoEndpoint(userInfo -> userInfo.oidcUserService(oidcUserService()))
            .failureHandler(authenticationFailureHandler()))
        .logout(logout -> logout.logoutSuccessUrl("/").permitAll());
    return http.build();
  }

  private AuthenticationFailureHandler authenticationFailureHandler() {
    return (HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) -> {
      if (logger.isErrorEnabled()) {
        logger.error("OAuth authentication failed: {}", exception.getMessage(), exception);
      }
      if (exception instanceof OAuth2AuthenticationException oauth2Exception
          && logger.isErrorEnabled()) {
        logger.error("OAuth2 Error Code: {}", oauth2Exception.getError().getErrorCode());
        logger.error("OAuth2 Error Description: {}",
            oauth2Exception.getError().getDescription());
      }
      try {
        response.sendRedirect("/login?error=true");
      } catch (IOException ioe) {
        if (logger.isErrorEnabled()) {
          logger.error("Redirect after OAuth failure failed", ioe);
        }
      }
    };
  }

  private OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
    OidcUserService delegate = new OidcUserService();
    return request -> {
      OidcUser user = delegate.loadUser(request);
      String subject = user.getSubject();
      if (subject == null || subject.isBlank()) {
        subject = user.getName();
      }
      String email = user.getAttribute("email");
      String displayName = user.getAttribute("name");
      String pictureUrl = user.getAttribute("picture");
      userDbService.upsertUser(subject, email, displayName, pictureUrl);

      Optional<CompanyAccount> accountOpt = companyAccountDbService.findBySubject(subject);
      boolean approvedCompany = accountOpt.map(CompanyAccount::isApproved).orElse(false);
      Set<String> storedRoles = userDbService.getRoles(subject);
      Set<String> effectiveRoles = new LinkedHashSet<>(storedRoles);
      if (approvedCompany) {
        effectiveRoles.remove("USER");
        effectiveRoles.add("THIRD_PARTY_INTEGRATION");
      } else {
        effectiveRoles.add("USER");
      }
      if (logger.isInfoEnabled()) {
        logger.info("User logged in with email: {}", email);
        logger.info("Checking against admin emails: {}", adminEmails);
      }
      if (email != null && adminEmails.contains(email.toLowerCase(Locale.ROOT))) {
        effectiveRoles.add("ADMIN");
      }
      if (!effectiveRoles.equals(storedRoles)) {
        userDbService.replaceRoles(subject, effectiveRoles);
      }
      Set<GrantedAuthority> authorities = new HashSet<>(user.getAuthorities());
      for (String role : effectiveRoles) {
        String authority = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        authorities.add(new SimpleGrantedAuthority(authority));
      }
      if (logger.isInfoEnabled()) {
        logger.info("User {} effective roles: {}", email, effectiveRoles);
      }
      return new DefaultOidcUser(authorities, user.getIdToken(), user.getUserInfo());
    };
  }
}
