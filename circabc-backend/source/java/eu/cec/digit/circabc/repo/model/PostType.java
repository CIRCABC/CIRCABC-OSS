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

import eu.cec.digit.circabc.model.DocumentModel;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Class containing behaviour for the post type. Inside Circabc, all posts must be versionnable.
 *
 * <p>{@link ForumModel#TYPE_POST post type}
 *
 * @author Yanick Pignot
 */
public class PostType {

    private static final Log logger = LogFactory.getLog(PostType.class);

    //     Dependencies
    private PolicyComponent policyComponent;
    private NodeService nodeService;

    public void init() {
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onCreateNode"),
                ForumModel.TYPE_POST,
                new JavaBehaviour(this, "makeVersionnable"));

        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onCreateNode"),
                ForumModel.TYPE_TOPIC,
                new JavaBehaviour(this, "addBProperties"));
    }

    /**
     * @param policyComponent the policy component to register behaviour with
     */
    public void setPolicyComponent(final PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void makeVersionnable(final ChildAssociationRef childAssocRef) {
        final NodeRef postRef = childAssocRef.getChildRef();
        if (isArchived(postRef) == false
                && nodeService.hasAspect(postRef, ContentModel.ASPECT_VERSIONABLE) == false) {
            // add versionable aspect (set auto-version)
            final Map<QName, Serializable> versionProps = new HashMap<>();
            versionProps.put(ContentModel.PROP_AUTO_VERSION, true);
            nodeService.addAspect(postRef, ContentModel.ASPECT_VERSIONABLE, versionProps);

            if (logger.isInfoEnabled()) {
                logger.info("Add AutoVersionning on post:" + postRef);
            }
        }
    }

    public void addBProperties(final ChildAssociationRef childAssocRef) {
        final NodeRef topicRef = childAssocRef.getChildRef();
        if (isArchived(topicRef) == false
                && nodeService.hasAspect(topicRef, DocumentModel.ASPECT_BPROPERTIES) == false) {
            final Map<QName, Serializable> bProps = new HashMap<>(2);
            bProps.put(DocumentModel.PROP_EXPIRATION_DATE, null);
            // issue 4779 the default security ranking should be normal not public
            bProps.put(DocumentModel.PROP_SECURITY_RANKING, DocumentModel.SECURITY_RANKINGS_NORMAL);
            nodeService.addAspect(topicRef, DocumentModel.ASPECT_BPROPERTIES, bProps);

            if (logger.isInfoEnabled()) {
                logger.info("addBPropertiesToTopics with properties: " + bProps + " on " + topicRef);
            }
        }
    }

    public final void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    private boolean isArchived(final NodeRef nodeRef) {
        return StoreRef.STORE_REF_ARCHIVE_SPACESSTORE.equals(nodeRef.getStoreRef());
    }
}
