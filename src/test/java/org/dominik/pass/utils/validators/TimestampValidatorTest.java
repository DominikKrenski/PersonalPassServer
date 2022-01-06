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

class TimestampValidatorTest {
  private static Validator validator;
  private static Properties props;

  @BeforeAll
  static void setUp() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
    props = readPropertiesFile("ValidationMessages.properties");
  }

  @Test
  @DisplayName("should pass validation")
  void shouldPassValidation() {
    Data data = new Data("05/08/1984T05:34:12.567Z");

    Set<ConstraintViolation<Data>> violations = validator.validate(data);

    assertEquals(0, violations.size());
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {
    "5/08/1984T12:00:34.123Z",
    "05/8/1984T15:45:34.345Z",
    "05/08/201T14:45:59.321Z",
    "05/08/1984 14:23:12.222Z",
    "05/08/1984T2:12:43.555Z",
    "05/08/1984T12:5:12.333Z",
    "05/08/1984T12:02:3.456Z",
    "05/08/1984T12:34:25.55Z",
    "05/08/1984T12:22:12.456",
    "05-08/1984T20:23:55.999Z",
    "05/08/1984t20:56:56.111z"
  })
  void shouldFailIfStringHasOneOfGivenValues(String entry) {
    Data data = new Data(entry);

    Map<String, List<String>> errors = convertConstraintViolationsIntoMap(validator.validate(data));

    assertEquals(1, errors.size());
    assertTrue(errors.get("timestamp").contains(props.getProperty("timestamp.pattern.message")));
  }

  @AllArgsConstructor
  @Getter
  private static final class Data {

    @Timestamp(message = "{timestamp.pattern.message}")
    private String timestamp;
  }
}
