package com.samyisok.jpassvaultserver.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.samyisok.jpassvaultserver.AppProperties;
import com.samyisok.jpassvaultserver.persistence.File;
import com.samyisok.jpassvaultserver.persistence.FileRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class VaultServiceLastUnitTest {

  @Mock
  FileRepository repository;

  @Mock
  AppProperties appProperties;

  @InjectMocks
  VaultService vaultService;

  @Test
  void returnsNewestStoredPayload() {
    File newest = new File("newest");
    when(repository.findFirstByOrderByIdDesc()).thenReturn(Optional.of(newest));

    assertEquals(newest, vaultService.last());
    verify(repository, times(1)).findFirstByOrderByIdDesc();
  }

  @Test
  void throwsWhenNothingStored() {
    when(repository.findFirstByOrderByIdDesc()).thenReturn(Optional.empty());

    assertThrows(FileNotFoundException.class, () -> vaultService.last());
  }
}
