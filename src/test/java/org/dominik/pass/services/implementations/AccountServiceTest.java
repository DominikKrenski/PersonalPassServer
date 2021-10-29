package org.dominik.pass.services.implementations;

import org.dominik.pass.data.dto.AccountDTO;
import org.dominik.pass.data.dto.RegistrationDTO;
import org.dominik.pass.data.enums.Role;
import org.dominik.pass.db.entities.Account;
import org.dominik.pass.db.repositories.AccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.UUID;

import static org.dominik.pass.utils.TestUtils.createAccountInstance;
import static org.dominik.pass.utils.TestUtils.createRegistrationDtoInstance;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {
  private static final Long ID = 1L;
  private static final UUID PUBLIC_ID = UUID.randomUUID();
  private static final String EMAIL = "dominik.krenski@gmail.com";
  private static final String PASSWORD = "b468879149f241f69ce185ee2cc1764047ece00f7aad0128053a12aee5be320c";
  private static final String SALT = "711882a4dc3dcb437eb6151c09025594";
  private static final String REMINDER = "simple dummy reminder";
  private static final Role ROLE = Role.ROLE_USER;
  private static final Instant CREATED_AT = Instant.now();
  private static final Instant UPDATED_AT = CREATED_AT.plusSeconds(500);
  private static final short VERSION = 1;

  @Mock AccountRepository accountRepository;
  @Mock PasswordEncoder passwordEncoder;
  @InjectMocks AccountServiceImpl accountService;

  @Test
  void shouldCreateAllDependencies() {
    assertNotNull(accountRepository);
    assertNotNull(passwordEncoder);
    assertNotNull(accountService);
  }

  @Test
  @DisplayName("should register account successfully")
  void shouldRegisterAccountSuccessfully() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    RegistrationDTO registrationDTO = createRegistrationDtoInstance(
        EMAIL,
        PASSWORD,
        SALT,
        REMINDER
    );

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

    when(passwordEncoder.encode(any(String.class))).thenReturn("encoded" + PASSWORD);
    when(accountRepository.save(any(Account.class))).thenReturn(account);

    AccountDTO dto = accountService.register(registrationDTO);

    assertEquals(ID, dto.getId());
    assertEquals(PUBLIC_ID.toString(), dto.getPublicId().toString());
    assertEquals(EMAIL, dto.getEmail());
    assertEquals(PASSWORD, dto.getPassword());
    assertEquals(SALT, dto.getSalt());
    assertEquals(REMINDER, dto.getReminder());
    assertEquals(ROLE.toString(), dto.getRole().toString());
    assertTrue(dto.isAccountNonExpired());
    assertTrue(dto.isAccountNonLocked());
    assertFalse(dto.isCredentialsNonExpired());
    assertFalse(dto.isEnabled());
    assertEquals(CREATED_AT, dto.getCreatedAt());
    assertEquals(UPDATED_AT, dto.getUpdatedAt());
    assertEquals(VERSION, dto.getVersion());
  }

  @Test
  @DisplayName("should register account with null reminder")
  void shouldRegisterAccountWithNullReminder() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    RegistrationDTO registrationDTO = createRegistrationDtoInstance(
        EMAIL,
        PASSWORD,
        SALT,
        null
    );

    Account account = createAccountInstance(
        ID,
        PUBLIC_ID,
        EMAIL,
        PASSWORD,
        SALT,
        null,
        ROLE,
        true,
        true,
        true,
        true,
        CREATED_AT,
        UPDATED_AT,
        VERSION
    );

    when(passwordEncoder.encode(any(String.class))).thenReturn("encoded" + PASSWORD);
    when(accountRepository.save(any(Account.class))).thenReturn(account);

    AccountDTO dto = accountService.register(registrationDTO);

    assertEquals(ID, dto.getId());
    assertEquals(PUBLIC_ID.toString(), dto.getPublicId().toString());
    assertEquals(EMAIL, dto.getEmail());
    assertEquals(PASSWORD, dto.getPassword());
    assertEquals(SALT, dto.getSalt());
    assertNull(dto.getReminder());
    assertEquals(ROLE.toString(), dto.getRole().toString());
    assertTrue(dto.isAccountNonExpired());
    assertTrue(dto.isAccountNonLocked());
    assertTrue(dto.isCredentialsNonExpired());
    assertTrue(dto.isEnabled());
    assertEquals(CREATED_AT, dto.getCreatedAt());
    assertEquals(UPDATED_AT, dto.getUpdatedAt());
    assertEquals(VERSION, dto.getVersion());
  }
}
