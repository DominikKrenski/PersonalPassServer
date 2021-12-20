package org.dominik.pass.utils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.dominik.pass.data.dto.RegistrationDTO;
import org.dominik.pass.data.enums.Role;
import org.dominik.pass.db.entities.Account;
import org.dominik.pass.db.entities.Address;
import org.dominik.pass.db.entities.RefreshToken;
import org.dominik.pass.db.entities.Site;
import org.dominik.pass.utils.serializers.ApiInstantSerializer;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.validation.ConstraintViolation;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TestUtils {
  public static Account createAccountInstance(
      Long id,
      UUID publicId,
      String email,
      String password,
      String salt,
      String reminder,
      Role role,
      boolean accountNonExpired,
      boolean accountNonLocked,
      boolean credentialsNonExpired,
      boolean enabled,
      Instant createdAt,
      Instant updatedAt,
      short version) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
    Class<?> clazz = Account.class;
    Class<?> superClazz = clazz.getSuperclass();

    // get class default constructor
    Constructor<?> constructor = clazz.getDeclaredConstructor();

    // set constructor accessible
    constructor.setAccessible(true);

    // create account instance
    Account account = (Account) constructor.newInstance();

    // get all class fields
    List<Field> fields = Arrays.asList(clazz.getDeclaredFields());

    // set all fields accessible
    fields.forEach(field -> field.setAccessible(true));

    // get all super class fields
    List<Field> superFields = Arrays.asList(superClazz.getDeclaredFields());

    // set all super class fields accessible
    superFields.forEach(field -> field.setAccessible(true));

    // set values of super class fields
    superFields.forEach(field -> {
      String fieldname = field.getName();

      try {
        switch (fieldname) {
          case "createdAt" -> field.set(account, createdAt);
          case "updatedAt" -> field.set(account, updatedAt);
          case "version" -> field.set(account, version);
        }
      } catch (IllegalAccessException ex) {
        ex.printStackTrace();
      }
    });

    // set values of class fields
    fields.forEach(field -> {
      String fieldname = field.getName();

      try {
        switch (fieldname) {
          case "id" -> field.set(account, id);
          case "publicId" -> field.set(account, publicId);
          case "email" -> field.set(account, email);
          case "password" -> field.set(account, password);
          case "salt" -> field.set(account, salt);
          case "reminder" -> field.set(account, reminder);
          case "role" -> field.set(account, role);
          case "accountNonExpired" -> field.set(account, accountNonExpired);
          case "accountNonLocked" -> field.set(account, accountNonLocked);
          case "credentialsNonExpired" -> field.set(account, credentialsNonExpired);
          case "enabled" -> field.set(account, enabled);
        }
      } catch (IllegalAccessException ex) {
        ex.printStackTrace();
      }
    });

    return account;
  }

  public static Site createSiteInstance(
      Long id,
      UUID publicId,
      String entry,
      Account account,
      Instant createdAt,
      Instant updatedAt,
      short version
  ) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
    Class<?> clazz = Site.class;
    Class<?> superClazz = clazz.getSuperclass();

    // get class default constructor
    Constructor<?> constructor = clazz.getDeclaredConstructor();

    // set constructor accessible
    constructor.setAccessible(true);

    // create Site instance
    Site site = (Site) constructor.newInstance();

    // get all class fields
    List<Field> fields = Arrays.asList(clazz.getDeclaredFields());

    // set all fields accessible
    fields.forEach(field -> field.setAccessible(true));

    // get all super class fields
    List<Field> superFields = Arrays.asList(superClazz.getDeclaredFields());

    // set all super class fields accessible
    superFields.forEach(field -> field.setAccessible(true));

    // set values of super class fields
    superFields.forEach(field -> {
      String fieldname = field.getName();

      try {
        switch (fieldname) {
          case "createdAt" -> field.set(site, createdAt);
          case "updatedAt" -> field.set(site, updatedAt);
          case "version" -> field.set(site, version);
        }
      } catch (IllegalAccessException ex) {
        ex.printStackTrace();
      }
    });

    // set values of class fields
    fields.forEach(field -> {
      String fieldname = field.getName();

      try {
        switch (fieldname) {
          case "id" -> field.set(site, id);
          case "publicId" -> field.set(site, publicId);
          case "site" -> field.set(site, entry);
          case "account" -> field.set(site, account);
        }
      } catch (IllegalAccessException ex) {
        ex.printStackTrace();
      }
    });

    return site;
  }

  public static Address createAddressInstance(
      Long id,
      UUID publicId,
      String entry,
      Account account,
      Instant createdAt,
      Instant updatedAt,
      short version
  ) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
    Class<?> clazz = Address.class;
    Class<?> superClazz = clazz.getSuperclass();

    // get class default constructor
    Constructor<?> constructor = clazz.getDeclaredConstructor();

    // set constructor accessible
    constructor.setAccessible(true);

    // create Address instance
    Address address = (Address) constructor.newInstance();

    // get all class fields
    List<Field> fields = Arrays.asList(clazz.getDeclaredFields());

    // set all fields accessible
    fields.forEach(field -> field.setAccessible(true));

    // get all super class fields
    List<Field> superFields = Arrays.asList(superClazz.getDeclaredFields());

    // set all super class fields accessible
    superFields.forEach(field -> field.setAccessible(true));

    // set values of super class fields
    superFields.forEach(field -> {
      String fieldname = field.getName();

      try {
        switch (fieldname) {
          case "createdAt" -> field.set(address, createdAt);
          case "updatedAt" -> field.set(address, updatedAt);
          case "version" -> field.set(address, version);
        }
      } catch (IllegalAccessException ex) {
        ex.printStackTrace();
      }
    });

    // set values of class fields
    fields.forEach(field -> {
      String fieldname = field.getName();

      try {
        switch (fieldname) {
          case "id" -> field.set(address, id);
          case "publicId" -> field.set(address, publicId);
          case "address" -> field.set(address, entry);
          case "account" -> field.set(address, account);
        }
      } catch (IllegalAccessException ex) {
        ex.printStackTrace();
      }
    });

    return address;
  }

  public static RefreshToken createRefreshTokenInstance(
      Long id,
      String token,
      boolean used,
      Account account,
      Instant createdAt,
      Instant updatedAt,
      short version
  ) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
    Class<?> clazz = RefreshToken.class;
    Class<?> superClazz = clazz.getSuperclass();

    // get class default constructor
    Constructor<?> constructor = clazz.getDeclaredConstructor();

    // set constructor accessible
    constructor.setAccessible(true);

    // create RefreshToken instance
    RefreshToken refreshToken = (RefreshToken) constructor.newInstance();

    // get all class fields
    List<Field> fields = Arrays.asList(clazz.getDeclaredFields());

    // set all fields accessible
    fields.forEach(field -> field.setAccessible(true));

    // get all super class fields
    List<Field> superFields = Arrays.asList(superClazz.getDeclaredFields());

    // set all super class fields accessible
    superFields.forEach(field -> field.setAccessible(true));

    // set values of super class fields
    superFields.forEach(field -> {
      String fieldname = field.getName();

      try {
        switch (fieldname) {
          case "createdAt" -> field.set(refreshToken, createdAt);
          case "updatedAt" -> field.set(refreshToken, updatedAt);
          case "version" -> field.set(refreshToken, version);
        }
      } catch (IllegalAccessException ex) {
        ex.printStackTrace();
      }
    });

    // set values of class fields
    fields.forEach(field -> {
      String fieldname = field.getName();

      try {
        switch (fieldname) {
          case "id" -> field.set(refreshToken, id);
          case "used" -> field.set(refreshToken, used);
          case "token" -> field.set(refreshToken, token);
          case "account" -> field.set(refreshToken, account);
        }
      } catch (IllegalAccessException ex) {
        ex.printStackTrace();
      }
    });

    return refreshToken;
  }

  public static RegistrationDTO createRegistrationDtoInstance(String email, String password, String salt, String reminder) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
    Class<?> clazz = RegistrationDTO.class;

    // get class constructor
    Constructor<?> constructor = clazz.getDeclaredConstructor();

    // set constructor accessible
    constructor.setAccessible(true);

    // create RegistrationDTO instance
    RegistrationDTO dto = (RegistrationDTO) constructor.newInstance();

    // get all class fields
    List<Field> fields = Arrays.asList(clazz.getDeclaredFields());

    // set all fields accessible
    fields.forEach(field -> field.setAccessible(true));

    // set all fields's values
    fields.forEach(field -> {
      String fieldname = field.getName();

      try {
        switch (fieldname) {
          case "email" -> field.set(dto, email);
          case "password" -> field.set(dto, password);
          case "salt" -> field.set(dto, salt);
          case "reminder" -> field.set(dto, reminder);
        }
      } catch (IllegalAccessException ex) {
        ex.printStackTrace();
      }
    });

    return dto;
  }

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

  public static String getSubErrorsString(String json) {
    String regex = "(.*)(\"errors\":\\[.*]}])(.*)";
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(json);

    String substring = null;

    if (matcher.matches()) {
      substring = matcher.group(2).substring(9);
    }

    return substring;
  }

  public static Map<String, TestValidationError> convertErrorListToMap(List<TestValidationError> validationErrors) {
    Map<String, TestValidationError> map = new HashMap<>();
    validationErrors.forEach(error -> map.put(error.getField(), error));

    return map;
  }

  public static byte[] hmacSHA512(String data, String key) throws NoSuchAlgorithmException, InvalidKeyException {
    Mac hmacSHA512 = Mac.getInstance("HmacSHA512");
    SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
    hmacSHA512.init(keySpec);

    return hmacSHA512.doFinal(data.getBytes(StandardCharsets.UTF_8));
  }

  public static String base64UrlEncode(String data) {
    return Base64.getUrlEncoder().withoutPadding().encodeToString(data.getBytes(StandardCharsets.UTF_8));
  }

  public static String base64UrlEncode(byte[] data) {
    return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
  }

  public static String base64UrlDecode(String data) {
    return new String(Base64.getUrlDecoder().decode(data));
  }

  public static String generateJwtToken(String issuer, String subject, long issuedAt, String audience, long expiration, String key) throws JsonProcessingException, NoSuchAlgorithmException, InvalidKeyException {
    ObjectMapper mapper = createObjectMapperInstance();

    JwtHeader jwtHeader = new JwtHeader();
    JwtPayload jwtPayload = new JwtPayload(issuer, subject, issuedAt, audience, expiration);

    String header = base64UrlEncode(mapper.writeValueAsString(jwtHeader));
    String payload = base64UrlEncode(mapper.writeValueAsString(jwtPayload));
    String signature = base64UrlEncode(hmacSHA512(header + "." + payload, key));

    return header + "." + payload + "." + signature;
  }

  public static String convertInstantIntoString(Instant date) {
    DateTimeFormatter dtf =
        DateTimeFormatter
            .ofPattern("dd/MM/yyyy'T'HH:mm:ss.SSS'Z'")
            .withZone(ZoneId.of("UTC"));

    return dtf.format(date);
  }

  @Getter
  @ToString
  public static final class TestValidationError {
    private String field;
    @JsonInclude private Object rejectedValue;
    private List<String> validationMessages;
  }

  @Getter
  @ToString
  public static final class TestAddressDTO {
    private UUID publicId;
    private String address;
    private String createdAt;
    private String updatedAt;
  }

  @Getter
  @ToString
  public static final class JwtHeader {
    private final String alg = "HS512";
  }

  @RequiredArgsConstructor
  @Getter
  @ToString
  public static final class JwtPayload {
    private final String iss;
    private final String sub;
    private final long iat;
    private final String aud;
    private final long exp;
  }
}
