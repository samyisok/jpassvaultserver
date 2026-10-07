package com.samyisok.jpassvaultserver;

import com.samyisok.jpassvaultserver.persistence.DatabasePermissions;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class JpassvaultserverApplication {

  static final String DEFAULT_DATASOURCE_URL = "jdbc:h2:file:./data/maindb";
  private static final String URL_ARG_PREFIX = "--spring.datasource.url=";

  public static void main(String[] args) {
    // Create the data directory owner-only before the database opens. The
    // directory is the security boundary; the database files are tightened
    // again after H2 creates them.
    DatabasePermissions.ensureOwnerOnlyDirectory(
        DatabasePermissions.dataDirectoryFor(resolveDatasourceUrl(args)));
    SpringApplication.run(JpassvaultserverApplication.class, args);
  }

  /**
   * Resolves the datasource URL with the same precedence Spring uses for
   * command-line arguments, system properties, then environment variables, so
   * the directory created here is the one H2 opens.
   */
  static String resolveDatasourceUrl(String[] args) {
    String fromArgs = fromArgs(args);
    if (fromArgs != null) {
      return fromArgs;
    }
    String fromProperty = System.getProperty("spring.datasource.url");
    if (fromProperty != null && !fromProperty.isBlank()) {
      return fromProperty;
    }
    String fromEnvironment = System.getenv("SPRING_DATASOURCE_URL");
    if (fromEnvironment != null && !fromEnvironment.isBlank()) {
      return fromEnvironment;
    }
    return DEFAULT_DATASOURCE_URL;
  }

  private static String fromArgs(String[] args) {
    if (args == null) {
      return null;
    }
    for (String arg : args) {
      if (arg.startsWith(URL_ARG_PREFIX)) {
        String value = arg.substring(URL_ARG_PREFIX.length());
        if (!value.isBlank()) {
          return value;
        }
      }
    }
    return null;
  }
}
