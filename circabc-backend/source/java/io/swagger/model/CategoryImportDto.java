package io.swagger.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * DTO for importing FAQ category data with complete hierarchy
 */
public class CategoryImportDto {

  private String id = null;

  private I18nProperty title = new I18nProperty();

  private Integer numberOfArticles = 0;

  private List<ArticleImportDto> articles = new ArrayList<>();

  private List<SubcategoryImportDto> subcategories = new ArrayList<>();

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

  public List<ArticleImportDto> getArticles() {
    return articles;
  }

  public void setArticles(List<ArticleImportDto> articles) {
    this.articles = articles;
  }

  public List<SubcategoryImportDto> getSubcategories() {
    return subcategories;
  }

  public void setSubcategories(List<SubcategoryImportDto> subcategories) {
    this.subcategories = subcategories;
  }

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CategoryImportDto that = (CategoryImportDto) o;
    return (
      Objects.equals(this.id, that.id) &&
      Objects.equals(this.title, that.title) &&
      Objects.equals(this.numberOfArticles, that.numberOfArticles) &&
      Objects.equals(this.articles, that.articles) &&
      Objects.equals(this.subcategories, that.subcategories)
    );
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, title, numberOfArticles, articles, subcategories);
  }

  @Override
  public String toString() {
    return (
      "class CategoryImportDto {\n" +
      "    id: " +
      toIndentedString(id) +
      "\n" +
      "    title: " +
      toIndentedString(title) +
      "\n" +
      "    numberOfArticles: " +
      toIndentedString(numberOfArticles) +
      "\n" +
      "    articles: " +
      toIndentedString(articles) +
      "\n" +
      "    subcategories: " +
      toIndentedString(subcategories) +
      "\n" +
      "}"
    );
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    return Util.toIndentedString(o);
  }
}
