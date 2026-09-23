package io.swagger.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Data transfer object describing the "Information" service page of a CIRCABC
 * Interest Group.
 *
 * <p>An instance carries the configuration of the Information landing page: the
 * URL of the published content, whether the content should be adapted, whether
 * older information should still be displayed, and the per-profile permission
 * map that governs access to the page. It is serialized to and from JSON when
 * exchanged with the Angular frontend through the REST API.
 */
public class InformationPage {

  /** URL of the published Information page content. */
  private String url = null;

  /** Whether the Information page content should be adapted. */
  private Boolean adapt = null;

  /** Whether previously published (old) information should still be displayed. */
  private Boolean displayOldInformation = false;

  /** Map of profile identifiers to the permission granted on this page. */
  private Map<String, String> permissions = new HashMap<>();

  /**
   * Get url
   *
   * @return url
   */
  public String getUrl() {
    return url;
  }

  /**
   * Sets the URL of the published Information page content.
   *
   * @param url the URL to set
   */
  public void setUrl(String url) {
    this.url = url;
  }

  /**
   * Get adapt
   *
   * @return adapt
   */
  public Boolean getAdapt() {
    return adapt;
  }

  /**
   * Sets whether the Information page content should be adapted.
   *
   * @param adapt the adapt flag to set
   */
  public void setAdapt(Boolean adapt) {
    this.adapt = adapt;
  }

  /**
   * Compares this Information page to another object for equality based on the
   * URL, adapt flag, display-old-information flag and permission map.
   *
   * @param o the object to compare with
   * @return {@code true} if the given object is an equivalent
   *         {@code InformationPage}, {@code false} otherwise
   */
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    InformationPage informationPage = (InformationPage) o;
    return (
      Objects.equals(this.url, informationPage.url) &&
      Objects.equals(this.adapt, informationPage.adapt) &&
      Objects.equals(
        this.displayOldInformation,
        informationPage.displayOldInformation
      ) &&
      Objects.equals(this.permissions, informationPage.permissions)
    );
  }

  /**
   * Returns a hash code consistent with {@link #equals(Object)}.
   *
   * @return the hash code for this Information page
   */
  @Override
  public int hashCode() {
    return Objects.hash(url, adapt, permissions);
  }

  /**
   * Returns a human-readable, indented string representation of this
   * Information page.
   *
   * @return a string representation of this object
   */
  @Override
  public String toString() {
    return (
      "class InformationPage {\n" +
      "    url: " +
      toIndentedString(url) +
      "\n" +
      "    adapt: " +
      toIndentedString(adapt) +
      "\n" +
      "    displayOldInformation: " +
      toIndentedString(displayOldInformation) +
      "\n" +
      "    permissions: " +
      toIndentedString(permissions) +
      "\n" +
      "}"
    );
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first line).
   *
   * @param o the object to render
   * @return the indented string representation
   */
  private String toIndentedString(java.lang.Object o) {
    return Util.toIndentedString(o);
  }

  /**
   * @return the permissions
   */
  public Map<String, String> getPermissions() {
    return permissions;
  }

  /**
   * @param permissions the permissions to set
   */
  public void setPermissions(Map<String, String> permissions) {
    this.permissions = permissions;
  }

  /**
   * Returns whether previously published (old) information should still be
   * displayed.
   *
   * @return the displayOldInformation flag
   */
  public Boolean getDisplayOldInformation() {
    return displayOldInformation;
  }

  /**
   * Sets whether previously published (old) information should still be
   * displayed.
   *
   * @param displayOldInformation the displayOldInformation flag to set
   */
  public void setDisplayOldInformation(Boolean displayOldInformation) {
    this.displayOldInformation = displayOldInformation;
  }
}
