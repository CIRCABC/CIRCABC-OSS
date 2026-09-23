package com.sii.eucaptcha.security;

import com.sii.eucaptcha.controller.constants.CaptchaConstants;
import com.sii.eucaptcha.exceptions.WrongCaptchaRotationDegree;
import java.security.SecureRandom;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CaptchaRandom {

  private static SecureRandom secureRandom;

  private CaptchaRandom() {}

  public static SecureRandom getSecureInstance() {
    if (secureRandom == null) {
      secureRandom = new SecureRandom();
    }
    return secureRandom;
  }

  public static int getRandomRotationAngle(int degree)
    throws WrongCaptchaRotationDegree {
    if (
      degree < CaptchaConstants.MIN_DEGREE ||
      degree > CaptchaConstants.MAX_DEGREE
    ) throw new WrongCaptchaRotationDegree();
    else {
      int randomRangeMax = 6;
      int randomRangeMin = 1;

      log.debug("min = {}", randomRangeMin);
      log.debug("max = {}", randomRangeMax);
      int randomNumber =
        getSecureInstance().nextInt(randomRangeMax) + randomRangeMin;
      log.debug("randomNumber = {}", randomNumber);
      log.debug("degree = {}", degree);

      return randomNumber * degree;
    }
  }
}
