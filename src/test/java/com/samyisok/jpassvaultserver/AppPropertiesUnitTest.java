package com.samyisok.jpassvaultserver;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class AppPropertiesUnitTest {

  @Test
  void rejectsMissingSecret() {
    AppProperties properties = new AppProperties();

    assertThrows(IllegalStateException.class, () -> properties.validateSecret(null));
    assertThrows(IllegalStateException.class, () -> properties.validateSecret("   "));
  }

  @Test
  void rejectsPreviouslyCommittedPlaceholder() {
    AppProperties properties = new AppProperties();

    assertThrows(IllegalStateException.class,
        () -> properties.validateSecret(AppProperties.PLACEHOLDER_SECRET));
  }

  @Test
  void acceptsConfiguredSecret() {
    AppProperties properties = new AppProperties();

    assertDoesNotThrow(() -> properties.validateSecret("a-real-secret"));
  }

  @Test
  void masksSecretInToString() {
    AppProperties properties = new AppProperties();
    properties.setSecretKey("super-secret");
    properties.setUseSecretKeyFromEnv(false);

    String dump = properties.toString();

    assertFalse(dump.contains("super-secret"));
    assertTrue(dump.contains("****"));
  }
}
