package org.dominik.pass.utils.validators;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

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

class EmailAddressValidatorTest {
  private static Validator validator;
  private static Properties props;

  @BeforeAll
  static void setUp() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
    props = readPropertiesFile("ValidationMessages.properties");
  }

  @ParameterizedTest
  @ValueSource(strings = {"dominik.krenski@gmail.com", "pass.dominik-krenski@ovh", "dominik@gmail"})
  @DisplayName("should pass if email has one of the given values")
  void shouldPassIfEmailsAreValid(String email) {
    Data data = new Data(email);

    Set<ConstraintViolation<Data>> violations = validator.validate(data);

    assertEquals(0, violations.size());
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {"dominik.yahoo"})
  @DisplayName("should fail if email has one of the given values")
  void shouldFailIfEmailIsOneOfTheFollowing(String email) {
    Data data = new Data(email);

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
