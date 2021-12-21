package org.dominik.pass.controllers;

import lombok.extern.slf4j.Slf4j;
import org.dominik.pass.data.dto.AddressDTO;
import org.dominik.pass.data.dto.EncryptedDataDTO;
import org.dominik.pass.security.AccountDetails;
import org.dominik.pass.security.utils.SecurityUtils;
import org.dominik.pass.services.definitions.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(value = "/addresses", produces = MediaType.APPLICATION_JSON_VALUE)
public class AddressController {
  private final SecurityUtils securityUtils;
  private final AddressService addressService;

  @Autowired
  public AddressController(SecurityUtils securityUtils, AddressService addressService) {
    this.securityUtils = securityUtils;
    this.addressService = addressService;
  }

  @GetMapping(
      value = {"", "/"},
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  @PreAuthorize("hasRole('ROLE_USER')")
  public List<AddressDTO> getAllUserAddresses() {
    AccountDetails accountDetails = securityUtils.getPrincipal();
    return addressService.getAllUserAddresses(accountDetails.getPublicId());
  }

  @GetMapping(
      value = "/{id}",
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  @PreAuthorize("hasRole('ROLE_USER')")
  public AddressDTO getAddress(@PathVariable UUID id) {
    return addressService.getAddress(id);
  }

  @PostMapping(
      value = {"", "/"},
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  @PreAuthorize("hasRole('ROLE_USER')")
  public AddressDTO createAddress(@Valid @RequestBody EncryptedDataDTO addressData) {
    AccountDetails accountDetails = securityUtils.getPrincipal();

    return addressService.save(addressData.getData(), accountDetails.getPublicId());
  }

  @PutMapping(
      value = "/{id}",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('ROLE_USER')")
  public void updatedAddress(@Valid @RequestBody EncryptedDataDTO addressData, @PathVariable UUID id) {
    addressService.updateAddress(addressData.getData(), id);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('ROLE_USER')")
  public void deleteAddress(@PathVariable UUID id) {
    addressService.deleteAddress(id);
  }
}
