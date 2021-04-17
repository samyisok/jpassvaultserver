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
    String key = getKey();

    if (key == null) {
      throw new RuntimeException("Key do not exists");
    }

    return key.equals(token);
  }

  String getKey() {
    return appProperties.getUseSecretKeyFromEnv() ? appProperties.getEnvSecretKey()
        : appProperties.getSecretKey();
  }
}
