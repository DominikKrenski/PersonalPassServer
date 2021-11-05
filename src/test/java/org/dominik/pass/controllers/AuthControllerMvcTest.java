package org.dominik.pass.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.dominik.pass.configuration.AuthControllerMvcTestConfig;
import org.dominik.pass.data.dto.AccountDTO;
import org.dominik.pass.data.dto.RegistrationDTO;
import org.dominik.pass.data.enums.Role;
import org.dominik.pass.db.entities.Account;
import org.dominik.pass.errors.exceptions.ConflictException;
import org.dominik.pass.errors.exceptions.NotFoundException;
import org.dominik.pass.services.definitions.AccountService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;

import static org.dominik.pass.utils.TestUtils.*;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    value = AuthController.class,
    excludeAutoConfiguration = {UserDetailsServiceAutoConfiguration.class},
    properties = {
        "spring.main.banner-mode=off"
    }
)
@Import(AuthControllerMvcTestConfig.class)
@ActiveProfiles("test")
public class AuthControllerMvcTest {
  private static final String EMAIL ="dominik.krenski@gmail.com";
  private static final String PASSWORD = "b468879149f241f69ce185ee2cc1764047ece00f7aad0128053a12aee5be320c";
  private static final String SALT = "711882a4dc3dcb437eb6151c09025594";
  private static final String REMINDER = "dummy reminder";
  private static final Long ID = 1L;
  private static final UUID PUBLIC_ID = UUID.randomUUID();
  private static final Instant CREATED_AT = Instant.now();
  private static final Instant UPDATED_AT = CREATED_AT.plusSeconds(1000);
  private static final short VERSION = 1;
  private static final String URL = "/auth/signup";
  private static final String SALT_URL = "/auth/salt";
  private static final String TIMESTAMP_PATTERN="\\d{2}/\\d{2}/\\d{4}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z";

  private static Properties props;

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper mapper;
  @Autowired AccountService accountService;

  @BeforeAll
  static void setUp() {
    props = readPropertiesFile("ValidationMessages.properties");
  }

  @Test
  @DisplayName("should register account")
  void shouldRegisterAccount() throws Exception {
    RegistrationDTO registrationDTO = createRegistrationDtoInstance(
        EMAIL,
        PASSWORD,
        SALT,
        REMINDER
    );

    Account account = createAccountInstance(
        ID,
        PUBLIC_ID,
        EMAIL,
        PASSWORD,
        SALT,
        REMINDER,
        Role.ROLE_USER,
        true,
        true,
        false,
        false,
        CREATED_AT,
        UPDATED_AT,
        VERSION
    );

    AccountDTO accountDTO = AccountDTO.fromAccount(account);

    when(accountService.register(any(RegistrationDTO.class))).thenReturn(accountDTO);

    mvc
        .perform(
            post(URL)
                .content(mapper.writeValueAsString(registrationDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.publicId").value(PUBLIC_ID.toString()));
  }

  @Test
  @DisplayName("should return validation error")
  void shouldReturnErrorResponseIfValidationFailed() throws Exception {
    RegistrationDTO registrationDTO = createRegistrationDtoInstance("dominik.yahoo", "a8r", "qwerty", null);

    // prepare individual password messages
    String passwordLengthMessage = props.getProperty("password.length.message").replace("{max}", "64");

    // prepare individual salt messages
    String saltLengthMessage = props.getProperty("salt.length.message").replace("{max}", "32");

    List<String> emailMessages = new LinkedList<>(List.of(props.getProperty("email.format.message")));

    List<String> passwordMessages = new LinkedList<>(List.of(
        props.getProperty("password.hex.message"),
        passwordLengthMessage
    ));

    List<String> saltMessages = new LinkedList<>(List.of(
        props.getProperty("salt.hex.message"),
        saltLengthMessage
    ));

    mvc
        .perform(
            post(URL)
                .content(mapper.writeValueAsString(registrationDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isUnprocessableEntity())
        .andDo(res -> {
          String body = res.getResponse().getContentAsString();
          ReadContext ctx = JsonPath.parse(body);
          String errors = getSubErrorsString(body);

          Map<String, TestValidationError> map = convertErrorListToMap(mapper.readValue(errors, new TypeReference<>() {}));

          assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase(), ctx.read("$.status"));
          assertTrue(Pattern.matches(TIMESTAMP_PATTERN, ctx.read("$.timestamp")));
          assertEquals("Validation Error", ctx.read("$.message"));

          assertEquals("email", map.get("email").getField());
          assertEquals("dominik.yahoo", map.get("email").getRejectedValue());
          assertTrue(map.get("email").getValidationMessages().containsAll(emailMessages));

          assertEquals("password", map.get("password").getField());
          assertEquals("a8r", map.get("password").getRejectedValue());
          assertTrue(map.get("password").getValidationMessages().containsAll(passwordMessages));

          assertEquals("salt", map.get("salt").getField());
          assertEquals("qwerty", map.get("salt").getRejectedValue());
          assertTrue(map.get("salt").getValidationMessages().containsAll(saltMessages));
        });
  }

  @Test
  @DisplayName("should return conflict response")
  void shouldReturnConflictResponse() throws Exception {
    RegistrationDTO registrationDTO = createRegistrationDtoInstance(EMAIL, PASSWORD, SALT, REMINDER);

    when(accountService.register(any(RegistrationDTO.class))).thenThrow(new ConflictException("Conflict with other record"));

    mvc
        .perform(
            post(URL)
                .content(mapper.writeValueAsString(registrationDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.status").value(HttpStatus.CONFLICT.getReasonPhrase()))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)))
        .andExpect(jsonPath("$.message").value("Conflict with other record"));
  }

  @Test
  @DisplayName("should return salt")
  void shouldReturnSalt() throws Exception {
    Data data = new Data(EMAIL);

    Account account = createAccountInstance(
        ID,
        PUBLIC_ID,
        EMAIL,
        PASSWORD,
        SALT,
        REMINDER,
        Role.ROLE_USER,
        true,
        true,
        true,
        true,
        CREATED_AT,
        UPDATED_AT,
        VERSION
    );

    when(accountService.findByEmail(anyString())).thenReturn(AccountDTO.fromAccount(account));

    mvc
        .perform(
            get(SALT_URL)
                .content(mapper.writeValueAsString(data))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.salt").value(SALT));
  }

  @Test
  @DisplayName("should return validation error if email consists of one space")
  void shouldReturnValidationErrorIfEmailConsistsOfOneSpace() throws Exception {
    List<String> emailMessages = new LinkedList<>(
        List.of(props.getProperty("email.blank.message"), props.getProperty("email.format.message"))
    );

    Data data = new Data(" ");

    mvc
        .perform(
            get(SALT_URL)
                .content(mapper.writeValueAsString(data))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isUnprocessableEntity())
        .andDo(res -> {
          String body = res.getResponse().getContentAsString();
          ReadContext ctx = JsonPath.parse(body);

          String errors = getSubErrorsString(body);

          Map<String, TestValidationError> map = convertErrorListToMap(mapper.readValue(errors, new TypeReference<>(){}));

          assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase(), ctx.read("$.status"));
          assertEquals("Validation Error", ctx.read("$.message"));
          assertTrue(Pattern.matches(TIMESTAMP_PATTERN, ctx.read("$.timestamp")));

          assertEquals("email", map.get("email").getField());
          assertEquals(" ", map.get("email").getRejectedValue());
          assertTrue(map.get("email").getValidationMessages().containsAll(emailMessages));
        });
  }

  @Test
  @DisplayName("should return NotFoundException if account with given email does not exist")
  void shouldReturnNotFoundIfAccountWithGivenEmailCannotBeFound() throws Exception {
    Data data = new Data(EMAIL);

    when(accountService.findByEmail(anyString())).thenThrow(new NotFoundException("Account does not exist"));

    mvc
        .perform(
            get(SALT_URL)
                .content(mapper.writeValueAsString(data))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.getReasonPhrase()))
        .andExpect(jsonPath("$.message").value("Account does not exist"))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)));

  }

  @AllArgsConstructor
  @Getter
  private final static class Data {
    private String email;
  }
}
