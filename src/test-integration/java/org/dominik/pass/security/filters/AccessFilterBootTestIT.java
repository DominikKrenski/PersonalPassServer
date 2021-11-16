package org.dominik.pass.security.filters;

import org.dominik.pass.data.dto.AccountDTO;
import org.dominik.pass.services.definitions.AccountService;
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
import java.util.UUID;

import static org.dominik.pass.utils.TestUtils.generateJwtToken;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
@Sql("classpath:sql/02.auth-controller-test.sql")
@ActiveProfiles("integration")
class AccessFilterBootTestIT {
  private static final String URL = "/dummy";
  private static final String AUTH_HEADER = "Authorization";
  private static final String ISSUER = "personal-pass.dev";
  private static final String ACCESS_TOKEN_AUDIENCE = "access";
  private static final String REFRESH_TOKEN_AUDIENCE = "refresh";
  private static final String KEY = "gUkXn2r5u8x/A?D(G+KbPeShVmYq3s6v9y$B&E)H@McQfTjWnZr4u7w!z%C*F-Ja";
  private static final String TIMESTAMP_PATTERN = "\\d{2}/\\d{2}/\\d{4}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z";

  @Autowired MockMvc mvc;
  @Autowired AccountService accountService;

  @Test
  @DisplayName("should return 401 if URL is /dummy and no header is found")
  void shouldReturn401IfNoHeaderFound() throws Exception {
    mvc
        .perform(
            get(URL)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.getReasonPhrase()))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)))
        .andExpect(jsonPath("$.message").value("User must log in first"));
  }

  @Test
  @DisplayName("should return 403 if Authorization header does not start with `Bearer `")
  void shouldReturn403IfAuthorizationHeaderDoesNotStartWithBearer() throws Exception {
    mvc
        .perform(
            get(URL)
                .header(AUTH_HEADER, "token")
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.getReasonPhrase()))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)))
        .andExpect(jsonPath("$.message").value("Scheme missing or invalid"));
  }

  @Test
  @DisplayName("should return 403 if Authorization header starts with Bearer but substring is an empty string")
  void shouldReturn403IfAuthHeaderStartsWithBearerButSubstringIsEmpty() throws Exception {
    mvc
        .perform(
            get(URL)
                .header(AUTH_HEADER, "Bearer ")
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.getReasonPhrase()))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)))
        .andExpect(jsonPath("$.message").value("Scheme missing or invalid"));
  }

  @Test
  @DisplayName("should return 403 if token is expired")
  void shouldReturn403IfTokenIsExpired() throws Exception {
    long issued = Instant.now().getEpochSecond();
    long expiration = issued - 20;

    String accessToken = generateJwtToken(
        ISSUER,
        UUID.randomUUID().toString(),
        issued,
        ACCESS_TOKEN_AUDIENCE,
        expiration,
        KEY
    );

    mvc
        .perform(
            get(URL)
                .header(AUTH_HEADER, "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.getReasonPhrase()))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)))
        .andExpect(jsonPath("$.message").value("Access token expired"));
  }

  @Test
  @DisplayName("should return 403 if issuer is missing")
  void shouldReturn403IfIssuerIsMissing() throws Exception {
    long issued = Instant.now().getEpochSecond();
    long expired = issued + 10000;

    String accessToken = generateJwtToken(
        null,
        UUID.randomUUID().toString(),
        issued,
        ACCESS_TOKEN_AUDIENCE,
        expired,
        KEY
    );

    mvc
        .perform(
            get(URL)
                .header(AUTH_HEADER, "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.getReasonPhrase()))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)))
        .andExpect(jsonPath("$.message").value("Access token is invalid"));
  }

  @Test
  @DisplayName("should return 403 if issuer is invalid")
  void shouldReturn403IfIssuerIsInvalid() throws Exception {
    long issued = Instant.now().getEpochSecond();
    long expired = issued + 10000;

    String accessToken = generateJwtToken(
        "pass.dominik-krenski.ovh",
        UUID.randomUUID().toString(),
        issued,
        ACCESS_TOKEN_AUDIENCE,
        expired,
        KEY
    );

    mvc
        .perform(
            get(URL)
                .header(AUTH_HEADER, "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.getReasonPhrase()))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)))
        .andExpect(jsonPath("$.message").value("Access token is invalid"));
  }

  @Test
  @DisplayName("should return 403 if audience is missing")
  void shouldReturn403IfAudienceIsMissing() throws Exception {
    long issued = Instant.now().getEpochSecond();
    long expired = issued + 10000;

    String accessToken = generateJwtToken(
        ISSUER,
        UUID.randomUUID().toString(),
        issued,
        null,
        expired,
        KEY
    );

    mvc
        .perform(
            get(URL)
                .header(AUTH_HEADER, "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.getReasonPhrase()))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)))
        .andExpect(jsonPath("$.message").value("Access token is invalid"));
  }

  @Test
  @DisplayName("should return 403 if audience is invalid")
  void shouldReturn403IfAudienceIsInvalid() throws Exception {
    long issued = Instant.now().getEpochSecond();
    long expired = issued + 10000;

    String accessToken = generateJwtToken(
        ISSUER,
        UUID.randomUUID().toString(),
        issued,
        REFRESH_TOKEN_AUDIENCE,
        expired,
        KEY
    );

    mvc
        .perform(
            get(URL)
                .header(AUTH_HEADER, "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.getReasonPhrase()))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)))
        .andExpect(jsonPath("$.message").value("Access token is invalid"));
  }

  @Test
  @DisplayName("should return 403 if signature is invalid")
  void shouldReturn403IfSignatureIsInvalid() throws Exception {
    long issued = Instant.now().getEpochSecond();
    long expired = issued + 10000;

    String accessToken = generateJwtToken(
        ISSUER,
        UUID.randomUUID().toString(),
        issued,
        ACCESS_TOKEN_AUDIENCE,
        expired,
        KEY
    );

    accessToken = accessToken.substring(0, accessToken.length() - 1);

    mvc
        .perform(
            get(URL)
                .header(AUTH_HEADER, "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.getReasonPhrase()))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)))
        .andExpect(jsonPath("$.message").value("Access token is invalid"));
  }

  @Test
  @DisplayName("should return 403 if subject has invalid format")
  void shouldReturn403IfSubjetHashInvalidFormat() throws Exception {
    long issued = Instant.now().getEpochSecond();
    long expired = issued + 10000;

    String accessToken = generateJwtToken(
        ISSUER,
        "subject",
        issued,
        ACCESS_TOKEN_AUDIENCE,
        expired,
        KEY
    );

    mvc
        .perform(
            get(URL)
                .header(AUTH_HEADER, "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.getReasonPhrase()))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)))
        .andExpect(jsonPath("$.message").value("Access token is invalid"));
  }

  @Test
  @DisplayName("should return 403 if account is not found")
  void shouldReturn403IfAccountIsNotFound() throws Exception {
    long issued = Instant.now().getEpochSecond();
    long expired = issued + 10000;

    String accessToken = generateJwtToken(
        ISSUER,
        UUID.randomUUID().toString(),
        issued,
        ACCESS_TOKEN_AUDIENCE,
        expired,
        KEY
    );

    mvc
        .perform(
            get(URL)
                .header(AUTH_HEADER, "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.getReasonPhrase()))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)))
        .andExpect(jsonPath("$.message").value("Account does not exist"));
  }

  @Test
  @DisplayName("should return 400 if authenticated successfully but route does not exist")
  void shouldReturn400IfAuthSuccessfullyButRouteNotExist() throws Exception {
    AccountDTO accountDTO = accountService.findByEmail("dominik.krenski@gmail.com");
    long issued = Instant.now().getEpochSecond();
    long expired = issued + 10000;

    String accessToken = generateJwtToken(
        ISSUER,
        accountDTO.getPublicId().toString(),
        issued,
        ACCESS_TOKEN_AUDIENCE,
        expired,
        KEY
    );

    mvc
        .perform(
            get(URL)
                .header(AUTH_HEADER, "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)))
        .andExpect(jsonPath("$.message").value("Given route does not exist"));
  }
}
