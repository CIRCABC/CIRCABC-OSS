// NOSONAR: Package name uses camelCase for legacy compatibility.
// Renaming would break existing imports and configurations.
package com.sii.eucaptcha.captcha.text.textRender; // NOSONAR

import java.awt.image.BufferedImage;

public interface WordRenderer {
  /**
   * Render a word to a BufferedImage.
   *
   * @param word The sequence of characters to be rendered.
   * @param image The image onto which the word will be rendered.
   */
  void render(String word, BufferedImage image);
}
