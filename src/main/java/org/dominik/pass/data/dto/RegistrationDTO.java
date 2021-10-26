package org.dominik.pass.data.dto;

import lombok.Getter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

@Getter
@ToString
public final class RegistrationDTO implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  private String email;
  private String password;
  private String salt;
  private String reminder;
}
