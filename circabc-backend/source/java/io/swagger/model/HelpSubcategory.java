/**
 *
 */
package io.swagger.model;

import java.util.Objects;

/** @author beaurpi */
public class HelpSubcategory {

  private String id = null;

  private I18nProperty title = new I18nProperty();

  private Integer sortOrder = 0;

  private String parentId = null;

  private Integer numberOfArticles = 0;

  private String lastUpdate = null;

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    HelpSubcategory helpSubcategory = (HelpSubcategory) o;
    return (
      Objects.equals(this.id, helpSubcategory.id) &&
      Objects.equals(this.title, helpSubcategory.title) &&
      Objects.equals(this.sortOrder, helpSubcategory.sortOrder) &&
      Objects.equals(this.parentId, helpSubcategory.parentId) &&
      Objects.equals(this.numberOfArticles, helpSubcategory.numberOfArticles) &&
      Objects.equals(this.lastUpdate, helpSubcategory.lastUpdate)
    );
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, title);
  }

  @Override
  public String toString() {
    return (
      "class HelpSubcategory {\n" +
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
      "    lastUpdate: " +
      toIndentedString(lastUpdate) +
      "}"
    );
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

  public String getLastUpdate() {
    return lastUpdate;
  }

  public void setLastUpdate(String lastUpdate) {
    this.lastUpdate = lastUpdate;
  }
}
