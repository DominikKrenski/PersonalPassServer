package org.dominik.pass.configuration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import(JpaConfigDev.class)
public class DataJpaTestConfiguration {
}
