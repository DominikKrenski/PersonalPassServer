package org.dominik.pass.services.implementations;

import org.dominik.pass.data.dto.AccountDTO;
import org.dominik.pass.data.enums.Role;
import org.dominik.pass.db.entities.Account;
import org.dominik.pass.errors.exceptions.InternalException;
import org.dominik.pass.errors.exceptions.NotFoundException;
import org.dominik.pass.services.definitions.AccountService;
import org.dominik.pass.utils.EmailClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sendinblue.ApiException;
import sibModel.CreateSmtpEmail;

import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.UUID;

import static org.dominik.pass.utils.TestUtils.createAccountInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {
  private static Account account;

  @Mock EmailClient emailClient;
  @Mock AccountService accountService;
  @InjectMocks EmailServiceImpl emailService;

  @BeforeAll
  static void setUp() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    account = createAccountInstance(
        1L,
        UUID.randomUUID(),
        "dominik.krenski@gmail.com",
        "password",
        "salt",
        "reminder",
        Role.ROLE_USER,
        true,
        true,
        true,
        true,
        Instant.now(),
        Instant.now(),
        (short) 1
    );
  }

  @Test
  @DisplayName("should throw NotFound if email does not exist")
  void shouldThrowNotFoundIfEmailNotExists() {
    when(accountService.findByEmail(anyString())).thenThrow(new NotFoundException("Account does not exist"));

    assertThrows(NotFoundException.class, () -> emailService.sendHint("email"));
  }

  @Test
  @DisplayName("should throw InternalException if an error occurred during email send")
  void shouldThrowInternalExceptionIfErrorDuringEmailSendingOccurred() throws ApiException {
    when(accountService.findByEmail(anyString())).thenReturn(AccountDTO.fromAccount(account));
    when(emailClient.sendHintEmail(anyString(), anyString())).thenThrow(new ApiException("Cannot send email"));

    assertThrows(InternalException.class, () -> emailService.sendHint(anyString()));
  }

  @Test
  @DisplayName("should send hint email")
  void shouldSendHintEmail() throws ApiException {
    CreateSmtpEmail smtpEmail = new CreateSmtpEmail();
    smtpEmail.setMessageId("email-id");

    when(accountService.findByEmail(anyString())).thenReturn(AccountDTO.fromAccount(account));
    when(emailClient.sendHintEmail(anyString(), anyString())).thenReturn(smtpEmail);

    String result = emailService.sendHint("email");

    assertEquals("email-id", result);
  }
}
