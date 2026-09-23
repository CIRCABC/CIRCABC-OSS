package io.swagger.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Data transfer object representing a paginated collection of archived nodes.
 *
 * <p>It bundles the current page of {@link ArchiveNode} items together with the
 * total number of matching nodes (ignoring paging), allowing a client UI to
 * render the page contents while computing the total number of pages.
 */
public class PagedArchiveNodes {

  /** The archived nodes contained in the current page. */
  private List<ArchiveNode> data = new ArrayList<>();

  /** Total number of matching nodes across all pages, ignoring paging. */
  private Long total = null;

  /**
   * Returns the archived nodes contained in the current page.
   *
   * @return the list of archived nodes for this page
   */
  public List<ArchiveNode> getData() {
    return data;
  }

  /**
   * Sets the archived nodes for the current page.
   *
   * @param data the list of archived nodes to set
   */
  public void setData(List<ArchiveNode> data) {
    this.data = data;
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
   * Sets the total number of matching nodes across all pages.
   *
   * @param total the total number of nodes ignoring paging
   */
  public void setTotal(Long total) {
    this.total = total;
  }

  /**
   * Compares this object with another for equality based on the paged data and
   * total count.
   *
   * @param o the object to compare with
   * @return {@code true} if the given object is a {@code PagedArchiveNodes} with
   *     equal data and total, {@code false} otherwise
   */
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PagedArchiveNodes pagedArchiveNodes = (PagedArchiveNodes) o;
    return (
      Objects.equals(this.data, pagedArchiveNodes.data) &&
      Objects.equals(this.total, pagedArchiveNodes.total)
    );
  }

  /**
   * Returns a hash code derived from the paged data and total count.
   *
   * @return the hash code for this object
   */
  @Override
  public int hashCode() {
    return Objects.hash(data, total);
  }

  /**
   * Returns a human-readable string representation of this object.
   *
   * @return a string describing the data and total fields
   */
  @Override
  public String toString() {
    return (
      "class PagedArchiveNodes {\n" +
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
   *
   * @param o the object to convert to an indented string
   * @return the indented string representation of the object
   */
  private String toIndentedString(java.lang.Object o) {
    return Util.toIndentedString(o);
  }
}
