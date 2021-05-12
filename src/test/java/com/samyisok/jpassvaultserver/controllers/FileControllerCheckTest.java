package com.samyisok.jpassvaultserver.controllers;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class FileControllerCheckTest {

  @Autowired
  FileController fileController;

  @Test
  void shouldCallFindFirst1ByOrderByIdDesc() {
    Map<String, String> result = fileController.check();
    assertTrue(result.containsKey("check"));
    assertTrue(result.get("check").equals("ok"));
  }
}


