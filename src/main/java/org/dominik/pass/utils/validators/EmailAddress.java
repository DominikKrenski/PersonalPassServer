package org.dominik.pass.utils.validators;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EmailAddressValidator.class)
@Documented
public @interface EmailAddress {
  String message() default "Not valid email";
  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};
}
