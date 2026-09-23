package io.swagger.model;

import java.util.List;
import org.alfresco.util.Pair;

/**
 * Data holder that couples a set of Interest Groups with the permissions to be
 * applied when content is shared with them.
 *
 * <p>Instances of this class are typically used to describe a sharing request:
 * the {@code igs} identify the target Interest Groups and {@code permissions}
 * enumerate the permission levels that should be granted to those groups.
 */
public class ShareIGsAndPermissions {

  /**
   * The target Interest Groups, each represented as a {@link Pair} (for example
   * an identifier paired with a name or another related attribute).
   */
  List<Pair<String, String>> igs;

  /** The permission levels to grant to the associated Interest Groups. */
  List<String> permissions;

  /**
   * Creates a new holder associating a set of Interest Groups with the
   * permissions to be applied to them.
   *
   * @param igs the target Interest Groups
   * @param permissions the permission levels to grant to those groups
   */
  public ShareIGsAndPermissions(
    List<Pair<String, String>> igs,
    List<String> permissions
  ) {
    super();
    this.igs = igs;
    this.permissions = permissions;
  }

  /**
   * @return the igs
   */
  public List<Pair<String, String>> getIgs() {
    return igs;
  }

  /**
   * @param igs the igs to set
   */
  public void setIgs(List<Pair<String, String>> igs) {
    this.igs = igs;
  }

  /**
   * @return the permissions
   */
  public List<String> getPermissions() {
    return permissions;
  }

  /**
   * @param permissions the permissions to set
   */
  public void setPermissions(List<String> permissions) {
    this.permissions = permissions;
  }
}
