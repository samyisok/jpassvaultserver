package com.samyisok.jpassvaultserver.persistence;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DatabasePermissionsUnitTest {

  @TempDir
  Path tempDir;

  @Test
  void parsesFileBasedH2Url() {
    Path expected = tempDir.resolve("maindb");

    assertEquals(expected,
        DatabasePermissions.h2DatabaseFile("jdbc:h2:file:" + expected));
    assertEquals(expected,
        DatabasePermissions.h2DatabaseFile("jdbc:h2:file:" + expected + ";DB_CLOSE_DELAY=-1"));
    assertNull(DatabasePermissions.h2DatabaseFile("jdbc:h2:mem:testdb"));
    assertNull(DatabasePermissions.h2DatabaseFile(null));
  }

  @Test
  void appliesOwnerOnlyPermissions() throws IOException {
    assumeTrue(supportsPosix());
    Path directory = tempDir.resolve("data");
    Path databaseFile = directory.resolve("maindb.mv.db");
    Files.createDirectories(directory);
    Files.writeString(databaseFile, "data");

    DatabasePermissions.applyOwnerOnlyPermissions(databaseFile);

    assertEquals("rwx------",
        PosixFilePermissions.toString(Files.getPosixFilePermissions(directory)));
    assertEquals("rw-------",
        PosixFilePermissions.toString(Files.getPosixFilePermissions(databaseFile)));
  }

  @Test
  void ignoresNonFileDatabase() {
    assertDoesNotThrow(() -> DatabasePermissions.applyOwnerOnlyPermissions(null));
  }

  @Test
  void rejectsBlankOrDefaultCredentials() {
    assertThrows(IllegalStateException.class,
        () -> DatabasePermissions.validateCredentials("  ", null));
    assertThrows(IllegalStateException.class,
        () -> DatabasePermissions.validateCredentials("sa", ""));
    assertThrows(IllegalStateException.class,
        () -> DatabasePermissions.validateCredentials("sa", "sa"));
    assertThrows(IllegalStateException.class,
        () -> DatabasePermissions.validateCredentials("SA", "strong-pass"));
    assertThrows(IllegalStateException.class,
        () -> DatabasePermissions.validateCredentials("sa", "password"));
    assertDoesNotThrow(() -> DatabasePermissions.validateCredentials(null, null));
    assertDoesNotThrow(() -> DatabasePermissions.validateCredentials("user", "strong-pass"));
  }

  private static boolean supportsPosix() {
    return FileSystems.getDefault().supportedFileAttributeViews().contains("posix");
  }
}
