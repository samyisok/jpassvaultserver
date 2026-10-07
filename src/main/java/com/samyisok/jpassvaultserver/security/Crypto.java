package com.samyisok.jpassvaultserver.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class Crypto {
  private static final String SHA_256 = "SHA-256";

  private Crypto() {
  }

  public static boolean constantTimeEquals(String expected, String actual) {
    if (expected == null || actual == null) {
      return false;
    }
    byte[] expectedDigest = sha256(expected.getBytes(StandardCharsets.UTF_8));
    byte[] actualDigest = sha256(actual.getBytes(StandardCharsets.UTF_8));
    return MessageDigest.isEqual(expectedDigest, actualDigest);
  }

  public static String sha256Hex(String value) {
    if (value == null) {
      return "";
    }
    return HexFormat.of().formatHex(sha256(value.getBytes(StandardCharsets.UTF_8)));
  }

  private static byte[] sha256(byte[] input) {
    try {
      return MessageDigest.getInstance(SHA_256).digest(input);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 is not available", e);
    }
  }
}
