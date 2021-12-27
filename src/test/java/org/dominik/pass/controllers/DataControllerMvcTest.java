package org.dominik.pass.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import org.dominik.pass.configuration.ApiControllerMvcTestConfig;
import org.dominik.pass.data.dto.AccountDTO;
import org.dominik.pass.data.dto.DataDTO;
import org.dominik.pass.data.enums.DataType;
import org.dominik.pass.db.entities.Account;
import org.dominik.pass.db.entities.Data;
import org.dominik.pass.errors.exceptions.NotFoundException;
import org.dominik.pass.security.AccountDetails;
import org.dominik.pass.security.utils.SecurityUtils;
import org.dominik.pass.services.definitions.DataService;
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
import java.util.*;
import java.util.regex.Pattern;

import static org.dominik.pass.utils.TestUtils.*;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    value = DataController.class,
    excludeAutoConfiguration = {UserDetailsServiceAutoConfiguration.class},
    properties = {
        "spring.main.banner-mode=off"
    }
)
@WithMockUser
@Import(ApiControllerMvcTestConfig.class)
@ActiveProfiles("test")
class DataControllerMvcTest {
  private static final String DATA_URL = "/data";
  private static final String TIMESTAMP_PATTERN = "\\d{2}/\\d{2}/\\d{4}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z";
  private static Properties props;
  private static List<Account> accounts;
  private static List<Data> data;

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper mapper;
  @Autowired SecurityUtils securityUtils;
  @Autowired DataService dataService;

  @BeforeAll
  static void setUp() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    props = readPropertiesFile("ValidationMessages.properties");
    accounts = prepareAccountList();
    data = prepareDataList(accounts);
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

    when(securityUtils.getPrincipal()).thenReturn(AccountDetails.fromDTO(AccountDTO.fromAccount(accounts.get(0))));
    when(dataService.save(anyString(), eq(DataType.ADDRESS), eq(accounts.get(0).getPublicId()))).thenReturn(DataDTO.fromData(data.get(0)));

    mvc
        .perform(
            post(DATA_URL)
                .content(input)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.publicId").value(data.get(0).getPublicId().toString()))
        .andExpect(jsonPath("$.entry").value(data.get(0).getEntry()))
        .andExpect(jsonPath("$.type").value(data.get(0).getType().toString()))
        .andExpect(jsonPath("$.createdAt").value(convertInstantIntoString(data.get(0).getCreatedAt())))
        .andExpect(jsonPath("$.updatedAt").value(convertInstantIntoString(data.get(0).getUpdatedAt())));
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

    when(securityUtils.getPrincipal()).thenReturn(AccountDetails.fromDTO(AccountDTO.fromAccount(accounts.get(0))));
    when(dataService.save(anyString(), eq(DataType.PASSWORD), eq(accounts.get(0).getPublicId()))).thenReturn(DataDTO.fromData(data.get(2)));

    mvc
        .perform(
            post(DATA_URL)
                .content(input)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.publicId").value(data.get(2).getPublicId().toString()))
        .andExpect(jsonPath("$.entry").value(data.get(2).getEntry()))
        .andExpect(jsonPath("$.type").value(data.get(2).getType().toString()))
        .andExpect(jsonPath("$.createdAt").value(convertInstantIntoString(data.get(2).getCreatedAt())))
        .andExpect(jsonPath("$.updatedAt").value(convertInstantIntoString(data.get(2).getUpdatedAt())));
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

    when(securityUtils.getPrincipal()).thenReturn(AccountDetails.fromDTO(AccountDTO.fromAccount(accounts.get(0))));
    when(dataService.save(anyString(), eq(DataType.SITE), eq(accounts.get(0).getPublicId()))).thenReturn(DataDTO.fromData(data.get(5)));

    mvc
        .perform(
            post(DATA_URL)
                .content(input)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.publicId").value(data.get(5).getPublicId().toString()))
        .andExpect(jsonPath("$.entry").value(data.get(5).getEntry()))
        .andExpect(jsonPath("$.type").value(data.get(5).getType().toString()))
        .andExpect(jsonPath("$.createdAt").value(convertInstantIntoString(data.get(5).getCreatedAt())))
        .andExpect(jsonPath("$.updatedAt").value(convertInstantIntoString(data.get(5).getUpdatedAt())));
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

    when(securityUtils.getPrincipal()).thenReturn(AccountDetails.fromDTO(AccountDTO.fromAccount(accounts.get(0))));
    when(dataService.save(anyString(), eq(DataType.NOTE), eq(accounts.get(0).getPublicId()))).thenReturn(DataDTO.fromData(data.get(7)));

    mvc
        .perform(
            post(DATA_URL)
                .content(input)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.publicId").value(data.get(7).getPublicId().toString()))
        .andExpect(jsonPath("$.entry").value(data.get(7).getEntry()))
        .andExpect(jsonPath("$.type").value(data.get(7).getType().toString()))
        .andExpect(jsonPath("$.createdAt").value(convertInstantIntoString(data.get(7).getCreatedAt())))
        .andExpect(jsonPath("$.updatedAt").value(convertInstantIntoString(data.get(7).getUpdatedAt())));
  }

  @Test
  @DisplayName("should return UnprocessableEntity if entry field is blank")
  void shouldReturnUnprocessableEntityIfEntryFieldIsBlank() throws Exception {
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

    mvc
        .perform(
            post(DATA_URL)
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
              mapper.readValue(errors, new TypeReference<List<TestValidationError>>() {})
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
  @DisplayName("should return UnprocessableEntity if entry field is not formatted properly")
  void shouldReturnUnprocessableEntityIfEntryFieldIsNotFormattedProperly() throws Exception {
    String input = """
        {
          "type": "PASSWORD",
          "entry": "50d00dbe0817df9d676a8a2.af3453c9"
        }
        """;

    mvc
        .perform(
            post(DATA_URL)
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
              mapper.readValue(errors, new TypeReference<>(){})
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

    mvc
        .perform(
            post(DATA_URL)
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
              mapper.readValue(errors, new TypeReference<>(){})
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

    mvc
        .perform(
            post(DATA_URL)
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

    when(securityUtils.getPrincipal()).thenReturn(AccountDetails.fromDTO(AccountDTO.fromAccount(accounts.get(0))));
    doNothing().when(dataService).updateData(anyString(), eq(accounts.get(0).getPublicId()));

    mvc
        .perform(
            put(DATA_URL + "/" + data.get(0).getPublicId())
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

    when(securityUtils.getPrincipal()).thenReturn(AccountDetails.fromDTO(AccountDTO.fromAccount(accounts.get(0))));
    doThrow(new NotFoundException("Data not found")).when(dataService).updateData(anyString(), any(UUID.class));

    mvc
        .perform(
            put(DATA_URL + "/" + UUID.randomUUID())
                .content(input)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.getReasonPhrase()))
        .andExpect(jsonPath("$.message").value("Data not found"))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)));
  }

  @Test
  @DisplayName("should return BadRequest if both parameters are invalid")
  void shouldReturnUnprocessableEntityIfBotParametersAreInvalid() throws Exception {
    String input = """
        {
          "entry": "af3453c9"
        }
        """;

    mvc
        .perform(
            put(DATA_URL + "/ahfurhi")
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

    mvc
        .perform(
            put(DATA_URL + "/" + UUID.randomUUID())
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

    mvc
        .perform(
            put(DATA_URL + "/esjaoifoe")
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
    when(securityUtils.getPrincipal()).thenReturn(AccountDetails.fromDTO(AccountDTO.fromAccount(accounts.get(0))));
    doNothing().when(dataService).deleteData(any(UUID.class));

    mvc
        .perform(
            delete(DATA_URL + "/" + UUID.randomUUID())
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("should return BadRequest if path variable is not valid in DELETE request")
  void shouldReturnBadRequestIfPathVariableIsNotValidInDeleteRequest() throws Exception {
    mvc
        .perform(
            delete(DATA_URL + "/dafadf")
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
    when(securityUtils.getPrincipal()).thenReturn(AccountDetails.fromDTO(AccountDTO.fromAccount(accounts.get(1))));
    doThrow(new NotFoundException(("Data not found"))).when(dataService).deleteData(any(UUID.class));

    mvc
        .perform(
            delete(DATA_URL + "/" + UUID.randomUUID())
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.getReasonPhrase()))
        .andExpect(jsonPath("$.message").value("Data not found"))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)));

  }

  @Test
  @DisplayName("should delete all user data")
  void shouldDeleteAllUserData() throws Exception {
    when(securityUtils.getPrincipal()).thenReturn(AccountDetails.fromDTO(AccountDTO.fromAccount(accounts.get(0))));
    doNothing().when(dataService).deleteAllUserData(any(UUID.class));

    mvc
        .perform(
            delete(DATA_URL)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("should return data with given public id")
  void shouldReturnDataWithGivenPublicId() throws Exception {
    when(securityUtils.getPrincipal()).thenReturn(AccountDetails.fromDTO(AccountDTO.fromAccount(accounts.get(0))));
    when(dataService.findData(any(UUID.class))).thenReturn(DataDTO.fromData(data.get(0)));

    mvc
        .perform(
            get(DATA_URL + "/" + UUID.randomUUID())
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.publicId").value(data.get(0).getPublicId().toString()))
        .andExpect(jsonPath("$.entry").value(data.get(0).getEntry()))
        .andExpect(jsonPath("$.type").value(data.get(0).getType().toString()))
        .andExpect(jsonPath("$.createdAt").value(convertInstantIntoString(data.get(0).getCreatedAt())))
        .andExpect(jsonPath("$.updatedAt").value(convertInstantIntoString(data.get(0).getUpdatedAt())));
  }

  @Test
  @DisplayName("should return NotFound if data with given public id does not exist")
  void shouldReturnNotFoundIfDataWithGivenPublicIdDoesNotExist() throws Exception {
    when(securityUtils.getPrincipal()).thenReturn(AccountDetails.fromDTO(AccountDTO.fromAccount(accounts.get(0))));
    when(dataService.findData(any(UUID.class))).thenThrow(new NotFoundException("Data not found"));

    mvc
        .perform(
            get(DATA_URL + "/" + UUID.randomUUID())
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.getReasonPhrase()))
        .andExpect(jsonPath("$.message").value("Data not found"))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)));
  }

  @Test
  @DisplayName("should return BadRequest if path variable is incorrect in GET by id")
  void shouldReturnBadRequestIfPathVariableIsIncorrectInGetById() throws Exception {
    mvc
        .perform(
            get(DATA_URL + "/adfasf")
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
    when(securityUtils.getPrincipal()).thenReturn(AccountDetails.fromDTO(AccountDTO.fromAccount(accounts.get(0))));
    when(dataService.findAllUserDataByType(DataType.ADDRESS, accounts.get(0).getPublicId()))
        .thenAnswer(i -> data
            .stream()
            .filter(d -> d.getType() == DataType.ADDRESS && d.getAccount().getPublicId() == accounts.get(0).getPublicId())
            .map(DataDTO::fromData)
            .toList());

    mvc
        .perform(
            get(DATA_URL + "?type=" + DataType.ADDRESS)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andDo(res -> {
          String body = res.getResponse().getContentAsString();
          var addresses = mapper.readValue(body, new TypeReference<List<TestDataDTO>>(){});

          assertEquals(2, addresses.size());
        });
  }

  @Test
  @DisplayName("should return all dorciad passwords")
  void shouldReturnAllDorciadPasswords() throws Exception {
    when(securityUtils.getPrincipal()).thenReturn(AccountDetails.fromDTO(AccountDTO.fromAccount(accounts.get(1))));
    when(dataService.findAllUserDataByType(DataType.PASSWORD, accounts.get(1).getPublicId()))
        .thenAnswer(i -> data
            .stream()
            .filter(d -> d.getType() == DataType.PASSWORD && d.getAccount().getPublicId() == accounts.get(1).getPublicId())
            .map(DataDTO::fromData)
            .toList()
        );

    mvc
        .perform(
            get(DATA_URL + "?type=" + DataType.PASSWORD)
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
  void shouldReturnAnEmptyListIfUserHasNoDataWithGivenType() throws Exception {
    when(securityUtils.getPrincipal()).thenReturn(AccountDetails.fromDTO(AccountDTO.fromAccount(accounts.get(2))));
    when(dataService.findAllUserDataByType(DataType.NOTE, accounts.get(2).getPublicId()))
        .thenAnswer(i -> data
                .stream()
                .filter(d -> d.getType() == DataType.NOTE && d.getAccount().getPublicId() == accounts.get(2).getPublicId())
                .map(DataDTO::fromData)
                .toList()
            );

    mvc
        .perform(
            get(DATA_URL + "?type=" + DataType.NOTE)
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
    mvc
        .perform(
            get(DATA_URL + "?type=BAD")
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
    when(securityUtils.getPrincipal()).thenReturn(AccountDetails.fromDTO(AccountDTO.fromAccount(accounts.get(0))));
    when(dataService.findAllUserData(accounts.get(0).getPublicId()))
        .thenAnswer(i -> data
            .stream()
            .filter(d -> d.getAccount().getPublicId() == accounts.get(0).getPublicId())
            .map(DataDTO::fromData)
            .toList()
        );

    mvc
        .perform(
            get(DATA_URL + "/all")
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
    when(securityUtils.getPrincipal()).thenReturn(AccountDetails.fromDTO(AccountDTO.fromAccount(accounts.get(1))));
    when(dataService.findAllUserData(accounts.get(1).getPublicId()))
        .thenAnswer(i -> data
            .stream()
            .filter(d -> d.getAccount().getPublicId() == accounts.get(1).getPublicId())
            .map(DataDTO::fromData)
            .toList()
        );

    mvc
        .perform(
            get(DATA_URL + "/all")
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andDo(res -> {
          String body = res.getResponse().getContentAsString();
          var arr = mapper.readValue(body, new TypeReference<List<TestDataDTO>>() {});

          assertEquals(7, arr.size());
        });
  }

  @Test
  @DisplayName("should return an empty list if user has no data")
  void shouldReturnEmptyListIfUserHasNoData() throws Exception {
    when(securityUtils.getPrincipal()).thenReturn(AccountDetails.fromDTO(AccountDTO.fromAccount(accounts.get(2))));
    when(dataService.findAllUserData(accounts.get(2).getPublicId()))
        .thenAnswer(i -> data
            .stream()
            .filter(d -> d.getAccount().getPublicId() == accounts.get(2).getPublicId())
            .map(DataDTO::fromData)
            .toList()
        );

    mvc
        .perform(
            get(DATA_URL + "/all")
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andDo(res -> {
          String body = res.getResponse().getContentAsString();
          var arr = mapper.readValue(body, new TypeReference<List<TestDataDTO>>(){});

          assertEquals(0, arr.size());
        });
  }
}
