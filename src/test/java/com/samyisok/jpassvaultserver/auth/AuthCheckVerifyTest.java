package com.samyisok.jpassvaultserver.auth;


import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

@SpringBootTest
class AuthCheckVerifyTest {
  @SpyBean
  private AuthCheck authCheck;

  private String envToken = "env-token";

  @BeforeEach
  public void setUp() {
    reset();
    when(authCheck.getKey()).thenReturn(envToken);
  }

  @Test
  void verifyShouldBeTrue() {
    assertTrue(authCheck.verify(envToken));
  }

  @Test
  void verifyShouldBeFalse() {
    String emptyToken = "";
    assertFalse(authCheck.verify(emptyToken));
  }

  @Test
  void verifyShouldThrowException() {
    when(authCheck.getKey()).thenReturn(null);
    assertThrows(RuntimeException.class, () -> authCheck.verify(envToken));
    verify(authCheck, times(1)).getKey();
  }
}
