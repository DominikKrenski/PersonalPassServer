package org.dominik.pass.utils.validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class EmailAddressValidator implements ConstraintValidator<EmailAddress, String> {
  private static final String EMAIL_PATTERN = "^(?=.{1,254}$)(?=.{1,64}@)[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+)*@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$";

  @Override
  public void initialize(EmailAddress constraintAnnotation) {
    ConstraintValidator.super.initialize(constraintAnnotation);
  }

  @Override
  public boolean isValid(String email, ConstraintValidatorContext constraintValidatorContext) {
    return email != null && Pattern.matches(EMAIL_PATTERN, email);
  }
}
