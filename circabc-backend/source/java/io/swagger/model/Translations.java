package io.swagger.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * all the translations attachted to one node, it is not the translations of each of its properties
 * but the trasnlations of its content
 */
public class Translations {

    private Node pivot = null;

    private List<Node> translations = new ArrayList<>();

    /**
     * Get pivot
     *
     * @return pivot
     */
    public Node getPivot() {
        return pivot;
    }

    public void setPivot(Node pivot) {
        this.pivot = pivot;
    }

    public Translations addTranslationsItem(Node translationsItem) {
        this.translations.add(translationsItem);
        return this;
    }

    /**
     * Get translations
     *
     * @return translations
     */
    public List<Node> getTranslations() {
        return translations;
    }

    public void setTranslations(List<Node> translations) {
        this.translations = translations;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Translations objectTranslations = (Translations) o;
        return Objects.equals(this.pivot, objectTranslations.pivot)
                && Objects.equals(this.translations, objectTranslations.translations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pivot, translations);
    }

    @Override
    public String toString() {

        return "class Translations {\n"
                + "    pivot: "
                + toIndentedString(pivot)
                + "\n"
                + "    translations: "
                + toIndentedString(translations)
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
