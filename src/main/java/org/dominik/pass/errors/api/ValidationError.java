package org.dominik.pass.errors.api;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;

@RequiredArgsConstructor
@Getter
@ToString
public final class ValidationError extends SubError {
  @NonNull private final String field;
  @NonNull private final Object rejectedValue;
  private final List<String> validationMessages;
}
