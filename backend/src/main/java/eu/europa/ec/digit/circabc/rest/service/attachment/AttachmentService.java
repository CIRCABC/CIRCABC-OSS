package eu.europa.ec.digit.circabc.rest.service.attachment;

import io.swagger.model.Attachement;
import java.io.File;
import java.util.List;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Service for managing attachments between repository nodes.
 *
 * <p>An attachment expresses a relationship in which a "referer" node references another node (the
 * "refered" node) as one of its attachments. Implementations are responsible for creating,
 * removing and listing these attachment relationships, as well as for uploading new content that
 * becomes an attachment.
 */
public interface AttachmentService {
  /**
   * Removes the attachment relationship linking the given referer node to the referred node.
   *
   * @param referer the node that holds the attachment reference
   * @param refered the attached node to detach from the referer
   */
  void removeAttachement(NodeRef referer, NodeRef refered);

  /**
   * Attaches an existing repository node to the given referer node.
   *
   * @param referer the node that will hold the attachment reference
   * @param refered the existing node to attach to the referer
   * @return the {@link NodeRef} representing the created attachment
   */
  NodeRef addAttachement(NodeRef referer, NodeRef refered);

  /**
   * Uploads the given file as new content and attaches it to the referer node.
   *
   * @param referer the node that will hold the attachment reference
   * @param name the name to assign to the newly created attachment content
   * @param file the file whose content is uploaded and attached
   * @return the {@link NodeRef} of the newly created and attached content node
   */
  NodeRef addAttachement(NodeRef referer, String name, File file);

  /**
   * Retrieves all attachments referenced by the given referer node.
   *
   * @param referer the node whose attachments are requested
   * @return the list of {@link Attachement} instances attached to the referer; empty if none
   */
  List<Attachement> getAttachements(NodeRef referer);
}
