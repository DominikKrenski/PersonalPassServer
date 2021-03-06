package org.dominik.pass.services.implementations;

import org.dominik.pass.data.dto.AccountDTO;
import org.dominik.pass.data.dto.RegistrationDTO;
import org.dominik.pass.data.enums.Role;
import org.dominik.pass.db.entities.Account;
import org.dominik.pass.db.repositories.AccountRepository;
import org.dominik.pass.errors.exceptions.ConflictException;
import org.dominik.pass.errors.exceptions.InternalException;
import org.dominik.pass.errors.exceptions.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import javax.persistence.EntityManager;
import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.dominik.pass.utils.TestUtils.createAccountInstance;
import static org.dominik.pass.utils.TestUtils.createRegistrationDtoInstance;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
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
  @Mock EntityManager em;
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
    when(accountRepository.existsByEmail(any(String.class))).thenReturn(false);

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
    when(accountRepository.existsByEmail(any(String.class))).thenReturn(false);

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

  @Test
  @DisplayName("should throw conflict exception if account with given email already exists")
  void shouldThrowConflictExceptionIfAccountWithGivenEmailAlreadyExists() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    RegistrationDTO registrationDTO = createRegistrationDtoInstance(
        EMAIL,
        PASSWORD,
        SALT,
        null
    );

    when(accountRepository.existsByEmail(EMAIL)).thenReturn(true);

    assertThrows(ConflictException.class, () -> accountService.register(registrationDTO));
  }

  @Test
  @DisplayName("should return AccountDTO instance if account with given email exists")
  void shouldReturnAccountDtoInstanceIfAccountWithGivenEmailExists() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
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
        (short) 0
    );

    when(accountRepository.findByEmail(any(String.class))).thenReturn(Optional.of(account));

    AccountDTO accountDTO = accountService.findByEmail(EMAIL);

    assertEquals(account.getId(), accountDTO.getId());
    assertEquals(account.getPublicId().toString(), accountDTO.getPublicId().toString());
    assertEquals(account.getEmail(), accountDTO.getEmail());
    assertEquals(account.getPassword(), accountDTO.getPassword());
    assertEquals(account.getSalt(), accountDTO.getSalt());
    assertEquals(account.getReminder(), accountDTO.getReminder());
    assertEquals(account.getRole().toString(), accountDTO.getRole().toString());
    assertTrue(accountDTO.isAccountNonExpired());
    assertTrue(accountDTO.isAccountNonLocked());
    assertFalse(accountDTO.isCredentialsNonExpired());
    assertFalse(accountDTO.isEnabled());
    assertEquals(account.getCreatedAt(), accountDTO.getCreatedAt());
    assertEquals(account.getUpdatedAt(), accountDTO.getUpdatedAt());
    assertEquals(account.getVersion(), accountDTO.getVersion());
  }

  @Test
  @DisplayName("should throw NotFoundException if account with given email does not exist")
  void shouldThrowNotFoundExceptionIfAccountWithGivenEmailDoesNotExist() {
    when(accountRepository.findByEmail(any(String.class))).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> accountService.findByEmail(EMAIL));
  }

  @Test
  @DisplayName("should return AccountDTO instance if account with given public id exists")
  void shouldReturnAccountDtoInstanceIfAccountWithGivenPublicIdExists() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
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
        (short) 0
    );

    when(accountRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(account));

    AccountDTO accountDTO = accountService.findByPublicId(PUBLIC_ID);

    assertEquals(account.getId(), accountDTO.getId());
    assertEquals(account.getPublicId().toString(), accountDTO.getPublicId().toString());
    assertEquals(account.getEmail(), accountDTO.getEmail());
    assertEquals(account.getPassword(), accountDTO.getPassword());
    assertEquals(account.getSalt(), accountDTO.getSalt());
    assertEquals(account.getReminder(), accountDTO.getReminder());
    assertEquals(account.getRole().toString(), accountDTO.getRole().toString());
    assertTrue(accountDTO.isAccountNonExpired());
    assertTrue(accountDTO.isAccountNonLocked());
    assertFalse(accountDTO.isCredentialsNonExpired());
    assertFalse(accountDTO.isEnabled());
    assertEquals(account.getCreatedAt(), accountDTO.getCreatedAt());
    assertEquals(account.getUpdatedAt(), accountDTO.getUpdatedAt());
    assertEquals(account.getVersion(), accountDTO.getVersion());
  }

  @Test
  @DisplayName("should throw NotFoundException if account with given public id does not exist")
  void shouldThrowNotFoundIfAccountWithGivenPublicIdDoesNotExist() {
    when(accountRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> accountService.findByPublicId(PUBLIC_ID));
  }

  @Test
  @DisplayName("should throw Conflict if email is already in use")
  void shouldThrowConflictIfEmailIsAlreadyInUse() {
    when(accountRepository.existsByEmail(anyString())).thenReturn(true);

    assertThrows(ConflictException.class, () -> accountService.updateEmail("new", "old"));
  }

  @Test
  @DisplayName("should throw InternalException if account has not been updated")
  void shouldThrowInternalExceptionIfAccountNotUpdated() {
    when(accountRepository.existsByEmail(anyString())).thenReturn(false);
    when(accountRepository.updateEmail(anyString(), anyString())).thenReturn(0);

    assertThrows(InternalException.class, () -> accountService.updateEmail("new", "old"));
  }

  @Test
  @DisplayName("should return updated account")
  void shouldReturnUpdatedAccount() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    ReflectionTestUtils.setField(accountService, "em", em);

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

    when(accountRepository.existsByEmail(anyString())).thenReturn(false);
    when(accountRepository.updateEmail("new", "old")).thenReturn(1);
    when(accountRepository.findByEmail(anyString())).thenReturn(Optional.of(account));
    doNothing().when(em).clear();

    assertNotNull(accountService.updateEmail("new", "old"));
  }

  @Test
  @DisplayName("should throw NotFound if account with new email could not be found")
  void shouldThrowNotFoundIfAccountWithNewEmailNotExist() {
    ReflectionTestUtils.setField(accountService, "em", em);

    when(accountRepository.existsByEmail(anyString())).thenReturn(false);
    when(accountRepository.updateEmail(anyString(), anyString())).thenReturn(1);
    when(accountRepository.findByEmail(anyString())).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> accountService.updateEmail("new", "old"));
  }

  @Test
  @DisplayName("should throw NotFound if account to be deleted with given public id does not exist")
  void shouldThrowNotFoundIfAccounToBeDeletedtWithGivenPublicIdDoesNotExist() {
    when(accountRepository.deleteAccount(any(UUID.class))).thenReturn(0);

    assertThrows(NotFoundException.class, () -> accountService.deleteAccount(UUID.randomUUID()));
  }

  @Test
  @DisplayName("should delete account")
  void shouldDeleteAccount() {
    when(accountRepository.deleteAccount(any(UUID.class))).thenReturn(1);

    accountService.deleteAccount(UUID.randomUUID());

    verify(accountRepository).deleteAccount(any(UUID.class));
  }

  @Test
  @DisplayName("should update password")
  void shouldUpdatePassword() {
    when(accountRepository.updatePassword(any(UUID.class), anyString(), anyString(), anyString())).thenReturn(1);
    when(passwordEncoder.encode(anyString())).thenReturn("encoded");

    accountService.updatePassword(UUID.randomUUID(), "new_password", "new_salt", "new reminder");

    verify(accountRepository).updatePassword(any(UUID.class), anyString(), anyString(), anyString());
  }

  @Test
  @DisplayName("should not update password")
  void shouldNotUpdatePassword() {
    when(accountRepository.updatePassword(any(UUID.class), anyString(), anyString(), anyString())).thenReturn(0);
    when(passwordEncoder.encode(anyString())).thenReturn("encoded");

    assertThrows(NotFoundException.class, () -> accountService.updatePassword(UUID.randomUUID(), "pass", "salt", "rmdr"));
  }
}
