package com.sii.eucaptcha.captcha.text.image.noise.impl;

import com.sii.eucaptcha.captcha.text.image.noise.ImageNoiseProducer;
import com.sii.eucaptcha.security.CaptchaRandom;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.security.SecureRandom;

public class StraightLineImageNoiseProducer implements ImageNoiseProducer {

  private static final SecureRandom RAND = CaptchaRandom.getSecureInstance();
  private final Color color;
  private final int thickness;

  public StraightLineImageNoiseProducer(Color color, int thickness) {
    this.color = color;
    this.thickness = thickness;
  }

  @Override
  public void makeNoise(BufferedImage image) {
    Graphics2D graphics = image.createGraphics();
    int height = image.getHeight();
    int width = image.getWidth();
    int y1 = RAND.nextInt(height) + 1;
    int y2 = RAND.nextInt(height) + 1;
    drawLine(graphics, y1, width, y2);
  }

  private void drawLine(Graphics g, int y1, int x2, int y2) {
    int x1 = 0;

    // The thick line is in fact a filled polygon
    g.setColor(color);
    int dX = x2 - x1;
    int dY = y2 - y1;
    // line length
    double lineLength = Math.sqrt((double) dX * dX + (double) dY * dY);

    double scale = thickness / (2 * lineLength);

    // The x and y increments from an endpoint needed to create a
    // rectangle...
    double ddx = -scale * dY;
    double ddy = scale * dX;
    ddx += (ddx > 0) ? 0.5 : -0.5;
    ddy += (ddy > 0) ? 0.5 : -0.5;
    int dx = (int) ddx;
    int dy = (int) ddy;

    // Now we can compute the corner points...
    int[] xPoints = new int[4];
    int[] yPoints = new int[4];

    xPoints[0] = x1 + dx;
    yPoints[0] = y1 + dy;
    xPoints[1] = x1 - dx;
    yPoints[1] = y1 - dy;
    xPoints[2] = x2 - dx;
    yPoints[2] = y2 - dy;
    xPoints[3] = x2 + dx;
    yPoints[3] = y2 + dy;

    g.fillPolygon(xPoints, yPoints, 4);
  }
}
