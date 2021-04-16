package com.samyisok.jpassvaultserver.auth;


import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import com.samyisok.jpassvaultserver.AppProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class AuthCheckVerifyTest {

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
  void verify() {
    assertTrue(authCheck.verify(envToken));
  }

  @Test
  void verifyShouldBeFalse() {
    String emptyToken = "";
    assertFalse(authCheck.verify(emptyToken));
  }

  @Test
  void verifyShouldThrowException() {
    when(appProperties.getEnvSecretKey()).thenReturn(null);
    assertThrows(RuntimeException.class, () -> authCheck.verify(envToken));
  }
}
