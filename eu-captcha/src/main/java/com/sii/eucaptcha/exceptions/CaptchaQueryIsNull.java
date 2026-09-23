package com.sii.eucaptcha.exceptions;

public class CaptchaQueryIsNull extends NullPointerException {

  private static final String EXCEPTION_MESSAGE =
    "CaptchaQueryIdNull or missing";

  @Override
  public String getMessage() {
    return EXCEPTION_MESSAGE + super.getMessage();
  }
}
