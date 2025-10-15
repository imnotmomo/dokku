package dev.coms4156.project.backend.config;

import dev.coms4156.project.backend.model.CompanyAccount;
import dev.coms4156.project.backend.service.ApiCallLogService;
import dev.coms4156.project.backend.service.db.CompanyAccountDbService;
import dev.coms4156.project.backend.service.db.UserDbService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.Collections;
import java.util.Set;
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
  private final CompanyAccountDbService companyAccountDbService;
  private final UserDbService userDbService;

  /**
   * Create a logging interceptor that records requests to the audit log.
   *
   * @param callLogService service that persists API call entries
   * @param companyAccountDbService lookup service for approved third-party accounts
   * @param userDbService service that resolves stored user roles
   */
  public UserRequestLoggingInterceptor(final ApiCallLogService callLogService,
                                       final CompanyAccountDbService companyAccountDbService,
                                       final UserDbService userDbService) {
    this.callLogService = callLogService;
    this.companyAccountDbService = companyAccountDbService;
    this.userDbService = userDbService;
  }

  /**
   * Capture the request start time prior to controller execution.
   *
   * @param request active HTTP request
   * @param response active HTTP response
   * @param handler matched handler
   * @return true to continue processing
   */
  @Override
  public boolean preHandle(final HttpServletRequest request,
                           final HttpServletResponse response,
                           final Object handler) {
    request.setAttribute(START_TIME_ATTRIBUTE, System.currentTimeMillis());
    return true;
  }

  /**
   * Record request metadata for audited routes after completion.
   *
   * @param request active HTTP request
   * @param response active HTTP response
   * @param handler matched handler
   * @param ex optional exception thrown during processing
   */
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
    CompanyAccount account = companyAccountDbService.findBySubject(subject)
        .filter(CompanyAccount::isApproved).orElse(null);
    String callType;
    String userSubject = subject;
    String displaySubject = subject;
    String caller = subject;
    if (account != null) {
      callType = "THIRD_PARTY_INTEGRATION";
      String companyName = account.getCompanyName();
      if (companyName != null && !companyName.isBlank()) {
        displaySubject = companyName;
        caller = companyName;
      }
    } else {
      callType = "USER";
    }
    Set<String> roles = Collections.emptySet();
    try {
      roles = userDbService.getRoles(subject);
    } catch (DataAccessException dae) {
      if (logger.isWarnEnabled()) {
        logger.warn("Failed to load roles for subject {}", subject, dae);
      }
    }
    String userRole = resolveRole(roles, callType);
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
      callLogService.record(userSubject,
          callType,
          caller,
          request.getMethod(),
          uri,
          response.getStatus(),
          durationMs);
    } catch (DataAccessException dae) {
      if (logger.isWarnEnabled()) {
        logger.warn("Failed to persist API call log for subject {}", userSubject, dae);
      }
    }
    if (logger.isInfoEnabled()) {
      logger.info(
          "API call sub={} display={} role={} type={} method={} uri={} status={} dur={} at={}",
          userSubject,
          displaySubject,
          userRole,
          callType,
          request.getMethod(),
          uri,
          response.getStatus(),
          durationMs,
          Instant.now());
    }
    if (ex != null && logger.isInfoEnabled()) {
      logger.info("API call exception subject={} display={} uri={} exception={}",
          userSubject,
          displaySubject,
          uri,
          ex.getClass().getSimpleName(),
          ex);
    }
  }

  private String resolveRole(Set<String> roles, String fallback) {
    if (roles != null) {
      if (roles.contains("ADMIN")) {
        return "ADMIN";
      }
      if (roles.contains("THIRD_PARTY_INTEGRATION")) {
        return "THIRD_PARTY_INTEGRATION";
      }
      if (roles.contains("USER")) {
        return "USER";
      }
    }
    if (fallback != null && !fallback.isBlank()) {
      return fallback;
    }
    return "USER";
  }
}
