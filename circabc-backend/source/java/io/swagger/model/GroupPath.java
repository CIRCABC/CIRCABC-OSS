/**
 *
 */
package io.swagger.model;

import java.util.Objects;

/** @author beaurpi */
public class GroupPath {

    private Header header;
    private Category category;
    private InterestGroup group;

    /** @return the header */
    public Header getHeader() {
        return header;
    }

    /** @param header the header to set */
    public void setHeader(Header header) {
        this.header = header;
    }

    /** @return the category */
    public Category getCategory() {
        return category;
    }

    /** @param category the category to set */
    public void setCategory(Category category) {
        this.category = category;
    }

    /** @return the group */
    public InterestGroup getGroup() {
        return group;
    }

    /** @param group the group to set */
    public void setGroup(InterestGroup group) {
        this.group = group;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GroupPath groupPath = (GroupPath) o;
        return Objects.equals(this.header, groupPath.header)
                && Objects.equals(this.category, groupPath.category)
                && Objects.equals(this.group, groupPath.group);
    }

    @Override
    public int hashCode() {
        return Objects.hash(header, category, group);
    }

    @Override
    public String toString() {

        return "class GroupPath {\n"
                + "    header: "
                + toIndentedString(header)
                + "\n"
                + "    category: "
                + toIndentedString(category)
                + "\n"
                + "    group: "
                + toIndentedString(group)
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
