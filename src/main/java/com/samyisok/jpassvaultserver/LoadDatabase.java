package com.samyisok.jpassvaultserver;

import com.samyisok.jpassvaultserver.domains.FileRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
class LoadDatabase {
  @Bean
  CommandLineRunner initDatabase(FileRepository repository, AppProperties properties) {
    return args -> {
    };
  }
}
