package org.dominik.pass.services.implementations;

import org.dominik.pass.data.dto.AccountDTO;
import org.dominik.pass.data.dto.RefreshTokenDTO;
import org.dominik.pass.data.enums.Role;
import org.dominik.pass.db.entities.Account;
import org.dominik.pass.db.entities.RefreshToken;
import org.dominik.pass.db.repositories.RefreshTokenRepository;
import org.dominik.pass.errors.exceptions.InternalException;
import org.dominik.pass.errors.exceptions.NotFoundException;
import org.dominik.pass.services.definitions.AccountService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.persistence.EntityManager;

import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.dominik.pass.utils.TestUtils.createAccountInstance;
import static org.dominik.pass.utils.TestUtils.createRefreshTokenInstance;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {
  private static final Long ID = 2L;
  private static final UUID PUBLIC_ID = UUID.randomUUID();
  private static final String EMAIL = "dominik.krenski@gmail.com";
  private static final String PASSWORD = "b468879149f241f69ce185ee2cc1764047ece00f7aad0128053a12aee5be320c";
  private static final String SALT = "711882a4dc3dcb437eb6151c09025594";
  private static final String REMINDER = "dummy message";
  private static final Role ROLE = Role.ROLE_ADMIN;
  private static final Instant CREATED_AT = Instant.now().minusSeconds(4300);
  private static final Instant UPDATED_AT = Instant.now().minusSeconds(3400);

  private static Account account;

  @Mock private AccountService accountService;
  @Mock private RefreshTokenRepository tokenRepository;
  @Mock private EntityManager em;

  @InjectMocks private RefreshTokenServiceImpl tokenService;

  @BeforeAll
  static void setUp() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    account = createAccountInstance(
        ID,
        PUBLIC_ID,
        EMAIL,
        PASSWORD,
        SALT,
        REMINDER,
        ROLE,
        true,
        true,
        true,
        false,
        CREATED_AT,
        UPDATED_AT,
        (short) 3
    );
  }

  @Test
  @DisplayName("should save token")
  void shouldSaveToken() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    ReflectionTestUtils.setField(tokenService, "em", em);

    AccountDTO accountDTO = AccountDTO.fromAccount(account);

    when(accountService.findByEmail(any(String.class))).thenReturn(accountDTO);
    when(em.merge(any(Account.class))).thenReturn(account);
    when(tokenRepository.deleteAllAccountTokensByPublicId(any(UUID.class))).thenReturn(2);

    tokenService.login("refresh_token", "dominik.krenski@gmail.com");

    verify(accountService).findByEmail(EMAIL);
    verify(em).merge(isA(Account.class));
    verify(tokenRepository).deleteAllAccountTokensByPublicId(isA(UUID.class));
    verify(tokenRepository).save(isA(RefreshToken.class));
  }

  @Test
  @DisplayName("should throw NotFoundException if account with given email does not exist")
  void shouldThrowNotFoundIfAccountWithEmailNotExists() {
    ReflectionTestUtils.setField(tokenService, "em", em);
    when(accountService.findByEmail(any(String.class))).thenThrow(new NotFoundException("Account does not exist"));
    assertThrows(NotFoundException.class, () -> tokenService.login("refresh_token", EMAIL));
  }

  @Test
  @DisplayName("should return number of deleted entries")
  void shouldReturnNumberOfDeletedEntries() {
    when(tokenRepository.deleteAllAccountTokensByPublicId(any(UUID.class))).thenReturn(1);

    int deleted = tokenService.deleteAllAccountTokens(PUBLIC_ID.toString());

    assertEquals(1, deleted);
  }


  @Test
  @DisplayName("should return dto instance if RefreshToken with token field exists")
  void shouldReturnDtoInstanceIfRefreshTokenWithTokenFieldExists() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    ReflectionTestUtils.setField(tokenService, "em", em);

    RefreshToken refreshToken = createRefreshTokenInstance(ID, "refresh_token", false, account, CREATED_AT, UPDATED_AT, (short) 0);

    when(tokenRepository.findByToken(anyString())).thenReturn(Optional.of(refreshToken));

    RefreshTokenDTO tokenDTO = tokenService.findByToken("refresh_token");

    assertEquals(refreshToken.getId(), tokenDTO.getId());
    assertEquals(refreshToken.getToken(), tokenDTO.getToken());
    assertNull(tokenDTO.getAccount());
  }

  @Test
  @DisplayName("should throw NotFoundException if RefreshToken with given token entry does not exist")
  void shouldThrowNotFoundIfRefreshTokenWithGivenTokenEntryDoesNotExist() {
    ReflectionTestUtils.setField(tokenService, "em", em);

    when(tokenRepository.findByToken(anyString())).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> tokenService.findByToken(PUBLIC_ID.toString()));
  }

  @Test
  @DisplayName("should save new refresh token")
  void shouldSaveNewRefreshToken() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    ReflectionTestUtils.setField(tokenService, "em", em);

    RefreshToken refreshToken = createRefreshTokenInstance(ID, "refresh_token", false, account, CREATED_AT, UPDATED_AT, (short) 1);


    when(tokenRepository.markTokenAsUsed(anyString())).thenReturn(1);
    when(accountService.findByPublicId(any(UUID.class))).thenReturn(AccountDTO.fromAccount(account));
    when(em.merge(any(Account.class))).thenReturn(account);
    when(tokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);

    tokenService.saveNewRefreshToken("old_token", "new_token", UUID.randomUUID().toString());

    verify(tokenRepository).markTokenAsUsed(anyString());
    verify(accountService).findByPublicId(any(UUID.class));
    verify(em).merge(any(Account.class));
    verify(tokenRepository).save(any(RefreshToken.class));
  }

  @Test
  @DisplayName("should throw NotFoundException if Account with given public id does not exist")
  void shouldThrowNotFoundIfAccountWithPublicIdDoesNotExist() {
    ReflectionTestUtils.setField(tokenService, "em", em);

    when(accountService.findByPublicId(any(UUID.class))).thenThrow(new NotFoundException("Account not found"));

    assertThrows(NotFoundException.class, () -> tokenService.saveNewRefreshToken("old_token", "new_token", UUID.randomUUID().toString()));

    verify(tokenRepository).markTokenAsUsed(anyString());
    verify(accountService).findByPublicId(any(UUID.class));
    verify(em, never()).merge(any(Account.class));
    verify(tokenRepository, never()).save(any(RefreshToken.class));
  }

  @Test
  @DisplayName("should throw InternalException if email was not updated")
  void shouldThrowInternalExceptionIfEmailWasNotUpdated() {
    when(tokenRepository.deleteAllAccountTokensByEmail(anyString())).thenReturn(1);
    when(accountService.updateEmail(anyString(), anyString())).thenReturn(0);

    assertThrows(
        InternalException.class,
        () -> tokenService.saveRefreshTokenAfterEmailUpdate("old", "new", "token")
    );

    verify(tokenRepository).deleteAllAccountTokensByEmail(anyString());
    verify(accountService).updateEmail(anyString(), anyString());
    verify(accountService, never()).findByEmail(anyString());
    verify(em, never()).merge(any(Account.class));
    verify(tokenRepository, never()).save(any(RefreshToken.class));
  }

  @Test
  @DisplayName("should throw NotFound if account with given email does not exist")
  void shouldThrowNotFoundIfAccountWithGivenEmailDoesNotExist() {
    when(tokenRepository.deleteAllAccountTokensByEmail(anyString())).thenReturn(0);
    when(accountService.updateEmail(anyString(), anyString())).thenReturn(1);
    when(accountService.findByEmail(anyString())).thenThrow(new NotFoundException("Account does not exist"));

    assertThrows(
        NotFoundException.class,
        () -> tokenService.saveRefreshTokenAfterEmailUpdate("old", "new", "token")
    );

    verify(tokenRepository).deleteAllAccountTokensByEmail(anyString());
    verify(accountService).updateEmail(anyString(), anyString());
    verify(accountService).findByEmail(anyString());
    verify(em, never()).merge(any(Account.class));
    verify(tokenRepository, never()).save(any(RefreshToken.class));
  }

  @Test
  @DisplayName("should save refresh token after email update")
  void shouldSaveRefreshTokenAfterEmailToken() {
    ReflectionTestUtils.setField(tokenService, "em", em);

    when(tokenRepository.deleteAllAccountTokensByEmail(anyString())).thenReturn(3);
    when(accountService.updateEmail(anyString(), anyString())).thenReturn(1);
    when(accountService.findByEmail(anyString())).thenReturn(AccountDTO.fromAccount(account));
    when(em.merge(any(Account.class))).thenReturn(account);
    when(tokenRepository.save(any(RefreshToken.class))).thenReturn(new RefreshToken("token", account));

    tokenService.saveRefreshTokenAfterEmailUpdate("new", "old", "token");

    verify(tokenRepository).deleteAllAccountTokensByEmail(anyString());
    verify(accountService).updateEmail(anyString(), anyString());
    verify(accountService).findByEmail(anyString());
    verify(em).merge(any(Account.class));
    verify(tokenRepository).save(any(RefreshToken.class));
  }
}
