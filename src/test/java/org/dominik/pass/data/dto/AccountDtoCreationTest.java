package org.dominik.pass.data.dto;

import org.dominik.pass.data.enums.Role;
import org.dominik.pass.db.entities.Account;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.UUID;

import static org.dominik.pass.utils.TestUtils.createAccountInstance;
import static org.junit.jupiter.api.Assertions.*;

public class AccountDtoCreationTest {
  private static final Long ID = 1L;
  private static final UUID PUBLIC_IC = UUID.randomUUID();
  private static final String EMAIL = "dominik.krenski@gmail.com";
  private static final String PASSWORD = "b468879149f241f69ce185ee2cc1764047ece00f7aad0128053a12aee5be320c";
  private static final String SALT = "711882a4dc3dcb437eb6151c09025594";
  private static final String REMINDER = "simple dummy reminder";
  private static final Role ROLE = Role.ROLE_USER;
  private static final Instant CREATED_AT = Instant.now();
  private static final Instant UPDATED_AT = CREATED_AT.minusSeconds(2000);
  private static final short VERSION = 2;

  @Test
  @DisplayName("AccountDTO instance should be created from Account with all fields set")
  void shouldCreateAccountDtoInstanceWithAllFieldsSet() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    Account account = createAccountInstance(
        ID,
        PUBLIC_IC,
        EMAIL,
        PASSWORD,
        SALT,
        REMINDER,
        ROLE,
        true,
        true,
        false,
        true,
        CREATED_AT,
        UPDATED_AT,
        VERSION
    );

    AccountDTO accountDTO = AccountDTO.fromAccount(account);

    assertEquals(ID, accountDTO.getId());
    assertEquals(PUBLIC_IC.toString(), accountDTO.getPublicId().toString());
    assertEquals(EMAIL, accountDTO.getEmail());
    assertEquals(PASSWORD, accountDTO.getPassword());
    assertEquals(SALT, accountDTO.getSalt());
    assertEquals(REMINDER, accountDTO.getReminder());
    assertEquals(ROLE.toString(), accountDTO.getRole().toString());
    assertTrue(accountDTO.isAccountNonExpired());
    assertTrue(accountDTO.isAccountNonLocked());
    assertFalse(accountDTO.isCredentialsNonExpired());
    assertTrue(accountDTO.isEnabled());
    assertEquals(CREATED_AT, accountDTO.getCreatedAt());
    assertEquals(UPDATED_AT, accountDTO.getUpdatedAt());
    assertEquals(VERSION, accountDTO.getVersion());
  }

  @Test
  @DisplayName("AccountDTO instance should be created from Account with null reminder")
  void shouldCreateAccountDtoInstanceWithNullReminder() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    Account account = createAccountInstance(
        ID,
        PUBLIC_IC,
        EMAIL,
        PASSWORD,
        SALT,
        null,
        ROLE,
        false,
        false,
        false,
        true,
        CREATED_AT,
        UPDATED_AT,
        VERSION
    );

    AccountDTO accountDTO = AccountDTO.fromAccount(account);

    assertEquals(ID, accountDTO.getId());
    assertEquals(PUBLIC_IC.toString(), accountDTO.getPublicId().toString());
    assertEquals(EMAIL, accountDTO.getEmail());
    assertEquals(PASSWORD, accountDTO.getPassword());
    assertEquals(SALT, accountDTO.getSalt());
    assertNull(accountDTO.getReminder());
    assertEquals(ROLE.toString(), accountDTO.getRole().toString());
    assertFalse(accountDTO.isAccountNonExpired());
    assertFalse(accountDTO.isAccountNonLocked());
    assertFalse(accountDTO.isCredentialsNonExpired());
    assertTrue(accountDTO.isEnabled());
    assertEquals(CREATED_AT, accountDTO.getCreatedAt());
    assertEquals(UPDATED_AT, accountDTO.getUpdatedAt());
    assertEquals(VERSION, accountDTO.getVersion());
  }
}
