package org.dominik.pass.db.entities;

import org.dominik.pass.data.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.UUID;

import static org.dominik.pass.utils.TestUtils.createAccountInstance;
import static org.junit.jupiter.api.Assertions.*;

class RefreshTokenCreationTest {
  private static final Long ID = 1L;
  private static final UUID PUBLIC_ID = UUID.randomUUID();
  private static final String EMAIL = "dominik.krenski@gmail.com";
  private static final String PASSWORD = "b468879149f241f69ce185ee2cc1764047ece00f7aad0128053a12aee5be320c";
  private static final String SALT = "711882a4dc3dcb437eb6151c09025594";
  private static final String REMINDER = "dummy message";
  private static final Role ROLE = Role.ROLE_ADMIN;
  private static final Instant CREATED_AT = Instant.now().minusSeconds(5000);
  private static final Instant UPDATED_AT = Instant.now().minusSeconds(3800);
  private static final short VERSION = 1;

  @Test
  @DisplayName("should create instance")
  void shouldCreateInstance() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    Account account = createAccountInstance(
        ID,
        PUBLIC_ID,
        EMAIL,
        PASSWORD,
        SALT,
        REMINDER,
        ROLE,
        false,
        false,
        true,
        true,
        CREATED_AT,
        UPDATED_AT,
        VERSION
    );

    RefreshToken refreshToken = new RefreshToken("refresh_token", account);

    assertNull(refreshToken.getId());
    assertEquals("refresh_token", refreshToken.getToken());
    assertFalse(refreshToken.isUsed());
    assertNotNull(refreshToken.getAccount());
    assertNull(refreshToken.getCreatedAt());
    assertNull(refreshToken.getUpdatedAt());
    assertEquals(0, refreshToken.getVersion());
  }
}
