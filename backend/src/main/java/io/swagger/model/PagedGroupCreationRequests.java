package io.swagger.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Data transfer object representing a paginated collection of
 * {@link GroupCreationRequest} items.
 *
 * <p>It couples the current page of group-creation requests ({@link #data})
 * with the total number of requests available across all pages
 * ({@link #total}), allowing REST clients to render paginated views.</p>
 */
public class PagedGroupCreationRequests {

  /** The group-creation requests contained in the current page. */
  private List<GroupCreationRequest> data = new ArrayList<>();

  /** Total number of group-creation requests available across all pages. */
  private Long total = null;

  /**
   * Returns the group-creation requests contained in the current page.
   *
   * @return the list of group-creation requests; never {@code null}
   */
  public List<GroupCreationRequest> getData() {
    return data;
  }

  /**
   * Sets the group-creation requests for the current page.
   *
   * @param data the list of group-creation requests to set
   */
  public void setData(List<GroupCreationRequest> data) {
    this.data = data;
  }

  /**
   * Returns the total number of group-creation requests available across all
   * pages.
   *
   * @return the total count, or {@code null} if not set
   */
  public Long getTotal() {
    return total;
  }

  /**
   * Sets the total number of group-creation requests available across all
   * pages.
   *
   * @param total the total count to set
   */
  public void setTotal(Long total) {
    this.total = total;
  }

  /**
   * Compares this object with another for equality based on the {@link #data}
   * and {@link #total} fields.
   *
   * @param o the object to compare with
   * @return {@code true} if the given object is a
   *         {@code PagedGroupCreationRequests} with equal fields, {@code false}
   *         otherwise
   */
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PagedGroupCreationRequests pagedNews = (PagedGroupCreationRequests) o;
    return (
      Objects.equals(this.data, pagedNews.data) &&
      Objects.equals(this.total, pagedNews.total)
    );
  }

  /**
   * Returns a hash code consistent with {@link #equals(java.lang.Object)},
   * derived from the {@link #data} and {@link #total} fields.
   *
   * @return the hash code for this object
   */
  @Override
  public int hashCode() {
    return Objects.hash(data, total);
  }

  /**
   * Returns a human-readable string representation of this object, listing its
   * {@link #data} and {@link #total} fields.
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
   *
   * @param o the object to convert to an indented string
   * @return the indented string representation of the given object
   */
  private String toIndentedString(java.lang.Object o) {
    return Util.toIndentedString(o);
  }
}
