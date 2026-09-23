package com.sii.eucaptcha.captcha.text.image.background.impl;

import com.sii.eucaptcha.captcha.text.image.background.BackgroundProducer;
import java.awt.*;
import java.awt.image.BufferedImage;

public class TransparentBackgroundProducer implements BackgroundProducer {

  @Override
  public BufferedImage addBackground(BufferedImage image) {
    return getBackground(image.getWidth(), image.getHeight());
  }

  @Override
  public BufferedImage getBackground(int width, int height) {
    BufferedImage bg = new BufferedImage(
      width,
      height,
      Transparency.TRANSLUCENT
    );
    Graphics2D g = bg.createGraphics();

    g.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f));
    g.fillRect(0, 0, width, height);

    return bg;
  }
}
