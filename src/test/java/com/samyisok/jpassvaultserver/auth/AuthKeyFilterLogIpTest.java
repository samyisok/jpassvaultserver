package com.samyisok.jpassvaultserver.auth;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.mockito.MockitoAnnotations;


@SpringBootTest
class AuthKeyFilterLogIpTest {

  @Mock
  Logger log;

  @SpyBean
  AuthKeyFilter authKeyFilter;

  @Mock
  HttpServletRequest request;

  private String ip = "168.1.1.1";
  private String contextPath = "/path";

  @BeforeEach
  public void setUp() throws Exception {
    when(request.getHeader(anyString())).thenReturn(ip);
    when(request.getRemoteAddr()).thenReturn(ip);
    when(request.getContextPath()).thenReturn(contextPath);
    MockitoAnnotations.initMocks(this);
    // when(logger.isDebugEnabled()).thenReturn(false);)

  }

  @Test
  void shouldCallGetHeader() throws Exception {
    assertDoesNotThrow(() -> authKeyFilter.logIp(request));
    verify(request).getHeader(AuthKeyFilter.HEADER);
    verify(request, never()).getRemoteAddr();
    // verify(logger, times(1)).info("Correct Auth; ip: " + ip + "\n path: "
    // + contextPath);
    // authKeyFilter.verify(logger).info("1");
  }

}
