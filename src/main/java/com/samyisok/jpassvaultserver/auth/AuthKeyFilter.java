package com.samyisok.jpassvaultserver.auth;

import java.io.IOException;
import java.util.Locale;
import com.samyisok.jpassvaultserver.AppProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

@Component
public class AuthKeyFilter extends GenericFilterBean {

  private final AuthCheck authCheck;
  private final AuthThrottle authThrottle;
  private final AppProperties appProperties;

  static final String ERROR = "Invalid API KEY";
  static final String TOO_MANY_REQUESTS = "Too Many Requests";
  static final String HEADER = "X-FORWARDED-FOR";
  static final String FORWARDED_HEADER = "Forwarded";
  static final String INVALID_CREDENTIAL_EVENT = "Invalid credential presented";
  static final String NOSNIFF_HEADER = "X-Content-Type-Options";
  static final String CACHE_CONTROL_HEADER = "Cache-Control";
  static final int MAX_ADDRESS_LENGTH = 255;

  AuthKeyFilter(AuthCheck authCheck, AuthThrottle authThrottle,
      AppProperties appProperties) {
    this.authCheck = authCheck;
    this.authThrottle = authThrottle;
    this.appProperties = appProperties;
  }

  Logger getLogger() {
    return LoggerFactory.getLogger(AuthKeyFilter.class);
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response,
      FilterChain chain) throws IOException, ServletException {

    HttpServletRequest httpRequest = (HttpServletRequest) request;
    String token = httpRequest.getHeader("token");
    String logAddress = resolveClientAddress(httpRequest);
    // Throttle on the connection address so a client cannot evade the limit by
    // rotating a forwarded header; the resolved address is only for logging.
    String throttleKey = httpRequest.getRemoteAddr();

    if (authThrottle.isBlocked(throttleKey)) {
      responseWithTooManyRequests(response);
      return;
    }

    if (token != null && authCheck.verify(token)) {
      authThrottle.reset(throttleKey);
      logIp(httpRequest);
      chain.doFilter(request, response);
    } else {
      authThrottle.recordFailure(throttleKey);
      getLogger().info(INVALID_CREDENTIAL_EVENT + "; ip: " + logAddress);
      responseWithError(response);
    }
  }

  void logIp(HttpServletRequest httpRequest) {
    getLogger().info("Correct Auth; ip: " + resolveClientAddress(httpRequest) + "\n path: "
        + httpRequest.getContextPath());
  }

  String resolveClientAddress(HttpServletRequest request) {
    return resolveClientAddress(request,
        Boolean.TRUE.equals(appProperties.getTrustProxyHeaders()));
  }

  String resolveClientAddress(HttpServletRequest request, boolean trustProxyHeaders) {
    if (trustProxyHeaders) {
      String forwarded = sanitize(lastForwardedValue(request.getHeader(HEADER)));
      if (forwarded != null) {
        return forwarded;
      }
      String standard = sanitize(standardForwardedAddress(request.getHeader(FORWARDED_HEADER)));
      if (standard != null) {
        return standard;
      }
    }
    return request.getRemoteAddr();
  }

  private static String sanitize(String value) {
    if (value == null) {
      return null;
    }
    String cleaned = value.replaceAll("[\\r\\n\\t]", "").trim();
    if (cleaned.isEmpty()) {
      return null;
    }
    return cleaned.length() > MAX_ADDRESS_LENGTH ? cleaned.substring(0, MAX_ADDRESS_LENGTH)
        : cleaned;
  }

  private static String lastForwardedValue(String headerValue) {
    if (headerValue == null || headerValue.isBlank()) {
      return null;
    }
    String[] parts = headerValue.split(",");
    String last = parts[parts.length - 1].trim();
    return last.isEmpty() ? null : last;
  }

  private static String standardForwardedAddress(String headerValue) {
    if (headerValue == null || headerValue.isBlank()) {
      return null;
    }
    String[] elements = headerValue.split(",");
    String nearest = elements[elements.length - 1].trim();
    for (String part : nearest.split(";")) {
      String candidate = part.trim();
      if (candidate.toLowerCase(Locale.ROOT).startsWith("for=")) {
        String value = candidate.substring(4).trim();
        if (value.length() > 1 && value.startsWith("\"") && value.endsWith("\"")) {
          value = value.substring(1, value.length() - 1);
        }
        return value.isEmpty() ? null : value;
      }
    }
    return null;
  }

  void responseWithError(ServletResponse response) throws IOException {
    writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, ERROR);
  }

  void responseWithTooManyRequests(ServletResponse response) throws IOException {
    writeErrorResponse(response, 429, TOO_MANY_REQUESTS);
  }

  private void writeErrorResponse(ServletResponse response, int status, String body)
      throws IOException {
    HttpServletResponse resp = (HttpServletResponse) response;
    resp.reset();
    resp.setStatus(status);
    resp.setContentType("text/plain;charset=UTF-8");
    resp.setHeader(NOSNIFF_HEADER, "nosniff");
    resp.setHeader(CACHE_CONTROL_HEADER, "no-store");
    response.setContentLength(body.length());
    response.getWriter().write(body);
  }
}
