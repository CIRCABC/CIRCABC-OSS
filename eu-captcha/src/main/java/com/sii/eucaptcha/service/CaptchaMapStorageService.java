package com.sii.eucaptcha.service;

import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import net.jodah.expiringmap.ExpiringMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service("mapStorage")
@Slf4j
public class CaptchaMapStorageService implements CaptchaStorage {

  /**
   * Building a map with Expiration Time CAPTCHA_EXPIRY_TIME
   */
  @Value("${expiry.time.in.seconds}")
  private long captchaExpiryTime;

  private Map<String, String> captchaCodeMap;

  @PostConstruct
  public void init() {
    captchaCodeMap = ExpiringMap.builder()
      .expiration(captchaExpiryTime, TimeUnit.SECONDS)
      .build();
  }

  /**
   * Verify the Captcha based on the CaptchaID stored on the CaptchaCode Map
   *
   * @param captchaId     the ID of the Captcha
   * @param captchaAnswer the users answer on the Captcha
   * @return Boolean of the verification
   */
  @Override
  public boolean validateCaptcha(
    String captchaId,
    String captchaAnswer,
    boolean usingAudio
  ) {
    boolean result = false;

    if (captchaCodeMap.containsKey(captchaId)) {
      log.debug(
        "Given answer is {}, stored answer is {}",
        captchaAnswer,
        captchaCodeMap.get(captchaId)
      );
      // case sensitive
      String answer = StringUtils.deleteWhitespace(
        captchaCodeMap.get(captchaId)
      );
      if (answer == null) {
        result = false;
      } else if (!usingAudio) {
        result = answer.equals(captchaAnswer);
      }
      // if the audio is selected , ignore case sensitive
      else {
        result = answer.equalsIgnoreCase(captchaAnswer);
      }
    }
    removeCaptcha(captchaId);
    return result;
  }

  @Override
  public boolean validateWhatsUpCaptcha(
    String captchaId,
    String captchaAnswer
  ) {
    if (!captchaCodeMap.containsKey(captchaId)) {
      removeCaptcha(captchaId);
      return false;
    }
    String storedAnswer = captchaCodeMap.get(captchaId);
    removeCaptcha(captchaId);
    int storedAnswerAsInt = Integer.parseInt(storedAnswer);
    int givenAnswer = Integer.parseInt(captchaAnswer);

    log.debug(
      "stored answer = , givenAnswer = " + storedAnswerAsInt,
      givenAnswer
    );

    return (
      (givenAnswer == (storedAnswerAsInt * -1)) ||
      ((givenAnswer <= 0)
          ? ((givenAnswer * -1 - 360) == storedAnswerAsInt)
          : ((360 - givenAnswer) == storedAnswerAsInt))
    );
  }

  /**
   * Adding the Captcha ID and the answer the the MAP
   *
   * @param captchaId     the ID of the Captcha
   * @param captchaAnswer contains combination of key value
   *                      Captcha ID => Captcha answer
   */

  @Override
  public void addCaptcha(String captchaId, String captchaAnswer) {
    captchaCodeMap.putIfAbsent(captchaId, captchaAnswer);
  }

  /**
   * removing the Captcha ID and it and answer
   *
   * @param captchaId the ID of the Captcha
   */
  @Override
  public void removeCaptcha(String captchaId) {
    captchaCodeMap.remove(captchaId);
  }
}
