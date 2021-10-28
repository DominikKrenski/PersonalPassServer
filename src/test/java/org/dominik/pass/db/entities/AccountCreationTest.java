package org.dominik.pass.db.entities;

import org.dominik.pass.data.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class AccountCreationTest {
  private static final String EMAIL = "dominik.krenski@gmail.com";
  private static final String PASSWORD = "b468879149f241f69ce185ee2cc1764047ece00f7aad0128053a12aee5be320c";
  private static final String SALT = "711882a4dc3dcb437eb6151c09025594";
  private static final String REMINDER = "simple reminder";
  private static final UUID PUBLIC_ID = UUID.randomUUID();

  @Test
  @DisplayName("should create instance using all fields")
  void shouldCreateInstanceUsingAllFields() {
    Account account = Account
        .builder()
        .publicId(PUBLIC_ID)
        .email(EMAIL)
        .password(PASSWORD)
        .salt(SALT)
        .reminder(REMINDER)
        .role(Role.ROLE_ADMIN)
        .accountNonExpired(true)
        .accountNonLocked(false)
        .credentialsNonExpired(false)
        .enabled(false)
        .build();

    assertNull(account.getId());
    assertEquals(PUBLIC_ID.toString(), account.getPublicId().toString());
    assertEquals(EMAIL, account.getEmail());
    assertEquals(PASSWORD, account.getPassword());
    assertEquals(SALT, account.getSalt());
    assertEquals(REMINDER, account.getReminder());
    assertEquals(Role.ROLE_ADMIN.toString(), account.getRole().toString());
    assertTrue(account.isAccountNonExpired());
    assertFalse(account.isAccountNonLocked());
    assertFalse(account.isCredentialsNonExpired());
    assertFalse(account.isEnabled());
  }

  @Test
  @DisplayName("should initialize public id if none was given")
  void shouldInitializePublicIdIfNonWasGiven() {
    Account account = Account
        .builder()
        .email(EMAIL)
        .password(PASSWORD)
        .salt(SALT)
        .reminder(REMINDER)
        .role(Role.ROLE_USER)
        .accountNonExpired(false)
        .accountNonLocked(false)
        .credentialsNonExpired(false)
        .enabled(false)
        .build();

    assertNotNull(account.getPublicId());
  }

  @Test
  @DisplayName("should throw IllegalStateExceptionIfEmailNotPassed")
  void shouldThrowIllegalStateExceptionIfEmailNotPassed() {
    assertThrows(IllegalStateException.class, () -> Account
        .builder()
        .password(PASSWORD)
        .salt(SALT)
        .build());
  }

  @Test
  @DisplayName("should throw IllegalStateException if password was not set")
  void shouldThrowIllegalStateExceptionIfPasswordNotPassed() {
    assertThrows(IllegalStateException.class, () -> Account
        .builder()
        .email(EMAIL)
        .salt(SALT)
        .build());
  }

  @Test
  @DisplayName("should throw IllegalStateException if salt was not set")
  void shouldThrowIllegalStateExceptionIfSaltNotSet() {
    assertThrows(IllegalStateException.class, () -> Account
        .builder()
        .email(EMAIL)
        .password(PASSWORD)
        .build());
  }

  @Test
  @DisplayName("reminder should be null if not set")
  void reminderShouldBeNullIfNotSet() {
    Account account = Account
        .builder()
        .email(EMAIL)
        .password(PASSWORD)
        .salt(SALT)
        .build();

    assertNull(account.getReminder());
  }

  @Test
  @DisplayName("role should be ROLE_USER if not set")
  void roleShouldBeRoleUserIfNotSet() {
    Account account = Account
        .builder()
        .email(EMAIL)
        .password(PASSWORD)
        .salt(SALT)
        .build();

    assertEquals(Role.ROLE_USER.toString(), account.getRole().toString());
  }

  @Test
  @DisplayName("all boolean values should be true if not set")
  void allBooleanValuesShouldBeTrueIfNotSet() {
    Account account = Account
        .builder()
        .email(EMAIL)
        .password(PASSWORD)
        .salt(SALT)
        .build();

    assertTrue(account.isAccountNonExpired());
    assertTrue(account.isAccountNonLocked());
    assertTrue(account.isCredentialsNonExpired());
    assertTrue(account.isEnabled());
  }
}
