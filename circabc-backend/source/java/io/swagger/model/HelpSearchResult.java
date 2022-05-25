/**
 *
 */
package io.swagger.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** @author beaurpi */
public class HelpSearchResult {

    private List<HelpCategory> categories = new ArrayList<>();

    private List<HelpArticle> articles = new ArrayList<>();

    private List<HelpLink> links = new ArrayList<>();

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HelpSearchResult helpSearchResult = (HelpSearchResult) o;
        return Objects.equals(this.categories, helpSearchResult.categories)
                && Objects.equals(this.articles, helpSearchResult.articles)
                && Objects.equals(this.links, helpSearchResult.links);
    }

    @Override
    public int hashCode() {
        return Objects.hash(categories, articles, links);
    }

    @Override
    public String toString() {

        return "class HelpSearchResult {\n"
                + "    categories: "
                + toIndentedString(categories)
                + "\n"
                + "    articles: "
                + toIndentedString(articles)
                + "\n"
                + "    links: "
                + toIndentedString(links)
                + "\n }";
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        return Util.toIndentedString(o);
    }

    /** @return the categories */
    public List<HelpCategory> getCategories() {
        return categories;
    }

    /** @param categories the categories to set */
    public void setCategories(List<HelpCategory> categories) {
        this.categories = categories;
    }

    /** @return the articles */
    public List<HelpArticle> getArticles() {
        return articles;
    }

    /** @param articles the articles to set */
    public void setArticles(List<HelpArticle> articles) {
        this.articles = articles;
    }

    /** @return the links */
    public List<HelpLink> getLinks() {
        return links;
    }

    /** @param links the links to set */
    public void setLinks(List<HelpLink> links) {
        this.links = links;
    }
}
