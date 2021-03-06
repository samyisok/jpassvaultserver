package com.samyisok.jpassvaultserver.domains;

public class FileNotFoundException extends RuntimeException {
  static final long serialVersionUID = 1l;


  public FileNotFoundException() {
    super("Could not find file");
  }

  public FileNotFoundException(Long id) {
    super("Could not find file " + id);
  }
}
