package org.dominik.pass.data.dto;

import org.dominik.pass.data.enums.Role;
import org.dominik.pass.db.entities.Account;
import org.dominik.pass.db.entities.Site;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.UUID;

import static org.dominik.pass.utils.TestUtils.createAccountInstance;
import static org.dominik.pass.utils.TestUtils.createSiteInstance;
import static org.junit.jupiter.api.Assertions.*;

class SiteDtoCreationTest {
  private static final Long ACCOUNT_ID = 1L;
  private static final Long SITE_ID = 2L;
  private static final UUID ACCOUNT_PUBLIC_ID = UUID.randomUUID();
  private static final UUID SITE_PUBLIC_ID = UUID.randomUUID();
  private static final String EMAIL = "dominik.krenski@gmail.com";
  private static final String PASSWORD = "b468879149f241f69ce185ee2cc1764047ece00f7aad0128053a12aee5be320c";
  private static final String SALT = "711882a4dc3dcb437eb6151c09025594";
  private static final String REMINDER = "dummy";
  private static final Role ROLE = Role.ROLE_ADMIN;
  private static final Instant ACCOUNT_CREATED_AT = Instant.now().minusSeconds(3000);
  private static final Instant ACCOUNT_UPDATED_AT = Instant.now().minusSeconds(1500);
  private static final Instant SITE_CREATED_AT = Instant.now().minusSeconds(2000);
  private static final Instant SITE_UPDATED_AT = Instant.now().minusSeconds(400);
  private static final short ACCOUNT_VERSION = 1;
  private static final short SITE_VERSION = 0;
  private static final String ENTRY = "entry";

  private static Account account;
  private static Site site;

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
  @DisplayName("should create instance without account")
  void shouldCreateInstanceWithoutAccount() {
    SiteDTO dto = SiteDTO.fromSiteLazy(site);

    assertEquals(SITE_ID, dto.getId());
    assertEquals(SITE_PUBLIC_ID.toString(), dto.getPublicId().toString());
    assertEquals(ENTRY, dto.getSite());
    assertNull(dto.getAccount());
    assertEquals(SITE_CREATED_AT, dto.getCreatedAt());
    assertEquals(SITE_UPDATED_AT, dto.getUpdatedAt());
    assertEquals(SITE_VERSION, dto.getVersion());
  }

  @Test
  @DisplayName("should create instance with account")
  void shouldCreateInstanceWithAccount() {
    SiteDTO dto = SiteDTO.fromSiteEager(site);

    assertEquals(SITE_ID, dto.getId());
    assertEquals(SITE_PUBLIC_ID, dto.getPublicId());
    assertEquals(ENTRY, dto.getSite());
    assertEquals(SITE_CREATED_AT, dto.getCreatedAt());
    assertEquals(SITE_UPDATED_AT, dto.getUpdatedAt());
    assertEquals(SITE_VERSION, dto.getVersion());

    assertNotNull(dto.getAccount());
    assertEquals(ACCOUNT_ID, dto.getAccount().getId());
    assertEquals(ACCOUNT_PUBLIC_ID.toString(), dto.getAccount().getPublicId().toString());
    assertEquals(EMAIL, dto.getAccount().getEmail());
    assertEquals(PASSWORD, dto.getAccount().getPassword());
    assertEquals(SALT, dto.getAccount().getSalt());
    assertEquals(REMINDER, dto.getAccount().getReminder());
    assertEquals(ROLE.toString(), dto.getAccount().getRole().toString());
    assertTrue(dto.getAccount().isAccountNonExpired());
    assertTrue(dto.getAccount().isAccountNonLocked());
    assertTrue(dto.getAccount().isCredentialsNonExpired());
    assertTrue(dto.getAccount().isEnabled());
    assertEquals(ACCOUNT_CREATED_AT, dto.getAccount().getCreatedAt());
    assertEquals(ACCOUNT_UPDATED_AT, dto.getAccount().getUpdatedAt());
    assertEquals(ACCOUNT_VERSION, dto.getAccount().getVersion());
  }
}
