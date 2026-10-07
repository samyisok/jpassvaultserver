package com.samyisok.jpassvaultserver.controllers;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.samyisok.jpassvaultserver.domains.File;
import com.samyisok.jpassvaultserver.domains.FileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SpringBootTest
public class FileControllerNewFileTest {

  @MockitoBean
  FileRepository repository;

  @Mock
  File newFile;

  @Autowired
  FileController fileController;

  @BeforeEach
  void setUp() {
    when(newFile.getFile()).thenReturn("payload");
    when(repository.save(newFile)).thenReturn(newFile);
  }


  @Test
  void shouldSave() {
    File outputFile = fileController.newFile(newFile);
    assertEquals(newFile, outputFile);
    verify(repository, times(1)).save(newFile);
  }

}
