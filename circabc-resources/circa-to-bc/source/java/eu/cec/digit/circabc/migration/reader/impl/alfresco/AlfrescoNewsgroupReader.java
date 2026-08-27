package eu.cec.digit.circabc.migration.reader.impl.alfresco;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.cec.digit.circabc.migration.reader.NewsgroupReader;
import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.model.ModerationModel;
import eu.cec.digit.circabc.service.migration.ExportationException;

/**
 * NewsgroupReader implementation that reads forums/topics/messages from the CIRCABC Alfresco repository.
 */
public class AlfrescoNewsgroupReader implements NewsgroupReader {

    private static final Log logger = LogFactory.getLog(AlfrescoNewsgroupReader.class);

    private NodeService nodeService;

    @Override
    public List<String> listRootForums(final String newsgroupRootPath) throws ExportationException {
        return listChildrenOfType(newsgroupRootPath, ForumModel.TYPE_FORUM);
    }

    @Override
    public List<String> listSubForums(final String parentForumPath) throws ExportationException {
        return listChildrenOfType(parentForumPath, ForumModel.TYPE_FORUM);
    }

    @Override
    public List<String> listTopics(final String parentForumPath) throws ExportationException {
        return listChildrenOfType(parentForumPath, ForumModel.TYPE_TOPIC);
    }

    @Override
    public String getDiscussions(final String parentLibraryContent) throws ExportationException {
        try {
            final NodeRef ref = new NodeRef(parentLibraryContent);
            if (!nodeService.exists(ref)) {
                return null;
            }
            // Check if the node has a discussion (forum) child
            final List<ChildAssociationRef> children = nodeService.getChildAssocs(ref);
            for (final ChildAssociationRef child : children) {
                final QName type = nodeService.getType(child.getChildRef());
                if (ForumModel.TYPE_FORUMS.equals(type) || ForumModel.TYPE_FORUM.equals(type)) {
                    return child.getChildRef().toString();
                }
            }
            return null;
        } catch (Exception e) {
            logger.warn("Error getting discussions for " + parentLibraryContent, e);
            return null;
        }
    }

    @Override
    public List<String> listDiscussionsTopics(final String parentLibraryContent) throws ExportationException {
        final String discussionPath = getDiscussions(parentLibraryContent);
        if (discussionPath == null) {
            return Collections.emptyList();
        }
        // The discussion forum may contain sub-forums or topics directly
        final List<String> topics = new ArrayList<>();
        try {
            final NodeRef forumRef = new NodeRef(discussionPath);
            final List<ChildAssociationRef> children = nodeService.getChildAssocs(forumRef);
            for (final ChildAssociationRef child : children) {
                final QName type = nodeService.getType(child.getChildRef());
                if (ForumModel.TYPE_TOPIC.equals(type)) {
                    topics.add(child.getChildRef().toString());
                } else if (ForumModel.TYPE_FORUM.equals(type)) {
                    // Sub-forum in discussion - list its topics
                    topics.addAll(listTopics(child.getChildRef().toString()));
                }
            }
        } catch (Exception e) {
            logger.warn("Error listing discussion topics for " + parentLibraryContent, e);
        }
        return topics;
    }

    @Override
    public List<String> listMessages(final String parentTopicPath) throws ExportationException {
        return listChildrenOfType(parentTopicPath, ForumModel.TYPE_POST);
    }

    @Override
    public List<String> listReplies(final String parentMessagePath) throws ExportationException {
        // In Alfresco, replies are child posts of a post
        return listChildrenOfType(parentMessagePath, ForumModel.TYPE_POST);
    }

    @Override
    public List<String> listAttachements(final String parentMessagePath) throws ExportationException {
        return listChildrenOfType(parentMessagePath, ContentModel.TYPE_CONTENT);
    }

    @Override
    public List<String> listLinks(final String parentMessagePath) throws ExportationException {
        return listChildrenOfType(parentMessagePath, ContentModel.TYPE_LINK);
    }

    @Override
    public boolean isModeratedForum(final String newsgroupPath) throws ExportationException {
        try {
            final NodeRef ref = new NodeRef(newsgroupPath);
            return nodeService.hasAspect(ref, ModerationModel.ASPECT_MODERATED);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean isLibraryDiscussion(final String newsgroupPath) {
        try {
            final NodeRef ref = new NodeRef(newsgroupPath);
            final NodeRef parent = nodeService.getPrimaryParent(ref).getParentRef();
            final QName parentType = nodeService.getType(parent);
            // If the parent is a content or space node (not a newsgroup root), it's a library discussion
            return !nodeService.hasAspect(parent, CircabcModel.ASPECT_NEWSGROUP_ROOT) &&
                   !ForumModel.TYPE_FORUM.equals(parentType);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getDiscussionOn(final String topicPath) {
        try {
            final NodeRef ref = new NodeRef(topicPath);
            // Navigate up to find the library content this discussion is attached to
            NodeRef parent = nodeService.getPrimaryParent(ref).getParentRef();
            // Go up through forum/forums containers
            while (parent != null) {
                final QName type = nodeService.getType(parent);
                if (!ForumModel.TYPE_FORUM.equals(type) && !ForumModel.TYPE_FORUMS.equals(type)) {
                    return parent.toString();
                }
                parent = nodeService.getPrimaryParent(parent).getParentRef();
            }
        } catch (Exception e) {
            logger.warn("Error finding discussion target for " + topicPath, e);
        }
        return null;
    }

    private List<String> listChildrenOfType(final String parentPath, final QName expectedType) throws ExportationException {
        try {
            final NodeRef parentRef = new NodeRef(parentPath);
            if (!nodeService.exists(parentRef)) {
                return Collections.emptyList();
            }
            final List<ChildAssociationRef> children = nodeService.getChildAssocs(parentRef);
            final List<String> paths = new ArrayList<>();
            for (final ChildAssociationRef child : children) {
                final QName type = nodeService.getType(child.getChildRef());
                if (expectedType.equals(type)) {
                    paths.add(child.getChildRef().toString());
                }
            }
            return paths;
        } catch (Exception e) {
            throw new ExportationException("Error listing children of type " + expectedType + " for " + parentPath, e);
        }
    }

    // --- Setters for Spring injection ---

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }
}
