package org.dominik.pass.configuration;

import org.dominik.pass.utils.factories.YamlPropertySourceFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@TestConfiguration
@PropertySource(value = "classpath:data-jpa-test-configuration.yaml", factory = YamlPropertySourceFactory.class)
@EnableJpaAuditing
public class DataJpaTestConfiguration {
}
