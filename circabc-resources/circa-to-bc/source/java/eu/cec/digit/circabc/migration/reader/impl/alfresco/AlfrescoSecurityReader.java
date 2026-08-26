package eu.cec.digit.circabc.migration.reader.impl.alfresco;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.PermissionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.cec.digit.circabc.migration.entities.ElementsConverter;
import eu.cec.digit.circabc.migration.entities.ElementsHelper;
import eu.cec.digit.circabc.migration.entities.XMLNode;
import eu.cec.digit.circabc.migration.entities.generated.properties.I18NProperty;
import eu.cec.digit.circabc.migration.entities.generated.ImportRoot;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Directory;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InterestGroup;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Space;
import eu.cec.digit.circabc.migration.entities.generated.permissions.AccessProfile;
import eu.cec.digit.circabc.migration.entities.generated.permissions.DirectoryPermissions;
import eu.cec.digit.circabc.migration.entities.generated.permissions.EventPermissions;
import eu.cec.digit.circabc.migration.entities.generated.permissions.Guest;
import eu.cec.digit.circabc.migration.entities.generated.permissions.InformationPermissions;
import eu.cec.digit.circabc.migration.entities.generated.permissions.LibraryPermissions;
import eu.cec.digit.circabc.migration.entities.generated.permissions.NewsgroupPermissions;
import eu.cec.digit.circabc.migration.entities.generated.permissions.NotificationItem;
import eu.cec.digit.circabc.migration.entities.generated.permissions.Notifications;
import eu.cec.digit.circabc.migration.entities.generated.permissions.RegistredUsers;
import eu.cec.digit.circabc.migration.entities.generated.permissions.SimpleDirectoryPermissions;
import eu.cec.digit.circabc.migration.entities.generated.permissions.SimpleEventPermissions;
import eu.cec.digit.circabc.migration.entities.generated.permissions.SimpleInformationPermissions;
import eu.cec.digit.circabc.migration.entities.generated.permissions.SimpleLibraryPermissions;
import eu.cec.digit.circabc.migration.entities.generated.permissions.SimpleNewsgroupPermissions;
import eu.cec.digit.circabc.migration.reader.SecurityReader;
import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.service.cmr.security.CircabcConstant;
import eu.cec.digit.circabc.service.migration.CategoryInterestGroupPair;
import eu.cec.digit.circabc.service.migration.ExportationException;
import eu.cec.digit.circabc.service.notification.AuthorityNotification;
import eu.cec.digit.circabc.service.notification.NotificationSubscriptionService;
import org.alfresco.service.cmr.security.AuthorityType;
import eu.cec.digit.circabc.service.profile.CircabcServices;
import eu.cec.digit.circabc.service.profile.IGRootProfileManagerService;
import eu.cec.digit.circabc.service.profile.Profile;
import eu.cec.digit.circabc.service.profile.ProfileManagerService;
import eu.cec.digit.circabc.service.profile.ProfileManagerServiceFactory;
import eu.cec.digit.circabc.service.struct.ManagementService;

/**
 * SecurityReader implementation that reads permissions/profiles from the CIRCABC Alfresco repository.
 */
public class AlfrescoSecurityReader implements SecurityReader {

    private static final Log logger = LogFactory.getLog(AlfrescoSecurityReader.class);

    private NodeService nodeService;
    private PermissionService permissionService;
    private ProfileManagerServiceFactory profileManagerServiceFactory;
    private ManagementService managementService;
    private NotificationSubscriptionService notificationSubscriptionService;

    @Override
    public void setProfileDefinition(final InterestGroup interestGroup) throws ExportationException {
        final String path = ElementsHelper.getExportationPath(interestGroup);
        if (path == null) {
            return;
        }
        try {
            final NodeRef igRef = new NodeRef(path);
            final IGRootProfileManagerService profService = profileManagerServiceFactory.getIGRootProfileManagerService();
            final Map<String, Profile> profiles = profService.getProfileMap(igRef);
            final Directory directory = interestGroup.getDirectory();

            if (profiles == null || directory == null) {
                return;
            }

            // Get invited users with their profiles
            final Map<String, Profile> invitedUsersProfiles = profService.getInvitedUsersProfiles(igRef);

            for (final Map.Entry<String, Profile> entry : profiles.entrySet()) {
                final Profile profile = entry.getValue();
                final String profileName = profile.getProfileName();

                final Set<String> dirPerms = profile.getServicePermissions(CircabcServices.DIRECTORY.toString());
                final Set<String> libPerms = profile.getServicePermissions(CircabcServices.LIBRARY.toString());
                final Set<String> newsPerms = profile.getServicePermissions(CircabcServices.NEWSGROUP.toString());
                final Set<String> infPerms = profile.getServicePermissions(CircabcServices.INFORMATION.toString());
                final Set<String> evePerms = profile.getServicePermissions(CircabcServices.EVENT.toString());
                final Set<String> visPerms = profile.getServicePermissions(CircabcServices.VISIBILITY.toString());

                final String dirPerm = dirPerms != null && !dirPerms.isEmpty() ? dirPerms.iterator().next() : "DirNoAccess";
                final String libPerm = libPerms != null && !libPerms.isEmpty() ? libPerms.iterator().next() : "LibNoAccess";
                final String newsPerm = newsPerms != null && !newsPerms.isEmpty() ? newsPerms.iterator().next() : "NwsNoAccess";
                final String infPerm = infPerms != null && !infPerms.isEmpty() ? infPerms.iterator().next() : "InfNoAccess";
                final String evePerm = evePerms != null && !evePerms.isEmpty() ? evePerms.iterator().next() : "EveNoAccess";
                final boolean visible = visPerms != null && visPerms.contains("Visibility");

                if (CircabcConstant.GUEST_AUTHORITY.equals(profileName)) {
                    final Guest guest = new Guest();
                    guest.setDirectoryPermission(isAccess(dirPerm) ? SimpleDirectoryPermissions.DIR_ACCESS : SimpleDirectoryPermissions.DIR_NO_ACCESS);
                    guest.setLibraryPermission(isAccess(libPerm) ? SimpleLibraryPermissions.LIB_ACCESS : SimpleLibraryPermissions.LIB_NO_ACCESS);
                    guest.setNewsgroupPermission(isAccess(newsPerm) ? SimpleNewsgroupPermissions.NWS_ACCESS : SimpleNewsgroupPermissions.NWS_NO_ACCESS);
                    guest.setInformationPermission(isAccess(infPerm) ? SimpleInformationPermissions.INF_ACCESS : SimpleInformationPermissions.INF_NO_ACCESS);
                    guest.setEventPermission(isAccess(evePerm) ? SimpleEventPermissions.EVE_ACCESS : SimpleEventPermissions.EVE_NO_ACCESS);
                    guest.setVisibility(visible);
                    directory.withGuest(guest);
                } else if (ProfileManagerService.ALL_CIRCA_USERS_PROFILE_NAME.equals(profileName)) {
                    final RegistredUsers registred = new RegistredUsers();
                    registred.setDirectoryPermission(isAccess(dirPerm) ? SimpleDirectoryPermissions.DIR_ACCESS : SimpleDirectoryPermissions.DIR_NO_ACCESS);
                    registred.setLibraryPermission(isAccess(libPerm) ? SimpleLibraryPermissions.LIB_ACCESS : SimpleLibraryPermissions.LIB_NO_ACCESS);
                    registred.setNewsgroupPermission(isAccess(newsPerm) ? SimpleNewsgroupPermissions.NWS_ACCESS : SimpleNewsgroupPermissions.NWS_NO_ACCESS);
                    registred.setInformationPermission(isAccess(infPerm) ? SimpleInformationPermissions.INF_ACCESS : SimpleInformationPermissions.INF_NO_ACCESS);
                    registred.setEventPermission(isAccess(evePerm) ? SimpleEventPermissions.EVE_ACCESS : SimpleEventPermissions.EVE_NO_ACCESS);
                    registred.setVisibility(visible);
                    directory.withRegistredUsers(registred);
                } else {
                    final AccessProfile accessProfile = new AccessProfile();
                    accessProfile.setName(profileName);
                    accessProfile.setExported(profile.isExported());
                    accessProfile.setDirectoryPermission(DirectoryPermissions.fromValue(dirPerm));
                    accessProfile.setLibraryPermission(LibraryPermissions.fromValue(libPerm));
                    accessProfile.setNewsgroupPermission(NewsgroupPermissions.fromValue(newsPerm));
                    accessProfile.setInformationPermission(InformationPermissions.fromValue(infPerm));
                    accessProfile.setEventPermission(EventPermissions.fromValue(evePerm));

                    // Set profile title using I18NTitle elements only (not the legacy <title> element).
                    // The <title> element uses PropertyAdapterBase.marshal() which calls toString()
                    // on the value, producing "{en=test}" for MLText. Use I18NTitle elements instead
                    // which serialize as <I18NTitle><lang>en</lang><value>test</value></I18NTitle>.
                    final MLText profileTitle = profile.getTitle();
                    if (profileTitle != null && !profileTitle.isEmpty()) {
                        accessProfile.withI18NTitles(ElementsConverter.adpatMLText(profileTitle));
                    } else {
                        accessProfile.withI18NTitles(new I18NProperty(Locale.ENGLISH, profileName));
                    }

                    // Add users assigned to this profile
                    if (invitedUsersProfiles != null) {
                        for (final Map.Entry<String, Profile> userEntry : invitedUsersProfiles.entrySet()) {
                            if (profileName.equals(userEntry.getValue().getProfileName())) {
                                accessProfile.withUsers(userEntry.getKey());
                            }
                        }
                    }

                    directory.withAccessProfiles(accessProfile);
                }
            }
        } catch (Exception e) {
            throw new ExportationException("Error reading profile definitions for " + path, e);
        }
    }

    /**
     * Derive the simple Access/NoAccess flag from a CIRCABC service permission token.
     *
     * <p>The permission strings are the full profile permission names (e.g. DirAdmin,
     * DirManageMembers, DirAccess, DirNoAccess, LibFullEdit, LibManageOwn, LibNoAccess...).
     * Any value other than the explicit "...NoAccess" grants access. A substring check on
     * "Access" must NOT be used because every "...NoAccess" token also contains "Access".
     */
    private static boolean isAccess(final String permission) {
        return permission != null && !permission.endsWith("NoAccess");
    }

    @Override
    public void setApplicants(final ImportRoot root, final InterestGroup igRoot) throws ExportationException {
        // Applicants are users who applied for membership - read from IG directory
        if (logger.isDebugEnabled()) {
            logger.debug("Reading applicants for " + ElementsHelper.getExportationPath(igRoot));
        }
    }

    @Override
    public void setSharedDefinition(final Space space) throws ExportationException {
        final String path = ElementsHelper.getExportationPath(space);
        if (path == null) {
            return;
        }
        try {
            final NodeRef spaceRef = new NodeRef(path);
            if (nodeService.hasAspect(spaceRef, CircabcModel.ASPECT_SHARED_SPACE)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Shared space found: " + path);
                }
            }
        } catch (Exception e) {
            logger.warn("Error reading shared definition for " + path, e);
        }
    }

    @Override
    public void setPermission(final XMLNode node) throws ExportationException {
        final String path = ElementsHelper.getExportationPath(node);
        if (path == null) {
            return;
        }
        try {
            final NodeRef ref = new NodeRef(path);
            if (!nodeService.exists(ref)) {
                return;
            }
            final Set<AccessPermission> permissions = permissionService.getAllSetPermissions(ref);
            if (logger.isDebugEnabled()) {
                logger.debug("Permissions for " + path + ": " + (permissions != null ? permissions.size() : 0));
            }
        } catch (Exception e) {
            logger.warn("Error reading permissions for " + path, e);
        }
    }

    @Override
    public void setNotification(final XMLNode node) throws ExportationException {
        // Read notification subscriptions from CIRCABC's notification subscription service
        final String path = ElementsHelper.getExportationPath(node);
        if (path == null) {
            return;
        }
        try {
            final NodeRef ref = new NodeRef(path);
            if (!nodeService.exists(ref)) {
                return;
            }
            final NotificationSubscriptionService notifService = notificationSubscriptionService;
            if (notifService == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("NotificationSubscriptionService not available - skipping notification export for " + path);
                }
                return;
            }
            final Set<AuthorityNotification> notifications = notifService.getNotifications(ref);
            if (notifications != null && !notifications.isEmpty()) {
                final List<NotificationItem> items = new ArrayList<>();
                for (final AuthorityNotification notif : notifications) {
                    final eu.cec.digit.circabc.migration.entities.generated.permissions.NotificationStatus xmlStatus;
                    switch (notif.getNotificationStatus()) {
                        case SUBSCRIBED:
                            xmlStatus = eu.cec.digit.circabc.migration.entities.generated.permissions.NotificationStatus.SUSCRIBE;
                            break;
                        case UNSUBSCRIBED:
                            xmlStatus = eu.cec.digit.circabc.migration.entities.generated.permissions.NotificationStatus.UNSUSCRIBE;
                            break;
                        default:
                            xmlStatus = eu.cec.digit.circabc.migration.entities.generated.permissions.NotificationStatus.DEFAULT;
                            break;
                    }
                    final NotificationItem item = new NotificationItem();
                    item.setStatus(xmlStatus);
                    if (notif.getAuthorityType() == AuthorityType.USER) {
                        item.setUser(notif.getAuthority());
                    } else {
                        item.setProfile(notif.getAuthority());
                    }
                    items.add(item);
                }
                if (!items.isEmpty()) {
                    final Notifications notifElement = new Notifications(items);
                    if (node instanceof eu.cec.digit.circabc.migration.entities.generated.nodes.Message) {
                        ((eu.cec.digit.circabc.migration.entities.generated.nodes.Message) node).setNotifications(notifElement);
                    } else if (node instanceof eu.cec.digit.circabc.migration.entities.generated.nodes.Topic) {
                        ((eu.cec.digit.circabc.migration.entities.generated.nodes.Topic) node).setNotifications(notifElement);
                    } else if (node instanceof eu.cec.digit.circabc.migration.entities.generated.nodes.Forum) {
                        ((eu.cec.digit.circabc.migration.entities.generated.nodes.Forum) node).setNotifications(notifElement);
                    } else if (node instanceof eu.cec.digit.circabc.migration.entities.generated.nodes.Space) {
                        ((eu.cec.digit.circabc.migration.entities.generated.nodes.Space) node).setNotifications(notifElement);
                    } else if (node instanceof eu.cec.digit.circabc.migration.entities.generated.nodes.Content) {
                        ((eu.cec.digit.circabc.migration.entities.generated.nodes.Content) node).setNotifications(notifElement);
                    } else if (node instanceof eu.cec.digit.circabc.migration.entities.generated.nodes.LibraryTranslation) {
                        ((eu.cec.digit.circabc.migration.entities.generated.nodes.LibraryTranslation) node).setNotifications(notifElement);
                    } else if (node instanceof eu.cec.digit.circabc.migration.entities.generated.nodes.Dossier) {
                        ((eu.cec.digit.circabc.migration.entities.generated.nodes.Dossier) node).setNotifications(notifElement);
                    } else if (node instanceof eu.cec.digit.circabc.migration.entities.generated.nodes.Url) {
                        ((eu.cec.digit.circabc.migration.entities.generated.nodes.Url) node).setNotifications(notifElement);
                    } else if (node instanceof eu.cec.digit.circabc.migration.entities.generated.nodes.Link) {
                        ((eu.cec.digit.circabc.migration.entities.generated.nodes.Link) node).setNotifications(notifElement);
                    } else if (node instanceof eu.cec.digit.circabc.migration.entities.generated.nodes.Library) {
                        ((eu.cec.digit.circabc.migration.entities.generated.nodes.Library) node).setNotifications(notifElement);
                    } else if (node instanceof eu.cec.digit.circabc.migration.entities.generated.nodes.Newsgroups) {
                        ((eu.cec.digit.circabc.migration.entities.generated.nodes.Newsgroups) node).setNotifications(notifElement);
                    } else if (node instanceof eu.cec.digit.circabc.migration.entities.generated.nodes.Information) {
                        ((eu.cec.digit.circabc.migration.entities.generated.nodes.Information) node).setNotifications(notifElement);
                    } else if (node instanceof eu.cec.digit.circabc.migration.entities.generated.nodes.InfContent) {
                        ((eu.cec.digit.circabc.migration.entities.generated.nodes.InfContent) node).setNotifications(notifElement);
                    } else if (node instanceof eu.cec.digit.circabc.migration.entities.generated.nodes.InfNews) {
                        ((eu.cec.digit.circabc.migration.entities.generated.nodes.InfNews) node).setNotifications(notifElement);
                    } else if (node instanceof eu.cec.digit.circabc.migration.entities.generated.nodes.InformationTranslation) {
                        ((eu.cec.digit.circabc.migration.entities.generated.nodes.InformationTranslation) node).setNotifications(notifElement);
                    } else if (node instanceof eu.cec.digit.circabc.migration.entities.generated.nodes.InterestGroup) {
                        ((eu.cec.digit.circabc.migration.entities.generated.nodes.InterestGroup) node).setNotifications(notifElement);
                    }
                }
            }
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Error reading notifications for " + path + ": " + e.getMessage());
            }
        }
    }

    @Override
    public Set<CategoryInterestGroupPair> getAllSharedLinkTarget(final CategoryInterestGroupPair pair) throws ExportationException {
        // No cross-IG shared space dependencies for CIRCABC-to-CIRCABC export
        return Collections.emptySet();
    }

    @Override
    public Set<CategoryInterestGroupPair> getAllImportedProfileTarget(final CategoryInterestGroupPair pair) throws ExportationException {
        // No cross-IG imported profile dependencies for CIRCABC-to-CIRCABC export
        return Collections.emptySet();
    }

    // --- Setters for Spring injection ---

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    public void setProfileManagerServiceFactory(ProfileManagerServiceFactory profileManagerServiceFactory) {
        this.profileManagerServiceFactory = profileManagerServiceFactory;
    }

    public void setManagementService(ManagementService managementService) {
        this.managementService = managementService;
    }

    public void setNotificationSubscriptionService(NotificationSubscriptionService notificationSubscriptionService) {
        this.notificationSubscriptionService = notificationSubscriptionService;
    }
}
