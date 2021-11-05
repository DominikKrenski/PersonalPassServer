package org.dominik.pass.advices;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.dominik.pass.configuration.AuthControllerMvcTestConfig;
import org.dominik.pass.controllers.AuthController;
import org.dominik.pass.data.dto.RegistrationDTO;
import org.dominik.pass.errors.exceptions.ConflictException;
import org.dominik.pass.services.definitions.AccountService;
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

import static org.dominik.pass.utils.TestUtils.createRegistrationDtoInstance;
import static org.hamcrest.Matchers.matchesPattern;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
public class ApiControllerAdviceMvcTest {
  private static final String EMAIL ="dominik.krenski@gmail.com";
  private static final String PASSWORD = "b468879149f241f69ce185ee2cc1764047ece00f7aad0128053a12aee5be320c";
  private static final String SALT = "711882a4dc3dcb437eb6151c09025594";
  private static final String URL = "/auth/signup";
  private static final String TIMESTAMP_PATTERN="\\d{2}/\\d{2}/\\d{4}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z";

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper mapper;
  @Autowired AccountService accountService;

  @Test
  @DisplayName("should return message not readable if request body is missing")
  void shouldReturnMessageNotReadable() throws Exception {
    mvc
        .perform(
            post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)))
        .andExpect(jsonPath("$.message").value("Message is not formatted properly"));
  }

  @Test
  @DisplayName("should return request method not supported if get request was sent instead of post")
  void shouldReturnMethodNotSupported() throws Exception {
    mvc
        .perform(
            get(URL)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isMethodNotAllowed())
        .andExpect(jsonPath("$.status").value(HttpStatus.METHOD_NOT_ALLOWED.getReasonPhrase()))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)))
        .andExpect(jsonPath("$.message").value("Method not allowed for given route"));
  }

  @Test
  @DisplayName("should return handler not found if given route does not exist")
  void shouldReturnHandlerNotFoundException() throws Exception {
    mvc
        .perform(
            get("/dummy-url")
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)))
        .andExpect(jsonPath("$.message").value("Given route does not exist"));
  }

  @Test
  @DisplayName("should return media type not supported")
  void shouldReturnMediaTypeNotSupported() throws Exception {
    mvc
        .perform(
            post(URL)
                .contentType(MediaType.TEXT_PLAIN)
                .accept(MediaType.APPLICATION_JSON)
                .content("password=password")
        )
        .andExpect(status().isUnsupportedMediaType())
        .andExpect(jsonPath("$.status").value(HttpStatus.UNSUPPORTED_MEDIA_TYPE.getReasonPhrase()))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)))
        .andExpect(jsonPath("$.message").value("MediaType Not Supported"));
  }

  @Test
  @DisplayName("should return conflict message")
  void shouldReturnConflictMessage() throws Exception {
    when(accountService.register(any(RegistrationDTO.class))).thenThrow(new ConflictException("Email with given email already exists"));

    RegistrationDTO dto = createRegistrationDtoInstance(EMAIL, PASSWORD, SALT, null);
    String data = mapper.writeValueAsString(dto);

    mvc
        .perform(
            post(URL)
                .content(data)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.status").value(HttpStatus.CONFLICT.getReasonPhrase()))
        .andExpect(jsonPath("$.timestamp", matchesPattern(TIMESTAMP_PATTERN)))
        .andExpect(jsonPath("$.message").value("Email with given email already exists"));
  }
}
