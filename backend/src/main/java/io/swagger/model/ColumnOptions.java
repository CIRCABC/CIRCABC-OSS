/**
 *
 */
package io.swagger.model;

/**
 * Configuration flags that control which columns are displayed when rendering a document/content
 * listing (for example a library view) in the CIRCABC UI.
 *
 * <p>Each flag toggles the visibility of a single column. The default values reflect the columns
 * shown out of the box: {@code name}, {@code title}, {@code version}, {@code modification},
 * {@code size} and {@code expiration} are enabled by default, while {@code creation},
 * {@code status}, {@code description}, {@code author} and {@code securityRanking} are disabled by
 * default.
 *
 * @author beaurpi
 */
public class ColumnOptions {

  /** Whether the name column is displayed. Enabled by default. */
  private Boolean name = true;
  /** Whether the title column is displayed. Enabled by default. */
  private Boolean title = true;
  /** Whether the version column is displayed. Enabled by default. */
  private Boolean version = true;
  /** Whether the last-modification column is displayed. Enabled by default. */
  private Boolean modification = true;
  /** Whether the creation-date column is displayed. Disabled by default. */
  private Boolean creation = false;
  /** Whether the size column is displayed. Enabled by default. */
  private Boolean size = true;
  /** Whether the expiration column is displayed. Enabled by default. */
  private Boolean expiration = true;
  /** Whether the status column is displayed. Disabled by default. */
  private Boolean status = false;
  /** Whether the description column is displayed. Disabled by default. */
  private Boolean description = false;
  /** Whether the author column is displayed. Disabled by default. */
  private Boolean author = false;
  /** Whether the security-ranking column is displayed. Disabled by default. */
  private Boolean securityRanking = false;

  /** @return whether the name column is displayed */
  public Boolean getName() {
    return name;
  }

  /** @return whether the title column is displayed */
  public Boolean getTitle() {
    return title;
  }

  /** @param title whether the title column should be displayed */
  public void setTitle(Boolean title) {
    this.title = title;
  }

  /** @param name whether the name column should be displayed */
  public void setName(Boolean name) {
    this.name = name;
  }

  /** @return whether the version column is displayed */
  public Boolean getVersion() {
    return version;
  }

  /** @param version whether the version column should be displayed */
  public void setVersion(Boolean version) {
    this.version = version;
  }

  /** @return whether the last-modification column is displayed */
  public Boolean getModification() {
    return modification;
  }

  /** @param modification whether the last-modification column should be displayed */
  public void setModification(Boolean modification) {
    this.modification = modification;
  }

  /** @return whether the creation-date column is displayed */
  public Boolean getCreation() {
    return creation;
  }

  /** @param creation whether the creation-date column should be displayed */
  public void setCreation(Boolean creation) {
    this.creation = creation;
  }

  /** @return whether the size column is displayed */
  public Boolean getSize() {
    return size;
  }

  /** @param size whether the size column should be displayed */
  public void setSize(Boolean size) {
    this.size = size;
  }

  /** @return whether the expiration column is displayed */
  public Boolean getExpiration() {
    return expiration;
  }

  /** @param expiration whether the expiration column should be displayed */
  public void setExpiration(Boolean expiration) {
    this.expiration = expiration;
  }

  /** @return whether the status column is displayed */
  public Boolean getStatus() {
    return status;
  }

  /** @param status whether the status column should be displayed */
  public void setStatus(Boolean status) {
    this.status = status;
  }

  /** @return whether the description column is displayed */
  public Boolean getDescription() {
    return description;
  }

  /** @param description whether the description column should be displayed */
  public void setDescription(Boolean description) {
    this.description = description;
  }

  /** @return whether the author column is displayed */
  public Boolean getAuthor() {
    return author;
  }

  /** @param author whether the author column should be displayed */
  public void setAuthor(Boolean author) {
    this.author = author;
  }

  /** @return whether the security-ranking column is displayed */
  public Boolean getSecurityRanking() {
    return securityRanking;
  }

  /** @param securityRanking whether the security-ranking column should be displayed */
  public void setSecurityRanking(Boolean securityRanking) {
    this.securityRanking = securityRanking;
  }
}
