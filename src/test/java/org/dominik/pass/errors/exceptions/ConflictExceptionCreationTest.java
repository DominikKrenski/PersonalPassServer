package org.dominik.pass.errors.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ConflictExceptionCreationTest {
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
}
