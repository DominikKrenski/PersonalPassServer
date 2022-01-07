package org.dominik.pass.errors.exceptions;

import lombok.NonNull;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@ToString(callSuper = true)
public final class DataNumberException extends BaseException {
  public DataNumberException(@NonNull String message) {
    super(HttpStatus.BAD_REQUEST, message);
  }
}
