package org.dominik.pass.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

  @Operation(summary = "Get account")
  @ApiResponse(
    responseCode = "200",
    description = "Get account",
    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AccountDTO.class))
  )
  @GetMapping(
      value = "/",
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  @PreAuthorize("hasRole('ROLE_USER')")
  public AccountDTO getAccount() {
    AccountDetails accountDetails = securityUtils.getPrincipal();
    return accountService.findByEmail(accountDetails.getUsername());
  }

  @Operation(summary = "Update account's email address")
  @ApiResponse(
    responseCode = "200",
    description = "Email address has been updated successfully",
    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AccountDTO.class))
  )
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

  @Operation(summary = "Send email with password reminder")
  @ApiResponse(
    responseCode = "200",
    description = "Email has been sent",
    content = @Content(mediaType = "application/json", schema = @Schema(implementation = EmailResponse.class))
  )
  @PostMapping(
      value = "/hint",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  @Validated(EmailUpdate.class)
  public EmailResponse sendReminderEmail(@Valid @RequestBody AccountData body) {
    return new EmailResponse(emailService.sendHint(body.getEmail()));
  }

  @Operation(summary = "Send test email")
  @ApiResponse(
    responseCode = "200",
    description = "Test email has been sent",
    content = @Content(mediaType = "application/json", schema = @Schema(implementation = EmailResponse.class))
  )
  @GetMapping(
      value = "/test-email",
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  @PreAuthorize("hasRole('ROLE_USER')")
  public EmailResponse sendTestEmail() {
    AccountDetails accountDetails = securityUtils.getPrincipal();

    return new EmailResponse(emailService.sendTestEmail(accountDetails.getUsername()));
  }

  @Operation(summary = "Delete account")
  @ApiResponse(
    responseCode = "200",
    description = "Account has been deleted"
  )
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

  @Operation(summary = "Get salt assigned to given account")
  @ApiResponse(
    responseCode = "200",
    description = "Salt has been found",
    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthDTO.class))
  )
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

  @Operation(summary = "Update password")
  @ApiResponse(
    responseCode = "204",
    description = "Password and all related data has been updated"
  )
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

  @Schema(
    description = "Class used to pass successful response from Sendinblue API"
  )
  @AllArgsConstructor
  @Getter
  @ToString
  private static final class EmailResponse {

    @Schema(
      description = "Id of the email that has been sent successfully",
      name = "emailId",
      required = true
    )
    private String emailId;
  }

  @Schema(
    description = "Class used to update email and send email with password reminder"
  )
  @Getter
  @ToString
  private static final class AccountData {

    @Schema(
      description = "User's email address",
      name = "email",
      maxLength = 360
    )
    @NotBlank(message = "{email.blank.message}", groups = EmailUpdate.class)
    @Length(message = "{email.length.message}", groups = EmailUpdate.class)
    @EmailAddress(message = "{email.format.message}", groups = EmailUpdate.class)
    private String email;

    @Schema(
      description = "Password reminder",
      name = "reminder",
      maxLength = 255
    )
    @NotBlank(message = "{reminder.blank.message}", groups = ReminderUpdate.class)
    @Length(max = 255, message = "{reminder.length.message}", groups = ReminderUpdate.class)
    private String reminder;
  }

  private interface EmailUpdate {}
  private interface ReminderUpdate {}
}
