package org.dominik.pass.utils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
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
import org.dominik.pass.data.dto.UpdateDataDTO;
import org.dominik.pass.data.dto.UpdatePasswordDTO;
import org.dominik.pass.data.enums.DataType;
import org.dominik.pass.data.enums.Role;
import org.dominik.pass.db.entities.Account;
import org.dominik.pass.db.entities.Data;
import org.dominik.pass.db.entities.Key;
import org.dominik.pass.db.entities.RefreshToken;
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
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TestUtils {
  public static UpdateDataDTO createUpdateDataDtoInstance(UUID publicId, String entry, DataType type, Instant createdAt, Instant updatedAt) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
    Class<?> clazz = UpdateDataDTO.class;
    Constructor<?> constructor = clazz.getDeclaredConstructor();
    constructor.setAccessible(true);

    UpdateDataDTO dto = (UpdateDataDTO) constructor.newInstance();

    List<Field> fields = Arrays.asList(clazz.getDeclaredFields());

    fields.forEach(field -> field.setAccessible(true));

    fields.forEach(field -> {
      String fieldname = field.getName();

      try {
        switch (fieldname) {
          case "publicId" -> field.set(dto, publicId);
          case "entry" -> field.set(dto, entry);
          case "type" -> field.set(dto, type);
          case "createdAt" -> field.set(dto, createdAt);
          case "updatedAt" -> field.set(dto, updatedAt);
        }
      } catch (IllegalAccessException ex) {
        ex.printStackTrace();
      }
    });

    return dto;
  }

  public static UpdatePasswordDTO createUpdatePasswordDtoInstance(String password, String salt) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
    Class<?> clazz = UpdatePasswordDTO.class;
    Constructor<?> constructor = clazz.getDeclaredConstructor();
    constructor.setAccessible(true);

    UpdatePasswordDTO dto = (UpdatePasswordDTO) constructor.newInstance();

    List<Field> fields = Arrays.asList(clazz.getDeclaredFields());

    fields.forEach(field -> field.setAccessible(true));

    fields.forEach(field -> {
      String fieldname = field.getName();

      try {
        switch (fieldname) {
          case "password" -> field.set(dto, password);
          case "salt" -> field.set(dto, salt);
          case "data" -> field.set(dto, new ArrayList<>());
        }
      } catch (IllegalAccessException ex) {
        ex.printStackTrace();
      }
    });

    return dto;
  }

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

  public static Key createKeyInstance(
    Long id,
    String key,
    Account account,
    Instant createdAt,
    Instant updatedAt,
    short version
  ) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
    Class<?> clazz = Key.class;
    Class<?> superClazz = clazz.getSuperclass();

    Constructor<?> constructor = clazz.getDeclaredConstructor();
    constructor.setAccessible(true);

    Key instance = (Key) constructor.newInstance();

    List<Field> fields = Arrays.asList(clazz.getDeclaredFields());
    List<Field> superFields = Arrays.asList(superClazz.getDeclaredFields());

    fields.forEach(field -> field.setAccessible(true));
    superFields.forEach(field -> field.setAccessible(true));

    superFields.forEach(field -> {
      String fieldname = field.getName();

      try {
        switch (fieldname) {
          case "createdAt" -> field.set(instance, createdAt);
          case "updatedAt" -> field.set(instance, updatedAt);
          case "version" -> field.set(instance, version);
        }
      } catch (IllegalAccessException ex) {
        ex.printStackTrace();
      }
    });

    fields.forEach(field -> {
      String fieldname = field.getName();

      try {
        switch (fieldname) {
          case "id" -> field.set(instance, id);
          case "key" -> field.set(instance, key);
          case "account" -> field.set(instance, account);
        }
      } catch (IllegalAccessException ex) {
        ex.printStackTrace();
      }
    });

    return instance;
  }

  public static Data createDataInstance(
    Long id,
    UUID publicId,
    String entry,
    DataType type,
    Account account,
    Instant createdAt,
    Instant updatedAt,
    short version
  ) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
    Class<?> clazz = Data.class;
    Class<?> superClazz = clazz.getSuperclass();

    Constructor<?> constructor = clazz.getDeclaredConstructor();
    constructor.setAccessible(true);

    Data data = (Data) constructor.newInstance();

    List<Field> fields = Arrays.asList(clazz.getDeclaredFields());
    List<Field> superFields = Arrays.asList(superClazz.getDeclaredFields());

    fields.forEach(field -> field.setAccessible(true));
    superFields.forEach(field -> field.setAccessible(true));

    superFields.forEach(field -> {
      String fieldname = field.getName();

      try {
        switch (fieldname) {
          case "createdAt" -> field.set(data, createdAt);
          case "updatedAt" -> field.set(data, updatedAt);
          case "version" -> field.set(data, version);
        }
      } catch (IllegalAccessException ex) {
        ex.printStackTrace();
      }
    });

    fields.forEach(field -> {
      String fieldname = field.getName();

      try {
        switch (fieldname) {
          case "id" -> field.set(data, id);
          case "publicId" -> field.set(data, publicId);
          case "entry" -> field.set(data, entry);
          case "type" -> field.set(data, type);
          case "account" -> field.set(data, account);
        }
      } catch (IllegalAccessException ex) {
        ex.printStackTrace();
      }
    });

    return data;
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

  public static <T> Map<String, List<String>> convertConstraintViolationsIntoMap(Set<ConstraintViolation<T>> violations) {
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

  public static Instant convertStringToInstant(String timestamp) {
    var dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy'T'HH:mm:ss.SSS'Z'").withZone(ZoneId.of("UTC"));
    TemporalAccessor accessor = dtf.parse(timestamp);
    return Instant.from(accessor);
  }

  public static List<Account> prepareAccountList() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    List<Account> accounts = new LinkedList<>();
    accounts.add(
      createAccountInstance(
        1L,
        UUID.fromString("cee0fa30-d170-4d9c-af8a-93ab159e9532"),
        "dominik.krenski@gmail.com",
        "$2a$12$1rCLWvFfj1lcHm2lP1MJ/OyTNFseGh.mVdAGinD1gaOjjftBToa22",
        "711882a4dc3dcb437eb6151c09025594",
        "taka sobie prosta wiadomość",
        Role.ROLE_USER,
        true,
        true,
        true,
        true,
        Instant.now().minusSeconds(4000),
        Instant.now().minusSeconds(3000),
        (short) 0
      )
    );

    accounts.add(
      createAccountInstance(
        2L,
        UUID.fromString("e455b70f-50c5-4a96-9386-58f6ab9ba24b"),
        "dorciad@interia.pl",
        "$2a$12$1rCLWvFfj1lcHm2lP1MJ/OyTNFseGh.mVdAGinD1gaOzzftBToa22",
        "711882a4dc3dcb437eb6151c01225594",
        null,
        Role.ROLE_USER,
        true,
        true,
        true,
        true,
        Instant.now().minusSeconds(1500),
        Instant.now().minusSeconds(1500),
        (short) 0
      )
    );

    accounts.add(
      createAccountInstance(
        3L,
        UUID.fromString("f01048b2-622a-49b6-963e-5e8edeec8026"),
        "dominik@yahoo.com",
        "$2a$12$1rCLEvFfj1lcHm2lP1NJ/OyTNFseFh.mVdAGinD1gaOzzftBToa38",
        "745882a4dc3dcd437ebef51c11225594",
        "przykładowa przypominajka",
        Role.ROLE_USER,
        true,
        true,
        true,
        true,
        Instant.now().minusSeconds(5000),
        Instant.now().minusSeconds(4000),
        (short) 0
      )
    );

    return accounts;
  }

  public static List<Data> prepareDataList(List<Account> accounts) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    List<Data> data = new LinkedList<>();

    data.add(
      createDataInstance(
        1L,
        UUID.fromString("84ab5b68-2fa4-44eb-bd49-c5ab44eac6cd"),
        "entry_1",
        DataType.ADDRESS,
        accounts.get(0),
        Instant.now().minusSeconds(1000),
        Instant.now().minusSeconds(1000),
        (short) 0
      )
    );

    data.add(
      createDataInstance(
        2L,
        UUID.fromString("ec28a035-a31b-461d-9fcd-70c9982c1a22"),
        "entry_2",
        DataType.ADDRESS,
        accounts.get(0),
        Instant.now().minusSeconds(5000),
        Instant.now().minusSeconds(2000),
        (short) 0
      )
    );

    data.add(
      createDataInstance(
        3L,
        UUID.fromString("9f569e10-64e1-4493-99e0-6a988a232e6b"),
        "entry_3",
        DataType.PASSWORD,
        accounts.get(0),
        Instant.now().minusSeconds(3000),
        Instant.now().minusSeconds(1200),
        (short) 0
      )
    );

    data.add(
      createDataInstance(
        4L,
        UUID.fromString("9bfc99d8-8bf3-45e4-b8ee-4c286408ac29"),
        "entry_4",
        DataType.PASSWORD,
        accounts.get(0),
        Instant.now().minusSeconds(3400),
        Instant.now().minusSeconds(400),
        (short) 0
      )
    );

    data.add(
      createDataInstance(
        5L,
        UUID.fromString("05618eec-dc25-4c24-b908-4fce6cb04ad4"),
        "entry_5",
        DataType.SITE,
        accounts.get(0),
        Instant.now().minusSeconds(2000),
        Instant.now().minusSeconds(1200),
        (short) 0
      )
    );

    data.add(
      createDataInstance(
        6L,
        UUID.fromString("3299f2fe-f930-44b6-8b10-c23c2efe5d1f"),
        "entry_6",
        DataType.SITE,
        accounts.get(0),
        Instant.now().minusSeconds(300),
        Instant.now().minusSeconds(300),
        (short) 0
      )
    );

    data.add(
      createDataInstance(
        7L,
        UUID.fromString("67f9c86f-36eb-4fab-94ac-f68d113ad9d7"),
        "entry_7",
        DataType.NOTE,
        accounts.get(0),
        Instant.now().minusSeconds(3000),
        Instant.now().minusSeconds(1290),
        (short) 0
      )
    );

    data.add(
      createDataInstance(
        8L,
        UUID.fromString("67a09bd7-5d26-4532-9758-9be4fa5a58c6"),
        "entry_8",
        DataType.NOTE,
        accounts.get(0),
        Instant.now().minusSeconds(2300),
        Instant.now().minusSeconds(1234),
        (short) 0
      )
    );

    data.add(
      createDataInstance(
        9L,
        UUID.fromString("c4abe5cf-b30f-4a0e-8da1-6cdee6cad35a"),
        "entry_9",
        DataType.ADDRESS,
        accounts.get(1),
        Instant.now().minusSeconds(2100),
        Instant.now().minusSeconds(1234),
        (short) 0
      )
    );

    data.add(
      createDataInstance(
        10L,
        UUID.fromString("13b2b31d-aa13-4cab-82dd-c979f6e8d1fe"),
        "entry_10",
        DataType.ADDRESS,
        accounts.get(1),
        Instant.now().minusSeconds(2300),
        Instant.now().minusSeconds(1234),
        (short) 0
      )
    );

    data.add(
      createDataInstance(
        11L,
        UUID.fromString("f469ec9c-5041-4a1f-b840-69f3ae66c1ac"),
        "entry_11",
        DataType.PASSWORD,
        accounts.get(1),
        Instant.now().minusSeconds(1234),
        Instant.now().minusSeconds(1234),
        (short) 0
      )
    );

    data.add(
      createDataInstance(
        12L,
        UUID.fromString("c3a175d3-cb93-4418-a480-eaee21505a49"),
        "entry_12",
        DataType.SITE,
        accounts.get(1),
        Instant.now().minusSeconds(3000),
        Instant.now().minusSeconds(2000),
        (short) 0
      )
    );

    data.add(
      createDataInstance(
        13L,
        UUID.fromString("d088d359-c608-4d76-bf2d-60e0ec0f8fb7"),
        "entry_13",
        DataType.SITE,
        accounts.get(1),
        Instant.now().minusSeconds(5000),
        Instant.now().minusSeconds(4300),
        (short) 0
      )
    );

    data.add(
      createDataInstance(
        14L,
        UUID.fromString("c87b861c-eff2-4d59-aa0c-232c5d7ee181"),
        "entry_14",
        DataType.NOTE,
        accounts.get(1),
        Instant.now().minusSeconds(2345),
        Instant.now().minusSeconds(4567),
        (short) 0
      )
    );

    data.add(
      createDataInstance(
        15L,
        UUID.fromString("f4ed5604-667e-4395-9461-59b9a356b431"),
        "entry_15",
        DataType.NOTE,
        accounts.get(1),
        Instant.now().minusSeconds(8000),
        Instant.now().minusSeconds(6000),
        (short) 0
      )
    );

    return data;
  }

  @Getter
  @ToString
  public static final class TestValidationError {
    private String field;
    @JsonInclude
    private Object rejectedValue;
    private List<String> validationMessages;
  }

  @Getter
  @ToString
  public static final class TestDataDTO {
    private UUID publicId;
    private DataType type;
    private String entry;
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
