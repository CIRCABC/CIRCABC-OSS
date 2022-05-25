/**
 *
 */
package io.swagger.model;

import java.util.Objects;

/** @author beaurpi */
public class HelpCategory {

    private String id = null;

    private I18nProperty title = new I18nProperty();

    private Integer numberOfArticles = 0;

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HelpCategory helpCategory = (HelpCategory) o;
        return Objects.equals(this.id, helpCategory.id)
                && Objects.equals(this.title, helpCategory.title)
                && Objects.equals(this.numberOfArticles, helpCategory.numberOfArticles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title);
    }

    @Override
    public String toString() {

        return "class HelpCategory {\n"
                + "    id: "
                + toIndentedString(id)
                + "\n"
                + "    title: "
                + toIndentedString(title)
                + "\n"
                + "    numberOfArticles: "
                + toIndentedString(numberOfArticles)
                + "}";
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        return Util.toIndentedString(o);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public I18nProperty getTitle() {
        return title;
    }

    public void setTitle(I18nProperty title) {
        this.title = title;
    }

    public Integer getNumberOfArticles() {
        return numberOfArticles;
    }

    public void setNumberOfArticles(Integer numberOfArticles) {
        this.numberOfArticles = numberOfArticles;
    }
}
