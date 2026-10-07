package com.samyisok.jpassvaultserver.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.samyisok.jpassvaultserver.AppProperties;
import com.samyisok.jpassvaultserver.domains.File;
import com.samyisok.jpassvaultserver.domains.FileRepository;
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
class FileControllerPayloadSizeUnitTest {

  @Mock
  FileRepository repository;

  @Mock
  AppProperties appProperties;

  @InjectMocks
  FileController fileController;

  @BeforeEach
  void setUp() {
    when(appProperties.getMaxPayloadSize()).thenReturn(10L);
  }

  @Test
  void rejectsOversizedUploadWithoutStoring() {
    File oversized = new File("0123456789A");

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> fileController.newFile(oversized));

    assertEquals(HttpStatus.CONTENT_TOO_LARGE, exception.getStatusCode());
    verify(repository, never()).save(any());
  }

  @Test
  void acceptsUploadWithinLimit() {
    File accepted = new File("small");
    when(repository.save(accepted)).thenReturn(accepted);

    File result = fileController.newFile(accepted);

    assertEquals(accepted, result);
    verify(repository, times(1)).save(accepted);
  }
}
