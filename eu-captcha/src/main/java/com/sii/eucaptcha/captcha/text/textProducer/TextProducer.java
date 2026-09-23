// NOSONAR: Package name uses camelCase for legacy compatibility.
// Renaming would break existing imports and configurations.
package com.sii.eucaptcha.captcha.text.textProducer; // NOSONAR

public interface TextProducer {
  /**
   * Generate a series of characters to be used as the answer for the CAPTCHA.
   *
   * @return The answer for the CAPTCHA.
   */
  String getText();
}
