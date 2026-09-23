/**
 *
 */
package io.swagger.model.db;

/**
 * Plain data model representing a single localized translation entry as stored in the database.
 *
 * <p>Each instance associates a piece of translated text with a specific Alfresco locale, allowing
 * a value (such as a label or description) to be resolved for a given language. Instances are used
 * to carry translation rows between the persistence layer and the rest of the application.
 *
 * @author beaurpi
 */
public class TranslationEntry {

  /** Unique identifier of this translation entry. */
  private Long id;

  /** Identifier of the associated Alfresco locale that this translation applies to. */
  private Long alfLocaleId;

  /** The translated text value for the associated locale. */
  private String translation;

  /**
   * Returns the unique identifier of this translation entry.
   *
   * @return the entry id, or {@code null} if not yet assigned
   */
  public Long getId() {
    return id;
  }

  /**
   * Sets the unique identifier of this translation entry.
   *
   * @param id the entry id to set
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * Returns the identifier of the Alfresco locale this translation applies to.
   *
   * @return the Alfresco locale id, or {@code null} if not set
   */
  public Long getAlfLocaleId() {
    return alfLocaleId;
  }

  /**
   * Sets the identifier of the Alfresco locale this translation applies to.
   *
   * @param alfLocaleId the Alfresco locale id to set
   */
  public void setAlfLocaleId(Long alfLocaleId) {
    this.alfLocaleId = alfLocaleId;
  }

  /**
   * Returns the translated text value for the associated locale.
   *
   * @return the translation text, or {@code null} if not set
   */
  public String getTranslation() {
    return translation;
  }

  /**
   * Sets the translated text value for the associated locale.
   *
   * @param translation the translation text to set
   */
  public void setTranslation(String translation) {
    this.translation = translation;
  }
}
