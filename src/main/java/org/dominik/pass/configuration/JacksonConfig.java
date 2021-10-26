package org.dominik.pass.configuration;

import com.fasterxml.jackson.databind.module.SimpleModule;
import org.dominik.pass.utils.serializers.ApiInstantSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;

@Configuration
public class JacksonConfig {

  @Bean
  public Jackson2ObjectMapperBuilderCustomizer builderCustomizer() {
    SimpleModule module = new SimpleModule();
    module.addSerializer(Instant.class, new ApiInstantSerializer());
    return customizer -> customizer.modules(module);
  }
}
