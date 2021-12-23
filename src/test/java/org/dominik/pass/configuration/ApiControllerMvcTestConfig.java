package org.dominik.pass.configuration;

import org.dominik.pass.security.utils.JwtUtils;
import org.dominik.pass.security.utils.SecurityUtils;
import org.dominik.pass.services.definitions.*;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;

@TestConfiguration
@Import({SecurityConfigDev.class, JacksonConfig.class, PasswordEncoderConfig.class})
public class ApiControllerMvcTestConfig {

  @MockBean UserDetailsService detailsService;
  @MockBean AccountService accountService;
  @MockBean RefreshTokenService tokenService;
  @MockBean JwtUtils jwtUtils;
  @MockBean SecurityUtils securityUtils;
  @MockBean EmailService emailService;
}
