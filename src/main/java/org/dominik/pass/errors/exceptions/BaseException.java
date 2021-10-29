package org.dominik.pass.errors.exceptions;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import org.springframework.http.HttpStatus;

import java.time.Instant;

@Getter
@ToString(callSuper = true)
public abstract class BaseException extends RuntimeException {
  private final HttpStatus status;
  private final Instant timestamp;

  public BaseException(@NonNull HttpStatus status, @NonNull String message) {
    super(message);
    this.status = status;
    this.timestamp = Instant.now();
  }

  public BaseException(@NonNull String message) {
    this(HttpStatus.INTERNAL_SERVER_ERROR, message);
  }
}
