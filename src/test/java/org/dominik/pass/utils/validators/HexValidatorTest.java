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

public class HexValidatorTest {
  private static Validator validator;
  private static Properties props;

  @BeforeAll
  static void setUp() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
    props = readPropertiesFile("ValidationMessages.properties");
  }

  @Test
  @DisplayName("711882a4dc3dcb437eb6151c09025594 should pass validation")
  void shouldPassValidation() {
    Data data = new Data("711882a4dc3dcb437eb6151c09025594");

    Set<ConstraintViolation<Data>> violations = validator.validate(data);

    assertEquals(0, violations.size());
  }

  @Test
  @DisplayName("711882A4dc3dcB437Eb6151c09025594 should pass validation")
  void shouldPassValidationIgnoringCase() {
    Data data = new Data("711882A4dc3dcB437Eb6151c09025594");

    Set<ConstraintViolation<Data>> violations = validator.validate(data);

    assertEquals(0, violations.size());
  }

  @Test
  @DisplayName("should fail if string has odd number of characters")
  void shouldFailIfStringHasOddNumberOfCharacters() {
    Data data = new Data("711882A4dc3dcB437Eb6151c0902559");

    Map<String, List<String>> errors = convertConstraintViolationsIntoMap(validator.validate(data));

    assertEquals(1, errors.size());
    assertTrue(errors.get("entry").contains(props.getProperty("salt.hex.message")));
  }

  @Test
  @DisplayName("should fail if string contains invalid character")
  void shouldFailIfStringContainsInvalidCharacter() {
    Data data = new Data("7118g2A4dc3dcB437Eb6151c09025594");

    Map<String, List<String>> errors = convertConstraintViolationsIntoMap(validator.validate(data));

    assertEquals(1, errors.size());
    assertTrue(errors.get("entry").contains(props.getProperty("salt.hex.message")));
  }

  @Test
  @DisplayName("should fail is string is null")
  void shouldFailIfStringIsNull() {
    Data data = new Data(null);

    Map<String, List<String>> errors = convertConstraintViolationsIntoMap(validator.validate(data));

    assertEquals(1, errors.size());
    assertTrue(errors.get("entry").contains(props.getProperty("salt.hex.message")));
  }

  @Test
  @DisplayName("should fail if string is empty")
  void shouldFailIfStringIsEmpty() {
    Data data = new Data("");

    Map<String, List<String>> errors = convertConstraintViolationsIntoMap(validator.validate(data));

    assertEquals(1, errors.size());
    assertTrue(errors.get("entry").contains(props.getProperty("salt.hex.message")));
  }

  @Test
  @DisplayName("should fail if string consists of 4 spaces")
  void shouldFilIfStringConsistsOf4Spaces() {
    Data data = new Data("    ");

    Map<String, List<String>> errors = convertConstraintViolationsIntoMap(validator.validate(data));

    assertEquals(1, errors.size());
    assertTrue(errors.get("entry").contains(props.getProperty("salt.hex.message")));
  }

  @AllArgsConstructor
  @Getter
  private static final class Data {

    @Hex(message = "{salt.hex.message}")
    private String entry;
  }
}
