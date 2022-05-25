package eu.cec.digit.circabc.web.wai.dialog.profile;

import eu.cec.digit.circabc.CircabcConfig;
import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.repo.app.model.Profile;
import eu.cec.digit.circabc.repo.app.model.UserWithProfile;
import eu.cec.digit.circabc.service.cmr.security.CircabcConstant;
import eu.cec.digit.circabc.web.PermissionUtils;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.web.ui.common.SortableSelectItem;
import org.alfresco.web.ui.repo.WebResources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBManageUserProfilesDialog extends ManageUserProfilesDialog {

    /**
     *
     */
    private static final long serialVersionUID = -7729043999927155879L;

    @Override
    public List<Map> getFilterUsers() {
        if (shouldOptimize()) {

            String alfrescoGroup = getSelectedProfile();
            final String searchText = (getSearchText() == null) ? ""
                    : getSearchText().toLowerCase();
            final NodeRef igNodeRef = getActionNode().getNodeRef();
            final String currentUserName = getNavigator().getCurrentUser()
                    .getUserName();
            final long localeID = getCircabcService().getUserLocaleID(
                    currentUserName);
            if (alfrescoGroup == null) {
                alfrescoGroup = VALUE_ALL_PROFILES;
            }
            if (alfrescoGroup.equals(VALUE_ALL_PROFILES)) {
                alfrescoGroup = "";
            } else if (alfrescoGroup.startsWith(PermissionService.GROUP_PREFIX)) {
                alfrescoGroup = alfrescoGroup.replace(
                        PermissionService.GROUP_PREFIX, "");
            }
            List<UserWithProfile> result = getCircabcService()
                    .getFilteredUsers(igNodeRef, localeID, alfrescoGroup,
                            searchText);
            final List<Map> personNodes = new ArrayList<>(result.size());

            int currentNodeAdminCount = getCircabcService().getNumberOfAdmintrators(igNodeRef);

            for (UserWithProfile userWithProfile : result) {
                final Map<String, Object> node = new HashMap<String, Object>();
                final String firstName = userWithProfile.getFirstName();
                final String lastName = userWithProfile.getLastName();
                final String email = userWithProfile.getEmail();

                node.put(PermissionUtils.KEY_USER_FULL_NAME, firstName + " "
                        + lastName);
                node.put("firstName", firstName);
                node.put("lastName", lastName);
                node.put(PermissionUtils.KEY_EMAIL, email);

                node.put(PermissionUtils.KEY_AUTHORITY,
                        userWithProfile.getUserName());
                node.put(PermissionUtils.KEY_DISPLAY_NAME,
                        userWithProfile.getUserName());
                if (CircabcConfig.OSS) {
                    node.put(PermissionUtils.KEY_USER_NAME,
                            userWithProfile.getUserName());
                } else {
                    node.put(PermissionUtils.KEY_USER_NAME,
                            userWithProfile.getEcasUserName());
                }

                node.put(PermissionUtils.KEY_USER_PROFILE,
                        userWithProfile.getProfileTitle());
                node.put(PermissionUtils.KEY_ICON, WebResources.IMAGE_PERSON);
                if (userWithProfile.isImported()) {
                    node.put("canBeRemoved", Boolean.FALSE);
                } else {
                    if (userWithProfile.getUserName().equals(currentUserName)) {
                        node.put("canBeRemoved", Boolean.FALSE);
                    } else {
                        // Check if this is last leader
                        if (currentNodeAdminCount == 1
                                && userWithProfile.isAdmin()) {
                            node.put("canBeRemoved", Boolean.FALSE);
                        } else {
                            node.put("canBeRemoved", Boolean.TRUE);
                        }
                    }
                }
                personNodes.add(node);
            }
            return personNodes;

        } else {
            return super.getFilterUsers();
        }
    }

    private boolean shouldOptimize() {
        return getActionNode().hasAspect(CircabcModel.ASPECT_IGROOT)
                && getCircabcService().readFromDatabase();
    }

    @Override
    public List<Map> getFilteredUsers() {
        if (shouldOptimize()) {
            setCachedUsers(this.getFilterUsers());
            return getCachedUsers();
        } else {
            return super.getFilteredUsers();
        }

    }

    @Override
    public List<SortableSelectItem> getProfiles() {
        if (shouldOptimize()) {
            final String allProfileText = translate(ManageUserProfilesDialog.MSG_ALL_PROFILE);
            final List<SortableSelectItem> profiles = new ArrayList<>();
            profiles.add(new SortableSelectItem(
                    ManageUserProfilesDialog.VALUE_ALL_PROFILES, "<"
                    + allProfileText + ">", allProfileText));
            final String currentUserName = getNavigator().getCurrentUser()
                    .getUserName();
            final long localeID = getCircabcService().getUserLocaleID(
                    currentUserName);
            final NodeRef igNodeRef = getActionNode().getNodeRef();
            List<Profile> result = getCircabcService()
                    .getProfilesForIg(igNodeRef, localeID, null);
            for (Profile profile : result) {
                if (profile.getAlfrescoGroup().equals("guest") || profile.getAlfrescoGroup()
                        .equals("EVERYONE")) {
                    continue;
                }
                String title = profile.getTitle();
                if (profile.isImported()) {
                    title = Profile.replaceLast(title, "_", ":");
                }
                profiles.add(
                        new SortableSelectItem(PermissionService.GROUP_PREFIX + profile.getAlfrescoGroup(),
                                title, title));
            }

            return profiles;

        } else {
            return super.getProfiles();
        }

    }

    @Override
    public List<SortableSelectItem> getAssignableProfileList() {
        if (shouldOptimize()) {
            final List<SortableSelectItem> profiles = new ArrayList<>();
            final String currentUserName = getNavigator().getCurrentUser()
                    .getUserName();
            final long localeID = getCircabcService().getUserLocaleID(
                    currentUserName);
            final NodeRef igNodeRef = getActionNode().getNodeRef();
            List<Profile> result = getCircabcService()
                    .getProfilesForIg(igNodeRef, localeID, null);
            for (Profile profile : result) {
                if (profile.isImported()) {
                    continue;
                }
                if (profile.getAlfrescoGroup().equals(CircabcConstant.GUEST_AUTHORITY) || profile
                        .getAlfrescoGroup().equals(CircabcConstant.REGISTERED_AUTHORITY)) {
                    continue;
                }

                profiles.add(
                        new SortableSelectItem(PermissionService.GROUP_PREFIX + profile.getAlfrescoGroup(),
                                profile.getTitle(), profile.getTitle()));
            }
            return profiles;

        } else {
            return super.getAssignableProfileList();
        }
    }


}
