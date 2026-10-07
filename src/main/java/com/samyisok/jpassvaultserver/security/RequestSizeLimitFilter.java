package com.samyisok.jpassvaultserver.security;

import java.io.IOException;
import com.samyisok.jpassvaultserver.AppProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RequestSizeLimitFilter extends OncePerRequestFilter {
  static final String ERROR = "Payload Too Large";

  private final AppProperties appProperties;

  public RequestSizeLimitFilter(AppProperties appProperties) {
    this.appProperties = appProperties;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    long maxPayloadSize = appProperties.getMaxPayloadSize();
    long contentLength = request.getContentLengthLong();

    if (contentLength > maxPayloadSize) {
      writeError(response, HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
      return;
    }
    if (contentLength < 0 && hasBody(request.getMethod())) {
      // A body of unknown length cannot be bounded before it is read.
      writeError(response, HttpServletResponse.SC_LENGTH_REQUIRED);
      return;
    }
    filterChain.doFilter(request, response);
  }

  private static boolean hasBody(String method) {
    return "POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method);
  }

  private static void writeError(HttpServletResponse response, int status)
      throws IOException {
    response.reset();
    response.setStatus(status);
    response.setHeader(SecurityHeadersFilter.NOSNIFF_HEADER,
        SecurityHeadersFilter.NOSNIFF_VALUE);
    response.setHeader(SecurityHeadersFilter.CACHE_CONTROL_HEADER,
        SecurityHeadersFilter.CACHE_CONTROL_VALUE);
    response.setContentLength(ERROR.length());
    response.getWriter().write(ERROR);
  }
}
