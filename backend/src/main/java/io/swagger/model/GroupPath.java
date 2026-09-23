/**
 *
 */
package io.swagger.model;

import java.util.Objects;

/**
 * Data transfer object describing the location of an Interest Group within the CIRCABC hierarchy.
 *
 * <p>A {@code GroupPath} captures the chain of containing entities that lead to an Interest Group,
 * namely the {@link Header}, the {@link Category} and the {@link InterestGroup} itself. It is used
 * to represent the full navigational path (Header &rarr; Category &rarr; Interest Group) when
 * serializing group information in the REST API.
 *
 * @author beaurpi
 */
public class GroupPath {

  /** The top-level header that contains the category of this path. */
  private Header header;
  /** The category, nested under the header, that contains the interest group. */
  private Category category;
  /** The interest group located at the end of this path. */
  private InterestGroup group;

  /** @return the header */
  public Header getHeader() {
    return header;
  }

  /** @param header the header to set */
  public void setHeader(Header header) {
    this.header = header;
  }

  /** @return the category */
  public Category getCategory() {
    return category;
  }

  /** @param category the category to set */
  public void setCategory(Category category) {
    this.category = category;
  }

  /** @return the group */
  public InterestGroup getGroup() {
    return group;
  }

  /** @param group the group to set */
  public void setGroup(InterestGroup group) {
    this.group = group;
  }

  /**
   * Compares this path to another object for equality.
   *
   * <p>Two {@code GroupPath} instances are considered equal when their header, category and group
   * are all equal.
   *
   * @param o the object to compare with
   * @return {@code true} if the given object is a {@code GroupPath} with equal header, category and
   *     group; {@code false} otherwise
   */
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GroupPath groupPath = (GroupPath) o;
    return (
      Objects.equals(this.header, groupPath.header) &&
      Objects.equals(this.category, groupPath.category) &&
      Objects.equals(this.group, groupPath.group)
    );
  }

  /**
   * Returns a hash code consistent with {@link #equals(Object)}, derived from the header, category
   * and group.
   *
   * @return the hash code for this path
   */
  @Override
  public int hashCode() {
    return Objects.hash(header, category, group);
  }

  /**
   * Returns a human-readable, multi-line representation of this path listing its header, category
   * and group.
   *
   * @return a string representation of this {@code GroupPath}
   */
  @Override
  public String toString() {
    return (
      "class GroupPath {\n" +
      "    header: " +
      toIndentedString(header) +
      "\n" +
      "    category: " +
      toIndentedString(category) +
      "\n" +
      "    group: " +
      toIndentedString(group) +
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
