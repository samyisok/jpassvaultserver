package com.samyisok.jpassvaultserver.domains;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<File, Long> {

  List<File> findFirst1ByOrderByIdDesc();

  long deleteByIdNot(Long id);
}
