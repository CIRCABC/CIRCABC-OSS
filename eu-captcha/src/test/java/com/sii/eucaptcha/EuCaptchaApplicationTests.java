package com.sii.eucaptcha;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EuCaptchaApplicationTests {

  @Test
  void contextLoads() {}

  @DisplayName("Test Spring @Autowired Integration")
  @Test
  void testGet() {
    String stringToTest = "Hello JUnit 5";
    assertEquals("Hello JUnit 5", stringToTest);
  }
}
