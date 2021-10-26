package org.dominik.pass.utils;

import javax.validation.ConstraintViolation;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public final class TestUtils {
  public static Properties readPropertiesFile(String filename) {
    Properties props = new Properties();

    try (InputStream stream = TestUtils.class.getClassLoader().getResourceAsStream(filename)) {
      if (stream != null) {
        props.load(stream);
      }
    } catch (IOException ex) {
      ex.printStackTrace();
    }

    return props;
  }

  public static <T>Map<String, List<String>> convertConstraintViolationsIntoMap(Set<ConstraintViolation<T>> violations) {
    Map<String, List<String>> map = new HashMap<>();

    for (ConstraintViolation<T> violation : violations) {
      String property = violation.getPropertyPath().toString();

      if (!map.containsKey(property))
        map.put(property, new LinkedList<>());

      map.get(property).add(violation.getMessage());
    }

    return map;
  }
}
