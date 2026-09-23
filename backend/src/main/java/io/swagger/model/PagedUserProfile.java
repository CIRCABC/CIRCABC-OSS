package io.swagger.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A paginated container for {@link UserProfile} results.
 *
 * <p>This DTO wraps a page of user profiles together with the total number of
 * matching profiles (ignoring paging). It is typically serialized to JSON and
 * returned by REST endpoints that expose user profile listings, allowing the
 * client UI to render the current page while computing the overall number of
 * pages from {@link #getTotal()}.
 */
public class PagedUserProfile {

  /** The user profiles contained in the current page. Never {@code null}. */
  private List<UserProfile> data = new ArrayList<>();

  /** The total number of matching profiles across all pages, ignoring paging. */
  private Integer total = null;

  /**
   * Sets the page of user profiles and returns this instance for chaining.
   *
   * @param data the list of user profiles for the current page
   * @return this {@code PagedUserProfile} instance
   */
  public PagedUserProfile data(List<UserProfile> data) {
    this.data = data;
    return this;
  }

  /**
   * Appends a single user profile to the current page and returns this instance
   * for chaining.
   *
   * @param dataItem the user profile to add
   * @return this {@code PagedUserProfile} instance
   */
  public PagedUserProfile addDataItem(UserProfile dataItem) {
    this.data.add(dataItem);
    return this;
  }

  /**
   * Returns the user profiles contained in the current page.
   *
   * @return the list of user profiles for the current page
   */
  public List<UserProfile> getData() {
    return data;
  }

  /**
   * Sets the page of user profiles.
   *
   * @param data the list of user profiles for the current page
   */
  public void setData(List<UserProfile> data) {
    this.data = data;
  }

  /**
   * Sets the total number of matching profiles and returns this instance for
   * chaining.
   *
   * @param total the total number of matching profiles, ignoring paging
   * @return this {@code PagedUserProfile} instance
   */
  public PagedUserProfile total(Integer total) {
    this.total = total;
    return this;
  }

  /**
   * return the total amount of nodes, without paging so the UI can compute the number of pages
   *
   * @return total
   */
  public Integer getTotal() {
    return total;
  }

  /**
   * Sets the total number of matching profiles, ignoring paging.
   *
   * @param total the total number of matching profiles
   */
  public void setTotal(Integer total) {
    this.total = total;
  }

  /**
   * Compares this object with another for equality based on the page contents
   * and the total count.
   *
   * @param o the object to compare with
   * @return {@code true} if the given object is a {@code PagedUserProfile} with
   *     equal {@code data} and {@code total}; {@code false} otherwise
   */
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PagedUserProfile pagedUserProfile = (PagedUserProfile) o;
    return (
      Objects.equals(this.data, pagedUserProfile.data) &&
      Objects.equals(this.total, pagedUserProfile.total)
    );
  }

  /**
   * Returns a hash code consistent with {@link #equals(Object)}.
   *
   * @return the hash code derived from {@code data} and {@code total}
   */
  @Override
  public int hashCode() {
    return Objects.hash(data, total);
  }

  /**
   * Returns a human-readable representation of this object, listing its fields.
   *
   * @return a string representation of this {@code PagedUserProfile}
   */
  @Override
  public String toString() {
    return (
      "class PagedUserProfile {\n" +
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
   * @param o the object to convert
   * @return the indented string representation of the object
   */
  private String toIndentedString(java.lang.Object o) {
    return Util.toIndentedString(o);
  }
}
