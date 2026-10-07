package com.samyisok.jpassvaultserver;

import com.samyisok.jpassvaultserver.persistence.DatabasePermissions;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
class LoadDatabase {
  @Bean
  CommandLineRunner initDatabase(Environment environment) {
    return args -> DatabasePermissions.applyOwnerOnlyPermissions(
        DatabasePermissions.h2DatabaseFile(environment.getProperty("spring.datasource.url")));
  }
}
