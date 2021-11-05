package org.dominik.pass.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import org.dominik.pass.data.dto.AuthDTO;
import org.dominik.pass.services.definitions.AccountService;
import org.dominik.pass.utils.TestUtils;
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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.dominik.pass.utils.TestUtils.*;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
    properties = {
        "spring.profiles.active=integration"
    }
)
@AutoConfigureMockMvc
@Transactional
@Sql("classpath:/sql/02.auth-controller-test.sql")
@ActiveProfiles("integration")
public class AuthControllerRegistrationBootTestIT {
  private static final String URL = "/auth/signup";
  private static final String EMAIL = "dominik.krenski@gmail.com";
  private static final String PASSWORD = "b468879149f241f69ce185ee2cc1764047ece00f7aad0128053a12aee5be320c";
  private static final String SALT = "711882a4dc3dcb437eb6151c09025594";
  private static final String TIMESTAMP_REGEX = "\\d{2}/\\d{2}/\\d{4}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z";

  private static Properties props;

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper mapper;
  @Autowired AccountService accountService;

  @BeforeAll
  static void setUp() {
    props = readPropertiesFile("ValidationMessages.properties");
  }

  @Test
  @DisplayName("should return BadRequest if body is empty")
  void shouldRegisterNewAccount() throws Exception {
    mvc
        .perform(
            post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
        .andExpect(jsonPath("$.message").value("Message is not formatted properly"))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_REGEX)));
  }

  @Test
  @DisplayName("should return BadRequest if body has trailing comma")
  void shouldReturnBadRequestIfBodyHasTrailingComma() throws Exception {
    String body = """
        {
          "email": "dominik.krenski@gmail.com",
          "password": "password",
          "salt": "salt",
        }
        """;

    mvc
        .perform(
            post(URL)
                .content(body)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
        .andExpect(jsonPath("$.message").value("Message is not formatted properly"))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_REGEX)));
  }

  @Test
  @DisplayName("should return MethodNotAllowed if request method is get")
  void shouldReturnMethodNotAllowedIfRequestMethodIsGet() throws Exception {
    mvc
        .perform(
            get(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isMethodNotAllowed())
        .andExpect(jsonPath("$.status").value(HttpStatus.METHOD_NOT_ALLOWED.getReasonPhrase()))
        .andExpect(jsonPath("$.message").value("Method not allowed for given route"))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_REGEX)));
  }

  @Test
  @DisplayName("should return UnsupportedMediaType if body contains plain text")
  void shouldReturnUnsupportedMediaTypeIfBodyContainsPlainTest() throws Exception {
    String body = """
        "email": "dominik.krenski@gmail.com",
        "password": "password",
        "salt": "salt"
        """;

    mvc
        .perform(
            post(URL)
                .content(body)
                .contentType(MediaType.TEXT_PLAIN)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isUnsupportedMediaType())
        .andExpect(jsonPath("$.status").value(HttpStatus.UNSUPPORTED_MEDIA_TYPE.getReasonPhrase()))
        .andExpect(jsonPath("$.message").value("MediaType Not Supported"))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_REGEX)));
  }

  @Test
  @DisplayName("should return UnprocessableEntity if all fields are empty strings")
  void shouldReturnUnprocessableEntityIfAllFieldsAreEmptyStrings() throws Exception {
    // prepare email messages
    List<String> emailMessages = new LinkedList<>(
        List.of(
            props.getProperty("email.blank.message"),
            props.getProperty("email.format.message")
        )
    );

    // prepare password messages
    List<String> passwordMessages = new LinkedList<>(
        List.of(
            props.getProperty("password.length.message").replace("{max}", "64"),
            props.getProperty("password.hex.message"),
            props.getProperty("password.blank.message")
        )
    );

    // prepare salt messages
    List<String> saltMessages = new LinkedList<>(
        List.of(
            props.getProperty("salt.length.message").replace("{max}", "32"),
            props.getProperty("salt.hex.message"),
            props.getProperty("salt.blank.message")
        )
    );

    String content = """
        {
          "email": "",
          "password": "",
          "salt": ""
        }
        """;

    mvc
        .perform(
            post(URL)
                .content(content)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isUnprocessableEntity())
        .andDo(res -> {
          String body = res.getResponse().getContentAsString();

          ReadContext ctx = JsonPath.parse(body);
          String errors = getSubErrorsString(body);

          Map<String, TestUtils.TestValidationError> map = convertErrorListToMap(mapper.readValue(errors, new TypeReference<>(){}));

          assertEquals("email", map.get("email").getField());
          assertEquals("", map.get("email").getRejectedValue());
          assertTrue(map.get("email").getValidationMessages().containsAll(emailMessages));

          assertEquals("password", map.get("password").getField());
          assertEquals("", map.get("password").getRejectedValue());
          assertTrue(map.get("password").getValidationMessages().containsAll(passwordMessages));

          assertEquals("salt", map.get("salt").getField());
          assertEquals("", map.get("salt").getRejectedValue());
          assertTrue(map.get("salt").getValidationMessages().containsAll(saltMessages));
        });
  }

  @Test
  @DisplayName("should return ConflictException if account with given email already exists")
  void shouldReturnConflictIfAccountAlreadyExist() throws Exception {
    var registrationDTO = createRegistrationDtoInstance(EMAIL, PASSWORD, SALT, null);

    mvc
        .perform(
            post(URL)
                .content(mapper.writeValueAsString(registrationDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.status").value(HttpStatus.CONFLICT.getReasonPhrase()))
        .andExpect(jsonPath("$.message").value("Account with given email already exists"))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_REGEX)));
  }

  @Test
  @DisplayName("should register new account")
  void shouldRegisterAccount() throws Exception {
    var dto = createRegistrationDtoInstance("pass.dominik-krenski@ovh", PASSWORD, SALT, "dummy message");

    mvc
        .perform(
            post(URL)
                .content(mapper.writeValueAsString(dto))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andDo(print())
        .andExpect(status().isOk())
        .andDo(res -> {
          String body = res.getResponse().getContentAsString();

          String publicId = body.substring(13, body.length() - 2);

          var accountDTO = accountService.findByEmail("pass.dominik-krenski@ovh");

          assertEquals(accountDTO.getPublicId().toString(), publicId);
        });
  }
}
