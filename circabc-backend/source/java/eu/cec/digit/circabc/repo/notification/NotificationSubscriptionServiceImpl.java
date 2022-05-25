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
package eu.cec.digit.circabc.repo.notification;

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.model.UserModel;
import eu.cec.digit.circabc.service.notification.*;
import eu.cec.digit.circabc.service.profile.IGRootProfileManagerService;
import eu.cec.digit.circabc.service.profile.Profile;
import eu.cec.digit.circabc.service.profile.ProfileManagerServiceFactory;
import eu.cec.digit.circabc.service.struct.ManagementService;
import eu.cec.digit.circabc.service.user.UserService;
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.*;

/**
 * Concrete implementation of the Notification Subscription Service. The status is stored as a
 * permission.
 *
 * @author Yanick Pignot
 */
public class NotificationSubscriptionServiceImpl implements NotificationSubscriptionService {

    /**
     * The notification stored as a permission
     */
    public static final String NOTIFICATION_AS_PERMISSION = "NotificationStatus";
    /**
     * Logger
     */
    private static final Log logger = LogFactory.getLog(NotificationSubscriptionServiceImpl.class);
    /**
     * The notification status defined by default for any authority in any Intrest Group
     */
    public final NotificationStatus DEFAULT_TOP_LEVEL_STATUS = NotificationStatus.UNSUBSCRIBED;
    /**
     * The notification status defined by default for any authority in any node under an Interest
     * Group
     */
    public final NotificationStatus DEFAULT_STATUS_ANY_OTHER_NODE = NotificationStatus.INHERITED;

    private PermissionService permissionService;
    private NodeService nodeService;
    private ManagementService managementService;
    private IGRootProfileManagerService igProfileManagerService;
    private ProfileManagerServiceFactory profileManagerServiceFactory;
    private PersonService personService;
    private UserService userService;
    private MultilingualContentService multilingualContentService;

    private BehaviourFilter policyBehaviourFilter;

    public Set<AuthorityNotification> getNotifications(final NodeRef nodeRef) {
        ParameterCheck.mandatory("The node ref", nodeRef);

        if (logger.isDebugEnabled()) {
            logger.debug("Trying to retive the notification status for the node " + nodeRef + " ...");
        }

        // retreive all permission of the given node
        final Set<AccessPermission> permissions = getPermissionService().getAllSetPermissions(nodeRef);

        final boolean inheritParentPermissions =
                getPermissionService().getInheritParentPermissions(nodeRef);

        final Set<AuthorityNotification> parentNotifications = new HashSet<>();
        if (inheritParentPermissions) {
            Set<AccessPermission> parentPermissions = Collections.emptySet();
            ChildAssociationRef childAssociationRef = nodeService.getPrimaryParent(nodeRef);
            NodeRef parent = childAssociationRef.getParentRef();
            parentPermissions = getPermissionService().getAllSetPermissions(parent);
            for (final AccessPermission permission : parentPermissions) {
                if (isPermissionNotificationStatus(permission.getPermission())) {
                    parentNotifications.add(
                            new AuthorityNotificationImpl(
                                    computeAccessPermission(permission.getAccessStatus()),
                                    permission.getAuthority(),
                                    true));
                }
            }
        }

        final Set<AuthorityNotification> notifications = new HashSet<>(permissions.size());
        notifications.addAll(parentNotifications);

        // set the notification list by filtering the permissions that we need.
        for (final AccessPermission permission : permissions) {
            if (isPermissionNotificationStatus(permission.getPermission())) {

                final AuthorityNotificationImpl authorityNotif =
                        new AuthorityNotificationImpl(
                                computeAccessPermission(permission.getAccessStatus()),
                                permission.getAuthority(),
                                false);
                if (!parentNotifications.contains(authorityNotif)) {
                    notifications.add(authorityNotif);
                }
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug(
                    notifications.size()
                            + " Notification(s) status successfully retrived for the node "
                            + nodeRef
                            + "\n\tName:                      "
                            + nodeService.getProperty(nodeRef, ContentModel.PROP_NAME)
                            + "\n\tNotification status found: "
                            + notifications);
        }

        return notifications;
    }

    public void setNotificationStatus(
            final NodeRef nodeRef, final String authority, final NotificationStatus status) {
        checkAndReturnIG(nodeRef);
        ParameterCheck.mandatory("The notification status", status);
        ParameterCheck.mandatoryString("The autority", authority);

        final AuthorityType type = AuthorityType.getAuthorityType(authority);

        if (type == null || (!type.equals(AuthorityType.GROUP) && !type.equals(AuthorityType.USER))) {
            throw new IllegalStateException(
                    "The notification can only be setted to a Profile or to a user. Authority "
                            + authority
                            + " from type "
                            + type
                            + " is not allowed.");
        }

        if (status.equals(NotificationStatus.INHERITED)
                && nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_IGROOT)) {
            throw new IllegalStateException(
                    "The INHERITED notification status is not allowed for an Interest Group. Please select either SUSCRIBE or UNSUSCRIBE ");
        }

        policyBehaviourFilter.disableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);

        switch (status) {
            case SUBSCRIBED:
                getPermissionService().setPermission(nodeRef, authority, NOTIFICATION_AS_PERMISSION, true);
                break;

            case UNSUBSCRIBED:
                getPermissionService().setPermission(nodeRef, authority, NOTIFICATION_AS_PERMISSION, false);
                break;

            case INHERITED:
                removeNotificationImpl(nodeRef, authority);
                break;
        }
    }

    public AuthorityNotification getAuthorityNotificationStatus(
            final NodeRef nodeRef, final String authority) throws InvalidNodeRefException {
        checkAndReturnIG(nodeRef);
        ParameterCheck.mandatoryString("The authority", authority);

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Trying to retive the notification status of the authority "
                            + authority
                            + " for the node "
                            + nodeRef
                            + " ...");
        }

        final NodeRef interestGroup = getManagementService().getCurrentInterestGroup(nodeRef);

        final NotificationStatus notificationStatus =
                getAuthorityNotificationStatusImpl(nodeRef, interestGroup, authority);

        final AuthorityNotification authorityNotification =
                new AuthorityNotificationImpl(notificationStatus, authority);

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Notification status successfully retrived for the authority "
                            + authority
                            + "\n\tOn node: "
                            + nodeRef
                            + "\n\tResult :	"
                            + authorityNotification);
        }

        return authorityNotification;
    }

    public void removeNotification(NodeRef nodeRef, String authority) {
        ParameterCheck.mandatoryString("The autority", authority);
        ParameterCheck.mandatory("The noderef", nodeRef);

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Trying to remove the notification settings of the authority "
                            + authority
                            + " on the node "
                            + nodeRef
                            + "...");
        }

        removeNotificationImpl(nodeRef, authority);

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Notification settings successfully remove for the authority "
                            + authority
                            + " on the node "
                            + nodeRef
                            + "...");
        }
    }

    public UserNotificationReport getUserNotificationReport(
            final NodeRef nodeRef, final String userAuthority) {

        ParameterCheck.mandatory("The nodeRef", nodeRef);
        ParameterCheck.mandatoryString("The user Authority", userAuthority);

        if (!AuthorityType.getAuthorityType(userAuthority).equals(AuthorityType.USER)
                || !personService.personExists(userAuthority)) {
            if (logger.isDebugEnabled()) {
                logger.debug(
                        "No User Notification Report found for "
                                + userAuthority
                                + " on "
                                + nodeRef
                                + ". Reason: the user is be an right user (can't be ADMIN or GUEST)");
            }
            // no exception, the report is empty.
            return null;
        }

        final NodeRef igNoderef = managementService.getCurrentInterestGroup(nodeRef);
        if (igNoderef == null) {
            if (logger.isDebugEnabled()) {
                logger.debug(
                        "No User Notification Report found for "
                                + userAuthority
                                + " on "
                                + nodeRef
                                + ". Reason: nodeRef not a Interest Group child");
            }

            // no exception, the report is empty.
            return null;
        }

        // The profile can be null because the user can not be invited in the IG
        String profile = null;

        if (getIGProfileManagerService().getInvitedUsers(igNoderef).contains(userAuthority)) {
            profile = getPersonProfile(igNoderef, userAuthority);
        }

        final UserNotificationReport report =
                getReportUserProfileImpl(nodeRef, igNoderef, userAuthority, profile);

        if (logger.isDebugEnabled()) {
            logger.debug("User notification report successfully build " + report);
        }

        return report;
    }

    public Set<NotifiableUser> getNotifiableUsers(final NodeRef nodeRef)
            throws InvalidNodeRefException {

        final NodeRef igNoderef = checkAndReturnIG(nodeRef);

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Trying to retive the notifiable users settest in the interest group "
                            + igNoderef
                            + " of the node "
                            + nodeRef
                            + "...");
        }

        final Map<String, LeveledNotificationStatus> userNotif = new HashMap<>(10);
        final Map<String, LeveledNotificationStatus> profileNotif = new HashMap<>(80);

        // fill the explicit notifications map seperatly for users and profiles
        fillNotifiableAuthoritiesImpl(nodeRef, igNoderef, 0, userNotif, profileNotif);

        // with the profile define notifications, enhance the use map. At the same level, the user entry
        // is
        // louder that the profile entry.
        Set<String> usersInProfile = null;
        Profile profile;
        LeveledNotificationStatus userLeveledNotificationStatus = null;
        for (final Map.Entry<String, LeveledNotificationStatus> entry : profileNotif.entrySet()) {
            profile = getIGProfileManagerService().getProfileFromGroup(igNoderef, entry.getKey());
            if (profile != null) {
                usersInProfile =
                        getIGProfileManagerService().getPersonInProfile(igNoderef, profile.getProfileName());
                for (final String user : usersInProfile) {
                    userLeveledNotificationStatus = userNotif.get(user);
                    if (userLeveledNotificationStatus == null
                            || (userLeveledNotificationStatus.level > entry.getValue().level)) {
                        userNotif.put(user, entry.getValue());
                    }
                }
            }
        }

        // the last step is to return the users that are SUSCRIBE and where the Global Notification
        // properties is set as true
        final Set<NotifiableUser> usersToNotify = new HashSet<>(userNotif.size());

        NodeRef person = null;
        Map<QName, Serializable> properties = null;
        Boolean globalNotif;
        String authority;
        Locale locale;
        Serializable langObject;
        Serializable contentLanguageFilter;

        Locale contentLocale;

        Locale documentLocale;
        Locale pivotlocale;
        String documentLanguage = null;
        String pivotLanguage = null;
        boolean isMultilingualDocument =
                nodeService.hasAspect(nodeRef, ContentModel.ASPECT_MULTILINGUAL_DOCUMENT);
        if (isMultilingualDocument) {
            NodeRef pivot = getMultilingualContentService().getPivotTranslation(nodeRef);
            pivotlocale = (Locale) getNodeService().getProperty(pivot, ContentModel.PROP_LOCALE);
            if (getNodeService().hasAspect(nodeRef, ContentModel.ASPECT_MULTILINGUAL_EMPTY_TRANSLATION)) {
                documentLocale = pivotlocale;
            } else {
                documentLocale = (Locale) getNodeService().getProperty(nodeRef, ContentModel.PROP_LOCALE);
            }
            documentLanguage = documentLocale.getLanguage();
            pivotLanguage = pivotlocale.getLanguage();
        }

        for (final Map.Entry<String, LeveledNotificationStatus> entry : userNotif.entrySet()) {
            userLeveledNotificationStatus = entry.getValue();
            if (NotificationStatus.SUBSCRIBED.equals(userLeveledNotificationStatus.status)) {
                authority = entry.getKey();
                person = getPersonService().getPerson(authority);
                properties = getNodeService().getProperties(person);
                globalNotif = (Boolean) properties.get(UserModel.PROP_GLOBAL_NOTIFICATION);

                // if global notifications are turned off for this user, the user is not added/shown,
                // because global
                // notifications cannot be overridden
                if (globalNotif != null && globalNotif == true) {
                    // the reason why we need read permission because to fill the mail (notification) template
                    // we need the path
                    // to the affected folder to be included in it; and to include it we need access to it
                    final Boolean hasReadPermision =
                            (Boolean)
                                    AuthenticationUtil.runAs(
                                            new AuthenticationUtil.RunAsWork<Object>() {
                                                public Object doWork() {
                                                    return getPermissionService()
                                                            .hasPermission(nodeRef, PermissionService.READ_PROPERTIES)
                                                            .equals(AccessStatus.ALLOWED);
                                                }
                                            },
                                            authority);

                    if (hasReadPermision) {
                        // for instance, the interface language can be set either under a String or Locale
                        langObject =
                                getUserService().getPreference(person, UserService.PREF_INTERFACE_LANGUAGE);
                        locale = getLocale(langObject);
                        if (isMultilingualDocument) {

                            contentLanguageFilter =
                                    getUserService().getPreference(person, UserService.PREF_CONTENT_FILTER_LANGUAGE);
                            contentLocale = getLocale(contentLanguageFilter);
                            String contentLanguage = "all";
                            if (contentLocale != null) {
                                contentLanguage = contentLocale.getLanguage();
                            }
                            boolean isDocumentAndContentLanguageSame =
                                    documentLanguage.equalsIgnoreCase(contentLanguage);
                            boolean isDocumentAndPivitLanguageSame =
                                    documentLanguage.equalsIgnoreCase(pivotLanguage);
                            // notify user if content filter language is all or if it is content filter language
                            // or pivot language
                            if (contentLocale == null
                                    || isDocumentAndContentLanguageSame
                                    || isDocumentAndPivitLanguageSame) {
                                usersToNotify.add(new NotifiableUserImpl(person, locale, properties));
                            }
                        } else {
                            usersToNotify.add(new NotifiableUserImpl(person, locale, properties));
                        }
                    }
                }
            }
        }

        return usersToNotify;
    }

    private Locale getLocale(Serializable langObject) {
        Locale locale;
        if (langObject == null) {
            locale = null;
        } else if (langObject instanceof Locale) {
            locale = (Locale) langObject;
        } else {
            locale = new Locale(langObject.toString());
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
            final Map<String, LeveledNotificationStatus> profileNotif) {
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

            if (AuthorityType.GROUP.equals(type) && !authorityNotification.getInherited()) {
                if (!profileNotif.containsKey(authority) && !authorityNotification.getInherited()) {
                    profileNotif.put(authority, new LeveledNotificationStatus(status, level));
                }
            } else {
                if (!userNotif.containsKey(authority) && !authorityNotification.getInherited()) {
                    userNotif.put(authority, new LeveledNotificationStatus(status, level));
                }
            }
        }

        if (!from.equals(to)) {
            final ChildAssociationRef parentAssoc = getNodeService().getPrimaryParent(from);
            final NodeRef parentRef = parentAssoc.getParentRef();
            // TODO: see with Yanick
            fillNotifiableAuthoritiesImpl(parentRef, to, ++level, userNotif, profileNotif);
        }
    }

    private String getPersonProfile(NodeRef nodeRef, String userAutority) {
        final String profileName = getIGProfileManagerService().getPersonProfile(nodeRef, userAutority);
        final Profile pofile = getIGProfileManagerService().getProfile(nodeRef, profileName);

        return pofile.getPrefixedAlfrescoGroupName();
    }

    private void removeNotificationImpl(final NodeRef nodeRef, final String authority) {
        if (getDefinedAuthorityStatus(nodeRef, authority) != null) {
            getPermissionService().deletePermission(nodeRef, authority, NOTIFICATION_AS_PERMISSION);
        }
    }

    private NotificationStatus getAuthorityNotificationStatusImpl(
            final NodeRef fromRef, final NodeRef toRef, final String authority)
            throws InvalidNodeRefException {
        NotificationStatus status = getDefinedAuthorityStatus(fromRef, authority);

        if (status == null) {
            if (toRef == null) {
                status = DEFAULT_STATUS_ANY_OTHER_NODE;
            } else if (fromRef.equals(toRef)) {
                status = DEFAULT_TOP_LEVEL_STATUS;
            } else {
                final ChildAssociationRef parentAssoc = getNodeService().getPrimaryParent(fromRef);
                final NodeRef parentRef = parentAssoc.getParentRef();

                status = getAuthorityNotificationStatusImpl(parentRef, toRef, authority);
            }
        }

        return status;
    }

    private UserNotificationReport getReportUserProfileImpl(
            final NodeRef nodeRef, final NodeRef igRef, final String user, final String profile) {

        boolean isNodeRefIg = nodeRef.equals(igRef);

        final UserProfileStatuses statuses = fillStatuses(nodeRef, isNodeRefIg, user, profile);

        // We computed the access status of the given space. No recusive search is needed.
        // Now, compute if the user will recieve notification.
        final NodeRef person = getPersonService().getPerson(user);
        final Boolean globalNotification =
                (Boolean) getNodeService().getProperty(person, UserModel.PROP_GLOBAL_NOTIFICATION);

        Boolean willUserReceive = null;

        if (globalNotification != null && globalNotification == true) {
            UserProfileStatuses tempStatuses = statuses;
            NodeRef tempNodeRef = nodeRef;

            while (willUserReceive == null) {
                switch (tempStatuses.userStatus) {
                    case SUBSCRIBED:
                        willUserReceive = Boolean.TRUE;
                        break;
                    case UNSUBSCRIBED:
                        willUserReceive = Boolean.FALSE;
                        break;
                    case INHERITED:
                        willUserReceive = null;
                        break;
                }

                if (willUserReceive == null) {
                    // no explicit definition found for the user.
                    switch (tempStatuses.profileStatus) {
                        case SUBSCRIBED:
                            willUserReceive = Boolean.TRUE;
                            break;
                        case UNSUBSCRIBED:
                            willUserReceive = Boolean.FALSE;
                            break;
                        case INHERITED:
                            willUserReceive = null;
                            break;
                    }
                }

                // the worst case, no notification setted for the given space
                if (willUserReceive == null) {
                    final ChildAssociationRef parentAssoc = getNodeService().getPrimaryParent(tempNodeRef);
                    tempNodeRef = parentAssoc.getParentRef();
                    boolean topFolder = (tempNodeRef == null) || (tempNodeRef.equals(igRef));

                    tempStatuses = fillStatuses(tempNodeRef, topFolder, user, profile);
                }
            }
        } else {
            // the user will not receive the notification status
            willUserReceive = Boolean.FALSE;
        }

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
                willUserReceive);
    }

    private UserProfileStatuses fillStatuses(
            final NodeRef nodeRef, boolean topLevelNodeRef, final String user, final String profile) {
        NotificationStatus userStatus = null;
        NotificationStatus profileStatus = null;

        String authority = null;
        final Set<AccessPermission> permissions = getPermissionService().getAllSetPermissions(nodeRef);
        for (AccessPermission permission : permissions) {
            if (permission.getPermission().equals(NOTIFICATION_AS_PERMISSION)) {
                authority = permission.getAuthority();

                if (authority.equals(user)) {
                    userStatus = computeAccessPermission(permission.getAccessStatus());

                    if (profileStatus != null || profile == null) {
                        // profile status already setted too, the search is finished.
                        break;
                    }
                } else if (profile != null && authority.equals(profile)) {
                    profileStatus = computeAccessPermission(permission.getAccessStatus());

                    if (userStatus != null) {
                        // user status already setted too, the search is finished.
                        break;
                    }
                }
            }
        }

        if (userStatus == null) {
            userStatus = topLevelNodeRef ? DEFAULT_TOP_LEVEL_STATUS : DEFAULT_STATUS_ANY_OTHER_NODE;
        }
        if (profileStatus == null) {
            profileStatus = topLevelNodeRef ? DEFAULT_TOP_LEVEL_STATUS : DEFAULT_STATUS_ANY_OTHER_NODE;
        }

        return new UserProfileStatuses(userStatus, profileStatus);
    }

    private NotificationStatus getDefinedAuthorityStatus(
            final NodeRef nodeRef, final String authority) {
        NotificationStatus status = null;

        final Set<AccessPermission> permissions = getPermissionService().getAllSetPermissions(nodeRef);
        for (AccessPermission permission : permissions) {
            if (permission.getAuthority().equals(authority)
                    && permission.getPermission().equals(NOTIFICATION_AS_PERMISSION)) {
                status = computeAccessPermission(permission.getAccessStatus());
                break;
            }
        }

        return status;
    }

    /**
     * Wrap an AccessStatus to a Notification status
     */
    private NotificationStatus computeAccessPermission(final AccessStatus accessStatus) {
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

    private NodeRef checkAndReturnIG(final NodeRef nodeRef) throws InvalidNodeRefException {
        if (nodeRef == null || !nodeService.exists(nodeRef)) {
            throw new InvalidNodeRefException(
                    "The node ref is a mandatory parameter and must be existing.", nodeRef);
        }

        final NodeRef igNoderef = managementService.getCurrentInterestGroup(nodeRef);

        if (igNoderef == null) {
            throw new InvalidNodeRefException(
                    "The node received as parameter is not an Interest Group child.", nodeRef);
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
    public final void setPermissionService(final PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    // -----------------------------
    // IOC

    /**
     * @return the nodeService
     */
    protected final NodeService getNodeService() {
        return nodeService;
    }

    /**
     * @param nodeService the nodeService to set
     */
    public final void setNodeService(final NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /**
     * @return the managementService
     */
    protected final ManagementService getManagementService() {
        return managementService;
    }

    /**
     * @param managementService the managementService to set
     */
    public final void setManagementService(ManagementService managementService) {
        this.managementService = managementService;
    }

    /**
     * @return the profileManagerServiceFactory
     */
    protected final ProfileManagerServiceFactory getProfileManagerServiceFactory() {
        return profileManagerServiceFactory;
    }

    /**
     * @param profileManagerServiceFactory the profileManagerServiceFactory to set
     */
    public final void setProfileManagerServiceFactory(
            ProfileManagerServiceFactory profileManagerServiceFactory) {
        this.profileManagerServiceFactory = profileManagerServiceFactory;
    }

    /**
     * @return the profileManagerServiceFactory
     */
    protected final IGRootProfileManagerService getIGProfileManagerService() {
        if (igProfileManagerService == null) {
            igProfileManagerService = getProfileManagerServiceFactory().getIGRootProfileManagerService();
        }
        return igProfileManagerService;
    }

    /**
     * @return the personService
     */
    protected final PersonService getPersonService() {
        return personService;
    }

    /**
     * @param personService the personService to set
     */
    public final void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    /**
     * @return the userService
     */
    protected final UserService getUserService() {
        return userService;
    }

    /**
     * @param userService the userService to set
     */
    public final void setUserService(UserService userService) {
        this.userService = userService;
    }

    public BehaviourFilter getPolicyBehaviourFilter() {
        return policyBehaviourFilter;
    }

    public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
        this.policyBehaviourFilter = policyBehaviourFilter;
    }

    public MultilingualContentService getMultilingualContentService() {
        return multilingualContentService;
    }

    public void setMultilingualContentService(MultilingualContentService multilingualContentService) {
        this.multilingualContentService = multilingualContentService;
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
                final NotificationStatus userStatus, final NotificationStatus profileStatus) {
            super();
            this.userStatus = userStatus;
            this.profileStatus = profileStatus;
        }
    }
}
