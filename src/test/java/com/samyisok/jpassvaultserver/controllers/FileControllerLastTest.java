package com.samyisok.jpassvaultserver.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import com.samyisok.jpassvaultserver.domains.File;
import com.samyisok.jpassvaultserver.domains.FileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.samyisok.jpassvaultserver.domains.FileNotFoundException;

@SpringBootTest
public class FileControllerLastTest {

  @MockBean
  FileRepository repository;

  @Mock
  File newFile;

  @Mock
  File secondFile;

  @Autowired
  FileController fileController;

  List<File> listOfFiles = new ArrayList<>();

  @BeforeEach
  void setUp() {
    listOfFiles.add(newFile);
    listOfFiles.add(secondFile);
    when(repository.findFirst1ByOrderByIdDesc()).thenReturn(listOfFiles);
  }

  @Test
  void shouldCallFindFirst1ByOrderByIdDesc() {
    File outputFile = fileController.last();
    assertEquals(newFile, outputFile);
    verify(repository, times(1)).findFirst1ByOrderByIdDesc();
  }


  @Test
  void shouldThrowException() {
    when(repository.findFirst1ByOrderByIdDesc()).thenReturn(new ArrayList<File>());
    assertThrows(FileNotFoundException.class, () -> fileController.last());
  }
}
