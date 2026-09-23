package com.sii.eucaptcha.captcha;

import com.sii.eucaptcha.captcha.text.image.background.BackgroundProducer;
import com.sii.eucaptcha.captcha.text.image.background.impl.TransparentBackgroundProducer;
import com.sii.eucaptcha.captcha.text.image.gimpy.GimpyRenderer;
import com.sii.eucaptcha.captcha.text.image.noise.ImageNoiseProducer;
import com.sii.eucaptcha.captcha.text.textProducer.TextProducer;
import com.sii.eucaptcha.captcha.text.textRender.WordRenderer;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Date;

public class Captcha {

  private final String answer;
  private final BufferedImage image;
  private final Date timeStamp;

  private Captcha(CaptchaBuilder captchaBuilder) {
    this.answer = captchaBuilder.answer;
    this.image = captchaBuilder.image;
    this.timeStamp = captchaBuilder.timeStamp;
  }

  public String getAnswer() {
    return answer;
  }

  public BufferedImage getImage() {
    return image;
  }

  @Override
  public String toString() {
    return (
      "[Answer: " +
      answer +
      "][Timestamp: " +
      timeStamp +
      "][Image: " +
      image +
      "]"
    );
  }

  public static class CaptchaBuilder {

    private String answer;
    private BufferedImage image;
    private BufferedImage background;
    private Date timeStamp;
    private boolean border = false;

    public CaptchaBuilder withDimensions(int width, int height) {
      image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
      return this;
    }

    /**
     *
     * @param bgProd the chosen {@link BackgroundProducer}
     */
    public CaptchaBuilder withBackground(BackgroundProducer bgProd) {
      background = bgProd.getBackground(image.getWidth(), image.getHeight());
      return this;
    }

    /**
     * Generate the answer to the CAPTCHA using the given
     * {@link TextProducer}, and render it to the image using the given
     * {@link WordRenderer}.
     *
     * @param txtProd the chosen TextProducer
     * @param wRenderer the chosen WordRenderer
     */
    public CaptchaBuilder withText(
      TextProducer txtProd,
      WordRenderer wRenderer,
      boolean capitalized
    ) {
      if (capitalized) {
        answer = txtProd.getText();
      } else {
        answer = txtProd.getText().toLowerCase();
      }
      wRenderer.render(answer, image);
      return this;
    }

    /**
     * Add noise using the given NoiseProducer.
     *
     * @param nProd the chosen ImageNoiseProducer
     */
    public CaptchaBuilder withNoise(ImageNoiseProducer nProd) {
      nProd.makeNoise(image);
      return this;
    }

    /**
     * Gimp the image using the given {@link GimpyRenderer}.
     *
     * @param gimpy the chosen GimpyRenderer
     */
    public CaptchaBuilder gimp(GimpyRenderer gimpy) {
      gimpy.gimp(image);
      return this;
    }

    /**
     * Draw a single-pixel wide black border around the image.
     */
    public CaptchaBuilder withBorder() {
      border = true;
      return this;
    }

    /**
     * Build the CAPTCHA. This method should always be called, and should always
     * be called last.
     *
     * @return The constructed CAPTCHA.
     */
    public Captcha build() {
      if (background == null) {
        background = new TransparentBackgroundProducer().getBackground(
          image.getWidth(),
          image.getHeight()
        );
      }

      // Paint the main image over the background
      Graphics2D g = background.createGraphics();
      g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
      g.drawImage(image, null, null);

      if (border) {
        int width = image.getWidth();
        int height = image.getHeight();

        g.setColor(Color.BLACK);
        g.drawLine(0, 0, 0, width);
        g.drawLine(0, 0, width, 0);
        g.drawLine(0, height - 1, width, height - 1);
        g.drawLine(width - 1, height - 1, width - 1, 0);
      }
      image = background;
      timeStamp = new Date();
      return new Captcha(this);
    }
  }

  public static CaptchaBuilder newBuilder() {
    return new CaptchaBuilder();
  }
}
