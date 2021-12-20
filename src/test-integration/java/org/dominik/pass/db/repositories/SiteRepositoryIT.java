package org.dominik.pass.db.repositories;

import org.dominik.pass.configuration.DataJpaTestConfiguration;
import org.dominik.pass.db.entities.Account;
import org.dominik.pass.db.entities.Site;
import org.dominik.pass.errors.exceptions.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(
    properties = {
        "spring.main.banner-mode=off"
    }
)
@Import(DataJpaTestConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql("classpath:sql/05.site-repository-test.sql")
@ActiveProfiles("integration")
class SiteRepositoryIT {
  @Autowired private TestEntityManager em;
  @Autowired private AccountRepository accountRepository;
  @Autowired private SiteRepository siteRepository;

  @Test
  @DisplayName("should find all sites that belong to dominik")
  void shouldFindAllAddressesThatBelongToDominik() {
    Account account = accountRepository
        .findByEmail("dominik.krenski@gmail.com")
        .orElseThrow(() -> new NotFoundException("Account not foundA"));

    List<Site> sites = siteRepository.findAllByAccountPublicId(account.getPublicId());

    assertEquals(3, sites.size());
  }

  @Test
  @DisplayName("should find all sites that belong to dorota")
  void shouldFindAllSitesThatBelongToDorota() {
    Account account = accountRepository
        .findByEmail("dorciad@interia.pl")
        .orElseThrow(() -> new NotFoundException("Account not found"));

    List<Site> sites = siteRepository.findAllByAccountPublicId(account.getPublicId());

    assertEquals(1, sites.size());
  }

  @Test
  @DisplayName("should return empty list if account does not have any sites")
  void shouldReturnEmptyListIfAccountDoesNotHaveAnySites() {
    Account account = accountRepository
        .findByEmail("dominik@yahoo.com")
        .orElseThrow(() -> new NotFoundException("Account not found"));

    List<Site> sites = siteRepository.findAllByAccountPublicId(account.getPublicId());

    assertEquals(0, sites.size());
  }

  @Test
  @DisplayName("should find site by public id")
  void shouldFindSiteByPublicId() {
    List<Site> sites = siteRepository.findAll();
    Site site =
        siteRepository
            .findByPublicId(sites.get(0).getPublicId())
            .orElseThrow(() -> new NotFoundException("Site not found"));

    assertEquals(sites.get(0).getId(), site.getId());
    assertEquals(sites.get(0).getPublicId().toString(), site.getPublicId().toString());
    assertEquals(sites.get(0).getSite(), site.getSite());
  }

  @Test
  @DisplayName("should not find site with given public id")
  void shouldNotFindSiteWithGivenPublicId() {
    Optional<Site> site = siteRepository.findByPublicId(UUID.randomUUID());
    assertTrue(site.isEmpty());
  }

  @Test
  @DisplayName("should update site with  given public id")
  void shouldUpdateSiteWithGivenPublicId() {
    List<Site> sites = siteRepository.findAll();
    Site site = sites.get(2);

    int updated = siteRepository.updateSite("new dummy site", site.getPublicId());
    assertEquals(1, updated);
  }

  @Test
  @DisplayName("should not update if site with given public id does not exist")
  void shouldNotUpdateSiteWithGivenPublicIdDoesNotExist() {
    int updated = siteRepository.updateSite("new site", UUID.randomUUID());
    assertEquals(0, updated);
  }

  @Test
  @DisplayName("should delete site with given public id")
  void shouldDeleteSiteWithGivenPublicId() {
    List<Site> sites = siteRepository.findAll();
    int deleted = siteRepository.deleteSite(sites.get(3).getPublicId());

    assertEquals(1, deleted);
  }

  @Test
  @DisplayName("should not delete anything if site with given public id does not exist")
  void shouldNotDeleteAnythingIfSiteWithGivenPublicIdDoesNotExist() {
    int deleted = siteRepository.deleteSite(UUID.randomUUID());

    assertEquals(0, deleted);
  }
}
