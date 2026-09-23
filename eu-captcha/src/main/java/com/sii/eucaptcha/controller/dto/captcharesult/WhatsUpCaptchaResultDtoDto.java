package com.sii.eucaptcha.controller.dto.captcharesult;

import com.sii.eucaptcha.controller.constants.CaptchaConstants;

public class WhatsUpCaptchaResultDtoDto extends CaptchaResultDto {

  private Integer degree;

  public WhatsUpCaptchaResultDtoDto() {
    super();
  }

  public int getDegree() {
    return degree;
  }

  public void setDegree(int degree) {
    this.degree = degree;
  }

  public void setDegree(Integer degree) {
    if (degree == null) this.degree = CaptchaConstants.DEFAULT_DEGREE;
    else this.degree = degree;
  }
}
