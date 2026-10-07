package com.samyisok.jpassvaultserver.advices;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import com.samyisok.jpassvaultserver.services.FileNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SpringBootTest
public class FileNotFoundAdviceTest {

  @Autowired
  FileNotFoundAdvice fileNotFoundAdvice;

  @Mock
  FileNotFoundException fileNotFoundException;

  @BeforeEach
  void setUp() {
    when(fileNotFoundException.getMessage()).thenReturn("expected message");
  }

  @Test
  void shouldReturnGetMessage() {
    String message = fileNotFoundAdvice.handle(fileNotFoundException);
    assertEquals("expected message", message);
  }

}
