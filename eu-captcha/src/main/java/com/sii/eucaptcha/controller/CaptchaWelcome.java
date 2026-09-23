package com.sii.eucaptcha.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author mousab.aidoud
 * @version 1.0
 * Get the captcha welcome page .
 */
@Controller
public class CaptchaWelcome {

  @Value("${controller.captcha.welcomePage}")
  private String welcomePageName;

  /**
   *
   * @return JSP FILE FOR THE DEMO
   * (     server:port/euCaptcha    )
   */

  @Value("${controller.captcha.rotationCaptcha}")
  private String rotationCaptchaPageName;

  /**
   *
   * @return JSP FILE FOR THE DEMO
   * (     server:port/euCaptcha    )
   */

  @GetMapping("/")
  public String welcomePage() {
    return welcomePageName;
  }

  @GetMapping("/rotate")
  public String rotateCaptchaPage() {
    return rotationCaptchaPageName;
  }
}
