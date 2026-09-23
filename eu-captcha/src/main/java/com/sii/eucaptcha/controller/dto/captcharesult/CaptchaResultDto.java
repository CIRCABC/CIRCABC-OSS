package com.sii.eucaptcha.controller.dto.captcharesult;

import com.sii.eucaptcha.controller.constants.CaptchaConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;

public class CaptchaResultDto implements Serializable {

  @Schema(
    description = "Generated ID of the Captcha",
    example = "mj0kvg8s39sufq9uj8cs5ckorj"
  )
  private String captchaId;

  @Schema(
    description = "The CaptchaImage",
    example = "iVBORw0KGgoAAAANSUhEUgAAAZAAAADICAIAAABJdyC//..."
  )
  private String captchaImg;

  @Schema(
    description = "Type of the Captcha",
    example = "STANDARD for textual captcha or WHATS_UP for rotated image captcha"
  )
  private String captchaType = CaptchaConstants.STANDARD;

  public CaptchaResultDto() {
    super();
  }

  public String getCaptchaId() {
    return captchaId;
  }

  public void setCaptchaId(String captchaId) {
    this.captchaId = captchaId;
  }

  public String getCaptchaImg() {
    return captchaImg;
  }

  public void setCaptchaImg(String captchaImg) {
    this.captchaImg = captchaImg;
  }

  public String getCaptchaType() {
    return captchaType;
  }

  public void setCaptchaType(String captchaType) {
    this.captchaType = captchaType;
  }
}
