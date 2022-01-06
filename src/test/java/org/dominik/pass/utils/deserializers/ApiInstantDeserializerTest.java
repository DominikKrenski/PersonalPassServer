package org.dominik.pass.utils.deserializers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

import static org.dominik.pass.utils.TestUtils.createObjectMapperInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ApiInstantDeserializerTest {
  private static ObjectMapper mapper;

  @BeforeAll
  static void setUp() {
    mapper = createObjectMapperInstance();
  }

  @Test
  @DisplayName("should deserialize object")
  void shouldDeserializeObject() throws JsonProcessingException {
    String json = """
      {
        "timestamp": "05/08/1984T15:23:10.456Z"
      }
      """;

    Data data = mapper.readValue(json, Data.class);
    Instant instant = convertStringToInstant("05/08/1984T15:23:10.456Z");

    assertEquals(data.getTimestamp(), instant);
  }

  @Test
  @DisplayName("should throw JsonMappingException if timestamp has wrong format")
  void shouldThrowJsonMappingExceptionIfTimestampHasWrongFormat() {
    String json = """
      {
        "timestamp": "05/08/1984T15:23:10.456"
      }
      """;

    assertThrows(JsonMappingException.class, () -> mapper.readValue(json, Data.class));
  }

  private static Instant convertStringToInstant(String text) {
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy'T'HH:mm:ss.SSS'Z'").withZone(ZoneId.of("UTC"));
    TemporalAccessor temporalAccessor = dtf.parse(text);
    return Instant.from(temporalAccessor);
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  private static final class Data {
    @JsonDeserialize(using = ApiInstantDeserializer.class)
    private Instant timestamp;
  }
}
