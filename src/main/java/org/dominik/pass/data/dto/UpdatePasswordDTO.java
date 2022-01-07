package org.dominik.pass.data.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.dominik.pass.utils.validators.Hex;
import org.hibernate.validator.constraints.Length;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;

@NoArgsConstructor
@Getter
@ToString
public final class UpdatePasswordDTO {

  @NotBlank(message = "{password.blank.message}")
  @Length(min = 64, max = 64, message = "{password.length.message}")
  @Hex(message = "{password.hex.message}")
  private String password;

  @NotBlank(message = "{salt.blank.message}")
  @Length(min = 32, max = 32, message = "{salt.length.message}")
  @Hex(message = "{salt.hex.message}")
  private String salt;

  @Length(max = 255, message = "{reminder.length.message}")
  private String reminder;

  @Valid
  private List<UpdateDataDTO> data;
}
