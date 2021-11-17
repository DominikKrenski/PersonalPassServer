package org.dominik.pass.errors.exceptions;

import lombok.ToString;

@ToString(callSuper = true)
public class InternalException extends BaseException {
  public InternalException(String message) {
    super(message);
  }
}
