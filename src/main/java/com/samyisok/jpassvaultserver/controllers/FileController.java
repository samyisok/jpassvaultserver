package com.samyisok.jpassvaultserver.controllers;



import java.util.Map;
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
