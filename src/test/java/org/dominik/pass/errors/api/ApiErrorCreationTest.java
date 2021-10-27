package org.dominik.pass.errors.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ApiErrorCreationTest {

  @Test
  @DisplayName("should throw null pointer if status is null")
  void shouldThrowNullPointerIfStatusIsNull() {
    assertThrows(NullPointerException.class, () -> {
      ApiError apiError = ApiError
          .builder()
          .status(null)
          .timestamp(Instant.now())
          .message("Validation Error")
          .build();
    });
  }

  @Test
  @DisplayName("should throw null pointer if timestamp is null")
  void shouldThrowNullPointerIfTimestampIsNull() {
    assertThrows(NullPointerException.class, () -> {
      ApiError apiError = ApiError
          .builder()
          .status(HttpStatus.BAD_GATEWAY)
          .timestamp(null)
          .message("Dummy message")
          .build();
    });
  }

  @Test
  @DisplayName("should throw null pointer if message is null")
  void shouldThrowNullPointerIfMessageIsNull() {
    assertThrows(NullPointerException.class, () -> {
      ApiError apiError = ApiError
          .builder()
          .status(HttpStatus.BAD_GATEWAY)
          .timestamp(Instant.now())
          .message(null)
          .build();
    });
  }

  @Test
  @DisplayName("should throw null pointer if status is not invoked")
  void shouldThrowNullPointerIfStatusNotInvoked() {
    assertThrows(NullPointerException.class, () -> {
      ApiError apiError = ApiError
          .builder()
          .timestamp(Instant.now())
          .message("Dummy message")
          .build();
    });
  }

  @Test
  @DisplayName("should throw null pointer if timestamp is not invoked")
  void shouldThrowNullPointerIfTimestampNotInvoked() {
    assertThrows(NullPointerException.class, () -> {
      ApiError apiError = ApiError
          .builder()
          .status(HttpStatus.UNPROCESSABLE_ENTITY)
          .message("Dummy message")
          .build();
    });
  }

  @Test
  @DisplayName("should throw null pointer if message not invoked")
  void shouldThrowNullPointerIfMessageNotInvoked() {
    assertThrows(NullPointerException.class, () -> {
      ApiError apiError = ApiError
          .builder()
          .status(HttpStatus.MULTI_STATUS)
          .timestamp(Instant.now())
          .build();
    });
  }

  @Test
  @DisplayName("errors should be null if none passed in")
  void errorsShouldBeNullIfNonePassed() {
    ApiError apiError = ApiError
        .builder()
        .status(HttpStatus.MULTI_STATUS)
        .timestamp(Instant.now())
        .message("Dummy message")
        .build();

    assertNull(apiError.getErrors());
  }

  @Test
  @DisplayName("should initialize all fields successfully")
  void shouldInitializeAllFields() throws NoSuchFieldException, IllegalAccessException {
    ValidationError passwordError = new ValidationError(
        "password",
        "password",
        List.of("String is not valid HEX string", "Password must be exactly 64 characters long")
    );

    ValidationError emailError = new ValidationError(
        "email",
        "dominik.yahoo",
        List.of("Email is not valid")
    );

    List<ValidationError> errors = new LinkedList<>();
    errors.add(passwordError);
    errors.add(emailError);

    ApiError apiError = ApiError
        .builder()
        .status(HttpStatus.UNPROCESSABLE_ENTITY)
        .timestamp(Instant.now())
        .message("Validation Error")
        .errors(errors)
        .build();

    ValidationError restoredPasswordError = restoreValidationError(apiError.getErrors().get(0));
    ValidationError restoredEmailError = restoreValidationError(apiError.getErrors().get(1));

    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase(), apiError.getStatus());
    assertNotNull(apiError.getTimestamp());
    assertEquals("Validation Error", apiError.getMessage());
    assertEquals(passwordError.getField(), restoredPasswordError.getField());
    assertEquals(passwordError.getRejectedValue(), restoredPasswordError.getRejectedValue());
    assertTrue(passwordError.getValidationMessages().containsAll(restoredPasswordError.getValidationMessages()));
    assertEquals(emailError.getField(), restoredEmailError.getField());
    assertEquals(emailError.getRejectedValue(), restoredEmailError.getRejectedValue());
    assertTrue(emailError.getValidationMessages().containsAll(restoredEmailError.getValidationMessages()));
  }

  @SuppressWarnings("unchecked")
  private ValidationError restoreValidationError(SubError error) throws NoSuchFieldException, IllegalAccessException {
    Class<?> clazz = error.getClass();
    Field field = clazz.getDeclaredField("field");
    field.setAccessible(true);
    String fieldName = (String) field.get(error);

    field = clazz.getDeclaredField("rejectedValue");
    field.setAccessible(true);
    Object rejectedValue = field.get(error);

    field = clazz.getDeclaredField("validationMessages");
    field.setAccessible(true);
    List<String> validationMessages = (List<String>) field.get(error);

    return new ValidationError(fieldName, rejectedValue, validationMessages);
  }
}
