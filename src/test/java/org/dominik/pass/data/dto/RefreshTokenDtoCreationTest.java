package org.dominik.pass.data.dto;

import org.dominik.pass.data.enums.Role;
import org.dominik.pass.db.entities.Account;
import org.dominik.pass.db.entities.RefreshToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.UUID;

import static org.dominik.pass.utils.TestUtils.createAccountInstance;
import static org.dominik.pass.utils.TestUtils.createRefreshTokenInstance;
import static org.junit.jupiter.api.Assertions.*;

class RefreshTokenDtoCreationTest {
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
  @DisplayName("should create instance without account")
  void shouldCreateInstanceWithoutAccount() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    Account account = createAccountInstance(
        ID,
        PUBLIC_ID,
        EMAIL,
        PASSWORD,
        SALT,
        REMINDER,
        ROLE,
        true,
        true,
        false,
        false,
        CREATED_AT,
        UPDATED_AT,
        VERSION
    );

    RefreshToken refreshToken = createRefreshTokenInstance(
        ID,
        "access_token",
        false,
        account,
        Instant.now().minusSeconds(2000),
        Instant.now().minusSeconds(1800),
        VERSION
    );

    RefreshTokenDTO dto = RefreshTokenDTO.fromRefreshTokenLazy(refreshToken);

    assertEquals(refreshToken.getId(), dto.getId());
    assertEquals(refreshToken.getToken(), dto.getToken());
    assertEquals(refreshToken.isUsed(), dto.isUsed());
    assertEquals(refreshToken.getCreatedAt(), dto.getCreatedAt());
    assertEquals(refreshToken.getUpdatedAt(), dto.getUpdatedAt());
    assertEquals(refreshToken.getVersion(), dto.getVersion());
    assertNull(dto.getAccount());
  }

  @Test
  @DisplayName("should create instance with account")
  void shouldCreateInstanceWithAccount() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    Account account = createAccountInstance(
        ID,
        PUBLIC_ID,
        EMAIL,
        PASSWORD,
        SALT,
        REMINDER,
        ROLE,
        true,
        true,
        false,
        false,
        CREATED_AT,
        UPDATED_AT,
        VERSION
    );

    RefreshToken refreshToken = createRefreshTokenInstance(
        ID,
        "access_token",
        true,
        account,
        Instant.now().minusSeconds(2000),
        Instant.now().minusSeconds(1800),
        VERSION
    );

    RefreshTokenDTO dto = RefreshTokenDTO.fromRefreshTokenEager(refreshToken);

    assertEquals(refreshToken.getId(), dto.getId());
    assertEquals(refreshToken.getToken(), dto.getToken());
    assertEquals(refreshToken.isUsed(), dto.isUsed());
    assertEquals(refreshToken.getCreatedAt(), dto.getCreatedAt());
    assertEquals(refreshToken.getUpdatedAt(), dto.getUpdatedAt());
    assertEquals(refreshToken.getVersion(), dto.getVersion());

    assertNotNull(dto.getAccount());
    assertEquals(account.getId(), dto.getAccount().getId());
    assertEquals(account.getPublicId().toString(), dto.getAccount().getPublicId().toString());
    assertEquals(account.getEmail(), dto.getAccount().getEmail());
    assertEquals(account.getPassword(), dto.getAccount().getPassword());
    assertEquals(account.getSalt(), dto.getAccount().getSalt());
    assertEquals(account.getReminder(), dto.getAccount().getReminder());
    assertEquals(account.getRole().toString(), dto.getAccount().getRole().toString());
    assertTrue(dto.getAccount().isAccountNonExpired());
    assertTrue(dto.getAccount().isAccountNonLocked());
    assertFalse(dto.getAccount().isCredentialsNonExpired());
    assertFalse(dto.getAccount().isEnabled());
    assertEquals(account.getCreatedAt(), dto.getAccount().getCreatedAt());
    assertEquals(account.getUpdatedAt(), dto.getAccount().getUpdatedAt());
    assertEquals(account.getVersion(), dto.getAccount().getVersion());
  }
}
