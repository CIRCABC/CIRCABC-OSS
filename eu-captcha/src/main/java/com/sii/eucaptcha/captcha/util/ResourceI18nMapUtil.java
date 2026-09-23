package com.sii.eucaptcha.captcha.util;

import java.util.*;
import org.apache.commons.lang3.StringUtils;

public class ResourceI18nMapUtil {

  public final Map<String, String> voiceMap(Locale locale) {
    Map<String, String> voicesMap = new HashMap<>();
    ResourceBundle labels = ResourceBundle.getBundle("messages", locale);
    Enumeration<String> bundleKeys = labels.getKeys();
    while (bundleKeys.hasMoreElements()) {
      String key = bundleKeys.nextElement();
      String value = labels.getString(key);
      if (
        key.length() == 1 &&
        StringUtils.isNotBlank(value) &&
        value.contains(locale.getLanguage())
      ) {
        voicesMap.put(key, value);
      }
    }
    return voicesMap;
  }
}
