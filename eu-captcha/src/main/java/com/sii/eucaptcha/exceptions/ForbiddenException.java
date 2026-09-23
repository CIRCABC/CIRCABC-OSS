package com.sii.eucaptcha.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class ForbiddenException extends RuntimeException {

  private static final String EX_MESSAGE = "You can't get access";

  @Override
  public String getMessage() {
    return EX_MESSAGE;
  }
}
