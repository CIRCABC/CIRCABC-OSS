/**
 *
 */
package eu.europa.ec.digit.circabc.rest.template;

import freemarker.template.TemplateModelException;
import java.io.Serializable;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * FreeMarker template processor extension that resolves the e-mail address of a person node.
 *
 * <p>Used to fill the {@code <USER_EMAIL>} placeholder in mail/notification templates. When invoked
 * from a template with a person {@link NodeRef} (or a wrapped {@code TemplateNode}), it returns the
 * value of the {@link ContentModel#PROP_EMAIL cm:email} property of that node.
 *
 * @author schwerr
 * @see NodeRefBaseTemplateProcessorExtension
 */
public class MailAddress extends NodeRefBaseTemplateProcessorExtension {

  /**
   * Returns the e-mail address stored on the given node.
   *
   * @param nodeRef the reference of the person node whose e-mail address is requested
   * @return the value of the node's {@link ContentModel#PROP_EMAIL cm:email} property, or
   *     {@code null} if the property is not set
   * @throws TemplateModelException if the address cannot be resolved during template processing
   * @see
   *     eu.europa.ec.digit.circabc.rest.template.NodeRefBaseTemplateProcessorExtension#getResult(org.alfresco.service.cmr.repository.NodeRef)
   */
  @Override
  public String getResult(NodeRef nodeRef) throws TemplateModelException {
    final Map<QName, Serializable> props = getNodeService().getProperties(
      nodeRef
    );

    return (String) props.get(ContentModel.PROP_EMAIL);
  }
}
