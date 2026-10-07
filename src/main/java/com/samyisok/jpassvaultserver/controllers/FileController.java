package com.samyisok.jpassvaultserver.controllers;

import java.util.Map;
import com.samyisok.jpassvaultserver.AppProperties;
import com.samyisok.jpassvaultserver.domains.File;
import com.samyisok.jpassvaultserver.domains.FileNotFoundException;
import com.samyisok.jpassvaultserver.domains.FileRepository;
import com.samyisok.jpassvaultserver.security.Crypto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
class FileController {

  private static final String HASH_FIELD = "hash";
  private static final int MAX_CHECKSUM_LENGTH = 256;
  private static final String PAYLOAD_TOO_LARGE_MESSAGE =
      "Vault payload exceeds the configured maximum";

  private final FileRepository repository;
  private final AppProperties appProperties;

  FileController(FileRepository repository, AppProperties appProperties) {
    this.repository = repository;
    this.appProperties = appProperties;
  }

  @GetMapping("/check")
  Map<String, String> check() {
    return Map.of("check", "ok");
  }

  @GetMapping("/files/last/checksum")
  Map<String, String> checksum() {
    File file =
        repository.findFirst1ByOrderByIdDesc().stream().findFirst().orElse(null);

    if (file == null) {
      return Map.of(HASH_FIELD, "");
    }

    String storedChecksum = file.getChecksum();
    if (storedChecksum != null && !storedChecksum.isBlank()) {
      return Map.of(HASH_FIELD, storedChecksum);
    }

    return Map.of(HASH_FIELD, Crypto.sha256Hex(file.getFile()));
  }

  @PostMapping("/files")
  File newFile(@RequestBody File newFile) {
    ensureWithinPayloadLimit(newFile);
    return repository.save(newFile);
  }

  @GetMapping("/files/last")
  File last() {
    return repository.findFirst1ByOrderByIdDesc().stream().findFirst()
        .orElseThrow(() -> new FileNotFoundException());
  }

  private void ensureWithinPayloadLimit(File newFile) {
    String payload = newFile.getFile();
    if (payload != null && payload.length() > appProperties.getMaxPayloadSize()) {
      throw new ResponseStatusException(HttpStatus.CONTENT_TOO_LARGE,
          PAYLOAD_TOO_LARGE_MESSAGE);
    }
    String checksum = newFile.getChecksum();
    if (checksum != null && checksum.length() > MAX_CHECKSUM_LENGTH) {
      throw new ResponseStatusException(HttpStatus.CONTENT_TOO_LARGE,
          PAYLOAD_TOO_LARGE_MESSAGE);
    }
  }
}
