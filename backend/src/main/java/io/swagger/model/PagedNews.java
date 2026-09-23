package io.swagger.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Data transfer object representing a paginated collection of {@link News} items.
 *
 * <p>It bundles a single page of news entries ({@link #getData()}) together with the
 * total number of news items available across all pages ({@link #getTotal()}), so that
 * REST clients can render paginated news listings and pagination controls.
 */
@jakarta.annotation.Generated(
  value = "io.swagger.codegen.languages.SpringCodegen",
  date = "2017-06-28T14:35:39.431+02:00"
)
public class PagedNews {

  /** The news items contained in the current page. */
  private List<News> data = new ArrayList<>();

  /** The total number of news items available across all pages. */
  private Long total = null;

  /**
   * Returns the news items contained in the current page.
   *
   * @return the list of news items for this page; never {@code null}
   */
  public List<News> getData() {
    return data;
  }

  /**
   * Sets the news items for the current page.
   *
   * @param data the list of news items to expose for this page
   */
  public void setData(List<News> data) {
    this.data = data;
  }

  /**
   * Returns the total number of news items available across all pages.
   *
   * @return the total item count, or {@code null} if not set
   */
  public Long getTotal() {
    return total;
  }

  /**
   * Sets the total number of news items available across all pages.
   *
   * @param total the total item count
   */
  public void setTotal(Long total) {
    this.total = total;
  }

  /**
   * Compares this object with another for equality based on the page data and
   * the total item count.
   *
   * @param o the object to compare with
   * @return {@code true} if the given object is a {@code PagedNews} with equal
   *     {@code data} and {@code total}; {@code false} otherwise
   */
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PagedNews pagedNews = (PagedNews) o;
    return (
      Objects.equals(this.data, pagedNews.data) &&
      Objects.equals(this.total, pagedNews.total)
    );
  }

  /**
   * Returns a hash code consistent with {@link #equals(java.lang.Object)}, derived
   * from the page data and the total item count.
   *
   * @return the hash code for this object
   */
  @Override
  public int hashCode() {
    return Objects.hash(data, total);
  }

  /**
   * Returns a human-readable, indented string representation of this object,
   * primarily intended for debugging and logging.
   *
   * @return a string describing the page data and total item count
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PagedNews {\n");

    sb.append("    data: ").append(toIndentedString(data)).append("\n");
    sb.append("    total: ").append(toIndentedString(total)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first line).
   *
   * @param o the object to render; may be {@code null}
   * @return the indented string representation of the given object
   */
  private String toIndentedString(java.lang.Object o) {
    return Util.toIndentedString(o);
  }
}
