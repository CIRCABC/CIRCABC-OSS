package com.sii.eucaptcha.captcha.text.image.background.impl;

import com.sii.eucaptcha.captcha.text.image.background.BackgroundProducer;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class GradiatedBackgroundProducer implements BackgroundProducer {

  private final Color fromColor;
  private final Color toColor;

  public GradiatedBackgroundProducer(Color from, Color to) {
    fromColor = from;
    toColor = to;
  }

  @Override
  public BufferedImage getBackground(int width, int height) {
    // create an opaque image
    BufferedImage img = new BufferedImage(
      width,
      height,
      BufferedImage.TYPE_INT_RGB
    );

    Graphics2D g = img.createGraphics();
    RenderingHints hints = new RenderingHints(
      RenderingHints.KEY_ANTIALIASING,
      RenderingHints.VALUE_ANTIALIAS_ON
    );

    g.setRenderingHints(hints);

    // create the gradient color
    GradientPaint ytow = new GradientPaint(
      0,
      0,
      fromColor,
      width,
      height,
      toColor
    );

    g.setPaint(ytow);
    // draw gradient color
    g.fill(new Rectangle2D.Double(0, 0, width, height));

    // draw the transparent image over the background
    g.drawImage(img, 0, 0, null);
    g.dispose();

    return img;
  }

  @Override
  public BufferedImage addBackground(BufferedImage image) {
    int width = image.getWidth();
    int height = image.getHeight();

    return getBackground(width, height);
  }
}
