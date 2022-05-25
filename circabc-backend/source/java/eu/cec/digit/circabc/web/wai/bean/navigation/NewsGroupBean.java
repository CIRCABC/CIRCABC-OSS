/*******************************************************************************
 * Copyright 2006 European Community
 *
 *  Licensed under the EUPL, Version 1.1 or - as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  You may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  https://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 ******************************************************************************/
package eu.cec.digit.circabc.web.wai.bean.navigation;

import eu.cec.digit.circabc.action.evaluator.PostReplyEvaluator;
import eu.cec.digit.circabc.model.ModerationModel;
import eu.cec.digit.circabc.service.customisation.nav.NavigationPreference;
import eu.cec.digit.circabc.service.customisation.nav.NavigationPreferencesService;
import eu.cec.digit.circabc.service.newsgroup.ModerationService;
import eu.cec.digit.circabc.service.profile.permissions.NewsGroupPermissions;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.app.CircabcNavigationHandler;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNode;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNodeType;
import eu.cec.digit.circabc.web.repository.IGServicesNode;
import eu.cec.digit.circabc.web.wai.menu.ActionWrapper;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.*;

/**
 * Bean that backs the navigation inside the newsgroup service
 *
 * @author yanick pignot
 */
public class NewsGroupBean extends InterestGroupBean {

    public static final String CREATE_TOPIC = "CreateTopic";
    public static final String CREATE_POST = "CreatePost";
    public static final String JSP_NAME = "newsgroups-home.jsp";
    public static final String BEAN_NAME = "NewsGroupBean";
    public static final String MSG_PAGE_TITLE = "newsgroups_forums_title";
    public static final String MSG_PAGE_DESCRIPTION = "newsgroups_forums_title_desc";
    public static final String MSG_PAGE_ICON_ALT = "newsgroups_forums_icon_tooltip";
    private static final String PARAM_HIDE = "hide";
    private static final String IMAGES_ICONS_FORUMS_GIF = "/images/icons/forums.gif";
    private static final String CREATE_FORUM = "CreateForum";
    private static final String NEWSGROUPS_FORUM_CREATE_FORUM = "newsgroups_forum_create_forum";
    private static final String WAI_DIALOG_CREATE_FORUM_WAI = "wai:dialog:createForumWai";
    private static final String NEWSGROUPS_FORUM_CREATE_FORUM_TOOLTIP = "newsgroups_forum_create_forum_tooltip";
    private static final String NEWSGROUPS_FORUM_CREATE_TOPIC = "newsgroups_forum_create_topic";
    private static final String WAI_DIALOG_CREATE_TOPIC_WAI = "wai:dialog:createTopicWai";
    private static final String NEWSGROUPS_FORUM_CREATE_TOPIC_TOOLTIP = "newsgroups_forum_create_topic_tooltip";
    private static final String ID = "id";
    private static final String NEWSGROUPS_TOPIC_CREATE_POST_TOOLTIP = "newsgroups_topic_create_post_tooltip";
    private static final String WAI_DIALOG_CREATE_POST_WAI = "wai:dialog:createPostWai";
    private static final String NEWSGROUPS_TOPIC_CREATE_POST = "newsgroups_topic_create_post";
    private static final String GET_TOPIC_NODES_TIME_TO_QUERY_AND_BUILD_FORUM_NODES = "getTopicNodes | Time to query and build forum nodes: ";
    private static final String GET_FORUM_NODES_TIME_TO_QUERY_AND_BUILD_FORUMS_NODES = "GetForumNodes | Time to query and build forums nodes: ";
    private static final String MS = "ms";
    private static final String GET_POST_NODES_TIME_TO_QUERY_AND_BUILD_POST_NODES = "getPostNodes | Time to query and build post nodes: ";
    private static final String TYPE = ", type = ";
    private static final String FOUND_INVALID_OBJECT_IN_DATABASE_ID = "Found invalid object in database: id = ";
    private static final String PROP_DATE = "{http://www.alfresco.org/model/content/1.0}created";
    private static final Log logger = LogFactory.getLog(NewsGroupBean.class);
    /**
     *
     */
    private static final long serialVersionUID = -6967164595499663893L;
    private boolean onlyPendingPosts = false;

    private transient ModerationService moderationService;

    public NavigableNodeType getManagedNodeType() {
        return NavigableNodeType.NEWSGROUP;
    }

    public String getRelatedJsp() {
        return NAVIGATION_JSP_FOLDER + JSP_NAME;
    }

    public String getPageDescription() {
        return translate(MSG_PAGE_DESCRIPTION);
    }

    public String getPageTitle() {
        return translate(MSG_PAGE_TITLE);
    }

    public String getPageIcon() {
        return IMAGES_ICONS_FORUMS_GIF;
    }

    public String getPageIconAltText() {
        return translate(MSG_PAGE_ICON_ALT);
    }

    public void init(Map<String, String> parameters) {
        if (parameters != null) {
            if (parameters.containsKey(PARAM_HIDE)) {
                onlyPendingPosts = Boolean.valueOf(parameters.get(PARAM_HIDE));
            } else {
                onlyPendingPosts = false;
            }
        }


    }

    /**
     * @return the list of available action for the current node that should be a Library root or
     * child
     */
    public List<ActionWrapper> getActions() {
        final List<ActionWrapper> actions = new ArrayList<>(2);

        if (!FacesContext.getCurrentInstance().getViewRoot().getViewId()
                .equals(CircabcNavigationHandler.WAI_NAVIGATION_CONTAINER_PAGE)) {
            // don't display actions whan a dialog or a wizard is launched
            return null;
        }

        if (getNavigator().getCurrentNodeType().equals(NavigableNodeType.NEWSGROUP)) {
            actions.add(
                    new ActionWrapper(
                            CREATE_FORUM,
                            translate(NEWSGROUPS_FORUM_CREATE_FORUM),
                            WAI_DIALOG_CREATE_FORUM_WAI,
                            DIALOG_MANAGER_SETUP_PARAMETERS,
                            translate(NEWSGROUPS_FORUM_CREATE_FORUM_TOOLTIP),
                            ID,
                            (Serializable) getNavigator().getCurrentNodeId())
            );
        } else if (getNavigator().getCurrentNodeType().equals(NavigableNodeType.NEWSGROUP_FORUM)) {
            actions.add(
                    new ActionWrapper(
                            CREATE_FORUM,
                            translate(NEWSGROUPS_FORUM_CREATE_FORUM),
                            WAI_DIALOG_CREATE_FORUM_WAI,
                            DIALOG_MANAGER_SETUP_PARAMETERS,
                            translate(NEWSGROUPS_FORUM_CREATE_FORUM_TOOLTIP),
                            ID,
                            (Serializable) getNavigator().getCurrentNodeId())
            );

            actions.add(
                    new ActionWrapper(
                            CREATE_TOPIC,
                            translate(NEWSGROUPS_FORUM_CREATE_TOPIC),
                            WAI_DIALOG_CREATE_TOPIC_WAI,
                            DIALOG_MANAGER_SETUP_PARAMETERS,
                            translate(NEWSGROUPS_FORUM_CREATE_TOPIC_TOOLTIP),
                            ID,
                            (Serializable) getNavigator().getCurrentNodeId())
            );
        } else if (getNavigator().getCurrentNodeType().equals(NavigableNodeType.NEWSGROUP_TOPIC)) {

            PostReplyEvaluator pre = new PostReplyEvaluator();
            if (pre.evaluate(getCurrentNode())) {
                actions.add(
                        new ActionWrapper(
                                CREATE_POST,
                                translate(NEWSGROUPS_TOPIC_CREATE_POST),
                                WAI_DIALOG_CREATE_POST_WAI,
                                DIALOG_MANAGER_SETUP_PARAMETERS,
                                translate(NEWSGROUPS_TOPIC_CREATE_POST_TOOLTIP),
                                ID,
                                (Serializable) getNavigator().getCurrentNodeId())
                );
            }
        }

        return actions;
    }

    public NavigationPreference getForumNavigationPreference() {
        return getNavigationPreferencesService().getServicePreference(
                getCurrentNode().getNodeRef(), NavigationPreferencesService.NEWSGROUP_SERVICE,
                NavigationPreferencesService.FORUM_TYPE);
    }

    public NavigationPreference getTopicNavigationPreference() {
        return getNavigationPreferencesService().getServicePreference(
                getCurrentNode().getNodeRef(), NavigationPreferencesService.NEWSGROUP_SERVICE,
                NavigationPreferencesService.TOPIC_TYPE);
    }

    public String getRenderPropertyNameNavigationPreference() {
        return getNavigationPreferencesService().getServicePreference(
                getCurrentNode().getNodeRef(), NavigationPreferencesService.NEWSGROUP_SERVICE,
                NavigationPreferencesService.FORUM_TYPE).getRenderPropertyName();
    }

    /**
     * Get the list of forum nodes for the current newsgroups
     *
     * @return List of forum nodes for the current newsgroups
     */
    public List<NavigableNode> getForums() {
        long startTime = 0;
        if (logger.isDebugEnabled()) {
            startTime = System.currentTimeMillis();
        }

        final FacesContext context = FacesContext.getCurrentInstance();
        final RetryingTransactionHelper txnHelper = Repository.getRetryingTransactionHelper(context);
        final RetryingTransactionCallback<List<NavigableNode>> callback = new RetryingTransactionCallback<List<NavigableNode>>() {
            public List<NavigableNode> execute() throws Throwable {
                List<NavigableNode> forums;

                try {

                    final IGServicesNode serviceNode = (IGServicesNode) getNavigator().getCurrentIGService();

                    final List<FileInfo> files = getFileFolderService().listFolders(serviceNode.getNodeRef());
                    forums = new ArrayList<>(files.size());

                    NodeRef nodeRef;
                    QName type;
                    TypeDefinition typeDef;
                    NavigableNode node;
                    for (final FileInfo fileInfo : files) {
                        // create our Node representation from the NodeRef
                        nodeRef = fileInfo.getNodeRef();

                        //find it's type so we can see if it's a node we are interested in
                        type = getNodeService().getType(nodeRef);

                        // make sure the type is defined in the data dictionary
                        typeDef = getDictionaryService().getType(type);

                        if (typeDef != null) {
                            // extract forums
                            if (ContentModel.TYPE_SYSTEM_FOLDER.equals(type)
                                    || getDictionaryService().isSubClass(type, ContentModel.TYPE_SYSTEM_FOLDER)
                                    == false) {
                                if (ForumModel.TYPE_FORUM.equals(type) || getDictionaryService()
                                        .isSubClass(type, ForumModel.TYPE_FORUM)) {
                                    // create our Node representation
                                    node = ResolverHelper
                                            .createForumRepresentation(fileInfo, getNodeService(), getBrowseBean());
                                    forums.add(node);
                                }
                            }
                        } else {
                            if (logger.isWarnEnabled()) {
                                logger.warn(FOUND_INVALID_OBJECT_IN_DATABASE_ID + nodeRef + TYPE + type);
                            }
                        }

                    }

                } catch (final InvalidNodeRefException refErr) {
                    Utils.addErrorMessage(translate(Repository.ERROR_NODEREF, refErr.getNodeRef()));
                    forums = Collections.emptyList();
                } catch (final Throwable err) {
                    Utils.addErrorMessage(translate(Repository.ERROR_GENERIC, err.getMessage()), err);
                    forums = Collections.emptyList();
                }
                return forums;
            }
        };
        final List<NavigableNode> forums = txnHelper.doInTransaction(callback, true);

        if (logger.isDebugEnabled()) {
            long endTime = System.currentTimeMillis();
            logger
                    .debug(GET_FORUM_NODES_TIME_TO_QUERY_AND_BUILD_FORUMS_NODES + (endTime - startTime) + MS);
        }

        return forums;
    }

    /**
     * Get the list of topic nodes for the current forum
     *
     * @return List of topic nodes for the current forum
     */
    public List<NavigableNode> getTopics() {
        long startTime = 0;
        if (logger.isDebugEnabled()) {
            startTime = System.currentTimeMillis();
        }

        final FacesContext context = FacesContext.getCurrentInstance();
        final RetryingTransactionHelper txnHelper = Repository.getRetryingTransactionHelper(context);
        final RetryingTransactionCallback<List<NavigableNode>> callback = new RetryingTransactionCallback<List<NavigableNode>>() {
            public List<NavigableNode> execute() throws Throwable {
                List<NavigableNode> topics = null;
                try {
                    final IGServicesNode serviceNode = (IGServicesNode) getNavigator().getCurrentIGService();

                    final List<FileInfo> files = getFileFolderService().listFolders(serviceNode.getNodeRef());
                    topics = new ArrayList<>(files.size());

                    // Outtrack variable declaration
                    NodeRef nodeRef;
                    QName type;
                    TypeDefinition typeDef;
                    NavigableNode node;
                    for (final FileInfo fileInfo : files) {
                        // create our Node representation from the NodeRef
                        nodeRef = fileInfo.getNodeRef();

                        // find it's type so we can see if it's a node we are interested in
                        type = getNodeService().getType(nodeRef);

                        // make sure the type is defined in the data dictionary
                        typeDef = getDictionaryService().getType(type);

                        if (typeDef != null) {
                            // extract forums
                            if ((ContentModel.TYPE_SYSTEM_FOLDER.equals(type) || getDictionaryService()
                                    .isSubClass(type, ContentModel.TYPE_SYSTEM_FOLDER)) == false) {
                                if ((ForumModel.TYPE_TOPIC.equals(type) || getDictionaryService()
                                        .isSubClass(type, ForumModel.TYPE_TOPIC))) {
                                    // create our Node representation
                                    node = ResolverHelper
                                            .createTopicRepresentation(fileInfo, getNodeService(), getBrowseBean());
                                    topics.add(node);
                                }
                            }
                        } else {
                            if (logger.isWarnEnabled()) {
                                logger.warn(FOUND_INVALID_OBJECT_IN_DATABASE_ID + nodeRef + TYPE + type);
                            }
                        }
                    }
                } catch (final InvalidNodeRefException refErr) {
                    Utils.addErrorMessage(translate(Repository.ERROR_NODEREF, refErr.getNodeRef()));
                    topics = Collections.emptyList();
                } catch (final Throwable err) {
                    Utils.addErrorMessage(translate(Repository.ERROR_GENERIC, err.getMessage()), err);
                    topics = Collections.emptyList();
                }
                return topics;
            }
        };
        final List<NavigableNode> topics = txnHelper.doInTransaction(callback, true);

        if (logger.isDebugEnabled()) {
            long endTime = System.currentTimeMillis();
            logger
                    .debug(GET_TOPIC_NODES_TIME_TO_QUERY_AND_BUILD_FORUM_NODES + (endTime - startTime) + MS);
        }

        return topics;
    }

    /**
     * Get the list of post nodes for the current topic
     *
     * @return List of post nodes for the current topic
     */
    public List<NavigableNode> getPosts() {
        return getPosts(onlyPendingPosts);
    }

    private List<NavigableNode> getPosts(final boolean onlyPending) {
        long startTime = 0;
        if (logger.isDebugEnabled()) {
            startTime = System.currentTimeMillis();
        }

        final NavigationPreference topicNavigationPreference = getTopicNavigationPreference();

        final FacesContext context = FacesContext.getCurrentInstance();
        final RetryingTransactionHelper txnHelper = Repository.getRetryingTransactionHelper(context);
        final RetryingTransactionCallback<List<NavigableNode>> callback = new RetryingTransactionCallback<List<NavigableNode>>() {
            public List<NavigableNode> execute() throws Throwable {
                List<NavigableNode> posts;
                try {
                    final IGServicesNode serviceNode = (IGServicesNode) getNavigator().getCurrentIGService();

                    final List<FileInfo> files = getFileFolderService().listFiles(serviceNode.getNodeRef());
                    posts = new ArrayList<>(files.size());

                    // Outtrack variable declaration
                    NodeRef nodeRef;
                    QName type;
                    TypeDefinition typeDef;
                    NavigableNode node;

                    for (final FileInfo fileInfo : files) {
                        // create our Node representation from the NodeRef
                        nodeRef = fileInfo.getNodeRef();

                        if (onlyPending == false || getModerationService().isWaitingForApproval(nodeRef)) {
                            // find it's type so we can see if it's a node we are interested in
                            type = getNodeService().getType(nodeRef);

                            // make sure the type is defined in the data dictionary
                            typeDef = getDictionaryService().getType(type);

                            if (typeDef != null) {
                                if (ForumModel.TYPE_POST.equals(type) || getDictionaryService()
                                        .isSubClass(type, ForumModel.TYPE_POST)) {
                                    // create our Node representation
                                    node = ResolverHelper
                                            .createPostRepresentation(fileInfo, getNodeService(), getBrowseBean());
                                    posts.add(node);
                                }
                            } else {
                                if (logger.isWarnEnabled()) {
                                    logger.warn(FOUND_INVALID_OBJECT_IN_DATABASE_ID + nodeRef + TYPE + type);
                                }
                            }
                        }
                    }
                } catch (final InvalidNodeRefException refErr) {
                    Utils.addErrorMessage(translate(Repository.ERROR_NODEREF, refErr.getNodeRef()));
                    posts = Collections.emptyList();
                } catch (final Throwable err) {
                    Utils.addErrorMessage(translate(Repository.ERROR_GENERIC, err.getMessage()), err);
                    posts = Collections.emptyList();
                }

                final int desc = topicNavigationPreference.isInitialSortPostsDescending() ? -11 : 1;

                // Sort posts descending/ascending
                Collections.sort(posts, new Comparator<NavigableNode>() {

                    @Override
                    public int compare(NavigableNode o1, NavigableNode o2) {

                        Map<String, Object> properties1 = o1.getProperties();
                        Map<String, Object> properties2 = o2.getProperties();

                        Date date1 = (Date) properties1.get(PROP_DATE);
                        Date date2 = (Date) properties2.get(PROP_DATE);

                        if (date1.after(date2)) {
                            return desc;
                        } else if (date1.before(date2)) {
                            return desc * -1;
                        }

                        return 0;
                    }
                });

                return posts;
            }
        };
        final List<NavigableNode> posts = txnHelper.doInTransaction(callback, true);

        if (logger.isDebugEnabled()) {
            long endTime = System.currentTimeMillis();
            logger.debug(GET_POST_NODES_TIME_TO_QUERY_AND_BUILD_POST_NODES + (endTime - startTime) + MS);
        }

        return posts;
    }

    public boolean isFilterPendingAllowed() {
        return onlyPendingPosts == false && isUserModerator() && isModeratedForum();
    }

    public boolean isFilterAllAllowed() {
        return onlyPendingPosts == true && isUserModerator() && isModeratedForum();
    }

    private boolean isModeratedForum() {
        final Node node = getCurrentNode();
        return node.hasAspect(ModerationModel.ASPECT_MODERATED)
                && Boolean.TRUE
                .equals(node.getProperties().get(ModerationModel.PROP_IS_MODERATED.toString()));
    }

    protected boolean isUserModerator() {
        return getCurrentNode().hasPermission(NewsGroupPermissions.NWSMODERATE.toString());
    }

    public boolean isSubForumAllowed() {
        return true;
    }

    protected final ModerationService getModerationService() {
        if (moderationService == null) {
            moderationService = Services.getCircabcServiceRegistry(FacesContext.getCurrentInstance())
                    .getModerationService();
        }
        return moderationService;
    }

    public final void setModerationService(ModerationService moderationService) {
        this.moderationService = moderationService;
    }
}
