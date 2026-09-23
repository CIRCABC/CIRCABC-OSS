package com.sii.eucaptcha.service.whatsup;

import java.awt.image.BufferedImage;
import org.springframework.core.io.Resource;

public interface CaptchaWhatsUpImagesService {
  Resource loadRandomImage();
  BufferedImage rotate(BufferedImage bimg, double angle);
}
