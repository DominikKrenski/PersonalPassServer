package org.dominik.pass.utils.validators;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = HexValidator.class)
@Documented
public @interface Hex {
  String message() default "Not valid HEX";
  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};
}
