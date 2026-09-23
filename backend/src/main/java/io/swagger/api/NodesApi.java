package io.swagger.api;

import io.swagger.model.InterestGroup;
import io.swagger.model.Node;
import java.util.List;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Business-logic contract for operations on repository nodes (documents, folders, topics, posts,
 * etc.) within CIRCABC.
 *
 * <p>Implementations translate between Alfresco {@link NodeRef} references and the REST-layer
 * {@link Node} domain model, resolve node hierarchies (paths), manage node ownership, derive the
 * owning Interest Group of a node, and provide helper utilities for building unique node names and
 * handling file-name extensions. It is typically injected into the web-script endpoint classes that
 * expose node-related REST resources.
 *
 * @author beaurpi
 */
public interface NodesApi {
  /**
   * Builds the {@link Node} domain model for the given repository reference.
   *
   * @param nodeRef the reference of the repository node to load
   * @return the populated {@link Node} representation of the referenced node
   */
  Node getNode(final NodeRef nodeRef);

  /**
   * Populates the supplied {@link Node} instance with the data of the given repository reference.
   *
   * @param nodeRef the reference of the repository node to load
   * @param node the {@link Node} instance to populate and return
   * @return the {@link Node} instance enriched with the referenced node's data
   */
  Node getNode(final NodeRef nodeRef, Node node);

  /**
   * Retrieves a node by its identifier.
   *
   * @param id the identifier of the node to retrieve
   * @return the {@link Node} matching the given identifier
   */
  Node getNodeById(String id);

  /**
   * Resolves the ancestor path of the given node, from the root down to the node itself.
   *
   * @param id the identifier of the node whose path is requested
   * @return the ordered list of {@link Node} elements composing the node's path
   */
  List<Node> getPathByNode(String id);

  /**
   * Updates the ownership of the node so that the currently authenticated Alfresco user becomes its
   * owner.
   *
   * @param id the identifier of the node whose ownership is transferred
   * @return the updated {@link Node} reflecting the new ownership
   */
  Node nodesIdOwnershipPut(String id);

  /**
   * Generates a name that is unique among the children of the given parent, deriving it from the
   * provided candidate name (for example by appending a numeric suffix when needed).
   *
   * @param parent the reference of the parent node under which uniqueness must hold
   * @param candidateName the desired base name
   * @return a name guaranteed not to clash with existing children of the parent
   */
  String generateUniqueName(final NodeRef parent, final String candidateName);

  /**
   * Extracts the file-name extension from the given file name.
   *
   * @param fileName the file name to inspect
   * @return the extension portion of the file name, or an empty value when none is present
   */
  String getFileNameExtension(final String fileName);

  /**
   * Removes the extension from the given file name, returning only its base part.
   *
   * @param fileName the file name to strip
   * @return the file name without its extension
   */
  String removeFileNameExtension(final String fileName);

  /**
   * Resolves the Interest Group that owns the given node.
   *
   * @param id the identifier of the node whose owning Interest Group is requested
   * @return the {@link InterestGroup} the node belongs to
   * @throws InvalidArgumentException if the identifier is invalid or the node has no resolvable
   *     Interest Group
   */
  InterestGroup nodesIdGroupGet(String id) throws InvalidArgumentException;

  /**
   * Reads the original (source) node reference stored on a node via the
   * {@code ci:migrated} aspect. Returns {@code null} if the node is not migrated.
   * Intended for CIRCABC administrators.
   *
   * @param id the node id
   * @return the original (source) node reference, or {@code null}
   */
  String getOriginalNodeRef(String id);

  /**
   * Set (create or update) the original (source) node reference on a node,
   * applying the {@code ci:migrated} aspect. Intended for CIRCABC administrators.
   *
   * @param id the node id
   * @param originalNodeRef the original (source) node reference to store
   * @return the updated node
   */
  Node setOriginalNodeRef(String id, String originalNodeRef);

  /**
   * Remove the {@code ci:migrated} aspect (and its {@code ci:originalNodeRef}
   * property) from a node. Intended for CIRCABC administrators.
   *
   * @param id the node id
   */
  void deleteOriginalNodeRef(String id);

  /**
   * Resolve the migrated node whose {@code ci:originalNodeRef} matches the given
   * original node id (a {@code workspace://SpacesStore} uuid). Available to any user,
   * but the current user must have READ permission on the resolved node.
   *
   * @param originalId the original (source) node uuid
   * @return the migrated node, or {@code null} if no node references the original id
   * @throws org.alfresco.repo.security.permissions.AccessDeniedException if a node is
   *     found but the current user has no READ permission on it
   */
  Node resolveByOriginalNodeRef(String originalId);
}
