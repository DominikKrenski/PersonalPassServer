package org.dominik.pass.security.filters;

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

import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.notNullValue;
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
@Sql("classpath:sql/02.auth-controller-test.sql")
@ActiveProfiles("integration")
public class LoginFilterBootTestIT {
  private static final String URL = "/auth/signin";
  private static final String TIMESTAMP_PATTERN = "\\d{2}/\\d{2}/\\d{4}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z";

  @Autowired MockMvc mvc;

  @Test
  @DisplayName("should return Authentication Exception if method is GET")
  void shouldReturnAuthenticationExIfMethodIsGet() throws Exception {
    String body = """
        {
          "email": "dominik.krenski@gmail.com",
          "password": "Dominik1984"
        }
        """;
    mvc
        .perform(
            get(URL)
                .content(body)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.getReasonPhrase()))
        .andExpect(jsonPath("$.message").value("Authentication method not supported"))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)));
  }

  @Test
  @DisplayName("should return Authentication Exception if body is empty")
  void shouldReturnAuthenticationExceptionIfBodyIsEmpty() throws Exception {
    mvc
        .perform(
            post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.getReasonPhrase()))
        .andExpect(jsonPath("$.message").value("There is a problem with request data"))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)));
  }

  @Test
  @DisplayName("should return Authentication Exception if body has no closing bracket")
  void shouldReturnAuthenticationExceptionIfBodyHasNoClosingBracket() throws Exception {
    String body = """
        {
          "email": "dominik.krenski@gmail.com",
          "password": "b468879149f241f69ce185ee2cc1764047ece00f7aad0128053a12aee5be320c"
        """;

    mvc
        .perform(
            post(URL)
                .content(body)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.getReasonPhrase()))
        .andExpect(jsonPath("$.message").value("There is a problem with request data"))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)));
  }

  @Test
  @DisplayName("should return Authentication Exception if body contains plain text")
  void shouldReturnAuthenticationExceptionIfBodyContainsPlainText() throws Exception {
    String body = """
        "email": "dominik.krenski@gmail.com",
        "password": "b468879149f241f69ce185ee2cc1764047ece00f7aad0128053a12aee5be320c"
        """;

    mvc
        .perform(
            post(URL)
                .content(body)
                .contentType(MediaType.TEXT_PLAIN)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.getReasonPhrase()))
        .andExpect(jsonPath("$.message").value("Content-Type not supported"))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)));
  }

  @Test
  @DisplayName("should return Authentication Exception if email is an empty string")
  void shouldReturnAuthenticationExceptionIfEmailIsAnEmptyString() throws Exception {
    String body = """
        {
          "email": "",
          "password": "b468879149f241f69ce185ee2cc1764047ece00f7aad0128053a12aee5be320c"
        }
        """;

    mvc
        .perform(
            post(URL)
                .content(body)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.getReasonPhrase()))
        .andExpect(jsonPath("$.message").value("Email or password invalid"))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)));
  }

  @Test
  @DisplayName("should return Authentication Exception if email is not found")
  void shouldReturnAuthenticationExceptionIfEmailIsNotFound() throws Exception {
    String body = """
        {
          "email": "dominik.krenski@yahoo.com",
          "password": "b468879149f241f69ce185ee2cc1764047ece00f7aad0128053a12aee5be320c"
        }
        """;
    mvc
        .perform(
            post(URL)
                .content(body)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.getReasonPhrase()))
        .andExpect(jsonPath("$.message").value("Email or password invalid"))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)));
  }

  @Test
  @DisplayName("should return Authentication Exception if password is not valid")
  void shouldReturnAuthenticationExceptionIfPasswordIsNotValid() throws Exception {
    String body = """
        {
          "email": "dominik.krenski@gmail.com",
          "password": "b468879149f241f69ce185ee2cc1764047ece00f7aad0128053a12aee5be320a"
        }
        """;

    mvc
        .perform(
            post(URL)
                .content(body)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.getReasonPhrase()))
        .andExpect(jsonPath("$.message").value("Email or password invalid"))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)));
  }

  @Test
  @DisplayName("should return successful response")
  void shouldReturnSuccessfulResponse() throws Exception {
    String body = """
        {
          "email": "dominik.krenski@gmail.com",
          "password": "b468879149f241f69ce185ee2cc1764047ece00f7aad0128053a12aee5be320c"
        }
        """;

    mvc
        .perform(
            post(URL)
                .content(body)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken", notNullValue()))
        .andExpect(jsonPath("$.refreshToken", notNullValue()));
  }
}
