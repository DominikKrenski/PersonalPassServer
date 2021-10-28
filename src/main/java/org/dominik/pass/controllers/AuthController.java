package org.dominik.pass.controllers;

import lombok.extern.slf4j.Slf4j;
import org.dominik.pass.data.dto.AccountDTO;
import org.dominik.pass.data.dto.AuthDTO;
import org.dominik.pass.data.dto.RegistrationDTO;
import org.dominik.pass.services.definitions.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping(value = "/auth")
public class AuthController {
  private final AccountService accountService;

  @Autowired
  public AuthController(AccountService accountService) {
    this.accountService = accountService;
  }

  @PostMapping(value = "/signup", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public AuthDTO signup(@Valid @RequestBody RegistrationDTO dto) {
    AccountDTO accountDTO = accountService.register(dto);
    return AuthDTO
        .builder()
        .publicId(accountDTO.getPublicId())
        .build();
  }
}
