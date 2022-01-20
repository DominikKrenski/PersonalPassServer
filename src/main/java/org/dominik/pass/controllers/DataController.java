package org.dominik.pass.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

  @Operation(summary = "Save new data")
  @ApiResponse(
    responseCode = "200",
    description = "Data has been saved",
    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DataDTO.class))
  )
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

  @Operation(summary = "Update data with given public id")
  @ApiResponse(
    responseCode = "204",
    description = "Data has been updated"
  )
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

  @Operation(summary = "Delete data with given public id")
  @ApiResponse(
    responseCode = "204",
    description = "Data has been deleted"
  )
  @DeleteMapping(value = "/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('ROLE_USER')")
  public void deleteData(@PathVariable UUID id) {
    log.debug("PUBLIC ID: " + id);
    dataService.deleteData(id);
  }

  @Operation(summary = "Delete all data belonging to user")
  @ApiResponse(
    responseCode = "204",
    description = "All user data has been deleted"
  )
  @DeleteMapping(value = {"", "/"})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('ROLE_USER')")
  public void deleteAllUserData() {
    AccountDetails accountDetails = securityUtils.getPrincipal();

    dataService.deleteAllUserData(accountDetails.getPublicId());
  }

  @Operation(summary = "Find data by public id")
  @ApiResponse(
    responseCode = "200",
    description = "Data has been found",
    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DataDTO.class))
  )
  @GetMapping(
      value = "/{id}",
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  @PreAuthorize("hasRole('ROLE_USER')")
  public DataDTO findData(@PathVariable UUID id) {
    log.debug("DATA PUBLIC ID: " + id);
    return dataService.findData(id);
  }

  @Operation(summary = "Find all user data with given type")
  @ApiResponse(
    responseCode = "200",
    description = "Find all data with given type",
    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = DataDTO.class)))
  )
  @GetMapping(
      value = "",
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  @PreAuthorize("hasRole('ROLE_USER')")
  List<DataDTO> findAllUserDataByType(@RequestParam DataType type) {
    AccountDetails accountDetails = securityUtils.getPrincipal();
    return dataService.findAllUserDataByType(type, accountDetails.getPublicId());
  }

  @Operation(summary = "Get all user data")
  @ApiResponse(
    responseCode = "200",
    description = "Find all user data",
    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = DataDTO.class)))
  )
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
