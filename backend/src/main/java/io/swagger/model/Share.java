package io.swagger.model;

/**
 * Domain model describing how a content node (or resource) is shared with an
 * Interest Group.
 *
 * <p>Each instance associates a single Interest Group with the permission level
 * granted to it, pairing the group's identifier and display name with the
 * permission string. It is a plain data holder used to carry sharing
 * information through the CIRCABC REST layer.
 */
public class Share {

  /** Identifier of the Interest Group the resource is shared with. */
  String igId;

  /** Human-readable name of the Interest Group the resource is shared with. */
  String igName;

  /** Permission level granted to the Interest Group (e.g. read or write). */
  String permission;

  /**
   * Creates a new share association between an Interest Group and a permission
   * level.
   *
   * @param igId the identifier of the Interest Group
   * @param igName the display name of the Interest Group
   * @param permission the permission level granted to the Interest Group
   */
  public Share(String igId, String igName, String permission) {
    super();
    this.igId = igId;
    this.igName = igName;
    this.permission = permission;
  }

  /**
   * @return the igId
   */
  public String getIgId() {
    return igId;
  }

  /**
   * @param igId the igId to set
   */
  public void setIgId(String igId) {
    this.igId = igId;
  }

  /**
   * @return the igName
   */
  public String getIgName() {
    return igName;
  }

  /**
   * @param igName the igName to set
   */
  public void setIgName(String igName) {
    this.igName = igName;
  }

  /**
   * @return the permission
   */
  public String getPermission() {
    return permission;
  }

  /**
   * @param permission the permission to set
   */
  public void setPermission(String permission) {
    this.permission = permission;
  }
}
