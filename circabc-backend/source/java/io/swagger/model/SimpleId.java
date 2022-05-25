package io.swagger.model;

import java.util.Objects;

/**
 * SimpleId
 */
public class SimpleId {

    private String id = null;

    /**
     * Get id
     *
     * @return id
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SimpleId simpleId = (SimpleId) o;
        return Objects.equals(this.id, simpleId.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {

        return "class SimpleId {\n" + "    id: " + toIndentedString(id) + "\n" + "}";
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        return Util.toIndentedString(o);
    }
}
