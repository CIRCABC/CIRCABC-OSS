package eu.europa.ec.digit.circabc.rest.service.attachment;

import eu.europa.ec.digit.circabc.rest.service.helper.ContentManager;
import eu.europa.ec.digit.circabc.rest.service.helper.TemporaryFileManager;
import io.swagger.model.Attachement;
import io.swagger.model.AttachementImpl;
import io.swagger.model.alfresco.DocumentModel;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Default {@link AttachmentService} implementation backed by the Alfresco repository.
 *
 * <p>Attachments are modelled as relationships between a "referer" node and a "refered" node.
 * Two flavours of attachment are supported:
 *
 * <ul>
 *   <li><b>External references</b> - links to pre-existing repository nodes, expressed through the
 *       {@link DocumentModel#ASSOC_EXTERNAL_REFERENCES} peer association. Detaching such an
 *       attachment only removes the association; the target node is left untouched.
 *   <li><b>Hidden attachments</b> - content that "belongs" to the referer, stored as child nodes of
 *       type {@link DocumentModel#TYPE_HIDDEN_ATTACHEMENT_CONTENT} under the
 *       {@link DocumentModel#ASSOC_HIDDEN_REFERENCES} child association. Detaching such an
 *       attachment deletes the underlying node.
 * </ul>
 *
 * <p>Any node that holds attachments is tagged with the {@link DocumentModel#ASPECT_ATTACHABLE}
 * aspect.
 */
public class AttachmentServiceImpl implements AttachmentService {

  /** Alfresco service used to read and mutate nodes, aspects and associations. */
  @Autowired
  private NodeService nodeService;

  /** Helper used to create new content nodes from uploaded files. */
  @Autowired
  private ContentManager contentManager;

  /** Helper used to detect and manage temporary (not yet persisted) upload files. */
  @Autowired
  private TemporaryFileManager temporaryFileManager;

  /**
   * {@inheritDoc}
   *
   * <p>If the referred node is a hidden attachment it is deleted from the repository; otherwise only
   * the external reference association linking the two nodes is removed. Nodes that are not actually
   * attached to the referer are ignored.
   *
   * @param referer the node that holds the attachment reference
   * @param refered the attached node to detach from the referer
   */
  @Override
  public void removeAttachement(NodeRef referer, NodeRef refered) {
    if (getAttachementsNodes(referer).contains(refered)) {
      if (isHiddenAttachement(refered)) {
        nodeService.deleteNode(refered);
      } else {
        nodeService.removeAssociation(
          referer,
          refered,
          DocumentModel.ASSOC_EXTERNAL_REFERENCES
        );
      }
    }
  }

  /**
   * Attaches an existing node to the referer.
   *
   * <p>If the referred node is a temporary upload file it is moved under the referer as a hidden
   * attachment (a child node of type {@link DocumentModel#TYPE_HIDDEN_ATTACHEMENT_CONTENT}).
   * Otherwise it is attached as an external reference through {@link #attach(NodeRef, NodeRef)}.
   *
   * @param referer the node that will hold the attachment reference; tagged with
   *     {@link DocumentModel#ASPECT_ATTACHABLE} when a temporary file is moved
   * @param refered the existing (or temporary) node to attach
   * @return the {@link NodeRef} of the resulting attachment node
   */
  public NodeRef addAttachement(final NodeRef referer, final NodeRef refered) {
    if (temporaryFileManager.isTempFile(refered)) {
      nodeService.addAspect(referer, DocumentModel.ASPECT_ATTACHABLE, null);

      ChildAssociationRef assoc = nodeService.moveNode(
        refered,
        referer,
        DocumentModel.ASSOC_HIDDEN_REFERENCES,
        DocumentModel.TYPE_HIDDEN_ATTACHEMENT_CONTENT
      );

      return assoc.getChildRef();
    } else {
      return attach(referer, refered);
    }
  }

  /**
   * Attaches an existing node to the referer as an external reference.
   *
   * <p>Ensures the referer carries the {@link DocumentModel#ASPECT_ATTACHABLE} aspect and creates an
   * {@link DocumentModel#ASSOC_EXTERNAL_REFERENCES} association between the two nodes. If the node is
   * already attached the existing attachment is returned unchanged.
   *
   * @param referer the node that will hold the attachment reference
   * @param refered the existing node to attach
   * @return the {@link NodeRef} of the attached node (equal to {@code refered})
   */
  public NodeRef attach(final NodeRef referer, final NodeRef refered) {
    if (getAttachementsNodes(referer).contains(refered)) {
      return refered;
    }

    nodeService.addAspect(referer, DocumentModel.ASPECT_ATTACHABLE, null);
    nodeService.createAssociation(
      referer,
      refered,
      DocumentModel.ASSOC_EXTERNAL_REFERENCES
    );

    return refered;
  }

  /**
   * Uploads the given file as new hidden attachment content under the referer node.
   *
   * <p>Ensures the referer carries the {@link DocumentModel#ASPECT_ATTACHABLE} aspect and creates a
   * child content node of type {@link DocumentModel#TYPE_HIDDEN_ATTACHEMENT_CONTENT} under the
   * {@link DocumentModel#ASSOC_HIDDEN_REFERENCES} association.
   *
   * @param referer the node that will hold the attachment reference
   * @param name the name to assign to the newly created attachment content
   * @param file the file whose content is uploaded and attached
   * @return the {@link NodeRef} of the newly created and attached content node
   */
  public NodeRef addAttachement(
    final NodeRef referer,
    final String name,
    final File file
  ) {
    nodeService.addAspect(referer, DocumentModel.ASPECT_ATTACHABLE, null);

    return contentManager.createContent(
      referer,
      name,
      DocumentModel.ASSOC_HIDDEN_REFERENCES,
      DocumentModel.TYPE_HIDDEN_ATTACHEMENT_CONTENT,
      file,
      false
    );
  }

  /**
   * {@inheritDoc}
   *
   * <p>Wraps every attached node (both external references and hidden attachments) in an
   * {@link AttachementImpl}.
   *
   * @param referer the node whose attachments are requested
   * @return the list of {@link Attachement} instances attached to the referer; empty if none
   */
  @Override
  public List<Attachement> getAttachements(NodeRef referer) {
    final List<Attachement> attachements = new ArrayList<>();

    for (final NodeRef refered : getAttachementsNodes(referer)) {
      attachements.add(new AttachementImpl(referer, refered, nodeService));
    }

    return attachements;
  }

  /**
   * Returns the {@link NodeRef}s of all nodes attached to the referer.
   *
   * <p>Combines both external reference targets ({@link DocumentModel#ASSOC_EXTERNAL_REFERENCES})
   * and hidden attachment children ({@link DocumentModel#ASSOC_HIDDEN_REFERENCES}). Returns an empty
   * list when the referer does not carry the {@link DocumentModel#ASPECT_ATTACHABLE} aspect.
   *
   * @param referer the node whose attachment nodes are requested
   * @return the list of attached {@link NodeRef}s; empty if none
   */
  public List<NodeRef> getAttachementsNodes(NodeRef referer) {
    final List<NodeRef> childs = new ArrayList<>();

    if (nodeService.hasAspect(referer, DocumentModel.ASPECT_ATTACHABLE)) {
      final List<AssociationRef> attachementsAssoc =
        nodeService.getTargetAssocs(
          referer,
          DocumentModel.ASSOC_EXTERNAL_REFERENCES
        );
      for (final AssociationRef assoc : attachementsAssoc) {
        childs.add(assoc.getTargetRef());
      }

      final List<ChildAssociationRef> attachementChilds =
        nodeService.getChildAssocs(
          referer,
          DocumentModel.ASSOC_HIDDEN_REFERENCES,
          RegexQNamePattern.MATCH_ALL
        );
      for (final ChildAssociationRef assoc : attachementChilds) {
        childs.add(assoc.getChildRef());
      }
    }

    return childs;
  }

  /**
   * Indicates whether the given node is a hidden attachment (owned content) rather than an external
   * reference.
   *
   * @param refered the attachment node to test
   * @return {@code true} if the node is of type
   *     {@link DocumentModel#TYPE_HIDDEN_ATTACHEMENT_CONTENT}; {@code false} otherwise
   */
  public boolean isHiddenAttachement(NodeRef refered) {
    final QName type = nodeService.getType(refered);

    return DocumentModel.TYPE_HIDDEN_ATTACHEMENT_CONTENT.equals(type);
  }
}
