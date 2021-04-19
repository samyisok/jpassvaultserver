package com.samyisok.jpassvaultserver.advices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import com.samyisok.jpassvaultserver.domains.FileNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
public class FileNotFoundAdviceTest {

  @Autowired
  AuthTokenAdvice authTokenAdvice;

  @Mock
  FileNotFoundException fileNotFoundException;

  @BeforeEach
  void setUp() {
    when(fileNotFoundException.getMessage()).thenReturn("expected message");
  }

  @Test
  void shouldReturnGetMessage() {
    String message = authTokenAdvice.AuthTokenHandler(fileNotFoundException);
    assertEquals("expected message", message);
  }

}
