package com.samyisok.jpassvaultserver.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<File, Long> {

  Optional<File> findFirstByOrderByIdDesc();

  long deleteByIdNot(Long id);
}
