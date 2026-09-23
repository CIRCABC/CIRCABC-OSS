package com.sii.eucaptcha.captcha.audio.voice;

import com.sii.eucaptcha.captcha.audio.Sample;

public interface VoiceProducer {
  Sample getVocalization(char letter);
}
