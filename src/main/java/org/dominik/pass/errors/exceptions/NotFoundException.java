package org.dominik.pass.errors.exceptions;

import lombok.NonNull;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@ToString(callSuper = true)
public class NotFoundException extends BaseException {
  public NotFoundException(@NonNull String message) { super(HttpStatus.NOT_FOUND, message); }
}
