package com.samyisok.jpassvaultserver.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.samyisok.jpassvaultserver.persistence.File;
import com.samyisok.jpassvaultserver.persistence.FileRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class VaultRetentionIntegrationTest {

  @Autowired
  VaultService vaultService;

  @Autowired
  FileRepository repository;

  @BeforeEach
  void cleanDatabase() {
    repository.deleteAll();
  }

  @Test
  void keepsOnlyTheNewestPayload() {
    vaultService.store(new File("first", "checksum-1"));
    vaultService.store(new File("second", "checksum-2"));

    List<File> all = repository.findAll();

    assertEquals(1, all.size());
    assertEquals("second", all.get(0).getFile());
    assertEquals("second", vaultService.last().getFile());
    assertEquals("checksum-2", vaultService.lastChecksum());
  }
}
