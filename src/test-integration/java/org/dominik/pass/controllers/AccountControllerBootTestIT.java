package org.dominik.pass.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import org.dominik.pass.data.dto.AccountDTO;
import org.dominik.pass.db.repositories.RefreshTokenRepository;
import org.dominik.pass.services.definitions.AccountService;
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

import javax.persistence.EntityManager;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;

import static org.dominik.pass.utils.TestUtils.*;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
@Sql("classpath:/sql/01.token-repository-test.sql")
@ActiveProfiles("integration")
class AccountControllerBootTestIT {
  private static final String AUTH_HEADER = "Authorization";
  private static final String ACCOUNT_URL = "/accounts/";
  private static final String EMAIL_URL = "/accounts/email";
  private static final String ISSUER = "personal-pass.dev";
  private static final String AUDIENCE = "access";
  private static final String KEY = "gUkXn2r5u8x/A?D(G+KbPeShVmYq3s6v9y$B&E)H@McQfTjWnZr4u7w!z%C*F-Ja";
  private static final String TIMESTAMP_PATTERN = "\\d{2}/\\d{2}/\\d{4}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z";

  private static Properties props;
  private String accessToken;
  private AccountDTO accountDTO;

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper mapper;
  @Autowired AccountService accountService;
  @Autowired RefreshTokenRepository tokenRepository;
  @Autowired EntityManager em;

  @BeforeAll
  static void setUp() {
    props = readPropertiesFile("ValidationMessages.properties");
  }

  @BeforeEach
  void beforeEach() throws NoSuchAlgorithmException, InvalidKeyException, JsonProcessingException {
    accountDTO = accountService.findByEmail("dominik.krenski@gmail.com");
    long createdAt = Instant.now().getEpochSecond();

    accessToken = generateJwtToken(ISSUER, accountDTO.getPublicId().toString(), createdAt, AUDIENCE, createdAt + 1000, KEY);
  }

  @Test
  @DisplayName("should return info about account with reminder")
  void shouldReturnInfoAboutAccount() throws Exception {
    mvc
        .perform(
            get(ACCOUNT_URL)
                .header(AUTH_HEADER, "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("dominik.krenski@gmail.com"))
        .andExpect(jsonPath("$.reminder").value(accountDTO.getReminder()))
        .andExpect(jsonPath("$.createdAt").value(convertInstantIntoString(accountDTO.getCreatedAt())))
        .andExpect(jsonPath("$.updatedAt").value(convertInstantIntoString(accountDTO.getUpdatedAt())));
  }

  @Test
  @DisplayName("should return info about account without reminder")
  void shouldReturnAccountInfoWithoutReminder() throws Exception {
    accountDTO = accountService.findByEmail("dorciad@interia.pl");
    accessToken = generateJwtToken(ISSUER, accountDTO.getPublicId().toString(), Instant.now().getEpochSecond(), AUDIENCE, Instant.now().getEpochSecond() + 1000, KEY);

    mvc
        .perform(
            get(ACCOUNT_URL)
                .header(AUTH_HEADER, "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("dorciad@interia.pl"))
        .andExpect(jsonPath("$.reminder").doesNotExist())
        .andExpect(jsonPath("$.createdAt").value(convertInstantIntoString(accountDTO.getCreatedAt())))
        .andExpect(jsonPath("$.updatedAt").value(convertInstantIntoString(accountDTO.getUpdatedAt())));
  }

  @Test
  @DisplayName("should return Forbidden response if token has invalid subject")
  void shouldReturnForbiddenIfTokenHasInvalidSubject() throws Exception {
    accessToken = generateJwtToken(ISSUER, UUID.randomUUID().toString(), Instant.now().getEpochSecond(), AUDIENCE, Instant.now().getEpochSecond() + 1000, KEY);

    mvc
        .perform(
            get(ACCOUNT_URL)
                .header(AUTH_HEADER, "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.getReasonPhrase()))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)))
        .andExpect(jsonPath("$.message").value("Account does not exist"));
  }

  @Test
  @DisplayName("should return Conflict response if email is already in use")
  void shouldReturnConflictResponseIfEmailIsAlreadyInUse() throws Exception {
    String body = """
        {
          "email": "dorciad@interia.pl"
        }
        """;

    mvc
        .perform(
            put(EMAIL_URL)
                .header(AUTH_HEADER, "Bearer " + accessToken)
                .content(body)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.status").value(HttpStatus.CONFLICT.getReasonPhrase()))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)))
        .andExpect(jsonPath("$.message").value("Email is already in use"));
  }

  @Test
  @DisplayName("should return UnprocessableEntity if email is not valid")
  void shouldReturnUnprocessableEntityIfEmailIsNotValid() throws Exception {
    List<String> emailMessages = new LinkedList<>(
        List.of(
            props.getProperty("email.blank.message"),
            props.getProperty("email.format.message")
            )
    );

    String data = """
        {
          "email": "  "
        }
        """;

    mvc
        .perform(
            put(EMAIL_URL)
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
          Map<String, TestValidationError> map =
              convertErrorListToMap(
                  mapper.readValue(errors, new TypeReference<>(){}));

          assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase(), ctx.read("$.status"));
          assertEquals("Validation Error", ctx.read("$.message"));
          assertTrue(Pattern.matches(TIMESTAMP_PATTERN, ctx.read("$.timestamp")));
          assertEquals("email", map.get("email").getField());
          assertEquals("  ", map.get("email").getRejectedValue());
          assertTrue(map.get("email").getValidationMessages().containsAll(emailMessages));
        });
  }
}
