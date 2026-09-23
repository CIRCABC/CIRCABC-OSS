package com.sii.eucaptcha.captcha.audio;

import com.sii.eucaptcha.captcha.exception.AudioProcessingException;
import com.sii.eucaptcha.captcha.util.FileUtil;
import com.sii.eucaptcha.configuration.properties.SoundConfigProperties;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

public class Sample {

  public static final AudioFormat SC_AUDIO_FORMAT = new AudioFormat(
    16000, 
    16, 
    1, 
    true, 
    false 
  ); 

  private final AudioInputStream audioInputStream;

  private AudioInputStream silenceAudioInputStream;

  private SoundConfigProperties props;

  private boolean withSilenceInteruption = true;

  public Sample(
    AudioInputStream audioInputStream,
    boolean withSilenceInteruption,
    SoundConfigProperties props
  ) {
    this.withSilenceInteruption = withSilenceInteruption;
    this.props = props;

    if (withSilenceInteruption) {
      InputStream silenceIs = FileUtil.readResource(props.getSilenceAudio());
      if (silenceIs instanceof AudioInputStream audioIs) {
        silenceAudioInputStream = audioIs;
      } else {
        try {
          silenceAudioInputStream = AudioSystem.getAudioInputStream(silenceIs);
        } catch (UnsupportedAudioFileException | IOException e) {
          // NOSONAR: Logging exception - silence audio is optional, continue without it
          java.util.logging.Logger.getLogger(Sample.class.getName()).log(
            java.util.logging.Level.WARNING,
            "Failed to load silence audio",
            e
          );
        }
      }

      AudioInputStream appendedFiles = new AudioInputStream(
        new SequenceInputStream(audioInputStream, silenceAudioInputStream),
        audioInputStream.getFormat(),
        audioInputStream.getFrameLength() +
        silenceAudioInputStream.getFrameLength()
      );
      this.audioInputStream = appendedFiles;
    } else this.audioInputStream = audioInputStream;

    checkFormat(audioInputStream.getFormat());
  }

  public Sample(InputStream is) {
    if (is instanceof AudioInputStream audioIs) {
      audioInputStream = audioIs;
    } else {
      try {
        audioInputStream = AudioSystem.getAudioInputStream(is);
      } catch (UnsupportedAudioFileException e) {
        throw new AudioProcessingException("Unsupported audio file format", e);
      } catch (IOException e) {
        throw new AudioProcessingException("Failed to read audio stream", e);
      }
    }
    checkFormat(audioInputStream.getFormat());
  }

  public AudioInputStream getAudioInputStream() {
    return audioInputStream;
  }

  public AudioFormat getFormat() {
    return audioInputStream.getFormat();
  }

  /**
   * Return the number of samples of all channels
   *
   * @return The number of samples for all channels
   */
  public long getSampleCount() {
    long total =
      (audioInputStream.getFrameLength() * getFormat().getFrameSize() * 8) /
      getFormat().getSampleSizeInBits();
    return total / getFormat().getChannels();
  }

  public double[] getInterleavedSamples() {
    double[] samples = new double[(int) getSampleCount()];
    try {
      getInterleavedSamples(0, getSampleCount(), samples);
    } catch (IllegalArgumentException | IOException e) {
      // NOSONAR: Logging exception - return partial/empty samples array
      java.util.logging.Logger.getLogger(Sample.class.getName()).log(
        java.util.logging.Level.WARNING,
        "Failed to get interleaved samples",
        e
      );
    }

    return samples;
  }

  /**
   * Get the interleaved decoded samples for all channels, from sample index
   * <code>begin</code> (included) to sample index <code>end</code> (excluded)
   * and copy them into <code>samples</code>. <code>end</code> must not exceed
   * <code>getSampleCount()</code>, and the number of samples must not be so
   * large that the associated byte array cannot be allocated
   *
   * @param begin
   * @param end
   * @param samples
   * @throws IOException
   * @throws IllegalArgumentException
   */
  public double[] getInterleavedSamples(long begin, long end, double[] samples)
    throws IOException, IllegalArgumentException {
    long nbSamples = end - begin;
    long nbBytes =
      nbSamples *
      (getFormat().getSampleSizeInBits() / 8) *
      getFormat().getChannels();
    if (nbBytes > Integer.MAX_VALUE) {
      throw new IllegalArgumentException(
        "Too many samples. Try using a smaller wav."
      );
    }
    // allocate a byte buffer
    byte[] inBuffer = new byte[(int) nbBytes];
    // read bytes from audio file
    audioInputStream.read(inBuffer, 0, inBuffer.length);
    // decode bytes into samples.
    decodeBytes(inBuffer, samples);

    return samples;
  }

  // Decode bytes of audioBytes into audioSamples
  public void decodeBytes(byte[] audioBytes, double[] audioSamples) {
    int sampleSizeInBytes = getFormat().getSampleSizeInBits() / 8;
    int[] sampleBytes = new int[sampleSizeInBytes];
    int k = 0; // index in audioBytes
    for (int i = 0; i < audioSamples.length; i++) {
      // collect sample byte in big-endian order
      if (getFormat().isBigEndian()) {
        // bytes start with MSB
        for (int j = 0; j < sampleSizeInBytes; j++) {
          sampleBytes[j] = audioBytes[k++];
        }
      } else {
        // bytes start with LSB
        for (int j = sampleSizeInBytes - 1; j >= 0; j--) {
          sampleBytes[j] = audioBytes[k++];
        }
      }
      // get integer value from bytes
      int ival = 0;
      for (int j = 0; j < sampleSizeInBytes; j++) {
        ival += sampleBytes[j];
        if (j < sampleSizeInBytes - 1) ival <<= 8;
      }
      // decode value
      double ratio = Math.pow(2., (double) getFormat().getSampleSizeInBits() - 1);
      double val = ival / ratio;
      audioSamples[i] = val;
    }
  }

  /**
   * Helper method to convert a double[] to a byte[] in a format that can be
   * used by {@link AudioInputStream}. Typically this will be used with
   * a {@link Sample} that has been modified from its original.
   *
   * @see <a href="http://en.wiktionary.org/wiki/yak_shaving">Yak Shaving</a>
   *
   * @return A byte[] representing a sample
   */
  public static byte[] asByteArray(long sampleCount, double[] sample) {
    int bufferLength =
      Math.toIntExact(sampleCount) *
      (SC_AUDIO_FORMAT.getSampleSizeInBits() / 8);
    byte[] buffer = new byte[bufferLength];

    int intValue;
    for (int i = 0; i < sample.length; i++) {
      intValue = (int) (sample[i] * 32767);
      buffer[2 * i] = (byte) (intValue & 255);
      buffer[2 * i + 1] = (byte) (intValue >> 8);
    }

    return buffer;
  }

  @Override
  public String toString() {
    return "[Sample] samples: " + getSampleCount() + ", format: " + getFormat();
  }

  private static void checkFormat(AudioFormat af) {
    if (!af.matches(SC_AUDIO_FORMAT)) {
      throw new IllegalArgumentException(
        "Unsupported audio format.\nReceived: " +
        af.toString() +
        "\nExpected: " +
        SC_AUDIO_FORMAT
      );
    }
  }

  public boolean isWithSilenceInteruption() {
    return withSilenceInteruption;
  }

  public void setWithSilenceInteruption(boolean withSilenceInteruption) {
    this.withSilenceInteruption = withSilenceInteruption;
  }

  public SoundConfigProperties getProps() {
    return props;
  }

  public void setProps(SoundConfigProperties props) {
    this.props = props;
  }
}
