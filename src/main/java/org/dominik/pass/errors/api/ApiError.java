package org.dominik.pass.errors.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;

@Getter
@ToString
public final class ApiError {

  @Schema(
    description = "Response status",
    name = "status",
    required = true
  )
  private final String status;

  @Schema(
    description = "Date when exception occurred",
    name = "timestamp",
    required = true,
    example = "30/10/2020T21:45:59.234Z"
  )
  private final Instant timestamp;

  @Schema(
    description = "Message describing error",
    name = "message",
    required = true
  )
  private final String message;

  @Schema(
    description = "Additional errors",
    name = "errors"
  )
  private final List<? extends SubError> errors;

  private ApiError(@NonNull String status, @NonNull Instant timestamp, @NonNull String message, List<? extends SubError> errors) {
    this.status = status;
    this.timestamp = timestamp;
    this.message = message;
    this.errors = errors;
  }

  public static ErrorBuilder builder() {
    return new ErrorBuilder();
  }

  public static final class ErrorBuilder {
    private String status;
    private Instant timestamp;
    private String message;
    private List<? extends SubError> errors;

    private ErrorBuilder() {}

    public ErrorBuilder status(@NonNull HttpStatus status) {
      this.status = status.getReasonPhrase();
      return this;
    }

    public ErrorBuilder timestamp(@NonNull Instant timestamp) {
      this.timestamp = timestamp;
      return this;
    }

    public ErrorBuilder message(@NonNull String message) {
      this.message = message;
      return this;
    }

    public ErrorBuilder errors(@NonNull List<? extends SubError> errors) {
      this.errors = errors;
      return this;
    }

    public ApiError build() {
      return new ApiError(status, timestamp, message, errors);
    }
  }
}
