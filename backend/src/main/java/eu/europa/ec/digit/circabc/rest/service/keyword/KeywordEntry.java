/**
 *
 */
package eu.europa.ec.digit.circabc.rest.service.keyword;

/**
 * Represents a single localized keyword entry, pairing a language code with its keyword value.
 *
 * <p>Used in the manage keyword dialog during the keyword export and import phase, where keywords
 * are exchanged as language/value pairs.
 *
 * @author beaurpi
 */
public class KeywordEntry {

  /** ISO language code identifying the locale this keyword value applies to. */
  private String language;

  /** The keyword text for the associated {@link #language}. */
  private String value;

  /** Creates an empty keyword entry with no language or value set. */
  public KeywordEntry() {}

  /**
   * Creates a keyword entry for the given language and value.
   *
   * @param language the ISO language code for this entry
   * @param value the keyword value in the given language
   */
  public KeywordEntry(String language, String value) {
    this.language = language;
    this.value = value;
  }

  /**
   * Returns a string representation combining language and value, separated by a colon (e.g.
   * {@code "en:report"}).
   *
   * @return the {@code language:value} representation of this entry
   */
  @Override
  public String toString() {
    return language + ":" + value;
  }

  /** @return the language */
  public String getLanguage() {
    return language;
  }

  /** @param language the language to set */
  public void setLanguage(String language) {
    this.language = language;
  }

  /** @return the value */
  public String getValue() {
    return value;
  }

  /** @param value the value to set */
  public void setValue(String value) {
    this.value = value;
  }
}
