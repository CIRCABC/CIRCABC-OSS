package com.sii.eucaptcha.controller.dto.captcharesult;

public class TextualCaptchaResultDtoDto extends CaptchaResultDto {

  private String audioCaptcha;

  public TextualCaptchaResultDtoDto() {
    super();
  }

  public String getAudioCaptcha() {
    return audioCaptcha;
  }

  public void setAudioCaptcha(String audioCaptcha) {
    this.audioCaptcha = audioCaptcha;
  }
}
