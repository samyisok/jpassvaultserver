package com.samyisok.jpassvaultserver.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.samyisok.jpassvaultserver.AppProperties;
import com.samyisok.jpassvaultserver.persistence.File;
import com.samyisok.jpassvaultserver.persistence.FileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class VaultServiceStoreUnitTest {

  @Mock
  FileRepository repository;

  @Mock
  AppProperties appProperties;

  @InjectMocks
  VaultService vaultService;

  @BeforeEach
  void setUp() {
    when(appProperties.getMaxPayloadSize()).thenReturn(10L);
  }

  @Test
  void rejectsOversizedUploadWithoutStoring() {
    File oversized = new File("0123456789A");

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> vaultService.store(oversized));

    assertEquals(HttpStatus.CONTENT_TOO_LARGE, exception.getStatusCode());
    verify(repository, never()).save(any());
  }

  @Test
  void rejectsOversizedChecksumWithoutStoring() {
    File oversized = new File("small", "c".repeat(257));

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> vaultService.store(oversized));

    assertEquals(HttpStatus.CONTENT_TOO_LARGE, exception.getStatusCode());
    verify(repository, never()).save(any());
  }

  @Test
  void rejectsBlankPayloadWithBadRequest() {
    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> vaultService.store(new File("   ")));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    verify(repository, never()).save(any());
  }

  @Test
  void rejectsNullPayloadWithBadRequest() {
    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> vaultService.store(new File(null)));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    verify(repository, never()).save(any());
  }

  @Test
  void acceptsUploadWithinLimit() {
    File accepted = new File("small");
    when(repository.save(accepted)).thenReturn(accepted);

    File result = vaultService.store(accepted);

    assertEquals(accepted, result);
    verify(repository, times(1)).save(accepted);
  }

  @Test
  void keepsOnlyNewestByDeletingOtherRows() {
    File accepted = new File("small");
    when(repository.save(accepted)).thenReturn(accepted);

    vaultService.store(accepted);

    verify(repository, times(1)).deleteByIdNot(accepted.getId());
  }
}
