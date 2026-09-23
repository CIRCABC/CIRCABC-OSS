// NOSONAR: Package name uses camelCase for legacy compatibility.
// Renaming would break existing imports and configurations.
package com.sii.eucaptcha.captcha.text.textProducer.impl; // NOSONAR

import com.sii.eucaptcha.captcha.text.textProducer.TextProducer;
import com.sii.eucaptcha.security.CaptchaRandom;
import java.util.Random;
import java.util.Set;

public class DefaultTextProducer implements TextProducer {

  private static final Random RANDOM = CaptchaRandom.getSecureInstance();

  private final int length;
  private final Set<String> localizedCharacters;

  public DefaultTextProducer(int length, Set<String> localizedCharacters) {
    this.length = length;
    this.localizedCharacters = localizedCharacters;
  }

  @Override
  public String getText() {
    StringBuilder capText = new StringBuilder();
    for (int i = 0; i < length; i++) {
      capText.append(getRandomSetElement());
      capText.append(" ");
    }
    return capText.toString();
  }

  public String getRandomSetElement() {
    return localizedCharacters
      .stream()
      .skip(RANDOM.nextInt(localizedCharacters.size()))
      .findFirst()
      .orElse(null);
  }
}
