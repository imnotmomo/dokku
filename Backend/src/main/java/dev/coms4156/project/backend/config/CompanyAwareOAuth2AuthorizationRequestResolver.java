package dev.coms4156.project.backend.config;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

/**
 * Adds company id information to the OAuth2 authorization request if provided.
 */
public class CompanyAwareOAuth2AuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

  private final DefaultOAuth2AuthorizationRequestResolver delegate;

  public CompanyAwareOAuth2AuthorizationRequestResolver(
      ClientRegistrationRepository clientRegistrationRepository) {
    this.delegate = new DefaultOAuth2AuthorizationRequestResolver(
        clientRegistrationRepository, "/oauth2/authorization");
  }

  @Override
  public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
    return enhance(delegate.resolve(request), request);
  }

  @Override
  public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
    return enhance(delegate.resolve(request, clientRegistrationId), request);
  }

  private OAuth2AuthorizationRequest enhance(OAuth2AuthorizationRequest authorizationRequest,
      HttpServletRequest request) {
    if (authorizationRequest == null) {
      return null;
    }
    String companyId = request.getParameter("companyId");
    if (companyId == null || companyId.isBlank()) {
      return authorizationRequest;
    }
    Map<String, Object> additionalParameters =
        new HashMap<>(authorizationRequest.getAdditionalParameters());
    additionalParameters.put("companyId", companyId);
    return OAuth2AuthorizationRequest.from(authorizationRequest)
        .additionalParameters(additionalParameters)
        .build();
  }
}
