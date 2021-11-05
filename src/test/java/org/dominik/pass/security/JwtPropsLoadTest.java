package org.dominik.pass.security;

import org.dominik.pass.configuration.JwtConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith({SpringExtension.class})
@Import(JwtConfig.class)
@EnableConfigurationProperties({JwtConfig.class})
public class JwtPropsLoadTest {
  @Autowired JwtConfig jwtConfig;

  @BeforeAll
  static void setUp() {
    System.setProperty("spring.profiles.active", "test");
  }

  @Test
  @DisplayName("should load properties")
  void shouldLoadProperties() {
    assertEquals("personal-pass.dev", jwtConfig.getIssuer());
    assertEquals("gUkXn2r5u8x/A?D(G+KbPeShVmYq3s6v9y$B&E)H@McQfTjWnZr4u7w!z%C*F-Ja", jwtConfig.getKey());
    assertEquals(120, jwtConfig.getAccessToken().getExpiration());
    assertEquals("access", jwtConfig.getAccessToken().getAudience());
    assertEquals(300, jwtConfig.getRefreshToken().getExpiration());
    assertEquals("refresh", jwtConfig.getRefreshToken().getAudience());
  }
}
