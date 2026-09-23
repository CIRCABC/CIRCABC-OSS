package io.swagger.util;

import java.util.List;

/**
 * Utility holder for the set of languages supported by CIRCABC.
 *
 * <p>This class centralises the list of ISO 639-1 language codes (matching the official languages
 * of the European Union) that the application recognises. It exposes them as an immutable list so
 * that other components can validate user-supplied locales or iterate over the supported languages
 * without duplicating the codes.
 *
 * <p>The class is not meant to be instantiated; all members are static.
 *
 * @author beaurpi
 */
public class SupportedLanguages {

  /** Two-letter ISO 639-1 codes of every language supported by CIRCABC. */
  private static final String[] LANG_CODES = {
    "bg",
    "cs",
    "da",
    "de",
    "el",
    "en",
    "es",
    "et",
    "fi",
    "fr",
    "ga",
    "hr",
    "it",
    "lv",
    "lt",
    "hu",
    "mt",
    "nl",
    "pl",
    "pt",
    "ro",
    "sk",
    "sl",
    "sv",
  };

  /**
   * Immutable list of the supported language codes, derived from {@link #LANG_CODES}.
   *
   * <p>Use this constant to validate or enumerate the languages available in the application.
   */
  public static final List<String> availableLangCodes = List.of(LANG_CODES);

  /**
   * Prevents instantiation of this utility class.
   *
   * @throws IllegalStateException always, since the class is not meant to be instantiated
   */
  private SupportedLanguages() {
    throw new IllegalStateException("Utility class");
  }
}
