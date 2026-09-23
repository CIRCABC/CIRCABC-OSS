package com.sii.eucaptcha.captcha.text.image.background;

import java.awt.image.BufferedImage;

public interface BackgroundProducer {
  /**
   * Add the background to the given image.
   *
   * @param image The image onto which the background will be rendered.
   * @return The image with the background rendered.
   */
  BufferedImage addBackground(BufferedImage image);

  BufferedImage getBackground(int width, int height);
}
