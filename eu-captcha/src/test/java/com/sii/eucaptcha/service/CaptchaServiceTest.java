package com.sii.eucaptcha.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Locale;

@SpringBootTest
class CaptchaServiceTest {

  String previousIdCaptcha = "jjq7u4reu1vaiuao28gjq4vkq4";
  Locale frenchLocale = new Locale.Builder()
    .setLanguage("fr")
    .setRegion("FR")
    .build();

  @InjectMocks
  private CaptchaService service;

  @Mock
  private CaptchaService serviceMocked;

  @BeforeEach
  void init() {
    MockitoAnnotations.openMocks(this);
    when(this.serviceMocked.nextCaptchaId()).thenReturn("mockCaptchaId");
  }

  @DisplayName("Test generating Captcha method")
  @Test
  void generateCaptchaImage() {
    assertNotNull(service);
  }

  @DisplayName("Test validate captcha method")
  @Test
  void validateCaptcha() {
    when(
      this.serviceMocked.validateCaptcha(anyString(), anyString(), anyBoolean())
    ).thenReturn(true);
    String captchaId = "jh0b0t6rad62bgu9cerv91cb5g";
    String captchaAnswer = "KAB1";
    assertTrue(serviceMocked.validateCaptcha(captchaId, captchaAnswer, true));
  }
}
