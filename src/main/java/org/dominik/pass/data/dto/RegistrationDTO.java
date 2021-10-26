package org.dominik.pass.data.dto;

import lombok.Getter;
import lombok.ToString;
import org.dominik.pass.utils.validators.EmailAddress;
import org.dominik.pass.utils.validators.Hex;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import java.io.Serial;
import java.io.Serializable;

@Getter
@ToString
public final class RegistrationDTO implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  @NotBlank(message = "{email.blank.message}")
  @Length(max = 360, message = "{email.length.message}")
  @EmailAddress(message = "{email.format.message}")
  private String email;

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
}
