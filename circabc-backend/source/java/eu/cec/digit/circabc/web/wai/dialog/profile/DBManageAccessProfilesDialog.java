package eu.cec.digit.circabc.web.wai.dialog.profile;

import eu.cec.digit.circabc.repo.app.model.ProfileWithUsersCount;
import eu.cec.digit.circabc.service.cmr.security.CircabcConstant;
import eu.cec.digit.circabc.web.comparator.CircabcProfileDisplayTitleSort;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.myfaces.component.html.ext.SortableModel;

import javax.faces.model.DataModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class DBManageAccessProfilesDialog extends ManageAccessProfilesDialog {

    /**
     *
     */
    private static final long serialVersionUID = -5568536028085117906L;

    @Override
    public DataModel getProfilesDataModel() {
        if (getCircabcService().readFromDatabase()) {
            if (this.profileDataModel == null) {
                initDataModelFromDB();
            }
        }
        return super.getProfilesDataModel();
    }

    @Override
    public DataModel getImportedProfilesDataModel() {
        if (getCircabcService().readFromDatabase()) {
            if (importedProfileDataModel == null) {
                initDataModelFromDB();
            }
        }
        return super.getImportedProfilesDataModel();
    }

    private void initDataModelFromDB() {

        NodeRef igNodeRef = getIgNodeRef();
        final String currentUserName = getNavigator().getCurrentUser()
                .getUserName();
        final long localeID = getCircabcService().getUserLocaleID(
                currentUserName);

        final List<ProfileWithUsersCount> profiles = getCircabcService().getProfiles(
                getIgNodeRef(), localeID);
        final List<AccessProfileWrapper> webProfiles = new ArrayList<>(
                profiles.size());
        final List<AccessProfileWrapper> importedProfiles = new ArrayList<>(
                profiles.size());

        String userProfile = getIGRootProfileManager().getPersonProfile(
                igNodeRef, currentUserName);

        final NodeRef category = getManagementService().getCurrentCategory(
                igNodeRef);
        Set<String> prefixedAlfrescoGroupNames = getCircabcService()
                .getCategoryAlfrescoGroupsExceptCurrentIG(category, igNodeRef);

        for (ProfileWithUsersCount profile : profiles) {
            final AccessProfileWrapper accessProfileWrapper = new AccessProfileWrapper(
                    profile, igNodeRef);
            if (profile.isImported()) {
                importedProfiles.add(accessProfileWrapper);
            } else {
                if (userProfile != null
                        && userProfile.equals(accessProfileWrapper.getName())) {
                    accessProfileWrapper.setEditActionAvailable(false);
                }
                if (accessProfileWrapper.isExported()
                        && !prefixedAlfrescoGroupNames
                        .contains(accessProfileWrapper.getAutority())) {
                    accessProfileWrapper.setUnexportedActionAvailable(true);
                }
                if (profile.getAlfrescoGroup().equals(
                        CircabcConstant.GUEST_AUTHORITY)) {
                    accessProfileWrapper.setDeleteActionAvailable(false);
                }
                if (profile.getAlfrescoGroup().equals(
                        CircabcConstant.REGISTERED_AUTHORITY)) {
                    accessProfileWrapper.setDeleteActionAvailable(false);
                }
                if (profile.getNumberOfUsers() > 0) {
                    accessProfileWrapper.setDeleteActionAvailable(false);
                }

                if (!profile.isVisible()) {
                    continue;
                }

                webProfiles.add(accessProfileWrapper);
            }

        }

        final String language = getUserPreferencesBean().getLanguage();
        Collections.sort(webProfiles, new CircabcProfileDisplayTitleSort(
                language));
        Collections.sort(importedProfiles, new CircabcProfileDisplayTitleSort(
                language));
        if (this.profileDataModel == null) {
            this.profileDataModel = new SortableModel();
        }
        if (this.importedProfileDataModel == null) {
            this.importedProfileDataModel = new SortableModel();
        }
        this.profileDataModel.setWrappedData(webProfiles);
        this.importedProfileDataModel.setWrappedData(importedProfiles);

    }
}
