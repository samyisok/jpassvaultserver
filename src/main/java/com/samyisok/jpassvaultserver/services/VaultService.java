package com.samyisok.jpassvaultserver.services;

import com.samyisok.jpassvaultserver.AppProperties;
import com.samyisok.jpassvaultserver.persistence.File;
import com.samyisok.jpassvaultserver.persistence.FileRepository;
import com.samyisok.jpassvaultserver.security.Crypto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Application service for stored vault payloads: validates an upload, keeps only
 * the newest payload, and answers the change-detection lookup. Owns the use-case
 * rules so the controller only does HTTP.
 */
@Service
public class VaultService {
  private static final int MAX_CHECKSUM_LENGTH = 256;
  private static final String FILE_REQUIRED_MESSAGE = "file is required";
  private static final String PAYLOAD_TOO_LARGE_MESSAGE =
      "Vault payload exceeds the configured maximum";
  private static final String CHECKSUM_TOO_LARGE_MESSAGE =
      "Checksum exceeds the configured maximum";

  private final FileRepository repository;
  private final AppProperties appProperties;

  VaultService(FileRepository repository, AppProperties appProperties) {
    this.repository = repository;
    this.appProperties = appProperties;
  }

  @Transactional
  public File store(File newFile) {
    validate(newFile);
    File saved = repository.save(newFile);
    repository.deleteByIdNot(saved.getId());
    return saved;
  }

  public File last() {
    File file = newestOrNull();
    if (file == null) {
      throw new FileNotFoundException();
    }
    return file;
  }

  public String lastChecksum() {
    File file = newestOrNull();
    if (file == null) {
      return "";
    }
    String storedChecksum = file.getChecksum();
    if (storedChecksum != null && !storedChecksum.isBlank()) {
      return storedChecksum;
    }
    return Crypto.sha256Hex(file.getFile());
  }

  private File newestOrNull() {
    return repository.findFirstByOrderByIdDesc().orElse(null);
  }

  private void validate(File newFile) {
    String payload = newFile.getFile();
    if (payload == null || payload.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, FILE_REQUIRED_MESSAGE);
    }
    if (payload.length() > appProperties.getMaxPayloadSize()) {
      throw new ResponseStatusException(HttpStatus.CONTENT_TOO_LARGE,
          PAYLOAD_TOO_LARGE_MESSAGE);
    }
    String checksum = newFile.getChecksum();
    if (checksum != null && checksum.length() > MAX_CHECKSUM_LENGTH) {
      throw new ResponseStatusException(HttpStatus.CONTENT_TOO_LARGE,
          CHECKSUM_TOO_LARGE_MESSAGE);
    }
  }
}
