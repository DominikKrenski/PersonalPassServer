package org.dominik.pass.utils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.dominik.pass.utils.serializers.ApiInstantSerializer;

import javax.validation.ConstraintViolation;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serial;
import java.time.Instant;
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

  public static ObjectMapper createObjectMapperInstance() {
    ObjectMapper mapper = new ObjectMapper();

    // create module with custom Instant seializer
    SimpleModule instantModule = new SimpleModule();
    instantModule.addSerializer(Instant.class, new ApiInstantSerializer());

    mapper.registerModules(new JavaTimeModule(), new Jdk8Module(), instantModule);

    // set default property inclusion
    mapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_EMPTY);

    // set locale
    mapper.setLocale(new Locale("pl"));

    // set default property naming strategy
    mapper.setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE);

    // set properties' visibility
    mapper.setVisibility(PropertyAccessor.CREATOR, JsonAutoDetect.Visibility.PUBLIC_ONLY);
    mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.NONE);
    mapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.PUBLIC_ONLY);
    mapper.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.PUBLIC_ONLY);
    mapper.setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.PUBLIC_ONLY);

    // configure deserialization features
    mapper.configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, false);
    mapper.configure(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS, false);
    mapper.configure(DeserializationFeature.USE_LONG_FOR_INTS, false);
    mapper.configure(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY, false);
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
    mapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true);
    mapper.configure(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS, true);
    mapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, true);
    mapper.configure(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY, true);
    mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, true);
    mapper.configure(DeserializationFeature.FAIL_ON_UNRESOLVED_OBJECT_IDS, true);
    mapper.configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, true);
    mapper.configure(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES, true);
    mapper.configure(DeserializationFeature.FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY, true);
    mapper.configure(DeserializationFeature.FAIL_ON_TRAILING_TOKENS, true);
    mapper.configure(DeserializationFeature.WRAP_EXCEPTIONS, true);
    mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, false);
    mapper.configure(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS, false);
    mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, false);
    mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, false);
    mapper.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, false);
    mapper.configure(DeserializationFeature.ACCEPT_FLOAT_AS_INT, false);
    mapper.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, false);
    mapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, false);
    mapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, false);
    mapper.configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
    mapper.configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false);
    mapper.configure(DeserializationFeature.EAGER_DESERIALIZER_FETCH, true);

    // configure serialization features
    mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
    mapper.configure(SerializationFeature.INDENT_OUTPUT, false);
    mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, true);
    mapper.configure(SerializationFeature.FAIL_ON_SELF_REFERENCES, true);
    mapper.configure(SerializationFeature.WRAP_EXCEPTIONS, true);
    mapper.configure(SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS, true);
    mapper.configure(SerializationFeature.WRITE_SELF_REFERENCES_AS_NULL, false);
    mapper.configure(SerializationFeature.CLOSE_CLOSEABLE, false);
    mapper.configure(SerializationFeature.FLUSH_AFTER_WRITE_VALUE, true);
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    mapper.configure(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS, false);
    mapper.configure(SerializationFeature.WRITE_DATES_WITH_ZONE_ID, false);
    mapper.configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false);
    mapper.configure(SerializationFeature.WRITE_CHAR_ARRAYS_AS_JSON_ARRAYS, true);
    mapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, false);
    mapper.configure(SerializationFeature.WRITE_ENUMS_USING_INDEX, false);
    mapper.configure(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED, false);
    mapper.configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
    mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    mapper.configure(SerializationFeature.EAGER_SERIALIZER_FETCH, true);
    mapper.configure(SerializationFeature.USE_EQUALITY_FOR_OBJECT_ID, false);

    return mapper;
  }
}
