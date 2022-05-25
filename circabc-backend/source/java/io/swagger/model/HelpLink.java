/**
 *
 */
package io.swagger.model;

import java.util.Objects;

/** @author beaurpi */
public class HelpLink {

    private String id = null;

    private I18nProperty title = new I18nProperty();

    private String href;

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HelpLink helpLink = (HelpLink) o;
        return Objects.equals(this.id, helpLink.id)
                && Objects.equals(this.title, helpLink.title)
                && Objects.equals(this.href, helpLink.href);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, href);
    }

    @Override
    public String toString() {

        return "class HelpLink {\n"
                + "    id: "
                + toIndentedString(id)
                + "\n"
                + "    title: "
                + toIndentedString(title)
                + "\n"
                + "    href: "
                + toIndentedString(href)
                + "\n }";
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

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }
}
