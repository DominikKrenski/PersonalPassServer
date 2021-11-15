package org.dominik.pass.security.filters;

import org.dominik.pass.db.entities.Account;
import org.dominik.pass.db.entities.RefreshToken;
import org.dominik.pass.db.repositories.AccountRepository;
import org.dominik.pass.db.repositories.RefreshTokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.Instant;
import java.util.UUID;

import static org.dominik.pass.utils.TestUtils.generateJwtToken;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
@ActiveProfiles("integration")
class RefreshFilterBootTestIT {
  private static final String URL = "/auth/refresh";
  private static final String AUTH_HEADER = "Authorization";
  private static final String ISSUER = "personal-pass.dev";
  private static final String ACCESS_TOKEN_AUDIENCE = "access";
  private static final String REFRESH_TOKEN_AUDIENCE = "refresh";
  private static final String KEY = "gUkXn2r5u8x/A?D(G+KbPeShVmYq3s6v9y$B&E)H@McQfTjWnZr4u7w!z%C*F-Ja";
  private static final String TIMESTAMP_PATTERN = "\\d{2}/\\d{2}/\\d{4}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z";

  @Autowired MockMvc mvc;
  @Autowired EntityManager em;
  @Autowired RefreshTokenRepository tokenRepository;
  @Autowired AccountRepository accountRepository;

  @Test
  @DisplayName("should return 405 status if URL is `/auth/signup` and method is GET")
  void shouldReturn405IfUrlIsInvalid() throws Exception {
    mvc
        .perform(
            get("/auth/signup")
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isMethodNotAllowed());
  }

  @Test
  @DisplayName("should return 403 if request method is POST")
  void shouldReturn403IfRequestMethodIsPost() throws Exception {
    mvc
        .perform(
            post(URL)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.getReasonPhrase()))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)))
        .andExpect(jsonPath("$.message").value("Request method is not supported"));
  }

  @Test
  @DisplayName("should return 403 if Authorization header is missing")
  void shouldReturn403IfAuthorizationHeaderIsMissing() throws Exception {
    mvc
        .perform(
            get(URL)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.getReasonPhrase()))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)))
        .andExpect(jsonPath("$.message").value("Required header is missing"));
  }

  @Test
  @DisplayName("should return 403 if Authorization header does not start with Bearer ")
  void shouldReturn403IfAuthHeaderDoesNotStartWithBearer() throws Exception {
    mvc
        .perform(
            get(URL)
                .header(AUTH_HEADER, "Berer refresh_token")
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.getReasonPhrase()))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)))
        .andExpect(jsonPath("$.message").value("Scheme missing or invalid"));
  }

  @Test
  @DisplayName("should return 403 if Autorization header starts with Bearer but substring is an empty string")
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
    long issuedAt = Instant.now().getEpochSecond();
    long expiration = issuedAt - 20;

    String refreshToken = generateJwtToken(
        ISSUER,
        UUID.randomUUID().toString(),
        issuedAt,
        REFRESH_TOKEN_AUDIENCE,
        expiration,
        KEY
    );

    mvc
        .perform(
            get(URL)
                .header(AUTH_HEADER, "Bearer " + refreshToken)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.getReasonPhrase()))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)))
        .andExpect(jsonPath("$.message").value("Token is not valid"));
  }

  @Test
  @DisplayName("should return 403 if token has invalid audience")
  void shouldReturn403IfTokenHasInvalidAudience() throws Exception {
    long issudedAt = Instant.now().getEpochSecond();
    long expiration = issudedAt + 10000;

    String refreshToken = generateJwtToken(
        ISSUER,
        UUID.randomUUID().toString(),
        issudedAt,
        ACCESS_TOKEN_AUDIENCE,
        expiration,
        KEY
    );

    mvc
        .perform(
            get(URL)
                .header(AUTH_HEADER, "Bearer " + refreshToken)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.getReasonPhrase()))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)))
        .andExpect(jsonPath("$.message").value("Token is not valid"));
  }

  @Test
  @DisplayName("should return 403 if token has invalid issuer")
  void shouldReturn403IfTokenHasInvalidIssuer() throws Exception {
    long issuedAt = Instant.now().getEpochSecond();
    long expiration = issuedAt + 10000;

    String refreshToken = generateJwtToken(
        "pass.dominik-krenski.ovh",
        UUID.randomUUID().toString(),
        issuedAt,
        REFRESH_TOKEN_AUDIENCE,
        expiration,
        KEY
    );

    mvc
        .perform(
            get(URL)
                .header(AUTH_HEADER, "Bearer " + refreshToken)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.getReasonPhrase()))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)))
        .andExpect(jsonPath("$.message").value("Token is not valid"));
  }

  @Test
  @DisplayName("should return 403 if token has invalid signature")
  void shouldReturn403IfTokenHasInvalidSignature() throws Exception {
    long issuedAt = Instant.now().getEpochSecond();
    long expiration = issuedAt + 10000;

    String refreshToken = generateJwtToken(
        ISSUER,
        UUID.randomUUID().toString(),
        issuedAt,
        REFRESH_TOKEN_AUDIENCE,
        expiration,
        KEY
    );

    refreshToken = refreshToken.substring(0, refreshToken.length() - 1);

    mvc
        .perform(
            get(URL)
                .header(AUTH_HEADER, "Bearer " + refreshToken)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.getReasonPhrase()))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)))
        .andExpect(jsonPath("$.message").value("Token is not valid"));
  }

  @Test
  @DisplayName("should return 403 if token cannot be found in database")
  void shouldReturn403IfTokenCannotBeFoundInDatabase() throws Exception {
    long issuedAt = Instant.now().getEpochSecond();
    long expiration = issuedAt + 10000;

    String refreshToken = generateJwtToken(
        ISSUER,
        UUID.randomUUID().toString(),
        issuedAt,
        REFRESH_TOKEN_AUDIENCE,
        expiration,
        KEY
    );

    mvc
        .perform(
            get(URL)
                .header(AUTH_HEADER, "Bearer " + refreshToken)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.getReasonPhrase()))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)))
        .andExpect(jsonPath("$.message").value("Given token does not exist"));
  }

  @Test
  @DisplayName("should return 403 if refresh token has been used already")
  void shouldReturn403IfRefreshTokenHasBeenTokenAlready() throws Exception {
    Account account = accountRepository.save(
        new Account(
            "dominik.krenski@gmail.com",
            "$2a$12$1rCLWvFfj1lcHm2lP1MJ/OyTNFseGh.mVdAGinD1gaOjjftBToa22",
            "711882a4dc3dcb437eb6151c09025594",
            "dummy message"
            )
    );
    accountRepository.flush();

    long created = Instant.now().getEpochSecond();
    long expired = created + 10000;

    String refreshToken = generateJwtToken(
        ISSUER,
        account.getPublicId().toString(),
        created,
        REFRESH_TOKEN_AUDIENCE,
        expired,
        KEY
    );

    tokenRepository.save(new RefreshToken(refreshToken, account));
    tokenRepository.flush();

    tokenRepository.markTokenAsUsed(refreshToken);
    tokenRepository.flush();

    em.clear(); // clear cache to force Hibernate to reflect changes in database

    mvc
        .perform(
            get(URL)
                .header(AUTH_HEADER, "Bearer " + refreshToken)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.getReasonPhrase()))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)))
        .andExpect(jsonPath("$.message").value("Security Exception. Server detected that the same token has been used again"));
  }

  @Test
  @DisplayName("should return new pair of tokens")
  void shouldReturnNewPairOfTokens() throws Exception {
    Account account = accountRepository.save(
        new Account(
            "dominik.krenski@gmail.com",
            "$2a$12$1rCLWvFfj1lcHm2lP1MJ/OyTNFseGh.mVdAGinD1gaOjjftBToa22",
            "711882a4dc3dcb437eb6151c09025594",
            "dummy message"
        )
    );
    accountRepository.flush();

    long created = Instant.now().getEpochSecond();
    long expired = created + 10000;

    String refreshToken = generateJwtToken(
        ISSUER,
        account.getPublicId().toString(),
        created,
        REFRESH_TOKEN_AUDIENCE,
        expired,
        KEY
    );

    tokenRepository.save(new RefreshToken(refreshToken, account));
    tokenRepository.flush();

    em.clear();

    mvc
        .perform(
            get(URL)
                .header(AUTH_HEADER, "Bearer " + refreshToken)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken", notNullValue()))
        .andExpect(jsonPath("$.refreshToken", notNullValue()))
        .andDo(res -> {
          em.flush();
          em.clear();

          RefreshToken token = tokenRepository.findByToken(refreshToken).orElseThrow();

          assertTrue(token.isUsed());
          assertEquals(2, tokenRepository.findAll().size());
        });
  }
}
