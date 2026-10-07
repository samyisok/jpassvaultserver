package com.samyisok.jpassvaultserver.auth;

import com.samyisok.jpassvaultserver.AppProperties;
import com.samyisok.jpassvaultserver.security.Crypto;
import org.springframework.stereotype.Component;

@Component
public class AuthCheck implements TokenVerifiable {
  private final AppProperties appProperties;

  public AuthCheck(AppProperties appProperties) {
    this.appProperties = appProperties;
  }

  @Override
  public boolean verify(String token) {
    String key = getKey();

    if (key == null) {
      throw new IllegalStateException("API key is not configured");
    }

    return Crypto.constantTimeEquals(key, token);
  }

  String getKey() {
    return Boolean.TRUE.equals(appProperties.getUseSecretKeyFromEnv())
        ? appProperties.getEnvSecretKey()
        : appProperties.getSecretKey();
  }
}
