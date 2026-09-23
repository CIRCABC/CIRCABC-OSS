/**
 *
 */
package io.swagger.api;

import io.swagger.model.Node;
import io.swagger.model.Profile;
import java.util.List;

/**
 * Business operations for the Library service of an Interest Group.
 *
 * <p>Defines read operations used to inspect the locking and sharing state of nodes (documents and
 * folders) held in the Library. Implementations contain the Alfresco-specific logic and are wired
 * into the REST webscript layer, which exposes these operations as JSON endpoints.
 *
 * @author beaurpi
 */
public interface LibraryApi {
  /**
   * Returns the nodes that are currently locked within the given Library subtree.
   *
   * @param nodeId identifier of the node (typically a folder or the Library root) whose locked
   *     descendants should be retrieved
   * @return the list of locked {@link Node} items; empty if none are locked
   */
  List<Node> getLockedNodes(String nodeId);

  /**
   * Returns the nodes that are currently shared within the given Library subtree.
   *
   * @param nodeId identifier of the node (typically a folder or the Library root) whose shared
   *     descendants should be retrieved
   * @return the list of shared {@link Node} items; empty if none are shared
   */
  List<Node> getSharedNodes(String nodeId);

  /**
   * Returns the profiles with which the given node is shared.
   *
   * @param nodeId identifier of the node whose sharing profiles should be retrieved
   * @return the list of {@link Profile} entries the node is shared with; empty if not shared
   */
  List<Profile> getSharedProfiles(String nodeId);
}
