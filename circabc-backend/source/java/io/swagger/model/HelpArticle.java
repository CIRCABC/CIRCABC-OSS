/**
 *
 */
package io.swagger.model;

import org.joda.time.DateTime;

import java.util.Objects;

/** @author beaurpi */
public class HelpArticle {

    private String id = null;

    private I18nProperty title = new I18nProperty();

    private I18nProperty content = new I18nProperty();

    private DateTime lastUpdate;

    private String author = null;

    private String parentId = null;

    private Boolean highlighted = null;

    private Integer visitCounter = null;

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HelpArticle helpCategory = (HelpArticle) o;
        return Objects.equals(this.id, helpCategory.id)
                && Objects.equals(this.title, helpCategory.title)
                && Objects.equals(this.content, helpCategory.content)
                && Objects.equals(this.lastUpdate, helpCategory.lastUpdate)
                && Objects.equals(this.author, helpCategory.author)
                && Objects.equals(this.parentId, helpCategory.parentId)
                && Objects.equals(this.highlighted, helpCategory.highlighted)
                && Objects.equals(this.visitCounter, helpCategory.visitCounter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, content);
    }

    @Override
    public String toString() {

        return "class HelpArticle {\n"
                + "    id: "
                + toIndentedString(id)
                + "\n"
                + "    title: "
                + toIndentedString(title)
                + "\n"
                + "    content: "
                + toIndentedString(content)
                + "\n"
                + "    lastUpdate: "
                + toIndentedString(lastUpdate)
                + "\n"
                + "    author: "
                + toIndentedString(author)
                + "\n"
                + "    parentId: "
                + toIndentedString(parentId)
                + "\n"
                + "    highlighted: "
                + toIndentedString(highlighted)
                + "\n"
                + "    visitCounter: "
                + toIndentedString(visitCounter)
                + "\n"
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

    public I18nProperty getContent() {
        return content;
    }

    public void setContent(I18nProperty content) {
        this.content = content;
    }

    public DateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(DateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public Boolean getHighlighted() {
        return highlighted;
    }

    public void setHighlighted(Boolean highlighted) {
        this.highlighted = highlighted;
    }

    public Integer getVisitCounter() {
        return visitCounter;
    }

    public void setVisitCounter(Integer visitCounter) {
        this.visitCounter = visitCounter;
    }
}
