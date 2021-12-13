package org.dominik.pass.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import org.dominik.pass.configuration.ApiControllerMvcTestConfig;
import org.dominik.pass.data.dto.AccountDTO;
import org.dominik.pass.data.dto.AddressDTO;
import org.dominik.pass.data.enums.Role;
import org.dominik.pass.db.entities.Account;
import org.dominik.pass.db.entities.Address;
import org.dominik.pass.errors.exceptions.NotFoundException;
import org.dominik.pass.security.AccountDetails;
import org.dominik.pass.security.utils.SecurityUtils;
import org.dominik.pass.services.definitions.AddressService;
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
    value = AddressController.class,
    excludeAutoConfiguration = {UserDetailsServiceAutoConfiguration.class},
    properties = {
        "spring.main.banner-mode=off"
    }
)
@WithMockUser
@Import(ApiControllerMvcTestConfig.class)
@ActiveProfiles("test")
class AddressControllerMvcTest {
  private static final String ADDRESS_BASE_URL = "/addresses";
  private static final Long ACCOUNT_ID = 1L;
  private static final Long ADDRESS_ID = 1L;
  private static final UUID ACCOUNT_PUBLIC_ID = UUID.randomUUID();
  private static final UUID ADDRESS_PUBLIC_ID = UUID.randomUUID();
  private static final String EMAIL = "dominik.krenski@gmail.com";
  private static final String PASSWORD = "b468879149f241f69ce185ee2cc1764047ece00f7aad0128053a12aee5be320c";
  private static final String SALT = "711882a4dc3dcb437eb6151c09025594";
  private static final Role ROLE = Role.ROLE_USER;
  private static final String REMINDER = "dummy message";
  private static final Instant ACCOUNT_CREATED_AT = Instant.now().minusSeconds(5000);
  private static final Instant ACCOUNT_UPDATED_AT = Instant.now().minusSeconds(2000);
  private static final Instant ADDRESS_CREATED_AT = Instant.now().minusSeconds(50);
  private static final Instant ADDRESS_UPDATED_AT = Instant.now().minusSeconds(40);
  private static final short ACCOUNT_VERSION = 0;
  private static final short ADDRESS_VERSION = 0;
  private static final String ENTRY = "50d00dbe0817df9d676a8a2d.af3453c9";
  private static final String TIMESTAMP_PATTERN = "\\d{2}/\\d{2}/\\d{4}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z";

  private static Properties props;
  private static Account account;
  private static Address address;

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper mapper;
  @Autowired SecurityUtils securityUtils;
  @Autowired AddressService addressService;

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

    address = createAddressInstance(
        ADDRESS_ID,
        ADDRESS_PUBLIC_ID,
        ENTRY,
        account,
        ADDRESS_CREATED_AT,
        ADDRESS_UPDATED_AT,
        ADDRESS_VERSION
    );
  }

  @Test
  @DisplayName("should create address")
  void shouldCreateAddress() throws Exception {
    String data = """
        {
          "data": "50d00dbe0817df9d676a8a2d.af3453c9"
        }
        """;

    when(securityUtils.getPrincipal()).thenReturn(AccountDetails.fromDTO(AccountDTO.fromAccount(account)));
    when(addressService.save(anyString(), any(UUID.class))).thenReturn(AddressDTO.fromAddressLazy(address));

    mvc
        .perform(
            post(ADDRESS_BASE_URL)
                .content(data)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.publicId").value(address.getPublicId().toString()))
        .andExpect(jsonPath("$.address").value(address.getAddress()))
        .andExpect(jsonPath("$.createdAt").value(convertInstantIntoString(address.getCreatedAt())))
        .andExpect(jsonPath("$.updatedAt").value(convertInstantIntoString(address.getUpdatedAt())));
  }

  @Test
  @DisplayName("should return UnprocessableEntity if address data is blank")
  void shouldReturnUnprocessableEntityIfAddressDataIsBlank() throws Exception {
    String data = """
        {
          "data": "    "
        }
        """;

    List<String> messages = new LinkedList<>(
        List.of(
            props.getProperty("address.blank.message"),
            props.getProperty("address.pattern.message")
        )
    );

    mvc
        .perform(
            post(ADDRESS_BASE_URL)
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
          assertEquals("    ", map.get("data").getRejectedValue());
          assertTrue(map.get("data").getValidationMessages().containsAll(messages));
        });
  }

  @Test
  @DisplayName("should return UnprocessableEntity if address is not formatted properly")
  void shouldReturnUnprocessableEntityIfAddressIsNotFormattedProperly() throws Exception {
    String data = """
        {
          "data": "50d00dbe0817df9d676a8a2.af3453c9"
        }
        """;

    mvc
        .perform(
            post(ADDRESS_BASE_URL)
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
          assertTrue(map.get("data").getValidationMessages().contains(props.getProperty("address.pattern.message")));
        });
  }

  @Test
  @DisplayName("should return all user addresses")
  void shouldReturnAllUserAddresses() throws Exception {
    Address address_1 = createAddressInstance(
        2L,
        UUID.randomUUID(),
        "address_1",
        account,
        ADDRESS_CREATED_AT,
        ADDRESS_UPDATED_AT,
        ADDRESS_VERSION
    );

    Address address_2 = createAddressInstance(
        3L,
        UUID.randomUUID(),
        "address_2",
        account,
        ADDRESS_CREATED_AT,
        ADDRESS_UPDATED_AT,
        ADDRESS_VERSION
    );

    when(securityUtils.getPrincipal()).thenReturn(AccountDetails.fromDTO(AccountDTO.fromAccount(account)));
    when(addressService.getAllUserAddresses(any(UUID.class))).thenReturn(List.of(
        AddressDTO.fromAddressEager(address_1),
        AddressDTO.fromAddressEager(address_1),
        AddressDTO.fromAddressEager(address_2)
    ));

    mvc
        .perform(
            get(ADDRESS_BASE_URL)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andDo(res -> {
          String body = res.getResponse().getContentAsString();
          var entries = mapper.readValue(body, new TypeReference<List<TestAddressDTO>>(){});

          assertEquals(3, entries.size());
        });
  }

  @Test
  @DisplayName("should return an empty list if user has no addresses")
  void shouldReturnEmptyListIfUserHasNoAddresses() throws Exception {
    when(securityUtils.getPrincipal()).thenReturn(AccountDetails.fromDTO(AccountDTO.fromAccount(account)));
    when(addressService.getAllUserAddresses(any(UUID.class))).thenReturn(new LinkedList<>());

    mvc
        .perform(
            get(ADDRESS_BASE_URL)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andDo(res -> {
          String body = res.getResponse().getContentAsString();
          var entries = mapper.readValue(body, new TypeReference<List<TestAddressDTO>>(){});

          assertEquals(0, entries.size());
        });
  }

  @Test
  @DisplayName("should return address with given public id")
  void shouldReturnAddressWithGivenPublicId() throws Exception {
    when(securityUtils.getPrincipal()).thenReturn(AccountDetails.fromDTO(AccountDTO.fromAccount(account)));
    when(addressService.getAddress(any(UUID.class))).thenReturn(AddressDTO.fromAddressLazy(address));

    mvc
        .perform(
            get(ADDRESS_BASE_URL + "/" + UUID.randomUUID())
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.publicId").value(address.getPublicId().toString()))
        .andExpect(jsonPath("$.address").value(address.getAddress()))
        .andExpect(jsonPath("$.createdAt").value(convertInstantIntoString(address.getCreatedAt())))
        .andExpect(jsonPath("$.updatedAt").value(convertInstantIntoString(address.getUpdatedAt())));
  }

  @Test
  @DisplayName("should return NotFound if address with given public id does not exist")
  void shouldReturnNotFoundIfAddressWithPublicIdDoesNotExist() throws Exception {
    when(securityUtils.getPrincipal()).thenReturn(AccountDetails.fromDTO(AccountDTO.fromAccount(account)));
    when(addressService.getAddress(any(UUID.class))).thenThrow(new NotFoundException("Address does not exist"));

    mvc
        .perform(
            get(ADDRESS_BASE_URL + "/" + UUID.randomUUID())
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.getReasonPhrase()))
        .andExpect(jsonPath("$.message").value("Address does not exist"))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)));
  }

  @Test
  @DisplayName("should return BadRequest if path variable is not valid UUID")
  void shouldReturnBadRequestIfPathVariableIsNotValidUUID() throws Exception {
    mvc
        .perform(
            get(ADDRESS_BASE_URL + "/aklfjsoigkv")
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
        .andExpect(jsonPath("$.message").value("Bad Path Variable"))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)));
  }

  @Test
  @DisplayName("should update address")
  void shouldUpdateAddress() throws Exception {
    String data = """
        {
          "data": "50d00dbe0817df9d676a8a2d.af3453c9"
        }
        """;

    when(securityUtils.getPrincipal()).thenReturn(AccountDetails.fromDTO(AccountDTO.fromAccount(account)));
    doNothing().when(addressService).updateAddress(anyString(), any(UUID.class));

    mvc
        .perform(
            put(ADDRESS_BASE_URL + "/" + UUID.randomUUID())
                .content(data)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("should return NotFound if address could not be updated")
  void shouldReturnNotFoundIfAddressCouldNotBeUpdated() throws Exception {
    String data = """
        {
          "data": "50d00dbe0817df9d676a8a2d.af3453c9"
        }
        """;

    when(securityUtils.getPrincipal()).thenReturn(AccountDetails.fromDTO(AccountDTO.fromAccount(account)));
    doThrow(new NotFoundException("Address not found")).when(addressService).updateAddress(anyString(), any(UUID.class));

    mvc
        .perform(
            put(ADDRESS_BASE_URL + "/" + UUID.randomUUID())
                .content(data)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.getReasonPhrase()))
        .andExpect(jsonPath("$.message").value("Address not found"))
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
            put(ADDRESS_BASE_URL + "/dafadfa")
                .content(data)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isUnprocessableEntity());
  }

  @Test
  @DisplayName("should not update address if path variable is not valid UUID")
  void shouldNotUpdateAddressIfPathVariableIsNotValidUUID() throws Exception {
    String data = """
        {
          "data": "50d00dbe0817df9d676a8a2d.af3453c9"
        }
        """;

    mvc
        .perform(
            put(ADDRESS_BASE_URL + "/afdadfsf")
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
            put(ADDRESS_BASE_URL)
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
  @DisplayName("should delete address")
  void shouldDeleteAddress() throws Exception {
    when(securityUtils.getPrincipal()).thenReturn(AccountDetails.fromDTO(AccountDTO.fromAccount(account)));
    doNothing().when(addressService).deleteAddress(any(UUID.class));

    mvc
        .perform(
            delete(ADDRESS_BASE_URL + "/" + UUID.randomUUID())
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("should return MethodNotAllowed if path variable is missing")
  void shouldReturnError() throws Exception {
    mvc
        .perform(
            delete(ADDRESS_BASE_URL)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isMethodNotAllowed())
        .andExpect(jsonPath("$.status").value(HttpStatus.METHOD_NOT_ALLOWED.getReasonPhrase()))
        .andExpect(jsonPath("$.message").value("Method not allowed for given route"))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)));
  }

  @Test
  @DisplayName("should return BadRequest if path variable is not valid UUID in DELETE request")
  void shouldReturnBadRequestIfPathVariableIsInvalidInDeleteRequest() throws Exception {
    mvc
        .perform(
            delete(ADDRESS_BASE_URL + "/safasfasfd")
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
        .andExpect(jsonPath("$.message").value("Bad Path Variable"))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)));
  }

  @Test
  @DisplayName("should return NotFound if address could not be deleted")
  void shouldReturnNotFoundIfAddressCouldNotBeDeleted() throws Exception {
    when(securityUtils.getPrincipal()).thenReturn(AccountDetails.fromDTO(AccountDTO.fromAccount(account)));
    doThrow(new NotFoundException("Address not found")).when(addressService).deleteAddress(any(UUID.class));

    mvc
        .perform(
            delete(ADDRESS_BASE_URL + "/" + UUID.randomUUID())
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.getReasonPhrase()))
        .andExpect(jsonPath("$.message").value("Address not found"))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)));
  }
}
