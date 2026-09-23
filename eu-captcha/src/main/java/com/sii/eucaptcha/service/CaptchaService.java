package com.sii.eucaptcha.service;

import com.sii.eucaptcha.captcha.Captcha;
import com.sii.eucaptcha.captcha.audio.Sample;
import com.sii.eucaptcha.captcha.audio.noise.impl.EuCaptchaNoiseProducer;
import com.sii.eucaptcha.captcha.audio.voice.VoiceProducer;
import com.sii.eucaptcha.captcha.audio.voice.impl.LanguageVoiceProducer;
import com.sii.eucaptcha.captcha.text.image.background.impl.GradiatedBackgroundProducer;
import com.sii.eucaptcha.captcha.text.image.gimpy.impl.EuCaptchaGimpyRenderer;
import com.sii.eucaptcha.captcha.text.image.noise.impl.StraightLineImageNoiseProducer;
import com.sii.eucaptcha.captcha.text.textProducer.TextProducer;
import com.sii.eucaptcha.captcha.text.textProducer.impl.DefaultTextProducer;
import com.sii.eucaptcha.captcha.text.textRender.impl.CaptchaTextRender;
import com.sii.eucaptcha.captcha.util.ResourceI18nMapUtil;
import com.sii.eucaptcha.configuration.properties.SoundConfigProperties;
import com.sii.eucaptcha.controller.constants.CaptchaConstants;
import com.sii.eucaptcha.controller.dto.captchaquery.CaptchaQueryDto;
import com.sii.eucaptcha.controller.dto.captcharesult.*;
import com.sii.eucaptcha.exceptions.CaptchaQueryIsNull;
import com.sii.eucaptcha.exceptions.WrongCaptchaRotationDegree;
import com.sii.eucaptcha.security.CaptchaRandom;
import com.sii.eucaptcha.service.whatsup.CaptchaWhatsUpImagesService;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

/**
 * @author mousab.aidoud
 * @version 1.0
 *          Captcha Service Class
 */
@Service
@Slf4j
public class CaptchaService {

  /**
   * Parameters of Captcha {WIDTH , HEIGHT , EXPIRY TIME }
   */
  private static final int CAPTCHA_WIDTH = 400;
  private static final int CAPTCHA_HEIGHT = 200;

  /**
   * List of colors and background colors
   */
  private static final List<Color> COLORS = new ArrayList<>(3);
  private static final List<Color> COLOR_STRAIGHT_LINE_NOISE = new ArrayList<>(
    3
  );
  private static final List<Color> BACKGROUND_COLORS = new ArrayList<>(5);

  /**
   * List of fonts and font sizes
   */
  private static final Font FONTS_SERIF = new Font("Serif", Font.BOLD, 50);
  private static final Font FONTS_SANS_SERIF = new Font(
    "SansSerif",
    Font.BOLD,
    50
  );

  static {
    COLORS.add(Color.BLACK);
    COLORS.add(Color.GRAY);
    COLORS.add(Color.DARK_GRAY);

    BACKGROUND_COLORS.add(Color.PINK);
    BACKGROUND_COLORS.add(Color.ORANGE);
    BACKGROUND_COLORS.add(Color.LIGHT_GRAY);
    BACKGROUND_COLORS.add(Color.WHITE);
    BACKGROUND_COLORS.add(Color.CYAN);

    COLOR_STRAIGHT_LINE_NOISE.add(Color.RED);
    COLOR_STRAIGHT_LINE_NOISE.add(Color.ORANGE);
    COLOR_STRAIGHT_LINE_NOISE.add(Color.MAGENTA);
  }

  @Autowired
  SoundConfigProperties props;

  @Autowired
  ResourceLoader resourceLoader;

  @Autowired
  CaptchaWhatsUpImagesService captchaWhatsUpImagesService;

  @Autowired
  @Qualifier("mapStorage") // use mapStorage if you want to use just one node
  CaptchaStorage captchaStorage;

  private final SecureRandom random = CaptchaRandom.getSecureInstance();

  /**
   * @return Captcha ID
   */
  public String nextCaptchaId() {
    return new BigInteger(130, random).toString(32);
  }

  /**
   * @param previousCaptchaId the ID of the Captcha
   * @param locale            the chosen locale
   * @return String [] which contains the CaptchaID , Captcha Image , and Captcha
   *         Audio.
   */
  public CaptchaResultDto generateCaptchaImage(
    String previousCaptchaId,
    Locale locale,
    Integer captchaLength,
    boolean capitalized
  ) {
    int extraWidth = (captchaLength != null &&
        captchaLength > CaptchaConstants.DEFAULT_CAPTCHA_LENGTH)
      ? (captchaLength - CaptchaConstants.DEFAULT_CAPTCHA_LENGTH) *
      CaptchaConstants.DEFAULT_UNIT_WIDTH
      : 0;

    int extraHeight = (captchaLength != null &&
        captchaLength > CaptchaConstants.DEFAULT_CAPTCHA_LENGTH)
      ? (captchaLength - CaptchaConstants.DEFAULT_CAPTCHA_LENGTH) *
      CaptchaConstants.DEFAULT_UNIT_HEIGHT
      : 0;

    log.debug("extraWidth = {} extraHeight = {} ", extraWidth, extraHeight);

    // Case Reload Captcha
    if (previousCaptchaId != null) {
      removeCaptcha(previousCaptchaId);
    }
    int captchaTextLength = (captchaLength != null)
      ? captchaLength
      : CaptchaConstants.DEFAULT_CAPTCHA_LENGTH;

    Map<String, String> localesMap = new ResourceI18nMapUtil().voiceMap(locale);

    // Generate the Captcha Text
    TextProducer textProducer = new DefaultTextProducer(
      captchaTextLength,
      localesMap.keySet()
    );

    // Generate the Captcha drawing
    CaptchaTextRender wordRenderer = new CaptchaTextRender(
      COLORS,
      FONTS_SANS_SERIF,
      FONTS_SERIF
    );

    // Build The Captcha
    Captcha captcha = Captcha.newBuilder()
      .withDimensions(CAPTCHA_WIDTH + extraWidth, CAPTCHA_HEIGHT + extraHeight)
      .withText(textProducer, wordRenderer, capitalized)
      .withBackground(
        new GradiatedBackgroundProducer(
          BACKGROUND_COLORS.get(random.nextInt(BACKGROUND_COLORS.size())),
          BACKGROUND_COLORS.get(random.nextInt(BACKGROUND_COLORS.size()))
        )
      )
      .withNoise(
        new StraightLineImageNoiseProducer(
          COLOR_STRAIGHT_LINE_NOISE.get(
            random.nextInt(COLOR_STRAIGHT_LINE_NOISE.size())
          ),
          7
        )
      )
      .gimp(new EuCaptchaGimpyRenderer())
      .withBorder()
      .build();

    VoiceProducer voiceProducer = new LanguageVoiceProducer(localesMap);

    Double sampleVolume;
    Double noiseVolume;
    if (locale.getLanguage().equals("bg")) {
      sampleVolume = props.getSampleVolume();
      noiseVolume = 0.0D;
    } else {
      sampleVolume = props.getSampleVolume();
      noiseVolume = props.getNoiseVolume();
    }

    // Build the captcha audio file.
    CaptchaAudioService captchaAudioService = CaptchaAudioService.newBuilder()
      .withAnswer(captcha.getAnswer())
      .withVoice(voiceProducer)
      .withSoundConfigProperties(props)
      .withNoise(
        new EuCaptchaNoiseProducer(
          props.getDefaultNoises(),
          sampleVolume,
          noiseVolume
        )
      )
      .build();
    BufferedImage buf = captcha.getImage();
    ByteArrayOutputStream bao = new ByteArrayOutputStream();

    String captchaPngImage = "";

    try {
      ImageIO.write(buf, "png", bao);
      bao.flush();
      byte[] imageBytes = bao.toByteArray();
      bao.close();
      captchaPngImage = new String(
        Base64.getEncoder().encode(imageBytes),
        StandardCharsets.UTF_8
      );
    } catch (Exception e) {
      log.error("Failed to encode captcha image to Base64", e);
    }

    InputStream in = captchaAudioService.getChallenge().getAudioInputStream();
    Sample sample = new Sample(in);
    String captchaAudioFile = "";
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
      AudioSystem.write(
        sample.getAudioInputStream(),
        AudioFileFormat.Type.WAVE,
        baos
      );
      byte[] audioBytes = baos.toByteArray();
      baos.close();

      captchaAudioFile = new String(
        Base64.getEncoder().encode(audioBytes),
        StandardCharsets.UTF_8
      );
    } catch (IOException e) {
      log.error("Failed to encode audio file to Base64", e);
    }

    String captchaId = this.nextCaptchaId();
    // Adding the Captcha image , the captcha ID , the captcha audio file to the
    // String []

    TextualCaptchaResultDtoDto captchaDataResult =
      new TextualCaptchaResultDtoDto();
    captchaDataResult.setCaptchaId(captchaId);
    captchaDataResult.setAudioCaptcha(captchaAudioFile);
    captchaDataResult.setCaptchaImg(captchaPngImage);
    captchaDataResult.setCaptchaType(CaptchaConstants.STANDARD);

    addCaptcha(captchaId, captcha.getAnswer());
    log.debug(
      "Generated Captcha with captchaId: {} and answer: {}",
      captchaId,
      captcha.getAnswer()
    );
    return captchaDataResult;
  }

  public boolean validateCaptcha(
    String captchaId,
    String captchaAnswer,
    String captchaType,
    boolean usingAudio
  ) {
    if (
      CaptchaConstants.WHATS_UP.equalsIgnoreCase(captchaType)
    ) return validateWhatsUpCaptcha(captchaId, captchaAnswer);
    else return validateCaptcha(captchaId, captchaAnswer, usingAudio);
  }

  /**
   * Generate Captcha Image Wrapper
   *
   * @param captchaQueryDto the captcha Query that carry the query parameters
   * @return response as String contains CaptchaID and Captcha Image
   */

  public CaptchaResultDto generateCaptchaWrapper(
    CaptchaQueryDto captchaQueryDto
  ) {
    if (captchaQueryDto == null) {
      throw new CaptchaQueryIsNull();
    }

    String previousCaptchaId = captchaQueryDto.getPreviousCaptchaId();
    if (
      captchaQueryDto.getCaptchaType() != null &&
      captchaQueryDto
        .getCaptchaType()
        .equalsIgnoreCase(CaptchaConstants.WHATS_UP)
    ) {
      return generateWhatsUpCaptchaImage(
        previousCaptchaId,
        captchaQueryDto.getDegree()
      );
    } else {
      return generateCaptchaImage(
        previousCaptchaId,
        captchaQueryDto.getLocale(),
        captchaQueryDto.getCaptchaLength(),
        captchaQueryDto.isCapitalized()
      );
    }
  }

  public CaptchaResultDto generateWhatsUpCaptchaImage(
    String previousCaptchaId,
    Integer degree
  ) {
    if (previousCaptchaId != null) {
      removeCaptcha(previousCaptchaId);
    }
    String captchaId = this.nextCaptchaId();
    // Adding the Captcha image , the captcha ID , the captcha audio file to the
    // String []

    Resource resource = captchaWhatsUpImagesService.loadRandomImage();
    if (degree == null) degree = CaptchaConstants.DEFAULT_DEGREE;

    int rotationAngle = degree;
    try {
      rotationAngle = CaptchaRandom.getRandomRotationAngle(degree);
    } catch (WrongCaptchaRotationDegree wrongCaptchaRotationDegree) {
      log.error("Invalid rotation degree: {}", degree, wrongCaptchaRotationDegree);
    }

    String captchaPngImage = "";
    WhatsUpCaptchaResultDtoDto captchaDataResult =
      new WhatsUpCaptchaResultDtoDto();
    try {
      InputStream is = resource.getInputStream();
      BufferedImage buffImg = ImageIO.read(is);

      BufferedImage rotatedImage = captchaWhatsUpImagesService.rotate(
        buffImg,
        rotationAngle
      );

      captchaPngImage = encodeImageToBase64(rotatedImage);
    } catch (IOException e) {
      log.error("Failed to read or rotate image resource", e);
    }

    captchaDataResult.setCaptchaType(CaptchaConstants.WHATS_UP);
    captchaDataResult.setCaptchaImg(captchaPngImage);
    captchaDataResult.setCaptchaId(captchaId);
    captchaDataResult.setDegree(degree);

    log.debug(
      "add to storage captchaId = {} answer = {} ",
      captchaId,
      rotationAngle
    );
    addCaptcha(captchaId, Integer.toString(rotationAngle));

    return captchaDataResult;
  }

  public boolean validateCaptcha(
    String captchaId,
    String captchaAnswer,
    boolean usingAudio
  ) {
    return captchaStorage.validateCaptcha(captchaId, captchaAnswer, usingAudio);
  }

  private String encodeImageToBase64(BufferedImage image) {
    try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
      ImageIO.write(image, "png", os);
      os.flush();
      return new String(
        Base64.getEncoder().encode(os.toByteArray()),
        StandardCharsets.UTF_8
      );
    } catch (IOException e) {
      log.error("Failed to encode image to Base64", e);
      return "";
    }
  }

  public boolean validateWhatsUpCaptcha(
    String captchaId,
    String captchaAnswer
  ) {
    return captchaStorage.validateWhatsUpCaptcha(captchaId, captchaAnswer);
  }

  public void addCaptcha(String captchaId, String captchaAnswer) {
    captchaStorage.addCaptcha(captchaId, captchaAnswer);
  }

  public void removeCaptcha(String captchaId) {
    captchaStorage.removeCaptcha(captchaId);
  }
}
