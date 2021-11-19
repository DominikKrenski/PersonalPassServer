package org.dominik.pass.controllers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.dominik.pass.data.dto.AccountDTO;
import org.dominik.pass.data.dto.AuthDTO;
import org.dominik.pass.errors.exceptions.ConflictException;
import org.dominik.pass.errors.exceptions.InternalException;
import org.dominik.pass.security.AccountDetails;
import org.dominik.pass.security.utils.JwtUtils;
import org.dominik.pass.security.utils.SecurityUtils;
import org.dominik.pass.services.definitions.AccountService;
import org.dominik.pass.services.definitions.EmailService;
import org.dominik.pass.services.definitions.RefreshTokenService;
import org.dominik.pass.utils.validators.EmailAddress;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
  private final RefreshTokenService refreshTokenService;
  private final EmailService emailService;
  private final JwtUtils jwtUtils;

  @Autowired
  public AccountController(
      SecurityUtils securityUtils,
      AccountService accountService,
      RefreshTokenService refreshTokenService,
      EmailService emailService,
      JwtUtils jwtUtils
  ) {
    this.securityUtils = securityUtils;
    this.accountService = accountService;
    this.refreshTokenService = refreshTokenService;
    this.emailService = emailService;
    this.jwtUtils = jwtUtils;
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
  public AuthDTO updateEmail(@Valid @RequestBody AccountData body) {
    // check if new email is not in use already
    if (accountService.existsByEmail(body.getEmail()))
      throw new ConflictException("Email is already in use");

    AccountDetails accountDetails = securityUtils.getPrincipal();

    // generate a new pair of tokens
    String accessToken = jwtUtils.createToken(accountDetails.getPublicId().toString(), JwtUtils.TokenType.ACCESS_TOKEN);
    String refreshToken = jwtUtils.createToken(accountDetails.getPublicId().toString(), JwtUtils.TokenType.REFRESH_TOKEN);

    // save new refresh token in database
    refreshTokenService.saveRefreshTokenAfterEmailUpdate(body.getEmail(), accountDetails.getUsername(), refreshToken);

    // create new AuthDTO instance
    return AuthDTO.builder().accessToken(accessToken).refreshToken(refreshToken).build();
  }

  @PutMapping(
      value = "/reminder",
      consumes = MediaType.APPLICATION_JSON_VALUE
  )
  @PreAuthorize("hasRole('ROLE_USER')")
  @Validated(ReminderUpdate.class)
  public ResponseEntity<Object> updateReminder(@Valid @RequestBody AccountData body) {
    AccountDetails accountDetails = securityUtils.getPrincipal();
    int updated = accountService.updateReminder(body.getReminder(), accountDetails.getUsername());

    log.debug("UPDATED ROWS: " + updated);

   if (updated != 1)
     throw new InternalException("Reminder cannot be updated");

   return ResponseEntity.noContent().build();
  }

  @PostMapping(
      value = "/hint",
      consumes = MediaType.APPLICATION_JSON_VALUE
  )
  @Validated(EmailUpdate.class)
  public EmailResponse sendReminderEmail(@Valid @RequestBody AccountData body) {
    return new EmailResponse(emailService.sendHint(body.getEmail()));
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
