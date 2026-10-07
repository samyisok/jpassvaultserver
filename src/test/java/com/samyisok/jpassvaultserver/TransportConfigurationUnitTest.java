package com.samyisok.jpassvaultserver;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class TransportConfigurationUnitTest {

  @Test
  void failsWithoutTlsAndWithoutPlainHttpAcknowledgment() {
    AppProperties properties = new AppProperties();

    assertThrows(IllegalStateException.class,
        () -> properties.validateTransport(false, false));
  }

  @Test
  void allowsTlsConfigured() {
    AppProperties properties = new AppProperties();

    assertDoesNotThrow(() -> properties.validateTransport(true, false));
  }

  @Test
  void allowsExplicitPlainHttpAcknowledgment() {
    AppProperties properties = new AppProperties();

    assertDoesNotThrow(() -> properties.validateTransport(false, true));
  }
}
