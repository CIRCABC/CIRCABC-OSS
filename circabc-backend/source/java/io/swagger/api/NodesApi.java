package io.swagger.api;

import io.swagger.model.InterestGroup;
import io.swagger.model.Node;
import java.util.List;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author beaurpi
 */
public interface NodesApi {
  Node getNode(final NodeRef nodeRef);

  Node getNode(final NodeRef nodeRef, Node node);

  Node getNodeById(String id);

  List<Node> getPathByNode(String id);

  /**
   * used to update the ownership of the node the currently alf authenticated user
   * will take the
   * ownership
   */
  Node nodesIdOwnershipPut(String id);

  String generateUniqueName(final NodeRef parent, final String candidateName);

  String getFileNameExtension(final String fileName);

  String removeFileNameExtension(final String fileName);

  InterestGroup nodesIdGroupGet(String id) throws InvalidArgumentException;

  /**
   * Read the original (source) node reference stored on a node via the
   * {@code ci:migrated} aspect. Returns {@code null} if the node is not migrated.
   * Intended for CIRCABC administrators.
   */
  String getOriginalNodeRef(String id);

  /**
   * Set (create or update) the original (source) node reference on a node,
   * applying the {@code ci:migrated} aspect. Intended for CIRCABC administrators.
   *
   * @return the updated node
   */
  Node setOriginalNodeRef(String id, String originalNodeRef);

  /**
   * Remove the {@code ci:migrated} aspect (and its {@code ci:originalNodeRef}
   * property) from a node. Intended for CIRCABC administrators.
   */
  void deleteOriginalNodeRef(String id);

  /**
   * Resolve the migrated node whose {@code ci:originalNodeRef} matches the given
   * original node id (a workspace://SpacesStore uuid). Available to any user, but
   * the current user must have READ permission on the resolved node.
   *
   * @return the migrated node, or {@code null} if no node references the original id
   * @throws org.alfresco.repo.security.permissions.AccessDeniedException if a node is
   *         found but the current user has no READ permission on it
   */
  Node resolveByOriginalNodeRef(String originalId);
}
