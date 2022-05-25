package eu.cec.digit.circabc.action.evaluator;

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.service.external.repositories.ExternalRepositoriesManagementService;
import eu.cec.digit.circabc.web.Services;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.web.action.evaluator.BaseActionEvaluator;
import org.alfresco.web.bean.repository.Node;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.context.FacesContext;

/**
 * Evaluator to determine if there are external repositories configured at the IG root level and
 * therefore show the action to publish into them. It also evaluates if the current node was already
 * published and if the current user can publish.
 *
 * @author schwerr
 */
public class ExternalRepositoryActionEvaluator extends BaseActionEvaluator {

    private static final long serialVersionUID = -3696155440737331589L;

    private static final Log logger = LogFactory.getLog(ExternalRepositoryActionEvaluator.class);

    private static ExternalRepositoriesManagementService externalRepositoriesManagementService = null;

    /**
     * Retrieves the nodeRef of the IG where this nodeRef is stored. If there is no IG root, it
     * returns null.
     */
    public static NodeRef getIGRoot(NodeRef nodeRef, NodeService nodeService) {

        if (nodeRef == null) {
            return null;
        }

        do {
            nodeRef = nodeService.getParentAssocs(nodeRef).get(0).getParentRef();
        } while (nodeRef != null && !nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_IGROOT));

        if (nodeRef != null && nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_IGROOT)) {
            return nodeRef;
        }

        return null;
    }

    /**
     * Sets the value of the externalRepositoriesManagementService
     *
     * @param externalRepositoriesManagementService the externalRepositoriesManagementService to set.
     */
    public static void setExternalRepositoriesManagementService(
            ExternalRepositoriesManagementService externalRepositoriesManagementService) {
        ExternalRepositoryActionEvaluator.externalRepositoriesManagementService =
                externalRepositoriesManagementService;
    }

    /**
     * @see org.alfresco.web.action.evaluator.BaseActionEvaluator#evaluate(org.alfresco.web.bean.repository.Node)
     */
    @Override
    public boolean evaluate(Node node) {

        // First check if the system is operational, and if yes, do all other
        // checks
        if (!externalRepositoriesManagementService.isOperational()) {
            logger.debug("External repositories NOT operational.");
            return false;
        }

        final FacesContext fc = FacesContext.getCurrentInstance();
        final NodeService nodeService =
                Services.getAlfrescoServiceRegistry(fc).getNodeService();

        NodeRef documentNodeRef = node.getNodeRef();
        NodeRef igNodeRef = getIGRoot(documentNodeRef, nodeService);
        String userName = externalRepositoriesManagementService.
                getUserNameResolver().getUserName();

        // Evaluate if the current node is a working copy
        if (nodeService.hasAspect(documentNodeRef, ContentModel.ASPECT_WORKING_COPY)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Aspect WORKING_COPY present. Current Username: " +
                        userName + ", IG NodeRef: " + igNodeRef + ", "
                        + "Document NodeRef: " + documentNodeRef);
            }
            return false;
        }

        // Evaluate if the current user can publish
        if (!externalRepositoriesManagementService.isUserAuthorizedToPublish(userName)) {
            if (logger.isDebugEnabled()) {
                logger.debug("User not authorized to publish/register. Current Username: " +
                        userName + ", IG NodeRef: " + igNodeRef + ", "
                        + "Document NodeRef: " + documentNodeRef);
            }
            return false;
        }

        // Evaluate if the node was already published
        if (externalRepositoriesManagementService.wasPublishedTo(
                ExternalRepositoriesManagementService.EXTERNAL_REPOSITORY_NAME,
                documentNodeRef.toString())) {
            if (logger.isDebugEnabled()) {
                logger.debug("Document already published. Current Username: " +
                        userName + ", IG NodeRef: " + igNodeRef + ", "
                        + "Document NodeRef: " + documentNodeRef);
            }
            return false;
        }

        // Evaluate if the external repository was configured

        if (igNodeRef != null) {

            if (logger.isDebugEnabled()) {
                logger.debug("Current Username: " + userName +
                        ", IG NodeRef: " + igNodeRef);
            }

            NodeRef externalRepositoriesNodeRef = nodeService.
                    getChildByName(igNodeRef, ContentModel.ASSOC_CONTAINS,
                            "ExternalRepositoryConfigurations");

            if (logger.isDebugEnabled()) {
                logger.debug("External repositories configuration NodeRef: " +
                        externalRepositoriesNodeRef);
            }

            boolean result = externalRepositoriesNodeRef != null &&
                    nodeService.getChildAssocs(externalRepositoriesNodeRef).size() > 0;

            if (logger.isDebugEnabled()) {
                logger.debug("Show external repositories register link on right menu: " + result);
            }

            return result;
        }

        return false;
    }
}
