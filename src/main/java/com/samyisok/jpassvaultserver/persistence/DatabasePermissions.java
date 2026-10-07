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

  /** The directory that holds the database, or null for a non-file datasource. */
  public static Path dataDirectoryFor(String datasourceUrl) {
    Path databaseFile = h2DatabaseFile(datasourceUrl);
    return databaseFile == null ? null : databaseFile.getParent();
  }

  /**
   * Creates the directory (if needed) and, on POSIX systems, restricts it to the
   * owning user. Safe to call before the database exists.
   */
  public static void ensureOwnerOnlyDirectory(Path directory) {
    if (directory == null) {
      return;
    }
    try {
      Files.createDirectories(directory);
      if (supportsPosix()) {
        setPermissions(directory, DIRECTORY_PERMISSIONS);
      }
    } catch (IOException e) {
      throw new IllegalStateException(
          "Could not prepare database directory " + directory, e);
    }
  }

  /**
   * Tightens the directory and the database files (including H2 auxiliary files)
   * to owner-only on POSIX systems. Called after the database has been opened.
   */
  public static void applyOwnerOnlyPermissions(Path databaseFile) {
    if (databaseFile == null || !supportsPosix()) {
      return;
    }
    try {
      ensureOwnerOnlyDirectory(databaseFile.getParent());
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

  private static boolean supportsPosix() {
    return FileSystems.getDefault().supportedFileAttributeViews().contains("posix");
  }

  private static void setPermissions(Path path, Set<PosixFilePermission> permissions)
      throws IOException {
    Files.setPosixFilePermissions(path, permissions);
  }
}
