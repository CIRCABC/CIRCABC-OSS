package io.swagger.api;

import eu.europa.ec.digit.circabc.rest.service.notification.AuthorityNotification;
import eu.europa.ec.digit.circabc.rest.service.notification.NotificationManagerService;
import eu.europa.ec.digit.circabc.rest.service.notification.NotificationSubscriptionService;
import io.swagger.model.*;
import io.swagger.model.alfresco.UserModel;
import io.swagger.util.ApiToolBox;
import io.swagger.util.Converter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Business-logic implementation of {@link NotificationsApi}.
 *
 * <p>This service manages the notification configuration attached to Alfresco
 * nodes within CIRCABC. It covers two related concerns:</p>
 * <ul>
 *   <li><b>Notification permissions</b> &mdash; per-node, per-authority
 *       (user or profile/group) {@code NotificationStatus} permissions that
 *       determine whether a given authority is notified about changes to a
 *       node. These are stored through Alfresco's {@link PermissionService}.</li>
 *   <li><b>Notification subscriptions</b> &mdash; the effective set of
 *       authorities subscribed to a node and the list of users who can be
 *       notified, resolved through the
 *       {@link NotificationSubscriptionService}.</li>
 * </ul>
 *
 * <p>Mutating operations temporarily disable the {@code cm:auditable}
 * behaviour via the {@link BehaviourFilter} so that changing notification
 * permissions does not update the node's modified date.</p>
 *
 * @author beaurpi
 */
public class NotificationsApiImpl implements NotificationsApi {

  /** Logger for this service. */
  static final Log logger = LogFactory.getLog(NotificationsApiImpl.class);

  /** Access-status name marking an authority as allowed to be notified. */
  private static final String ALLOWED = "ALLOWED";

  /** Name of the Alfresco permission used to store notification state. */
  private static final String NOTIFICATION_STATUS = "NotificationStatus";

  /** Alfresco service used to read and mutate node permissions. */
  @Autowired
  private PermissionService permissionService;

  /** Alfresco service used to resolve person (user) nodes. */
  @Autowired
  private PersonService personService;

  /** Alfresco service used to read node properties. */
  @Autowired
  private NodeService nodeService;

  /** Service handling paste-related notification flags on nodes. */
  @Autowired
  private NotificationManagerService notificationManagerService;

  /** Service resolving effective notification subscriptions and notifiable users. */
  @Autowired
  private NotificationSubscriptionService notificationSubscriptionService;

  /**
   * Behaviour filter used to suppress auditable behaviour while changing
   * notification permissions, preventing the node's modified date from changing.
   */
  @Autowired
  private BehaviourFilter policyBehaviourFilter;

  /** API used to resolve the profiles (groups) of an interest group. */
  @Autowired
  private ProfilesApi profilesApi;

  /** API used to resolve user details from an authority name. */
  @Autowired
  private UsersApi usersApi;

  /** Utility used to resolve the interest group a node belongs to. */
  @Autowired
  private ApiToolBox apiToolBox;

  /**
   * Sets, clears, or removes the notification permission for a single authority
   * on a node.
   *
   * <p>The {@code value} argument drives the behaviour:</p>
   * <ul>
   *   <li>{@code "on"} &mdash; grants the {@code NotificationStatus} permission
   *       (authority is notified);</li>
   *   <li>{@code "off"} &mdash; denies the {@code NotificationStatus}
   *       permission (authority is not notified);</li>
   *   <li>any other value &mdash; removes the explicit permission, but only if
   *       the node does not inherit parent permissions.</li>
   * </ul>
   *
   * <p>Auditable behaviour is disabled during the change so the node's modified
   * date is preserved.</p>
   *
   * @param id        the identifier of the target node
   * @param authority the user or group authority whose notification permission
   *                  is being changed
   * @param value     desired state: {@code "on"}, {@code "off"}, or any other
   *                  value to remove the explicit permission
   */
  @Override
  public void nodesIdNotificationsAuthorityPut(
    String id,
    String authority,
    String value
  ) {
    NodeRef nodeRef = Converter.createNodeRefFromId(id);
    // disable filter to prevent the change of the nodes' modified date
    policyBehaviourFilter.disableBehaviour(
      nodeRef,
      ContentModel.ASPECT_AUDITABLE
    );

    if (id != null && authority != null) {
      if ("on".equals(value)) {
        permissionService.setPermission(
          nodeRef,
          authority,
          NOTIFICATION_STATUS,
          true
        );
      } else if ("off".equals(value)) {
        permissionService.setPermission(
          nodeRef,
          authority,
          NOTIFICATION_STATUS,
          false
        );
      } else {
        if (!permissionService.getInheritParentPermissions(nodeRef)) {
          permissionService.deletePermission(
            nodeRef,
            authority,
            NOTIFICATION_STATUS
          );
        }
      }
    }

    policyBehaviourFilter.enableBehaviour(
      nodeRef,
      ContentModel.ASPECT_AUDITABLE
    );
  }

  /**
   * Returns the paste-notification state for a node.
   *
   * @param id the identifier of the target node
   * @return a {@link PasteNotificationsState} indicating whether paste and
   *         paste-all notifications are enabled on the node
   * @see io.swagger.api.NotificationsApi#getPasteNotificationsState(java.lang.String)
   */
  @Override
  public PasteNotificationsState getPasteNotificationsState(String id) {
    NodeRef nodeRef = Converter.createNodeRefFromId(id);

    boolean pasteAllEnabled =
      notificationManagerService.isPasteAllNotificationEnabled(nodeRef);
    boolean pasteEnabled =
      notificationManagerService.isPasteNotificationEnabled(nodeRef);

    return new PasteNotificationsState(pasteEnabled, pasteAllEnabled);
  }

  /**
   * Updates the paste-notification state for a node.
   *
   * @param id             the identifier of the target node
   * @param pasteEnable    whether paste notifications should be enabled
   * @param pasteAllEnable whether paste-all notifications should be enabled
   * @see io.swagger.api.NotificationsApi#setPasteNotificationsState(java.lang.String,
   *      boolean,
   *      boolean)
   */
  @Override
  public void setPasteNotificationsState(
    String id,
    boolean pasteEnable,
    boolean pasteAllEnable
  ) {
    NodeRef nodeRef = Converter.createNodeRefFromId(id);

    notificationManagerService.setPasteNotificationEnabled(
      nodeRef,
      pasteEnable
    );
    notificationManagerService.setPasteAllNotificationEnabled(
      nodeRef,
      pasteAllEnable
    );
  }

  /**
   * Builds the full notification definition of a node: the explicit
   * {@code NotificationStatus} permissions set for individual users and for
   * profiles (groups) of the node's interest group.
   *
   * @param id the identifier of the target node
   * @return a {@link NotificationDefinition} listing the notified users and
   *         profiles, including whether each entry is inherited
   */
  @Override
  public NotificationDefinition nodesIdNotificationsGet(String id) {
    NodeRef nodeRef = Converter.createNodeRefFromId(id);
    NotificationDefinition result = new NotificationDefinition();

    NodeRef groupRef = apiToolBox.getCurrentInterestGroup(nodeRef);
    List<Profile> groupProfiles = profilesApi.groupsIdProfilesGet(
      groupRef.getId(),
      null,
      false
    );

    if (id != null) {
      Set<AccessPermission> notifications =
        permissionService.getAllSetPermissions(nodeRef);
      for (AccessPermission ac : notifications) {
        if (
          ac.getPermission().equals(NOTIFICATION_STATUS) &&
          ac.getAuthorityType().equals(AuthorityType.USER)
        ) {
          NotificationDefinitionUsers ndu = new NotificationDefinitionUsers();
          ndu.setUser(usersApi.usersUserIdGet(ac.getAuthority()));
          ndu.setNotifications(ac.getAccessStatus().name());
          ndu.setInherited(ac.isInherited());
          result.getUsers().add(ndu);
        } else if (
          ac.getPermission().equals(NOTIFICATION_STATUS) &&
          ac.getAuthorityType().equals(AuthorityType.GROUP)
        ) {
          NotificationDefinitionProfiles ndp = getProfileNotifications(
            ac,
            groupProfiles
          );
          ndp.setInherited(ac.isInherited());
          if (ndp.getProfile() != null) {
            result.getProfiles().add(ndp);
          }
        }
      }
    }

    return result;
  }

  /**
   * Maps a group-level access permission to a profile notification entry.
   *
   * @param perm          the group notification access permission
   * @param groupProfiles the profiles of the interest group used to resolve
   *                      the matching profile
   * @return a {@link NotificationDefinitionProfiles} carrying the resolved
   *         profile and its notification status
   */
  private NotificationDefinitionProfiles getProfileNotifications(
    AccessPermission perm,
    List<Profile> groupProfiles
  ) {
    NotificationDefinitionProfiles result =
      new NotificationDefinitionProfiles();
    result.setProfile(getProfile(perm, groupProfiles));
    result.setNotifications(perm.getAccessStatus().name());

    return result;
  }

  /**
   * Finds the profile whose group name matches the authority of the given
   * permission.
   *
   * @param perm          the access permission holding the group authority
   * @param groupProfiles the candidate profiles of the interest group
   * @return the matching {@link Profile}, or {@code null} if none matches
   */
  private Profile getProfile(
    AccessPermission perm,
    List<Profile> groupProfiles
  ) {
    Profile result = null;
    for (Profile p : groupProfiles) {
      if (perm.getAuthority().equals(p.getGroupName())) {
        result = p;
      }
    }
    return result;
  }

  /**
   * Removes the explicit {@code NotificationStatus} permission of an authority
   * on a node. Auditable behaviour is disabled during the change so the node's
   * modified date is preserved.
   *
   * @param id        the identifier of the target node
   * @param authority the user or group authority whose notification permission
   *                  is removed
   */
  @Override
  public void nodesIdNotificationsAuthorityDelete(String id, String authority) {
    NodeRef nodeRef = Converter.createNodeRefFromId(id);
    if (id != null && authority != null) {
      // disable filter to prevent the change of the nodes' modified date
      policyBehaviourFilter.disableBehaviour(
        nodeRef,
        ContentModel.ASPECT_AUDITABLE
      );
      permissionService.deletePermission(
        nodeRef,
        authority,
        NOTIFICATION_STATUS
      );
      policyBehaviourFilter.enableBehaviour(
        nodeRef,
        ContentModel.ASPECT_AUDITABLE
      );
    }
  }

  /**
   * Applies a full notification definition to a node, setting the
   * {@code NotificationStatus} permission for every profile and user contained
   * in the request body. A profile or user is granted the permission when its
   * notification value equals {@code ALLOWED}, otherwise it is denied.
   *
   * <p>Auditable behaviour is disabled during the update so the node's modified
   * date is preserved.</p>
   *
   * @param id   the identifier of the target node
   * @param body the notification definition describing profiles and users to
   *             configure
   * @return the resulting {@link NotificationDefinition} of the node after the
   *         update, as returned by {@link #nodesIdNotificationsGet(String)}
   */
  @Override
  public NotificationDefinition nodesIdNotificationsPost(
    String id,
    NotificationDefinition body
  ) {
    NodeRef nodeRef = Converter.createNodeRefFromId(id);

    // disable filter to prevent the change of the nodes' modified date
    policyBehaviourFilter.disableBehaviour(
      nodeRef,
      ContentModel.ASPECT_AUDITABLE
    );

    for (NotificationDefinitionProfiles ndp : body.getProfiles()) {
      permissionService.setPermission(
        nodeRef,
        ndp.getProfile().getGroupName(),
        NOTIFICATION_STATUS,
        ndp.getNotifications().equals(ALLOWED)
      );
    }

    for (NotificationDefinitionUsers ndu : body.getUsers()) {
      permissionService.setPermission(
        nodeRef,
        ndu.getUser().getUserId(),
        NOTIFICATION_STATUS,
        ndu.getNotifications().equals(ALLOWED)
      );
    }

    policyBehaviourFilter.enableBehaviour(
      nodeRef,
      ContentModel.ASPECT_AUDITABLE
    );

    return nodesIdNotificationsGet(id);
  }

  /**
   * Returns a paged list of the notification subscriptions (authorities and
   * their status) configured on a node.
   *
   * @param id        the identifier of the target node
   * @param startItem the zero-based index of the first item to return
   * @param amount    the maximum number of items to return; {@code 0} means
   *                  return all items
   * @param language  the language used to resolve profile display names
   * @return a {@link PagedNotificationConfigurations} holding the requested
   *         page and the total number of configurations
   * @see io.swagger.api.NotificationsApi#getNotifications(java.lang.String, int,
   *      int,
   *      java.lang.String)
   */
  @Override
  public PagedNotificationConfigurations getNotifications(
    String id,
    int startItem,
    int amount,
    String language
  ) {
    NodeRef nodeRef = Converter.createNodeRefFromId(id);

    final Set<AuthorityNotification> notifications =
      notificationSubscriptionService.getNotifications(nodeRef);
    final List<NotificationWrapper> result = new ArrayList<>(
      notifications.size()
    );

    for (final AuthorityNotification notification : notifications) {
      NotificationWrapper wrapper = wrapNotification(
        notification,
        nodeRef,
        language
      );

      if (wrapper == null) {
        logger.error(
          "The repository is corrupt. A notification has " +
            "been set with a non-managed Authotity Type. Only " +
            AuthorityType.GROUP +
            "  and " +
            AuthorityType.USER +
            " are allowed. \n\tAuthority found: " +
            notification.getAuthority() +
            "\n\tFrom type:       " +
            notification.getAuthorityType() +
            "\n\tWith the status: " +
            notification.getNotificationStatus() +
            "\n\tAuthority found: " +
            notification.getAuthorityType() +
            "\n\tOn node:         " +
            nodeRef.getId()
        );
      }

      result.add(wrapper);
    }

    int resultSize = result.size();

    List<NotificationWrapper> pagedConfigurations;

    if (amount == 0) {
      // amount == 0 means that we want all items
      pagedConfigurations = result;
    } else {
      pagedConfigurations = new ArrayList<>();

      int endItem = Math.min(startItem + amount, resultSize);

      for (int index = startItem; index < endItem; index++) {
        pagedConfigurations.add(result.get(index));
      }
    }

    return new PagedNotificationConfigurations(pagedConfigurations, resultSize);
  }

  /**
   * Returns a paged, filtered list of the notification subscriptions configured
   * on a node. Subscriptions are filtered by authority name, authority type and
   * notification status before paging is applied.
   *
   * @param id            the identifier of the target node
   * @param startItem     the zero-based index of the first item to return
   * @param amount        the maximum number of items to return; {@code 0} means
   *                      return all items
   * @param language      the language used to resolve profile display names
   * @param queryType     authority type to match (empty matches any type)
   * @param queryUserName substring the authority must contain
   * @param queryStatus   notification status to match (empty matches any status)
   * @return a {@link PagedNotificationConfigurations} holding the requested page
   *         and the total number of matching configurations
   */
  public PagedNotificationConfigurations getNotifications(
    String id,
    int startItem,
    int amount,
    String language,
    String queryType,
    String queryUserName,
    String queryStatus
  ) {
    NodeRef nodeRef = Converter.createNodeRefFromId(id);

    final Set<AuthorityNotification> fullNotifications =
      notificationSubscriptionService.getNotifications(nodeRef);

    List<AuthorityNotification> notifications = fullNotifications
      .stream()
      .filter(
        notification ->
          notification.getAuthority().contains(queryUserName) &&
          (queryType.isEmpty() ||
            notification.getAuthorityType().name().equals(queryType)) &&
          (queryStatus.isEmpty() ||
            notification.getNotificationStatus().name().equals(queryStatus))
      )
      .toList();

    final List<NotificationWrapper> result = new ArrayList<>(
      notifications.size()
    );

    for (final AuthorityNotification notification : notifications) {
      NotificationWrapper wrapper = wrapNotification(
        notification,
        nodeRef,
        language
      );

      if (wrapper == null) {
        logger.error(
          "The repository is corrupt. A notification has " +
            "been set with a non-managed Authotity Type. Only " +
            AuthorityType.GROUP +
            "  and " +
            AuthorityType.USER +
            " are allowed. \n\tAuthority found: " +
            notification.getAuthority() +
            "\n\tFrom type:       " +
            notification.getAuthorityType() +
            "\n\tWith the status: " +
            notification.getNotificationStatus() +
            "\n\tAuthority found: " +
            notification.getAuthorityType() +
            "\n\tOn node:         " +
            nodeRef.getId()
        );
      }

      result.add(wrapper);
    }

    int resultSize = result.size();

    List<NotificationWrapper> pagedConfigurations;

    if (amount == 0) {
      // amount == 0 means that we want all items
      pagedConfigurations = result;
    } else {
      pagedConfigurations = new ArrayList<>();

      int endItem = Math.min(startItem + amount, resultSize);

      for (int index = startItem; index < endItem; index++) {
        pagedConfigurations.add(result.get(index));
      }
    }

    return new PagedNotificationConfigurations(pagedConfigurations, resultSize);
  }

  /**
   * Converts an {@link AuthorityNotification} into a {@link NotificationWrapper}
   * enriched with a display name (and, for groups, a localized title).
   *
   * @param authorityNotification the notification to wrap
   * @param node                  the node the notification applies to
   * @param language              the language used to resolve profile names
   * @return the wrapped notification, or {@code null} if the authority type is
   *         neither {@code USER} nor {@code GROUP}
   */
  private NotificationWrapper wrapNotification(
    final AuthorityNotification authorityNotification,
    final NodeRef node,
    String language
  ) {
    String name;
    I18nProperty title = null;

    switch (authorityNotification.getAuthorityType()) {
      case USER:
        name = computeUserLogin(authorityNotification.getAuthority());
        break;
      case GROUP:
        name = getProfileName(
          node,
          authorityNotification.getAuthority(),
          language
        );
        title = getProfileTitle(node, authorityNotification.getAuthority());
        break;
      default:
        return null;
    }

    NotificationWrapper result = new NotificationWrapper(
      authorityNotification.getAuthorityType(),
      name,
      authorityNotification.getNotificationStatus(),
      authorityNotification.getAuthority(),
      node.getId(),
      authorityNotification.getInherited()
    );

    if (authorityNotification.getAuthorityType().name().equals("GROUP")) {
      result.setTitle(title);
    }

    return result;
  }

  /**
   * Resolves the display login/name of a user authority, preferring the ECAS
   * user name and falling back to the Alfresco username.
   *
   * @param authorityName the user authority (username)
   * @return the resolved display name for the user
   */
  private String computeUserLogin(String authorityName) {
    NodeRef personRef = personService.getPerson(authorityName);

    Map<QName, Serializable> properties = nodeService.getProperties(personRef);

    String displayName = (String) properties.get(UserModel.PROP_ECAS_USER_NAME);
    if (displayName == null) {
      displayName = (String) properties.get(ContentModel.PROP_USERNAME);
    }
    return displayName;
  }

  /**
   * Resolves the localized display title of a profile (group) within the
   * interest group that owns the node. Falls back to the {@code en-US} title
   * and then to the profile name when no localized title is available.
   *
   * @param nodeRef       the node used to determine the interest group
   * @param authorityName the group authority identifying the profile
   * @param language      the preferred language for the title
   * @return the resolved profile title/name, or {@code null} if no matching
   *         profile is found
   */
  private String getProfileName(
    NodeRef nodeRef,
    String authorityName,
    String language
  ) {
    String igId = apiToolBox.getCurrentInterestGroup(nodeRef).getId();

    List<Profile> profiles = profilesApi.groupsIdProfilesGet(igId, null, false);

    for (final Profile profile : profiles) {
      if (profile.getGroupName().equals(authorityName)) {
        String title = profile.getTitle().get(language);
        if (title == null) {
          title = profile.getTitle().get("en-US");
          if (title == null) {
            logger.warn(
              "Profile title not found for language '" +
                language +
                "' nor default 'en-US'. Retrieving the name instead: " +
                profile.getName()
            );
            return profile.getName();
          }
        }
        return title;
      }
    }

    return null;
  }

  /**
   * Resolves the full set of localized titles of a profile (group) within the
   * interest group that owns the node.
   *
   * @param nodeRef       the node used to determine the interest group
   * @param authorityName the group authority identifying the profile
   * @return the profile's {@link I18nProperty} titles, or an empty
   *         {@link I18nProperty} if no matching profile is found
   */
  private I18nProperty getProfileTitle(NodeRef nodeRef, String authorityName) {
    String igId = apiToolBox.getCurrentInterestGroup(nodeRef).getId();

    List<Profile> profiles = profilesApi.groupsIdProfilesGet(igId, null, false);

    for (final Profile profile : profiles) {
      if (profile.getGroupName().equals(authorityName)) {
        return profile.getTitle();
      }
    }

    return new I18nProperty();
  }

  /**
   * Returns a paged list of the users that can be notified for a node.
   *
   * @param id        the identifier of the target node
   * @param startItem the zero-based index of the first item to return
   * @param amount    the maximum number of items to return; {@code 0} means
   *                  return all items
   * @return a {@link PagedNotificationSubscribedUsers} holding the requested
   *         page and the total number of notifiable users
   * @see io.swagger.api.NotificationsApi#getNotifiableUsers(java.lang.String,
   *      int, int)
   */
  @Override
  public PagedNotificationSubscribedUsers getNotifiableUsers(
    String id,
    int startItem,
    int amount
  ) {
    NodeRef nodeRef = Converter.createNodeRefFromId(id);

    List<NotifiableUser> result = new ArrayList<>(
      notificationSubscriptionService.getNotifiableUsers(nodeRef)
    );

    int resultSize = result.size();

    List<NotifiableUser> pagedUsers;

    if (amount == 0) {
      // amount == 0 means that we want all items
      pagedUsers = result;
    } else {
      pagedUsers = new ArrayList<>();

      int endItem = Math.min(startItem + amount, resultSize);

      for (int index = startItem; index < endItem; index++) {
        pagedUsers.add(result.get(index));
      }
    }

    return new PagedNotificationSubscribedUsers(pagedUsers, resultSize);
  }

  /**
   * Returns a paged, filtered list of the users that can be notified for a node.
   * Users are filtered so that each supplied criterion must be contained in the
   * corresponding user field before paging is applied.
   *
   * @param id        the identifier of the target node
   * @param startItem the zero-based index of the first item to return
   * @param amount    the maximum number of items to return; {@code 0} means
   *                  return all items
   * @param userName  substring the user name must contain
   * @param firstName substring the first name must contain
   * @param lastName  substring the last name must contain
   * @param email     substring the email address must contain
   * @return a {@link PagedNotificationSubscribedUsers} holding the requested
   *         page and the total number of matching notifiable users
   * @see io.swagger.api.NotificationsApi#getNotifiableUsers(java.lang.String,
   *      int, int, String, String, String, String)
   */
  @Override
  public PagedNotificationSubscribedUsers getNotifiableUsers(
    String id,
    int startItem,
    int amount,
    final String userName,
    final String firstName,
    final String lastName,
    final String email
  ) {
    NodeRef nodeRef = Converter.createNodeRefFromId(id);

    List<NotifiableUser> fullResult = new ArrayList<>(
      notificationSubscriptionService.getNotifiableUsers(nodeRef)
    );

    List<NotifiableUser> result = fullResult
      .stream()
      .filter(
        user ->
          user.getUserName().contains(userName) &&
          user.getLastName().contains(lastName) &&
          user.getFirstName().contains(firstName) &&
          user.getEmailAddress().contains(email)
      )
      .toList();

    int resultSize = result.size();

    List<NotifiableUser> pagedUsers;

    if (amount == 0) {
      // amount == 0 means that we want all items
      pagedUsers = result;
    } else {
      pagedUsers = new ArrayList<>();

      int endItem = Math.min(startItem + amount, resultSize);

      for (int index = startItem; index < endItem; index++) {
        pagedUsers.add(result.get(index));
      }
    }

    return new PagedNotificationSubscribedUsers(pagedUsers, resultSize);
  }

  /**
   * Removes the notification subscription of an authority on a node.
   *
   * @param id        the identifier of the target node
   * @param authority the authority whose subscription is removed
   * @see io.swagger.api.NotificationsApi#removeNotification(java.lang.String,
   *      java.lang.String)
   */
  @Override
  public void removeNotification(String id, String authority) {
    NodeRef nodeRef = Converter.createNodeRefFromId(id);

    notificationSubscriptionService.removeNotification(nodeRef, authority);
  }

  /**
   * Sets the notification status of an authority on a node.
   *
   * @param id        the identifier of the target node
   * @param authority the authority whose notification status is set
   * @param status    the new {@link NotificationStatus} to apply
   * @see io.swagger.api.NotificationsApi#setNotificationStatus(java.lang.String,
   *      java.lang.String,
   *      eu.cec.digit.circabc.service.notification.NotificationStatus)
   */
  @Override
  public void setNotificationStatus(
    String id,
    String authority,
    NotificationStatus status
  ) {
    NodeRef nodeRef = Converter.createNodeRefFromId(id);

    notificationSubscriptionService.setNotificationStatus(
      nodeRef,
      authority,
      status
    );
  }

  /**
   * Indicates whether a given user is among the notifiable users of a node.
   *
   * @param id     the identifier of the target node
   * @param userId the user name to look for
   * @return {@code true} if the user is subscribed for notifications on the
   *         node, {@code false} otherwise
   */
  @Override
  public boolean isUsersubscribedForNotification(String id, String userId) {
    NodeRef nodeRef = Converter.createNodeRefFromId(id);
    Set<NotifiableUser> users =
      notificationSubscriptionService.getNotifiableUsers(nodeRef);
    boolean result = false;
    for (NotifiableUser user : users) {
      if (user.getUserName().equals(userId)) {
        result = true;
        break;
      }
    }
    return result;
  }
}
