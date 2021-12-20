package org.dominik.pass.services.implementations;

import lombok.NonNull;
import org.dominik.pass.data.dto.AccountDTO;
import org.dominik.pass.data.dto.SiteDTO;
import org.dominik.pass.db.entities.Account;
import org.dominik.pass.db.entities.Site;
import org.dominik.pass.db.repositories.SiteRepository;
import org.dominik.pass.errors.exceptions.NotFoundException;
import org.dominik.pass.services.definitions.AccountService;
import org.dominik.pass.services.definitions.SiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SiteServiceImpl implements SiteService {
  private final SiteRepository siteRepository;
  private final AccountService accountService;

  @PersistenceContext private EntityManager em;

  @Autowired
  public SiteServiceImpl(SiteRepository siteRepository, AccountService accountService) {
    this.siteRepository = siteRepository;
    this.accountService = accountService;
  }

  @Override
  @Transactional
  public SiteDTO save(@NonNull String site, @NonNull UUID accountPublicId) {
    AccountDTO accountDTO = accountService.findByPublicId(accountPublicId);
    Account account = em.merge(Account.fromDTO(accountDTO));

    Site newSite = new Site(site, account);

    return SiteDTO.fromSiteLazy(siteRepository.save(newSite));
  }

  @Override
  public List<SiteDTO> getAllUserSites(@NonNull UUID accountPublicId) {
    List<Site> sites = siteRepository.findAllByAccountPublicId(accountPublicId);

    return sites
        .stream()
        .map(SiteDTO::fromSiteLazy)
        .collect(Collectors.toList());
  }

  @Override
  public SiteDTO getSite(@NonNull UUID publicId) {
    return siteRepository
        .findByPublicId(publicId)
        .map(SiteDTO::fromSiteLazy)
        .orElseThrow(() -> new NotFoundException("Site with given id does not exist"));
  }

  @Override
  @Transactional
  public void updateSite(String site, UUID publicId) {
    int updated = siteRepository.updateSite(site, publicId);

    if (updated != 1)
      throw new NotFoundException("Site with given id does not exist");
  }

  @Override
  @Transactional
  public void deleteSite(@NonNull UUID publicId) {
    int deleted = siteRepository.deleteSite(publicId);

    if (deleted != 1)
      throw new NotFoundException("Site with given id does not exist");
  }
}
