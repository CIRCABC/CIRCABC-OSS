package com.sii.eucaptcha.exceptions;

public class CaptchaDataIsNull extends NullPointerException {

  private static final String EXCEPTION_MESSAGE =
    "CaptchaDataIsNull or missing";

  @Override
  public String getMessage() {
    return EXCEPTION_MESSAGE + super.getMessage();
  }
}
