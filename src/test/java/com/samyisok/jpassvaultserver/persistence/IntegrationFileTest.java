package com.samyisok.jpassvaultserver.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
public class IntegrationFileTest {
  String base64 = "test-string";
  File file;
  File outputFile;
  Instant nowDate;
  Instant shouldBeforeDate;

  @Autowired
  FileRepository repository;


  @BeforeEach
  void setUp() {
    nowDate = Instant.now();
    shouldBeforeDate = nowDate.plusSeconds(3600);
    this.file = new File(base64);
    repository.save(this.file);
    this.outputFile =
        repository.findFirstByOrderByIdDesc().orElseThrow();
  }

  @Test
  void shouldCreateFile() {
    File newFile = new File(base64);
    assertNotNull(newFile);
  }

  @Test
  void shouldGetId() {
    assertEquals(file.getId(), outputFile.getId());
  }

  @Test
  void shouldGetFile() {
    assertEquals(base64, outputFile.getFile());
  }

  @Test
  void shouldSetFile() {
    String newData = "newData";
    outputFile.setFile(newData);
    File newFile = repository.save(outputFile);
    assertEquals(newData, newFile.getFile());
  }

  @Test
  void shouldGetHashCode() {
    assertEquals(file.getId().hashCode(), outputFile.hashCode());
  }

  @Test
  void equalsShouldWork() {
    assertTrue(outputFile.equals(file));
    assertTrue(outputFile.equals(outputFile));
    assertFalse(outputFile.equals(new File(base64)));
    assertFalse(outputFile.equals(new Object()));
    assertFalse(outputFile.equals(null));
  }

  @Test
  void toStringShouldReturnIdRepr() {
    assertEquals(outputFile.toString(), "File [id=" + outputFile.getId() + "]");
  }

  @Test
  void shouldReturnCreatedDate() {
    Instant date = Instant.parse(outputFile.getCreatedDate().toString());
    assertNotNull(date);
    assertTrue(date.isAfter(nowDate));
    assertTrue(date.isBefore(shouldBeforeDate));
  }

  @Test
  void shouldReturnModifiedDate() {
    Instant date = Instant.parse(outputFile.getModifiedDate().toString());
    assertNotNull(date);
    assertTrue(date.isAfter(nowDate));
    assertTrue(date.isBefore(shouldBeforeDate));
  }
}
