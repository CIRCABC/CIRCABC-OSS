package io.swagger.api;

import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Business service contract for clipboard-style operations on repository nodes.
 *
 * <p>Implementations perform copy, move and link operations, transferring one or more source nodes
 * into a destination folder. The concrete operation is selected by an {@code action} code passed to
 * the {@code paste} methods.
 *
 * @author schwerr
 */
public interface ClipboardApi {
  /**
   * Pastes a single node into the destination folder. Depending on the requested {@code action},
   * the node is copied, linked or moved.
   *
   * @param nodeRef the reference of the source node to paste
   * @param destRef the reference of the destination folder that will receive the node
   * @param action the operation to perform (copy, link or move)
   * @throws FileNotFoundException if the source node or the destination folder cannot be resolved
   */
  void paste(final NodeRef nodeRef, final NodeRef destRef, final int action)
    throws FileNotFoundException;

  /**
   * Pastes a list of nodes into the destination folder. Depending on the requested {@code action},
   * each node is copied, linked or moved.
   *
   * @param nodeIds the identifiers of the source nodes to paste
   * @param destRef the reference of the destination folder that will receive the nodes
   * @param action the operation to perform (copy, link or move)
   * @throws FileNotFoundException if a source node or the destination folder cannot be resolved
   */
  void paste(final String[] nodeIds, final NodeRef destRef, final int action)
    throws FileNotFoundException;
}
