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
package eu.europa.ec.digit.circabc.rest.service.notification;

import eu.europa.ec.digit.circabc.rest.service.user.UserService;
import io.swagger.api.ProfilesApi;
import io.swagger.model.NotifiableUser;
import io.swagger.model.NotifiableUserImpl;
import io.swagger.model.NotificationStatus;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.model.alfresco.UserModel;
import io.swagger.util.ApiToolBox;
import java.io.Serializable;
import java.util.*;
import java.util.Optional;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.*;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Concrete implementation of the Notification Subscription Service. The status is stored as a
 * permission.
 *
 * @author Yanick Pignot
 */
public class NotificationSubscriptionServiceImpl
  implements NotificationSubscriptionService
{

  /**
   * The notification stored as a permission
   */
  public static final String NOTIFICATION_AS_PERMISSION = "NotificationStatus";

  /**
   * The notification status defined by default for any authority in any Intrest Group
   */
  private static final NotificationStatus DEFAULT_TOP_LEVEL_STATUS =
    NotificationStatus.UNSUBSCRIBED;
  /**
   * The notification status defined by default for any authority in any node under an Interest
   * Group
   */
  private static final NotificationStatus DEFAULT_STATUS_ANY_OTHER_NODE =
    NotificationStatus.INHERITED;

  @Autowired
  private PermissionService permissionService;

  @Autowired
  private NodeService nodeService;

  @Autowired
  private PersonService personService;

  @Autowired
  private UserService userService;

  @Autowired
  private MultilingualContentService multilingualContentService;

  @Autowired
  private BehaviourFilter policyBehaviourFilter;

  @Autowired
  private ApiToolBox apiToolBox;

  @Autowired
  private ProfilesApi profilesApi;

  @Autowired
  private AuthorityService authorityService;

  /**
   * Returns the notification subscriptions explicitly defined on the given node, together with the
   * ones inherited from its primary parent when the node inherits parent permissions.
   *
   * <p>Only the permissions that represent a notification status (see {@link
   * #NOTIFICATION_AS_PERMISSION}) are taken into account. Inherited notifications are flagged as
   * such and are not duplicated by locally defined ones.
   *
   * @param nodeRef the node whose notification subscriptions must be retrieved; mandatory
   * @return the set of {@link AuthorityNotification} defined on (and inherited by) the node; never
   *     {@code null}
   */
  public Set<AuthorityNotification> getNotifications(final NodeRef nodeRef) {
    ParameterCheck.mandatory("The node ref", nodeRef);

    // retreive all permission of the given node
    final Set<AccessPermission> permissions =
      getPermissionService().getAllSetPermissions(nodeRef);

    final boolean inheritParentPermissions =
      getPermissionService().getInheritParentPermissions(nodeRef);

    final Set<AuthorityNotification> parentNotifications = new HashSet<>();
    if (inheritParentPermissions) {
      ChildAssociationRef childAssociationRef = nodeService.getPrimaryParent(
        nodeRef
      );
      NodeRef parent = childAssociationRef.getParentRef();
      Set<AccessPermission> parentPermissions =
        getPermissionService().getAllSetPermissions(parent);
      for (final AccessPermission permission : parentPermissions) {
        if (isPermissionNotificationStatus(permission.getPermission())) {
          parentNotifications.add(
            new AuthorityNotificationImpl(
              computeAccessPermission(permission.getAccessStatus()),
              permission.getAuthority(),
              true
            )
          );
        }
      }
    }

    final Set<AuthorityNotification> notifications = HashSet.newHashSet(
      permissions.size()
    );
    notifications.addAll(parentNotifications);

    // set the notification list by filtering the permissions that we need.
    for (final AccessPermission permission : permissions) {
      if (isPermissionNotificationStatus(permission.getPermission())) {
        final AuthorityNotificationImpl authorityNotif =
          new AuthorityNotificationImpl(
            computeAccessPermission(permission.getAccessStatus()),
            permission.getAuthority(),
            false
          );
        if (!parentNotifications.contains(authorityNotif)) {
          notifications.add(authorityNotif);
        }
      }
    }

    return notifications;
  }

  /**
   * Sets the notification status of an authority (a user or a profile/group) on the given node.
   *
   * <p>The status is persisted as an Alfresco permission: {@code SUBSCRIBED} grants the
   * notification permission, {@code UNSUBSCRIBED} denies it, and {@code INHERITED} removes any
   * locally defined status so that the parent's status applies again. The auditable aspect
   * behaviour is temporarily disabled while the permission is updated.
   *
   * @param nodeRef the node on which the status is set; must be an existing Interest Group child
   * @param authority the user or group authority the status applies to; mandatory
   * @param status the notification status to apply; mandatory
   * @throws InvalidNodeRefException if the node does not exist or is not part of an Interest Group
   * @throws IllegalStateException if the authority is neither a user nor a group, or if {@code
   *     INHERITED} is requested on an Interest Group root
   */
  public void setNotificationStatus(
    final NodeRef nodeRef,
    final String authority,
    final NotificationStatus status
  ) {
    checkAndReturnIG(nodeRef);
    ParameterCheck.mandatory("The notification status", status);
    ParameterCheck.mandatoryString("The autority", authority);

    final AuthorityType type = AuthorityType.getAuthorityType(authority);

    if (
      type == null ||
      (!type.equals(AuthorityType.GROUP) && !type.equals(AuthorityType.USER))
    ) {
      throw new IllegalStateException(
        "The notification can only be setted to a Profile or to a user. Authority " +
          authority +
          " from type " +
          type +
          " is not allowed."
      );
    }

    if (
      status.equals(NotificationStatus.INHERITED) &&
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_IGROOT)
    ) {
      throw new IllegalStateException(
        "The INHERITED notification status is not allowed for an Interest Group. Please select either SUSCRIBE or UNSUSCRIBE "
      );
    }

    policyBehaviourFilter.disableBehaviour(
      nodeRef,
      ContentModel.ASPECT_AUDITABLE
    );

    switch (status) {
      case SUBSCRIBED:
        getPermissionService().setPermission(
          nodeRef,
          authority,
          NOTIFICATION_AS_PERMISSION,
          true
        );
        break;
      case UNSUBSCRIBED:
        getPermissionService().setPermission(
          nodeRef,
          authority,
          NOTIFICATION_AS_PERMISSION,
          false
        );
        break;
      case INHERITED:
        removeNotificationImpl(nodeRef, authority);
        break;
    }
  }

  /**
   * Resolves the effective notification status of an authority on the given node, walking up the
   * node hierarchy (up to the enclosing Interest Group) until an explicitly defined status is
   * found, falling back to the applicable default status otherwise.
   *
   * @param nodeRef the node for which the status is resolved; must be an existing Interest Group
   *     child
   * @param authority the user or group authority whose status is resolved; mandatory
   * @return the resolved {@link AuthorityNotification} for the authority on the node
   * @throws InvalidNodeRefException if the node does not exist or is not part of an Interest Group
   */
  public AuthorityNotification getAuthorityNotificationStatus(
    final NodeRef nodeRef,
    final String authority
  ) throws InvalidNodeRefException {
    checkAndReturnIG(nodeRef);
    ParameterCheck.mandatoryString("The authority", authority);

    final NodeRef interestGroup = apiToolBox.getCurrentInterestGroup(nodeRef);

    final NotificationStatus notificationStatus =
      getAuthorityNotificationStatusImpl(nodeRef, interestGroup, authority);

    return new AuthorityNotificationImpl(notificationStatus, authority);
  }

  /**
   * Removes any locally defined notification status for the given authority on the node, so that
   * the status is inherited from the parent again. Does nothing if no status was explicitly
   * defined.
   *
   * @param nodeRef the node from which the notification status is removed; mandatory
   * @param authority the user or group authority whose local status is removed; mandatory
   */
  public void removeNotification(NodeRef nodeRef, String authority) {
    ParameterCheck.mandatoryString("The autority", authority);
    ParameterCheck.mandatory("The noderef", nodeRef);

    removeNotificationImpl(nodeRef, authority);
  }

  /**
   * Builds a report describing whether and why a given user would receive notifications for the
   * node, combining the user's own status, their profile status within the Interest Group and their
   * global notification preference.
   *
   * @param nodeRef the node for which the report is computed; mandatory
   * @param userAuthority the user authority the report is about; mandatory
   * @return the {@link UserNotificationReport}, or {@code null} if the authority is not an existing
   *     user or the node is not part of an Interest Group
   */
  public UserNotificationReport getUserNotificationReport(
    final NodeRef nodeRef,
    final String userAuthority
  ) {
    ParameterCheck.mandatory("The nodeRef", nodeRef);
    ParameterCheck.mandatoryString("The user Authority", userAuthority);

    if (
      !AuthorityType.getAuthorityType(userAuthority).equals(
        AuthorityType.USER
      ) ||
      !personService.personExists(userAuthority)
    ) {
      // no exception, the report is empty.
      return null;
    }

    final NodeRef igNoderef = apiToolBox.getCurrentInterestGroup(nodeRef);
    if (igNoderef == null) {
      // no exception, the report is empty.
      return null;
    }

    // The profile can be null because the user can not be invited in the IG
    String profile = null;

    if (profilesApi.getInvitedUsers(igNoderef).contains(userAuthority)) {
      profile = getPersonProfileGroupName(igNoderef, userAuthority);
    }

    return getReportUserProfileImpl(nodeRef, igNoderef, userAuthority, profile);
  }

  /**
   * Computes the set of users that must actually be notified for the given node.
   *
   * <p>Subscriptions defined directly on users and those defined on profiles/groups are collected
   * along the node hierarchy up to the enclosing Interest Group, profile subscriptions are expanded
   * to their members, and the resulting users are filtered to keep only those who are subscribed,
   * have global notifications enabled, hold read access on the node and match the document language
   * constraints.
   *
   * @param nodeRef the node for which notifiable users are computed; must be an existing Interest
   *     Group child
   * @return the set of {@link NotifiableUser} to notify; never {@code null}
   * @throws InvalidNodeRefException if the node does not exist or is not part of an Interest Group
   */
  public Set<NotifiableUser> getNotifiableUsers(final NodeRef nodeRef)
    throws InvalidNodeRefException {
    final NodeRef igNoderef = checkAndReturnIG(nodeRef);
    final Map<String, LeveledNotificationStatus> userNotif = HashMap.newHashMap(
      10
    );
    final Map<String, LeveledNotificationStatus> profileNotif =
      HashMap.newHashMap(80);
    fillNotifiableAuthoritiesImpl(
      nodeRef,
      igNoderef,
      0,
      userNotif,
      profileNotif
    );
    mergeProfileNotificationsIntoUserNotifications(userNotif, profileNotif);
    return buildNotifiableUsersSet(nodeRef, userNotif);
  }

  private void mergeProfileNotificationsIntoUserNotifications(
    Map<String, LeveledNotificationStatus> userNotif,
    Map<String, LeveledNotificationStatus> profileNotif
  ) {
    for (Map.Entry<
      String,
      LeveledNotificationStatus
    > entry : profileNotif.entrySet()) {
      Set<String> usersInProfile = authorityService.getContainedAuthorities(
        AuthorityType.USER,
        entry.getKey(),
        true
      );
      for (String user : usersInProfile) {
        LeveledNotificationStatus userStatus = userNotif.get(user);
        if (userStatus == null || userStatus.level > entry.getValue().level) {
          userNotif.put(user, entry.getValue());
        }
      }
    }
  }

  private Set<NotifiableUser> buildNotifiableUsersSet(
    NodeRef nodeRef,
    Map<String, LeveledNotificationStatus> userNotif
  ) {
    Set<NotifiableUser> usersToNotify = HashSet.newHashSet(userNotif.size());
    DocumentLanguageInfo langInfo = getDocumentLanguageInfo(nodeRef);

    for (Map.Entry<
      String,
      LeveledNotificationStatus
    > entry : userNotif.entrySet()) {
      if (
        !NotificationStatus.SUBSCRIBED.equals(entry.getValue().status)
      ) continue;
      String authority = entry.getKey();
      NodeRef person = personService.getPerson(authority);
      Map<QName, Serializable> properties = nodeService.getProperties(person);
      Boolean globalNotif = (Boolean) properties.get(
        UserModel.PROP_GLOBAL_NOTIFICATION
      );
      if (
        Boolean.TRUE.equals(globalNotif) &&
        hasReadPermission(nodeRef, authority) &&
        shouldNotifyUser(person, langInfo)
      ) {
        Locale locale = getLocale(
          userService.getPreference(person, UserService.PREF_INTERFACE_LANGUAGE)
        );
        usersToNotify.add(new NotifiableUserImpl(person, locale, properties));
      }
    }
    return usersToNotify;
  }

  private boolean hasReadPermission(NodeRef nodeRef, String authority) {
    return AuthenticationUtil.runAs(
      () ->
        getPermissionService()
          .hasPermission(nodeRef, PermissionService.READ_PROPERTIES)
          .equals(AccessStatus.ALLOWED),
      authority
    );
  }

  private boolean shouldNotifyUser(
    NodeRef person,
    DocumentLanguageInfo langInfo
  ) {
    if (!langInfo.isMultilingual) return true;
    Serializable contentLanguageFilter = userService.getPreference(
      person,
      UserService.PREF_CONTENT_FILTER_LANGUAGE
    );
    Locale contentLocale = getLocale(contentLanguageFilter);
    if (contentLocale == null) return true;
    String contentLanguage = contentLocale.getLanguage();
    return (
      langInfo.documentLanguage.equalsIgnoreCase(contentLanguage) ||
      langInfo.documentLanguage.equalsIgnoreCase(langInfo.pivotLanguage)
    );
  }

  private static class DocumentLanguageInfo {

    boolean isMultilingual;
    String documentLanguage;
    String pivotLanguage;
  }

  private DocumentLanguageInfo getDocumentLanguageInfo(NodeRef nodeRef) {
    DocumentLanguageInfo info = new DocumentLanguageInfo();
    info.isMultilingual = nodeService.hasAspect(
      nodeRef,
      ContentModel.ASPECT_MULTILINGUAL_DOCUMENT
    );
    if (!info.isMultilingual) return info;
    NodeRef pivot = multilingualContentService.getPivotTranslation(nodeRef);
    Locale pivotLocale = (Locale) nodeService.getProperty(
      pivot,
      ContentModel.PROP_LOCALE
    );
    Locale documentLocale = nodeService.hasAspect(
      nodeRef,
      ContentModel.ASPECT_MULTILINGUAL_EMPTY_TRANSLATION
    )
      ? pivotLocale
      : (Locale) nodeService.getProperty(nodeRef, ContentModel.PROP_LOCALE);
    info.documentLanguage = documentLocale.getLanguage();
    info.pivotLanguage = pivotLocale.getLanguage();
    return info;
  }

  private Locale getLocale(Serializable langObject) {
    Locale locale;
    if (langObject == null) {
      locale = null;
    } else if (langObject instanceof Locale loc) {
      locale = loc;
    } else {
      locale = Locale.of(langObject.toString());
    }
    return locale;
  }

  // -----------------------------
  // Helpers

  private void fillNotifiableAuthoritiesImpl(
    final NodeRef from,
    final NodeRef to,
    int level,
    final Map<String, LeveledNotificationStatus> userNotif,
    final Map<String, LeveledNotificationStatus> profileNotif
  ) {
    if (from == null) {
      return;
    }

    NotificationStatus status = null;
    String authority = null;
    AuthorityType type = null;

    final Set<AuthorityNotification> notifications = getNotifications(from);
    for (AuthorityNotification authorityNotification : notifications) {
      authority = authorityNotification.getAuthority();
      status = authorityNotification.getNotificationStatus();
      type = authorityNotification.getAuthorityType();

      if (
        AuthorityType.GROUP.equals(type) &&
        !authorityNotification.getInherited()
      ) {
        if (
          !profileNotif.containsKey(authority) &&
          !authorityNotification.getInherited()
        ) {
          profileNotif.put(
            authority,
            new LeveledNotificationStatus(status, level)
          );
        }
      } else {
        if (
          !userNotif.containsKey(authority) &&
          !authorityNotification.getInherited()
        ) {
          userNotif.put(
            authority,
            new LeveledNotificationStatus(status, level)
          );
        }
      }
    }

    if (!from.equals(to)) {
      final ChildAssociationRef parentAssoc = nodeService.getPrimaryParent(
        from
      );
      final NodeRef parentRef = parentAssoc.getParentRef();
      fillNotifiableAuthoritiesImpl(
        parentRef,
        to,
        ++level,
        userNotif,
        profileNotif
      );
    }
  }

  private String getPersonProfileGroupName(
    NodeRef nodeRef,
    String userAutority
  ) {
    return profilesApi.getPersonProfileGroupName(nodeRef, userAutority);
  }

  private void removeNotificationImpl(
    final NodeRef nodeRef,
    final String authority
  ) {
    if (getDefinedAuthorityStatus(nodeRef, authority) != null) {
      getPermissionService().deletePermission(
        nodeRef,
        authority,
        NOTIFICATION_AS_PERMISSION
      );
    }
  }

  private NotificationStatus getAuthorityNotificationStatusImpl(
    final NodeRef fromRef,
    final NodeRef toRef,
    final String authority
  ) throws InvalidNodeRefException {
    NotificationStatus status = getDefinedAuthorityStatus(fromRef, authority);

    if (status == null) {
      if (toRef == null) {
        status = DEFAULT_STATUS_ANY_OTHER_NODE;
      } else if (fromRef.equals(toRef)) {
        status = DEFAULT_TOP_LEVEL_STATUS;
      } else {
        final ChildAssociationRef parentAssoc = nodeService.getPrimaryParent(
          fromRef
        );
        final NodeRef parentRef = parentAssoc.getParentRef();

        status = getAuthorityNotificationStatusImpl(
          parentRef,
          toRef,
          authority
        );
      }
    }

    return status;
  }

  private UserNotificationReport getReportUserProfileImpl(
    final NodeRef nodeRef,
    final NodeRef igRef,
    final String user,
    final String profile
  ) {
    boolean isNodeRefIg = nodeRef.equals(igRef);
    final UserProfileStatuses statuses = fillStatuses(
      nodeRef,
      isNodeRefIg,
      user,
      profile
    );

    final NodeRef person = personService.getPerson(user);
    final Boolean globalNotification = (Boolean) nodeService.getProperty(
      person,
      UserModel.PROP_GLOBAL_NOTIFICATION
    );

    Boolean willUserReceive = computeWillUserReceive(
      globalNotification,
      statuses,
      nodeRef,
      igRef,
      user,
      profile
    );

    if (willUserReceive == null) {
      throw new IllegalStateException("Pathologic case found in the algorithm");
    }

    return new UserNotificationReportImpl(
      nodeRef,
      user,
      profile,
      globalNotification,
      statuses.userStatus,
      statuses.profileStatus,
      willUserReceive
    );
  }

  private Boolean computeWillUserReceive(
    Boolean globalNotification,
    UserProfileStatuses statuses,
    NodeRef nodeRef,
    NodeRef igRef,
    String user,
    String profile
  ) {
    if (!Boolean.TRUE.equals(globalNotification)) {
      return Boolean.FALSE;
    }

    UserProfileStatuses tempStatuses = statuses;
    NodeRef tempNodeRef = nodeRef;

    while (true) {
      Optional<Boolean> result = resolveNotificationStatus(tempStatuses);
      if (result.isPresent()) {
        return result.get();
      }

      final ChildAssociationRef parentAssoc = nodeService.getPrimaryParent(
        tempNodeRef
      );
      tempNodeRef = parentAssoc.getParentRef();
      boolean topFolder = (tempNodeRef == null) || (tempNodeRef.equals(igRef));
      tempStatuses = fillStatuses(tempNodeRef, topFolder, user, profile);
    }
  }

  private Optional<Boolean> resolveNotificationStatus(
    UserProfileStatuses statuses
  ) {
    if (statuses.userStatus == NotificationStatus.SUBSCRIBED) {
      return Optional.of(Boolean.TRUE);
    }
    if (statuses.userStatus == NotificationStatus.UNSUBSCRIBED) {
      return Optional.of(Boolean.FALSE);
    }
    if (statuses.profileStatus == NotificationStatus.SUBSCRIBED) {
      return Optional.of(Boolean.TRUE);
    }
    if (statuses.profileStatus == NotificationStatus.UNSUBSCRIBED) {
      return Optional.of(Boolean.FALSE);
    }
    return Optional.empty();
  }

  private UserProfileStatuses fillStatuses(
    final NodeRef nodeRef,
    boolean topLevelNodeRef,
    final String user,
    final String profile
  ) {
    NotificationStatus userStatus = null;
    NotificationStatus profileStatus = null;

    final Set<AccessPermission> permissions =
      getPermissionService().getAllSetPermissions(nodeRef);

    for (AccessPermission permission : permissions) {
      if (permission.getPermission().equals(NOTIFICATION_AS_PERMISSION)) {
        String authority = permission.getAuthority();

        if (authority.equals(user)) {
          userStatus = computeAccessPermission(permission.getAccessStatus());
        } else if (profile != null && authority.equals(profile)) {
          profileStatus = computeAccessPermission(permission.getAccessStatus());
        }

        if (userStatus != null && (profileStatus != null || profile == null)) {
          break;
        }
      }
    }

    return new UserProfileStatuses(
      resolveStatus(userStatus, topLevelNodeRef),
      resolveStatus(profileStatus, topLevelNodeRef)
    );
  }

  private NotificationStatus resolveStatus(
    NotificationStatus status,
    boolean topLevelNodeRef
  ) {
    if (status != null) {
      return status;
    }
    return topLevelNodeRef
      ? DEFAULT_TOP_LEVEL_STATUS
      : DEFAULT_STATUS_ANY_OTHER_NODE;
  }

  private NotificationStatus getDefinedAuthorityStatus(
    final NodeRef nodeRef,
    final String authority
  ) {
    NotificationStatus status = null;

    final Set<AccessPermission> permissions =
      getPermissionService().getAllSetPermissions(nodeRef);
    for (AccessPermission permission : permissions) {
      if (
        permission.getAuthority().equals(authority) &&
        permission.getPermission().equals(NOTIFICATION_AS_PERMISSION)
      ) {
        status = computeAccessPermission(permission.getAccessStatus());
        break;
      }
    }

    return status;
  }

  /**
   * Wrap an AccessStatus to a Notification status
   */
  private NotificationStatus computeAccessPermission(
    final AccessStatus accessStatus
  ) {
    NotificationStatus status;

    switch (accessStatus) {
      case ALLOWED:
        status = NotificationStatus.SUBSCRIBED;
        break;
      case DENIED:
        status = NotificationStatus.UNSUBSCRIBED;
        break;
      default:
        // should never appears ....
        status = NotificationStatus.INHERITED;
        break;
    }

    return status;
  }

  private NodeRef checkAndReturnIG(final NodeRef nodeRef)
    throws InvalidNodeRefException {
    if (nodeRef == null || !nodeService.exists(nodeRef)) {
      throw new InvalidNodeRefException(
        "The node ref is a mandatory parameter and must be existing.",
        nodeRef
      );
    }

    final NodeRef igNoderef = apiToolBox.getCurrentInterestGroup(nodeRef);

    if (igNoderef == null) {
      throw new InvalidNodeRefException(
        "The node received as parameter is not an Interest Group child.",
        nodeRef
      );
    }

    return igNoderef;
  }

  /**
   * Test is the given permission wrappes the notification status
   */
  private boolean isPermissionNotificationStatus(final String permission) {
    return permission != null && NOTIFICATION_AS_PERMISSION.equals(permission);
  }

  /**
   * @return the permissionService
   */
  protected final PermissionService getPermissionService() {
    return permissionService;
  }

  /**
   * @param permissionService the permissionService to set
   */
  public final void setPermissionService(
    final PermissionService permissionService
  ) {
    this.permissionService = permissionService;
  }

  /**
   * Internal wrapper to store any notification status associated to an integer.
   *
   * @author yanick pignot
   */
  private static class LeveledNotificationStatus {

    private NotificationStatus status;
    private int level;

    public LeveledNotificationStatus(NotificationStatus status, int level) {
      super();
      this.status = status;
      this.level = level;
    }
  }

  private static class UserProfileStatuses {

    private NotificationStatus userStatus;
    private NotificationStatus profileStatus;

    public UserProfileStatuses(
      final NotificationStatus userStatus,
      final NotificationStatus profileStatus
    ) {
      super();
      this.userStatus = userStatus;
      this.profileStatus = profileStatus;
    }
  }
}
