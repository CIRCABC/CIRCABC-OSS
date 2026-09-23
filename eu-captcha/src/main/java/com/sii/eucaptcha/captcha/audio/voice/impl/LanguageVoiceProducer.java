package com.sii.eucaptcha.captcha.audio.voice.impl;

import com.sii.eucaptcha.captcha.audio.Sample;
import com.sii.eucaptcha.captcha.audio.voice.VoiceProducer;
import com.sii.eucaptcha.captcha.util.FileUtil;
import java.util.Map;

/**
 * @author mousab.aidoud
 * @version 1.0
 * Language voice producer
 */
public class LanguageVoiceProducer implements VoiceProducer {

  private final Map<String, String> voices;

  /**
   * @param voices the map with the audiofiles for the chosen locale
   */
  public LanguageVoiceProducer(Map<String, String> voices) {
    this.voices = voices;
  }

  /**
   * Listing of the audio files for each characters
   *
   * @param num the char to be converted to text
   * @return audio file for each char
   */
  @Override
  public Sample getVocalization(char num) {
    String str = String.valueOf(num);

    String filename = voices.get(str);
    return new Sample(FileUtil.readResource(filename));
  }
}
