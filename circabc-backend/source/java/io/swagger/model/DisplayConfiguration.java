/**
 *
 */
package io.swagger.model;

import java.util.Objects;

/** @author beaurpi */
public class DisplayConfiguration {

    private Boolean display = true;

    /** @return the display */
    public Boolean getDisplay() {
        return display;
    }

    /** @param display the display to set */
    public void setDisplay(Boolean display) {
        this.display = display;
    }

    @Override
    public int hashCode() {
        return Objects.hash(display);
    }

    @Override
    public String toString() {

        return "class DisplayConfiguration {\n"
                + "    display: "
                + toIndentedString(display)
                + "\n"
                + "}";
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        return Util.toIndentedString(o);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DisplayConfiguration that = (DisplayConfiguration) o;
        return Objects.equals(display, that.display);
    }
}
