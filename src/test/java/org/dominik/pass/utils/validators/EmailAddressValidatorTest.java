package org.dominik.pass.utils.validators;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static org.dominik.pass.utils.TestUtils.convertConstraintViolationsIntoMap;
import static org.dominik.pass.utils.TestUtils.readPropertiesFile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EmailAddressValidatorTest {
  private static Validator validator;
  private static Properties props;

  @BeforeAll
  static void setUp() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
    props = readPropertiesFile("ValidationMessages.properties");
  }

  @Test
  @DisplayName("should pass if email is dominik.krenski@gmail.com")
  void shouldPassIfEmailIsMine() {
    Data data = new Data("dominik.krenski@gmail.com");

    Set<ConstraintViolation<Data>> violations = validator.validate(data);

    assertEquals(0, violations.size());
  }

  @Test
  @DisplayName("should pass if email is pass.dominik-krenski.ovh")
  void shouldPassIfEmailContainsSubdomain() {
    Data data = new Data("pass.dominik-krenski@ovh");

    Set<ConstraintViolation<Data>> violations = validator.validate(data);

    assertEquals(0, violations.size());
  }

  @Test
  @DisplayName("should pass if email is dominik@gmail")
  void shouldPassIfEmailIsdominikAtGmail() {
    Data data = new Data("dominik@gmail");

    Set<ConstraintViolation<Data>> violations = validator.validate(data);

    assertEquals(0, violations.size());
  }

  @Test
  @DisplayName("should fail is email is null")
  void shouldFailIfEmailIsNull() {
    Data data = new Data(null);

    Map<String, List<String>> errors = convertConstraintViolationsIntoMap(validator.validate(data));

    assertEquals(1, errors.size());
    assertTrue(errors.get("email").contains(props.getProperty("email.format.message")));
  }

  @Test
  @DisplayName("should fail if email is dominik.yahoo")
  void shouldFailIfEmailIsDominikDotYahoo() {
    Data data = new Data("dominik.yahoo");

    Map<String, List<String>> errors = convertConstraintViolationsIntoMap(validator.validate(data));

    assertEquals(1, errors.size());
    assertTrue(errors.get("email").contains(props.getProperty("email.format.message")));
  }

  @AllArgsConstructor
  @Getter
  private static final class Data {

    @EmailAddress(message = "{email.format.message}")
    private String email;
  }
}
