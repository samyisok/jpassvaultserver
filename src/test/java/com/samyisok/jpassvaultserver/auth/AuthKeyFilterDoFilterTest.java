package com.samyisok.jpassvaultserver.auth;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SpringBootTest
public class AuthKeyFilterDoFilterTest {

  @Mock
  Logger logger;

  @MockitoSpyBean
  AuthKeyFilter authKeyFilter;

  @MockitoSpyBean
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
    when(authCheck.getKey()).thenReturn(token);
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
    when(authCheck.getKey()).thenReturn("another-token");
    authKeyFilter.doFilter(request, response, chain);
    verify(authKeyFilter, times(1)).getLogger();
    verify(logger, times(1)).info("Invalid Token: " + token);
  }

  @Test
  void shouldCallResponseWithError() throws Exception {
    when(authCheck.verify(token)).thenReturn(false);
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
