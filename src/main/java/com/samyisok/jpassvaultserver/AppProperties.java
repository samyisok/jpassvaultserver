package com.samyisok.jpassvaultserver;

import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app-properties")
public class AppProperties {
  private String secretKey;
  private Boolean useSecretKeyFromEnv;

  @Autowired
  private Environment env;

  /**
   * @return the secretKey
   */
  public String getSecretKey() {
    return secretKey;
  }

  /**
   * @param secretKey the secretKey to set
   */
  public void setSecretKey(String secretKey) {
    this.secretKey = secretKey;
  }


  public String getEnvSecretKey() {
    return env.getProperty("secret-key");
  }

  /**
   * @return the useSecretKeyFromEnv
   */
  public Boolean getUseSecretKeyFromEnv() {
    return useSecretKeyFromEnv;
  }

  /**
   * @param useSecretKeyFromEnv the useSecretKeyFromEnv to set
   */
  public void setUseSecretKeyFromEnv(Boolean useSecretKeyFromEnv) {
    this.useSecretKeyFromEnv = useSecretKeyFromEnv;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */

  @Override
  public String toString() {
    return "AppProperties [secretKey=" + secretKey + ", useSecretKeyFromEnv="
        + useSecretKeyFromEnv + "]";
  }
}
