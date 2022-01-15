package org.dominik.pass.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.dominik.pass.data.dto.AccountDTO;
import org.dominik.pass.db.entities.Key;
import org.dominik.pass.db.entities.RefreshToken;
import org.dominik.pass.db.repositories.KeyRepository;
import org.dominik.pass.db.repositories.RefreshTokenRepository;
import org.dominik.pass.services.definitions.AccountService;
import org.dominik.pass.services.definitions.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.dominik.pass.utils.TestUtils.generateJwtToken;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.profiles.active=integration"
    }
)
@AutoConfigureMockMvc
@Transactional
@Sql("classpath:/sql/01.token-repository-test.sql")
@ActiveProfiles("integration")
class AuthControllerSignoutBootTestIT {
  private static final String AUTH_HEADER = "Authorization";
  private static final String SIGNOUT_URL = "/auth/signout";
  private static final String ISSUER = "personal-pass.dev";
  private static final String AUDIENCE = "access";
  private static final String KEY = "gUkXn2r5u8x/A?D(G+KbPeShVmYq3s6v9y$B&E)H@McQfTjWnZr4u7w!z%C*F-Ja";

  private String accessToken;
  private AccountDTO accountDTO;

  @Autowired MockMvc mvc;
  @Autowired AccountService accountService;
  @Autowired RefreshTokenRepository tokenRepository;
  @Autowired KeyRepository keyRepository;

  @BeforeEach
  void beforeEach() throws NoSuchAlgorithmException, InvalidKeyException, JsonProcessingException {
    accountDTO = accountService.findByEmail("dominik.krenski@gmail.com");
    long createdAt = Instant.now().getEpochSecond();

    accessToken = generateJwtToken(ISSUER, accountDTO.getPublicId().toString(), createdAt, AUDIENCE, createdAt + 1000, KEY);
  }

  @Test
  @DisplayName("should log out successfully")
  void shouldLogoutSuccessfully() throws Exception {
    mvc
        .perform(
            get(SIGNOUT_URL)
                .header(AUTH_HEADER, "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andDo(res -> {
          List<RefreshToken> tokens = tokenRepository.findAll();
          Optional<Key> key = keyRepository.findById(accountService.findByEmail("dominik.krenski@gmail.com").getId());

          assertTrue(key.isEmpty());
          assertEquals(0, tokens.size());
        });
  }
}
