package com.samyisok.jpassvaultserver.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.samyisok.jpassvaultserver.persistence.File;
import com.samyisok.jpassvaultserver.services.VaultService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FileControllerDelegationUnitTest {

  @Mock
  VaultService vaultService;

  @InjectMocks
  FileController fileController;

  @Test
  void checkIsInline() {
    assertEquals("ok", fileController.check().get("check"));
  }

  @Test
  void checksumDelegatesToService() {
    when(vaultService.lastChecksum()).thenReturn("client-value");

    assertEquals("client-value", fileController.checksum().get("hash"));
  }

  @Test
  void newFileDelegatesToService() {
    File payload = new File("payload");
    when(vaultService.store(payload)).thenReturn(payload);

    assertEquals(payload, fileController.newFile(payload));
    verify(vaultService, times(1)).store(payload);
  }

  @Test
  void lastDelegatesToService() {
    File payload = new File("payload");
    when(vaultService.last()).thenReturn(payload);

    assertEquals(payload, fileController.last());
  }
}
