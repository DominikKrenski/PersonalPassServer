package org.dominik.pass.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import org.dominik.pass.data.dto.SiteDTO;
import org.dominik.pass.services.definitions.SiteService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;

import static org.dominik.pass.utils.TestUtils.*;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.profiles.active=integration"
    }
)
@AutoConfigureMockMvc
@Transactional
@Sql("classpath:sql/06.site-controller-test.sql")
@ActiveProfiles("integration")
class SiteControllerBootTestIT {
  private static final String AUTH_HEADER = "Authorization";
  private static final String SITE_BASE_URL = "/sites";
  private static final String ISSUER = "personal-pass.dev";
  private static final String AUDIENCE = "access";
  private static final String KEY = "gUkXn2r5u8x/A?D(G+KbPeShVmYq3s6v9y$B&E)H@McQfTjWnZr4u7w!z%C*F-Ja";
  private static final String TIMESTAMP_PATTERN = "\\d{2}/\\d{2}/\\d{4}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z";
  private static final String UUID_PATTERN = "^[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-4[0-9A-Fa-f]{3}-[89ABab][0-9A-Fa-f]{3}-[0-9A-Fa-f]{12}$";
  private static final String ACCOUNT_PUBLIC_ID = "d85db87a-df23-49e3-baef-8523e84902d1";

  private static Properties props;
  private String accessToken;

  @Autowired
  MockMvc mvc;
  @Autowired
  ObjectMapper mapper;
  @Autowired
  SiteService siteService;

  @BeforeAll
  static void setUp() {
    props = readPropertiesFile("ValidationMessages.properties");
  }

  @BeforeEach
  void beforeEach() throws NoSuchAlgorithmException, InvalidKeyException, JsonProcessingException {
    long createdAt = Instant.now().getEpochSecond();
    accessToken = generateJwtToken(ISSUER, ACCOUNT_PUBLIC_ID, createdAt, AUDIENCE, createdAt + 1000, KEY);
  }

  @Test
  @DisplayName("should create new site")
  void shouldCreateNewAddress() throws Exception {
    String data = """
        {
          "data": "abd00dbe0817df9d676a8a2d.af3453c9"
        }
        """;

    mvc
        .perform(
            post(SITE_BASE_URL)
                .header(AUTH_HEADER, "Bearer " + accessToken)
                .content(data)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.publicId", matchesPattern(UUID_PATTERN)))
        .andExpect(jsonPath("$.site").value("abd00dbe0817df9d676a8a2d.af3453c9"))
        .andExpect(jsonPath("$.createdAt", matchesPattern(TIMESTAMP_PATTERN)))
        .andExpect(jsonPath("$.updatedAt", matchesPattern(TIMESTAMP_PATTERN)));
  }

  @Test
  @DisplayName("should return UnprocessableEntity if site is blank")
  void shouldReturnUnprocessableEntityIfSiteIsBlank() throws Exception {
    String data = """
        {
          "data": " "
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
                .header(AUTH_HEADER, "Bearer " + accessToken)
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
              mapper.readValue(errors, new TypeReference<List<TestValidationError>>() {
              })
          );

          assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase(), ctx.read("$.status"));
          assertEquals("Validation Error", ctx.read("$.message"));
          assertTrue(Pattern.matches(TIMESTAMP_PATTERN, ctx.read("$.timestamp")));
          assertEquals("data", map.get("data").getField());
          assertEquals(" ", map.get("data").getRejectedValue());
          assertTrue(map.get("data").getValidationMessages().containsAll(messages));
        });
  }

  @Test
  @DisplayName("should return UnprocessableEntity if site is not formatted properly")
  void shouldReturnUnprocessableEntityIfSiteIsNotFormattedProperly() throws Exception {
    String data = """
        {
          "data": "50d00dbe0817df9d676a8a2.af3453c9"
        }
        """;

    mvc
        .perform(
            post(SITE_BASE_URL)
                .header(AUTH_HEADER, "Bearer " + accessToken)
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
              mapper.readValue(errors, new TypeReference<List<TestValidationError>>() {
              })
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
  @DisplayName("should update site")
  void shouldUpdateSite() throws Exception {
    String data = """
        {
          "data": "50d00dbe0817df9d676a8a2d.af3453c9"
        }
        """;

    mvc
        .perform(
            put(SITE_BASE_URL + "/498c0f91-955b-4c66-b6fb-7b1161f09561")
                .header(AUTH_HEADER, "Bearer " + accessToken)
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

    mvc
        .perform(
            put(SITE_BASE_URL + "/128c0f91-955b-4c66-b6fb-7b1161f09561")
                .header(AUTH_HEADER, "Bearer " + accessToken)
                .content(data)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNotFound())
        .andExpect(jsonPath(".status").value(HttpStatus.NOT_FOUND.getReasonPhrase()))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)))
        .andExpect(jsonPath("$.message").value("Site with given id does not exist"));
  }

  @Test
  @DisplayName("should return UnprocessableEntity if both PUT parameters are invalid")
  void shouldReturnUnprocessableEntityIfBothPutParametersAreInvalid() throws Exception {
    String data = """
        {
          "data": "50d00dbe0817df9d676a8ag.af3453c9"
        }
        """;

    mvc
        .perform(
            put(SITE_BASE_URL + "/sfaeara")
                .header(AUTH_HEADER, "Bearer " + accessToken)
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
              mapper.readValue(errors, new TypeReference<List<TestValidationError>>() {
              })
          );

          assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase(), ctx.read("$.status"));
          assertEquals("Validation Error", ctx.read("$.message"));
          assertTrue(Pattern.matches(TIMESTAMP_PATTERN, ctx.read("$.timestamp")));
          assertEquals("data", map.get("data").getField());
          assertEquals("50d00dbe0817df9d676a8ag.af3453c9", map.get("data").getRejectedValue());
          assertTrue(map.get("data").getValidationMessages().contains(props.getProperty("data.pattern.message")));
        });
  }

  @Test
  @DisplayName("should not update site if path variable is not valid UUID")
  void shouldNotUpdateSiteIfPathVariableIsNotValidUUID() throws Exception {
    String data = """
        {
          "data": "50d00dbe0817df9d676a8a2d.af3453c9"
        }
        """;

    mvc
        .perform(
            put(SITE_BASE_URL + "/dafiauhe")
                .header(AUTH_HEADER, "Bearer " + accessToken)
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
                .header(AUTH_HEADER, "Bearer " + accessToken)
                .content(data)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isMethodNotAllowed())
        .andExpect(jsonPath("$.status").value(HttpStatus.METHOD_NOT_ALLOWED.getReasonPhrase()))
        .andExpect(jsonPath("$.message").value("Method not allowed for given route"))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)));
  }

  @Test
  @DisplayName("should delete site")
  void shouldDeleteSite() throws Exception {
    mvc
        .perform(
            delete(SITE_BASE_URL + "/498c0f91-955b-4c66-b6fb-7b1161f09561")
                .header(AUTH_HEADER, "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNoContent())
        .andDo(res -> {
          List<SiteDTO> sites = siteService.getAllUserSites(UUID.fromString(ACCOUNT_PUBLIC_ID));
          assertEquals(2, sites.size());
        });
  }

  @Test
  @DisplayName("should return MethodNotAllowed if path variable is missing in DELETE request")
  void shouldReturnMethodNotAllowedIfPathVariableIsMissingInDeleteRequest() throws Exception {
    mvc
        .perform(
            delete(SITE_BASE_URL)
                .header(AUTH_HEADER, "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isMethodNotAllowed())
        .andExpect(jsonPath("$.status").value(HttpStatus.METHOD_NOT_ALLOWED.getReasonPhrase()))
        .andExpect(jsonPath("$.message").value("Method not allowed for given route"))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)));
  }

  @Test
  @DisplayName("should return BadRequest if path variable is not valid UUID in DELETE request")
  void shouldReturnBadRequestIfPathVariablwIsNotValidUuidInDeleteRequest() throws Exception {
    mvc
        .perform(
            delete(SITE_BASE_URL + "/ar4w")
                .header(AUTH_HEADER, "Bearer " + accessToken)
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
    mvc
        .perform(
            delete(SITE_BASE_URL + "/" + UUID.randomUUID())
                .header(AUTH_HEADER, "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.getReasonPhrase()))
        .andExpect(jsonPath("$.message").value("Site with given id does not exist"))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)));
  }

  @Test
  @DisplayName("should return all user sites")
  void shouldReturnAllUserSites() throws Exception {
    mvc
        .perform(
            get(SITE_BASE_URL)
                .header(AUTH_HEADER, "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andDo(res -> {
          String body = res.getResponse().getContentAsString();
          var entries = mapper.readValue(body, new TypeReference<List<TestSiteDTO>>() {});
          assertEquals(3, entries.size());
        });
  }

  @Test
  @DisplayName("should return an empty list if user has no sites")
  void shouldReturnEmptyListIfUserHasNoSites() throws Exception {
    String token = generateJwtToken(ISSUER, "983ce893-acc1-431c-b9e3-ffbe4394ef42", Instant.now().getEpochSecond(), AUDIENCE, Instant.now().getEpochSecond() + 1000, KEY );

    mvc
        .perform(
            get(SITE_BASE_URL)
                .header(AUTH_HEADER, "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andDo(res -> {
          String body = res.getResponse().getContentAsString();
          var entries = mapper.readValue(body, new TypeReference<List<TestSiteDTO>>() {});

          assertEquals(0, entries.size());
        });
  }

  @Test
  @DisplayName("should return site with given public id")
  void shouldReturnSiteWithGivenPublicId() throws Exception {
    mvc
        .perform(
            get(SITE_BASE_URL + "/498c0f91-955b-4c66-b6fb-7b1161f09561")
                .header(AUTH_HEADER, "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.publicId").value("498c0f91-955b-4c66-b6fb-7b1161f09561"))
        .andExpect(jsonPath("$.site").value("50d00dbe0817df9d676a8adc.af3453c95"))
        .andExpect(jsonPath("$.createdAt", matchesPattern(TIMESTAMP_PATTERN)))
        .andExpect(jsonPath("$.updatedAt", matchesPattern(TIMESTAMP_PATTERN)));
  }

  @Test
  @DisplayName("should return NotFound if site with given public id does not exist")
  void shouldReturnNotFoundIfSiteWithGivenPublicIdDoesNotExist() throws Exception {
    mvc
        .perform(
            get(SITE_BASE_URL + "/" + UUID.randomUUID())
                .header(AUTH_HEADER, "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.getReasonPhrase()))
        .andExpect(jsonPath("$.message").value("Site with given id does not exist"))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)));
  }

  @Test
  @DisplayName("should return BadRequest if path variable is not valid UUID")
  void shouldReturnBadRequestIfPathVariableIsNotValidUUID() throws Exception {
    mvc
        .perform(
            get(SITE_BASE_URL + "/dafafr")
                .header(AUTH_HEADER, "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)))
        .andExpect(jsonPath("$.message").value("Bad Path Variable"));
  }
}
