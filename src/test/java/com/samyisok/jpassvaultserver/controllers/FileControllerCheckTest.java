package com.samyisok.jpassvaultserver.controllers;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class FileControllerCheckTest {

  // @MockBean
  // FileRepository repository;

  // @Mock
  // File newFile;

  // @Mock
  // File secondFile;

  @Autowired
  FileController fileController;

  // List<File> listOfFiles = new ArrayList<>();

  @BeforeEach
  void setUp() {
    // listOfFiles.add(newFile);
    // listOfFiles.add(secondFile);
    // when(repository.findFirst1ByOrderByIdDesc()).thenReturn(listOfFiles);
  }

  @Test
  void shouldCallFindFirst1ByOrderByIdDesc() {
    Map<String, String> result = fileController.check();
    assertTrue(result.containsKey("check"));
    assertTrue(result.get("check").equals("ok"));
  }
}


