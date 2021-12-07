package org.dominik.pass.services.implementations;


import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.dominik.pass.data.dto.AccountDTO;
import org.dominik.pass.errors.exceptions.InternalException;
import org.dominik.pass.services.definitions.AccountService;
import org.dominik.pass.services.definitions.EmailService;
import org.dominik.pass.utils.EmailClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sendinblue.ApiException;
import sibModel.CreateSmtpEmail;

import java.util.Arrays;

@Slf4j
@Component
public class EmailServiceImpl implements EmailService {
  private final EmailClient emailClient;
  private final AccountService accountService;

  @Autowired
  public EmailServiceImpl(EmailClient emailClient, AccountService accountService) {
    this.emailClient = emailClient;
    this.accountService = accountService;
  }

  public String sendHint(@NonNull String email) {
    AccountDTO accountDTO = accountService.findByEmail(email);

    try {
      CreateSmtpEmail result = emailClient.sendHintEmail(email, accountDTO.getReminder());
      return result.getMessageId();
    } catch (ApiException ex) {
      log.error("SEND HINT ERROR: " + Arrays.toString(ex.getStackTrace()));
      throw new InternalException("There is a problem with sending email");
    }
  }

  @Override
  public String sendTestEmail(@NonNull String email) {
    try {
      CreateSmtpEmail result = emailClient.sendTestEmail(email);
      return result.getMessageId();
    } catch (ApiException ex) {
      log.error("SENT TEST EMAIL ERROR: " + Arrays.toString(ex.getStackTrace()));
      throw new InternalException("There is a problem with sending email");
    }
  }
}
