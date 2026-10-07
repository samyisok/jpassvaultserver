package com.samyisok.jpassvaultserver.auth;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import com.samyisok.jpassvaultserver.AppProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthCheckConstantTimeUnitTest {

  @Mock
  AppProperties appProperties;

  @InjectMocks
  AuthCheck authCheck;

  @BeforeEach
  void setUp() {
    when(appProperties.getUseSecretKeyFromEnv()).thenReturn(true);
    when(appProperties.getEnvSecretKey()).thenReturn("correct-token");
  }

  @Test
  void acceptsCorrectToken() {
    assertTrue(authCheck.verify("correct-token"));
  }

  @Test
  void rejectsTokenDifferingInAnyPosition() {
    assertFalse(authCheck.verify("correct-toker"));
    assertFalse(authCheck.verify("orrect-token"));
    assertFalse(authCheck.verify(""));
    assertFalse(authCheck.verify(null));
  }
}
