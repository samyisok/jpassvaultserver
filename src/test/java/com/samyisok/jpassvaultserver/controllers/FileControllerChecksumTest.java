package com.samyisok.jpassvaultserver.controllers;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.samyisok.jpassvaultserver.domains.File;
import com.samyisok.jpassvaultserver.domains.FileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SpringBootTest
public class FileControllerChecksumTest {

  @MockitoBean
  FileRepository repository;

  @Mock
  File newFile;

  @Autowired
  FileController fileController;

  List<File> emptyListOfFiles = new ArrayList<>();

  @BeforeEach
  void setUp() {
    when(repository.findFirst1ByOrderByIdDesc()).thenReturn(emptyListOfFiles);
  }

  @Test
  void shouldReturnEmptyHashIfEmpty() {
    Map<String, String> result = fileController.checksum();
    assertTrue(result.containsKey("hash"));
    assertTrue(result.get("hash").equals(""));
  }

  @Test
  void shouldReturnHashIfExist() {
    List<File> listOfFiles = new ArrayList<>();
    String file = "file";
    when(newFile.getFile()).thenReturn(file);
    listOfFiles.add(newFile);
    when(repository.findFirst1ByOrderByIdDesc()).thenReturn(listOfFiles);

    Map<String, String> result = fileController.checksum();

    assertTrue(result.containsKey("hash"));
    assertTrue(result.get("hash").equals("8C7DD922AD47494FC02C388E12C00EAC"));
  }


  @Test
  void shouldReturnEmptyIfException() {
    List<File> listOfFiles = new ArrayList<>();
    String file = "file";
    when(newFile.getFile()).thenReturn(file);
    listOfFiles.add(newFile);
    when(repository.findFirst1ByOrderByIdDesc()).thenReturn(listOfFiles);

    try (
        MockedStatic<MessageDigest> md = Mockito.mockStatic(MessageDigest.class)) {
      md.when(() -> MessageDigest.getInstance("MD5"))
          .thenThrow(NoSuchAlgorithmException.class);
      Map<String, String> result = fileController.checksum();

      assertTrue(result.containsKey("hash"));
      assertTrue(result.get("hash").equals(""));
    }
  }

}


