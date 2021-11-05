package org.dominik.pass.configuration;

import lombok.Getter;
import lombok.Setter;
import org.dominik.pass.utils.factories.YamlPropertySourceFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ConfigurationProperties
@PropertySource(value = "classpath:jwt-${spring.profiles.active}.yaml", factory = YamlPropertySourceFactory.class)
@Getter
@Setter
public class JwtConfig {
  private String issuer;
  private String key;
  private JwtToken accessToken = new JwtToken();
  private JwtToken refreshToken = new JwtToken();

  @Getter
  @Setter
  public static final class JwtToken {
    private String audience;
    private int expiration;
  }
}
