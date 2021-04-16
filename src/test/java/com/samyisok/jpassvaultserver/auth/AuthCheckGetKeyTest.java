package com.samyisok.jpassvaultserver.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import com.samyisok.jpassvaultserver.AppProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class AuthCheckGetKeyTest {

  @MockBean
  private AppProperties appProperties;

  @Autowired
  private AuthCheck authCheck;

  private String envToken = "env-token";
  private String localToken = "local-token";

  @BeforeEach
  public void setUp() {
    when(appProperties.getUseSecretKeyFromEnv()).thenReturn(true);
    when(appProperties.getEnvSecretKey()).thenReturn(envToken);
    when(appProperties.getSecretKey()).thenReturn(localToken);

  }

  @Test
  void getKey() {
    assertEquals(authCheck.getKey(), envToken);
    verify(appProperties, times(1)).getEnvSecretKey();
    verify(appProperties, times(1)).getUseSecretKeyFromEnv();
    verify(appProperties, never()).getSecretKey();
  }

  @Test
  void getLocalKey() {
    when(appProperties.getUseSecretKeyFromEnv()).thenReturn(false);
    assertEquals(authCheck.getKey(), localToken);
    verify(appProperties, never()).getEnvSecretKey();
    verify(appProperties, times(1)).getUseSecretKeyFromEnv();
    verify(appProperties, times(1)).getSecretKey();
  }
}
