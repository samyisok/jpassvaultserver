package com.samyisok.jpassvaultserver;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "properties")
public class Properties {
  private String secretKey;

  /**
   * @return the key
   */
  public String getSecretKey() {
    return secretKey;
  }

  /**
   * @param secretKey the key to set
   */
  public void setSecretKey(String secretKey) {
    this.secretKey = secretKey;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */

  @Override
  public String toString() {
    return "Properties [key=" + secretKey + "]";
  }
}
