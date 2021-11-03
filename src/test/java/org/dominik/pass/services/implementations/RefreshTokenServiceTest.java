package org.dominik.pass.services.implementations;

import org.dominik.pass.data.dto.AccountDTO;
import org.dominik.pass.data.enums.Role;
import org.dominik.pass.db.entities.Account;
import org.dominik.pass.db.entities.RefreshToken;
import org.dominik.pass.db.repositories.RefreshTokenRepository;
import org.dominik.pass.errors.exceptions.NotFoundException;
import org.dominik.pass.services.definitions.AccountService;
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
import java.util.UUID;

import static org.dominik.pass.utils.TestUtils.createAccountInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RefreshTokenServiceTest {
  private static final Long ID = 2L;
  private static final UUID PUBLIC_ID = UUID.randomUUID();
  private static final String EMAIL = "dominik.krenski@gmail.com";
  private static final String PASSWORD = "b468879149f241f69ce185ee2cc1764047ece00f7aad0128053a12aee5be320c";
  private static final String SALT = "711882a4dc3dcb437eb6151c09025594";
  private static final String REMINDER = "dummy message";
  private static final Role ROLE = Role.ROLE_ADMIN;
  private static final Instant CREATED_AT = Instant.now().minusSeconds(4300);
  private static final Instant UPDATED_AT = Instant.now().minusSeconds(3400);

  @Mock private AccountService accountService;
  @Mock private RefreshTokenRepository tokenRepository;
  @Mock private EntityManager em;

  @InjectMocks private RefreshTokenServiceImpl tokenService;

  @Test
  @DisplayName("should save token")
  void shouldSaveToken() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    ReflectionTestUtils.setField(tokenService, "em", em);

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
        true,
        false,
        CREATED_AT,
        UPDATED_AT,
        (short) 3
    );

    AccountDTO accountDTO = AccountDTO.fromAccount(account);

    when(accountService.findByEmail(any(String.class))).thenReturn(accountDTO);
    when(em.merge(any(Account.class))).thenReturn(account);
    when(tokenRepository.deleteAllAccountTokens(any(UUID.class))).thenReturn(2);

    tokenService.login("refresh_token", "dominik.krenski@gmail.com");

    verify(accountService).findByEmail(EMAIL);
    verify(em).merge(isA(Account.class));
    verify(tokenRepository).deleteAllAccountTokens(isA(UUID.class));
    verify(tokenRepository).save(isA(RefreshToken.class));
  }

  @Test
  @DisplayName("should throw NotFoundException if account with given email does not exist")
  void shouldThrowNotFoundIfAccountWithEmailNotExists() {
    ReflectionTestUtils.setField(tokenService, "em", em);
    when(accountService.findByEmail(any(String.class))).thenThrow(new NotFoundException("Account does not exist"));
    assertThrows(NotFoundException.class, () -> tokenService.login("refresh_token", EMAIL));
  }
}
