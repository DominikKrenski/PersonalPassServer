package org.dominik.pass.services.implementations;

import org.dominik.pass.data.dto.AccountDTO;
import org.dominik.pass.data.dto.SiteDTO;
import org.dominik.pass.data.enums.Role;
import org.dominik.pass.db.entities.Account;
import org.dominik.pass.db.entities.Site;
import org.dominik.pass.db.repositories.SiteRepository;
import org.dominik.pass.errors.exceptions.NotFoundException;
import org.dominik.pass.services.definitions.AccountService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.persistence.EntityManager;
import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.dominik.pass.utils.TestUtils.createAccountInstance;
import static org.dominik.pass.utils.TestUtils.createSiteInstance;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SiteServiceTest {
  private static final Long ACCOUNT_ID = 1L;
  private static final Long SITE_ID = 1L;
  private static final UUID ACCOUNT_PUBLIC_ID = UUID.randomUUID();
  private static final UUID SITE_PUBLIC_ID = UUID.randomUUID();
  private static final String EMAIL = "dominik.krenski@gmail.com";
  private static final String PASSWORD = "b468879149f241f69ce185ee2cc1764047ece00f7aad0128053a12aee5be320c";
  private static final String SALT = "711882a4dc3dcb437eb6151c09025594";
  private static final String REMINDER = "dummy message";
  private static final Role ROLE = Role.ROLE_ADMIN;
  private static final Instant ACCOUNT_CREATED_AT = Instant.now().minusSeconds(4000);
  private static final Instant ACCOUNT_UPDATED_AT = Instant.now().minusSeconds(2400);
  private static final Instant SITE_CREATED_AT = Instant.now().minusSeconds(100);
  private static final Instant SITE_UPDATED_AT = Instant.now().minusSeconds(100);
  private static final short ACCOUNT_VERSION = 0;
  private static final short SITE_VERSION = 0;
  private static final String ENTRY = "site_1";

  private static Account account;
  private static Site site;

  @Mock private SiteRepository siteRepository;
  @Mock private AccountService accountService;
  @Mock private EntityManager em;
  @InjectMocks private SiteServiceImpl siteService;

  @BeforeAll
  static void setUp() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    account = createAccountInstance(
        ACCOUNT_ID,
        ACCOUNT_PUBLIC_ID,
        EMAIL,
        PASSWORD,
        SALT,
        REMINDER,
        ROLE,
        true,
        true,
        true,
        true,
        ACCOUNT_CREATED_AT,
        ACCOUNT_UPDATED_AT,
        ACCOUNT_VERSION
    );

    site = createSiteInstance(
        SITE_ID,
        SITE_PUBLIC_ID,
        ENTRY,
        account,
        SITE_CREATED_AT,
        SITE_UPDATED_AT,
        SITE_VERSION
    );
  }

  @Test
  @DisplayName("should save new site")
  void shouldSaveNewSite() {
    ReflectionTestUtils.setField(siteService, "em", em);

    when(accountService.findByPublicId(any(UUID.class))).thenReturn(AccountDTO.fromAccount(account));
    when(em.merge(any(Account.class))).thenReturn(account);
    when(siteRepository.save(any(Site.class))).thenReturn(site);

    SiteDTO dto = siteService.save("new site", SITE_PUBLIC_ID);

    assertEquals(SITE_ID, dto.getId());
    assertEquals(SITE_PUBLIC_ID.toString(), dto.getPublicId().toString());
    assertEquals(ENTRY, dto.getSite());
    assertNull(dto.getAccount());
    assertEquals(SITE_CREATED_AT, dto.getCreatedAt());
    assertEquals(SITE_UPDATED_AT, dto.getUpdatedAt());
    assertEquals(SITE_VERSION, dto.getVersion());
  }

  @Test
  @DisplayName("should return all sites belonging to user")
  void shouldReturnAllSitesBelongingToUser() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    UUID sitePublicId = UUID.randomUUID();
    String entry = "second entry";
    Instant createdAt = Instant.now().minusSeconds(20);
    Instant updatedAt = createdAt.minusSeconds(10);
    short version = 1;

    Site site2 = createSiteInstance(2L, sitePublicId, entry, account, createdAt, updatedAt, version);

    when(siteRepository.findAllByAccountPublicId(any(UUID.class))).thenReturn(List.of(site, site2));

    List<SiteDTO> sites = siteService.getAllUserSites(UUID.randomUUID());

    assertEquals(2, sites.size());
  }

  @Test
  @DisplayName("should return empty list if user has no sites")
  void shouldReturnEmptyListIfUserHasNoSites() {
    when(siteRepository.findAllByAccountPublicId(any(UUID.class))).thenReturn(new ArrayList<>());

    List<SiteDTO> sites = siteService.getAllUserSites(UUID.randomUUID());

    assertEquals(0, sites.size());
  }

  @Test
  @DisplayName("should return site by public id")
  void shouldReturnSiteByPublicId() {
    when(siteRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(site));

    SiteDTO dto = siteService.getSite(UUID.randomUUID());

    assertEquals(SITE_ID, dto.getId());
    assertEquals(SITE_PUBLIC_ID, dto.getPublicId());
    assertEquals(ENTRY, dto.getSite());
    assertNull(dto.getAccount());
    assertEquals(SITE_CREATED_AT, dto.getCreatedAt());
    assertEquals(SITE_UPDATED_AT, dto.getUpdatedAt());
    assertEquals(SITE_VERSION, dto.getVersion());
  }

  @Test
  @DisplayName("should throw NotFound if site with given public id does not exist")
  void shouldThrowNotFoundIfSiteWithGivenPublicIdDoesNotExist() {
    when(siteRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> siteService.getSite(UUID.randomUUID()));
  }

  @Test
  @DisplayName("should update site")
  void shouldUpdateSite() {
    when(siteRepository.updateSite(anyString(), any(UUID.class))).thenReturn(1);

    siteService.updateSite("site", UUID.randomUUID());

    verify(siteRepository).updateSite(anyString(), any(UUID.class));
  }

  @Test
  @DisplayName("should not update site")
  void shouldNotUpdateSite() {
    when(siteRepository.updateSite(anyString(), any(UUID.class))).thenReturn(0);

    assertThrows(NotFoundException.class, () -> siteService.updateSite("address", UUID.randomUUID()));
  }

  @Test
  @DisplayName("should delete site")
  void shouldDeleteSite() {
    when(siteRepository.deleteSite(any(UUID.class))).thenReturn(1);

    siteService.deleteSite(UUID.randomUUID());

    verify(siteRepository).deleteSite(any(UUID.class));
  }

  @Test
  @DisplayName("should not delete site")
  void shouldNotDeleteSite() {
    when(siteRepository.deleteSite(any(UUID.class))).thenReturn(0);

    assertThrows(NotFoundException.class, () -> siteService.deleteSite(UUID.randomUUID()));
  }
}
