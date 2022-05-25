/**
 *
 */
package eu.cec.digit.circabc.repo.template;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import java.io.Serializable;
import java.util.Map;

/**
 * Retrieves the person's mail address placeholder <USER_EMAIL> to be passed to the template.
 *
 * @author schwerr
 */
public class MailAddress extends NodeRefBaseTemplateProcessorExtension
        implements TemplateMethodModelEx {

    /**
     * @see
     *     eu.cec.digit.circabc.repo.template.NodeRefBaseTemplateProcessorExtension#getResult(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public String getResult(NodeRef nodeRef) throws TemplateModelException {

        final Map<QName, Serializable> props = getNodeService().getProperties(nodeRef);

        return (String) props.get(ContentModel.PROP_EMAIL);
    }
}
