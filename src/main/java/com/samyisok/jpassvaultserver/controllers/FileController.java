package com.samyisok.jpassvaultserver.controllers;



import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import javax.xml.bind.DatatypeConverter;
import com.samyisok.jpassvaultserver.domains.File;
import com.samyisok.jpassvaultserver.domains.FileNotFoundException;
import com.samyisok.jpassvaultserver.domains.FileRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;



@RestController
class FileController {

  private final FileRepository repository;

  FileController(FileRepository repository) {
    this.repository = repository;
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
      return Map.of("hash", "");
    }

    String fileDb = file.getFile();
    MessageDigest md;
    String checksum = "";
    try {
      md = MessageDigest.getInstance("MD5");
      md.update(fileDb.getBytes());
      byte[] digest = md.digest();
      checksum = DatatypeConverter
          .printHexBinary(digest).toUpperCase();
    } catch (NoSuchAlgorithmException e) {
    }

    return Map.of("hash", checksum);
  }

  @PostMapping("/files")
  File newFile(@RequestBody File newFile) {
    return repository.save(newFile);
  }

  @GetMapping("/files/last")
  File last() {
    return repository.findFirst1ByOrderByIdDesc().stream().findFirst()
        .orElseThrow(() -> new FileNotFoundException());
  }
}
