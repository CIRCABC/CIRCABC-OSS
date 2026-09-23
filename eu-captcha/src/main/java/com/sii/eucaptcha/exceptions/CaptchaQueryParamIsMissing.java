package com.sii.eucaptcha.exceptions;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CaptchaQueryParamIsMissing extends NullPointerException {

  private final String exceptionMessage;

  private final List<String> missingParameters;

  public CaptchaQueryParamIsMissing() {
    super();
    this.exceptionMessage =
      "the captcha query does not have a parameter or more for the given captcha type";
    this.missingParameters = Collections.emptyList();
  }

  public CaptchaQueryParamIsMissing(
    String captchaType,
    String... missingParameters
  ) {
    this.missingParameters = Arrays.stream(missingParameters).toList();

    this.exceptionMessage =
      "the captcha query does not have one or more of folowing parameters " +
      this.missingParameters.toString() +
      " for the captchaType = " +
      captchaType;
  }

  @Override
  public String getMessage() {
    return exceptionMessage;
  }
}
