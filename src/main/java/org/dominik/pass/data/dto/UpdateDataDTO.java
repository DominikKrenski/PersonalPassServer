package org.dominik.pass.data.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.ToString;
import org.dominik.pass.data.enums.DataType;
import org.dominik.pass.utils.deserializers.ApiInstantDeserializer;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.Instant;
import java.util.UUID;

@Getter
@ToString
public final class UpdateDataDTO {
  @NotNull(message="{public_id.null.message}")
  private UUID publicId;

  @NotBlank(message = "data.blank.message")
  @Pattern(
    regexp = "^[a-fA-F0-9]{24}\\.[a-fA-F0-9]+$",
    message = "{data.pattern.message}"
  )
  private String entry;

  @NotNull(message = "{data.null.message}")
  private DataType type;

  @JsonDeserialize(using = ApiInstantDeserializer.class)
  @NotNull(message = "{timestamp.null.message}")
  private Instant createdAt;

  @JsonDeserialize(using = ApiInstantDeserializer.class)
  @NotNull(message = "{timestamp.null.message}")
  private Instant updatedAt;
}
