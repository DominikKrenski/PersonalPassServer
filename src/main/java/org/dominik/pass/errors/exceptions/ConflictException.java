package org.dominik.pass.errors.exceptions;

import lombok.NonNull;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@ToString(callSuper = true)
public class ConflictException extends BaseException {
  public ConflictException(@NonNull String message) {
    super(HttpStatus.CONFLICT, message);
  }
}
