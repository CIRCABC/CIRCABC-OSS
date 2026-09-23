/**
 *
 */
package io.swagger.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Data model representing the aggregated result of a search against the CIRCABC help content.
 *
 * <p>A single search may match several kinds of help resources, so this object groups the matches
 * into three separate collections: help categories, help articles and help links. It is typically
 * serialized to JSON and returned by the help search REST endpoint.
 *
 * @author beaurpi
 */
public class HelpSearchResult {

  /** Help categories that matched the search query. */
  private List<HelpCategory> categories = new ArrayList<>();

  /** Help articles that matched the search query. */
  private List<HelpArticle> articles = new ArrayList<>();

  /** Help links that matched the search query. */
  private List<HelpLink> links = new ArrayList<>();

  /**
   * Compares this result with another object for equality. Two {@code HelpSearchResult} instances
   * are equal when their categories, articles and links collections are all equal.
   *
   * @param o the object to compare with
   * @return {@code true} if the given object is an equal {@code HelpSearchResult}, {@code false}
   *     otherwise
   */
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    HelpSearchResult helpSearchResult = (HelpSearchResult) o;
    return (
      Objects.equals(this.categories, helpSearchResult.categories) &&
      Objects.equals(this.articles, helpSearchResult.articles) &&
      Objects.equals(this.links, helpSearchResult.links)
    );
  }

  /**
   * Returns a hash code consistent with {@link #equals(Object)}, derived from the categories,
   * articles and links collections.
   *
   * @return the hash code for this result
   */
  @Override
  public int hashCode() {
    return Objects.hash(categories, articles, links);
  }

  /**
   * Returns a human-readable, indented string representation of this result, listing its
   * categories, articles and links. Intended for logging and debugging.
   *
   * @return a string representation of this result
   */
  @Override
  public String toString() {
    return (
      "class HelpSearchResult {\n" +
      "    categories: " +
      toIndentedString(categories) +
      "\n" +
      "    articles: " +
      toIndentedString(articles) +
      "\n" +
      "    links: " +
      toIndentedString(links) +
      "\n }"
    );
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first line).
   *
   * @param o the object to convert to an indented string
   * @return the indented string representation of the object
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
