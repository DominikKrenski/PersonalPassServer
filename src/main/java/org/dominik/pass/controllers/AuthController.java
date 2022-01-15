package org.dominik.pass.controllers;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.dominik.pass.data.dto.AccountDTO;
import org.dominik.pass.data.dto.AuthDTO;
import org.dominik.pass.data.dto.RegistrationDTO;
import org.dominik.pass.security.AccountDetails;
import org.dominik.pass.security.utils.SecurityUtils;
import org.dominik.pass.services.definitions.AccountService;
import org.dominik.pass.services.definitions.KeyService;
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
  private final KeyService keyService;
  private final SecurityUtils securityUtils;

  @Autowired
  public AuthController(
      AccountService accountService,
      RefreshTokenService tokenService,
      KeyService keyService,
      SecurityUtils securityUtils
  ) {
    this.accountService = accountService;
    this.tokenService = tokenService;
    this.keyService = keyService;
    this.securityUtils = securityUtils;
  }

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

  @GetMapping(
      value = "/signout"
  )
  @PreAuthorize("hasRole('ROLE_USER')")
  public ResponseEntity<Object> signout() {
    AccountDetails accountDetails = securityUtils.getPrincipal();
    tokenService.logout(accountDetails.getPublicId().toString());

    return ResponseEntity.ok().build();
  }

  @Getter
  private static final class Data {
    @NotBlank(message = "{email.blank.message}")
    @Length(max = 360, message = "{email.length.message}")
    @EmailAddress(message = "{email.format.message}")
    private String email;
  }
}
