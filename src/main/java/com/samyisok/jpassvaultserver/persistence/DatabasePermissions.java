package com.samyisok.jpassvaultserver.persistence;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

public final class DatabasePermissions {
  private static final String H2_FILE_PREFIX = "jdbc:h2:file:";
  private static final String H2_DATABASE_SUFFIX = ".mv.db";
  private static final Set<PosixFilePermission> DIRECTORY_PERMISSIONS =
      PosixFilePermissions.fromString("rwx------");
  private static final Set<PosixFilePermission> FILE_PERMISSIONS =
      PosixFilePermissions.fromString("rw-------");

  private DatabasePermissions() {
  }

  public static Path h2DatabaseFile(String datasourceUrl) {
    if (datasourceUrl == null || !datasourceUrl.startsWith(H2_FILE_PREFIX)) {
      return null;
    }
    String location = datasourceUrl.substring(H2_FILE_PREFIX.length());
    int optionsIndex = location.indexOf(';');
    if (optionsIndex >= 0) {
      location = location.substring(0, optionsIndex);
    }
    if (location.isBlank()) {
      return null;
    }
    return Path.of(location).toAbsolutePath().normalize();
  }

  public static void applyOwnerOnlyPermissions(Path databaseFile) {
    if (databaseFile == null || !supportsPosix()) {
      return;
    }
    try {
      Path directory = databaseFile.getParent();
      if (directory != null) {
        Files.createDirectories(directory);
        setPermissions(directory, DIRECTORY_PERMISSIONS);
      }
      for (Path candidate : new Path[] {databaseFile,
          databaseFile.resolveSibling(databaseFile.getFileName() + H2_DATABASE_SUFFIX),
          databaseFile.resolveSibling(databaseFile.getFileName() + ".lock.db"),
          databaseFile.resolveSibling(databaseFile.getFileName() + ".trace.db")}) {
        if (Files.exists(candidate)) {
          setPermissions(candidate, FILE_PERMISSIONS);
        }
      }
    } catch (IOException e) {
      throw new IllegalStateException(
          "Could not restrict database permissions for " + databaseFile, e);
    }
  }

  public static void validateCredentials(String username, String password) {
    if (username != null && (username.isBlank() || "sa".equalsIgnoreCase(username))) {
      throw new IllegalStateException(
          "Database username must not be blank or the default 'sa'");
    }
    if (password != null
        && (password.isBlank() || "sa".equals(password) || "password".equals(password))) {
      throw new IllegalStateException("Database password must not be blank or a default value");
    }
  }

  private static boolean supportsPosix() {
    return FileSystems.getDefault().supportedFileAttributeViews().contains("posix");
  }

  private static void setPermissions(Path path, Set<PosixFilePermission> permissions)
      throws IOException {
    Files.setPosixFilePermissions(path, permissions);
  }
}
