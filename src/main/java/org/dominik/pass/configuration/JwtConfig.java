package org.dominik.pass.configuration;

import lombok.Getter;
import lombok.Setter;
import org.dominik.pass.utils.factories.YamlPropertySourceFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = "classpath:jwt-${spring.profiles.active:dev}.yaml", factory = YamlPropertySourceFactory.class)
@Getter
public class JwtConfig {
  private String issuer;
  private String key;
  private Token accessToken = new Token();
  private Token refreshToken = new Token();

  @Getter
  @Setter
  public static final class Token {
    private String audience;
    private int expiration;
  }
}
