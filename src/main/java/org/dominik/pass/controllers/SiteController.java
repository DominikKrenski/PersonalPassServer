package org.dominik.pass.controllers;

import lombok.extern.slf4j.Slf4j;
import org.dominik.pass.data.dto.EncryptedDataDTO;
import org.dominik.pass.data.dto.SiteDTO;
import org.dominik.pass.security.AccountDetails;
import org.dominik.pass.security.utils.SecurityUtils;
import org.dominik.pass.services.definitions.SiteService;
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
@RequestMapping(value = "/sites", produces = MediaType.APPLICATION_JSON_VALUE)
public class SiteController {
  private final SecurityUtils securityUtils;
  private final SiteService siteService;

  @Autowired
  public SiteController(SecurityUtils securityUtils, SiteService siteService) {
    this.securityUtils = securityUtils;
    this.siteService = siteService;
  }

  @GetMapping(
      value = {"", "/"},
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  @PreAuthorize("hasRole('ROLE_USER')")
  public List<SiteDTO> getAllUserSites() {
    AccountDetails accountDetails = securityUtils.getPrincipal();
    return siteService.getAllUserSites(accountDetails.getPublicId());
  }

  @GetMapping(
      value ="/{id}",
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  @PreAuthorize("hasRole('ROLE_USER')")
  public SiteDTO getSite(@PathVariable UUID id) {
    return siteService.getSite(id);
  }

  @PostMapping(
      value = {"", "/"},
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  @PreAuthorize("hasRole('ROLE_USER')")
  public SiteDTO createSite(@Valid @RequestBody EncryptedDataDTO dataDTO) {
    AccountDetails accountDetails = securityUtils.getPrincipal();

    return siteService.save(dataDTO.getData(), accountDetails.getPublicId());
  }

  @PutMapping(
      value = "/{id}",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('ROLE_USER')")
  public void updateSite(@Valid @RequestBody EncryptedDataDTO dataDTO, @PathVariable UUID id) {
    siteService.updateSite(dataDTO.getData(), id);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('ROLE_USER')")
  public void deleteSite(@PathVariable UUID id) {
    siteService.deleteSite(id);
  }
}
