/**
 * ***************************************************************************** Copyright 2006
 * European Community
 *
 * <p>Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 *
 * <p>https://joinup.ec.europa.eu/software/page/eupl
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * ****************************************************************************
 */
package eu.europa.ec.digit.circabc.rest.service.keyword;

import java.io.Serializable;
import java.util.Locale;
import java.util.Map;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Default implementation of the {@link Keyword} contract.
 *
 * <p>A keyword is a labelled tag that can be attached, as part of a collection, to a node in the
 * Alfresco repository. A keyword can be either:
 *
 * <ul>
 *   <li>monolingual, in which case its label is held as a single {@link String} in {@link #value};
 *       or
 *   <li>multilingual, in which case its labels are held per {@link Locale} in an {@link MLText}
 *       instance ({@link #mlValues}).
 * </ul>
 *
 * <p>At most one of {@link #value} and {@link #mlValues} is populated at any time: promoting a
 * keyword to multilingual (see {@link #makeMultilingual(Locale)}) moves the monolingual value into
 * the {@link MLText} map and clears {@link #value}. The optional {@link #id} references the
 * repository node backing the keyword, and {@link #selected} is a transient UI/selection flag.
 *
 * <p>Instances are {@link Serializable} so they can be carried across the web-script layer.
 *
 * @author Yanick Pignot
 */
public class KeywordImpl implements Keyword, Serializable {

  /**
   * Serialization version identifier for this {@link Serializable} type.
   */
  private static final long serialVersionUID = -6691678274689941982L;

  /**
   * The value of the keyword
   */
  private String value;

  /**
   * The ml values of the keyword
   */
  private MLText mlValues;

  /**
   * The id of the keyword, i.e. the {@link NodeRef} of the repository node backing it; may be
   * {@code null} for keywords not yet persisted.
   */
  private NodeRef id;

  /**
   * Transient flag indicating whether this keyword is currently selected (e.g. in a UI); may be
   * {@code null} when unset.
   */
  private Boolean selected;

  /**
   * Constructs a monolingual keyword bound to a repository node.
   *
   * @param id    the {@link NodeRef} of the node backing this keyword, may be {@code null}
   * @param value the (single, non-localized) value of the keyword
   */
  /*package*/ KeywordImpl(final NodeRef id, final String value) {
    this.id = id;
    this.value = value;
    mlValues = null;
  }

  /**
   * Constructs a keyword bound to a repository node, optionally seeded with a single localized
   * value.
   *
   * <p>When {@code locale} is {@code null} the keyword is created as monolingual holding
   * {@code value}; otherwise it is created as multilingual holding {@code value} for the given
   * {@code locale}.
   *
   * @param id     the {@link NodeRef} of the node backing this keyword, may be {@code null}
   * @param locale the keyword's locale, or {@code null} for a monolingual keyword
   * @param value  the value of the keyword
   */
  /*package*/ KeywordImpl(
    final NodeRef id,
    final Locale locale,
    final String value
  ) {
    this.id = id;
    if (locale == null) {
      this.value = value;
      this.mlValues = null;
    } else {
      this.mlValues = new MLText();
      this.mlValues.put(locale, value);
      this.value = null;
    }
  }

  /**
   * Constructs a multilingual keyword bound to a repository node from its localized values.
   *
   * @param id       the {@link NodeRef} of the node backing this keyword, may be {@code null}
   * @param mlValues the multilingual values of the keyword
   */
  /*package*/ KeywordImpl(final NodeRef id, final MLText mlValues) {
    this.id = id;
    this.mlValues = mlValues;
    this.value = null;
  }

  /**
   * Constructs a monolingual keyword that is not bound to any repository node.
   *
   * @param value the (single, non-localized) value of the keyword
   */
  public KeywordImpl(final String value) {
    this(null, null, value);
  }

  /**
   * Constructs a multilingual keyword, not bound to any repository node, seeded with a single
   * localized value.
   *
   * @param locale the keyword's locale
   * @param value  the value of the keyword for the given locale
   */
  public KeywordImpl(final Locale locale, final String value) {
    this(null, locale, value);
  }

  /**
   * Constructs a multilingual keyword, not bound to any repository node, from its localized values.
   *
   * @param mlValues the multilingual values of the keyword
   */
  public KeywordImpl(final MLText mlValues) {
    this(null, mlValues);
  }

  /**
   * Returns the monolingual value of this keyword.
   *
   * @return the value, or {@code null} if the keyword is multilingual
   */
  public String getValue() {
    return value;
  }

  /**
   * Returns the multilingual values of this keyword.
   *
   * @return the {@link MLText} values, or {@code null} if the keyword is monolingual
   */
  public MLText getMLValues() {
    return mlValues;
  }

  /**
   * Indicates whether this keyword holds multilingual (localized) values.
   *
   * @return {@code true} if the keyword is multilingual, {@code false} if it is monolingual
   */
  public boolean isKeywordTranslated() {
    return mlValues != null;
  }

  /**
   * Computes a hash code consistent with {@link #equals(Object)}, based on the keyword's id,
   * multilingual values and monolingual value.
   *
   * @return the hash code for this keyword
   */
  @Override
  public int hashCode() {
    final int PRIME = 31;
    int result = 1;
    result = PRIME * result + ((id == null) ? 0 : id.hashCode());
    result = PRIME * result + ((mlValues == null) ? 0 : mlValues.hashCode());
    result = PRIME * result + ((value == null) ? 0 : value.hashCode());
    return result;
  }

  /**
   * Compares this keyword with another object for equality. Two keywords are considered equal when
   * they have the same id, multilingual values and monolingual value (and pass the superclass
   * equality check).
   *
   * @param obj the object to compare with
   * @return {@code true} if the given object is an equal keyword, {@code false} otherwise
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final KeywordImpl other = (KeywordImpl) obj;
    if (id == null) {
      if (other.id != null) {
        return false;
      }
    } else if (!id.equals(other.id)) {
      return false;
    }
    if (mlValues == null) {
      if (other.mlValues != null) {
        return false;
      }
    } else if (!mlValues.equals(other.mlValues)) {
      return false;
    }
    if (value == null) {
      if (other.value != null) {
        return false;
      }
    } else if (!value.equals(other.value)) {
      return false;
    }
    return true;
  }

  /**
   * Returns a human-readable representation of this keyword.
   *
   * <p>For a multilingual keyword the returned string lists every locale/value pair in the form
   * {@code (lang) value}, separated by commas. For a monolingual keyword the raw value is returned.
   *
   * @return the string representation of this keyword
   */
  public String toString() {
    final StringBuilder buff = new StringBuilder();

    if (isKeywordTranslated()) {
      boolean first = true;

      for (Map.Entry<Locale, String> entry : getMLValues().entrySet()) {
        if (first) {
          first = false;
        } else {
          buff.append(", ");
        }

        buff
          .append('(')
          .append(entry.getKey().getLanguage())
          .append(") ")
          .append(entry.getValue());
      }
    } else {
      buff.append(getValue());
    }

    return buff.toString();
  }

  /**
   * Returns the string representation of this keyword, equivalent to {@link #toString()}.
   *
   * @return the string representation of this keyword
   */
  public String getString() {
    return toString();
  }

  /**
   * Returns the identifier of this keyword.
   *
   * @return the {@link NodeRef} backing this keyword, or {@code null} if it is not persisted
   */
  public NodeRef getId() {
    return id;
  }

  /**
   * Adds a localized translation to this (already multilingual) keyword.
   *
   * @param locale  the locale of the translation; must not be {@code null}
   * @param keyword the translated value; must not be {@code null} or empty
   * @throws NullPointerException          if {@code locale} is {@code null}, or {@code keyword} is
   *                                       {@code null} or empty
   * @throws UnsupportedOperationException if this keyword is not multilingual; call
   *                                       {@link #makeMultilingual(Locale)} first
   */
  public void addTranlatation(final Locale locale, final String keyword)
    throws UnsupportedOperationException {
    if (locale == null) {
      throw new NullPointerException("The locale is a mandatory parameter");
    }
    if (keyword == null || keyword.isEmpty()) {
      throw new NullPointerException("The keyword is a mandatory parameter");
    }
    if (!isKeywordTranslated()) {
      throw new UnsupportedOperationException(
        "Make the keyword multilingual before add transtlation"
      );
    }

    mlValues.addValue(locale, keyword);
  }

  /**
   * Promotes this monolingual keyword to a multilingual one, using the current monolingual value as
   * the value for the supplied locale and clearing the monolingual value.
   *
   * @param locale the locale under which the current value is stored; must not be {@code null}
   * @throws NullPointerException          if {@code locale} is {@code null}
   * @throws UnsupportedOperationException if this keyword is already multilingual
   */
  public void makeMultilingual(final Locale locale)
    throws UnsupportedOperationException {
    if (locale == null) {
      throw new NullPointerException("The locale is a mandatory parameter");
    }
    if (isKeywordTranslated()) {
      throw new UnsupportedOperationException(
        "The keyword is already seted being multilingual"
      );
    }

    this.mlValues = new MLText();
    this.mlValues.put(locale, value);
    this.value = null;
  }

  /**
   * Replaces all translations of this keyword with the supplied localized values, making the
   * keyword multilingual and clearing any monolingual value.
   *
   * @param translations the multilingual values to set; must contain at least one entry
   * @throws NullPointerException if {@code translations} is {@code null} or empty
   */
  public void setTranlatations(final MLText translations) {
    if (translations == null || translations.size() < 1) {
      throw new NullPointerException("At least one translation is required");
    }

    this.mlValues = translations;
    this.value = null;
  }

  /**
   * Checks whether this keyword has, for the given locale, a translation equal to the supplied
   * value.
   *
   * @param locale the locale to look up
   * @param value  the value to compare against the stored translation
   * @return {@code true} if a translation exists for {@code locale} and it equals {@code value},
   *         {@code false} otherwise (including when the keyword is monolingual)
   */
  @Override
  public boolean exists(Locale locale, String value) {
    boolean result = false;
    if (this.mlValues != null) {
      String localValue = this.mlValues.getValue(locale);
      if (localValue != null) {
        result = localValue.equals(value);
      }
    }

    return result;
  }

  /**
   * Returns the transient selection flag of this keyword.
   *
   * @return the selection flag, or {@code null} if unset
   */
  public Boolean getSelected() {
    return selected;
  }

  /**
   * Sets the transient selection flag of this keyword.
   *
   * @param selected the selection flag to set
   */
  public void setSelected(Boolean selected) {
    this.selected = selected;
  }
}
