package com.samyisok.jpassvaultserver.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import com.samyisok.jpassvaultserver.AppProperties;
import com.samyisok.jpassvaultserver.domains.File;
import com.samyisok.jpassvaultserver.domains.FileRepository;
import com.samyisok.jpassvaultserver.security.Crypto;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FileControllerChecksumUnitTest {

  @Mock
  FileRepository repository;

  @Mock
  AppProperties appProperties;

  @InjectMocks
  FileController fileController;

  @BeforeEach
  void setUp() {
    when(repository.findFirst1ByOrderByIdDesc()).thenReturn(List.of());
  }

  @Test
  void echoesClientSuppliedChecksumVerbatim() {
    File stored = new File("payload", "client-hmac-value");
    when(repository.findFirst1ByOrderByIdDesc()).thenReturn(List.of(stored));

    assertEquals("client-hmac-value", fileController.checksum().get("hash"));
  }

  @Test
  void fallsBackToSha256WhenChecksumMissing() {
    File stored = new File("payload");
    when(repository.findFirst1ByOrderByIdDesc()).thenReturn(List.of(stored));

    assertEquals(Crypto.sha256Hex("payload"), fileController.checksum().get("hash"));
  }

  @Test
  void storesChecksumOnTheEntity() {
    File stored = new File("payload", "client-hmac-value");

    assertEquals("client-hmac-value", stored.getChecksum());
    assertEquals("payload", stored.getFile());
  }
}
