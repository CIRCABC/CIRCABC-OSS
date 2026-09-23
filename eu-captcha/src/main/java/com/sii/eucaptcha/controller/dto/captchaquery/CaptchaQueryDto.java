package com.sii.eucaptcha.controller.dto.captchaquery;

import com.sii.eucaptcha.controller.constants.CaptchaConstants;
import com.sii.eucaptcha.exceptions.CaptchaQueryParamIsMissing;
import java.util.Locale;
import org.apache.commons.lang3.StringUtils;

public class CaptchaQueryDto {

  private Locale locale;
  private Integer captchaLength;
  private String previousCaptchaId;
  private String captchaType;
  private Integer degree;
  private boolean capitalized;

  public CaptchaQueryDto(CaptchaQueryDtoBuilder captchaQueryDtoBuilder) {
    this.locale = captchaQueryDtoBuilder.locale;
    this.captchaLength = captchaQueryDtoBuilder.captchaLength;
    this.previousCaptchaId = captchaQueryDtoBuilder.previousCaptchaId;
    this.captchaType = captchaQueryDtoBuilder.captchaType;
    this.degree = captchaQueryDtoBuilder.degree;
    this.capitalized = captchaQueryDtoBuilder.capitalized;
  }

  public Locale getLocale() {
    return locale;
  }

  public Integer getCaptchaLength() {
    return captchaLength;
  }

  public String getPreviousCaptchaId() {
    return previousCaptchaId;
  }

  public String getCaptchaType() {
    return captchaType;
  }

  public Integer getDegree() {
    return degree;
  }

  public boolean isCapitalized() {
    return capitalized;
  }

  @Override
  public String toString() {
    return (
      "CaptchaQueryDto{" +
      "locale=" +
      locale +
      ", captchaLength=" +
      captchaLength +
      ", previousCaptchaId='" +
      previousCaptchaId +
      '\'' +
      ", captchaType='" +
      captchaType +
      '\'' +
      '}'
    );
  }

  public static class CaptchaQueryDtoBuilder {

    private Locale locale;
    private Integer captchaLength;
    private String previousCaptchaId;
    private String captchaType;
    private Integer degree;
    private boolean capitalized;

    public CaptchaQueryDtoBuilder(String captchaType) {
      if (captchaType == null) this.captchaType = CaptchaConstants.STANDARD;
      else this.captchaType = captchaType;
    }

    public CaptchaQueryDtoBuilder locale(String locale) {
      if (StringUtils.isNotBlank(locale)) {
        this.locale = createLocale(locale);
      } else {
        this.locale = Locale.ENGLISH;
      }
      return this;
    }

    public CaptchaQueryDtoBuilder captchaLength(Integer captchaLength) {
      if (captchaLength == null || captchaLength < 0) this.captchaLength =
        CaptchaConstants.DEFAULT_CAPTCHA_LENGTH;
      else this.captchaLength = captchaLength;
      return this;
    }

    public CaptchaQueryDtoBuilder previousCaptchaId(String previousCaptchaId) {
      this.previousCaptchaId = previousCaptchaId;
      return this;
    }

    public CaptchaQueryDtoBuilder degree(Integer degree) {
      if (degree == null) this.degree = CaptchaConstants.DEFAULT_DEGREE;
      else this.degree = degree;
      return this;
    }

    public CaptchaQueryDtoBuilder capitalized(boolean capitalized) {
      this.capitalized = capitalized;
      return this;
    }

    public CaptchaQueryDto build() {
      CaptchaQueryDto captchaQueryDto = new CaptchaQueryDto(this);
      validateCaptchaQueryDtoObject(captchaQueryDto);
      return captchaQueryDto;
    }

    protected void validateCaptchaQueryDtoObject(
      CaptchaQueryDto captchaQueryDto
    ) {
      if (
        CaptchaConstants.STANDARD.equalsIgnoreCase(
          captchaQueryDto.getCaptchaType()
        ) &&
        (captchaQueryDto.getLocale() == null ||
          captchaQueryDto.captchaLength == null)
      ) {
        throw new CaptchaQueryParamIsMissing(
          captchaQueryDto.getCaptchaType(),
          "locale",
          "captchaLenghth"
        );
      } else if (
        CaptchaConstants.WHATS_UP.equalsIgnoreCase(
          captchaQueryDto.getCaptchaType()
        ) &&
        captchaQueryDto.getDegree() == null
      ) {
        throw new CaptchaQueryParamIsMissing(
          captchaQueryDto.getCaptchaType(),
          "degree"
        );
      }
    }

    private Locale createLocale(String locale) {
      String[] localeStrings = locale.split("-");
      return new Locale.Builder()
        .setLanguage(localeStrings[0])
        .setRegion(localeStrings[1])
        .build();
    }
  }
}
