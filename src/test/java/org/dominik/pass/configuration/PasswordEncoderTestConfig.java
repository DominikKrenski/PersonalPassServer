package org.dominik.pass.configuration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

@TestConfiguration
public class PasswordEncoderTestConfig {

  @Bean
  public PasswordEncoder passwordEncoder() throws NoSuchAlgorithmException {
    return new BCryptPasswordEncoder(12, SecureRandom.getInstance("NativePRNGNonBlocking"));
  }
}
