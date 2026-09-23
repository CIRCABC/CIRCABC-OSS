package com.sii.eucaptcha.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(CaptchaController.class)
class CaptchaControllerTest {

  @Test
  void contextLoads() {
    assertTrue(true);
  }
}
