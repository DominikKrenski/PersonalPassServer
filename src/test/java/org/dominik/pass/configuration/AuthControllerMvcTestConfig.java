package org.dominik.pass.configuration;

import org.dominik.pass.security.utils.JwtUtils;
import org.dominik.pass.services.definitions.AccountService;
import org.dominik.pass.services.definitions.RefreshTokenService;
import org.dominik.pass.utils.factories.YamlPropertySourceFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.core.userdetails.UserDetailsService;

@TestConfiguration
@Import({SecurityConfigDev.class, JacksonConfig.class, PasswordEncoderConfig.class})
@PropertySource(value = "classpath:mvc-test-configuration.yaml", factory = YamlPropertySourceFactory.class)
public class AuthControllerMvcTestConfig {

  @MockBean UserDetailsService detailsService;
  @MockBean AccountService accountService;
  @MockBean RefreshTokenService tokenService;
  @MockBean JwtUtils jwtUtils;
}
