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
package eu.cec.digit.circabc.repo.newsgroup;

import eu.cec.digit.circabc.aspect.ContentNotifyAspect;
import eu.cec.digit.circabc.model.ModerationModel;
import eu.cec.digit.circabc.service.newsgroup.AbuseReport;
import eu.cec.digit.circabc.service.newsgroup.ModerationService;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.*;

/**
 * Implementation of newsgroup moderation service.
 *
 * @author Yanick Pignot
 */
public class ModerationServiceImpl implements ModerationService {

    private static final Log logger = LogFactory.getLog(ModerationServiceImpl.class);

    /* For performance purposes, cache the well know forum containers */
    private static final List<QName> FORUM_CONTAINERS =
            Arrays.asList(ForumModel.TYPE_FORUMS, ForumModel.TYPE_FORUM, ForumModel.TYPE_TOPIC);

    private static final String MODERATION_ACTION_ACCEPT = "accept";
    private static final String MODERATION_ACTION_REFUSE = "refuse";
    private static final String MODERATION_ACTION_REFUSE_AUTO_MESSAGE =
            "This post has been automatically refused, because this forum is not anymore moderated";

    private NodeService nodeService;
    private DictionaryService dictionaryService;
    private PermissionService permissionService;
    private ContentService contentService;
    private BehaviourFilter policyBehaviourFilter;

    public void applyModeration(final NodeRef container, final boolean makeContentWaiting) {
        ParameterCheck.mandatory("The container", container);

        if (!isContainer(container)) {
            throw new IllegalArgumentException("Impossible to apply moderation on a non-container node.");
        }

        applyModerationImpl(container, makeContentWaiting);
    }

    private void applyModerationImpl(final NodeRef container, final boolean makeContentWaiting) {
        if (!isContainerModerated(container)) {
            nodeService.addAspect(
                    container,
                    ModerationModel.ASPECT_MODERATED,
                    Collections.singletonMap(ModerationModel.PROP_IS_MODERATED, (Serializable) Boolean.TRUE));

            if (logger.isDebugEnabled()) {
                logger.debug("Node successfully marked as moderate: " + container);
            }

            for (final ChildAssociationRef assoc : nodeService.getChildAssocs(container)) {
                final NodeRef childRef = assoc.getChildRef();
                if (isContainer(childRef)) {
                    applyModerationImpl(childRef, makeContentWaiting);
                } else if (makeContentWaiting) {
                    waitForApproval(childRef);
                }
            }
        }
    }

    public void accept(final NodeRef content) {
        ParameterCheck.mandatory("Content reference", content);
        if (isContainer(content)) {
            throw new IllegalArgumentException(
                    "Impossible to accept a container. The moderation is possible only on a content or a post");
        } else if (!isWaitingForApproval(content)) {
            throw new IllegalArgumentException(
                    "Impossible to accept a post not defined being waiting for approval!");
        }

        permissionService.setInheritParentPermissions(content, true);

        final Map<QName, Serializable> moderationProperties = getCommonModerationProperties(false);
        nodeService.addAspect(content, ModerationModel.ASPECT_APPROVED, moderationProperties);

        nodeService.removeAspect(content, ModerationModel.ASPECT_WAITING_APPROVAL);

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Node successfully accepted by "
                            + moderationProperties.get(ModerationModel.PROP_APPROVED_BY)
                            + " on "
                            + moderationProperties.get(ModerationModel.PROP_APPROVED_ON)
                            + ": "
                            + content);
        }
    }

    public void reject(final NodeRef content, final String message) {
        ParameterCheck.mandatory("Content reference", content);
        if (isContainer(content)) {
            throw new IllegalArgumentException(
                    "Impossible to reject a container. The moderation is possible only on a content or a post");
        } else if (!isWaitingForApproval(content)) {
            throw new IllegalArgumentException(
                    "Impossible to reject a post not defined being waiting for approval!");
        }

        final Map<QName, Serializable> moderationProperties = getCommonModerationProperties(true);
        moderationProperties.put(ModerationModel.PROP_REJECT_MESSAGE, message == null ? "" : message);
        nodeService.addAspect(content, ModerationModel.ASPECT_REJECTED, moderationProperties);

        boolean wasEnable = false;

        policyBehaviourFilter.disableBehaviour(content, ModerationModel.ASPECT_REJECTED);
        try {

            wasEnable = !policyBehaviourFilter.isEnabled();
            policyBehaviourFilter.disableBehaviour(content, ContentNotifyAspect.ASPECT_CONTENT_NOTIFY);
            // make a version with the rejected content and put an empty message.
            contentService.getWriter(content, ContentModel.PROP_CONTENT, true).putContent("");
        } finally {
            policyBehaviourFilter.enableBehaviour(content, ModerationModel.ASPECT_REJECTED);

            if (wasEnable) {
                policyBehaviourFilter.enableBehaviour(content, ContentNotifyAspect.ASPECT_CONTENT_NOTIFY);
            }
        }

        nodeService.removeAspect(content, ModerationModel.ASPECT_WAITING_APPROVAL);

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Node successfully rejected by "
                            + moderationProperties.get(ModerationModel.PROP_REJECT_BY)
                            + " on "
                            + moderationProperties.get(ModerationModel.PROP_REJECT_ON)
                            + ": "
                            + content);
            logger.debug("With message:  " + message);
        }
    }

    public void waitForApproval(final NodeRef content) {
        ParameterCheck.mandatory("Content reference", content);
        if (isContainer(content)) {
            throw new IllegalArgumentException(
                    "Impossible to make a container waiting for approval. The moderation is possible only on a content or a post");
        }

        if (isRejected(content)) {
            nodeService.removeAspect(content, ModerationModel.ASPECT_REJECTED);
        }

        nodeService.addAspect(content, ModerationModel.ASPECT_WAITING_APPROVAL, null);
        /* Only NwsModerate permissions (transgress inheritance) will be allowed to see the post */
        permissionService.setInheritParentPermissions(content, false);

        if (logger.isDebugEnabled()) {
            logger.debug("Node successfully waiting for approval: " + content);
        }
    }

    public AbuseReport signalAbuse(NodeRef content, String message) {
        ParameterCheck.mandatory("Content reference", content);
        if (isContainer(content)) {
            throw new IllegalArgumentException(
                    "Impossible to make a container signaled for abuse. The moderation is possible only on a content or a post");
        }

        final List<AbuseReport> abuses = getAbuses(content);
        final AbuseReportImpl newAbuse = new AbuseReportImpl(message);
        abuses.add(newAbuse);

        if (nodeService.hasAspect(content, ModerationModel.ASPECT_ABUSE_SIGNALED)) {
            nodeService.setProperty(content, ModerationModel.PROP_ABUSE_MESSAGES, (Serializable) abuses);
        } else {
            nodeService.addAspect(
                    content,
                    ModerationModel.ASPECT_ABUSE_SIGNALED,
                    Collections.singletonMap(ModerationModel.PROP_ABUSE_MESSAGES, (Serializable) abuses));
        }

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Abuse successfully signaled on the node : " + content + ". With messages: " + abuses);
        }

        return newAbuse;
    }

    public void signalNotAbuse(NodeRef content) {
        ParameterCheck.mandatory("Content reference", content);
        if (isContainer(content)) {
            throw new IllegalArgumentException(
                    "Impossible to make a container signaled for abuse. The moderation is possible only on a content or a post");
        } else if (!nodeService.hasAspect(content, ModerationModel.ASPECT_ABUSE_SIGNALED)) {
            throw new IllegalArgumentException("No abuse defined on this node!");
        }

        nodeService.removeAspect(content, ModerationModel.ASPECT_ABUSE_SIGNALED);
        nodeService.removeProperty(content, ModerationModel.PROP_ABUSE_MESSAGES);

        if (logger.isDebugEnabled()) {
            logger.debug("Abuse successfully removed on the node : " + content);
        }
    }

    public List<AbuseReport> getAbuses(NodeRef content) {
        ParameterCheck.mandatory("Content reference", content);
        final List<AbuseReport> reports;

        if (nodeService.hasAspect(content, ModerationModel.ASPECT_ABUSE_SIGNALED)) {
            reports =
                    (List<AbuseReport>) nodeService.getProperty(content, ModerationModel.PROP_ABUSE_MESSAGES);
        } else {
            reports = null;
        }

        return (reports == null) ? new ArrayList<AbuseReport>() : reports;
    }

    public boolean isContainerModerated(NodeRef nodeRef) {
        ParameterCheck.mandatory("Container reference", nodeRef);

        final NodeRef container;

        if (isContainer(nodeRef)) {
            container = nodeRef;
        } else {
            container = nodeService.getPrimaryParent(nodeRef).getParentRef();
        }

        final Boolean hasAspect = nodeService.hasAspect(container, ModerationModel.ASPECT_MODERATED);

        if (hasAspect) {
            final Boolean isModerated =
                    (Boolean) nodeService.getProperty(container, ModerationModel.PROP_IS_MODERATED);

            return isModerated != null && isModerated;
        } else {
            return false;
        }
    }

    public boolean isWaitingForApproval(NodeRef content) {
        ParameterCheck.mandatory("Content reference", content);

        return nodeService.hasAspect(content, ModerationModel.ASPECT_WAITING_APPROVAL);
    }

    public boolean isApproved(final NodeRef content) {
        ParameterCheck.mandatory("Content reference", content);

        return nodeService.hasAspect(content, ModerationModel.ASPECT_APPROVED);
    }

    public boolean isRejected(NodeRef content) {
        ParameterCheck.mandatory("Content reference", content);

        return nodeService.hasAspect(content, ModerationModel.ASPECT_REJECTED);
    }

    private boolean isContainer(final NodeRef nodeRef) {
        final QName type = nodeService.getType(nodeRef);
        return FORUM_CONTAINERS.contains(type)
                || dictionaryService.isSubClass(type, ContentModel.TYPE_FOLDER);
    }

    private Map<QName, Serializable> getCommonModerationProperties(boolean reject) {
        final Map<QName, Serializable> moderationProps = new HashMap<>(3);
        final Date date = new Date();
        final String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();

        if (reject) {
            moderationProps.put(ModerationModel.PROP_REJECT_ON, date);
            moderationProps.put(ModerationModel.PROP_REJECT_BY, currentUser);
        } else {
            moderationProps.put(ModerationModel.PROP_APPROVED_ON, date);
            moderationProps.put(ModerationModel.PROP_APPROVED_BY, currentUser);
        }

        return moderationProps;
    }

    public final void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public final void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    public final void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    public final void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    /**
     * @param policyBehaviourFilter the policyBehaviourFilter to set
     */
    public final void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
        this.policyBehaviourFilter = policyBehaviourFilter;
    }

    @Override
    public void stopModeration(NodeRef container, String action) {

        ParameterCheck.mandatory("The container", container);
        ParameterCheck.mandatory("The action", action);

        if (!isContainer(container)) {
            throw new IllegalArgumentException("Impossible to apply moderation on a non-container node.");
        }

        if (!(action.equals(MODERATION_ACTION_ACCEPT) || action.equals(MODERATION_ACTION_REFUSE))) {
            throw new IllegalArgumentException("Impossible to stop moderation, wrong action specified.");
        }

        stopModerationImpl(container, action);
    }

    private void stopModerationImpl(NodeRef container, String action) {

        if (isContainerModerated(container)) {

            for (final ChildAssociationRef assoc : nodeService.getChildAssocs(container)) {
                final NodeRef childRef = assoc.getChildRef();

                if (isContainer(childRef)) {
                    stopModerationImpl(childRef, action);

                    nodeService.removeAspect(childRef, ModerationModel.ASPECT_MODERATED);
                    nodeService.removeProperty(childRef, ModerationModel.PROP_IS_MODERATED);

                    if (logger.isDebugEnabled()) {
                        logger.debug("Node successfully marked as not moderate: " + container);
                    }

                } else if (action.equals(MODERATION_ACTION_ACCEPT)) {
                    if (nodeService.hasAspect(childRef, ModerationModel.ASPECT_WAITING_APPROVAL)) {
                        accept(childRef);
                    }
                } else if (action.equals(MODERATION_ACTION_REFUSE)) {
                    if (nodeService.hasAspect(childRef, ModerationModel.ASPECT_WAITING_APPROVAL)) {
                        reject(childRef, MODERATION_ACTION_REFUSE_AUTO_MESSAGE);
                    }
                }
            }

            nodeService.removeAspect(container, ModerationModel.ASPECT_MODERATED);
            nodeService.removeProperty(container, ModerationModel.PROP_IS_MODERATED);
        }
    }
}
