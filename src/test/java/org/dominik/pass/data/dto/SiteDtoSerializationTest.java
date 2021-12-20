package org.dominik.pass.data.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import org.dominik.pass.data.enums.Role;
import org.dominik.pass.db.entities.Account;
import org.dominik.pass.db.entities.Site;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.UUID;

import static org.dominik.pass.utils.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SiteDtoSerializationTest {
  private static final Long ACCOUNT_ID = 1L;
  private static final Long SITE_ID = 3L;
  private static final UUID ACCOUNT_PUBLIC_ID = UUID.randomUUID();
  private static final UUID SITE_PUBLIC_ID = UUID.randomUUID();
  private static final String EMAIL = "dominik.krenski@gmail.com";
  private static final String PASSWORD = "b468879149f241f69ce185ee2cc1764047ece00f7aad0128053a12aee5be320c";
  private static final String SALT = "711882a4dc3dcb437eb6151c09025594";
  private static final String REMINDER = "dummy";
  private static final Role ROLE = Role.ROLE_USER;
  private static final Instant ACCOUNT_CREATED_AT = Instant.now().minusSeconds(3000);
  private static final Instant ACCOUNT_UPDATED_AT = Instant.now().minusSeconds(2000);
  private static final Instant SITE_CREATED_AT = Instant.now().minusSeconds(2000);
  private static final Instant SITE_UPDATED_AT = Instant.now().minusSeconds(200);
  private static final short ACCOUNT_VERSION = 0;
  private static final short SITE_VERSION = 2;
  private static final String ENTRY = "entry";

  private static Site site;
  private static ObjectMapper mapper;

  @BeforeAll
  static void setUp() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    Account account = createAccountInstance(
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

    mapper = createObjectMapperInstance();
  }

  @Test
  @DisplayName("should serialize site with account instance")
  void shouldSerializeSiteWithAccountInstance()  throws JsonProcessingException {
    SiteDTO dto = SiteDTO.fromSiteEager(site);
    String json = mapper.writeValueAsString(dto);

    ReadContext ctx = JsonPath.parse(json);

    assertEquals(SITE_PUBLIC_ID.toString(), ctx.read("$.publicId"));
    assertEquals(ENTRY, ctx.read("$.site"));
    assertEquals(convertInstantIntoString(SITE_CREATED_AT), ctx.read("$.createdAt"));
    assertEquals(convertInstantIntoString(SITE_UPDATED_AT), ctx.read("$.updatedAt"));
    assertThrows(PathNotFoundException.class, () -> ctx.read("$.id"));
    assertThrows(PathNotFoundException.class, () -> ctx.read("$.version"));
    assertThrows(PathNotFoundException.class, () -> ctx.read("$.account"));
  }

  @Test
  @DisplayName("should serialize site without account instance")
  void shouldSerializeSiteWithoutAccountInstance() throws JsonProcessingException {
    SiteDTO dto = SiteDTO.fromSiteLazy(site);
    String json = mapper.writeValueAsString(dto);

    ReadContext ctx = JsonPath.parse(json);

    assertEquals(SITE_PUBLIC_ID.toString(), ctx.read("$.publicId"));
    assertEquals(ENTRY, ctx.read("$.site"));
    assertEquals(convertInstantIntoString(SITE_CREATED_AT), ctx.read("$.createdAt"));
    assertEquals(convertInstantIntoString(SITE_UPDATED_AT), ctx.read("$.updatedAt"));
    assertThrows(PathNotFoundException.class, () -> ctx.read("$.id"));
    assertThrows(PathNotFoundException.class, () -> ctx.read("$.version"));
    assertThrows(PathNotFoundException.class, () -> ctx.read("$.account"));
  }
}
