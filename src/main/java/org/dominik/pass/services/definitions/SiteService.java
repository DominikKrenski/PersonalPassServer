package org.dominik.pass.services.definitions;

import org.dominik.pass.data.dto.SiteDTO;

import java.util.List;
import java.util.UUID;

public interface SiteService {
  SiteDTO save(String site, UUID accountPublicId);
  List<SiteDTO> getAllUserSites(UUID accountPublicId);
  SiteDTO getSite(UUID publicId);
  void updateSite(String site, UUID publicId);
  void deleteSite(UUID publicId);
}
