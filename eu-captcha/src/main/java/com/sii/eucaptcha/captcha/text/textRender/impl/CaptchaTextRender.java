// NOSONAR: Package name uses camelCase for legacy compatibility.
// Renaming would break existing imports and configurations.
package com.sii.eucaptcha.captcha.text.textRender.impl; // NOSONAR

import static java.lang.Character.isUpperCase;

import com.sii.eucaptcha.captcha.text.textRender.WordRenderer;
import com.sii.eucaptcha.security.CaptchaRandom;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.security.SecureRandom;
import java.util.*;
import java.util.List;

/**
 * @author mousab.aidoud
 * @version 1.0
 * Captcha Text drawing class
 */
public class CaptchaTextRender implements WordRenderer {

  /**
   * List of Color and font
   */
  private static final Random RANDOM = CaptchaRandom.getSecureInstance();

  private final List<Color> colors = new ArrayList<>();
  private final List<Font> fontsSansSerif = new ArrayList<>();
  private final List<Font> fontsSerif = new ArrayList<>();

  /**
   * Build a <code>WordRenderer</code> using the given <code>Color</code>s and
   * <code>Font</code>s.
   *
   * @param colors the colors to be used
   * @param fontsSansSerif the fonts to be used for small characters
   * @Param fontsSerif the fonts to be used for Capital characters
   */
  public CaptchaTextRender(
    List<Color> colors,
    Font fontsSansSerif,
    Font fontsSerif
  ) {
    this.colors.addAll(colors);
    this.fontsSansSerif.add(fontsSansSerif);
    this.fontsSerif.add(fontsSerif);
  }

  @Override
  public void render(String word, BufferedImage image) {
    Graphics2D g = image.createGraphics();

    RenderingHints hints = new RenderingHints(
      RenderingHints.KEY_ANTIALIASING,
      RenderingHints.VALUE_ANTIALIAS_ON
    );
    hints.add(
      new RenderingHints(
        RenderingHints.KEY_RENDERING,
        RenderingHints.VALUE_RENDER_QUALITY
      )
    );
    g.setRenderingHints(hints);

    FontRenderContext frc = g.getFontRenderContext();
    SecureRandom rand = CaptchaRandom.getSecureInstance();
    double lowerX = 0.3;
    double upperX = 0.2;
    double x = (rand.nextFloat() * (upperX - lowerX)) + upperX;

    double lowerY = 0.45;
    double upperY = 0.6;
    double y = (rand.nextFloat() * (upperY - lowerY)) + upperY;

    int xBaseline = (int) Math.round(image.getWidth() * x);
    int yBaseline = image.getHeight() - (int) Math.round(image.getHeight() * y);

    char[] chars = new char[1];

    for (char c : word.toCharArray()) {
      chars[0] = c;
      g.setColor(colors.get(RANDOM.nextInt(colors.size())));
      Font font;
      Map<TextAttribute, Object> textAttributes = new HashMap<>();
      textAttributes.put(TextAttribute.TRACKING, 0.5);
      textAttributes.put(TextAttribute.KERNING, TextAttribute.KERNING_ON);
      if (isUpperCase(c)) {
        int choiceFont = RANDOM.nextInt(fontsSerif.size());
        Font baseFont = fontsSerif.get(choiceFont);
        font = baseFont.deriveFont(textAttributes);
      } else {
        int choiceFont = RANDOM.nextInt(fontsSansSerif.size());
        Font baseFont = fontsSerif.get(choiceFont);
        font = baseFont.deriveFont(textAttributes);
      }
      Map<TextAttribute, Object> map = new HashMap<>();

      /* Kerning makes the text spacing more natural */
      map.put(TextAttribute.KERNING, TextAttribute.KERNING_ON);
      font = font.deriveFont(map);
      g.setFont(font);
      GlyphVector gv = font.createGlyphVector(frc, chars);

      AffineTransform affineTransform = new AffineTransform();
      affineTransform.rotate(Math.toRadians(15), 0, 0);
      g.setTransform(affineTransform);

      g.drawChars(chars, 0, chars.length, xBaseline, yBaseline);

      int width = (int) gv.getVisualBounds().getWidth();
      xBaseline = xBaseline + width;
    }
  }
}
