package io.swagger.api;

import eu.cec.digit.circabc.model.UserModel;
import eu.cec.digit.circabc.service.notification.*;
import eu.cec.digit.circabc.web.wai.dialog.notification.NotificationWrapper;
import io.swagger.model.*;
import io.swagger.util.ApiToolBox;
import io.swagger.util.Converter;
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author beaurpi
 */
public class NotificationsApiImpl implements NotificationsApi {

    static final Log logger = LogFactory.getLog(NotificationsApiImpl.class);
    private static final String ALLOWED = "ALLOWED";
    private static final String NOTIFICATION_STATUS = "NotificationStatus";
    private PermissionService permissionService;
    private PersonService personService;
    private NodeService nodeService;
    private NotificationManagerService notificationManagerService;
    private NotificationSubscriptionService notificationSubscriptionService;
    private BehaviourFilter policyBehaviourFilter;
    private ProfilesApi profilesApi;
    private UsersApi usersApi;
    private ApiToolBox apiToolBox;

    public ApiToolBox getApiToolBox() {
        return apiToolBox;
    }

    public void setApiToolBox(ApiToolBox apiToolBox) {
        this.apiToolBox = apiToolBox;
    }

    @Override
    public void nodesIdNotificationsAuthorityPut(String id, String authority, String value) {
        NodeRef nodeRef = Converter.createNodeRefFromId(id);
        // disable filter to prevent the change of the nodes' modified date
        policyBehaviourFilter.disableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);

        if (id != null && authority != null) {
            if ("on".equals(value)) {
                permissionService.setPermission(nodeRef, authority, NOTIFICATION_STATUS, true);
            } else if ("off".equals(value)) {
                permissionService.setPermission(nodeRef, authority, NOTIFICATION_STATUS, false);
            } else {
                if (!permissionService.getInheritParentPermissions(nodeRef)) {
                    permissionService.deletePermission(nodeRef, authority, NOTIFICATION_STATUS);
                }
            }
        }

        policyBehaviourFilter.enableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
    }

    /**
     * @see io.swagger.api.NotificationsApi#getPasteNotificationsState(java.lang.String)
     */
    @Override
    public PasteNotificationsState getPasteNotificationsState(String id) {

        NodeRef nodeRef = Converter.createNodeRefFromId(id);

        boolean pasteAllEnabled = notificationManagerService.isPasteAllNotificationEnabled(nodeRef);
        boolean pasteEnabled = notificationManagerService.isPasteNotificationEnabled(nodeRef);

        return new PasteNotificationsState(pasteEnabled, pasteAllEnabled);
    }

    /**
     * @see io.swagger.api.NotificationsApi#setPasteNotificationsState(java.lang.String, boolean,
     * boolean)
     */
    @Override
    public void setPasteNotificationsState(String id, boolean pasteEnable, boolean pasteAllEnable) {

        NodeRef nodeRef = Converter.createNodeRefFromId(id);

        notificationManagerService.setPasteNotificationEnabled(nodeRef, pasteEnable);
        notificationManagerService.setPasteAllNotificationEnabled(nodeRef, pasteAllEnable);
    }

    /**
     * @return the permissionService
     */
    public PermissionService getPermissionService() {
        return permissionService;
    }

    /**
     * @param permissionService the permissionService to set
     */
    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    /**
     * @return the nodeService
     */
    public NodeService getNodeService() {
        return nodeService;
    }

    /**
     * @param nodeService the nodeService to set
     */
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /**
     * @param notificationManagerService the notificationManagerService to set
     */
    public void setNotificationManagerService(NotificationManagerService notificationManagerService) {
        this.notificationManagerService = notificationManagerService;
    }

    /**
     * @param notificationSubscriptionService the notificationSubscriptionService to set
     */
    public void setNotificationSubscriptionService(
            NotificationSubscriptionService notificationSubscriptionService) {
        this.notificationSubscriptionService = notificationSubscriptionService;
    }

    /**
     * @param personService the personService to set
     */
    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    /**
     * @return the profilesApi
     */
    public ProfilesApi getProfilesApi() {
        return profilesApi;
    }

    /**
     * @param profilesApi the profilesApi to set
     */
    public void setProfilesApi(ProfilesApi profilesApi) {
        this.profilesApi = profilesApi;
    }

    /**
     * @return the usersApi
     */
    public UsersApi getUsersApi() {
        return usersApi;
    }

    /**
     * @param usersApi the usersApi to set
     */
    public void setUsersApi(UsersApi usersApi) {
        this.usersApi = usersApi;
    }

    @Override
    public NotificationDefinition nodesIdNotificationsGet(String id) {
        NodeRef nodeRef = Converter.createNodeRefFromId(id);
        NotificationDefinition result = new NotificationDefinition();

        NodeRef groupRef = apiToolBox.getCurrentInterestGroup(nodeRef);
        List<Profile> groupProfiles = profilesApi.groupsIdProfilesGet(groupRef.getId(), null, false);

        if (id != null) {
            Set<AccessPermission> notifications = permissionService.getAllSetPermissions(nodeRef);
            for (AccessPermission ac : notifications) {
                if (ac.getPermission().equals(NOTIFICATION_STATUS)
                        && ac.getAuthorityType().equals(AuthorityType.USER)) {
                    NotificationDefinitionUsers ndu = new NotificationDefinitionUsers();
                    ndu.setUser(usersApi.usersUserIdGet(ac.getAuthority()));
                    ndu.setNotifications(ac.getAccessStatus().name());
                    ndu.setInherited(ac.isInherited());
                    result.getUsers().add(ndu);
                } else if (ac.getPermission().equals(NOTIFICATION_STATUS)
                        && ac.getAuthorityType().equals(AuthorityType.GROUP)) {
                    NotificationDefinitionProfiles ndp = getProfileNotifications(ac, groupProfiles);
                    ndp.setInherited(ac.isInherited());
                    if (ndp.getProfile() != null) {
                        result.getProfiles().add(ndp);
                    }
                }
            }
        }

        return result;
    }

    private NotificationDefinitionProfiles getProfileNotifications(
            AccessPermission perm, List<Profile> groupProfiles) {

        NotificationDefinitionProfiles result = new NotificationDefinitionProfiles();
        result.setProfile(getProfile(perm, groupProfiles));
        result.setNotifications(perm.getAccessStatus().name());

        return result;
    }

    private Profile getProfile(AccessPermission perm, List<Profile> groupProfiles) {
        Profile result = null;
        for (Profile p : groupProfiles) {
            if (perm.getAuthority().equals(p.getGroupName())) {
                result = p;
            }
        }
        return result;
    }

    @Override
    public void nodesIdNotificationsAuthorityDelete(String id, String authority) {
        NodeRef nodeRef = Converter.createNodeRefFromId(id);
        if (id != null && authority != null) {
            //  disable filter to prevent the change of the nodes' modified date
            policyBehaviourFilter.disableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
            permissionService.deletePermission(nodeRef, authority, NOTIFICATION_STATUS);
            policyBehaviourFilter.enableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
        }
    }

    @Override
    public NotificationDefinition nodesIdNotificationsPost(String id, NotificationDefinition body) {

        NodeRef nodeRef = Converter.createNodeRefFromId(id);

        // disable filter to prevent the change of the nodes' modified date
        policyBehaviourFilter.disableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);

        for (NotificationDefinitionProfiles ndp : body.getProfiles()) {
            permissionService.setPermission(
                    nodeRef,
                    ndp.getProfile().getGroupName(),
                    NOTIFICATION_STATUS,
                    ndp.getNotifications().equals(ALLOWED));
        }

        for (NotificationDefinitionUsers ndu : body.getUsers()) {
            permissionService.setPermission(
                    nodeRef,
                    ndu.getUser().getUserId(),
                    NOTIFICATION_STATUS,
                    ndu.getNotifications().equals(ALLOWED));
        }

        policyBehaviourFilter.enableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);

        return nodesIdNotificationsGet(id);
    }

    /**
     * @see io.swagger.api.NotificationsApi#getNotifications(java.lang.String, int, int,
     * java.lang.String)
     */
    @Override
    public PagedNotificationConfigurations getNotifications(
            String id, int startItem, int amount, String language) {

        NodeRef nodeRef = Converter.createNodeRefFromId(id);

        final Set<AuthorityNotification> notifications =
                notificationSubscriptionService.getNotifications(nodeRef);
        final List<NotificationWrapper> result = new ArrayList<>(notifications.size());

        for (final AuthorityNotification notification : notifications) {

            NotificationWrapper wrapper = wrapNotification(notification, nodeRef, language);

            if (wrapper == null) {
                logger.error(
                        "The repository is corrupt. A notification has "
                                + "been set with a non-managed Authotity Type. Only "
                                + AuthorityType.GROUP
                                + "  and "
                                + AuthorityType.USER
                                + " are allowed. \n\tAuthority found: "
                                + notification.getAuthority()
                                + "\n\tFrom type:       "
                                + notification.getAuthorityType()
                                + "\n\tWith the status: "
                                + notification.getNotificationStatus()
                                + "\n\tAuthority found: "
                                + notification.getAuthorityType()
                                + "\n\tOn node:         "
                                + nodeRef.getId());
            }

            result.add(wrapper);
        }

        if (logger.isDebugEnabled()) {
            logger.debug(
                    result.size()
                            + " Notification status found for the node "
                            + nodeRef.getId()
                            + "\n\tNotifications: "
                            + result
                            + "\n\tFor node:      "
                            + nodeRef.getId());
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

    private NotificationWrapper wrapNotification(
            final AuthorityNotification authorityNotification, final NodeRef node, String language) {
        String name;
        I18nProperty title = null;

        switch (authorityNotification.getAuthorityType()) {
            case USER:
                name = computeUserLogin(authorityNotification.getAuthority());
                break;
            case GROUP:
                name = getProfileName(node, authorityNotification.getAuthority(), language);
                title = getProfileTitle(node, authorityNotification.getAuthority());
                break;
            default:
                return null;
        }

        NotificationWrapper result =
                new NotificationWrapper(
                        authorityNotification.getAuthorityType(),
                        name,
                        authorityNotification.getNotificationStatus(),
                        authorityNotification.getAuthority(),
                        node.getId(),
                        authorityNotification.getInherited());

        if (authorityNotification.getAuthorityType().name().equals("GROUP")) {
            result.setTitle(title);
        }

        return result;
    }

    private String computeUserLogin(String authorityName) {

        NodeRef personRef = personService.getPerson(authorityName);

        Map<QName, Serializable> properties = nodeService.getProperties(personRef);

        String displayName = (String) properties.get(UserModel.PROP_ECAS_USER_NAME);
        if (displayName == null) {
            displayName = (String) properties.get(ContentModel.PROP_USERNAME);
        }
        return displayName;
    }

    private String getProfileName(NodeRef nodeRef, String authorityName, String language) {

        String igId = apiToolBox.getCurrentInterestGroup(nodeRef).getId();

        List<Profile> profiles = profilesApi.groupsIdProfilesGet(igId, null, false);

        for (final Profile profile : profiles) {
            if (profile.getGroupName().equals(authorityName)) {
                String title = profile.getTitle().get(language);
                if (title == null) {
                    title = profile.getTitle().get("en-US");
                    if (title == null) {
                        logger.warn(
                                "Profile title not found for language '"
                                        + language
                                        + "' nor default 'en-US'. Retrieving the name instead: "
                                        + profile.getName());
                        return profile.getName();
                    }
                }
                return title;
            }
        }

        return null;
    }

    private I18nProperty getProfileTitle(NodeRef nodeRef, String authorityName) {

        String igId = apiToolBox.getCurrentInterestGroup(nodeRef).getId();

        List<Profile> profiles = profilesApi.groupsIdProfilesGet(igId, null, false);

        for (final Profile profile : profiles) {
            if (profile.getGroupName().equals(authorityName)) {
                return profile.getTitle();
            }
        }

        return null;
    }

    /**
     * @see io.swagger.api.NotificationsApi#getNotifiableUsers(java.lang.String, int, int)
     */
    @Override
    public PagedNotificationSubscribedUsers getNotifiableUsers(String id, int startItem, int amount) {

        NodeRef nodeRef = Converter.createNodeRefFromId(id);

        List<NotifiableUser> result =
                new ArrayList<>(notificationSubscriptionService.getNotifiableUsers(nodeRef));

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
     * @see io.swagger.api.NotificationsApi#removeNotification(java.lang.String, java.lang.String)
     */
    @Override
    public void removeNotification(String id, String authority) {

        NodeRef nodeRef = Converter.createNodeRefFromId(id);

        notificationSubscriptionService.removeNotification(nodeRef, authority);
    }

    /**
     * @see io.swagger.api.NotificationsApi#setNotificationStatus(java.lang.String, java.lang.String,
     * eu.cec.digit.circabc.service.notification.NotificationStatus)
     */
    @Override
    public void setNotificationStatus(String id, String authority, NotificationStatus status) {

        NodeRef nodeRef = Converter.createNodeRefFromId(id);

        notificationSubscriptionService.setNotificationStatus(nodeRef, authority, status);
    }

    @Override
    public boolean isUsersubscribedForNotification(String id, String userId) {
        NodeRef nodeRef = Converter.createNodeRefFromId(id);
        Set<NotifiableUser> users = notificationSubscriptionService.getNotifiableUsers(nodeRef);
        boolean result = false;
        for (NotifiableUser user : users) {
            if (user.getUserName().equals(userId)) {
                result = true;
                break;
            }
        }
        return result;
    }

    public BehaviourFilter getPolicyBehaviourFilter() {
        return policyBehaviourFilter;
    }

    public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
        this.policyBehaviourFilter = policyBehaviourFilter;
    }
}
