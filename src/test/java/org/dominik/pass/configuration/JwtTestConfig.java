package org.dominik.pass.configuration;

import lombok.Getter;
import lombok.Setter;
import org.dominik.pass.utils.factories.YamlPropertySourceFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.PropertySource;

@TestConfiguration
@ConfigurationProperties
@PropertySource(value = "classpath:jwt-${spring.profiles.active:dev}.yaml", factory = YamlPropertySourceFactory.class)
@Getter
@Setter
public class JwtTestConfig {
  private String issuer;
  private String key;
  private Token accessToken = new Token();
  private Token refreshToken = new Token();

  @Getter
  @Setter
  public static class Token {
    private String audience;
    private int expiration;
  }
}
