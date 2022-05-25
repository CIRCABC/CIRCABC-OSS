package eu.cec.digit.circabc.action.evaluator;

import eu.cec.digit.circabc.web.Services;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.action.evaluator.BaseActionEvaluator;
import org.alfresco.web.bean.repository.Node;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Checks if the document is editable in office and shows the action if true Checks if the Edit in
 * Office action must be rendered according to the content's mimetype, permissions and browser being
 * used
 *
 * @author schwerr
 */
public class EditInOfficeEvaluator extends BaseActionEvaluator {

    private static final long serialVersionUID = 3040243159462905324L;

    private static final List<String> officeMimetypes =
            new ArrayList<>(Arrays.asList(
                    MimetypeMap.MIMETYPE_WORD,
                    MimetypeMap.MIMETYPE_EXCEL,
                    MimetypeMap.MIMETYPE_PPT,
                    MimetypeMap.MIMETYPE_OPENXML_WORDPROCESSING,
                    MimetypeMap.MIMETYPE_OPENXML_SPREADSHEET,
                    MimetypeMap.MIMETYPE_OPENXML_PRESENTATION));

    /**
     * @see org.alfresco.web.action.evaluator.BaseActionEvaluator#evaluate(org.alfresco.web.bean.repository.Node)
     */
    @Override
    public boolean evaluate(Node node) {

        FacesContext fc = FacesContext.getCurrentInstance();

        DictionaryService dictionaryService =
                Services.getAlfrescoServiceRegistry(fc).getDictionaryService();
        NodeService nodeService =
                Services.getAlfrescoServiceRegistry(fc).getNodeService();
        ContentService contentService =
                Services.getAlfrescoServiceRegistry(fc).getContentService();
        PermissionService permissionService =
                Services.getAlfrescoServiceRegistry(fc).getPermissionService();
        CheckOutCheckInService checkOutCheckInService =
                Services.getAlfrescoServiceRegistry(fc).getCheckOutCheckInService();

        NodeRef nodeRef = node.getNodeRef();

        // Check if the current user has write permission on the node
        if (permissionService.hasPermission(nodeRef,
                PermissionService.WRITE) == AccessStatus.DENIED) {
            return false;
        }

        // Get all content definitions
        Collection<QName> contentQNames =
                dictionaryService.getAllProperties(DataTypeDefinition.CONTENT);

        QName nodeType = nodeService.getType(nodeRef);

        TypeDefinition typeDefinition = dictionaryService.getType(nodeType);

        Map<QName, PropertyDefinition> propertyDefinitions =
                typeDefinition.getProperties();

        // Iterate through all content definitions until we find the actual
        // content property to extract. This step is necessary because CIRCABC
        // uses default and custom content properties, which means that the
        // actual content could be in a different property than cm:content
        // (example: newsgroup attachments)
        for (QName contentQName : contentQNames) {

            // Check if the property exists for the nodeRef's type
            if (propertyDefinitions.containsKey(contentQName)) {

                // Check if the document is an Office document
                ContentReader reader = contentService.getReader(nodeRef,
                        contentQName);
                if (reader != null &&
                        officeMimetypes.contains(reader.getMimetype())) {

                    // If valid, check if the browser is IE
                    HttpServletRequest request =
                            (HttpServletRequest) FacesContext
                                    .getCurrentInstance()
                                    .getExternalContext()
                                    .getRequest();

                    String userAgent = request.getHeader("User-Agent");

                    // return true if the browser is MSIE and the node
                    // is not locked nor a working copy (checked out)
                    return userAgent != null &&
                            (userAgent.contains("MSIE") ||
                                    userAgent.contains("rv:11.")) &&
                            !node.isLocked() &&
                            checkOutCheckInService.getWorkingCopy(
                                    node.getNodeRef()) == null &&
                            !nodeService.hasAspect(node.getNodeRef(),
                                    ContentModel.ASPECT_WORKING_COPY);
                }
            }
        }

        return false;
    }
}
