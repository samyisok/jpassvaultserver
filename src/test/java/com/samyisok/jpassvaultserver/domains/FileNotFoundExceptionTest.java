package com.samyisok.jpassvaultserver.domains;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
public class FileNotFoundExceptionTest {

  @Test
  void shouldGetMessage() {
    FileNotFoundException ex = new FileNotFoundException();
    assertEquals("Could not find file", ex.getMessage());
  }

  @Test
  void shouldGetMessageWithId() {
    long id = 42;
    FileNotFoundException ex = new FileNotFoundException(id);
    assertEquals("Could not find file " + id, ex.getMessage());
  }


}
