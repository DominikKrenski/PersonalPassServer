package org.dominik.pass.db.repositories;

import org.dominik.pass.configuration.DataJpaTestConfiguration;
import org.dominik.pass.data.enums.Role;
import org.dominik.pass.db.entities.Account;
import org.dominik.pass.db.entities.RefreshToken;
import org.dominik.pass.errors.exceptions.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {
    "spring.main.banner-mode=off"
})
@Import(DataJpaTestConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql("classpath:sql/01.token-repository-test.sql")
@ActiveProfiles("integration")
class RefreshTokenRepositoryIT {
  private static final String EMAIL = "dominik.krenski@gmail.com";
  private static final String PASSWORD = "b468879149f241f69ce185ee2cc1764047ece00f7aad0128053a12aee5be320c";
  private static final String SALT = "711882a4dc3dcb437eb6151c09025594";
  private static final String REMINDER = "dummy message";
  private static final Role ROLE = Role.ROLE_ADMIN;


  @Autowired
  private TestEntityManager em;
  @Autowired
  RefreshTokenRepository tokenRepository;
  @Autowired
  AccountRepository accountRepository;

  @Test
  @DisplayName("should save refresh token")
  void shouldSaveRefreshToken() {
    Account account = accountRepository
        .findByEmail("dominik.krenski@gmail.com")
        .orElseThrow(() -> new NotFoundException("Account not found"));

    RefreshToken savedToken = tokenRepository.save(new RefreshToken("refresh_token", account));
    em.flush();

    assertNotNull(savedToken.getId());
    assertEquals("refresh_token", savedToken.getToken());
    assertFalse(savedToken.isUsed());
    assertNotNull(savedToken.getAccount());
    assertNotNull(savedToken.getCreatedAt());
    assertNotNull(savedToken.getUpdatedAt());
    assertEquals(0, savedToken.getVersion());
  }

  @Test
  @DisplayName("should delete all tokens based on account's public id")
  void shouldDeleteAllTokensBasedOnAccountPublicId() {
    Account account = accountRepository
        .findByEmail("dominik.krenski@gmail.com")
        .orElseThrow(() -> new NotFoundException("Account not found"));

    int deleted = tokenRepository.deleteAllAccountTokensByPublicId(account.getPublicId());

    assertEquals(3, deleted);
  }

  @Test
  @DisplayName("should find all tokens based on account's public id (lazy)")
  void shouldFindAllTokensBasedOnAccountPublicIdLazy() {
    Account account = accountRepository
        .findByEmail("dominik.krenski@gmail.com")
        .orElseThrow(() -> new NotFoundException("Account not found"));

    List<RefreshToken> tokens = tokenRepository.findByAccountPublicId(account.getPublicId());

    assertEquals(3, tokens.size());
  }

  @Test
  @DisplayName("should find token based on token entry")
  void shouldFindTokenBasedOnTokenEntry() {
    Optional<RefreshToken> tokenOptional = tokenRepository.findByToken("refresh_token_2");
    assertTrue(tokenOptional.isPresent());
  }

  @Test
  @DisplayName("should mark token as used if token='refresh_token_1'")
  void shouldMarkTokenAsUsed() throws Exception {
    int updatedTokens = tokenRepository.markTokenAsUsed("refresh_token_1");
    RefreshToken token = tokenRepository.findByToken("refresh_token_1").orElseThrow(() -> new Exception("Token does not exist"));

    assertEquals(1, updatedTokens);
    assertTrue(token.isUsed());
  }

  @Test
  @DisplayName("should delete all tokens based on account's email")
  void shouldDeleteAllTokensBasedOnAccountsEmail() {
    int deletedTokens = tokenRepository.deleteAllAccountTokensByEmail("dominik.krenski@gmail.com");

    assertEquals(3, deletedTokens);
  }
}
