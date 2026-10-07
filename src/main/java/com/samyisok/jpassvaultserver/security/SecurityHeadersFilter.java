package com.samyisok.jpassvaultserver.security;

import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(1)
public class SecurityHeadersFilter extends OncePerRequestFilter {
  static final String NOSNIFF_HEADER = "X-Content-Type-Options";
  static final String NOSNIFF_VALUE = "nosniff";
  static final String CACHE_CONTROL_HEADER = "Cache-Control";
  static final String CACHE_CONTROL_VALUE = "no-store";

  @Override
  protected boolean shouldNotFilterErrorDispatch() {
    return false;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    response.setHeader(NOSNIFF_HEADER, NOSNIFF_VALUE);
    response.setHeader(CACHE_CONTROL_HEADER, CACHE_CONTROL_VALUE);
    filterChain.doFilter(request, response);
  }
}
