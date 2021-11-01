package org.dominik.pass.errors.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CustomExceptionsCreationTest {
  @Test
  @DisplayName("should create ConflictException successfully")
  void shouldCreateConflictExceptionInstance() {
    try {
      throw new ConflictException("Database conflict");
    } catch (BaseException ex) {
      assertEquals(HttpStatus.CONFLICT, ex.getStatus());
      assertEquals("Database conflict", ex.getMessage());
      assertNotNull(ex.getTimestamp());
    }
  }

  @Test
  @DisplayName("should create NotFoundException successfully")
  void shouldCreateNotFoundExceptionInstance() {
    try {
      throw new NotFoundException("Not Found");
    } catch (BaseException ex) {
      assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
      assertEquals("Not Found", ex.getMessage());
      assertNotNull(ex.getTimestamp());
    }
  }
}
