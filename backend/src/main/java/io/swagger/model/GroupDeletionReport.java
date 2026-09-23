/**
 *
 */
package io.swagger.model;

import java.util.List;

/**
 * Data transfer object summarizing the blocking conditions that prevent a group (or interest
 * group) from being deleted.
 *
 * <p>When a deletion is requested, the platform inspects the resources that still depend on the
 * group and reports them back so the caller can resolve them before retrying. The report groups
 * these dependencies into three categories:
 *
 * <ul>
 *   <li>{@link #lockedNodes} — nodes that are currently locked and therefore cannot be removed;
 *   <li>{@link #sharedFolders} — folders that are shared with other groups and would be affected;
 *   <li>{@link #sharedProfiles} — profiles that are shared and still reference the group.
 * </ul>
 *
 * @author beaurpi
 */
public class GroupDeletionReport {

  /** Nodes that are locked and thus block the group deletion. */
  private List<Node> lockedNodes;

  /** Folders shared with other groups that would be impacted by the deletion. */
  private List<Node> sharedFolders;

  /** Shared profiles that still reference the group being deleted. */
  private List<Profile> sharedProfiles;

  /** @return the lockedNodes */
  public List<Node> getLockedNodes() {
    return lockedNodes;
  }

  /** @param lockedNodes the lockedNodes to set */
  public void setLockedNodes(List<Node> lockedNodes) {
    this.lockedNodes = lockedNodes;
  }

  /** @return the sharedFolders */
  public List<Node> getSharedFolders() {
    return sharedFolders;
  }

  /** @param sharedFolders the sharedFolders to set */
  public void setSharedFolders(List<Node> sharedFolders) {
    this.sharedFolders = sharedFolders;
  }

  /** @return the sharedProfiles */
  public List<Profile> getSharedProfiles() {
    return sharedProfiles;
  }

  /** @param sharedProfiles the sharedProfiles to set */
  public void setSharedProfiles(List<Profile> sharedProfiles) {
    this.sharedProfiles = sharedProfiles;
  }
}
