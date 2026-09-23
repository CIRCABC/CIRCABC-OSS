package com.sii.eucaptcha.captcha.text.image.noise;

import java.awt.image.BufferedImage;

public interface ImageNoiseProducer {
  void makeNoise(BufferedImage image);
}
