package com.samyisok.jpassvaultserver.auth;

public interface TokenVerifiable {
  boolean verify(String token);
}
