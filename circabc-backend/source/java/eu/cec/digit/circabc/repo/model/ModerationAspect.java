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

import eu.cec.digit.circabc.model.ModerationModel;
import eu.cec.digit.circabc.service.newsgroup.ModerationService;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.content.ContentServicePolicies.OnContentUpdatePolicy;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Moderation model aspect behaviour.
 *
 * <p>{@link ModerationModel#ASPECT_MODERATED moderation aspect}
 *
 * @author Yanick Pignot
 */
public class ModerationAspect
        implements NodeServicePolicies.OnCreateChildAssociationPolicy,
        ContentServicePolicies.OnContentUpdatePolicy {

    private static final Log logger = LogFactory.getLog(ModerationAspect.class);

    private static final List<QName> FORUM_CONTAINERS =
            Arrays.asList(ForumModel.TYPE_FORUMS, ForumModel.TYPE_FORUM, ForumModel.TYPE_TOPIC);

    /**
     * The policy component
     */
    private PolicyComponent policyComponent;

    private ModerationService moderationService;
    private DictionaryService dictionaryService;
    private NodeService nodeService;

    /**
     * Spring initialise method used to register the policy behaviours
     */
    public void init() {

        this.policyComponent.bindAssociationBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onCreateChildAssociation"),
                ModerationModel.ASPECT_MODERATED,
                new JavaBehaviour(this, "onCreateChildAssociation"));

        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onCreateReplyAssociation"),
                ModerationModel.ASPECT_REJECTED,
                new JavaBehaviour(this, "onCreateChildAssociation"));

        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onCreateReplyAssociation"),
                ModerationModel.ASPECT_WAITING_APPROVAL,
                new JavaBehaviour(this, "onCreateChildAssociation"));

        this.policyComponent.bindClassBehaviour(
                OnContentUpdatePolicy.QNAME,
                ModerationModel.ASPECT_REJECTED,
                new JavaBehaviour(this, "onContentUpdate"));
    }

    /**
     * When a rejected post is updated, make it as waiting approval yet.
     */
    public void onContentUpdate(NodeRef nodeRef, boolean newContent) {
        if (moderationService.isRejected(nodeRef)) {
            moderationService.waitForApproval(nodeRef);
        }
    }

    /**
     * Ensure that no reply can be done on a post waiting for approval Or a post rejected
     */
    public void onCreateReplyAssociation(
            final ChildAssociationRef childAssocRef, final boolean isNewNode) {

        if (childAssocRef.getQName().equals(ContentModel.ASPECT_REFERENCING)) {
            final NodeRef parentRef = childAssocRef.getParentRef();

            if (moderationService.isWaitingForApproval(parentRef)) {
                throw new IllegalStateException("Reply to a 'waiting for approval' post is not allowed.");
            } else if (moderationService.isRejected(parentRef)) {
                throw new IllegalStateException("Reply to a 'rejected' post is not allowed.");
            }
        }
    }

    /**
     * Make child container being moderated and child contents (post) being waiting for approval!
     */
    public void onCreateChildAssociation(
            final ChildAssociationRef childAssocRef, final boolean isNewNode) {
        final NodeRef parentRef = childAssocRef.getParentRef();
        if (moderationService.isContainerModerated(parentRef)) {

            final NodeRef childRef = childAssocRef.getChildRef();
            final QName childType = nodeService.getType(childRef);

            if (isContainer(childType)) {
                moderationService.applyModeration(childRef, false);

                if (logger.isDebugEnabled()) {
                    logger.debug("Make container (" + childType + ") being moderated: " + childRef);
                }
            } else {
                moderationService.waitForApproval(childRef);

                if (logger.isDebugEnabled()) {
                    logger.debug("Make content (" + childType + ") being waiting for approval: " + childRef);
                }
            }
        }
    }

    /**
     * @param childType
     * @return
     */
    private boolean isContainer(final QName childType) {
        return childType.equals(ForumModel.TYPE_POST) == false
                || FORUM_CONTAINERS.contains(childType)
                || dictionaryService.isSubClass(childType, ContentModel.TYPE_FOLDER);
    }

    /**
     * Set the policy component
     *
     * @param policyComponent the policy component
     */
    public void setPolicyComponent(final PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public final void setModerationService(ModerationService moderationService) {
        this.moderationService = moderationService;
    }

    public final void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public final void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }
}
