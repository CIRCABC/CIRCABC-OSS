package io.swagger.api;

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.model.ModerationModel;
import eu.cec.digit.circabc.service.customisation.mail.MailPreferencesService;
import eu.cec.digit.circabc.service.customisation.mail.MailTemplate;
import eu.cec.digit.circabc.service.customisation.mail.MailWrapper;
import eu.cec.digit.circabc.service.mail.MailService;
import eu.cec.digit.circabc.service.newsgroup.AbuseReport;
import eu.cec.digit.circabc.service.newsgroup.ModerationService;
import eu.cec.digit.circabc.service.struct.ManagementService;
import eu.cec.digit.circabc.web.wai.dialog.forums.ModerationDialog;
import io.swagger.model.*;
import io.swagger.util.Converter;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.node.getchildren.GetChildrenCannedQuery;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.FileFilterMode;
import org.alfresco.util.FileFilterMode.Client;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.*;

/**
 * @author beaurpi
 */
public class ForumsApiImpl implements ForumsApi {

    private static final Log logger = LogFactory.getLog(ModerationDialog.class);
    String[] moderatorPermissions = new String[]{"LibAdmin", "NwsModerate", "NwsAdmin"};
    private NodesApi nodesApi;
    private UsersApi usersApi;
    private ProfilesApi profilesApi;
    private GroupsApi groupsApi;
    private NodeService secureNodeService;
    private ModerationService moderationService;
    private PersonService personService;
    private MailPreferencesService mailPreferencesService;
    private MailService mailService;
    private ContentService contentService;
    private ManagementService managementService;
    private FileFolderService fileFolderService;

    @Override
    public List<Node> getForumById(String id) {
        NodeRef forumRef = Converter.createNodeRefFromId(id);
        List<Node> result = new ArrayList<>();

        if (secureNodeService.hasAspect(forumRef, CircabcModel.ASPECT_NEWSGROUP)) {

            PagingRequest pr = new PagingRequest(0, 10000);
            List<Pair<QName, Boolean>> sortProps = new ArrayList<>(1);

            Pair<QName, Boolean> sortFolderFirstPair =
                    new Pair<>(GetChildrenCannedQuery.SORT_QNAME_NODE_IS_FOLDER, false);

            sortProps.add(sortFolderFirstPair);

            Pair<QName, Boolean> sortPair = new Pair<>(ContentModel.PROP_MODIFIED, false);
            sortProps.add(sortPair);

            FileFilterMode.setClient(Client.cmis);
            final PagingResults<FileInfo> list =
                    getFileFolderService().list(forumRef, true, true, null, sortProps, pr);
            FileFilterMode.clearClient();

            for (FileInfo item : list.getPage()) {
                final NodeRef childRef = item.getNodeRef();
                result.add(nodesApi.getNode(childRef));
            }
        }

        return result;
    }

    /**
     * @return the nodesApi
     */
    public NodesApi getNodesApi() {
        return nodesApi;
    }

    /**
     * @param nodesApi the nodesApi to set
     */
    public void setNodesApi(NodesApi nodesApi) {
        this.nodesApi = nodesApi;
    }

    /**
     * @param usersApi the usersApi to set
     */
    public void setUsersApi(UsersApi usersApi) {
        this.usersApi = usersApi;
    }

    /**
     * @param profilesApi the profilesApi to set
     */
    public void setProfilesApi(ProfilesApi profilesApi) {
        this.profilesApi = profilesApi;
    }

    /**
     * @param groupsApi the groupsApi to set
     */
    public void setGroupsApi(GroupsApi groupsApi) {
        this.groupsApi = groupsApi;
    }

    /**
     * @return the secureNodeService
     */
    public NodeService getSecureNodeService() {
        return secureNodeService;
    }

    /**
     * @param secureNodeService the secureNodeService to set
     */
    public void setSecureNodeService(NodeService secureNodeService) {
        this.secureNodeService = secureNodeService;
    }

    /**
     * @param moderationService the moderationService to set
     */
    public void setModerationService(ModerationService moderationService) {
        this.moderationService = moderationService;
    }

    /**
     * @param personService the personService to set
     */
    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    /**
     * @param mailPreferencesService the mailPreferencesService to set
     */
    public void setMailPreferencesService(MailPreferencesService mailPreferencesService) {
        this.mailPreferencesService = mailPreferencesService;
    }

    /**
     * @param mailService the mailService to set
     */
    public void setMailService(MailService mailService) {
        this.mailService = mailService;
    }

    /**
     * @param contentService the contentService to set
     */
    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    /**
     * @param managementService the managementService to set
     */
    public void setManagementService(ManagementService managementService) {
        this.managementService = managementService;
    }

    @Override
    public Node forumsIdSubforumsPost(String id, Node body) {
        NodeRef parentRef = Converter.createNodeRefFromId(id);

        NodeRef nodeRef = createNode(body, parentRef, ForumModel.TYPE_FORUM);
        MLText titles = Converter.toMLText(body.getTitle());
        secureNodeService.setProperty(nodeRef, ContentModel.PROP_TITLE, titles);

        return nodesApi.getNode(nodeRef);
    }

    private NodeRef createNode(Node body, NodeRef parentRef, QName nodeType) {
        if (!(secureNodeService.getType(parentRef).equals(ForumModel.TYPE_FORUM)
                || secureNodeService.getType(parentRef).equals(ForumModel.TYPE_FORUMS))) {
            throw new InvalidTypeException(
                    "Node creation failed, the parent noderef does not have the type forum",
                    ContentModel.TYPE_FOLDER);
        }

        QName nameQName =
                QName.createQName(
                        NamespaceService.FORUMS_MODEL_1_0_URI, QName.createValidLocalName(body.getName()));

        ChildAssociationRef newSpace =
                secureNodeService.createNode(parentRef, ContentModel.ASSOC_CONTAINS, nameQName, nodeType);
        NodeRef nodeRef = newSpace.getChildRef();
        secureNodeService.setProperty(nodeRef, ContentModel.PROP_NAME, body.getName());
        MLText titles = Converter.toMLText(body.getTitle());
        secureNodeService.setProperty(nodeRef, ContentModel.PROP_TITLE, titles);
        MLText descriptions = Converter.toMLText(body.getDescription());
        secureNodeService.setProperty(nodeRef, ContentModel.PROP_DESCRIPTION, descriptions);
        return nodeRef;
    }

    @Override
    public Node forumsIdContentPost(String id, Node body) {
        NodeRef parentRef = Converter.createNodeRefFromId(id);
        NodeRef nodeRef = createNode(body, parentRef, ForumModel.TYPE_TOPIC);
        return nodesApi.getNode(nodeRef);
    }

    /**
     * @see io.swagger.api.ForumsApi#forumsIdDelete(java.lang.String)
     */
    @Override
    public void forumsIdDelete(String id) {

        NodeRef forumRef = getForumNodeRef(id);

        secureNodeService.deleteNode(forumRef);
    }

    /**
     * @see io.swagger.api.ForumsApi#updateForum(java.lang.String, io.swagger.model.Node)
     */
    @Override
    public void updateForum(String id, Node forumNode) {

        NodeRef forumRef = getForumNodeRef(id);

        I18nProperty title = forumNode.getTitle();
        if (title != null) {
            secureNodeService.setProperty(forumRef, ContentModel.PROP_TITLE, Converter.toMLText(title));
        }
        // for a forum only update these properties and ignore the rest

        I18nProperty description = forumNode.getDescription();
        if (description != null) {
            secureNodeService.setProperty(
                    forumRef, ContentModel.PROP_DESCRIPTION, Converter.toMLText(description));
        }

        secureNodeService.setProperty(forumRef, ContentModel.PROP_NAME, forumNode.getName());
    }

    private NodeRef getForumNodeRef(String id) {

        NodeRef forumRef = Converter.createNodeRefFromId(id);

        if (!secureNodeService.exists(forumRef)) {
            throw new IllegalArgumentException("The forum with id " + id + " could not be found.");
        }

        if (!ForumModel.TYPE_FORUM.equals(secureNodeService.getType(forumRef))) {
            throw new InvalidTypeException(
                    "The given id does not correspond to a node of type forum", ForumModel.TYPE_FORUM);
        }

        return forumRef;
    }

    /**
     * @see io.swagger.api.ForumsApi#toggleModeration(java.lang.String, boolean, boolean)
     */
    @Override
    public void toggleModeration(String id, boolean enable, boolean acceptAll) {

        NodeRef forumRef = Converter.createNodeRefFromId(id);

        Serializable moderationStatusSerializable =
                secureNodeService.getProperty(forumRef, ModerationModel.PROP_IS_MODERATED);

        boolean moderationEnabled = false;

        if (moderationStatusSerializable != null) {
            moderationEnabled = Boolean.parseBoolean(moderationStatusSerializable.toString());
        }

        if (enable && !moderationEnabled) {
            moderationService.applyModeration(forumRef, false);
        } else if (!enable && moderationEnabled) {
            moderationService.stopModeration(forumRef, acceptAll ? "accept" : "refuse");
        }
    }

    /**
     * @see io.swagger.api.ForumsApi#verifyPost(java.lang.String, boolean, java.lang.String)
     */
    @Override
    public void verifyPost(String id, boolean approve, String rejectReason) throws Exception {

        NodeRef postRef = Converter.createNodeRefFromId(id);

        if (approve) {
            moderationService.accept(postRef);
        } else {
            reject(postRef, rejectReason == null ? "" : rejectReason);
        }
    }

    private void reject(final NodeRef postRef, final String message) throws Exception {

        // get the content for mailing before it is versioned
        final String oldContent =
                contentService.getReader(postRef, ContentModel.PROP_CONTENT).getContentString();

        moderationService.reject(postRef, message);

        final String creator =
                (String) secureNodeService.getProperty(postRef, ContentModel.PROP_CREATOR);

        if (personService.personExists(creator)) {

            // the properties are not refreshed in ActionNodeYet
            final Map<QName, Serializable> props = secureNodeService.getProperties(postRef);
            final Date moderated = (Date) props.get(ModerationModel.PROP_REJECT_ON);
            final String reason = (String) props.get(ModerationModel.PROP_REJECT_MESSAGE);

            final NodeRef creatorRef = personService.getPerson(creator);
            User user = usersApi.usersUserIdGet(creator);
            final String creatorEmail = user.getEmail();
            String noReply = mailService.getNoReplyEmailAddress();

            final Locale locale = new Locale(user.getUiLang());
            final Map<String, Object> model =
                    mailPreferencesService.buildDefaultModel(postRef, creatorRef, null);
            model.put(MailTemplate.KEY_REJECT_DATE, moderated);
            model.put(MailTemplate.KEY_REJECT_REASON, (reason == null) ? "" : reason);
            model.put(MailTemplate.KEY_REJECTED_CONTENT, (oldContent == null) ? "" : oldContent);
            final MailWrapper mail =
                    mailPreferencesService.getDefaultMailTemplate(postRef, MailTemplate.REJECT_POST);

            mailService.send(
                    noReply,
                    creatorEmail,
                    null,
                    mail.getSubject(model, locale),
                    mail.getBody(model, locale),
                    true,
                    false);
        } else {
            if (logger.isWarnEnabled()) {
                logger.warn(
                        "The creator '"
                                + creator
                                + "' of the post '"
                                + postRef
                                + "' could not be found. Rejection mail not sent.");
            }
        }
    }

    /**
     * @see io.swagger.api.ForumsApi#getSignaledAbuses(java.lang.String)
     */
    @Override
    public List<AbuseReport> getSignaledAbuses(String id) {

        NodeRef postRef = Converter.createNodeRefFromId(id);

        return moderationService.getAbuses(postRef);
    }

    /**
     * @see io.swagger.api.ForumsApi#signalAbuse(java.lang.String, java.lang.String)
     */
    @Override
    public void signalAbuse(String id, String abuseText) throws Exception {

        NodeRef postRef = Converter.createNodeRefFromId(id);

        signalAbuse(postRef, abuseText == null ? "" : abuseText);
    }

    /**
     * @see io.swagger.api.ForumsApi#removeAbuses(java.lang.String)
     */
    @Override
    public void removeAbuses(String id) {

        NodeRef postRef = Converter.createNodeRefFromId(id);

        moderationService.signalNotAbuse(postRef);
    }

    protected void signalAbuse(NodeRef nodeRef, final String message) throws Exception {

        final AbuseReport report = moderationService.signalAbuse(nodeRef, message);

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Node successfully signaled as abuse: " + nodeRef + ". With message: " + message);
        }

        final Set<NodeRef> moderators = getModerators(nodeRef);

        if (logger.isDebugEnabled()) {
            logger.debug(moderators.size() + " moderator(s) found for : " + nodeRef);
        }

        // get the current user and properties, he is the reporter
        User reporter = usersApi.usersUserIdGet(report.getReporter());
        final String reporterEmail = reporter.getEmail();

        MLPropertyInterceptor.setMLAware(false);

        for (final NodeRef moderator : moderators) {

            final String moderatorEmail =
                    (String) secureNodeService.getProperty(moderator, ContentModel.PROP_EMAIL);
            String noReply = mailService.getNoReplyEmailAddress();
            String moderatorUserId =
                    (String) secureNodeService.getProperty(moderator, ContentModel.PROP_USERNAME);
            User moderatorUser = usersApi.usersUserIdGet(moderatorUserId);
            final Locale locale = new Locale(moderatorUser.getUiLang());
            final Map<String, Object> model =
                    mailPreferencesService.buildDefaultModel(nodeRef, moderator, null);
            model.put(MailTemplate.KEY_ABUSE_DATE, report.getReportDate());
            model.put(
                    MailTemplate.KEY_ABUSE_REASON, (report.getMessage() == null) ? "" : report.getMessage());
            final MailWrapper mail =
                    mailPreferencesService.getDefaultMailTemplate(nodeRef, MailTemplate.SIGNAL_ABUSE);

            mailService.send(
                    noReply,
                    moderatorEmail,
                    reporterEmail,
                    mail.getSubject(model, locale),
                    mail.getBody(model, locale),
                    true,
                    false);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Mails successfully sent.");
        }
    }

    protected Set<NodeRef> getModerators(NodeRef postRef) {

        final NodeRef topicRef = secureNodeService.getPrimaryParent(postRef).getParentRef();
        final NodeRef igRef = managementService.getCurrentInterestGroup(topicRef);

        List<UserProfile> moderators = new ArrayList<>();

        // get all profiles of the given IG
        List<Profile> profiles = profilesApi.groupsIdProfilesGet(igRef.getId(), "", false);

        // filter out those profiles that are not LibAdmin, NwsModerate or NwsAdmin
        for (Profile profile : profiles) {

            if (!hasPermission(profile.getPermissions(), moderatorPermissions)) {
                continue;
            }

            // build each profile name as a list, because the API expect them so
            List<String> profileToCheck = new ArrayList<>();
            profileToCheck.add(profile.getGroupName());

            // get the members of the profileToCheck and add them to the moderators list
            PagedUserProfile pagedUserProfile =
                    groupsApi.groupsIdMembersGet(igRef.getId(), profileToCheck, null, -1, 0, null, "");

            moderators.addAll(pagedUserProfile.getData());
        }

        // get the nodeRef for each moderator given its user id and add it to the result list
        final Set<NodeRef> moderatorsRef = new HashSet<>(moderators.size());

        for (UserProfile moderator : moderators) {

            final String userName = moderator.getUser().getUserId();

            if (personService.personExists(userName)) {

                NodeRef personRef = personService.getPerson(userName);

                // avoid duplicates
                moderatorsRef.add(personRef);
            }
        }

        return moderatorsRef;
    }

    private boolean hasPermission(Map<String, String> permissionMap, String[] permissions) {

        // check if the map of permissions includes at least one of the
        // permissions being passed
        for (String permission : permissions) {
            if (permissionMap.containsValue(permission)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public List<Node> forumsIdSubforumsGet(String id) {
        NodeRef forumRef = Converter.createNodeRefFromId(id);
        List<Node> result = new ArrayList<>();

        if (secureNodeService.hasAspect(forumRef, CircabcModel.ASPECT_NEWSGROUP)) {

            List<ChildAssociationRef> children = secureNodeService.getChildAssocs(forumRef);

            for (ChildAssociationRef item : children) {

                if (secureNodeService.getType(item.getChildRef()).equals(ForumModel.TYPE_FORUM)) {
                    final NodeRef childRef = item.getChildRef();
                    result.add(nodesApi.getNode(childRef));
                }
            }
        }

        return result;
    }

    @Override
    public List<Node> forumsIdSubforumsGet(String id, String sorting) {

        List<Node> result = new ArrayList<>();

        NodeRef forumRef = Converter.createNodeRefFromId(id);

        PagingRequest pr = new PagingRequest(0, 10000);
        List<Pair<QName, Boolean>> sortProps = new ArrayList<>(1);

        Pair<QName, Boolean> sortFolderFirstPair =
                new Pair<>(GetChildrenCannedQuery.SORT_QNAME_NODE_IS_FOLDER, false);

        sortProps.add(sortFolderFirstPair);

        if (!sorting.equalsIgnoreCase("")) {
            Pair<QName, Boolean> sortPair =
                    new Pair<>(
                            QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, sorting.split("_")[0]),
                            (sorting.endsWith("ASC")));
            sortProps.add(sortPair);
        }

        Set<QName> ignored = new HashSet<>();
        ignored.add(ForumModel.TYPE_TOPIC);

        FileFilterMode.setClient(Client.cmis);
        final PagingResults<FileInfo> list =
                getFileFolderService().list(forumRef, false, true, ignored, sortProps, pr);
        FileFilterMode.clearClient();

        for (FileInfo item : list.getPage()) {
            final NodeRef childRef = item.getNodeRef();
            result.add(nodesApi.getNode(childRef));
        }

        return result;
    }

    /**
     * @return the fileFolderService
     */
    public FileFolderService getFileFolderService() {
        return fileFolderService;
    }

    /**
     * @param fileFolderService the fileFolderService to set
     */
    public void setFileFolderService(FileFolderService fileFolderService) {
        this.fileFolderService = fileFolderService;
    }
}
