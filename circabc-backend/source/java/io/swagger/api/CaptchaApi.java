package io.swagger.api;

public interface CaptchaApi {
  boolean validate(String captchaToken, String captchaId, String answer);
}
