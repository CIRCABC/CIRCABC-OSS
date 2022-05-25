package io.swagger.model;

import java.util.Objects;

/**
 * object used for both enabling a document for multilingual aspect.
 */
public class MultilingualAspectMetadata {

    private String pivotLang = null;

    private String author = null;

    /**
     * Get pivotLang
     *
     * @return pivotLang
     */
    public String getPivotLang() {
        return pivotLang;
    }

    public void setPivotLang(String pivotLang) {
        this.pivotLang = pivotLang;
    }

    /**
     * Get author
     *
     * @return author
     */
    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MultilingualAspectMetadata multilingualAspectMetadata = (MultilingualAspectMetadata) o;
        return Objects.equals(this.pivotLang, multilingualAspectMetadata.pivotLang)
                && Objects.equals(this.author, multilingualAspectMetadata.author);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pivotLang, author);
    }

    @Override
    public String toString() {

        return "class MultilingualAspectMetadata {\n"
                + "    pivotLang: "
                + toIndentedString(pivotLang)
                + "\n"
                + "    author: "
                + toIndentedString(author)
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
