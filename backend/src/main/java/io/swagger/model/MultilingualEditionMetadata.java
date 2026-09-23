package io.swagger.model;

import java.util.Objects;

/**
 * Data transfer object carrying the metadata required to enable a document for
 * multilingual editions and to create a new edition of that document.
 *
 * <p>Instances are typically deserialized from the JSON request body of the
 * REST endpoints that manage multilingual editions and passed to the service
 * layer. The object groups the caller-supplied options that influence how the
 * new edition is created (versioning note, minor/major change flag, the
 * reference of the new pivot translation and whether notifications should be
 * suppressed).
 */
public class MultilingualEditionMetadata {

  /** Free-text note associated with the new edition (e.g. a version comment). */
  private String note = null;

  /**
   * Flag indicating whether the edition represents a minor change
   * ({@code true}) rather than a major one ({@code false}).
   */
  private Boolean minorChange = null;

  /** Reference of the node to be used as the new pivot (reference) translation. */
  private String newPivotRef = null;

  /**
   * Flag indicating whether notifications normally triggered by the edition
   * should be suppressed ({@code true} disables them).
   */
  private Boolean disableNotifications = null;

  /**
   * Get note
   *
   * @return note
   */
  public String getNote() {
    return note;
  }

  /**
   * Set the free-text note associated with the new edition.
   *
   * @param note the version note or comment to associate with the edition
   */
  public void setNote(String note) {
    this.note = note;
  }

  /**
   * Get minorChange
   *
   * @return minorChange
   */
  public Boolean getMinorChange() {
    return minorChange;
  }

  /**
   * Set whether the edition represents a minor change.
   *
   * @param minorChange {@code true} for a minor change, {@code false} for a
   *     major change
   */
  public void setMinorChange(Boolean minorChange) {
    this.minorChange = minorChange;
  }

  /**
   * Get newPivotRef
   *
   * @return newPivotRef
   */
  public String getNewPivotRef() {
    return newPivotRef;
  }

  /**
   * Set the reference of the node to be used as the new pivot translation.
   *
   * @param newPivotRef the reference of the new pivot (reference) translation
   */
  public void setNewPivotRef(String newPivotRef) {
    this.newPivotRef = newPivotRef;
  }

  /**
   * Get disableNotifications
   *
   * @return disableNotifications
   */
  public Boolean getDisableNotifications() {
    return disableNotifications;
  }

  /**
   * Set whether notifications triggered by the edition should be suppressed.
   *
   * @param disableNotifications {@code true} to disable notifications,
   *     {@code false} to send them as usual
   */
  public void setDisableNotifications(Boolean disableNotifications) {
    this.disableNotifications = disableNotifications;
  }

  /**
   * Compares this metadata object with another for equality based on all of
   * its fields ({@code note}, {@code minorChange}, {@code newPivotRef} and
   * {@code disableNotifications}).
   *
   * @param o the object to compare with
   * @return {@code true} if the given object is a
   *     {@code MultilingualEditionMetadata} with equal field values,
   *     {@code false} otherwise
   */
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MultilingualEditionMetadata multilingualEditionMetadata =
      (MultilingualEditionMetadata) o;
    return (
      Objects.equals(this.note, multilingualEditionMetadata.note) &&
      Objects.equals(
        this.minorChange,
        multilingualEditionMetadata.minorChange
      ) &&
      Objects.equals(
        this.newPivotRef,
        multilingualEditionMetadata.newPivotRef
      ) &&
      Objects.equals(
        this.disableNotifications,
        multilingualEditionMetadata.disableNotifications
      )
    );
  }

  /**
   * Returns a hash code consistent with {@link #equals(java.lang.Object)},
   * derived from all fields of this object.
   *
   * @return the computed hash code
   */
  @Override
  public int hashCode() {
    return Objects.hash(note, minorChange, newPivotRef, disableNotifications);
  }

  /**
   * Returns a human-readable, multi-line representation of this object listing
   * each field and its value. Intended for debugging and logging only.
   *
   * @return a string representation of this metadata object
   */
  @Override
  public String toString() {
    return (
      "class MultilingualEditionMetadata {\n" +
      "    note: " +
      toIndentedString(note) +
      "\n" +
      "    minorChange: " +
      toIndentedString(minorChange) +
      "\n" +
      "    newPivotRef: " +
      toIndentedString(newPivotRef) +
      "\n" +
      "    disableNotifications: " +
      toIndentedString(disableNotifications) +
      "\n" +
      "}"
    );
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first line).
   *
   * @param o the object to render, may be {@code null}
   * @return the indented string representation of the object
   */
  private String toIndentedString(java.lang.Object o) {
    return Util.toIndentedString(o);
  }
}
