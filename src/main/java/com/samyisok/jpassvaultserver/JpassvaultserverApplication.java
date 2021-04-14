package com.samyisok.jpassvaultserver;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class JpassvaultserverApplication {

	public static void main(String[] args) {
		SpringApplication.run(JpassvaultserverApplication.class, args);
	}

	@Bean
	ApplicationRunner applicationRunner(Properties properties) {
		return args -> System.out.println(properties);
	}

}
