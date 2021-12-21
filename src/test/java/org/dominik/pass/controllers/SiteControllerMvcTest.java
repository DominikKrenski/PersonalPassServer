package org.dominik.pass.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import org.dominik.pass.configuration.ApiControllerMvcTestConfig;
import org.dominik.pass.data.dto.AccountDTO;
import org.dominik.pass.data.dto.SiteDTO;
import org.dominik.pass.data.enums.Role;
import org.dominik.pass.db.entities.Account;
import org.dominik.pass.db.entities.Site;
import org.dominik.pass.errors.exceptions.NotFoundException;
import org.dominik.pass.security.AccountDetails;
import org.dominik.pass.security.utils.SecurityUtils;
import org.dominik.pass.services.definitions.SiteService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;

import static org.dominik.pass.utils.TestUtils.*;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    value = SiteController.class,
    excludeAutoConfiguration = {UserDetailsServiceAutoConfiguration.class},
    properties = {
        "spring.main.banner-mode=off"
    }
)
@WithMockUser
@Import(ApiControllerMvcTestConfig.class)
@ActiveProfiles("test")
class SiteControllerMvcTest {
  private static final String SITE_BASE_URL = "/sites";
  private static final Long ACCOUNT_ID = 1L;
  private static final Long SITE_ID = 1L;
  private static final UUID ACCOUNT_PUBLIC_ID = UUID.randomUUID();
  private static final UUID SITE_PUBLIC_ID = UUID.randomUUID();
  private static final String EMAIL = "dominik.krenski@gmail.com";
  private static final String PASSWORD = "b468879149f241f69ce185ee2cc1764047ece00f7aad0128053a12aee5be320c";
  private static final String SALT = "711882a4dc3dcb437eb6151c09025594";
  private static final Role ROLE = Role.ROLE_USER;
  private static final String REMINDER = "dummy message";
  private static final Instant ACCOUNT_CREATED_AT = Instant.now().minusSeconds(5000);
  private static final Instant ACCOUNT_UPDATED_AT = Instant.now().minusSeconds(2000);
  private static final Instant SITE_CREATED_AT = Instant.now().minusSeconds(50);
  private static final Instant SITE_UPDATED_AT = Instant.now().minusSeconds(40);
  private static final short ACCOUNT_VERSION = 0;
  private static final short SITE_VERSION = 0;
  private static final String ENTRY = "50d00dbe0817df9d676a8a2d.af3453c9";
  private static final String TIMESTAMP_PATTERN = "\\d{2}/\\d{2}/\\d{4}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z";

  private static Properties props;
  private static Account account;
  private static Site site;

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper mapper;
  @Autowired SecurityUtils securityUtils;
  @Autowired SiteService siteService;

  @BeforeAll
  static void setUp() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    props = readPropertiesFile("ValidationMessages.properties");
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
  @DisplayName("should create site")
  void shouldCreateSite() throws Exception {
    String data = """
        {
          "data": "50d00dbe0817df9d676a8a2d.af3453c9"
        }
        """;

    when(securityUtils.getPrincipal()).thenReturn(AccountDetails.fromDTO(AccountDTO.fromAccount(account)));
    when(siteService.save(anyString(), any(UUID.class))).thenReturn(SiteDTO.fromSiteLazy(site));

    mvc
        .perform(
            post(SITE_BASE_URL)
                .content(data)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.publicId").value(site.getPublicId().toString()))
        .andExpect(jsonPath("$.site").value(site.getSite()))
        .andExpect(jsonPath("$.createdAt").value(convertInstantIntoString(site.getCreatedAt())))
        .andExpect(jsonPath("$.updatedAt").value(convertInstantIntoString(site.getUpdatedAt())));
  }

  @Test
  @DisplayName("should return UnprocessableEntity if data is blank")
  void shouldReturnUnprocessableEntityIfDataIsBlank() throws Exception {
    String data = """
        {
          "data": "   "
        }
        """;

    List<String> messages = new LinkedList<>(
        List.of(
            props.getProperty("data.blank.message"),
            props.getProperty("data.pattern.message")
        )
    );

    mvc
        .perform(
            post(SITE_BASE_URL)
                .content(data)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isUnprocessableEntity())
        .andDo(res -> {
          String body = res.getResponse().getContentAsString();
          ReadContext ctx = JsonPath.parse(body);
          String errors = getSubErrorsString(body);

          Map<String, TestValidationError> map = convertErrorListToMap(
              mapper.readValue(errors, new TypeReference<>(){})
          );

          assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase(), ctx.read("$.status"));
          assertEquals("Validation Error", ctx.read("$.message"));
          assertTrue(Pattern.matches(TIMESTAMP_PATTERN, ctx.read("$.timestamp")));
          assertEquals("data", map.get("data").getField());
          assertEquals("   ", map.get("data").getRejectedValue());
          assertTrue(map.get("data").getValidationMessages().containsAll(messages));
        });
  }

  @Test
  @DisplayName("should return UnprocessableEntity if data is not formatted properly")
  void shouldReturnUnprocessableEntityIfDataIsNotFormattedProperly() throws Exception {
    String data = """
        {
          "data": "50d00dbe0817df9d676a8a2.af3453c9"
        }
        """;

    mvc
        .perform(
            post(SITE_BASE_URL)
                .content(data)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andDo(res -> {
          String body = res.getResponse().getContentAsString();
          ReadContext ctx = JsonPath.parse(body);
          String errors = getSubErrorsString(body);

          Map<String, TestValidationError> map = convertErrorListToMap(
              mapper.readValue(errors, new TypeReference<>(){})
          );

          assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase(), ctx.read("$.status"));
          assertEquals("Validation Error", ctx.read("$.message"));
          assertTrue(Pattern.matches(TIMESTAMP_PATTERN, ctx.read("$.timestamp")));
          assertEquals("data", map.get("data").getField());
          assertEquals("50d00dbe0817df9d676a8a2.af3453c9", map.get("data").getRejectedValue());
          assertTrue(map.get("data").getValidationMessages().contains(props.getProperty("data.pattern.message")));
        });
  }

  @Test
  @DisplayName("should return all user sites")
  void shouldReturnAllUserSites() throws Exception {
    Site site_1 = createSiteInstance(
        2L,
        UUID.randomUUID(),
        "site_1",
        account,
        SITE_CREATED_AT,
        SITE_UPDATED_AT,
        SITE_VERSION
    );

    Site site_2 = createSiteInstance(
        3L,
        UUID.randomUUID(),
        "site_2",
        account,
        SITE_CREATED_AT,
        SITE_UPDATED_AT,
        SITE_VERSION
    );

    when(securityUtils.getPrincipal()).thenReturn(AccountDetails.fromDTO(AccountDTO.fromAccount(account)));
    when(siteService.getAllUserSites(any(UUID.class))).thenReturn(List.of(
        SiteDTO.fromSiteEager(site),
        SiteDTO.fromSiteEager(site_1),
        SiteDTO.fromSiteEager(site_2)
    ));

    mvc
        .perform(
            get(SITE_BASE_URL)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andDo(res -> {
          String body = res.getResponse().getContentAsString();
          var entries = mapper.readValue(body, new TypeReference<List<TestSiteDTO>>(){});

          assertEquals(3, entries.size());
        });
  }

  @Test
  @DisplayName("should return an empty list if user has no sites")
  void shouldReturnEmptyListIfUserHasNoSites() throws Exception {
    when(securityUtils.getPrincipal()).thenReturn(AccountDetails.fromDTO(AccountDTO.fromAccount(account)));
    when(siteService.getAllUserSites(any(UUID.class))).thenReturn(new LinkedList<>());

    mvc
        .perform(
            get(SITE_BASE_URL)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andDo(res -> {
          String body = res.getResponse().getContentAsString();
          var entries = mapper.readValue(body, new TypeReference<List<TestSiteDTO>>(){});

          assertEquals(0, entries.size());
        });
  }

  @Test
  @DisplayName("should return site with given public id")
  void shouldReturnSiteWithGivenPublicId() throws Exception {
    when(securityUtils.getPrincipal()).thenReturn(AccountDetails.fromDTO(AccountDTO.fromAccount(account)));
    when(siteService.getSite(any(UUID.class))).thenReturn(SiteDTO.fromSiteLazy(site));

    mvc
        .perform(
            get(SITE_BASE_URL + "/" + UUID.randomUUID())
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.publicId").value(site.getPublicId().toString()))
        .andExpect(jsonPath("$.site").value(site.getSite()))
        .andExpect(jsonPath("$.createdAt").value(convertInstantIntoString(site.getCreatedAt())))
        .andExpect(jsonPath("$.updatedAt").value(convertInstantIntoString(site.getUpdatedAt())));
  }

  @Test
  @DisplayName("should return NotFound if site with given public id does not exist")
  void shouldReturnNotFoundIfSiteWithGivenPublicIdDoesNotExist() throws Exception {
    when(securityUtils.getPrincipal()).thenReturn(AccountDetails.fromDTO(AccountDTO.fromAccount(account)));
    when(siteService.getSite(any(UUID.class))).thenThrow(new NotFoundException("Site does not exist"));

    mvc
        .perform(
            get(SITE_BASE_URL + "/" + UUID.randomUUID())
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.getReasonPhrase()))
        .andExpect(jsonPath("$.message").value("Site does not exist"))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)));
  }

  @Test
  @DisplayName("should return BadRequest if path variable is not valid UUID")
  void shouldReturnBadRequestIfPathVariableIsNotValidUUID() throws Exception {
    mvc
        .perform(
            get(SITE_BASE_URL + "/afjakjv")
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
        .andExpect(jsonPath("$.message").value("Bad Path Variable"))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)));
  }

  @Test
  @DisplayName("should update site")
  void shouldUpdateSite() throws Exception {
    String data = """
        {
          "data": "50d00dbe0817df9d676a8a2d.af3453c9"
        }
        """;

    when(securityUtils.getPrincipal()).thenReturn(AccountDetails.fromDTO(AccountDTO.fromAccount(account)));
    doNothing().when(siteService).updateSite(anyString(), any(UUID.class));

    mvc
        .perform(
            put(SITE_BASE_URL + "/" + UUID.randomUUID())
                .content(data)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("should return NotFound if site could not be updated")
  void shouldReturnNotFoundIfSiteCouldNotBeUpdated() throws Exception {
    String data = """
        {
          "data": "50d00dbe0817df9d676a8a2d.af3453c9"
        }
        """;

    when(securityUtils.getPrincipal()).thenReturn(AccountDetails.fromDTO(AccountDTO.fromAccount(account)));
    doThrow(new NotFoundException("Site not found")).when(siteService).updateSite(anyString(), any(UUID.class));

    mvc
        .perform(
            put(SITE_BASE_URL + "/" + UUID.randomUUID())
                .content(data)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.getReasonPhrase()))
        .andExpect(jsonPath("$.message").value("Site not found"))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)));
  }

  @Test
  @DisplayName("should return UnprocessableEntity if both parameters are invalid")
  void shouldReturnUnprocessableEntityIfBothParametersAreInvalid() throws Exception {
    String data = """
        {
          "data": "afadfafaf"
        }
        """;

    mvc
        .perform(
            put(SITE_BASE_URL + "/afadfgr")
                .content(data)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isUnprocessableEntity());
  }

  @Test
  @DisplayName("should not update site if path variable is invalid")
  void shouldNotUpdateSiteIfPathVariableIsInvalid() throws Exception {
    String data = """
        {
          "data": "50d00dbe0817df9d676a8a2d.af3453c9"
        }
        """;

    mvc
        .perform(
            put(SITE_BASE_URL + "/adkfrio")
                .content(data)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
        .andExpect(jsonPath("$.message").value("Bad Path Variable"))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)));
  }

  @Test
  @DisplayName("should return MethodNotAllowed if path variable is missing in PUT request")
  void shouldReturnMethodNotAllowedIfPathVariableIsMissingInPutRequest() throws Exception {
    String data = """
        {
          "data": "50d00dbe0817df9d676a8a2d.af3453c9"
        }
        """;

    mvc
        .perform(
            put(SITE_BASE_URL)
                .content(data)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isMethodNotAllowed())
        .andExpect(jsonPath("$.status").value(HttpStatus.METHOD_NOT_ALLOWED.getReasonPhrase()))
        .andExpect(jsonPath("$.message").value("Method not allowed for given route"))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)));
  }

  @Test
  @DisplayName("should delete site")
  void shouldDeleteSite() throws Exception {
    when(securityUtils.getPrincipal()).thenReturn(AccountDetails.fromDTO(AccountDTO.fromAccount(account)));
    doNothing().when(siteService).deleteSite(any(UUID.class));

    mvc
        .perform(
            delete(SITE_BASE_URL + "/" + UUID.randomUUID())
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("should return MethodNotAllowed if path variable is missing in DELETE request")
  void shouldReturnMethodNotAllowedIfPathVariableIsMissingInDeleteRequest() throws Exception {
    mvc
        .perform(
            delete(SITE_BASE_URL)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isMethodNotAllowed())
        .andExpect(jsonPath("$.status").value(HttpStatus.METHOD_NOT_ALLOWED.getReasonPhrase()))
        .andExpect(jsonPath("$.message").value("Method not allowed for given route"))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)));
  }

  @Test
  @DisplayName("should return BadRequest if path variable is not valid in DELETE request")
  void shouldReturnBadRequestIfPathVariableIsNotValidInDeleteRequest() throws Exception {
    mvc
        .perform(
            delete(SITE_BASE_URL + "/dfgwrt4")
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
        .andExpect(jsonPath("$.message").value("Bad Path Variable"))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)));
  }

  @Test
  @DisplayName("should return NotFound if site could not be deleted")
  void shouldReturnNotFoundIfSiteCouldNotBeDeleted() throws Exception {
    when(securityUtils.getPrincipal()).thenReturn(AccountDetails.fromDTO(AccountDTO.fromAccount(account)));
    doThrow(new NotFoundException("Site not found")).when(siteService).deleteSite(any(UUID.class));

    mvc
        .perform(
            delete(SITE_BASE_URL + "/" + UUID.randomUUID())
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.getReasonPhrase()))
        .andExpect(jsonPath("$.message").value("Site not found"))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)));
  }
}
