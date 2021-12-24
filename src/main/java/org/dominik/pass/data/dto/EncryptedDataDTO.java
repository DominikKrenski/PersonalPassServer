package org.dominik.pass.data.dto;

import lombok.Getter;
import lombok.ToString;
import org.dominik.pass.data.enums.DataType;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Getter
@ToString
public final class EncryptedDataDTO {
  @NotNull(message = "{data.null.message}")
  private DataType type;

  @NotBlank(message = "{data.blank.message}")
  @Pattern(regexp = "^[a-fA-F0-9]{24}\\.[a-fA-F0-9]+$", message = "{data.pattern.message}")
  private String entry;
}
