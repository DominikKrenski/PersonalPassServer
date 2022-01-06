package org.dominik.pass.controllers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.dominik.pass.data.dto.AccountDTO;
import org.dominik.pass.data.dto.AuthDTO;
import org.dominik.pass.data.dto.UpdatePasswordDTO;
import org.dominik.pass.security.AccountDetails;
import org.dominik.pass.security.utils.SecurityUtils;
import org.dominik.pass.services.definitions.AccountService;
import org.dominik.pass.services.definitions.DataService;
import org.dominik.pass.services.definitions.EmailService;
import org.dominik.pass.utils.validators.EmailAddress;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

@Slf4j
@Validated
@RestController
@RequestMapping(value = "/accounts", produces = MediaType.APPLICATION_JSON_VALUE)
public class AccountController {
  private final SecurityUtils securityUtils;
  private final AccountService accountService;
  private final DataService dataService;
  private final EmailService emailService;

  @Autowired
  public AccountController(
      SecurityUtils securityUtils,
      AccountService accountService,
      DataService dataService,
      EmailService emailService
  ) {
    this.securityUtils = securityUtils;
    this.accountService = accountService;
    this.dataService = dataService;
    this.emailService = emailService;
  }

  @GetMapping(
      value = "/",
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  @PreAuthorize("hasRole('ROLE_USER')")
  public AccountDTO getAccount() {
    AccountDetails accountDetails = securityUtils.getPrincipal();
    return accountService.findByEmail(accountDetails.getUsername());
  }

  @PutMapping(
      value = "/email",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  @PreAuthorize("hasRole('ROLE_USER')")
  @Validated(EmailUpdate.class)
  public AccountDTO updateEmail(@Valid @RequestBody AccountData body) {
    AccountDetails accountDetails = securityUtils.getPrincipal();

    return accountService.updateEmail(body.getEmail(), accountDetails.getUsername());
  }

  @PostMapping(
      value = "/hint",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  @Validated(EmailUpdate.class)
  public EmailResponse sendReminderEmail(@Valid @RequestBody AccountData body) {
    return new EmailResponse(emailService.sendHint(body.getEmail()));
  }

  @GetMapping(
      value = "/test-email",
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  @PreAuthorize("hasRole('ROLE_USER')")
  public EmailResponse sendTestEmail() {
    AccountDetails accountDetails = securityUtils.getPrincipal();

    return new EmailResponse(emailService.sendTestEmail(accountDetails.getUsername()));
  }

  @DeleteMapping(
      value = {"", "/"},
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('ROLE_USER')")
  public void deleteAccount() {
    AccountDetails accountDetails = securityUtils.getPrincipal();

    accountService.deleteAccount(accountDetails.getPublicId());
  }

  @GetMapping(
      value="/salt",
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  @PreAuthorize("hasRole('ROLE_USER')")
  public AuthDTO getSalt() {
    AccountDetails accountDetails = securityUtils.getPrincipal();
    AccountDTO accountDTO = accountService.findByPublicId(accountDetails.getPublicId());
    return AuthDTO.builder().salt(accountDTO.getSalt()).build();
  }

  @PutMapping(
    consumes = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('ROLE_USER')")
  public void updatePassword(@RequestBody @Valid UpdatePasswordDTO passwordDTO) {
    AccountDetails accountDetails = securityUtils.getPrincipal();
    log.debug(passwordDTO.toString());

    dataService.updateAllData(accountDetails.getPublicId(), passwordDTO);
  }

  @AllArgsConstructor
  @Getter
  @ToString
  private static final class EmailResponse {
    private String emailId;
  }

  @Getter
  @ToString
  private static final class AccountData {
    @NotBlank(message = "{email.blank.message}", groups = EmailUpdate.class)
    @Length(message = "{email.length.message}", groups = EmailUpdate.class)
    @EmailAddress(message = "{email.format.message}", groups = EmailUpdate.class)
    private String email;

    @NotBlank(message = "{reminder.blank.message}", groups = ReminderUpdate.class)
    @Length(max = 255, message = "{reminder.length.message}", groups = ReminderUpdate.class)
    private String reminder;
  }

  private interface EmailUpdate {}
  private interface ReminderUpdate {}
}
