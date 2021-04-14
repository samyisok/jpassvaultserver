package com.samyisok.jpassvaultserver.controllers;


import java.util.List;
import com.samyisok.jpassvaultserver.domains.File;
import com.samyisok.jpassvaultserver.domains.FileRepository;
import com.samyisok.jpassvaultserver.domains.FileNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
class FileController {

  private final FileRepository repository;

  FileController(FileRepository repository) {
    this.repository = repository;
  }

  // Aggregate root
  // tag::get-aggregate-root[]
  @GetMapping("/files")
  List<File> all() {
    return repository.findAll();
  }
  // end::get-aggregate-root[]

  @PostMapping("/files")
  File newFile(@RequestBody File newFile) {
    return repository.save(newFile);
  }

  // Single item

  @GetMapping("/files/{id}")
  File one(@PathVariable Long id) {

    return repository.findById(id)
        .orElseThrow(() -> new FileNotFoundException(id));
  }
}
