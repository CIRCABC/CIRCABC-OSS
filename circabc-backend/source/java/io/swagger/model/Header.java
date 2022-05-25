package io.swagger.model;

import java.util.List;
import java.util.Objects;

/**
 * Representation of a header in CIRCABC in a JSON object
 */
public class Header {

    private String id = null;

    private String name = null;

    private I18nProperty description = null;

    private List<Category> categories = null;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get description
     *
     * @return description
     */
    public I18nProperty getDescription() {
        return description;
    }

    public void setDescription(I18nProperty description) {
        this.description = description;
    }

    /**
     * @return the categories
     */
    public List<Category> getCategories() {
        return categories;
    }

    /**
     * @param categories the categories to set
     */
    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Header header = (Header) o;
        return Objects.equals(this.id, header.id)
                && Objects.equals(this.name, header.name)
                && Objects.equals(this.description, header.description)
                && Objects.equals(this.categories, header.categories);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, categories);
    }

    @Override
    public String toString() {

        return "class Header {\n"
                + "    id: "
                + toIndentedString(id)
                + "\n"
                + "    name: "
                + toIndentedString(name)
                + "\n"
                + "    description: "
                + toIndentedString(description)
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
