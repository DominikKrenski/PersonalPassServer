package org.dominik.pass.utils.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class ApiInstantSerializer extends JsonSerializer<Instant> {
  private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy'T'HH:mm:ss.SSS'Z'").withZone(ZoneId.of("UTC"));

  @Override
  public void serialize(Instant instant, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
    String date = dtf.format(instant);
    jsonGenerator.writeString(date);
  }
}
