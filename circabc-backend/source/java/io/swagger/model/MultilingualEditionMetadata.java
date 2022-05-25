package io.swagger.model;

import java.util.Objects;

/**
 * object used for both enabling a document for create a new edition.
 */
public class MultilingualEditionMetadata {

    private String note = null;

    private Boolean minorChange = null;

    private String newPivotRef = null;

    private Boolean disableNotifications = null;

    /**
     * Get note
     *
     * @return note
     */
    public String getNote() {
        return note;
    }

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

    public void setDisableNotifications(Boolean disableNotifications) {
        this.disableNotifications = disableNotifications;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MultilingualEditionMetadata multilingualEditionMetadata = (MultilingualEditionMetadata) o;
        return Objects.equals(this.note, multilingualEditionMetadata.note)
                && Objects.equals(this.minorChange, multilingualEditionMetadata.minorChange)
                && Objects.equals(this.newPivotRef, multilingualEditionMetadata.newPivotRef)
                && Objects.equals(
                this.disableNotifications, multilingualEditionMetadata.disableNotifications);
    }

    @Override
    public int hashCode() {
        return Objects.hash(note, minorChange, newPivotRef, disableNotifications);
    }

    @Override
    public String toString() {

        return "class MultilingualEditionMetadata {\n"
                + "    note: "
                + toIndentedString(note)
                + "\n"
                + "    minorChange: "
                + toIndentedString(minorChange)
                + "\n"
                + "    newPivotRef: "
                + toIndentedString(newPivotRef)
                + "\n"
                + "    disableNotifications: "
                + toIndentedString(disableNotifications)
                + "\n"
                + "}";
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        return Util.toIndentedString(o);
    }
}
