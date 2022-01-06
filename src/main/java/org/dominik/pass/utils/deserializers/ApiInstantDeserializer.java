package org.dominik.pass.utils.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

public final class ApiInstantDeserializer extends StdDeserializer<Instant> {
  private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy'T'HH:mm:ss.SSS'Z'").withZone(ZoneId.of("UTC"));

  public ApiInstantDeserializer() {
    this(null);
  }

  public ApiInstantDeserializer(Class<?> clazz) {
    super(clazz);
  }

  @Override
  public Instant deserialize(JsonParser parser, DeserializationContext ctx) throws IOException, JsonProcessingException {
    String timestamp = parser.getText();

    TemporalAccessor temporalAccessor = dtf.parse(timestamp);
    return Instant.from(temporalAccessor);
  }
}
