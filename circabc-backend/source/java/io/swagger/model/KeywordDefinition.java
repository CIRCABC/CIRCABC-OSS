package io.swagger.model;

import java.util.Objects;

public class KeywordDefinition {

    private String id = null;

    private String name = null;

    private I18nProperty title = new I18nProperty();

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

    /**
     * Get name
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get title
     *
     * @return title
     */
    public I18nProperty getTitle() {
        return title;
    }

    public void setTitle(I18nProperty title) {
        this.title = title;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        KeywordDefinition keywordDefinition = (KeywordDefinition) o;
        return Objects.equals(this.id, keywordDefinition.id)
                && Objects.equals(this.name, keywordDefinition.name)
                && Objects.equals(this.title, keywordDefinition.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, title);
    }

    @Override
    public String toString() {

        return "class KeywordDefinition {\n"
                + "    id: "
                + toIndentedString(id)
                + "\n"
                + "    name: "
                + toIndentedString(name)
                + "\n"
                + "    title: "
                + toIndentedString(title)
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
