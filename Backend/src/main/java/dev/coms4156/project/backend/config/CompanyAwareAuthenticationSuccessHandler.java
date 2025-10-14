package dev.coms4156.project.backend.config;

import dev.coms4156.project.backend.service.db.UserDbService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

/**
 * Handles assigning company information after OAuth2 login.
 */
public class CompanyAwareAuthenticationSuccessHandler
    extends SavedRequestAwareAuthenticationSuccessHandler {

  private final UserDbService userDbService;
  private final HttpSessionOAuth2AuthorizationRequestRepository authorizationRequestRepository;

  public CompanyAwareAuthenticationSuccessHandler(UserDbService userDbService,
      HttpSessionOAuth2AuthorizationRequestRepository authorizationRequestRepository) {
    this.userDbService = userDbService;
    this.authorizationRequestRepository = authorizationRequestRepository;
  }

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                      Authentication authentication)
      throws ServletException, IOException {
    OAuth2AuthorizationRequest authRequest =
        authorizationRequestRepository.removeAuthorizationRequest(request, response);
    Long companyId = extractCompanyId(authRequest);
    if (companyId != null
        && authentication instanceof OAuth2AuthenticationToken token
        && token.getPrincipal() instanceof OidcUser oidcUser) {
      String subject = oidcUser.getSubject();
      userDbService.assignCompany(subject, companyId);
      Set<GrantedAuthority> updatedAuthorities = new HashSet<>(authentication.getAuthorities());
      updatedAuthorities.add(new SimpleGrantedAuthority("ROLE_COMPANY"));
      OidcUser updatedUser = new DefaultOidcUser(updatedAuthorities,
          oidcUser.getIdToken(), oidcUser.getUserInfo());
      OAuth2AuthenticationToken newAuth = new OAuth2AuthenticationToken(updatedUser,
          updatedAuthorities, token.getAuthorizedClientRegistrationId());
      SecurityContextHolder.getContext().setAuthentication(newAuth);
      authentication = newAuth;
    }
    super.onAuthenticationSuccess(request, response, authentication);
  }

  private Long extractCompanyId(OAuth2AuthorizationRequest authRequest) {
    if (authRequest == null) {
      return null;
    }
    Object value = authRequest.getAdditionalParameters().get("companyId");
    if (value instanceof Long l) {
      return l;
    }
    if (value instanceof String s && !s.isBlank()) {
      try {
        return Long.parseLong(s);
      } catch (NumberFormatException ex) {
        return null;
      }
    }
    return null;
  }
}
