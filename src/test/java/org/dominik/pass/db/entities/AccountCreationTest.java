package org.dominik.pass.db.entities;

import org.dominik.pass.data.dto.AccountDTO;
import org.dominik.pass.data.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.UUID;

import static org.dominik.pass.utils.TestUtils.createAccountInstance;
import static org.junit.jupiter.api.Assertions.*;

class AccountCreationTest {
  private static final String EMAIL = "dominik.krenski@gmail.com";
  private static final String PASSWORD = "b468879149f241f69ce185ee2cc1764047ece00f7aad0128053a12aee5be320c";
  private static final String SALT = "711882a4dc3dcb437eb6151c09025594";
  private static final String REMINDER = "simple reminder";
  private static final UUID PUBLIC_ID = UUID.randomUUID();
  private static final Instant CREATED_AT = Instant.now().minusSeconds(4000);
  private static final Instant UPDATED_AT = Instant.now().minusSeconds(3190);

  @Test
  @DisplayName("should create account using 4-parameter constructor")
  void shouldCreateAccount() {
    Account account = new Account(EMAIL, PASSWORD, SALT, REMINDER);

    assertNull(account.getId());
    assertNotNull(account.getPublicId());
    assertEquals(EMAIL, account.getEmail());
    assertEquals(PASSWORD, account.getPassword());
    assertEquals(SALT, account.getSalt());
    assertEquals(REMINDER, account.getReminder());
    assertEquals(Role.ROLE_USER.toString(), account.getRole().toString());
    assertTrue(account.isAccountNonExpired());
    assertTrue(account.isAccountNonLocked());
    assertTrue(account.isCredentialsNonExpired());
    assertTrue(account.isEnabled());
    assertNull(account.getCreatedAt());
    assertNull(account.getUpdatedAt());
    assertEquals(0, account.getVersion());
  }

  @Test
  @DisplayName("should create Account instance from AccountDTO")
  void shouldCreateAccountFromAccountDTO() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    Account account = createAccountInstance(
        1L,
        PUBLIC_ID,
        EMAIL,
        PASSWORD,
        SALT,
        REMINDER,
        Role.ROLE_ADMIN,
        true,
        true,
        false,
        false,
        CREATED_AT,
        UPDATED_AT,
        (short) 3
    );

    AccountDTO accountDTO = AccountDTO.fromAccount(account);
    Account restored = Account.fromDTO(accountDTO);

    assertEquals(account.getId(), restored.getId());
    assertEquals(account.getPublicId().toString(), restored.getPublicId().toString());
    assertEquals(account.getEmail(), restored.getEmail());
    assertEquals(account.getPassword(), restored.getPassword());
    assertEquals(account.getSalt(), restored.getSalt());
    assertEquals(account.getReminder(), restored.getReminder());
    assertEquals(account.getRole().toString(), restored.getRole().toString());
    assertTrue(restored.isAccountNonExpired());
    assertTrue(restored.isAccountNonLocked());
    assertFalse(restored.isCredentialsNonExpired());
    assertFalse(restored.isEnabled());
    assertEquals(account.getCreatedAt(), restored.getCreatedAt());
    assertEquals(account.getUpdatedAt(), restored.getUpdatedAt());
    assertEquals(account.getVersion(), restored.getVersion());
  }

  @Test
  @DisplayName("should throw NullPointerException if email is null")
  void shouldThrowExceptionIfEmailIsNull() {
    assertThrows(NullPointerException.class, () -> new Account(null, PASSWORD, SALT, REMINDER));
  }

  @Test
  @DisplayName("should throw NullPointerException if password is null")
  void shouldThrowExceptionIfPasswordIsNull() {
    assertThrows(NullPointerException.class, () -> new Account(EMAIL, null, SALT, REMINDER));
  }

  @Test
  @DisplayName("should throw NullPointerException if salt is null")
  void shouldThrowExceptionIfSaltIsNull() {
    assertThrows(NullPointerException.class, () -> new Account(EMAIL, PASSWORD, null, REMINDER));
  }

  @Test
  @DisplayName("reminder should be null")
  void reminderShouldBeNull() {
    Account account = new Account(EMAIL, PASSWORD, SALT, null);

    assertNull(account.getReminder());
  }
}
