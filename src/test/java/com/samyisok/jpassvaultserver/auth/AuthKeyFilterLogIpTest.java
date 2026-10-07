package com.samyisok.jpassvaultserver.auth;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SpringBootTest
class AuthKeyFilterLogIpTest {

  @Mock
  Logger logger;

  @MockitoSpyBean
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
    when(authKeyFilter.getLogger()).thenReturn(logger);
  }

  @Test
  void shouldIgnoreForwardedHeaderByDefault() throws Exception {
    assertDoesNotThrow(() -> authKeyFilter.logIp(request));

    verify(request, never()).getHeader(AuthKeyFilter.HEADER);
    verify(request, times(1)).getRemoteAddr();
    verify(request, times(1)).getContextPath();
    verify(logger, times(1)).info("Correct Auth; ip: " + ip + "\n path: "
        + contextPath);
  }

  @Test
  void shouldUseForwardedHeaderWhenTrustEnabled() {
    when(request.getHeader(AuthKeyFilter.HEADER)).thenReturn(ip);

    String address = authKeyFilter.resolveClientAddress(request, true);

    verify(request, times(1)).getHeader(AuthKeyFilter.HEADER);
    assertEquals(ip, address);
  }

}
