package io.swagger.model;

import java.util.Objects;

/**
 * DTO for importing FAQ article data
 */
public class ArticleImportDto {

  private String id = null;

  private String parentId = null;

  private I18nProperty title = new I18nProperty();

  private I18nProperty content = new I18nProperty();

  private Boolean highlighted = null;

  private Integer sortOrder = 0;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getParentId() {
    return parentId;
  }

  public void setParentId(String parentId) {
    this.parentId = parentId;
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

  public Boolean getHighlighted() {
    return highlighted;
  }

  public void setHighlighted(Boolean highlighted) {
    this.highlighted = highlighted;
  }

  public Integer getSortOrder() {
    return sortOrder;
  }

  public void setSortOrder(Integer sortOrder) {
    this.sortOrder = sortOrder;
  }

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ArticleImportDto that = (ArticleImportDto) o;
    return (
      Objects.equals(this.id, that.id) &&
      Objects.equals(this.parentId, that.parentId) &&
      Objects.equals(this.title, that.title) &&
      Objects.equals(this.content, that.content) &&
      Objects.equals(this.highlighted, that.highlighted) &&
      Objects.equals(this.sortOrder, that.sortOrder)
    );
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, parentId, title, content, highlighted, sortOrder);
  }

  @Override
  public String toString() {
    return (
      "class ArticleImportDto {\n" +
      "    id: " +
      toIndentedString(id) +
      "\n" +
      "    parentId: " +
      toIndentedString(parentId) +
      "\n" +
      "    title: " +
      toIndentedString(title) +
      "\n" +
      "    content: " +
      toIndentedString(content) +
      "\n" +
      "    highlighted: " +
      toIndentedString(highlighted) +
      "\n" +
      "    sortOrder: " +
      toIndentedString(sortOrder) +
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
