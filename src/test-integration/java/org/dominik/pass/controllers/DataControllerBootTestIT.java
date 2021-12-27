package org.dominik.pass.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import org.dominik.pass.data.enums.DataType;
import org.junit.jupiter.api.BeforeAll;
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

import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;

import static org.dominik.pass.utils.TestUtils.*;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.*;
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
@Sql("classpath:sql/03.sample-data.sql")
@ActiveProfiles("integration")
class DataControllerBootTestIT {
  private static final String DATA_URL = "/data";
  private static final String DOMINIK_KRENSKI_ID = "cee0fa30-d170-4d9c-af8a-93ab159e9532";
  private static final String DORCIAD_ID = "e455b70f-50c5-4a96-9386-58f6ab9ba24b";
  private static final String DOMINIK_ID = "f01048b2-622a-49b6-963e-5e8edeec8026";
  private static final String AUTH_HEADER = "Authorization";
  private static final String ISSUER = "personal-pass.dev";
  private static final String AUDIENCE = "access";
  private static final String KEY = "gUkXn2r5u8x/A?D(G+KbPeShVmYq3s6v9y$B&E)H@McQfTjWnZr4u7w!z%C*F-Ja";
  private static final String TIMESTAMP_PATTERN = "\\d{2}/\\d{2}/\\d{4}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z";
  private static final String UUID_PATTERN = "^[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-4[0-9A-Fa-f]{3}-[89ABab][0-9A-Fa-f]{3}-[0-9A-Fa-f]{12}$";

  private static Properties props;
  private static String accessToken;

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper mapper;

  @BeforeAll
  static void setUp() {
    props = readPropertiesFile("ValidationMessages.properties");
  }

  @Test
  @DisplayName("should create address")
  void shouldCreateAddress() throws Exception {
    String input = """
        {
          "type": "ADDRESS",
          "entry": "50d00dbe0817df9d676a8a2d.af3453c8abcdef345670948432984975834791"
        }
        """;

    accessToken = generateJwtToken(ISSUER, DOMINIK_KRENSKI_ID, Instant.now().getEpochSecond(), AUDIENCE, Instant.now().plusSeconds(1000).getEpochSecond(), KEY);

    mvc
        .perform(
            post(DATA_URL)
                .header(AUTH_HEADER, "Bearer " + accessToken)
                .content(input)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.publicId", matchesPattern(UUID_PATTERN)))
        .andExpect(jsonPath("$.entry").value("50d00dbe0817df9d676a8a2d.af3453c8abcdef345670948432984975834791"))
        .andExpect(jsonPath("$.type").value(DataType.ADDRESS.toString()))
        .andExpect(jsonPath("$.createdAt", matchesPattern(TIMESTAMP_PATTERN)))
        .andExpect(jsonPath("$.updatedAt", matchesPattern(TIMESTAMP_PATTERN)));
  }

  @Test
  @DisplayName("should create password")
  void shouldCreatePassword() throws Exception {
    String input = """
        {
          "type": "PASSWORD",
          "entry": "50d00dbe0817df9d676a8a2d.af3453c8abcdef345670948432984975834791"
        }
        """;

    accessToken = generateJwtToken(ISSUER, DOMINIK_KRENSKI_ID, Instant.now().getEpochSecond(), AUDIENCE, Instant.now().plusSeconds(100).getEpochSecond(), KEY);

    mvc
        .perform(
            post(DATA_URL)
                .header(AUTH_HEADER, "Bearer " + accessToken)
                .content(input)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.publicId", matchesPattern(UUID_PATTERN)))
        .andExpect(jsonPath("$.entry").value("50d00dbe0817df9d676a8a2d.af3453c8abcdef345670948432984975834791"))
        .andExpect(jsonPath("$.type").value(DataType.PASSWORD.toString()))
        .andExpect(jsonPath("$.createdAt", matchesPattern(TIMESTAMP_PATTERN)))
        .andExpect(jsonPath("$.updatedAt", matchesPattern(TIMESTAMP_PATTERN)));
  }

  @Test
  @DisplayName("should create site")
  void shouldCreateSite() throws Exception {
    String input = """
        {
          "type": "SITE",
          "entry": "50d00dbe0817df9d676a8a2d.af3453c8abcdef345670948432984975834791"
        }
        """;

    accessToken = generateJwtToken(ISSUER, DOMINIK_KRENSKI_ID, Instant.now().getEpochSecond(), AUDIENCE, Instant.now().plusSeconds(100).getEpochSecond(), KEY);

    mvc
        .perform(
            post(DATA_URL)
                .header(AUTH_HEADER, "Bearer " + accessToken)
                .content(input)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.publicId", matchesPattern(UUID_PATTERN)))
        .andExpect(jsonPath("$.entry").value("50d00dbe0817df9d676a8a2d.af3453c8abcdef345670948432984975834791"))
        .andExpect(jsonPath("$.type").value(DataType.SITE.toString()))
        .andExpect(jsonPath("$.createdAt", matchesPattern(TIMESTAMP_PATTERN)))
        .andExpect(jsonPath("$.updatedAt", matchesPattern(TIMESTAMP_PATTERN)));
  }

  @Test
  @DisplayName("should create note")
  void shouldCreateNote() throws Exception {
    String input = """
        {
          "type": "NOTE",
          "entry": "50d00dbe0817df9d676a8a2d.af3453c8abcdef345670948432984975834791"
        }
        """;

    accessToken = generateJwtToken(ISSUER, DOMINIK_KRENSKI_ID, Instant.now().getEpochSecond(), AUDIENCE, Instant.now().plusSeconds(100).getEpochSecond(), KEY);

    mvc
        .perform(
            post(DATA_URL)
                .header(AUTH_HEADER, "Bearer " + accessToken)
                .content(input)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.publicId", matchesPattern(UUID_PATTERN)))
        .andExpect(jsonPath("$.entry").value("50d00dbe0817df9d676a8a2d.af3453c8abcdef345670948432984975834791"))
        .andExpect(jsonPath("$.type").value(DataType.NOTE.toString()))
        .andExpect(jsonPath("$.createdAt", matchesPattern(TIMESTAMP_PATTERN)))
        .andExpect(jsonPath("$.updatedAt", matchesPattern(TIMESTAMP_PATTERN)));
  }

  @Test
  @DisplayName("should return UnprocessableEntity if entry is blank")
  void shouldReturnUnprocessableEntityIfEntryIsBlank() throws Exception {
    String input = """
        {
          "type": "ADDRESS",
          "entry": "  "
        }
        """;

    List<String> messages = new LinkedList<>(
        List.of(
            props.getProperty("data.blank.message"),
            props.getProperty("data.pattern.message")
        )
    );

    accessToken = generateJwtToken(ISSUER, DOMINIK_KRENSKI_ID, Instant.now().getEpochSecond(), AUDIENCE, Instant.now().plusSeconds(1000).getEpochSecond(), KEY);

    mvc
        .perform(
            post(DATA_URL)
                .header(AUTH_HEADER, "Bearer " + accessToken)
                .content(input)
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
          assertEquals("entry", map.get("entry").getField());
          assertEquals("  ", map.get("entry").getRejectedValue());
          assertTrue(map.get("entry").getValidationMessages().containsAll(messages));
        });
  }

  @Test
  @DisplayName("should return UnprocessableEntity if entry is not formatted properly")
  void shouldReturnUnprocessableEntityIfEntryIsNotFormattedProperly() throws Exception {
    String input = """
        {
          "type": "PASSWORD",
          "entry": "50d00dbe0817df9d676a8a2.af3453c9"
        }
        """;

    accessToken = generateJwtToken(ISSUER, DOMINIK_KRENSKI_ID, Instant.now().getEpochSecond(), AUDIENCE, Instant.now().plusSeconds(1000).getEpochSecond(), KEY);

    mvc
        .perform(
            post(DATA_URL)
                .header(AUTH_HEADER, "Bearer " + accessToken)
                .content(input)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isUnprocessableEntity())
        .andDo(res -> {
          String body = res.getResponse().getContentAsString();
          ReadContext ctx = JsonPath.parse(body);
          String errors = getSubErrorsString(body);

          Map<String, TestValidationError> map = convertErrorListToMap(
              mapper.readValue(errors, new TypeReference<>() {
              })
          );

          assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase(), ctx.read("$.status"));
          assertEquals("Validation Error", ctx.read("$.message"));
          assertTrue(Pattern.matches(TIMESTAMP_PATTERN, ctx.read("$.timestamp")));
          assertEquals("entry", map.get("entry").getField());
          assertEquals("50d00dbe0817df9d676a8a2.af3453c9", map.get("entry").getRejectedValue());
          assertTrue(map.get("entry").getValidationMessages().contains(props.getProperty("data.pattern.message")));
        });
  }

  @Test
  @DisplayName("should return UnprocessableEntity if type is missing")
  void shouldReturnUnprocessableEntityIfTypeIsMissing() throws Exception {
    String input = """
        {
          "entry": "50d00dbe0817df9d676a8a2d.af3453c8abcdef345670948432984975834791"
        }
        """;

    accessToken = generateJwtToken(ISSUER, DOMINIK_KRENSKI_ID, Instant.now().getEpochSecond(), AUDIENCE, Instant.now().plusSeconds(1000).getEpochSecond(), KEY);

    mvc
        .perform(
            post(DATA_URL)
                .header(AUTH_HEADER, "Bearer " + accessToken)
                .content(input)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isUnprocessableEntity())
        .andDo(res -> {
          String body = res.getResponse().getContentAsString();
          ReadContext ctx = JsonPath.parse(body);
          String errors = getSubErrorsString(body);

          Map<String, TestValidationError> map = convertErrorListToMap(
              mapper.readValue(errors, new TypeReference<>() {
              })
          );

          assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase(), ctx.read("$.status"));
          assertEquals("Validation Error", ctx.read("$.message"));
          assertEquals("type", map.get("type").getField());
          assertNull(map.get("type").getRejectedValue());
          assertTrue(map.get("type").getValidationMessages().contains(props.getProperty("data.null.message")));
        });
  }

  @Test
  @DisplayName("should return BadRequest if type is wrong")
  void shouldReturnBadRequestIfTypeIsWrong() throws Exception {
    String input = """
        {
          "type": "WRONG",
          "entry": "50d00dbe0817df9d676a8a2d.af3453c8abcdef345670948432984975834791"
        }
        """;

    accessToken = generateJwtToken(ISSUER, DOMINIK_KRENSKI_ID, Instant.now().getEpochSecond(), AUDIENCE, Instant.now().plusSeconds(1000).getEpochSecond(), KEY);

    mvc
        .perform(
            post(DATA_URL)
                .header(AUTH_HEADER, "Bearer " + accessToken)
                .content(input)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value("Bad Request"))
        .andExpect(jsonPath("$.message").value("Message is not formatted properly"))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)));
  }

  @Test
  @DisplayName("should update data")
  void shouldUpdateData() throws Exception {
    String input = """
        {
          "entry": "50d00dbe0817df9d676a8a2d.af3453c8abcdef345670948432984975834791"
        }
        """;

    accessToken = generateJwtToken(ISSUER, DOMINIK_KRENSKI_ID, Instant.now().getEpochSecond(), AUDIENCE, Instant.now().plusSeconds(1000).getEpochSecond(), KEY);

    mvc
        .perform(
            put(DATA_URL + "/05618eec-dc25-4c24-b908-4fce6cb04ad4")
                .header(AUTH_HEADER, "Bearer " + accessToken)
                .content(input)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("should return NotFound if data could not be updated")
  void shouldReturnNotFoundIfDataCouldNotBeUpdated() throws Exception {
    String input = """
        {
          "entry": "50d00dbe0817df9d676a8a2d.af3453c8abcdef345670948432984975834791"
        }
        """;

    accessToken = generateJwtToken(ISSUER, DOMINIK_KRENSKI_ID, Instant.now().getEpochSecond(), AUDIENCE, Instant.now().plusSeconds(1000).getEpochSecond(), KEY);

    mvc
        .perform(
            put(DATA_URL + "/" + UUID.randomUUID())
                .header(AUTH_HEADER, "Bearer " + accessToken)
                .content(input)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.getReasonPhrase()))
        .andExpect(jsonPath("$.message").value("Data with given id does not exist"))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)));
  }

  @Test
  @DisplayName("should return BadRequest if both parameters are invalid")
  void shouldReturnBadRequestIfBothParametersAreInvalid() throws Exception {
    String input = """
        {
          "entry": "af3453c9"
        }
        """;

    accessToken = generateJwtToken(ISSUER, DOMINIK_KRENSKI_ID, Instant.now().getEpochSecond(), AUDIENCE, Instant.now().plusSeconds(1000).getEpochSecond(), KEY);

    mvc
        .perform(
            put(DATA_URL + "/aksfaf")
                .header(AUTH_HEADER, "Bearer " + accessToken)
                .content(input)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
        .andExpect(jsonPath("$.message").value("Bad Path Variable"))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)));
  }

  @Test
  @DisplayName("should return UnprocessableEntity if data is invalid")
  void shouldReturnUnprocessableEntityIfDataIsInvalid() throws Exception {
    String input = """
        {
          "entry": "sakdf748ryf"
        }
        """;

    accessToken = generateJwtToken(ISSUER, DOMINIK_KRENSKI_ID, Instant.now().getEpochSecond(), AUDIENCE, Instant.now().plusSeconds(1000).getEpochSecond(), KEY);

    mvc
        .perform(
            put(DATA_URL + "/" + UUID.randomUUID())
                .header(AUTH_HEADER, "Bearer " + accessToken)
                .content(input)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isUnprocessableEntity());
  }

  @Test
  @DisplayName("should not update data if path variable is not valid in PUT request")
  void shouldNotUpdateDataIfPathVariableIsNotValidInPutRequest() throws Exception {

    String input = """
        {
          "entry": "50d00dbe0817df9d676a8a2d.af3453c9"
        }
        """;

    accessToken = generateJwtToken(ISSUER, DOMINIK_KRENSKI_ID, Instant.now().getEpochSecond(), AUDIENCE, Instant.now().plusSeconds(1000).getEpochSecond(), KEY);

    mvc
        .perform(
            put(DATA_URL + "/asffsg")
                .header(AUTH_HEADER, "Bearer " + accessToken)
                .content(input)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
        .andExpect(jsonPath("$.message").value("Bad Path Variable"))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)));
  }

  @Test
  @DisplayName("should delete data")
  void shouldDeleteData() throws Exception {
    accessToken = generateJwtToken(ISSUER, DOMINIK_KRENSKI_ID, Instant.now().getEpochSecond(), AUDIENCE, Instant.now().plusSeconds(1000).getEpochSecond(), KEY);

    mvc
        .perform(
            delete(DATA_URL + "/3299f2fe-f930-44b6-8b10-c23c2efe5d1f")
                .header(AUTH_HEADER, "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("should return BadRequest if path variable is not valid in DELETE request")
  void shouldReturnBadRequestIfPathVariableIsNotValidInDeleteRequest() throws Exception {
    accessToken = generateJwtToken(ISSUER, DOMINIK_KRENSKI_ID, Instant.now().getEpochSecond(), AUDIENCE, Instant.now().plusSeconds(1000).getEpochSecond(), KEY);

    mvc
        .perform(
            delete(DATA_URL + "/adfafas")
                .header(AUTH_HEADER, "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
        .andExpect(jsonPath("$.message").value("Bad Path Variable"))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)));
  }

  @Test
  @DisplayName("should return NotFound if data could not be deleted")
  void shouldReturnNotFoundIfDataCouldNotBeDeleted() throws Exception {
    accessToken = generateJwtToken(ISSUER, DOMINIK_KRENSKI_ID, Instant.now().getEpochSecond(), AUDIENCE, Instant.now().plusSeconds(1000).getEpochSecond(), KEY);

    mvc
        .perform(
            delete(DATA_URL + "/" + UUID.randomUUID())
                .header(AUTH_HEADER, "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.getReasonPhrase()))
        .andExpect(jsonPath("$.message").value("Data with given id does not exist"))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)));
  }

  @Test
  @DisplayName("should delete all user data")
  void shouldDeleteAllUserData() throws Exception {
    accessToken = generateJwtToken(ISSUER, DOMINIK_KRENSKI_ID, Instant.now().getEpochSecond(), AUDIENCE, Instant.now().plusSeconds(1000).getEpochSecond(), KEY);

    mvc
        .perform(
            delete(DATA_URL)
                .header(AUTH_HEADER, "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("should return data with given public id")
  void shouldReturnDataWithGivenPublicId() throws Exception {
    accessToken = generateJwtToken(ISSUER, DOMINIK_KRENSKI_ID, Instant.now().getEpochSecond(), AUDIENCE, Instant.now().plusSeconds(1000).getEpochSecond(), KEY);

    mvc
        .perform(
            get(DATA_URL + "/67f9c86f-36eb-4fab-94ac-f68d113ad9d7")
                .header(AUTH_HEADER, "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.publicId").value("67f9c86f-36eb-4fab-94ac-f68d113ad9d7"))
        .andExpect(jsonPath("$.entry").value("entry_7"))
        .andExpect(jsonPath("$.type").value(DataType.NOTE.toString()))
        .andExpect(jsonPath("$.createdAt", matchesPattern(TIMESTAMP_PATTERN)))
        .andExpect(jsonPath("$.updatedAt", matchesPattern(TIMESTAMP_PATTERN)));
  }

  @Test
  @DisplayName("should return NotFound if data with given public id does not exist")
  void shouldReturnNotFoundIfDataWithGivenPublicIdDoesNotExist() throws Exception {
    accessToken = generateJwtToken(ISSUER, DOMINIK_KRENSKI_ID, Instant.now().getEpochSecond(), AUDIENCE, Instant.now().plusSeconds(1000).getEpochSecond(), KEY);

    mvc
        .perform(
            get(DATA_URL + "/" + UUID.randomUUID())
                .header(AUTH_HEADER, "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.getReasonPhrase()))
        .andExpect(jsonPath("$.message").value("Data with given id does not exist"))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)));
  }

  @Test
  @DisplayName("should return BadRequest if path variable is incorrect")
  void shouldReturnBadRequestIfPathVariableIsIncorrent() throws Exception {
    accessToken = generateJwtToken(ISSUER, DOMINIK_KRENSKI_ID, Instant.now().getEpochSecond(), AUDIENCE, Instant.now().plusSeconds(1000).getEpochSecond(), KEY);

    mvc
        .perform(
            get(DATA_URL + "/afiouaf")
                .header(AUTH_HEADER, "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
        .andExpect(jsonPath("$.message").value("Bad Path Variable"))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)));
  }

  @Test
  @DisplayName("should return all dominik.krenski addresses")
  void shouldReturnAllDominikKrenskiAddresses() throws Exception {
    accessToken = generateJwtToken(ISSUER, DOMINIK_KRENSKI_ID, Instant.now().getEpochSecond(), AUDIENCE, Instant.now().plusSeconds(1000).getEpochSecond(), KEY);

    mvc
        .perform(
            get(DATA_URL + "?type=" + DataType.ADDRESS)
                .header(AUTH_HEADER, "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andDo(
            res -> {
              String body = res.getResponse().getContentAsString();
              var addresses = mapper.readValue(body, new TypeReference<List<TestDataDTO>>(){});

              assertEquals(2, addresses.size());
            }
        );
  }

  @Test
  @DisplayName("should return all dorciad password")
  void shouldReturnAllDorciadPasswords() throws Exception {
    accessToken = generateJwtToken(ISSUER, DORCIAD_ID, Instant.now().getEpochSecond(), AUDIENCE, Instant.now().plusSeconds(1000).getEpochSecond(), KEY);

    mvc
        .perform(
            get(DATA_URL + "?type=" + DataType.PASSWORD)
                .header(AUTH_HEADER, "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andDo(res -> {
          String body = res.getResponse().getContentAsString();
          var passwords = mapper.readValue(body, new TypeReference<List<TestDataDTO>>(){});

          assertEquals(1, passwords.size());
        });
  }

  @Test
  @DisplayName("should return an empty list if user has no data with given type")
  void shouldReturnEmptyListIfUserHasNoDataWithGivenType() throws Exception {
    accessToken = generateJwtToken(ISSUER, DOMINIK_ID, Instant.now().getEpochSecond(), AUDIENCE, Instant.now().plusSeconds(1000).getEpochSecond(), KEY);

    mvc
        .perform(
            get(DATA_URL + "?type=" + DataType.NOTE)
                .header(AUTH_HEADER, "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andDo(res -> {
          String body = res.getResponse().getContentAsString();
          var notes = mapper.readValue(body, new TypeReference<List<TestDataDTO>>(){});

          assertEquals(0, notes.size());
        });
  }

  @Test
  @DisplayName("should return BadRequest if type is invalid")
  void shouldReturnBadRequestIfTypeIsInvalid() throws Exception {
    accessToken = generateJwtToken(ISSUER, DOMINIK_ID, Instant.now().getEpochSecond(), AUDIENCE, Instant.now().plusSeconds(1000).getEpochSecond(), KEY);

    mvc
        .perform(
            get(DATA_URL + "?type=BAD")
                .header(AUTH_HEADER, "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
        .andExpect(jsonPath("$.message").value("Bad Path Variable"))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)));
  }

  @Test
  @DisplayName("should return all dominik.krenski data")
  void shouldReturnAllDominikKrenskiData() throws Exception {
    accessToken = generateJwtToken(ISSUER, DOMINIK_KRENSKI_ID, Instant.now().getEpochSecond(), AUDIENCE, Instant.now().plusSeconds(1000).getEpochSecond(), KEY);

    mvc
        .perform(
            get(DATA_URL + "/all")
                .header(AUTH_HEADER, "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andDo(res -> {
          String body = res.getResponse().getContentAsString();
          var arr = mapper.readValue(body, new TypeReference<List<TestDataDTO>>(){});

          assertEquals(8, arr.size());
        });
  }

  @Test
  @DisplayName("should return all dorciad data")
  void shouldReturnAllDorciadData() throws Exception {
    accessToken = generateJwtToken(ISSUER, DORCIAD_ID, Instant.now().getEpochSecond(), AUDIENCE, Instant.now().plusSeconds(1000).getEpochSecond(), KEY);

    mvc
        .perform(
            get(DATA_URL + "/all")
                .header(AUTH_HEADER, "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andDo(res -> {
          String body = res.getResponse().getContentAsString();
          var arr = mapper.readValue(body, new TypeReference<List<TestDataDTO>>(){});

          assertEquals(7, arr.size());
        });
  }

  @Test
  @DisplayName("should return an empty list if user has no data")
  void shouldReturnEmptyListIfUserHasNoData() throws Exception {
    accessToken = generateJwtToken(ISSUER, DOMINIK_ID, Instant.now().getEpochSecond(), AUDIENCE, Instant.now().plusSeconds(1000).getEpochSecond(), KEY);

    mvc
        .perform(
            get(DATA_URL + "/all")
                .header(AUTH_HEADER, "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andDo(res -> {
          String body = res.getResponse().getContentAsString();
          var arr = mapper.readValue(body, new TypeReference<List<TestDataDTO>>(){});

          assertEquals(0, arr.size());
        });
  }
}
