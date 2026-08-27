package io.swagger.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * DTO for exporting FAQ subcategory data with articles
 */
public class SubcategoryExportDto {

  private String id = null;

  private I18nProperty title = new I18nProperty();

  private Integer sortOrder = 0;

  private String parentId = null;

  private Integer numberOfArticles = 0;

  private List<ArticleExportDto> articles = new ArrayList<>();

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

  public Integer getSortOrder() {
    return sortOrder;
  }

  public void setSortOrder(Integer sortOrder) {
    this.sortOrder = sortOrder;
  }

  public String getParentId() {
    return parentId;
  }

  public void setParentId(String parentId) {
    this.parentId = parentId;
  }

  public Integer getNumberOfArticles() {
    return numberOfArticles;
  }

  public void setNumberOfArticles(Integer numberOfArticles) {
    this.numberOfArticles = numberOfArticles;
  }

  public List<ArticleExportDto> getArticles() {
    return articles;
  }

  public void setArticles(List<ArticleExportDto> articles) {
    this.articles = articles;
  }

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SubcategoryExportDto that = (SubcategoryExportDto) o;
    return (
      Objects.equals(this.id, that.id) &&
      Objects.equals(this.title, that.title) &&
      Objects.equals(this.sortOrder, that.sortOrder) &&
      Objects.equals(this.parentId, that.parentId) &&
      Objects.equals(this.numberOfArticles, that.numberOfArticles) &&
      Objects.equals(this.articles, that.articles)
    );
  }

  @Override
  public int hashCode() {
    return Objects.hash(
      id,
      title,
      sortOrder,
      parentId,
      numberOfArticles,
      articles
    );
  }

  @Override
  public String toString() {
    return (
      "class SubcategoryExportDto {\n" +
      "    id: " +
      toIndentedString(id) +
      "\n" +
      "    title: " +
      toIndentedString(title) +
      "\n" +
      "    sortOrder: " +
      toIndentedString(sortOrder) +
      "\n" +
      "    parentId: " +
      toIndentedString(parentId) +
      "\n" +
      "    numberOfArticles: " +
      toIndentedString(numberOfArticles) +
      "\n" +
      "    articles: " +
      toIndentedString(articles) +
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
