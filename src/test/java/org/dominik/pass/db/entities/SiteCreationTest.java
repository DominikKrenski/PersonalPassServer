package org.dominik.pass.db.entities;

import org.dominik.pass.data.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.UUID;

import static org.dominik.pass.utils.TestUtils.createAccountInstance;
import static org.junit.jupiter.api.Assertions.*;

class SiteCreationTest {
  private static final Long ID = 1L;
  private static final UUID PUBLIC_ID = UUID.randomUUID();
  private static final String EMAIL = "dominik.krenski@gmail.com";
  private static final String PASSWORD = "b468879149f241f69ce185ee2cc1764047ece00f7aad0128053a12aee5be320c";
  private static final String SALT = "711882a4dc3dcb437eb6151c09025594";
  private static final String REMINDER = "dummy";
  private static final Role ROLE = Role.ROLE_USER;
  private static final Instant CREATED_AT = Instant.now().minusSeconds(5000);
  private static final Instant UPDATED_AT = Instant.now().minusSeconds(2000);
  private static final short VERSION = 0;

  @Test
  @DisplayName("should create instance")
  void shouldCreateInstance() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    Account account = createAccountInstance(
        ID,
        PUBLIC_ID,
        EMAIL,
        PASSWORD,
        SALT,
        REMINDER,
        ROLE,
        false,
        false,
        true,
        true,
        CREATED_AT,
        UPDATED_AT,
        VERSION
    );

    Site site = new Site("entry", account);

    assertNull(site.getId());
    assertNotNull(site.getPublicId());
    assertEquals("entry", site.getSite());
    assertNotNull(site.getAccount());
    assertNull(site.getCreatedAt());
    assertNull(site.getUpdatedAt());
    assertEquals(0, site.getVersion());
  }
}
