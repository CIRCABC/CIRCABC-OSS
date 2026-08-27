package io.swagger.model;

import java.util.Objects;

/**
 * DTO for returning import operation results with statistics
 */
public class ImportResult {

  private String message = null;

  private Integer categoriesProcessed = 0;

  private Integer subcategoriesProcessed = 0;

  private Integer articlesProcessed = 0;

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public Integer getCategoriesProcessed() {
    return categoriesProcessed;
  }

  public void setCategoriesProcessed(Integer categoriesProcessed) {
    this.categoriesProcessed = categoriesProcessed;
  }

  public Integer getSubcategoriesProcessed() {
    return subcategoriesProcessed;
  }

  public void setSubcategoriesProcessed(Integer subcategoriesProcessed) {
    this.subcategoriesProcessed = subcategoriesProcessed;
  }

  public Integer getArticlesProcessed() {
    return articlesProcessed;
  }

  public void setArticlesProcessed(Integer articlesProcessed) {
    this.articlesProcessed = articlesProcessed;
  }

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ImportResult that = (ImportResult) o;
    return (
      Objects.equals(this.message, that.message) &&
      Objects.equals(this.categoriesProcessed, that.categoriesProcessed) &&
      Objects.equals(
        this.subcategoriesProcessed,
        that.subcategoriesProcessed
      ) &&
      Objects.equals(this.articlesProcessed, that.articlesProcessed)
    );
  }

  @Override
  public int hashCode() {
    return Objects.hash(
      message,
      categoriesProcessed,
      subcategoriesProcessed,
      articlesProcessed
    );
  }

  @Override
  public String toString() {
    return (
      "class ImportResult {\n" +
      "    message: " +
      toIndentedString(message) +
      "\n" +
      "    categoriesProcessed: " +
      toIndentedString(categoriesProcessed) +
      "\n" +
      "    subcategoriesProcessed: " +
      toIndentedString(subcategoriesProcessed) +
      "\n" +
      "    articlesProcessed: " +
      toIndentedString(articlesProcessed) +
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
