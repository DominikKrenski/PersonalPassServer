package org.dominik.pass.errors.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;

import static org.dominik.pass.utils.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

public class ApiErrorSerializationTest {
  private static final String TIMESTAMP_PATTERN="\\d{2}/\\d{2}/\\d{4}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z";
  private static Properties props;
  private static ObjectMapper mapper;

  @BeforeAll
  static void setUp() {
    mapper = createObjectMapperInstance();
    props = readPropertiesFile("ValidationError.messages");
  }

  @Test
  @DisplayName("should serialize without sub errors")
  void shouldSerializeWithoutSubErrors() throws JsonProcessingException {
    ApiError apiError = ApiError
        .builder()
        .status(HttpStatus.MULTI_STATUS)
        .timestamp(Instant.now())
        .message("Dummy message")
        .build();

    String json = mapper.writeValueAsString(apiError);

    ReadContext ctx = JsonPath.parse(json);

    assertEquals(HttpStatus.MULTI_STATUS.getReasonPhrase(), ctx.read("$.status"));
    assertTrue(Pattern.matches(TIMESTAMP_PATTERN, ctx.read("$.timestamp")));
    assertEquals("Dummy message", ctx.read("$.message"));
    assertThrows(PathNotFoundException.class, () -> ctx.read("$.errors"));
  }

  @Test
  @DisplayName("should serialize with 2 validation errors")
  void shouldSerializeWith2ValidationErrors() throws JsonProcessingException {
    List<String> passwordMessages = new LinkedList<>();
    passwordMessages.add(props.getProperty("password.hex.message"));
    passwordMessages.add(props.getProperty("password.length.message"));

    List<String> saltMessages = new LinkedList<>();
    saltMessages.add(props.getProperty("salt.hex.message"));
    saltMessages.add(props.getProperty("salt.length.message"));

    ValidationError passwordError = new ValidationError("password", "aa", passwordMessages);
    ValidationError saltError = new ValidationError("salt", "bbb", saltMessages);

    List<ValidationError> validationErrors = new LinkedList<>();
    validationErrors.add(passwordError);
    validationErrors.add(saltError);

    ApiError apiError = ApiError
        .builder()
        .status(HttpStatus.UNPROCESSABLE_ENTITY)
        .timestamp(Instant.now())
        .message("Validation Error")
        .errors(validationErrors)
        .build();

    String json = mapper.writeValueAsString(apiError);

    ReadContext ctx = JsonPath.parse(json);

    String errorsString = getSubErrorsString(json);
    Map<String, TestValidationError> map = convertErrorListToMap(mapper.readValue(errorsString, new TypeReference<>() {}));

    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase(), ctx.read("$.status"));
    assertTrue(Pattern.matches(TIMESTAMP_PATTERN, ctx.read("$.timestamp")));
    assertEquals("Validation Error", ctx.read("$.message"));
    assertEquals("password", map.get("password").getField());
    assertEquals("aa", map.get("password").getRejectedValue());
    assertTrue(map.get("password").getValidationMessages().containsAll(passwordMessages));
    assertEquals("salt", map.get("salt").getField());
    assertEquals("bbb", map.get("salt").getRejectedValue());
    assertTrue(map.get("salt").getValidationMessages().containsAll(saltMessages));

  }
}
