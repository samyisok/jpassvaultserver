package com.samyisok.jpassvaultserver.controllers;

import java.util.Map;
import com.samyisok.jpassvaultserver.persistence.File;
import com.samyisok.jpassvaultserver.services.VaultService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
class FileController {

  private static final String HASH_FIELD = "hash";

  private final VaultService vaultService;

  FileController(VaultService vaultService) {
    this.vaultService = vaultService;
  }

  @GetMapping("/check")
  Map<String, String> check() {
    return Map.of("check", "ok");
  }

  @GetMapping("/files/last/checksum")
  Map<String, String> checksum() {
    return Map.of(HASH_FIELD, vaultService.lastChecksum());
  }

  @PostMapping("/files")
  File newFile(@RequestBody File newFile) {
    return vaultService.store(newFile);
  }

  @GetMapping("/files/last")
  File last() {
    return vaultService.last();
  }
}
