package io.swagger.model;

import java.util.Objects;

/**
 * GuardAuthorization
 */
public class GuardAuthorization {

    private Boolean granted = null;

    /**
     * Get granted
     *
     * @return granted
     */
    public Boolean getGranted() {
        return granted;
    }

    public void setGranted(Boolean granted) {
        this.granted = granted;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GuardAuthorization guardAuthorization = (GuardAuthorization) o;
        return Objects.equals(this.granted, guardAuthorization.granted);
    }

    @Override
    public int hashCode() {
        return Objects.hash(granted);
    }

    @Override
    public String toString() {

        return "class GuardAuthorization {\n"
                + "    granted: "
                + toIndentedString(granted)
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
