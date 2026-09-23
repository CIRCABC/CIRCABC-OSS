package io.swagger.model;

import java.util.HashMap;
import java.util.Objects;

/**
 * The object that is used to compile all the translations of a node into a JSON object. It is
 * basically composed of a map with a key languaguage code and its value.
 *
 * <p>Each entry maps a language code (e.g. {@code "en"}, {@code "fr"}) to the corresponding
 * translated text, allowing a single property to carry all of its localized variants.
 */
public class I18nProperty extends HashMap<String, String> {

  /** Serialization version identifier for this {@link HashMap} subtype. */
  private static final long serialVersionUID = -3027818284745604594L;

  /** Creates an empty translation map with no language entries. */
  public I18nProperty() {
    super();
  }

  /**
   * Creates a translation map pre-populated with a single language entry.
   *
   * @param language the language code used as the map key (e.g. {@code "en"})
   * @param value the translated text associated with the given language
   */
  public I18nProperty(String language, String value) {
    super();
    super.put(language, value);
  }

  /**
   * Indicates whether the given object is equal to this translation map.
   *
   * @param o the object to compare with this instance
   * @return {@code true} if the given object is considered equal to this instance, {@code false}
   *     otherwise
   */
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    return Objects.equals(this, o);
  }

  /**
   * Returns a hash code value for this translation map.
   *
   * @return the hash code derived from the underlying map contents
   */
  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode());
  }

  /**
   * Returns a human-readable, indented string representation of this translation map.
   *
   * @return a formatted string describing this instance
   */
  @Override
  public String toString() {
    return (
      "class I18nProperty {\n" +
      "    " +
      toIndentedString(super.toString()) +
      "\n" +
      "}"
    );
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first line).
   *
   * @param o the object whose string representation should be indented
   * @return the indented string representation of the given object
   */
  private String toIndentedString(java.lang.Object o) {
    return Util.toIndentedString(o);
  }

  /**
   * Determines the best available translation to use when no specific language is requested.
   *
   * <p>The resolution order is: the only entry when the map contains a single translation, then the
   * English ({@code "en"}) value if present and non-empty, then the first non-empty value found.
   *
   * @return the resolved default translation, or an empty string if no non-empty value exists
   */
  public String getDefaultValue() {
    if (this.size() == 1) {
      return this.values().iterator().next();
    }

    String en = this.get("en");
    if (en != null && !en.isEmpty()) {
      return en;
    }

    for (String value : this.values()) {
      if (!value.isEmpty()) {
        return value;
      }
    }

    return "";
  }
}
