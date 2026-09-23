package eu.europa.ec.digit.circabc.rest.service;

import java.io.InputStream;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Business service that encapsulates the Alfresco Check-Out/Check-In (COCI)
 * versioning workflow for content nodes.
 *
 * <p>Implementations wrap the repository check-out/check-in operations used to
 * safely edit a document: a node is checked out to produce a working copy, the
 * working copy content is updated, and the changes are either committed back to
 * the original node via a check-in (creating a new version) or discarded by
 * cancelling the check-out.
 */
public interface CociContentBusinessSrv {
  /**
   * Checks out the given content node, creating a working copy that can be
   * edited independently of the original.
   *
   * @param nodeRef the reference to the original content node to check out
   * @return the reference to the newly created working copy
   */
  NodeRef checkOut(NodeRef nodeRef);

  /**
   * Replaces the content of a working copy with the supplied stream.
   *
   * @param workingCopyRef the reference to the working copy to update
   * @param inputStream the stream providing the new content
   * @param mimeType the MIME type to associate with the new content
   */
  void update(NodeRef workingCopyRef, InputStream inputStream, String mimeType);

  /**
   * Returns the working copy currently associated with the given original node.
   *
   * @param orginalNodeRef the reference to the original checked-out node
   * @return the reference to the associated working copy, or {@code null} if
   *     the node has no working copy
   */
  NodeRef getWorkingCopy(NodeRef orginalNodeRef);

  /**
   * Cancels the check-out for the given working copy, discarding any changes and
   * releasing the lock on the original node.
   *
   * @param workingCopyRef the reference to the working copy whose check-out is
   *     to be cancelled
   * @return the reference to the original node that was unlocked
   */
  NodeRef cancelCheckOut(NodeRef workingCopyRef);

  /**
   * Checks in the given working copy, committing its content back to the
   * original node as a new version.
   *
   * @param workingCopyRef the reference to the working copy to check in
   * @param minor {@code true} to create a minor version increment, {@code false}
   *     to create a major version increment
   * @param s the version history comment / description associated with the
   *     check-in
   * @param keepCheckOut {@code true} to keep the node checked out (retaining the
   *     working copy) after the check-in, {@code false} to release it
   * @return the reference to the checked-in original node
   */
  NodeRef checkIn(
    NodeRef workingCopyRef,
    boolean minor,
    String s,
    boolean keepCheckOut
  );
}
