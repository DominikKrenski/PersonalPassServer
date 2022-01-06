package org.dominik.pass.utils.validators;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TimestampValidator.class)
@Documented
public @interface Timestamp {
  String message() default "Not valid timestamp";
  Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
