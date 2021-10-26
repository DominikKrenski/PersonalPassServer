package org.dominik.pass.utils.validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public final class HexValidator implements ConstraintValidator<Hex, String> {
  private static final String HEX_PATTERN = "^[a-fA-F0-9]{2,}$";

  @Override
  public void initialize(Hex constraintAnnotation) {
    ConstraintValidator.super.initialize(constraintAnnotation);
  }

  @Override
  public boolean isValid(String entry, ConstraintValidatorContext constraintValidatorContext) {
    return entry != null && entry.length() % 2 == 0 && Pattern.matches(HEX_PATTERN, entry);
  }
}
