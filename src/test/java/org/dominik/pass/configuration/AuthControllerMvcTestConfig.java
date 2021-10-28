package org.dominik.pass.configuration;

import org.dominik.pass.services.definitions.AccountService;
import org.dominik.pass.utils.factories.YamlPropertySourceFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

@TestConfiguration
@Import({SecurityTestConfig.class, JacksonTestConfig.class, PasswordEncoderTestConfig.class})
@PropertySource(value = "classpath:mvc-test-configuration.yaml", factory = YamlPropertySourceFactory.class)
public class AuthControllerMvcTestConfig {

  @MockBean
  AccountService accountService;
}
