package com.samyisok.jpassvaultserver.services;

public class FileNotFoundException extends RuntimeException {
  static final long serialVersionUID = 1L;

  public FileNotFoundException() {
    super("Could not find file");
  }
}
