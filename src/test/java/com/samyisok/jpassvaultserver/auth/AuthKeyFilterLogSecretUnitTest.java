package com.samyisok.jpassvaultserver.auth;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.samyisok.jpassvaultserver.AppProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.Logger;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthKeyFilterLogSecretUnitTest {

  @Mock
  AuthCheck authCheck;

  @Mock
  AuthThrottle authThrottle;

  @Mock
  AppProperties appProperties;

  @Mock
  Logger logger;

  @Mock
  HttpServletRequest request;

  @Mock
  HttpServletResponse response;

  @Mock
  FilterChain chain;

  AuthKeyFilter authKeyFilter;

  @BeforeEach
  void setUp() throws Exception {
    authKeyFilter = spy(new AuthKeyFilter(authCheck, authThrottle, appProperties));
    doReturn(logger).when(authKeyFilter).getLogger();
    doNothing().when(authKeyFilter).responseWithError(any(ServletResponse.class));
    when(appProperties.getTrustProxyHeaders()).thenReturn(false);
    when(request.getRemoteAddr()).thenReturn("10.0.0.1");
    when(authThrottle.isBlocked("10.0.0.1")).thenReturn(false);
  }

  @Test
  void doesNotWriteSubmittedTokenToLogs() throws Exception {
    String submittedToken = "submitted-token-value";
    when(request.getHeader("token")).thenReturn(submittedToken);
    when(authCheck.verify(submittedToken)).thenReturn(false);

    authKeyFilter.doFilter(request, response, chain);

    ArgumentCaptor<String> messages = ArgumentCaptor.forClass(String.class);
    verify(logger, times(1)).info(messages.capture());
    String logged = messages.getValue();
    assertFalse(logged.contains(submittedToken));
    assertTrue(logged.contains(AuthKeyFilter.INVALID_CREDENTIAL_EVENT));
    assertTrue(logged.contains("10.0.0.1"));
  }
}
