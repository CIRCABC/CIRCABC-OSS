package com.sii.eucaptcha.controller.dto.captcharesult;

import com.sii.eucaptcha.controller.constants.CaptchaConstants;

public class CaptchaResultDtoFactory {

  public CaptchaResultDtoFactory() {
    super();
  }

  public CaptchaResultDto getCaptcha(String captchaType) {
    if (
      captchaType == null ||
      CaptchaConstants.STANDARD.equalsIgnoreCase(captchaType)
    ) {
      return new TextualCaptchaResultDtoDto();
    } else if (captchaType.equalsIgnoreCase(CaptchaConstants.WHATS_UP)) {
      return new WhatsUpCaptchaResultDtoDto();
    }
    return null;
  }
}
