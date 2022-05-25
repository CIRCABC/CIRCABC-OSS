/**
 * ***************************************************************************** Copyright 2006
 * European Community
 *
 * <p>Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 *
 * <p>https://joinup.ec.europa.eu/software/page/eupl
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * ****************************************************************************
 */
package eu.cec.digit.circabc.repo.model;

import eu.cec.digit.circabc.error.CircabcRuntimeException;
import eu.cec.digit.circabc.model.DossierModel;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Dossier model type behaviour. Do not allow user to add content to dossier (only file links and
 * folder links are allowed)
 *
 * @author Slobodan Filipovic
 */
public class DossierModelType
        implements NodeServicePolicies.BeforeCreateNodePolicy, NodeServicePolicies.OnMoveNodePolicy {

    private static final String ERR_MSG_ONLY_LINKS = "only_links";

    @SuppressWarnings("unused")
    private static final Log logger = LogFactory.getLog(DossierModelType.class);

    /**
     * The policy component
     */
    private PolicyComponent policyComponent;

    /**
     * The node service
     */
    private NodeService nodeService;

    /**
     * Spring initialise method used to register the policy behaviours
     */
    public void init() {
        // Register the policy behaviours
        policyComponent.bindClassBehaviour(
                NodeServicePolicies.BeforeCreateNodePolicy.QNAME,
                ContentModel.TYPE_CMOBJECT,
                new JavaBehaviour(this, "beforeCreateNode"));

        policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnMoveNodePolicy.QNAME,
                ContentModel.TYPE_CMOBJECT,
                new JavaBehaviour(this, "onMoveNode"));
    }

    public void beforeCreateNode(
            final NodeRef parentRef,
            final QName assocTypeQName,
            final QName assocQName,
            final QName nodeTypeQName) {
        final QName parentType = nodeService.getType(parentRef);
        checkTypes(nodeTypeQName, parentType);
    }

    /**
     * Allow only to create types filelink , folderlink , or forum in dossier
     */
    private void checkTypes(final QName nodeTypeQName, final QName parentType) {
        if (parentType.equals(DossierModel.TYPE_DOSSIER_SPACE)
                && !(nodeTypeQName.equals(ApplicationModel.TYPE_FILELINK)
                || nodeTypeQName.equals(ApplicationModel.TYPE_FOLDERLINK)
                || nodeTypeQName.equals(ForumModel.TYPE_FORUM))) {
            throw new CircabcRuntimeException(ERR_MSG_ONLY_LINKS);
        }
    }

    /**
     * Set the policy component
     *
     * @param policyComponent the policy component
     */
    public void setPolicyComponent(final PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    /**
     * Set the node service
     *
     * @param nodeService the node service
     */
    public void setNodeService(final NodeService nodeService) {
        this.nodeService = nodeService;
    }

    @Override
    public void onMoveNode(
            ChildAssociationRef oldChildAssocRef, ChildAssociationRef newChildAssocRef) {
        final NodeRef parentRef = newChildAssocRef.getParentRef();
        final QName parentType = nodeService.getType(parentRef);
        final NodeRef nodeRef = newChildAssocRef.getChildRef();
        final QName nodeTypeQName = nodeService.getType(nodeRef);
        checkTypes(nodeTypeQName, parentType);
    }
}
