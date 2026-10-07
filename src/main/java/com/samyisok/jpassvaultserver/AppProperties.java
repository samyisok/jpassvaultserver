package com.samyisok.jpassvaultserver;

import jakarta.annotation.PostConstruct;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app-properties")
public class AppProperties {
  static final String PLACEHOLDER_SECRET = "555505424923a833fe77cfa68c497bf2";
  static final long DEFAULT_MAX_PAYLOAD_SIZE = 10L * 1024 * 1024;
  private static final String MASK = "****";

  private String secretKey;
  private Boolean useSecretKeyFromEnv = Boolean.TRUE;
  private Boolean trustProxyHeaders = Boolean.FALSE;
  private Boolean allowPlainHttp = Boolean.FALSE;
  private Long maxPayloadSize = DEFAULT_MAX_PAYLOAD_SIZE;

  @Autowired
  private Environment env;

  @PostConstruct
  public void validateConfiguration() {
    validateSecret(getEffectiveSecretKey());
    validateTransport(isTlsConfigured(), Boolean.TRUE.equals(allowPlainHttp));
  }

  void validateSecret(String secret) {
    if (secret == null || secret.isBlank()) {
      throw new IllegalStateException(
          "No API secret configured: set JPASSVAULT_SECRET (or app-properties.secret-key with use-secret-key-from-env=false) before starting");
    }
    if (PLACEHOLDER_SECRET.equals(secret)) {
      throw new IllegalStateException(
          "The API secret is the previously committed placeholder value and must be changed");
    }
  }

  void validateTransport(boolean tlsConfigured, boolean plainHttpAllowed) {
    if (!tlsConfigured && !plainHttpAllowed) {
      throw new IllegalStateException(
          "Refusing to start without TLS: configure server.ssl.* or set app-properties.allow-plain-http=true for local development only");
    }
  }

  boolean isTlsConfigured() {
    if (env == null) {
      return false;
    }
    boolean enabled = !"false".equalsIgnoreCase(env.getProperty("server.ssl.enabled"));
    boolean keyMaterial = env.getProperty("server.ssl.key-store") != null
        || env.getProperty("server.ssl.certificate") != null;
    return enabled && keyMaterial;
  }

  String getEffectiveSecretKey() {
    return Boolean.TRUE.equals(useSecretKeyFromEnv) ? getEnvSecretKey() : secretKey;
  }

  public String getSecretKey() {
    return secretKey;
  }

  public void setSecretKey(String secretKey) {
    this.secretKey = secretKey;
  }

  public String getEnvSecretKey() {
    return env.getProperty("JPASSVAULT_SECRET");
  }

  public Boolean getUseSecretKeyFromEnv() {
    return useSecretKeyFromEnv;
  }

  public void setUseSecretKeyFromEnv(Boolean useSecretKeyFromEnv) {
    this.useSecretKeyFromEnv = useSecretKeyFromEnv;
  }

  public Boolean getTrustProxyHeaders() {
    return trustProxyHeaders;
  }

  public void setTrustProxyHeaders(Boolean trustProxyHeaders) {
    this.trustProxyHeaders = trustProxyHeaders;
  }

  public Boolean getAllowPlainHttp() {
    return allowPlainHttp;
  }

  public void setAllowPlainHttp(Boolean allowPlainHttp) {
    this.allowPlainHttp = allowPlainHttp;
  }

  public Long getMaxPayloadSize() {
    return maxPayloadSize;
  }

  public void setMaxPayloadSize(Long maxPayloadSize) {
    this.maxPayloadSize = maxPayloadSize == null ? DEFAULT_MAX_PAYLOAD_SIZE : maxPayloadSize;
  }

  @Override
  public String toString() {
    return "AppProperties [secretKey=" + mask(secretKey) + ", useSecretKeyFromEnv="
        + useSecretKeyFromEnv + "]";
  }

  private static String mask(String value) {
    return value == null ? null : MASK;
  }
}
