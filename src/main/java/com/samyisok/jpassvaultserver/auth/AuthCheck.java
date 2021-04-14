package com.samyisok.jpassvaultserver.auth;

import com.samyisok.jpassvaultserver.AppProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuthCheck implements TokenVerifiable {
  @Autowired
  private AppProperties appProperties;

  @Override
  public boolean verify(String token) {
    String key = appProperties.getSecretKey();
    return key.equals(token);
  }
}
