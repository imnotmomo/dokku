package dev.coms4156.project.backend.config;

import dev.coms4156.project.backend.service.ApiCallLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Logs API invocations with the authenticated user's OAuth subject identifier.
 */
@Component
public class UserRequestLoggingInterceptor implements HandlerInterceptor {

  private static final Logger logger =
      LoggerFactory.getLogger(UserRequestLoggingInterceptor.class);
  private static final String START_TIME_ATTRIBUTE = UserRequestLoggingInterceptor.class.getName()
      + ".start";
  private final ApiCallLogService callLogService;

  public UserRequestLoggingInterceptor(final ApiCallLogService callLogService) {
    this.callLogService = callLogService;
  }

  @Override
  public boolean preHandle(final HttpServletRequest request,
                           final HttpServletResponse response,
                           final Object handler) {
    request.setAttribute(START_TIME_ATTRIBUTE, System.currentTimeMillis());
    return true;
  }

  @Override
  public void afterCompletion(final HttpServletRequest request,
                              final HttpServletResponse response,
                              final Object handler,
                              @Nullable final Exception ex) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      return;
    }
    Object principal = authentication.getPrincipal();
    if (!(principal instanceof OAuth2AuthenticatedPrincipal oauthPrincipal)) {
      return;
    }
    String subject = oauthPrincipal.getAttribute("sub");
    if (subject == null || subject.isBlank()) {
      subject = oauthPrincipal.getName();
    }
    if (subject == null || subject.isBlank()) {
      return;
    }
    String uri = request.getRequestURI();
    if (!uri.startsWith("/v1/")) {
      return;
    }
    String query = request.getQueryString();
    if (query != null && !query.isBlank()) {
      uri = uri + '?' + query;
    }
    Object startedAt = request.getAttribute(START_TIME_ATTRIBUTE);
    long durationMs = 0L;
    if (startedAt instanceof Long) {
      durationMs = System.currentTimeMillis() - (Long) startedAt;
    }
    try {
      callLogService.record(subject, request.getMethod(), uri, response.getStatus(), durationMs);
    } catch (DataAccessException dae) {
      if (logger.isWarnEnabled()) {
        logger.warn("Failed to persist API call log for subject {}", subject, dae);
      }
    }
    if (logger.isInfoEnabled()) {
      logger.info("API invocation userSub={} method={} uri={} status={} durationMs={} at={}",
          subject,
          request.getMethod(),
          uri,
          response.getStatus(),
          durationMs,
          Instant.now());
      if (ex != null) {
        logger.info("API invocation had exception userSub={} uri={} exception={}",
            subject, uri, ex.getClass().getSimpleName(), ex);
      }
    }
  }
}
