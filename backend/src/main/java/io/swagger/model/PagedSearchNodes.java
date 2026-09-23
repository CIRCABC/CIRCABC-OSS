package io.swagger.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Data transfer object representing a single page of node search results.
 *
 * <p>It bundles the {@link SearchNode} items that belong to the current page
 * together with the total number of matching nodes across all pages. The
 * {@code total} value is intentionally independent of paging so that the UI
 * can compute how many pages exist. This model is serialized to JSON and
 * returned by search-related REST endpoints.</p>
 */
@jakarta.annotation.Generated(
  value = "io.swagger.codegen.languages.SpringCodegen",
  date = "2017-03-22T15:13:11.258+01:00"
)
public class PagedSearchNodes {

  /** The search result nodes contained in the current page. */
  private List<SearchNode> data = new ArrayList<>();

  /**
   * The total number of matching nodes across all pages, ignoring paging,
   * used by the UI to compute the number of pages.
   */
  private Long total = null;

  /**
   * Adds a single search result node to the current page and returns this
   * instance to allow fluent chaining.
   *
   * @param dataItem the search node to append to the page
   * @return this {@code PagedSearchNodes} instance
   */
  public PagedSearchNodes addDataItem(SearchNode dataItem) {
    this.data.add(dataItem);
    return this;
  }

  /**
   * Returns the search result nodes for the current page.
   *
   * @return the list of {@link SearchNode} items in this page
   */
  public List<SearchNode> getData() {
    return data;
  }

  /**
   * Sets the search result nodes for the current page.
   *
   * @param data the list of {@link SearchNode} items to set
   */
  public void setData(List<SearchNode> data) {
    this.data = data;
  }

  /**
   * Returns the total amount of nodes without paging, so the UI can compute
   * the number of pages.
   *
   * @return the total number of matching nodes across all pages
   */
  public Long getTotal() {
    return total;
  }

  /**
   * Sets the total amount of matching nodes across all pages.
   *
   * @param total the total number of matching nodes to set
   */
  public void setTotal(Long total) {
    this.total = total;
  }

  /**
   * Compares this object with another for equality based on the page data
   * and the total node count.
   *
   * @param o the object to compare against
   * @return {@code true} if the given object is a {@code PagedSearchNodes}
   *     with equal {@code data} and {@code total}, {@code false} otherwise
   */
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PagedSearchNodes pagedSearchNodes = (PagedSearchNodes) o;
    return (
      Objects.equals(this.data, pagedSearchNodes.data) &&
      Objects.equals(this.total, pagedSearchNodes.total)
    );
  }

  /**
   * Returns a hash code consistent with {@link #equals(Object)}, derived
   * from the page data and the total node count.
   *
   * @return the hash code for this object
   */
  @Override
  public int hashCode() {
    return Objects.hash(data, total);
  }

  /**
   * Returns a human-readable, multi-line string representation of this page,
   * including its data items and total count.
   *
   * @return a string representation of this {@code PagedSearchNodes}
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PagedSearchNodes {\n");

    sb.append("    data: ").append(toIndentedString(data)).append("\n");
    sb.append("    total: ").append(toIndentedString(total)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    return Util.toIndentedString(o);
  }
}
