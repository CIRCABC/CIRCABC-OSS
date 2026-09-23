package com.sii.eucaptcha.captcha.audio.noise;

import com.sii.eucaptcha.captcha.audio.Sample;
import java.util.List;

public interface NoiseProducer {
  /**
   * Handling the volume of the captcha audio and the volume of the noises
   * @param noiseSamples the audio files to generate the spoken Captcha
   * @return audio mixed with noise
   */
  Sample produceNoise(List<Sample> noiseSamples);
}
