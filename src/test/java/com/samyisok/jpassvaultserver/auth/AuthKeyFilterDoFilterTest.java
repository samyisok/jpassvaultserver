package com.samyisok.jpassvaultserver.auth;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

@SpringBootTest
public class AuthKeyFilterDoFilterTest {

  @Mock
  Logger logger;

  @SpyBean
  AuthKeyFilter authKeyFilter;

  @SpyBean
  AuthCheck authCheck;

  @Mock
  HttpServletRequest request;

  @Mock
  HttpServletResponse response;

  @Mock
  FilterChain chain;

  private String token = "test-token";

  @BeforeEach
  public void setUp() throws Exception {
    when(request.getHeader(anyString())).thenReturn(token);
    when(authKeyFilter.getLogger()).thenReturn(logger);
    doNothing().when(authKeyFilter).logIp(request);
    doNothing().when(authKeyFilter).responseWithError(response);;
  }

  @Test
  void shouldCallGetHeader() throws Exception {
    authKeyFilter.doFilter(request, response, chain);
    verify(request, times(1)).getHeader("token");
  }

  @Test
  void shouldCallVerify() throws Exception {
    authKeyFilter.doFilter(request, response, chain);
    verify(authCheck, times(1)).verify(token);
  }

  @Test
  void shouldCallLogIpIfTokenIsVerified() throws Exception {
    when(authCheck.verify(token)).thenReturn(true);
    authKeyFilter.doFilter(request, response, chain);
    verify(authKeyFilter, times(1)).logIp(request);
  }

  @Test
  void shouldCallDoFilterIfVerified() throws Exception {
    when(authCheck.verify(token)).thenReturn(true);
    authKeyFilter.doFilter(request, response, chain);
    verify(chain, times(1)).doFilter(request, response);
  }

  @Test
  void shouldCallInfo() throws Exception {
    authKeyFilter.doFilter(request, response, chain);
    verify(authKeyFilter, times(1)).getLogger();
    verify(logger, times(1)).info("Invalid Token: " + token);
  }

  @Test
  void shouldCallResponseWithError() throws Exception {
    authKeyFilter.doFilter(request, response, chain);
    verify(authKeyFilter, times(1)).responseWithError(response);
  }

  @Test
  void shouldCallResponseWithErrorIfTokenIsNull() throws Exception {
    when(request.getHeader(anyString())).thenReturn(null);
    authKeyFilter.doFilter(request, response, chain);
    verify(authKeyFilter, times(1)).responseWithError(response);
  }
}
