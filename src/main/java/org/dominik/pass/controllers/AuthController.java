package org.dominik.pass.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.dominik.pass.data.dto.AccountDTO;
import org.dominik.pass.data.dto.AuthDTO;
import org.dominik.pass.data.dto.RegistrationDTO;
import org.dominik.pass.security.AccountDetails;
import org.dominik.pass.security.utils.SecurityUtils;
import org.dominik.pass.services.definitions.AccountService;
import org.dominik.pass.services.definitions.RefreshTokenService;
import org.dominik.pass.utils.validators.EmailAddress;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

@Slf4j
@RestController
@RequestMapping(value = "/auth")
public class AuthController {
  private final AccountService accountService;
  private final RefreshTokenService tokenService;
  private final SecurityUtils securityUtils;

  @Autowired
  public AuthController(
      AccountService accountService,
      RefreshTokenService tokenService,
      SecurityUtils securityUtils
  ) {
    this.accountService = accountService;
    this.tokenService = tokenService;
    this.securityUtils = securityUtils;
  }

  @Operation(summary = "Create a new account")
  @ApiResponse(
    responseCode = "200",
    description = "An account has been created successfully",
    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthDTO.class))
  )
  @PostMapping(
      value = "/signup",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  public AuthDTO signup(@Valid @RequestBody RegistrationDTO dto) {
    AccountDTO accountDTO = accountService.register(dto);
    return AuthDTO
        .builder()
        .publicId(accountDTO.getPublicId())
        .build();
  }

  @Operation(summary = "Send salt assigned to account with given email address")
  @ApiResponse(
    responseCode = "200",
    description = "Salt has been found",
    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthDTO.class))
  )
  @PostMapping(
      value = "/salt",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  public AuthDTO salt(@Valid @RequestBody Data data) {
    AccountDTO accountDTO = accountService.findByEmail(data.getEmail());
    return AuthDTO
        .builder()
        .salt(accountDTO.getSalt())
        .build();
  }

  @Operation(summary = "Log out user")
  @ApiResponse(
    responseCode = "200",
    description = "User has been log out successfully"
  )
  @GetMapping(
      value = "/signout"
  )
  @PreAuthorize("hasRole('ROLE_USER')")
  public ResponseEntity<Object> signout() {
    AccountDetails accountDetails = securityUtils.getPrincipal();
    tokenService.logout(accountDetails.getPublicId().toString());

    return ResponseEntity.ok().build();
  }

  @Schema(
    description = "Class used to get password salt belonging to user with given email address"
  )
  @Getter
  private static final class Data {

    @Schema(
      description = "User's email address",
      name = "email",
      required = true,
      maxLength = 360
    )
    @NotBlank(message = "{email.blank.message}")
    @Length(max = 360, message = "{email.length.message}")
    @EmailAddress(message = "{email.format.message}")
    private String email;
  }
}
