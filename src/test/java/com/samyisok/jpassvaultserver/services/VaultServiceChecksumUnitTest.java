package com.samyisok.jpassvaultserver.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import com.samyisok.jpassvaultserver.AppProperties;
import com.samyisok.jpassvaultserver.persistence.File;
import com.samyisok.jpassvaultserver.persistence.FileRepository;
import com.samyisok.jpassvaultserver.security.Crypto;
import java.util.Optional;
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
class VaultServiceChecksumUnitTest {

  @Mock
  FileRepository repository;

  @Mock
  AppProperties appProperties;

  @InjectMocks
  VaultService vaultService;

  @BeforeEach
  void setUp() {
    when(repository.findFirstByOrderByIdDesc()).thenReturn(Optional.empty());
  }

  @Test
  void returnsEmptyWhenNoPayloadStored() {
    assertEquals("", vaultService.lastChecksum());
  }

  @Test
  void echoesClientSuppliedChecksumVerbatim() {
    File stored = new File("payload", "client-hmac-value");
    when(repository.findFirstByOrderByIdDesc()).thenReturn(Optional.of(stored));

    assertEquals("client-hmac-value", vaultService.lastChecksum());
  }

  @Test
  void fallsBackToSha256WhenChecksumMissing() {
    File stored = new File("payload");
    when(repository.findFirstByOrderByIdDesc()).thenReturn(Optional.of(stored));

    assertEquals(Crypto.sha256Hex("payload"), vaultService.lastChecksum());
  }
}
