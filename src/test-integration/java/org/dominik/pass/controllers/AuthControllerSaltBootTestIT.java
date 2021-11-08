package org.dominik.pass.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import lombok.AllArgsConstructor;
import lombok.Getter;
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
import java.util.regex.Pattern;

import static org.dominik.pass.utils.TestUtils.*;
import static org.hamcrest.Matchers.matchesPattern;
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
@Sql("classpath:sql/02.auth-controller-test.sql")
@ActiveProfiles("integration")
class AuthControllerSaltBootTestIT {
  private static final String URL = "/auth/salt";
  private static final String EMAIL = "dominik.krenski@gmail.com";
  private static final String PASSWORD = "b468879149f241f69ce185ee2cc1764047ece00f7aad0128053a12aee5be320c";
  private static final String SALT = "711882a4dc3dcb437eb6151c09025594";
  private static final String TIMESTAMP_REGEX = "\\d{2}/\\d{2}/\\d{4}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z";

  private static Properties props;

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper mapper;

  @BeforeAll
  static void setUp() {
    props = readPropertiesFile("ValidationMessages.properties");
  }

  @Test
  @DisplayName("should return BadRequest if body is empty")
  void shouldReturnHttpMessageNotReadableResponseIfBodyIsEmpty() throws Exception {
    mvc
        .perform(
            get(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
        .andExpect(jsonPath("$.message").value("Message is not formatted properly"))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_REGEX)));
  }

  @Test
  @DisplayName("should return BadRequest if body has no closing bracket")
  void shouldReturnBadRequestIfBodyHasNoClosingBracket() throws Exception {
    String body = """
        {
          "email": "dominik.krenski@gmail.com"
        """;

    mvc
        .perform(
            get(URL)
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
  @DisplayName("should return MethodNotAllowed if request method is wrong")
  void shouldReturnMethodNotAllowedIfRequestMethodIsWrong() throws Exception {
    Data data = new Data(EMAIL);

    mvc
        .perform(
            post(URL)
                .content(mapper.writeValueAsString(data))
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
  void shouldReturnUnsupportedMediaTypeIfBodyContainsPlainText() throws Exception {
    String body = """
        "email": "dominik.krenski@gmail.com"
        """;
    mvc
        .perform(
            get(URL)
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
  @DisplayName("should return UnprocessableEntity if email is an empty string")
  void shouldReturnUnprocessableEntityIfEmailIsAnEmptyString() throws Exception {
    List<String> emailMessages = new LinkedList<>(
        List.of(props.getProperty("email.blank.message"), props.getProperty("email.format.message"))
    );

    String content = """
        {
          "email": ""
        }
        """;
    mvc
        .perform(
            get(URL)
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
          assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase(), ctx.read("$.status"));
          assertEquals("Validation Error", ctx.read("$.message"));
          assertTrue(Pattern.matches(TIMESTAMP_REGEX, ctx.read("$.timestamp")));
        });
  }

  @Test
  @DisplayName("should return NotFoundException if account with given email does not exist")
  void shouldReturnNotFoundExceptionIfAccountWithGivenEmailDoesNotExist() throws Exception {
    Data data = new Data("dominik.krenski@yahoo.com");

    mvc
        .perform(
            get(URL)
                .content(mapper.writeValueAsString(data))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.getReasonPhrase()))
        .andExpect(jsonPath("$.message").value("Account does not exist"))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_REGEX)));
  }

  @Test
  @DisplayName("should return salt belonging to dominik.krenski@gmail.com")
  void shouldReturnSaltBelongingToDominikKrenski() throws Exception {
    Data data = new Data(EMAIL);

    mvc
        .perform(
            get(URL)
                .content(mapper.writeValueAsString(data))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.salt").value("711882a4dc3dcb437eb6151c09025594"));
  }

  @Test
  @DisplayName("should return salt belonging to dorciad@interial.pl")
  void shouldReturnSaltBelongingToDorciad() throws Exception {
    Data data = new Data("dorciad@interia.pl");

    mvc
        .perform(
            get(URL)
                .content(mapper.writeValueAsString(data))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.salt").value("741882a4dc3dcb437eb6151d09025f94"));
  }

  @Test
  @DisplayName("should return salt belonging to krenska.dorota@gmail.com")
  void shouldReturnSaltBelongingToKrenskaDorota() throws Exception {
    Data data = new Data("krenska.dorota@gmail.com");

    mvc
        .perform(
            get(URL)
                .content(mapper.writeValueAsString(data))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.salt").value("741882a9dc3dcd437eb6151da9025f94"));
  }

  @AllArgsConstructor
  @Getter
  private static final class Data {
    private String email;
  }
}
