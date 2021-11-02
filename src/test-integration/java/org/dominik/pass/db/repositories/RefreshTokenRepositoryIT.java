package org.dominik.pass.db.repositories;

import org.dominik.pass.configuration.DataJpaTestConfiguration;
import org.dominik.pass.data.enums.Role;
import org.dominik.pass.db.entities.Account;
import org.dominik.pass.db.entities.RefreshToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest(properties = {
    "spring.main.banner-mode=off"
})
@Import(DataJpaTestConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class RefreshTokenRepositoryIT {
  private static final String EMAIL = "dominik.krenski@gmail.com";
  private static final String PASSWORD = "b468879149f241f69ce185ee2cc1764047ece00f7aad0128053a12aee5be320c";
  private static final String SALT = "711882a4dc3dcb437eb6151c09025594";
  private static final String REMINDER = "dummy message";
  private static final Role ROLE = Role.ROLE_ADMIN;


  @Autowired private TestEntityManager em;
  @Autowired RefreshTokenRepository tokenRepository;

  @Test
  @DisplayName("should save refresh token")
  void shouldSaveRefreshToken() {
    Account account = Account
        .builder()
        .email(EMAIL)
        .password(PASSWORD)
        .salt(SALT)
        .reminder(REMINDER)
        .build();

    Account savedAccount = em.persistAndFlush(account);

    RefreshToken savedToken = tokenRepository.save(new RefreshToken("refresh_token", savedAccount));
    em.flush();

    assertNotNull(savedToken.getId());
    assertEquals("refresh_token", savedToken.getToken());
    assertNotNull(savedToken.getAccount());
    assertNotNull(savedToken.getCreatedAt());
    assertNotNull(savedToken.getUpdatedAt());
    assertEquals(0, savedToken.getVersion());
  }

}
