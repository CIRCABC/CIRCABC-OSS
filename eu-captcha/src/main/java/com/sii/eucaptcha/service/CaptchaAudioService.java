package com.sii.eucaptcha.service;

import com.sii.eucaptcha.captcha.audio.Mixer;
import com.sii.eucaptcha.captcha.audio.Sample;
import com.sii.eucaptcha.captcha.audio.noise.NoiseProducer;
import com.sii.eucaptcha.captcha.audio.voice.VoiceProducer;
import com.sii.eucaptcha.configuration.properties.SoundConfigProperties;
import com.sii.eucaptcha.security.CaptchaRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author mousab.aidoud
 * @version 1.0
 * Captcha Audio class
 */
public class CaptchaAudioService {

  private static final Random SECURE_RANDOM = CaptchaRandom.getSecureInstance();

  private final String answer;
  private final Sample challenge;

  private CaptchaAudioService(CaptchaAudioServiceBuilder captchaAudioBuilder) {
    this.answer = captchaAudioBuilder.answer;
    this.challenge = captchaAudioBuilder.challenge;
  }

  /**
   * @return challenge
   */
  public Sample getChallenge() {
    return challenge;
  }

  /**
   * @return builder to string
   */
  @Override
  public String toString() {
    return "[Answer: " + answer + "]";
  }

  public static class CaptchaAudioServiceBuilder {

    private String answer = "";
    private Sample challenge;
    private final List<VoiceProducer> voiceProducers;
    private final List<NoiseProducer> noiseProducers;
    private SoundConfigProperties soundConfigProperties;

    public CaptchaAudioServiceBuilder() {
      voiceProducers = new ArrayList<>();
      noiseProducers = new ArrayList<>();
    }

    public CaptchaAudioServiceBuilder withAnswer(String answer) {
      this.answer = answer;
      return this;
    }

    public CaptchaAudioServiceBuilder withVoice(VoiceProducer voiceProducer) {
      voiceProducers.add(voiceProducer);
      return this;
    }

    public CaptchaAudioServiceBuilder withNoise(NoiseProducer noiseProducer) {
      noiseProducers.add(noiseProducer);
      return this;
    }

    public CaptchaAudioServiceBuilder withSoundConfigProperties(SoundConfigProperties soundConfigProperties) {
      this.soundConfigProperties = soundConfigProperties;
      return this;
    }

    /**
     * Build the audio
     *
     * @return captcha audio service builder
     */
    public CaptchaAudioService build() {
      // Convert answer to an array
      char[] answerArray = answer.toCharArray();

      // Make a List of Samples for each character
      List<Sample> samples = new ArrayList<>();
      for (char c : answerArray) {
        if (!Character.isWhitespace(c)) {
          // Create Sample for this character from one of the
          // VoiceProducers
          VoiceProducer voiceProducer = voiceProducers.get(
            SECURE_RANDOM.nextInt(voiceProducers.size())
          );
          Sample sample = voiceProducer.getVocalization(c);

          Sample sampleWithInterruption = new Sample(
            sample.getAudioInputStream(),
            true,
            soundConfigProperties
          );

          samples.add(sampleWithInterruption);
        }
      }
      // 3. Add noise, if any, and return the result
      if (!noiseProducers.isEmpty()) {
        NoiseProducer nProd = noiseProducers.get(
          SECURE_RANDOM.nextInt(noiseProducers.size())
        );
        challenge = nProd.produceNoise(samples);
        return new CaptchaAudioService(this);
      }
      challenge = Mixer.append(samples);
      return new CaptchaAudioService(this);
    }
  }

  public static CaptchaAudioServiceBuilder newBuilder() {
    return new CaptchaAudioServiceBuilder();
  }
}
