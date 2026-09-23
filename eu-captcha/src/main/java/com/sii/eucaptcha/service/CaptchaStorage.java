package com.sii.eucaptcha.service;

public interface CaptchaStorage {
  boolean validateCaptcha(
    String captchaId,
    String captchaAnswer,
    boolean usingAudio
  );

  boolean validateWhatsUpCaptcha(String captchaId, String captchaAnswer);

  void addCaptcha(String captchaId, String captchaAnswer);

  void removeCaptcha(String captchaId);
}
