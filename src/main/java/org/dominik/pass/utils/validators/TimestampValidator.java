package org.dominik.pass.utils.validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class TimestampValidator implements ConstraintValidator<Timestamp, String> {
  private static final String TIMESTAMP_PATTERN = "\\d{2}/\\d{2}/\\d{4}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z";

  @Override
  public void initialize(Timestamp constraintAnnotation) {
    ConstraintValidator.super.initialize(constraintAnnotation);
  }

  @Override
  public boolean isValid(String timestamp, ConstraintValidatorContext constraintValidatorContext) {
    return timestamp != null && Pattern.matches(TIMESTAMP_PATTERN, timestamp);
  }
}
