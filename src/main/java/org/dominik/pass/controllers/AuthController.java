package org.dominik.pass.controllers;

import lombok.extern.slf4j.Slf4j;
import org.dominik.pass.data.dto.RegistrationDTO;
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

  @PostMapping(value = "/signup", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public void signup(@Valid @RequestBody RegistrationDTO dto) {
    log.debug("SIGNUP METHOD REACHED");
  }
}