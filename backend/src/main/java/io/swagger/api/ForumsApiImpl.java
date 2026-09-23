package io.swagger.api;

import eu.europa.ec.digit.circabc.rest.service.mail.MailPreferencesService;
import eu.europa.ec.digit.circabc.rest.service.mail.MailService;
import eu.europa.ec.digit.circabc.rest.service.mail.MailTemplate;
import eu.europa.ec.digit.circabc.rest.service.mail.MailWrapper;
import eu.europa.ec.digit.circabc.rest.service.newsgroup.ModerationService;
import io.swagger.model.AbuseReport;
import io.swagger.model.I18nProperty;
import io.swagger.model.Node;
import io.swagger.model.PagedNodes;
import io.swagger.model.PagedUserProfile;
import io.swagger.model.Profile;
import io.swagger.model.User;
import io.swagger.model.UserProfile;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.model.alfresco.ModerationModel;
import io.swagger.util.ApiToolBox;
import io.swagger.util.Converter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.node.getchildren.GetChildrenCannedQuery;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.FileFilterMode;
import org.alfresco.util.FileFilterMode.Client;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Default implementation of {@link ForumsApi} that contains the business logic
 * for the CIRCABC Newsgroup / Forum service.
 *
 * <p>This service operates on Alfresco nodes of type {@code fm:forum},
 * {@code fm:forums} and {@code fm:topic}. It supports listing forum contents
 * (with optional pagination and sorting), creating sub-forums and topics,
 * updating and deleting forums, as well as the moderation workflow: toggling
 * moderation on a forum, approving or rejecting posts, and handling abuse
 * reports. Rejection and abuse events trigger notification e-mails sent through
 * the {@link MailService} using templates resolved by the
 * {@link MailPreferencesService}.</p>
 *
 * <p>Collaborating APIs ({@link NodesApi}, {@link UsersApi},
 * {@link ProfilesApi}, {@link GroupsApi}) and Alfresco services are injected via
 * Spring {@code @Autowired} wiring.</p>
 *
 * @author beaurpi
 */
public class ForumsApiImpl implements ForumsApi {

  /** Logger used to report mail sending failures and moderation warnings. */
  private static final Log logger = LogFactory.getLog(ForumsApiImpl.class);

  /**
   * CIRCABC profile permissions that identify a user as a forum moderator.
   * A profile holding at least one of these permissions is entitled to
   * moderate posts and receive abuse notifications.
   */
  String[] moderatorPermissions = new String[] {
    "LibAdmin",
    "NwsModerate",
    "NwsAdmin",
  };

  @Autowired
  private NodesApi nodesApi;

  @Autowired
  private UsersApi usersApi;

  @Autowired
  private ProfilesApi profilesApi;

  @Autowired
  private GroupsApi groupsApi;

  @Autowired
  private NodeService nodeService;

  @Autowired
  private ModerationService moderationService;

  @Autowired
  private PersonService personService;

  @Autowired
  private MailPreferencesService mailPreferencesService;

  @Autowired
  private MailService mailService;

  @Autowired
  private ContentService contentService;

  @Autowired
  private FileFolderService fileFolderService;

  @Autowired
  private ApiToolBox apiToolBox;

  /**
   * Returns the direct children of a newsgroup forum, sorted with folders first
   * and then by descending modification date.
   *
   * <p>If the given node does not carry the
   * {@link CircabcModel#ASPECT_NEWSGROUP} aspect, an empty list is returned.</p>
   *
   * @param id the identifier of the forum node
   * @return the list of child {@link Node}s of the forum, or an empty list if
   *         the node is not a newsgroup
   */
  @Override
  public List<Node> getForumById(String id) {
    NodeRef forumRef = Converter.createNodeRefFromId(id);
    List<Node> result = new ArrayList<>();

    if (nodeService.hasAspect(forumRef, CircabcModel.ASPECT_NEWSGROUP)) {
      PagingRequest pr = new PagingRequest(0, 10000);
      List<Pair<QName, Boolean>> sortProps = new ArrayList<>(1);

      Pair<QName, Boolean> sortFolderFirstPair = new Pair<>(
        GetChildrenCannedQuery.SORT_QNAME_NODE_IS_FOLDER,
        false
      );

      sortProps.add(sortFolderFirstPair);

      Pair<QName, Boolean> sortPair = new Pair<>(
        ContentModel.PROP_MODIFIED,
        false
      );
      sortProps.add(sortPair);

      FileFilterMode.setClient(Client.cmis);
      final PagingResults<FileInfo> list = getFileFolderService().list(
        forumRef,
        true,
        true,
        null,
        sortProps,
        pr
      );
      FileFilterMode.clearClient();

      for (FileInfo item : list.getPage()) {
        final NodeRef childRef = item.getNodeRef();
        result.add(nodesApi.getNode(childRef));
      }
    }

    return result;
  }

  /**
   * Returns a paginated view of the children of a newsgroup forum, sorted with
   * folders first and then by descending modification date.
   *
   * <p>If the given node does not carry the
   * {@link CircabcModel#ASPECT_NEWSGROUP} aspect, an empty result is returned.
   * When {@code limit} is greater than zero and {@code page} is non-negative,
   * the returned data is limited to the requested page window; otherwise all
   * children are returned. The total count reflects the full number of children
   * regardless of pagination.</p>
   *
   * @param id    the identifier of the forum node
   * @param page  the zero-based page index
   * @param limit the maximum number of items per page; values &lt;= 0 disable
   *              pagination
   * @param sort  the requested sort criterion (currently not applied)
   * @return a {@link PagedNodes} holding the requested page of children and the
   *         total number of children
   */
  @Override
  public PagedNodes getForumById(String id, int page, int limit, String sort) {
    PagedNodes pagedResult = new PagedNodes();
    NodeRef forumRef = Converter.createNodeRefFromId(id);
    List<Node> result = new ArrayList<>();

    if (nodeService.hasAspect(forumRef, CircabcModel.ASPECT_NEWSGROUP)) {
      PagingRequest pr = new PagingRequest(0, 100000);
      List<Pair<QName, Boolean>> sortProps = new ArrayList<>(1);

      Pair<QName, Boolean> sortFolderFirstPair = new Pair<>(
        GetChildrenCannedQuery.SORT_QNAME_NODE_IS_FOLDER,
        false
      );

      sortProps.add(sortFolderFirstPair);

      Pair<QName, Boolean> sortPair = new Pair<>(
        ContentModel.PROP_MODIFIED,
        false
      );
      sortProps.add(sortPair);

      FileFilterMode.setClient(Client.cmis);
      final PagingResults<FileInfo> list = getFileFolderService().list(
        forumRef,
        true,
        true,
        null,
        sortProps,
        pr
      );
      FileFilterMode.clearClient();
      List<FileInfo> items = list.getPage();

      int start = 0;
      int end = items.size();

      if (limit > 0 && page >= 0) {
        start = page * limit;
        end = (Math.min(start + limit, items.size()));
      }

      for (int i = start; i < end; i++) {
        final NodeRef childRef = items.get(i).getNodeRef();
        result.add(nodesApi.getNode(childRef));
      }

      pagedResult.setTotal((long) getFileFolderService().list(forumRef).size());
      pagedResult.setData(result);
    }

    return pagedResult;
  }

  /**
   * Creates a new sub-forum (node of type {@code fm:forum}) under the given
   * parent forum and applies the supplied localized title.
   *
   * @param id   the identifier of the parent forum node
   * @param body the {@link Node} describing the sub-forum to create (name,
   *             title and description)
   * @return the newly created sub-forum as a {@link Node}
   * @throws org.alfresco.service.cmr.dictionary.InvalidTypeException if the
   *         parent node is not a forum or forums container
   */
  @Override
  public Node forumsIdSubforumsPost(String id, Node body) {
    NodeRef parentRef = Converter.createNodeRefFromId(id);

    NodeRef nodeRef = createNode(body, parentRef, ForumModel.TYPE_FORUM);
    MLText titles = Converter.toMLText(body.getTitle());
    nodeService.setProperty(nodeRef, ContentModel.PROP_TITLE, titles);

    return nodesApi.getNode(nodeRef);
  }

  /**
   * Creates a child node under the given parent forum and initializes its name,
   * title and description properties.
   *
   * @param body      the {@link Node} carrying the name, title and description
   *                  to apply to the new node
   * @param parentRef the reference of the parent forum or forums container
   * @param nodeType  the Alfresco type ({@code QName}) of the node to create
   *                  (e.g. {@code fm:forum} or {@code fm:topic})
   * @return the {@link NodeRef} of the newly created node
   * @throws org.alfresco.service.cmr.dictionary.InvalidTypeException if the
   *         parent node is neither a forum nor a forums container
   */
  private NodeRef createNode(Node body, NodeRef parentRef, QName nodeType) {
    if (
      !(nodeService.getType(parentRef).equals(ForumModel.TYPE_FORUM) ||
        nodeService.getType(parentRef).equals(ForumModel.TYPE_FORUMS))
    ) {
      throw new InvalidTypeException(
        "Node creation failed, the parent noderef does not have the type forum",
        ContentModel.TYPE_FOLDER
      );
    }

    QName nameQName = QName.createQName(
      NamespaceService.FORUMS_MODEL_1_0_URI,
      QName.createValidLocalName(body.getName())
    );

    ChildAssociationRef newSpace = nodeService.createNode(
      parentRef,
      ContentModel.ASSOC_CONTAINS,
      nameQName,
      nodeType
    );
    NodeRef nodeRef = newSpace.getChildRef();
    nodeService.setProperty(nodeRef, ContentModel.PROP_NAME, body.getName());
    MLText titles = Converter.toMLText(body.getTitle());
    nodeService.setProperty(nodeRef, ContentModel.PROP_TITLE, titles);
    MLText descriptions = Converter.toMLText(body.getDescription());
    nodeService.setProperty(
      nodeRef,
      ContentModel.PROP_DESCRIPTION,
      descriptions
    );
    return nodeRef;
  }

  /**
   * Creates a new topic (node of type {@code fm:topic}) under the given forum.
   *
   * @param id   the identifier of the parent forum node
   * @param body the {@link Node} describing the topic to create (name, title
   *             and description)
   * @return the newly created topic as a {@link Node}
   * @throws org.alfresco.service.cmr.dictionary.InvalidTypeException if the
   *         parent node is not a forum or forums container
   */
  @Override
  public Node forumsIdContentPost(String id, Node body) {
    NodeRef parentRef = Converter.createNodeRefFromId(id);
    NodeRef nodeRef = createNode(body, parentRef, ForumModel.TYPE_TOPIC);
    return nodesApi.getNode(nodeRef);
  }

  /**
   * Deletes the forum identified by the given id.
   *
   * @param id the identifier of the forum node to delete
   * @throws IllegalArgumentException if no node exists for the given id
   * @throws org.alfresco.service.cmr.dictionary.InvalidTypeException if the
   *         node is not of type forum
   * @see io.swagger.api.ForumsApi#forumsIdDelete(java.lang.String)
   */
  @Override
  public void forumsIdDelete(String id) {
    NodeRef forumRef = getForumNodeRef(id);

    nodeService.deleteNode(forumRef);
  }

  /**
   * Updates the editable properties of a forum. Only the title, description and
   * name are updated; any other properties carried by the supplied node are
   * ignored. Title and description are only updated when provided (non-null).
   *
   * @param id        the identifier of the forum node to update
   * @param forumNode the {@link Node} carrying the new title, description and
   *                  name
   * @throws IllegalArgumentException if no node exists for the given id
   * @throws org.alfresco.service.cmr.dictionary.InvalidTypeException if the
   *         node is not of type forum
   * @see io.swagger.api.ForumsApi#updateForum(java.lang.String,
   *      io.swagger.model.Node)
   */
  @Override
  public void updateForum(String id, Node forumNode) {
    NodeRef forumRef = getForumNodeRef(id);

    I18nProperty title = forumNode.getTitle();
    if (title != null) {
      nodeService.setProperty(
        forumRef,
        ContentModel.PROP_TITLE,
        Converter.toMLText(title)
      );
    }
    // for a forum only update these properties and ignore the rest

    I18nProperty description = forumNode.getDescription();
    if (description != null) {
      nodeService.setProperty(
        forumRef,
        ContentModel.PROP_DESCRIPTION,
        Converter.toMLText(description)
      );
    }

    nodeService.setProperty(
      forumRef,
      ContentModel.PROP_NAME,
      forumNode.getName()
    );
  }

  /**
   * Resolves and validates a forum node reference from its identifier.
   *
   * @param id the identifier of the forum node
   * @return the {@link NodeRef} of the existing forum
   * @throws IllegalArgumentException if no node exists for the given id
   * @throws org.alfresco.service.cmr.dictionary.InvalidTypeException if the
   *         node is not of type forum
   */
  private NodeRef getForumNodeRef(String id) {
    NodeRef forumRef = Converter.createNodeRefFromId(id);

    if (!nodeService.exists(forumRef)) {
      throw new IllegalArgumentException(
        "The forum with id " + id + " could not be found."
      );
    }

    if (!ForumModel.TYPE_FORUM.equals(nodeService.getType(forumRef))) {
      throw new InvalidTypeException(
        "The given id does not correspond to a node of type forum",
        ForumModel.TYPE_FORUM
      );
    }

    return forumRef;
  }

  /**
   * Enables or disables the moderation workflow on the given forum. The change
   * is only applied when it differs from the current moderation state: enabling
   * applies moderation, while disabling stops it and either accepts or refuses
   * any pending posts depending on {@code acceptAll}.
   *
   * @param id        the identifier of the forum node
   * @param enable    {@code true} to enable moderation, {@code false} to
   *                  disable it
   * @param acceptAll when disabling moderation, {@code true} to accept all
   *                  pending posts, {@code false} to refuse them
   * @see io.swagger.api.ForumsApi#toggleModeration(java.lang.String, boolean,
   *      boolean)
   */
  @Override
  public void toggleModeration(String id, boolean enable, boolean acceptAll) {
    NodeRef forumRef = Converter.createNodeRefFromId(id);

    Serializable moderationStatusSerializable = nodeService.getProperty(
      forumRef,
      ModerationModel.PROP_IS_MODERATED
    );

    boolean moderationEnabled = false;

    if (moderationStatusSerializable != null) {
      moderationEnabled = Boolean.parseBoolean(
        moderationStatusSerializable.toString()
      );
    }

    if (enable && !moderationEnabled) {
      moderationService.applyModeration(forumRef, false);
    } else if (!enable && moderationEnabled) {
      moderationService.stopModeration(
        forumRef,
        acceptAll ? "accept" : "refuse"
      );
    }
  }

  /**
   * Moderates a pending post by either approving it or rejecting it. When
   * rejected, a notification e-mail carrying the reason is sent to the post
   * author.
   *
   * @param id           the identifier of the post node to moderate
   * @param approve      {@code true} to accept the post, {@code false} to
   *                     reject it
   * @param rejectReason the reason for rejection; may be {@code null}, in which
   *                     case an empty reason is used
   * @see io.swagger.api.ForumsApi#verifyPost(java.lang.String, boolean,
   *      java.lang.String)
   */
  @Override
  public void verifyPost(String id, boolean approve, String rejectReason) {
    NodeRef postRef = Converter.createNodeRefFromId(id);

    if (approve) {
      moderationService.accept(postRef);
    } else {
      reject(postRef, rejectReason == null ? "" : rejectReason);
    }
  }

  /**
   * Rejects a post through the moderation service and notifies its author by
   * e-mail. The post content is captured before rejection so it can be included
   * in the notification. If the author cannot be resolved as a person, no mail
   * is sent and a warning is logged.
   *
   * @param postRef the reference of the post to reject
   * @param message the rejection reason to record and include in the e-mail
   */
  private void reject(final NodeRef postRef, final String message) {
    // get the content for mailing before it is versioned
    final String oldContent = contentService
      .getReader(postRef, ContentModel.PROP_CONTENT)
      .getContentString();

    moderationService.reject(postRef, message);

    final String creator = (String) nodeService.getProperty(
      postRef,
      ContentModel.PROP_CREATOR
    );

    if (personService.personExists(creator)) {
      // the properties are not refreshed in ActionNodeYet
      final Map<QName, Serializable> props = nodeService.getProperties(postRef);
      final Date moderated = (Date) props.get(ModerationModel.PROP_REJECT_ON);
      final String reason = (String) props.get(
        ModerationModel.PROP_REJECT_MESSAGE
      );

      final NodeRef creatorRef = personService.getPerson(creator);
      User user = usersApi.usersUserIdGet(creator);
      final String creatorEmail = user.getEmail();
      String noReply = mailService.getNoReplyEmailAddress();

      final Locale locale = Locale.of(user.getUiLang());
      final Map<String, Object> model =
        mailPreferencesService.buildDefaultModel(postRef, creatorRef, null);
      model.put(MailTemplate.KEY_REJECT_DATE, moderated);
      model.put(MailTemplate.KEY_REJECT_REASON, (reason == null) ? "" : reason);
      model.put(
        MailTemplate.KEY_REJECTED_CONTENT,
        (oldContent == null) ? "" : oldContent
      );
      final MailWrapper mail = mailPreferencesService.getDefaultMailTemplate(
        postRef,
        MailTemplate.REJECT_POST
      );

      try {
        mailService.send(
          noReply,
          creatorEmail,
          null,
          mail.getSubject(model, locale),
          mail.getBody(model, locale),
          true,
          false
        );
      } catch (jakarta.mail.MessagingException e) {
        logger.error("Failed to send rejection email to " + creatorEmail, e);
      }
    } else {
      if (logger.isWarnEnabled()) {
        logger.warn(
          "The creator '" +
            creator +
            "' of the post '" +
            postRef +
            "' could not be found. Rejection mail not sent."
        );
      }
    }
  }

  /**
   * Returns the abuse reports that have been signaled against the given post.
   *
   * @param id the identifier of the post node
   * @return the list of {@link AbuseReport}s recorded for the post
   * @see io.swagger.api.ForumsApi#getSignaledAbuses(java.lang.String)
   */
  @Override
  public List<AbuseReport> getSignaledAbuses(String id) {
    NodeRef postRef = Converter.createNodeRefFromId(id);

    return moderationService.getAbuses(postRef);
  }

  /**
   * Signals an abuse against the given post and notifies the relevant
   * moderators by e-mail.
   *
   * @param id        the identifier of the post node being reported
   * @param abuseText the description of the abuse; may be {@code null}, in
   *                  which case an empty message is used
   * @see io.swagger.api.ForumsApi#signalAbuse(java.lang.String, java.lang.String)
   */
  @Override
  public void signalAbuse(String id, String abuseText) {
    NodeRef postRef = Converter.createNodeRefFromId(id);

    signalAbuse(postRef, abuseText == null ? "" : abuseText);
  }

  /**
   * Clears the abuse reports on the given post, marking it as not abusive.
   *
   * @param id the identifier of the post node whose abuse reports are removed
   * @see io.swagger.api.ForumsApi#removeAbuses(java.lang.String)
   */
  @Override
  public void removeAbuses(String id) {
    NodeRef postRef = Converter.createNodeRefFromId(id);

    moderationService.signalNotAbuse(postRef);
  }

  /**
   * Records an abuse report against the given node and sends a notification
   * e-mail (localized per recipient) to each moderator of the enclosing
   * interest group. The reporter is added in copy of each notification. Mail
   * sending failures are logged and do not interrupt processing of the
   * remaining moderators.
   *
   * @param nodeRef the reference of the node (post) being reported
   * @param message the abuse description to record and include in the e-mail
   */
  protected void signalAbuse(NodeRef nodeRef, final String message) {
    final AbuseReport report = moderationService.signalAbuse(nodeRef, message);

    final Set<NodeRef> moderators = getModerators(nodeRef);

    // get the current user and properties, he is the reporter
    User reporter = usersApi.usersUserIdGet(report.getReporter());
    final String reporterEmail = reporter.getEmail();

    MLPropertyInterceptor.setMLAware(false);

    for (final NodeRef moderator : moderators) {
      final String moderatorEmail = (String) nodeService.getProperty(
        moderator,
        ContentModel.PROP_EMAIL
      );
      String noReply = mailService.getNoReplyEmailAddress();
      String moderatorUserId = (String) nodeService.getProperty(
        moderator,
        ContentModel.PROP_USERNAME
      );
      User moderatorUser = usersApi.usersUserIdGet(moderatorUserId);
      final Locale locale = Locale.of(moderatorUser.getUiLang());
      final Map<String, Object> model =
        mailPreferencesService.buildDefaultModel(nodeRef, moderator, null);
      model.put(MailTemplate.KEY_ABUSE_DATE, report.getReportDate());
      model.put(
        MailTemplate.KEY_ABUSE_REASON,
        (report.getMessage() == null) ? "" : report.getMessage()
      );
      final MailWrapper mail = mailPreferencesService.getDefaultMailTemplate(
        nodeRef,
        MailTemplate.SIGNAL_ABUSE
      );

      try {
        mailService.send(
          noReply,
          moderatorEmail,
          reporterEmail,
          mail.getSubject(model, locale),
          mail.getBody(model, locale),
          true,
          false
        );
      } catch (jakarta.mail.MessagingException e) {
        logger.error(
          "Failed to send abuse notification to " + moderatorEmail,
          e
        );
      }
    }
  }

  /**
   * Resolves the set of person nodes that moderate the interest group
   * containing the given post. Moderators are determined by inspecting the
   * profiles of the enclosing interest group and selecting the members of any
   * profile that holds at least one of the {@link #moderatorPermissions}.
   * Duplicate persons are eliminated.
   *
   * @param postRef the reference of the post whose moderators are resolved
   * @return the set of {@link NodeRef}s of the moderator persons; may be empty
   */
  protected Set<NodeRef> getModerators(NodeRef postRef) {
    final NodeRef topicRef = nodeService
      .getPrimaryParent(postRef)
      .getParentRef();
    final NodeRef igRef = apiToolBox.getCurrentInterestGroup(topicRef);

    List<UserProfile> moderators = new ArrayList<>();

    // get all profiles of the given IG
    List<Profile> profiles = profilesApi.groupsIdProfilesGet(
      igRef.getId(),
      "",
      false
    );

    // filter out those profiles that are not LibAdmin, NwsModerate or NwsAdmin
    for (Profile profile : profiles) {
      if (!hasPermission(profile.getPermissions(), moderatorPermissions)) {
        continue;
      }

      // build each profile name as a list, because the API expect them so
      List<String> profileToCheck = new ArrayList<>();
      profileToCheck.add(profile.getGroupName());

      // get the members of the profileToCheck and add them to the moderators list
      PagedUserProfile pagedUserProfile = groupsApi.groupsIdMembersGet(
        igRef.getId(),
        profileToCheck,
        null,
        -1,
        0,
        null,
        ""
      );

      moderators.addAll(pagedUserProfile.getData());
    }

    // get the nodeRef for each moderator given its user id and add it to the result
    // list
    final Set<NodeRef> moderatorsRef = HashSet.newHashSet(moderators.size());

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

  /**
   * Checks whether a permission map contains at least one of the given
   * permissions.
   *
   * @param permissionMap the map of permissions (profile name to permission
   *                      value) to inspect
   * @param permissions   the permission values to look for
   * @return {@code true} if any of the given permissions is present as a value
   *         in the map, {@code false} otherwise
   */
  private boolean hasPermission(
    Map<String, String> permissionMap,
    String[] permissions
  ) {
    // check if the map of permissions includes at least one of the
    // permissions being passed
    for (String permission : permissions) {
      if (permissionMap.containsValue(permission)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Returns the direct sub-forums (children of type {@code fm:forum}) of the
   * given newsgroup forum.
   *
   * <p>If the given node does not carry the
   * {@link CircabcModel#ASPECT_NEWSGROUP} aspect, an empty list is returned.</p>
   *
   * @param id the identifier of the parent forum node
   * @return the list of sub-forum {@link Node}s, or an empty list if the node
   *         is not a newsgroup
   */
  @Override
  public List<Node> forumsIdSubforumsGet(String id) {
    NodeRef forumRef = Converter.createNodeRefFromId(id);
    List<Node> result = new ArrayList<>();

    if (nodeService.hasAspect(forumRef, CircabcModel.ASPECT_NEWSGROUP)) {
      List<ChildAssociationRef> children = nodeService.getChildAssocs(forumRef);

      for (ChildAssociationRef item : children) {
        if (
          nodeService.getType(item.getChildRef()).equals(ForumModel.TYPE_FORUM)
        ) {
          final NodeRef childRef = item.getChildRef();
          result.add(nodesApi.getNode(childRef));
        }
      }
    }

    return result;
  }

  /**
   * Returns the sub-forums of the given forum, always listing folders first and
   * excluding topics, with an optional additional sort criterion.
   *
   * <p>The {@code sorting} argument, when not empty, is expected in the form
   * {@code <property>_<direction>} where the property is a content-model local
   * name and the direction ends with {@code ASC} for ascending order (any other
   * value results in descending order).</p>
   *
   * @param id      the identifier of the parent forum node
   * @param sorting the optional sort specification; an empty string applies
   *                only the default folders-first ordering
   * @return the list of sub-forum {@link Node}s
   */
  @Override
  public List<Node> forumsIdSubforumsGet(String id, String sorting) {
    List<Node> result = new ArrayList<>();

    NodeRef forumRef = Converter.createNodeRefFromId(id);

    PagingRequest pr = new PagingRequest(0, 10000);
    List<Pair<QName, Boolean>> sortProps = new ArrayList<>(1);

    Pair<QName, Boolean> sortFolderFirstPair = new Pair<>(
      GetChildrenCannedQuery.SORT_QNAME_NODE_IS_FOLDER,
      false
    );

    sortProps.add(sortFolderFirstPair);

    if (!sorting.equalsIgnoreCase("")) {
      Pair<QName, Boolean> sortPair = new Pair<>(
        QName.createQName(
          NamespaceService.CONTENT_MODEL_1_0_URI,
          sorting.split("_")[0]
        ),
        (sorting.endsWith("ASC"))
      );
      sortProps.add(sortPair);
    }

    Set<QName> ignored = new HashSet<>();
    ignored.add(ForumModel.TYPE_TOPIC);

    FileFilterMode.setClient(Client.cmis);
    final PagingResults<FileInfo> list = getFileFolderService().list(
      forumRef,
      false,
      true,
      ignored,
      sortProps,
      pr
    );
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
