package org.dominik.pass.controllers;

import lombok.extern.slf4j.Slf4j;
import org.dominik.pass.data.dto.DataDTO;
import org.dominik.pass.data.dto.EncryptedDataDTO;
import org.dominik.pass.data.enums.DataType;
import org.dominik.pass.security.AccountDetails;
import org.dominik.pass.security.utils.SecurityUtils;
import org.dominik.pass.services.definitions.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@Slf4j
@Validated
@RestController
@RequestMapping(
    value = "/data",
    produces = MediaType.APPLICATION_JSON_VALUE
)
public class DataController {
  private final SecurityUtils securityUtils;
  private final DataService dataService;

  @Autowired
  public DataController(SecurityUtils securityUtils, DataService dataService) {
    this.securityUtils = securityUtils;
    this.dataService = dataService;
  }

  @PostMapping(
      value = {"", "/"},
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  @PreAuthorize("hasRole('ROLE_USER')")
  @Validated(EncryptedDataDTO.DataCreate.class)
  public DataDTO saveData(@Valid @RequestBody EncryptedDataDTO encryptedData) {
    log.debug("NEW DATA: " + encryptedData);
    AccountDetails accountDetails = securityUtils.getPrincipal();

    return dataService.save(encryptedData.getEntry(), encryptedData.getType(), accountDetails.getPublicId());
  }

  @PutMapping(
      value = "/{id}",
      consumes = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('ROLE_USER')")
  @Validated(EncryptedDataDTO.DataUpdate.class)
  public void updateData(@Valid @RequestBody EncryptedDataDTO encryptedData, @PathVariable UUID id) {
    log.debug("DATA: " + encryptedData);
    log.debug("PATH VARIABLE: " + id);
    dataService.updateData(encryptedData.getEntry(), id);
  }

  @DeleteMapping(value = "/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('ROLE_USER')")
  public void deleteData(@PathVariable UUID id) {
    log.debug("PUBLIC ID: " + id);
    dataService.deleteData(id);
  }

  @DeleteMapping(value = {"", "/"})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('ROLE_USER')")
  public void deleteAllUserData() {
    AccountDetails accountDetails = securityUtils.getPrincipal();

    dataService.deleteAllUserData(accountDetails.getPublicId());
  }

  @GetMapping(
      value = "/{id}",
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  @PreAuthorize("hasRole('ROLE_USER')")
  public DataDTO findData(@PathVariable UUID id) {
    log.debug("DATA PUBLIC ID: " + id);
    return dataService.findData(id);
  }

  @GetMapping(
      value = "",
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  @PreAuthorize("hasRole('ROLE_USER')")
  List<DataDTO> findAllUserDataByType(@RequestParam DataType type) {
    AccountDetails accountDetails = securityUtils.getPrincipal();
    return dataService.findAllUserDataByType(type, accountDetails.getPublicId());
  }

  @GetMapping(
      value = "/all",
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  @PreAuthorize("hasRole('ROLE_USER')")
  List<DataDTO> findAllUserData() {
    AccountDetails accountDetails = securityUtils.getPrincipal();
    return dataService.findAllUserData(accountDetails.getPublicId());
  }
}
