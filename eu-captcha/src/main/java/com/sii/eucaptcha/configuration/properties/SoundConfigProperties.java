package com.sii.eucaptcha.configuration.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:sound.properties")
@ConfigurationProperties(prefix = "sound.noises")
public class SoundConfigProperties {

  @Value("${sound.noises.default}")
  private String[] defaultNoises;

  @Value("${sound.noises.samplevolume}")
  private Double sampleVolume;

  @Value("${sound.noises.noisevolume}")
  private Double noiseVolume;

  @Value("${sound.noises.silence.audio}")
  private String silenceAudio;

  public String[] getDefaultNoises() {
    return defaultNoises;
  }

  public void setDefaultNoises(String[] defaultNoises) {
    this.defaultNoises = defaultNoises;
  }

  public Double getSampleVolume() {
    return sampleVolume;
  }

  public void setSampleVolume(Double sampleVolume) {
    this.sampleVolume = sampleVolume;
  }

  public Double getNoiseVolume() {
    return noiseVolume;
  }

  public void setNoiseVolume(Double noiseVolume) {
    this.noiseVolume = noiseVolume;
  }

  public String getSilenceAudio() {
    return silenceAudio;
  }

  public void setSilenceAudio(String silenceAudio) {
    this.silenceAudio = silenceAudio;
  }
}
