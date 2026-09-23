package eu.europa.ec.digit.circabc.rest.service;

import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.version.VersionBaseModel;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionType;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Business service implementation for the document check-out / check-in (COCI)
 * workflow on top of the Alfresco repository.
 *
 * <p>This class delegates to Alfresco's {@link CheckOutCheckInService} to manage
 * the working-copy lifecycle of a content node: checking a document out for
 * editing, updating the content of the working copy, cancelling a check-out and
 * checking the working copy back in (creating a new version). It is used by the
 * CIRCABC REST layer to expose these operations to callers.</p>
 */
public class CociContentBusinessImpl implements CociContentBusinessSrv {

  /** Alfresco service that performs the actual check-out / check-in operations. */
  @Autowired
  private CheckOutCheckInService checkOutCheckInService;

  /** Alfresco node service used to read node properties (e.g. the content URL). */
  @Autowired
  private NodeService nodeService;

  /** Alfresco content service used to obtain writers for updating node content. */
  @Autowired
  private ContentService contentService;

  /**
   * Checks out the given document, creating a working copy that can be edited
   * independently of the original.
   *
   * @param nodeRef the reference of the document to check out
   * @return the {@link NodeRef} of the newly created working copy
   */
  @Override
  public NodeRef checkOut(NodeRef nodeRef) {
    return checkOutCheckInService.checkout(nodeRef);
  }

  /**
   * Replaces the content of the given document with the supplied stream,
   * optionally updating its MIME type.
   *
   * <p>Typically invoked on a working copy while it is checked out, so that the
   * new content is persisted when the working copy is subsequently checked in.</p>
   *
   * @param document    the node whose content is to be updated
   * @param inputStream the stream providing the new content
   * @param mimeType    the MIME type to assign to the content; when {@code null}
   *                    the existing MIME type is left unchanged, which allows a
   *                    different type of file to be uploaded when supplied
   */
  @Override
  public void update(
    NodeRef document,
    InputStream inputStream,
    String mimeType
  ) {
    final ContentWriter writer = contentService.getWriter(
      document,
      ContentModel.PROP_CONTENT,
      true
    );

    // also update the mime type in case a different type of file is uploaded
    if (mimeType != null) {
      writer.setMimetype(mimeType);
    }
    writer.putContent(inputStream);
  }

  /**
   * Returns the working copy associated with the given original document, if one
   * currently exists.
   *
   * @param orginalNodeRef the reference of the original (checked-out) document
   * @return the {@link NodeRef} of the working copy, or {@code null} if the
   *         document is not currently checked out
   */
  @Override
  public NodeRef getWorkingCopy(NodeRef orginalNodeRef) {
    return checkOutCheckInService.getWorkingCopy(orginalNodeRef);
  }

  /**
   * Cancels a check-out, discarding the working copy and its unsaved changes and
   * restoring the original document to its editable state.
   *
   * @param workingCopyRef the reference of the working copy to discard
   * @return the {@link NodeRef} of the original document
   */
  @Override
  public NodeRef cancelCheckOut(NodeRef workingCopyRef) {
    return checkOutCheckInService.cancelCheckout(workingCopyRef);
  }

  /**
   * Checks the given working copy back in, applying its content to the original
   * document and creating a new version.
   *
   * <p>The supplied version note is stored as the version description and the
   * {@code minor} flag determines whether a minor or major version increment is
   * recorded. The content of the new version is taken from the working copy's
   * current content.</p>
   *
   * @param workingCopy  the reference of the working copy to check in
   * @param minor        {@code true} to record a minor version increment,
   *                     {@code false} for a major increment
   * @param versionNote  the description to associate with the new version; when
   *                     {@code null} an empty description is stored
   * @param keepCheckOut {@code true} to keep the document checked out (retaining
   *                     the working copy) after check-in, {@code false} to
   *                     release the check-out
   * @return the {@link NodeRef} of the original document after check-in
   */
  @Override
  public NodeRef checkIn(
    final NodeRef workingCopy,
    final boolean minor,
    final String versionNote,
    boolean keepCheckOut
  ) {
    // add version history text to props
    final Map<String, Serializable> props = new HashMap<>(1, 1.0f);
    props.put(Version.PROP_DESCRIPTION, versionNote == null ? "" : versionNote);

    // set the flag for minor or major change
    if (minor) {
      props.put(VersionBaseModel.PROP_VERSION_TYPE, VersionType.MINOR);
    } else {
      props.put(VersionBaseModel.PROP_VERSION_TYPE, VersionType.MAJOR);
    }

    final ContentData contentData = (ContentData) getNodeService().getProperty(
      workingCopy,
      ContentModel.PROP_CONTENT
    );
    final String contentUrl = (contentData == null
      ? null
      : contentData.getContentUrl());

    return checkOutCheckInService.checkin(
      workingCopy,
      props,
      contentUrl,
      keepCheckOut
    );
  }

  /**
   * Provides access to the injected Alfresco node service for subclasses.
   *
   * @return the {@link NodeService} instance
   */
  protected final NodeService getNodeService() {
    return nodeService;
  }
}
