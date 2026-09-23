package io.swagger.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * DTO representing a paginated collection of {@link AppMessage} items.
 *
 * <p>It bundles the page of application messages returned to the client together with the total
 * number of matching messages available on the server side (ignoring paging). This allows the UI
 * to render the current page while still being able to compute the overall number of pages.
 */
public class PagedAppMessages {

  /** The application messages contained in the current page. Never {@code null}. */
  private List<AppMessage> data = new ArrayList<>();

  /** The total number of matching messages across all pages, ignoring paging. */
  private Long total = null;

  /**
   * Returns the application messages contained in the current page.
   *
   * @return the list of {@link AppMessage} items for this page
   */
  public List<AppMessage> getData() {
    return data;
  }

  /**
   * Sets the application messages contained in the current page.
   *
   * @param data the list of {@link AppMessage} items to expose for this page
   */
  public void setData(List<AppMessage> data) {
    this.data = data;
  }

  /**
   * Returns the total amount of matching messages, without paging, so the UI can compute the number
   * of pages.
   *
   * @return the total number of matching messages across all pages
   */
  public Long getTotal() {
    return total;
  }

  /**
   * Sets the total amount of matching messages available without paging.
   *
   * @param total the total number of matching messages across all pages
   */
  public void setTotal(Long total) {
    this.total = total;
  }

  /**
   * Compares this object with another for equality based on the {@code data} and {@code total}
   * fields.
   *
   * @param o the object to compare with
   * @return {@code true} if the given object is a {@code PagedAppMessages} with equal fields,
   *     {@code false} otherwise
   */
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PagedAppMessages pagedNodes = (PagedAppMessages) o;
    return (
      Objects.equals(this.data, pagedNodes.data) &&
      Objects.equals(this.total, pagedNodes.total)
    );
  }

  /**
   * Returns a hash code consistent with {@link #equals(Object)}, derived from the {@code data} and
   * {@code total} fields.
   *
   * @return the hash code for this object
   */
  @Override
  public int hashCode() {
    return Objects.hash(data, total);
  }

  /**
   * Returns a human-readable, multi-line string representation of this object.
   *
   * @return a string describing the {@code data} and {@code total} fields
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
