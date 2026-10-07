package com.samyisok.jpassvaultserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class JpassvaultserverApplicationUnitTest {

  @AfterEach
  void clearProperty() {
    System.clearProperty("spring.datasource.url");
  }

  @Test
  void usesArgumentWhenPresent() {
    assertEquals("jdbc:h2:file:/from/arg",
        JpassvaultserverApplication.resolveDatasourceUrl(
            new String[] {"--spring.datasource.url=jdbc:h2:file:/from/arg"}));
  }

  @Test
  void argumentTakesPrecedenceOverProperty() {
    System.setProperty("spring.datasource.url", "jdbc:h2:file:/from/property");

    assertEquals("jdbc:h2:file:/from/arg",
        JpassvaultserverApplication.resolveDatasourceUrl(
            new String[] {"--spring.datasource.url=jdbc:h2:file:/from/arg"}));
  }

  @Test
  void usesSystemPropertyWhenNoArgument() {
    System.setProperty("spring.datasource.url", "jdbc:h2:file:/from/property");

    assertEquals("jdbc:h2:file:/from/property",
        JpassvaultserverApplication.resolveDatasourceUrl(new String[] {}));
  }

  @Test
  void fallsBackToDefault() {
    assertEquals(JpassvaultserverApplication.DEFAULT_DATASOURCE_URL,
        JpassvaultserverApplication.resolveDatasourceUrl(new String[] {}));
  }
}
