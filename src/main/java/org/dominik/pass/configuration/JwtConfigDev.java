package org.dominik.pass.configuration;

import lombok.Getter;
import lombok.Setter;
import org.dominik.pass.utils.factories.YamlPropertySourceFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

import java.util.Map;

@Configuration
@PropertySource(value = "classpath:jwt-dev.yaml", factory = YamlPropertySourceFactory.class)
@Profile("dev")
@Getter
public class JwtConfigDev {
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
