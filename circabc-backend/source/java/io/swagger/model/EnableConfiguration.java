/**
 *
 */
package io.swagger.model;

import java.util.Objects;

/** @author beaurpi */
public class EnableConfiguration {

    private Boolean enable = true;

    /** @return the enable */
    public Boolean getEnable() {
        return enable;
    }

    /** @param enable the enable to set */
    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    @Override
    public int hashCode() {
        return Objects.hash(enable);
    }

    @Override
    public String toString() {

        return "class EnableConfiguration {\n" + "    enable: " + toIndentedString(enable) + "\n" + "}";
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
        EnableConfiguration that = (EnableConfiguration) o;
        return Objects.equals(enable, that.enable);
    }
}
