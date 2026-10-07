package com.samyisok.jpassvaultserver.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SpringBootTest
class ErrorHygieneUnitTest {

  @Autowired
  Environment environment;

  @Mock
  HttpServletRequest request;

  @Mock
  HttpServletResponse response;

  @Mock
  FilterChain chain;

  @Test
  void addsNoSniffAndNoStoreHeaders() throws Exception {
    new SecurityHeadersFilter().doFilterInternal(request, response, chain);

    verify(response, times(1)).setHeader("X-Content-Type-Options", "nosniff");
    verify(response, times(1)).setHeader("Cache-Control", "no-store");
    verify(chain, times(1)).doFilter(request, response);
  }

  @Test
  void hidesInternalErrorDetails() {
    assertEquals("never", environment.getProperty("server.error.include-message"));
    assertEquals("never", environment.getProperty("server.error.include-stacktrace"));
    assertEquals("false", environment.getProperty("server.error.include-exception"));
  }
}
