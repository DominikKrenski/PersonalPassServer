package org.dominik.pass.errors.exceptions;

import org.dominik.pass.db.entities.Data;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CustomExceptionsCreationTest {
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

  @Test
  @DisplayName("should create InternalException successfully")
  void shouldCreateInternalExceptionInstance() {
    try {
      throw new InternalException("Internal error");
    } catch (InternalException ex) {
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatus());
      assertEquals("Internal error", ex.getMessage());
      assertNotNull(ex.getTimestamp());
    }
  }

  @Test
  @DisplayName("should create DataNumberException successfuly")
  void shouldCreateDataNumberExceptionSuccessfuly() {
    try {
      throw new DataNumberException("Data number is invalid");
    } catch (DataNumberException ex) {
      assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
      assertEquals("Data number is invalid", ex.getMessage());
      assertNotNull(ex.getTimestamp());
    }
  }
}
