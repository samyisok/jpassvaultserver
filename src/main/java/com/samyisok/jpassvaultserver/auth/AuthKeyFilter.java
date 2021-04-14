package com.samyisok.jpassvaultserver.auth;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

@Component
public class AuthKeyFilter extends GenericFilterBean {
  private static final Logger log = LoggerFactory.getLogger(AuthKeyFilter.class);

  @Autowired
  private AuthCheck authCheck;

  @Override
  public void doFilter(ServletRequest request, ServletResponse response,
      FilterChain chain) throws IOException, ServletException {

    HttpServletRequest httpRequest = (HttpServletRequest) request;
    String token = httpRequest.getHeader("token");

    if (token != null && authCheck.verify(token)) {
      String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR");
      if (ipAddress == null) {
        ipAddress = httpRequest.getRemoteAddr();
      }
      log.info("Correct Auth; ip: " + ipAddress + "\n path: "
          + httpRequest.getContextPath());
      chain.doFilter(request, response);
    } else {
      log.info("Ivalid Token: " + token);
      HttpServletResponse resp = (HttpServletResponse) response;
      String error = "Invalid API KEY";
      resp.reset();
      resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentLength(error.length());
      response.getWriter().write(error);
    }
  }
}
