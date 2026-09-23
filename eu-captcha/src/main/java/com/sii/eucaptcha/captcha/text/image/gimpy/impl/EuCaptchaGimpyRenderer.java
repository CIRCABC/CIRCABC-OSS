package com.sii.eucaptcha.captcha.text.image.gimpy.impl;

import com.sii.eucaptcha.captcha.text.image.gimpy.GimpyRenderer;
import com.sii.eucaptcha.security.CaptchaRandom;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.security.SecureRandom;

/**
 * @author mousab.aidoud
 * @version 1.0
 * GimpyRender Class
 */
public class EuCaptchaGimpyRenderer implements GimpyRenderer {

  private final Color borderColor;

  /**
   * Constructor
   */
  public EuCaptchaGimpyRenderer() {
    this.borderColor = Color.gray;
  }

  /**
   *
   * @param image the Captcha image
   */
  @Override
  public void gimp(BufferedImage image) {
    int height = image.getHeight();
    int width = image.getWidth();
    int hstripes = height / 10;
    int hspace = height / (hstripes + 1);
    Graphics2D graph = (Graphics2D) image.getGraphics();
    int i;
    for (i = hspace; i < height; i += hspace) {
      graph.setColor(this.borderColor);
      graph.drawLine(0, i, width, i);
    }
    int[] pix = new int[height * width];
    int j = 0;
    for (int j1 = 0; j1 < width; ++j1) {
      for (int k1 = 0; k1 < height; ++k1) {
        pix[j] = image.getRGB(j1, k1);
        ++j;
      }
    }

    double distance = this.ranInt(width / 4, width / 3);
    int wMid = image.getWidth() / 2;
    int hMid = image.getHeight() / 2;
    for (int x = 0; x < image.getWidth(); ++x) {
      for (int y = 0; y < image.getHeight(); ++y) {
        int relX = x - wMid;
        int relY = y - hMid;
        double d1 = Math.sqrt((double) relX * relX + (double) relY * relY);
        if (d1 < distance) {
          int j2 =
            wMid +
            (int) (((this.fishEyeFormula(d1 / distance) * distance) / d1) *
              (x - wMid));
          int k2 =
            hMid +
            (int) (((this.fishEyeFormula(d1 / distance) * distance) / d1) *
              (y - hMid));
          image.setRGB(x, y, pix[j2 * height + k2]);
        }
      }
    }
    graph.dispose();
  }

  /**
   *
   * @param i width to calculate randomized
   * @param j width to be randomized
   * @return the randomized int value
   */
  private int ranInt(int i, int j) {
    SecureRandom rand = CaptchaRandom.getSecureInstance();
    double d = rand.nextDouble();
    return (int) (i + (j - i + 1) * d);
  }

  /**
   *
   * @param s the starting value to randomize
   * @return the calculated random value
   */
  private double fishEyeFormula(double s) {
    if (s < 0.0D) {
      return 0.0D;
    } else {
      return s > 1.0D ? s : -0.75D * s * s * s + 1.5D * s * s + 0.25D * s;
    }
  }
}
