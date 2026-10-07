package com.samyisok.jpassvaultserver.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class FileNotFoundExceptionUnitTest {

  @Test
  void hasStableMessage() {
    assertEquals("Could not find file", new FileNotFoundException().getMessage());
  }
}
