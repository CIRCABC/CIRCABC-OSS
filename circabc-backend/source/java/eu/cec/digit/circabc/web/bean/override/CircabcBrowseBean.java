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
package eu.cec.digit.circabc.web.bean.override;

import eu.cec.digit.circabc.business.api.BusinessRegistry;
import eu.cec.digit.circabc.business.api.user.UserDetailsBusinessSrv;
import eu.cec.digit.circabc.config.CircabcConfiguration;
import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.model.DocumentModel;
import eu.cec.digit.circabc.model.ModerationModel;
import eu.cec.digit.circabc.service.CircabcServiceRegistry;
import eu.cec.digit.circabc.service.cmr.security.CircabcConstant;
import eu.cec.digit.circabc.service.newsgroup.AbuseReport;
import eu.cec.digit.circabc.service.newsgroup.AttachementService;
import eu.cec.digit.circabc.service.newsgroup.ModerationService;
import eu.cec.digit.circabc.service.profile.Profile;
import eu.cec.digit.circabc.service.profile.ProfileManagerService;
import eu.cec.digit.circabc.service.profile.permissions.LibraryPermissions;
import eu.cec.digit.circabc.service.struct.ManagementService;
import eu.cec.digit.circabc.web.Beans;
import eu.cec.digit.circabc.web.PermissionUtils;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.WebClientHelper;
import eu.cec.digit.circabc.web.WebClientHelper.ExtendedURLMode;
import eu.cec.digit.circabc.web.app.CircabcNavigationHandler;
import eu.cec.digit.circabc.web.bean.navigation.AspectResolver;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNode;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNodeType;
import eu.cec.digit.circabc.web.repository.InterestGroupNode;
import eu.cec.digit.circabc.web.servlet.SimpleDownloadServlet;
import eu.cec.digit.circabc.web.ui.common.UtilsCircabc;
import eu.cec.digit.circabc.web.ui.component.UINavigationList;
import eu.cec.digit.circabc.web.ui.repo.component.UISimpleSearch;
import eu.cec.digit.circabc.web.wai.app.WaiApplication;
import eu.cec.digit.circabc.web.wai.bean.navigation.LibraryBean;
import eu.cec.digit.circabc.web.wai.bean.navigation.ResolverHelper;
import eu.cec.digit.circabc.web.wai.bean.navigation.WelcomeBean;
import eu.cec.digit.circabc.web.wai.dialog.search.AdvancedSearchDialog;
import eu.cec.digit.circabc.web.wai.dialog.search.SearchResultDialog;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.search.QueryParameterDefinition;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.context.UIContextService;
import org.alfresco.web.app.servlet.DownloadContentServlet;
import org.alfresco.web.bean.BrowseBean;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.NodePropertyResolver;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIActionLink;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import javax.faces.application.ViewHandler;
import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.component.UIParameter;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import java.io.Serializable;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Extension of the Alfresco Browse bean that manage the specific Circabc navigation features and
 * the wai browse.
 *
 * @author yanick pignot
 */
public class CircabcBrowseBean extends BrowseBean {

    /**
     * max length for display name
     */
    public static final int SHORTNAME_LEN = 29;
    public static final String WAI_BROWSE_OUTCOME = "browse-wai";
    public static final String PREFIXED_WAI_BROWSE_OUTCOME =
            CircabcNavigationHandler.WAI_PREFIX + WAI_BROWSE_OUTCOME;
    public static final String CLOSE_NAVIGATION_OUTCOME =
            WAI_BROWSE_OUTCOME + CircabcNavigationHandler.OUTCOME_SEPARATOR + "close";
    public static final String PREFIXED_WAI_CLOSE_NAVIGATION_OUTCOME =
            CircabcNavigationHandler.WAI_PREFIX
                    + CLOSE_NAVIGATION_OUTCOME;
    public static final int DEFAULT_LIST_ELEMENT_NUMBER = 10;
    public static final String MSG_ACCESS_DENIED = "permissions.err_access_denied";
    public static final String MSG_NODE_DELETED = "browse_error_node_deleted";
    public static final String MSG_NODE_UNAVAILABLE = "browse_generic_error_node_unavailable";
    public static final String MSG_REDIRECTED_TO = "browse_redirection_to";
    public static final String MSG_FROM_TOPIC = "advanced_search_from_topic";
    public static final String ALTERNATIVE_DOWNLOAD_URL = "alternativeDownloadUrl";
    public static final String ALTERNATIVE_BROWSE_URL = "alternativeBrowseUrl";
    private static final Log logger = LogFactory.getLog(CircabcBrowseBean.class);
    private static final String SIGNATURE_START_HTML = "---------------<br />";
    private static final String MESSAGE_SEPARATOR = " - ";
    private static final String MSG_ABUSE_REPORT = "post_moderation_abuse_detail";
    private static final String MSG_REJECT_DETAILS = "post_moderation_reject_detail";
    private static final String NO_MESSAGE = " / ";
    private static final String REGISTRED = "registred";
    private static final String ANONYMOUS = "anonymous";
    private static final String GUEST = CircabcConstant.GUEST_AUTHORITY;
    private static final String END_BRACKETS = "'))]";
    private static final String SUBTYPE_OF = "./*[(subtypeOf('";
    private static final String SLASH = "/";
    private static final String ID = "id";
    private static final String ICON = "icon";
    private static final String OPEN_BLOCKQUOTE = "<br /><blockquote><p>";
    private static final String CLOSE_BLOCKQUOTE = "</p></blockquote>";
    private static final String BR = "<br>";
    private static final String SLASH_SPACE = "/ ";
    private static final String NULL = "null";
    private static final long serialVersionUID = 2991898295440580141L;
    private static final String RESOLVER_NAME = "resolverTopicPosts";
    /**
     * Resolver to get the trunckated string of the name with 100 characters and "..." if too long
     * (&gt; 100)
     */
    public NodePropertyResolver resolverTrunckedOneHundredName = new NodePropertyResolver() {
        private static final long serialVersionUID = -2532907181898250014L;

        public Object get(final Node node) {
            return UtilsCircabc.cropEncodeLight(node.getName(), 100);
        }
    };
    /**
     * Resolver to get the URL of the post attachment.
     */
    public NodePropertyResolver resolverAttachmentDownload = new NodePropertyResolver() {

        private static final long serialVersionUID = 4048859853585650378L;

        public Object get(Node node) {
            return DownloadContentServlet.generateDownloadURL(node.getNodeRef(), node.getName()) +
                    "?property=" + DocumentModel.PROP_CONTENT;
        }
    };
    /**
     * Resolver to get the trunckated string of the name with 400 characters and "..." if too long
     * (&gt; 400)
     */
    public NodePropertyResolver resolverTrunckedFourHundredName = new NodePropertyResolver() {
        private static final long serialVersionUID = 5928155255659015928L;

        public Object get(final Node node) {
            final String title = (String) node.getProperties().get(ContentModel.PROP_TITLE.toString());
            if (title == null || title.trim().length() < 1) {
                return UtilsCircabc.cropEncodeLight(node.getName(), 400);
            } else {
                return UtilsCircabc.cropEncodeLight(title, 400);
            }
        }
    };
    /**
     * Resolver to get a more beautiful display path
     */
    public NodePropertyResolver resolverSimpleDisplayPath = new NodePropertyResolver() {
        private static final long serialVersionUID = 7626985426368819373L;

        public Object get(final Node node) {
            // TODO : Better method - go up and stop to Library or news group (2
            // method diff) and display name and /
            final StringBuilder buf = new StringBuilder(64);
            final Path path = node.getNodePath();
            // We don't want the the leaf
            final int count = path.size() - 1;
            // Find the library Node
            String elementString;
            Path.Element element;
            ChildAssociationRef elementRef;
            for (int i = 5; i < count; i++) {
                elementString = null;
                element = path.get(i);
                if (element instanceof Path.ChildAssocElement) {
                    elementRef = ((Path.ChildAssocElement) element).getRef();
                    if (elementRef.getParentRef() != null) {
                        elementString = elementRef.getQName().getLocalName();
                    }
                } else {
                    elementString = element.getElementString();
                }

                if (elementString != null) {
                    buf.append(elementString);
                    buf.append(SLASH);
                }
            }

            return buf.toString();
        }
    };
    /**
     * Resolver to get a more beautiful display path
     */
    public NodePropertyResolver resolverSimpleDisplayPathLight = new NodePropertyResolver() {
        private static final long serialVersionUID = 7987625426368819373L;

        public Object get(final Node node) {
            final Object displayPath = node.getProperties().get(ResolverHelper.DISPLAY_PATH);

            if (displayPath == null) {
                return NULL;
            }

            return displayPath.toString().replaceAll(SLASH, SLASH_SPACE);
        }
    };
    /**
     * Resolver to get a more beautiful display path
     */
    public NodePropertyResolver resolverShortName = new NodePropertyResolver() {
        private static final long serialVersionUID = 7987532426368819373L;

        public Object get(final Node node) {
            final String name = node.getName();
            if (name == null) {
                return NULL;
            }

            int max = name.length();

            return name.substring(0, max > SHORTNAME_LEN ? SHORTNAME_LEN : max);
        }
    };
    /**
     * Resolver to get the number of replies on a topic
     */
    public NodePropertyResolver resolverReplies = new NodePropertyResolver() {
        private static final long serialVersionUID = -2619261180780344575L;

        public Object get(final Node node) {
            final FacesContext context = FacesContext.getCurrentInstance();
            final ServiceRegistry serviceRegistry = Services.getAlfrescoServiceRegistry(context);

            // TODO : Alfesco code -> optimize avec lucene ?
            // query for the number of posts within the given node
            final String repliesXPath = SUBTYPE_OF + ForumModel.TYPE_POST + END_BRACKETS;

            final List<NodeRef> replies = serviceRegistry.getSearchService()
                    .selectNodes(node.getNodeRef(), repliesXPath,
                            new QueryParameterDefinition[]{}, serviceRegistry.getNamespaceService(), false);

            // reduce the count by 1 as one of the posts will be the initial
            // post
            int noReplies = replies.size() - 1;

            if (noReplies < 0) {
                noReplies = 0;
            }

            return noReplies;
        }
    };
    /**
     * Resolver to get the number of replies on a topic
     */
    public NodePropertyResolver resolverPost = new NodePropertyResolver() {
        private static final long serialVersionUID = -2619261180780344575L;

        public Object get(final Node node) {
            final NodeRef topic = getTopic(node.getNodeRef());
            final String postBestTitle = getBestTitle(topic);
            return postBestTitle;
        }

        private String getBestTitle(final NodeRef nodeRef) {
            final Node node = new Node(nodeRef);
            final String title = (String) node.getProperties().get(ContentModel.PROP_TITLE.toString());

            if (title == null || title.trim().length() < 1) {
                return node.getName();
            } else {
                return title;
            }
        }

        private NodeRef getTopic(final NodeRef post) {
            final NodeService nodeService = getNodeService();
            NodeRef parent = nodeService.getPrimaryParent(post).getParentRef();

            while (!ForumModel.TYPE_TOPIC.equals(nodeService.getType(parent))) {
                parent = nodeService.getPrimaryParent(post).getParentRef();
            }

            return parent;
        }
    };
    /**
     * Resolver to get the content of a post (400 max version). Not encoded
     */
    public NodePropertyResolver resolverContentFourHundred = new NodePropertyResolver() {
        private static final long serialVersionUID = -4872929142142064529L;

        public Object get(final Node node) {
            String content = "";
            final FacesContext context = FacesContext.getCurrentInstance();
            final ServiceRegistry serviceRegistry = Services.getAlfrescoServiceRegistry(context);

            // get the content property from the node and retrieve the full
            // content as a string (obviously should only be used for small
            // amounts of content)
            final ContentReader reader = serviceRegistry.getContentService()
                    .getReader(node.getNodeRef(), ContentModel.PROP_CONTENT);
            if (reader != null) {
                content = UtilsCircabc.cropLight(reader.getContentString(), 400);
            }

            return UtilsCircabc.getFirstLines(content, BR, 8);
        }
    };
    /**
     * Resolver to get the trunckated string of the parent's name with 50 characters and "..." if too
     * long
     */
    public NodePropertyResolver resolverFromTopic = new NodePropertyResolver() {
        private static final long serialVersionUID = 5928155255673485928L;

        public Object get(final Node node) {
            final NodeService nodeService = getNodeService();
            final NodeRef parent = nodeService.getPrimaryParent(node.getNodeRef()).getParentRef();

            final Node topic = new Node(parent);
            final String name = topic.getName();
            final String title = (String) topic.getProperties().get(ContentModel.PROP_TITLE.toString());

            final String copedName = UtilsCircabc
                    .cropEncodeLight((title == null || title.length() < 0) ? name : title, 50);
            final FacesContext fc = FacesContext.getCurrentInstance();

            String buf =
                    "" + MessageFormat.format(Application.getMessage(fc, MSG_FROM_TOPIC), copedName) +
                            OPEN_BLOCKQUOTE + resolverContentFourHundred.get(node) + CLOSE_BLOCKQUOTE;

            return buf;
        }
    };
    /**
     * Resolver to get the content of a post (fullversion)
     */
    public NodePropertyResolver resolverContent = new NodePropertyResolver() {
        private static final long serialVersionUID = -8543304828699792888L;

        public Object get(final Node node) {
            String content = null;
            final FacesContext context = FacesContext.getCurrentInstance();
            final ServiceRegistry serviceRegistry = Services.getAlfrescoServiceRegistry(context);

            // get the content property from the node and retrieve the full
            // content as a string (obviously should only be used for small
            // amounts of content)
            final ContentReader reader = serviceRegistry.getContentService()
                    .getReader(node.getNodeRef(), ContentModel.PROP_CONTENT);
            if (reader != null) {
                content = reader.getContentString();
            }

            return content;
        }
    };
    /**
     * Resolver to get the trunckated string of the name with 100 characters and "..." if too long
     * (&gt; 100)
     */
    public NodePropertyResolver resolverBrowseWaiUrl = new NodePropertyResolver() {
        private static final long serialVersionUID = -2532907187778250014L;

        public Object get(final Node node) {
            return WebClientHelper.getGeneratedWaiFullUrl(node, ExtendedURLMode.HTTP_WAI_BROWSE);
        }
    };
    /**
     * Resolver to get the node creator username (ecas moniker if existing)
     */
    public NodePropertyResolver resolverCreatorUserName = new NodePropertyResolver() {
        private static final long serialVersionUID = -2532907187778250014L;

        public Object get(final Node node) {
            final FacesContext context = FacesContext.getCurrentInstance();
            final ServiceRegistry serviceRegistry = Services.getAlfrescoServiceRegistry(context);
            final PersonService personService = serviceRegistry.getPersonService();

            final String creator = (String) node.getProperties()
                    .get(ContentModel.PROP_CREATOR.toString());

            if (creator != null && creator.equals(CircabcConstant.GUEST_AUTHORITY) == false) {
                if (personService.personExists(creator)) {
                    try {
                        final NodeRef person = personService.getPerson(creator);
                        final Map<QName, Serializable> properties = getNodeService().getProperties(person);
                        final String firstName = (String) properties.get(ContentModel.PROP_FIRSTNAME);
                        final String lastName = (String) properties.get(ContentModel.PROP_LASTNAME);

                        if (firstName == null && lastName == null) {
                            return PermissionUtils.computeUserLogin(properties);
                        } else {
                            return (firstName == null ? "" : firstName + " ") + (lastName == null ? ""
                                    : lastName);
                        }
                    } catch (AccessDeniedException ade) {
                        // guest can't see ADMIN details
                        return creator;
                    }

                } else {
                    return creator;
                }

            } else {
                return Application.getMessage(context, GUEST);
            }
        }
    };
    /**
     * Resolver to get if a node is moderated
     */
    public NodePropertyResolver resolverLastPost = new NodePropertyResolver() {
        private static final long serialVersionUID = -2532907187778259999L;

        public Object get(final Node node) {
            Date lastDate = null;
            FileInfo lastPost = null;

            for (final FileInfo fileInfo : getPosts(node)) {
                if (lastDate == null || lastDate.before(fileInfo.getCreatedDate())) {
                    lastDate = fileInfo.getCreatedDate();
                    lastPost = fileInfo;
                }
            }

            if (lastPost != null) {
                final FacesContext fc = FacesContext.getCurrentInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                final String formatedDate = sdf.format(lastDate);
                return formatedDate + " (" + lastPost.getProperties().get(ContentModel.PROP_CREATOR) + ")";
            } else {
                return "";
            }
        }
    };
    public NodePropertyResolver resolverAlternativeDownload = new NodePropertyResolver() {
        private static final long serialVersionUID = 4540744401261261034L;

        public Object get(Node node) {
            return SimpleDownloadServlet.generateDownloadURL(node.getNodeRef(), node.getName());
        }
    };
    public NodePropertyResolver resolverAlternativeBrowse = new NodePropertyResolver() {

        /**
         *
         */
        private static final long serialVersionUID = -7272718275573622821L;

        public Object get(Node node) {
            return SimpleDownloadServlet.generateBrowseURL(node.getNodeRef(), node.getName());
        }
    };
    /**
     * Resolver to get the node creator profile
     */
    public NodePropertyResolver resolverCreatorProfile = new NodePropertyResolver() {
        private static final long serialVersionUID = -2532907187778250014L;

        public Object get(final Node node) {
            final FacesContext context = FacesContext.getCurrentInstance();
            final ServiceRegistry serviceRegistry = Services.getAlfrescoServiceRegistry(context);
            final PersonService personService = serviceRegistry.getPersonService();
            final AuthorityService authorityService = serviceRegistry.getAuthorityService();

            final String creator = (String) node.getProperties()
                    .get(ContentModel.PROP_CREATOR.toString());

            if (creator != null && personService.personExists(creator)) {
                if (authorityService.isAdminAuthority(creator)) {
                    return "Administrator";
                } else if (creator.equals(CircabcConstant.GUEST_AUTHORITY)) {
                    return Application.getMessage(context, ANONYMOUS);
                } else {
                    final CircabcServiceRegistry circabcServiceRegistry = Services
                            .getCircabcServiceRegistry(context);
                    final ManagementService managementService = circabcServiceRegistry.getManagementService();

                    final NodeRef nodeRef = node.getNodeRef();
                    final NodeRef igRoot = managementService.getCurrentInterestGroup(nodeRef);

                    // try to get IGRoot profile
                    String profileName = getProfileName(
                            circabcServiceRegistry.getIGRootProfileManagerService(), igRoot, creator);

                    // else try to get Category profile
                    if (profileName == null) {
                        profileName = getProfileName(circabcServiceRegistry.getCategoryProfileManagerService(),
                                managementService.getCurrentCategory(igRoot), creator);
                    }

                    // else try to get Root profile
                    if (profileName == null) {
                        profileName = getProfileName(
                                circabcServiceRegistry.getCircabcRootProfileManagerService(),
                                managementService.getCircabcNodeRef(), creator);
                    }

                    return profileName == null ? Application.getMessage(context, REGISTRED) : profileName;
                }
            } else {
                return "";
            }

        }

        private String getProfileName(final ProfileManagerService profileService, final NodeRef nodeRef,
                                      final String creator) {
            final String profileStr = profileService.getPersonProfile(nodeRef, creator);
            if (profileStr != null) {
                final Profile profile = profileService.getProfile(nodeRef, profileStr);
                return profile.getProfileDisplayName();
            } else {
                return null;
            }
        }
    };
    /**
     * Resolver to get the number of elements in a space
     */
    public NodePropertyResolver resolverSpaceSize = new NodePropertyResolver() {
        private static final long serialVersionUID = -2619269988780344575L;

        public Object get(final Node node) {

            final FacesContext context = FacesContext.getCurrentInstance();
            final ServiceRegistry serviceRegistry = Services.getAlfrescoServiceRegistry(context);
            final NodeRef ref = node.getNodeRef();

            List<FileInfo> childs;

            try {
                childs = serviceRegistry.getFileFolderService().list(ref);
            } catch (AccessDeniedException e) {
                childs = null;
            }

            return (childs == null ? "N/A" : childs.size());
        }
    };
    /**
     * Resolver to get the number of documents in a space
     */
    public NodePropertyResolver resolverDocumentNumber = new NodePropertyResolver() {
        private static final long serialVersionUID = -1111169988780344575L;

        public Object get(final Node node) {

            final FacesContext context = FacesContext.getCurrentInstance();
            final ServiceRegistry serviceRegistry = Services.getAlfrescoServiceRegistry(context);
            final NodeRef ref = node.getNodeRef();

            List<FileInfo> childs;

            try {
                childs = serviceRegistry.getFileFolderService().listFiles(ref);
            } catch (AccessDeniedException e) {
                childs = null;
            }

            return (childs == null ? "N/A" : childs.size());
        }
    };
    private Integer cachedListElementNumber;
    private transient BusinessRegistry businessRegistry;

    // All circabc resolver
    private transient CircabcServiceRegistry serviceRegistry;
    /**
     * Resolver to get if a node is moderated
     */
    public NodePropertyResolver resolverIsModerated = new NodePropertyResolver() {
        private static final long serialVersionUID = -2532907187778259999L;

        public Object get(final Node node) {
            return getModerationService().isContainerModerated(node.getNodeRef());
        }
    };
    /**
     * Resolver to get if a node is waiting for approval
     */
    public NodePropertyResolver resolverWaitingForApproval = new NodePropertyResolver() {
        private static final long serialVersionUID = -2532909999778250014L;

        public Object get(final Node node) {
            return getModerationService().isWaitingForApproval(node.getNodeRef());
        }
    };
    /**
     * Resolver to get if a node is Approved (moderation process)
     */
    public NodePropertyResolver resolverIsApproved = new NodePropertyResolver() {
        private static final long serialVersionUID = -2532909999778250014L;

        public Object get(final Node node) {
            return getModerationService().isApproved(node.getNodeRef());
        }
    };
    /**
     * Resolver to get if a node is Rejected (moderation process)
     */
    public NodePropertyResolver resolverIsRejected = new NodePropertyResolver() {
        private static final long serialVersionUID = -5329099997788790014L;

        public Object get(final Node node) {
            return getModerationService().isRejected(node.getNodeRef());
        }
    };
    /**
     * Resolver to get if abuse is signaled on this node (moderation process)
     */
    public NodePropertyResolver resolverIsAbuseSignaled = new NodePropertyResolver() {
        private static final long serialVersionUID = -5329099997788790014L;

        public Object get(final Node node) {
            return getModerationService().getAbuses(node.getNodeRef()).size() > 0;
        }
    };
    /**
     * Resolver to get if a node is moderated
     */
    public NodePropertyResolver resolverNumberWaitingPost = new NodePropertyResolver() {
        private static final long serialVersionUID = -2532907187778259999L;

        public Object get(final Node node) {
            int count = 0;

            if (getModerationService().isContainerModerated(node.getNodeRef())) {
                for (final FileInfo fileInfo : getPosts(node)) {
                    if (getModerationService().isWaitingForApproval(fileInfo.getNodeRef())) {
                        count++;
                    }
                }
            }

            return count;
        }
    };
    /**
     * Resolver to get if a node is moderated
     */
    public NodePropertyResolver resolverNumberRejectedPost = new NodePropertyResolver() {
        private static final long serialVersionUID = -2532907187778259999L;

        public Object get(final Node node) {
            int count = 0;

            if (getModerationService().isContainerModerated(node.getNodeRef())) {
                for (final FileInfo fileInfo : getPosts(node)) {
                    if (getModerationService().isRejected(fileInfo.getNodeRef())) {
                        count++;
                    }
                }
            }

            return count;
        }
    };
    /**
     * Resolver to get if a node is moderated
     */
    public NodePropertyResolver resolverNumberApprovedPost = new NodePropertyResolver() {
        private static final long serialVersionUID = -2532907187778259999L;

        public Object get(final Node node) {
            int count = 0;

            if (getModerationService().isContainerModerated(node.getNodeRef())) {
                for (final FileInfo fileInfo : getPosts(node)) {
                    if (getModerationService().isApproved(fileInfo.getNodeRef())) {
                        count++;
                    }
                }
            }

            return count;
        }
    };
    /**
     * Resolver to the I18N message for Report An Abuse
     */
    public NodePropertyResolver resolverAbuseMessages = new NodePropertyResolver() {
        private static final long serialVersionUID = -2532909999778111999L;

        public Object get(final Node node) {
            final List<AbuseReport> reports = getModerationService().getAbuses(node.getNodeRef());
            if (reports.size() > 0) {
                final StringBuilder buff = new StringBuilder("");
                final FacesContext fc = FacesContext.getCurrentInstance();
                boolean first = true;
                for (final AbuseReport report : reports) {
                    final String reporter = report.getReporter();
                    final Date date = report.getReportDate();
                    final String message = report.getMessage();
                    final String safeMessage =
                            (message == null || message.trim().length() < 1) ? NO_MESSAGE : message.replace(
                                    "\n", " ");
                    final String i18nMessage = MessageFormat
                            .format(Application.getMessage(fc, MSG_ABUSE_REPORT), reporter, date,
                                    safeMessage);

                    if (first) {
                        first = false;
                    } else {
                        buff.append(MESSAGE_SEPARATOR);
                    }

                    buff.append(i18nMessage);
                }

                return buff.toString();
            } else {
                return "";
            }

        }
    };
    /**
     * Resolver to the I18N message for Reject a moderated post
     */
    public NodePropertyResolver resolverRejectMessages = new NodePropertyResolver() {
        private static final long serialVersionUID = -2532909999778111999L;

        public Object get(final Node node) {
            if (getModerationService().isRejected(node.getNodeRef())) {
                final FacesContext fc = FacesContext.getCurrentInstance();
                final String moderator = (String) node.getProperties()
                        .get(ModerationModel.PROP_REJECT_BY.toString());
                final Date date = (Date) node.getProperties()
                        .get(ModerationModel.PROP_REJECT_ON.toString());
                final String message = (String) node.getProperties()
                        .get(ModerationModel.PROP_REJECT_MESSAGE.toString());
                final String safeMessage =
                        (message == null || message.trim().length() < 1) ? NO_MESSAGE : message.replace("\n",
                                " ");
                final String i18nMessage = MessageFormat
                        .format(Application.getMessage(fc, MSG_REJECT_DETAILS), moderator, date,
                                safeMessage);

                return i18nMessage;
            } else {
                return "";
            }

        }
    };
    /**
     * Resolver to the I18N message for Reject a moderated post
     */
    public NodePropertyResolver resolverHasAttachement = new NodePropertyResolver() {
        private static final long serialVersionUID = -2532909977441111999L;

        public Object get(final Node node) {
            final boolean hasAttachement =
                    getAttachementService().getAttachements(node.getNodeRef()).size() > 0;
            return hasAttachement;
        }
    };
    /**
     * Resolver to get the node creator profile
     */
    public NodePropertyResolver resolverCreatorAvatar = new NodePropertyResolver() {
        private static final long serialVersionUID = -1132907187778250014L;

        public Object get(final Node node) {
            final PersonService personService = getServiceRegistry().getAlfrescoServiceRegistry()
                    .getPersonService();
            final UserDetailsBusinessSrv userDetailsService = getBusinessRegistry()
                    .getUserDetailsBusinessSrv();

            final String creator = (String) node.getProperties()
                    .get(ContentModel.PROP_CREATOR.toString());

            NodeRef avatarRef = null;

            if (creator != null && personService.personExists(creator)) {
                try {
                    final NodeRef person = personService.getPerson(creator);

                    avatarRef = userDetailsService.getAvatar(person);
                } catch (AccessDeniedException ade) {
                    // guest can't see ADMIN details
                } catch (final Exception ignore) {
                    // don't stop the process if any error occus.
                    if (logger.isErrorEnabled()) {
                        logger.error("Impossible to retreive avatar of " + creator, ignore);
                    }
                }
            }

            if (avatarRef == null) {
                avatarRef = userDetailsService.getDefaultAvatar();
            }

            return WebClientHelper.getGeneratedWaiUrl(avatarRef, ExtendedURLMode.HTTP_DOWNLOAD, true);
        }
    };
    /**
     * Resolver to get the node creator profile
     */
    public NodePropertyResolver resolverCreatorSignature = new NodePropertyResolver() {
        private static final long serialVersionUID = -2532907187778251114L;

        public Object get(final Node node) {
            final PersonService personService = getServiceRegistry().getAlfrescoServiceRegistry()
                    .getPersonService();
            final UserDetailsBusinessSrv userDetailsService = getBusinessRegistry()
                    .getUserDetailsBusinessSrv();

            final String creator = (String) node.getProperties()
                    .get(ContentModel.PROP_CREATOR.toString());

            String signature = null;

            if (creator != null && personService.personExists(creator)) {
                try {
                    final NodeRef person = personService.getPerson(creator);

                    signature = userDetailsService.getUserDetails(person).getSignature();
                } catch (final AccessDeniedException ade) {
                    // guest can't see admin details
                } catch (final Exception ignore) {
                    // don't stop the process if any error occus.
                    if (logger.isErrorEnabled()) {
                        logger.error("Impossible to retreive avatar of " + creator, ignore);
                    }
                }
            }

            if (signature == null || signature.trim().length() == 0) {
                return "";
            } else {
                return SIGNATURE_START_HTML + signature.replace("\n", "<br />");
            }
        }
    };
    private SearchResultDialog searchResultDialog;

    public void setupContentAction(String id, boolean invalidate) {
        super.setupContentAction(id, invalidate);
        Node node = getDocument();
        if (node != null) {
            node.addPropertyResolver(ALTERNATIVE_DOWNLOAD_URL, this.resolverAlternativeDownload);
            node.addPropertyResolver(ALTERNATIVE_BROWSE_URL, this.resolverAlternativeBrowse);
        }
    }

    /**
     * Action called to display the circabc home page (already created or not)
     */
    public void clickCircabcHome() {
        final Node igHomeNode = getCircabcNavigator().getCircabcHomeNode();

        if (igHomeNode != null) {
            clickWai(igHomeNode.getNodeRef());
        } else {
            if (logger.isWarnEnabled()) {
                logger.warn("Goto circabc Root");
            }
            WaiApplication.getNavigationManager().initNavigation(WelcomeBean.BEAN_NAME);
            // nothing else to do ... stay to the welcome.
        }
    }

    public void clickCircabcHome(final ActionEvent event) {
        this.clickCircabcHome();
    }

    /**
     * Refresh the actual state of the navigation bean with the same values.
     */
    public void refreshBrowsing() {
        if (logger.isDebugEnabled()) {
            logger.debug("refreshBrowsing");
        }
        final Node node = getCircabcNavigator().getCurrentNode();

        if (node == null) {
            this.clickCircabcHome(null);
        } else {
            clickWai(node.getNodeRef());
        }
    }

    /**
     * Action called when a folder space is clicked in a WAI page. Have an accessible navigation into
     * the space.
     */
    public void clickWai(final ActionEvent event) {

        RetryingTransactionCallback<Object> txnWork = new RetryingTransactionCallback<Object>() {
            public Object execute() throws Exception {
                String id = null;
                Map<String, String> params = null;
                final UIComponent component = event.getComponent();
                if (component instanceof UINavigationList) {
                    final UINavigationList navigationList = (UINavigationList) component;
                    id = navigationList.getId();
                    id = id.substring(1);
                } else if (component instanceof UIActionLink) {
                    final UIActionLink link = (UIActionLink) component;
                    params = link.getParameterMap();
                    id = params.get(ID);
                } else {
                    final UICommand command = (UICommand) component;
                    final List<?> childs = command.getChildren();
                    params = new HashMap<>(childs.size());

                    for (final Object child : childs) {
                        if (child instanceof UIParameter) {
                            params.put(((UIParameter) child).getName(),
                                    ((UIParameter) child).getValue().toString());
                        }
                    }

                    id = params.get(ID);
                }

                clickWai(id);

                if (params != null && params.size() > 1) {
                    WaiApplication.getNavigationManager().setParamsToApply(params);
                } else {
                    // don't set params if contains only ID
                    WaiApplication.getNavigationManager().setParamsToApply(null);
                }

                return null;
            }
        };

        final FacesContext context = FacesContext.getCurrentInstance();
        final RetryingTransactionHelper txnHelper = Repository.getRetryingTransactionHelper(context);
        // Migration 3.1 -> 3.4.6 - 20/12/2011 - Changed read to read/write transaction
        txnHelper.doInTransaction(txnWork, false);
    }

    /**
     * Action called when a folder space is clicked in a WAI page. Have an accessible navigation into
     * the space.
     */
    public void clickWai(final String id) {
        if (id != null && id.length() != 0) {
            try {
                NodeRef ref = new NodeRef(Repository.getStoreRef(), id);
                FacesContext fc = FacesContext.getCurrentInstance();
                // handle special folder/content link node case
                if (ApplicationModel.TYPE_FOLDERLINK.equals(getNodeService().getType(ref))) {
                    ref = (NodeRef) getNodeService().getProperty(ref, ContentModel.PROP_LINK_DESTINATION);

                    fc.getExternalContext().getSessionMap()
                            .put(LibraryBean.FROM_LINK_TO_SHARED, new NodeRef(Repository.getStoreRef(), id));
                    fc.getExternalContext().getSessionMap().put(LibraryBean.TARGET_GROUP_LINK_TO_SHARED, ref);
                }

                clickWai(ref);
            } catch (final InvalidNodeRefException refErr) {
                Utils.addErrorMessage(MessageFormat.format(
                        Application.getMessage(FacesContext.getCurrentInstance(), Repository.ERROR_NODEREF),
                        id));
            }
        }
    }

    /**
     * Action called when a folder space is clicked in a WAI page. Have an accessible navigation into
     * the space.
     */
    public void clickWai(final NodeRef ref) {

        boolean isRunNotSecured = false;
        String userName = AuthenticationUtil.getRunAsUser();
        boolean canRead =
                getPermissionService().hasPermission(ref, PermissionService.READ) == AccessStatus.ALLOWED;
        boolean isLibNoAccess =
                getPermissionService().hasPermission(ref, LibraryPermissions.LIBNOACCESS.toString())
                        == AccessStatus.ALLOWED;
        isRunNotSecured = (!canRead && isLibNoAccess);
        if (isRunNotSecured) {
            try {
                AuthenticationUtil.setRunAsUserSystem();
                clickWaiSecure(ref);
            } finally {
                AuthenticationUtil.setRunAsUser(userName);
            }
        } else {
            clickWaiSecure(ref);
        }
    }

    /**
     * @param ref
     */
    private void clickWaiSecure(final NodeRef ref) {
        NodeRef refToSend = getSecureNodeRef(ref);

        if (!refToSend.equals(ref)) {
            final FacesContext fc = FacesContext.getCurrentInstance();

            if (getPermissionService().hasPermission(ref, PermissionService.READ)
                    != AccessStatus.ALLOWED) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Access Denied on node:" + ref);
                }

                Utils.addErrorMessage(I18NUtil.getMessage(MSG_ACCESS_DENIED));
            } else if (!getNodeService().exists(ref)) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Deleted node:" + ref);
                }
                Utils.addErrorMessage(Application.getMessage(fc, MSG_NODE_DELETED));
            } else {
                if (logger.isWarnEnabled()) {
                    logger.warn("Unavailable node:" + ref);
                }
                Utils.addErrorMessage(Application.getMessage(fc, MSG_NODE_UNAVAILABLE));
            }

            getCircabcNavigator().setCurrentNodeId(refToSend.getId());

            final Serializable nodeName = getNodeService().getProperty(refToSend, ContentModel.PROP_NAME);
            Utils.addErrorMessage(
                    MessageFormat.format(Application.getMessage(fc, MSG_REDIRECTED_TO), nodeName));
        }

        final FacesContext fc = FacesContext.getCurrentInstance();

        final Node node = new Node(refToSend);

        if (!AspectResolver.isNodeManagedByCircabc(node)) {
            if (logger.isWarnEnabled()) {
                logger.warn("redirected to circabc root node");
            }
            refToSend = getCircabcNavigator().getCircabcHomeNode().getNodeRef();
        }

        fc.getApplication().getNavigationHandler()
                .handleNavigation(fc, refToSend.getId(), PREFIXED_WAI_BROWSE_OUTCOME);

        final QName modelType = node.getType();

        // make sure the type is defined in the data dictionary
        final TypeDefinition typeDef = getDictionaryService().getType(modelType);

        if (typeDef != null) {
            // look for Space or File nodes
            if ((ContentModel.TYPE_FOLDER.equals(modelType) || getDictionaryService()
                    .isSubClass(modelType,
                            ContentModel.TYPE_FOLDER))
                    && (ApplicationModel.TYPE_FOLDERLINK.equals(modelType) || getDictionaryService()
                    .isSubClass(modelType,
                            ApplicationModel.TYPE_FOLDERLINK))) {
                setActionSpace(node);
                // resolve icon in-case one has not been set
                node.addPropertyResolver(ICON, this.resolverSpaceIcon);
                setDocument(null);
            } else {
                setDocument(node);
                setActionSpace(null);
            }
        }
    }

    /**
     * Action called when a folder space is clicked in a WAI page. Have an accessible navigation into
     * the space.
     */
    public void clickWaiIgService(final ActionEvent event) {
        final UIActionLink link = (UIActionLink) event.getComponent();
        final Map<String, String> params = link.getParameterMap();
        final String service = params.get("service");
        final String id = params.get(ID);

        clickWai(id);

        getCircabcNavigator().activateIGService(service);

        applyWaiBrowsing(getCircabcNavigator().getCurrentIGService().getId(), false);
    }

    /**
     * Refresh the actual state of the navigation bean with the parent of the curent node.
     */
    public void clickParent() {
        Node node = null;
        final NavigableNodeType type = getCircabcNavigator().getCurrentNodeType();

        if (type != null) {
            switch (type) {
                case CIRCABC_ROOT:
                    // do nothing and refresh the view
                    if (logger.isDebugEnabled()) {
                        logger.debug("clickParent:goto circabc root");
                    }
                    break;
                case CATEGORY_HEADER:
                    if (logger.isDebugEnabled()) {
                        logger.debug("clickParent:goto circabc root");
                    }
                    node = getCircabcNavigator().getCircabcHomeNode();
                    break;
                case LIBRARY_ML_CONTENT:
                    final NodeRef pivot = getMultilingualContentService().getPivotTranslation(
                            getCircabcNavigator().getCurrentNode().getNodeRef());
                    node = new Node(pivot);
                    if (logger.isDebugEnabled()) {
                        logger.debug("clickParent:goto pivot translation");
                    }
                    break;
                case EVENT_APPOINTMENT:
                    node = (Node) getCircabcNavigator().getCurrentIGRoot()
                            .get(InterestGroupNode.EVENT_SERVICE);
                    if (logger.isDebugEnabled()) {
                        logger.debug("clickParent:goto Event Service");
                    }
                    break;
                default:
                    final NodeRef parentRef = getNodeService()
                            .getPrimaryParent(getCircabcNavigator().getCurrentNode().getNodeRef())
                            .getParentRef();
                    node = new Node(parentRef);
                    if (logger.isDebugEnabled()) {
                        logger.debug("clickParent:goto parent");
                    }
                    break;
            }
        }

        if (node == null) {
            if (logger.isWarnEnabled()) {
                logger.warn("goto circabc Home");
            }
            this.clickCircabcHome(null);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("goto node:" + node);
            }
            clickWai(node.getNodeRef());
        }
    }

    // ------------------------------------------------------------------------------
    // Property Resolvers

    /**
     * perform the navigation with the dicscussion of the given node
     */
    public void clickNodeDiscussion(final ActionEvent event) {

        final UIActionLink link = (UIActionLink) event.getComponent();
        final Map<String, String> params = link.getParameterMap();
        final String id = params.get(ID);

        if (id != null && id.length() != 0) {
            try {
                NodeRef ref = new NodeRef(Repository.getStoreRef(), id);

                // handle special folder link node case
                if (ApplicationModel.TYPE_FOLDERLINK.equals(getNodeService().getType(ref))) {
                    ref = (NodeRef) getNodeService().getProperty(ref, ContentModel.PROP_LINK_DESTINATION);
                }

                if (getNodeService().hasAspect(ref, ForumModel.ASPECT_DISCUSSABLE) == false) {
                    if (logger.isErrorEnabled()) {
                        logger.error("discuss called for an object that does not have a discussion! " + id);
                    }

                    Utils.addErrorMessage(
                            "Discuss called for an object that does not have a discussion! " + id);
                } else {
                    // as the node has the discussable aspect there must be a
                    // discussions child assoc
                    final List<ChildAssociationRef> children = getNodeService()
                            .getChildAssocs(ref, ForumModel.ASSOC_DISCUSSION,
                                    RegexQNamePattern.MATCH_ALL);

                    // there should only be one child, retrieve it if there is
                    if (children.size() == 1) {
                        // show the forum (without s) for the discussion
                        ref = children.get(0).getChildRef();
                    } else {
                        // this should never happen as the action evaluator
                        // should stop the action
                        // from displaying, just in case print a warning to the
                        // console
                        if (logger.isWarnEnabled()) {
                            logger.warn(
                                    "Node has the discussable aspect but does not have 1 child, it has " + children
                                            .size()
                                            + " children!");
                        }

                        throw new InvalidNodeRefException(
                                "Node has the discussable aspect but does not have 1 child, it has "
                                        + children.size() + " children!", ref);
                    }

                    clickWai(ref);

                }
            } catch (final InvalidNodeRefException refErr) {
                if (logger.isErrorEnabled()) {
                    logger.error("InvalidNodeRefException:" + id, refErr);
                }
                Utils.addErrorMessage(MessageFormat.format(
                        Application.getMessage(FacesContext.getCurrentInstance(), Repository.ERROR_NODEREF),
                        id));
            }
        }
    }

    /**
     * @param event the navigation via the ml content details of the given node
     */
    public void clickWaiMLContainer(final ActionEvent event) {

        final UIActionLink link = (UIActionLink) event.getComponent();
        final Map<String, String> params = link.getParameterMap();
        final String id = params.get(ID);

        if (id != null && id.length() != 0) {
            try {
                NodeRef ref = new NodeRef(Repository.getStoreRef(), id);

                ref = getMultilingualContentService().getTranslationContainer(ref);

                clickWai(ref);
            } catch (final InvalidNodeRefException refErr) {
                if (logger.isErrorEnabled()) {
                    logger.error("InvalidNodeRefException:" + id, refErr);
                }
                Utils.addErrorMessage(MessageFormat.format(
                        Application.getMessage(FacesContext.getCurrentInstance(), Repository.ERROR_NODEREF),
                        id));
            }
        }
    }

    /**
     * Click to browse the current space into the native interface. The browsing is secured, only a
     * subclass of Folder can be navigable inside the native interface.
     */
    public void clickNativeSpace(final ActionEvent event) {
        final UIActionLink link = (UIActionLink) event.getComponent();
        final Map<String, String> params = link.getParameterMap();
        final String id = params.get(ID);
        NodeRef ref = null;
        QName type = null;

        if (id != null && id.length() != 0) {
            try {
                ref = new NodeRef(Repository.getStoreRef(), id);
                type = getNodeService().getType(ref);
            } catch (final InvalidNodeRefException refErr) {
                if (logger.isErrorEnabled()) {
                    logger.error("InvalidNodeRefException:" + id, refErr);
                }
                Utils.addErrorMessage(MessageFormat.format(
                        Application.getMessage(FacesContext.getCurrentInstance(), Repository.ERROR_NODEREF),
                        id));
            }
        }

        if (getNodeService().getType(ref).equals(ContentModel.TYPE_MULTILINGUAL_CONTAINER)) {
            ref = getMultilingualContentService().getPivotTranslation(ref);
            if (logger.isDebugEnabled()) {
                logger.debug("node is a multilingual node, get the pivot translation:" + ref);
            }
        }

        if (getNodeService().hasAspect(ref, CircabcModel.ASPECT_LIBRARY)) {
            if (!(ContentModel.TYPE_FOLDER.equals(type) || getDictionaryService()
                    .isSubClass(type, ContentModel.TYPE_FOLDER))) {
                ref = getNodeService().getPrimaryParent(ref).getParentRef();
                if (logger.isDebugEnabled()) {
                    logger.debug("node is not a Folder (sub) type, goto parent:" + ref);
                }
            }
        }

        clickSpace(ref);
    }

    /**
     * Action called from the Simple Search component. Sets up the SearchContext object with the
     * values from the simple search menu.
     */
    public void searchWai(final ActionEvent event) {
        final UIComponent component = event.getComponent();

        UISimpleSearch search = null;

        if (component instanceof UISimpleSearch) {
            search = (UISimpleSearch) component;
        } else {
            search = (UISimpleSearch) component.getParent();
        }

        getCircabcNavigator().setSearchContext(search.getSearchContext());
        final FacesContext fc = FacesContext.getCurrentInstance();

        if (search.getSearchContext() != null) {
            getSearchResultDialog().reset();

            fc.getApplication().getNavigationHandler()
                    .handleNavigation(fc, null, SearchResultDialog.WAI_DIALOG_CALL);
        } else {
            fc.getApplication().getNavigationHandler()
                    .handleNavigation(fc, null, AdvancedSearchDialog.WAI_DIALOG_CALL);
        }

        final ViewHandler viewHandler = fc.getApplication().getViewHandler();
        final UIViewRoot viewRoot = viewHandler
                .createView(fc, CircabcNavigationHandler.WAI_DIALOG_CONTAINER_PAGE);
        viewRoot.setViewId(CircabcNavigationHandler.WAI_DIALOG_CONTAINER_PAGE);
        fc.setViewRoot(viewRoot);
        fc.renderResponse();
    }

    /**
     * This method is called when the navigation parameters are ready to be activated.
     */
    public void applyWaiBrowsing(final String nodeId, final boolean informListeners) {
        NavigableNodeType currentNodeType = null;

        if (nodeId != null) {
            // set the current node Id ready for page refresh
            getCircabcNavigator().setCurrentNodeId(nodeId);

            // set up the dispatch context for the navigation handler
            getCircabcNavigator()
                    .setupDispatchContext(new Node(new NodeRef(Repository.getStoreRef(), nodeId)));

            if (informListeners) {
                // inform any listeners that the current space has changed
                UIContextService.getInstance(FacesContext.getCurrentInstance()).spaceChanged();
            }

            currentNodeType = getCircabcNavigator().getCurrentNodeType();

        }

        if (currentNodeType != null) {
            WaiApplication.setNavigationManager(currentNodeType);
        }
    }

    /**
     * @return the number of elements that should be displayed in a common rich list.
     */
    public int getListElementNumber() {
        if (cachedListElementNumber == null) {
            final String numberStr = CircabcConfiguration
                    .getProperty(CircabcConfiguration.DEFAULT_LIST_ELEMENT_NUMBER_PROPERTIES);

            int response = DEFAULT_LIST_ELEMENT_NUMBER;

            if (numberStr != null && numberStr.length() > 1) {
                try {
                    response = Integer.parseInt(numberStr);
                } catch (final NumberFormatException ex) {
                    logger.error("The property file " + CircabcConfiguration.DEFAULT_PROPERTY_FILE
                            + " is corrupted. The value of the key "
                            + CircabcConfiguration.DEFAULT_LIST_ELEMENT_NUMBER_PROPERTIES
                            + " must be a valid integer and not " + numberStr
                            + "\nPlease correct the problem and restart the server. For instance the value used by default is "
                            + DEFAULT_LIST_ELEMENT_NUMBER);
                }
            } else {
                logger.error("The property file " + CircabcConfiguration.DEFAULT_PROPERTY_FILE
                        + " not complete. The value of the key "
                        + CircabcConfiguration.DEFAULT_LIST_ELEMENT_NUMBER_PROPERTIES
                        + " must be set and must be a valid integer and not "
                        + "\nPlease correct the problem and restart the server. For instance the value used by default is "
                        + DEFAULT_LIST_ELEMENT_NUMBER);
            }

            cachedListElementNumber = response;
        }

        return cachedListElementNumber;
    }

    /**
     * Get the list for navigation level one inside the title bar
     *
     * @return <Node> The list for the title bar showing the current Circabc
     * Categorie and IG Root if browsed
     */
    public List<Node> getNavigationListTitleBar() {
        final List<Node> result = new LinkedList<>();

        final CircabcNavigationBean nav = getCircabcNavigator();

        if (nav.getCurrentIGRoot() != null) {
            result.add(nav.getCurrentIGRoot());
        }
        if (nav.getCurrentCategory() != null) {
            ((LinkedList<Node>) result).addFirst(nav.getCurrentCategory());
        }

        return result;
    }

    protected final CircabcNavigationBean getCircabcNavigator() {
        return (CircabcNavigationBean) this.navigator;
    }

    @SuppressWarnings("unchecked")
    public List<FileInfo> getPosts(final Node node) {
        List<FileInfo> childs = null;

        boolean isMap = node instanceof Map;
        if (isMap) {
            final Map<?, ?> map = (Map<?, ?>) node;
            childs = (List<FileInfo>) map.get(RESOLVER_NAME);
        }

        if (childs == null) {
            final NodeRef nodeRef = node.getNodeRef();
            childs = listAllFiles(nodeRef);

            if (isMap) {
                ((Map<String, List<FileInfo>>) node).put(RESOLVER_NAME, childs);
            }
        }

        return childs;
    }

    /**
     * @param nodeRef
     * @return
     */
    private List<FileInfo> listAllFiles(final NodeRef nodeRef) {
        final List<FileInfo> files = getFileFolderService().listFiles(nodeRef);
        for (final FileInfo fileInfo : getFileFolderService().listFolders(nodeRef)) {
            files.addAll(listAllFiles(fileInfo.getNodeRef()));
        }
        return files;
    }

    // ------------------------------------------------------------------------------
    // Helper methods

    private NodeRef getSecureNodeRef(final NodeRef fromNodeRef) {
        if (getNodeService().exists(fromNodeRef)
                && getPermissionService().hasPermission(fromNodeRef, PermissionService.READ)
                == AccessStatus.ALLOWED) {
            return fromNodeRef;
        } else {
            if (logger.isWarnEnabled()) {
                if (!getNodeService().exists(fromNodeRef)) {
                    logger.warn("Node reference does not exists : " + fromNodeRef.toString());
                } else if (getPermissionService().hasPermission(fromNodeRef, PermissionService.READ)
                        != AccessStatus.ALLOWED) {
                    logger.warn("User " + AuthenticationUtil.getFullyAuthenticatedUser()
                            + " does not have read permission on node  : " + fromNodeRef.toString());
                }
            }
            if (getCircabcNavigator().getCurrentNodeId() == null) {
                return getCircabcNavigator().getCircabcHomeNode().getNodeRef();
            }
            if (isNodeSecure(getDocument())) {
                return getDocument().getNodeRef();
            } else if (isNodeSecure(getCircabcNavigator().getCurrentNode())) {
                return getCircabcNavigator().getCurrentNode().getNodeRef();
            } else if (isNodeSecure(getCircabcNavigator().getCurrentIGService())) {
                return getCircabcNavigator().getCurrentIGService().getNodeRef();
            } else if (isNodeSecure(getCircabcNavigator().getCurrentIGRoot())) {
                return getCircabcNavigator().getCurrentIGRoot().getNodeRef();
            } else if (isNodeSecure(getCircabcNavigator().getCurrentCategory())) {
                return getCircabcNavigator().getCurrentCategory().getNodeRef();
            } else if (isNodeSecure(getCircabcNavigator().getCurrentCategoryHeader())) {
                return getCircabcNavigator().getCurrentCategoryHeader().getNodeRef();
            } else {
                return getCircabcNavigator().getCircabcHomeNode().getNodeRef();
            }
        }
    }

    public boolean isNodeSecure(final Node node) {
        if (node == null) {
            return false;
        } else {
            return isNodeSecure(node.getNodeRef());
        }
    }

    private boolean isNodeSecure(final NodeRef nodeRef) {
        if (nodeRef == null) {
            return false;
        } else {
            if (getPermissionService().hasPermission(nodeRef, PermissionService.READ)
                    == AccessStatus.ALLOWED) {
                return getNodeService().exists(nodeRef);
            } else {
                return false;
            }
        }
    }

    /**
     * @return the searchResultDialog
     */
    protected final SearchResultDialog getSearchResultDialog() {
        if (searchResultDialog == null) {
            searchResultDialog = Beans.getSearchResultDialog();
        }
        return searchResultDialog;
    }

    protected final ModerationService getModerationService() {
        return getServiceRegistry().getModerationService();
    }

    protected final AttachementService getAttachementService() {
        return getServiceRegistry().getAttachementService();
    }

    /**
     * @return the permissionService
     */
    protected final PermissionService getPermissionService() {
        return getServiceRegistry().getAlfrescoServiceRegistry().getPermissionService();
    }

    /**
     * @return the businessRegistry
     */
    protected final BusinessRegistry getBusinessRegistry() {
        if (businessRegistry == null) {
            businessRegistry = Services.getBusinessRegistry(FacesContext.getCurrentInstance());
        }
        return businessRegistry;
    }

    /**
     * @param businessRegistry the businessRegistry to set
     */
    public final void setBusinessRegistry(BusinessRegistry businessRegistry) {
        this.businessRegistry = businessRegistry;
    }

    /**
     * @return the serviceRegistry
     */
    protected final CircabcServiceRegistry getServiceRegistry() {
        if (serviceRegistry == null) {
            serviceRegistry = Services.getCircabcServiceRegistry(FacesContext.getCurrentInstance());
        }
        return serviceRegistry;
    }

    /**
     * @param serviceRegistry the serviceRegistry to set
     */
    public final void setServiceRegistry(CircabcServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public String getBannerRenderPropertyName() {

        NavigableNode categoryNode = getCircabcNavigator().getCurrentCategory();
        String renderPropertyName = "";

        if (categoryNode != null && categoryNode.getProperties()
                .containsKey(CircabcModel.PROP_NAVIGATION_LIST_RENDER_TYPE.toString())) {
            /*
             * if = shortname or title to be used for banner
             */
            renderPropertyName = categoryNode.getProperties()
                    .get(CircabcModel.PROP_NAVIGATION_LIST_RENDER_TYPE.toString()).toString();

        } else {
            renderPropertyName = "shortname";
        }

        return renderPropertyName;
    }
}
