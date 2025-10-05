package dev.coms4156.project.backend.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
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

  /**
   * Construct the configuration with a set of admin email addresses.
   */
  public SecurityConfig(@Value("${app.admin.emails:}") final String adminEmailList) {
    this.adminEmails = Arrays.stream(adminEmailList.split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .map(s -> s.toLowerCase(Locale.ROOT))
        .collect(Collectors.toSet());
  }

  /**
   * Configure HTTP security rules and OAuth2 login for Google-authenticated users.
   */
  @Bean
  SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/", "/index").permitAll()
            .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
            .requestMatchers("/login", "/error", "/oauth2/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/v1/bathrooms/**").permitAll()
            .anyRequest().authenticated())
        .oauth2Login(oauth -> oauth
            .userInfoEndpoint(userInfo -> userInfo.oidcUserService(oidcUserService()))
            .defaultSuccessUrl("/", true)
            .failureHandler(authenticationFailureHandler()))
        .logout(logout -> logout.logoutSuccessUrl("/").permitAll());
    return http.build();
  }

  /**
   * Custom failure handler to log OAuth errors.
   */
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

  /**
   * Loads the Google user profile and assigns ROLE_USER plus ROLE_ADMIN when email matches.
   */
  private OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
    OidcUserService delegate = new OidcUserService();
    return request -> {
      OidcUser user = delegate.loadUser(request);
      Set<GrantedAuthority> authorities = new HashSet<>(user.getAuthorities());
      authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
      String email = user.getAttribute("email");
      if (logger.isInfoEnabled()) {
        logger.info("User logged in with email: {}", email);
        logger.info("Checking against admin emails: {}", adminEmails);
      }
      if (email != null && adminEmails.contains(email.toLowerCase(Locale.ROOT))) {
        if (logger.isInfoEnabled()) {
          logger.info("User {} is an admin, adding ROLE_ADMIN", email);
        }
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
      } else {
        if (logger.isInfoEnabled()) {
          logger.info("User {} is NOT an admin", email);
        }
      }
      return new DefaultOidcUser(authorities, user.getIdToken(), user.getUserInfo());
    };
  }
}
