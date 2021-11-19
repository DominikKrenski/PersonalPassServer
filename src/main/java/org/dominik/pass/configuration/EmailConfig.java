package org.dominik.pass.configuration;

import lombok.Getter;
import lombok.Setter;
import org.dominik.pass.utils.factories.YamlPropertySourceFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ConfigurationProperties
@PropertySource(value = "classpath:email-${spring.profiles.active}.yaml", factory = YamlPropertySourceFactory.class)
@Getter
@Setter
public class EmailConfig {
  private String apiKey;
}
