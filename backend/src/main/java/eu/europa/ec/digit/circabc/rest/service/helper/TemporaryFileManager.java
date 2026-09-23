/*******************************************************************************
 * Copyright 2006 European Community
 *
 *  Licensed under the EUPL, Version 1.1 or - as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  You may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  https://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 ******************************************************************************/
package eu.europa.ec.digit.circabc.rest.service.helper;

import io.swagger.api.CircabcApi;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Helper that manages temporary files stored in the Alfresco repository.
 *
 * <p>Temporary content is created under a dedicated "temp" folder located inside the CIRCABC
 * dictionary node. This manager centralises the lifecycle of those temporary nodes: creating them,
 * determining whether a given node is a temporary file, removing individual temporary files, and
 * purging stale temporary files that have not been accessed within a configurable retention
 * window.
 *
 * @author Yanick Pignot
 */
public class TemporaryFileManager {

  /** Logger used to report non-fatal issues (e.g. concurrent deletion in a clustered setup). */
  private static final Log logger = LogFactory.getLog(
    TemporaryFileManager.class
  );

  @Autowired
  private NodeService nodeService;

  @Autowired
  private FileFolderService fileFolderService;

  @Autowired
  private ContentManager contentManager;

  @Autowired
  private CircabcApi circabcApi;

  /** Name of the folder, created under the CIRCABC dictionary node, that holds temporary files. */
  private String tempRootFolderName = "temp";

  /** Cached reference to the temporary root folder; lazily resolved on first access. */
  private NodeRef tempRootRef;

  /* The time that a file can stay in the tempdirectory before cleanup.
   * Since they are not many temp files created, this can be huge.
   * We have to ensure that the user not longer need to access to it
   **/
  /**
   * Maximum time, in milliseconds, that a file may remain in the temporary folder before it becomes
   * eligible for cleanup by {@link #removeTempFiles()}.
   */
  private long noAccessTime = 1000L * 60L * 60L * 24L; // 24 hours

  /**
   * Creates a temporary file node under the temporary root folder, creating the root folder if it
   * does not yet exist.
   *
   * @param file the file whose content is stored in the newly created node
   * @param name the name to assign to the created content node
   * @return the {@link NodeRef} of the created temporary content node
   */
  public NodeRef createTempFile(final File file, final String name) {
    final NodeRef parent = getTempRoot(true);
    return contentManager.createContent(
      parent,
      name,
      ContentModel.ASSOC_CONTAINS,
      ContentModel.TYPE_CONTENT,
      file,
      true
    );
  }

  /**
   * Removes a single temporary file node.
   *
   * <p>The node is only deleted when it is recognised as a temporary file (see
   * {@link #isTempFile(NodeRef)}). Before deletion the {@code temporary} aspect is applied so the
   * node is permanently removed instead of being moved to the archive store. Any failure is logged
   * as a warning rather than propagated, as it typically indicates concurrent access in a
   * clustered environment.
   *
   * @param nodeRef the reference of the temporary node to remove
   */
  public void removeTempFile(final NodeRef nodeRef) {
    try {
      if (isTempFile(nodeRef)) {
        // ensure that the node will not be moved in the archive store
        nodeService.addAspect(nodeRef, ContentModel.ASPECT_TEMPORARY, null);
        nodeService.deleteNode(nodeRef);
      }
    } catch (Exception t) {
      if (logger.isWarnEnabled()) {
        logger.warn(
          "Impossible to delete temp node " +
            nodeRef +
            ". Certainly due to a cluster concurent access.",
          t
        );
      }
    }
  }

  /**
   * Purges stale temporary files from the temporary root folder.
   *
   * <p>Iterates over every child of the temporary root and removes any node whose last modified
   * date (falling back to its creation date) is older than the configured retention window
   * ({@code noAccessTime}). Nodes without a modified or created date are also removed. Does nothing
   * if the temporary root folder does not exist.
   */
  public void removeTempFiles() {
    final NodeRef tempRoot = getTempRoot(false);

    if (tempRoot != null) {
      final List<NodeRef> contents = new ArrayList<>();

      final List<ChildAssociationRef> childs = nodeService.getChildAssocs(
        tempRoot
      );

      for (final ChildAssociationRef assoc : childs) {
        contents.add(assoc.getChildRef());
      }

      final Date now = new Date(System.currentTimeMillis() - noAccessTime);

      Date modified;
      for (final NodeRef ref : contents) {
        modified = (Date) nodeService.getProperty(
          ref,
          ContentModel.PROP_MODIFIED
        );

        if (modified == null) {
          modified = (Date) nodeService.getProperty(
            ref,
            ContentModel.PROP_CREATED
          );
        }

        if (modified == null || modified.before(now)) {
          removeTempFile(ref);
        }
      }
    }
  }

  /**
   * Determines whether the given node is a temporary file managed by this manager.
   *
   * @param nodeRef the node to test; may be {@code null}
   * @return {@code true} if the node exists and its primary parent is the temporary root folder,
   *     {@code false} otherwise (including when the node is {@code null} or does not exist)
   */
  public boolean isTempFile(final NodeRef nodeRef) {
    if (nodeRef == null || !nodeService.exists(nodeRef)) {
      return false;
    } else {
      final NodeRef tempRoot = getTempRoot(false);
      return (
        tempRoot != null &&
        nodeService.getPrimaryParent(nodeRef).getParentRef().equals(tempRoot)
      );
    }
  }

  /**
   * Returns the temporary root folder, creating it if it does not yet exist.
   *
   * @return the {@link NodeRef} of the temporary root folder
   */
  public NodeRef getTempRoot() {
    return getTempRoot(true);
  }

  /**
   * Resolves the temporary root folder located under the CIRCABC dictionary node.
   *
   * <p>The reference is cached after the first resolution. When the folder does not exist it is
   * created only if {@code createIfMissing} is {@code true}.
   *
   * @param createIfMissing whether the folder should be created when it does not exist
   * @return the {@link NodeRef} of the temporary root folder, or {@code null} if it does not exist
   *     and {@code createIfMissing} is {@code false}
   */
  private NodeRef getTempRoot(boolean createIfMissing) {
    if (tempRootRef == null) {
      final NodeRef cbcDDFolder = circabcApi.getCircabcDictionaryNodeRef();

      tempRootRef = nodeService.getChildByName(
        cbcDDFolder,
        ContentModel.ASSOC_CONTAINS,
        tempRootFolderName
      );

      if (tempRootRef == null && createIfMissing) {
        tempRootRef = fileFolderService
          .create(cbcDDFolder, tempRootFolderName, ContentModel.TYPE_FOLDER)
          .getNodeRef();
      }
    }

    return tempRootRef;
  }

  // create a job that clean all files that are not modified since 24 hours

  /**
   * Sets the name of the temporary root folder.
   *
   * @param name the folder name to use for temporary files
   */
  public void setTempRootFolderName(final String name) {
    this.tempRootFolderName = name;
  }

  /**
   * Sets the retention window after which temporary files become eligible for cleanup.
   *
   * @param millis the retention time in milliseconds
   */
  public void setTimeBeforeDeletion(long millis) {
    this.noAccessTime = millis;
  }
}
