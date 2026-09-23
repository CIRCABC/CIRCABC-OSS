package io.swagger.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Data transfer object representing a paginated collection of {@link Node} items.
 *
 * <p>This model wraps the subset of nodes returned for a single page together with the
 * total number of nodes available across all pages. The total is provided independently of
 * paging so the UI can compute the number of pages to display.
 */
public class PagedNodes {

  /** The nodes contained in the current page. Never {@code null}; defaults to an empty list. */
  private List<Node> data = new ArrayList<>();

  /** The total number of nodes across all pages, ignoring paging. May be {@code null}. */
  private Long total = null;

  /**
   * Sets the page contents and returns this instance for fluent chaining.
   *
   * @param data the list of nodes for the current page
   * @return this {@code PagedNodes} instance
   */
  public PagedNodes data(List<Node> data) {
    this.data = data;
    return this;
  }

  /**
   * Appends a single node to the current page contents.
   *
   * @param dataItem the node to add
   * @return this {@code PagedNodes} instance
   */
  public PagedNodes addDataItem(Node dataItem) {
    this.data.add(dataItem);
    return this;
  }

  /**
   * Get data
   *
   * @return data
   */
  public List<Node> getData() {
    return data;
  }

  /**
   * Sets the page contents.
   *
   * @param data the list of nodes for the current page
   */
  public void setData(List<Node> data) {
    this.data = data;
  }

  /**
   * Sets the total node count and returns this instance for fluent chaining.
   *
   * @param total the total number of nodes across all pages
   * @return this {@code PagedNodes} instance
   */
  public PagedNodes total(Long total) {
    this.total = total;
    return this;
  }

  /**
   * return the total amount of nodes, without paging so the UI can compute the number of pages
   *
   * @return total
   */
  public Long getTotal() {
    return total;
  }

  /**
   * Sets the total node count.
   *
   * @param total the total number of nodes across all pages
   */
  public void setTotal(Long total) {
    this.total = total;
  }

  /**
   * Compares this object with another for equality based on the page contents and total count.
   *
   * @param o the object to compare with
   * @return {@code true} if the given object is a {@code PagedNodes} with equal data and total
   */
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PagedNodes pagedNodes = (PagedNodes) o;
    return (
      Objects.equals(this.data, pagedNodes.data) &&
      Objects.equals(this.total, pagedNodes.total)
    );
  }

  /**
   * Returns a hash code consistent with {@link #equals(Object)}.
   *
   * @return the hash code derived from the data and total fields
   */
  @Override
  public int hashCode() {
    return Objects.hash(data, total);
  }

  /**
   * Returns a human-readable, multi-line string representation of this object.
   *
   * @return a string describing the data and total fields
   */
  @Override
  public String toString() {
    return (
      "class PagedNodes {\n" +
      "    data: " +
      toIndentedString(data) +
      "\n" +
      "    total: " +
      toIndentedString(total) +
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
