package eu.europa.ec.digit.circabc.rest.dynamic.authority;

/**
 * Simple value holder describing the permission level granted to an authority
 * across the CIRCABC Interest Group services.
 *
 * <p>An Interest Group exposes several distinct services (Library, Newsgroup
 * and Information), and an authority (user or group) can be assigned an
 * independent permission on each of them. This class aggregates those
 * per-service permission values into a single object so they can be carried
 * together through the dynamic authority layer.
 *
 * <p>Each permission is represented as a {@code String} corresponding to an
 * Alfresco permission/role name (for example a Library, Newsgroup or
 * Information role). A {@code null} value indicates that no specific permission
 * is set for the related service.
 */
public class CircabcPermission {

  /** Permission (Alfresco role name) granted for the Library service. */
  private String libraryPermission;

  /**
   * Returns the permission granted for the Library service.
   *
   * @return the Library permission/role name, or {@code null} if none is set
   */
  public String getLibraryPermission() {
    return libraryPermission;
  }

  /**
   * Sets the permission granted for the Library service.
   *
   * @param libraryPermission the Library permission/role name to assign; may be
   *     {@code null} to indicate no specific permission
   */
  public void setLibraryPermission(String libraryPermission) {
    this.libraryPermission = libraryPermission;
  }

  /** Permission (Alfresco role name) granted for the Newsgroup service. */
  private String newsGroupPermission;

  /**
   * Returns the permission granted for the Newsgroup service.
   *
   * @return the Newsgroup permission/role name, or {@code null} if none is set
   */
  public String getNewsGroupPermission() {
    return newsGroupPermission;
  }

  /**
   * Sets the permission granted for the Newsgroup service.
   *
   * @param newsGroupPermission the Newsgroup permission/role name to assign; may
   *     be {@code null} to indicate no specific permission
   */
  public void setNewsGroupPermission(String newsGroupPermission) {
    this.newsGroupPermission = newsGroupPermission;
  }

  /** Permission (Alfresco role name) granted for the Information service. */
  private String informationPermission;

  /**
   * Returns the permission granted for the Information service.
   *
   * @return the Information permission/role name, or {@code null} if none is set
   */
  public String getInformationPermission() {
    return informationPermission;
  }

  /**
   * Sets the permission granted for the Information service.
   *
   * @param informationPermission the Information permission/role name to assign;
   *     may be {@code null} to indicate no specific permission
   */
  public void setInformationPermission(String informationPermission) {
    this.informationPermission = informationPermission;
  }
}
