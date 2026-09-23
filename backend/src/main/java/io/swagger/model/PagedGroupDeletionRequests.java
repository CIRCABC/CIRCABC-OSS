package io.swagger.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Data transfer object representing a paged (server-side paginated) collection of
 * {@link GroupDeletionRequest} items.
 *
 * <p>This model bundles a single page of group deletion requests together with the
 * total number of matching records across all pages, allowing REST clients to render
 * pagination controls without loading the entire result set at once.
 */
public class PagedGroupDeletionRequests {

  /** The group deletion requests contained in the current page. Never {@code null}. */
  private List<GroupDeletionRequest> data = new ArrayList<>();

  /** Total number of group deletion requests available across all pages. */
  private Long total = null;

  /**
   * Returns the group deletion requests contained in the current page.
   *
   * @return the list of {@link GroupDeletionRequest} items for this page
   */
  public List<GroupDeletionRequest> getData() {
    return data;
  }

  /**
   * Sets the group deletion requests for the current page.
   *
   * @param data the list of {@link GroupDeletionRequest} items to associate with this page
   */
  public void setData(List<GroupDeletionRequest> data) {
    this.data = data;
  }

  /**
   * Returns the total number of group deletion requests available across all pages.
   *
   * @return the total record count, or {@code null} if not set
   */
  public Long getTotal() {
    return total;
  }

  /**
   * Sets the total number of group deletion requests available across all pages.
   *
   * @param total the total record count
   */
  public void setTotal(Long total) {
    this.total = total;
  }

  /**
   * Compares this object with another for equality based on the page {@code data}
   * and {@code total} fields.
   *
   * @param o the object to compare against
   * @return {@code true} if the given object is a {@link PagedGroupDeletionRequests}
   *     with equal {@code data} and {@code total}; {@code false} otherwise
   */
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PagedGroupDeletionRequests pagedNews = (PagedGroupDeletionRequests) o;
    return (
      Objects.equals(this.data, pagedNews.data) &&
      Objects.equals(this.total, pagedNews.total)
    );
  }

  /**
   * Returns a hash code consistent with {@link #equals(java.lang.Object)}, derived
   * from the {@code data} and {@code total} fields.
   *
   * @return the hash code for this object
   */
  @Override
  public int hashCode() {
    return Objects.hash(data, total);
  }

  /**
   * Returns a human-readable string representation of this object, listing its
   * {@code data} and {@code total} fields.
   *
   * @return a string representation of this object
   */
  @Override
  public String toString() {
    return (
      "class PagedNews {\n" +
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
