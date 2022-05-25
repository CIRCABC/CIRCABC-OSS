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
package eu.cec.digit.circabc.web.wai.dialog.profile;

import eu.cec.digit.circabc.service.app.CircabcService;
import eu.cec.digit.circabc.service.cmr.security.CircabcConstant;
import eu.cec.digit.circabc.service.profile.*;
import eu.cec.digit.circabc.service.profile.permissions.*;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNodeType;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import org.alfresco.service.cmr.ml.ContentFilterLanguagesService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIInput;
import javax.faces.component.UISelectOne;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;
import java.text.MessageFormat;
import java.util.*;
import java.util.Map.Entry;

/**
 * Baked bean for access profiles edition
 *
 * @author Yanick Pignot
 */
public class EditAccessProfileDialog extends BaseWaiDialog {

    private static final String COMPONENT_EDIT_PROFILE_LANGUAGE = "edit-profile-language";
    private static final String COMPONENT_EDIT_PROFILE_VALUE = "edit-profile-value";
    private static final String PARAM_PROFILE_NAME = "profileName";

    private static final long serialVersionUID = -4789363975315953605L;

    private static final Log logger = LogFactory.getLog(EditAccessProfileDialog.class);

    private static final String MSG_CONTAINER_TITLE = "edit_access_profile_dialog_page_title";

    private static final String MSG_ERROR_TRANSLATION_REQUIRED = "edit_access_profile_dialog_error_no_translation";
    private static final String MSG_ERROR_LOCALE_REQUIRED = "edit_access_profile_dialog_error_locale_required";
    private static final String MSG_ERROR_VALUE_REQUIRED = "edit_access_profile_dialog_error_value_required";
    private static final String MSG_ERROR_LOCALE_DUPLICATED = "edit_access_profile_dialog_error_locale_duplicated";
    private static final String MSG_ERROR_CURRENT_PROFILE = "edit_access_profile_dialog_warn_edit_current_profile";


    /**
     * datamodel for table of translations for users
     */
    private transient DataModel translationDataModel = null;
    private transient DataModel permissionDataModel = null;

    private AccessProfileWrapper selectedProfile;
    private String value;
    private String language;
    private boolean editPorfileOfCurrentUser;
    private String oldProfileName;
    private String profileName = "";

    private transient ContentFilterLanguagesService contentFilterLanguagesService;

    private CircabcService circabcService;

    @Override
    public void init(final Map<String, String> params) {
        super.init(params);
        restored();

        if (params != null) {
            selectedProfile = null;
            this.editPorfileOfCurrentUser = false;

            final NodeRef igNodeRef = getActionNode().getNodeRef();
            final String profileName = params.get(PARAM_PROFILE_NAME);
            oldProfileName = profileName;
            if (oldProfileName != null) {
                if (oldProfileName.equals(CircabcConstant.GUEST_AUTHORITY)) {
                    logRecord.setActivity("Change permissions on public");
                } else if (oldProfileName
                        .equals(CircabcRootProfileManagerService.ALL_CIRCA_USERS_PROFILE_NAME)) {
                    logRecord.setActivity("Change permissions on registered");
                }
            }
            if (profileName != null) {
                // edition mode
                final ProfileManagerService profService = getProfileManagerServiceFactory()
                        .getIGRootProfileManagerService();
                final String currentUserName = getNavigator().getCurrentUser().getUserName();
                final String currentUserProfile = profService.getPersonProfile(igNodeRef, currentUserName);
                selectedProfile = new AccessProfileWrapper(
                        profService.getProfileNoCache(igNodeRef, profileName), igNodeRef);
                if (selectedProfile.getName().equals(currentUserProfile)) {
                    this.editPorfileOfCurrentUser = true;

                    Utils.addStatusMessage(FacesMessage.SEVERITY_INFO, translate(MSG_ERROR_CURRENT_PROFILE));
                }
            } else {
                // creation mode
                selectedProfile = new AccessProfileWrapper(igNodeRef);
            }
        }

        if (getActionNode() == null) {
            throw new IllegalArgumentException("The node id is a mandatory parameter");
        } else if (NavigableNodeType.IG_ROOT.isNodeFromType(getActionNode()) == false) {
            throw new IllegalArgumentException("This page is accessible only for an interest group");
        }

        if (getActionNode().hasPermission(DirectoryPermissions.DIRADMIN.toString()) == false) {
            Utils.addErrorMessage(translate(ManageAccessProfilesDialog.MSG_NO_LONGER_PERM));
        }
    }

    @Override
    public void restored() {
        this.translationDataModel = null;
        this.permissionDataModel = null;
        this.value = null;
        this.language = null;
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Throwable {
        // at least one translation must be defined
        if (getTranslationDataModel().getRowCount() < 1) {
            Utils.addErrorMessage(translate(MSG_ERROR_TRANSLATION_REQUIRED));

            if (logger.isDebugEnabled()) {
                logger.debug("Impossible to add a title to the Profile " + selectedProfile.getName()
                        + " because the list of translation is empty.");
            }

            return null;
        }

        long startTime = 0;

        if (logger.isDebugEnabled()) {
            startTime = System.currentTimeMillis();
        }

        // apply user defined permissions
        applyNewPermissions();

        if (logger.isDebugEnabled()) {
            logger
                    .debug("Trying to add update / create  the Profile " + selectedProfile.getName() + "...");
        }

        final ProfileManagerService profService = getProfileManagerServiceFactory()
                .getIGRootProfileManagerService();
        final Profile webProf = this.selectedProfile.toProfile();
        final NodeRef igNodeRef = getActionNode().getNodeRef();

        // we are in the create mode
        if (selectedProfile.getAutority() == null) {
            // create the profile
            createProfile(profService, webProf, igNodeRef);
        }

        final HashMap<String, Set<String>> servicesPermissions = webProf.getServicesPermissions();

        final String info;
        if (oldProfileName == null) {
            profileName = webProf.getProfileName();
            info = MessageFormat.format("Profile {0} permissions : {1}", webProf.getProfileName(),
                    servicesPermissions.toString());
        } else {
            profileName = oldProfileName;
            info = MessageFormat
                    .format("Profile {0} permissions : {1}", oldProfileName, servicesPermissions.toString());
        }
        logRecord.setInfo(info);

        // update the profile at the IG Root level and at ig services level
        profService.updateProfile(igNodeRef, webProf.getProfileName(), webProf.getServicesPermissions(),
                false);
        profService.updateProfileMap(igNodeRef, webProf);

        if (logger.isDebugEnabled()) {
            logger
                    .debug("Profile  successfully updated for the Interest Group " + getActionNode().getName()
                            + "\n	ig: " + igNodeRef
                            + "\n	profile: " + selectedProfile.getName());

            logger.debug(
                    "Time to create/update profile: " + (System.currentTimeMillis() - startTime) + "ms");
        }

        // reset permission cache on the action node
        getActionNode().reset();
        // reset permission cache on all navigation node
        getNavigator().updateCircabcNavigationContext();

        return outcome;
    }


    /**
     * @param profService
     * @param webProf
     * @param igNodeRef
     * @throws ProfileException
     */
    private void createProfile(final ProfileManagerService profService, final Profile webProf,
                               final NodeRef igNodeRef) throws ProfileException {
        final Map<String, Profile> existingProfile = profService.getProfileMap(igNodeRef);

        StringBuilder name = new StringBuilder(webProf.getProfileName());

        if (existingProfile.containsKey(name.toString())) {
            for (int x = 0; ; x++) {
                name.append(x);
                if (!existingProfile.containsKey(name.toString())) {
                    break;
                }
            }
        }

        profService.addProfile(igNodeRef, name.toString(), webProf.getServicesPermissions());

        final Profile createdProfile = profService.getProfile(igNodeRef, name.toString());
        webProf.setProfileName(createdProfile.getProfileName());
        webProf.setAlfrescoGroupName(createdProfile.getAlfrescoGroupName());
        webProf.setPrefixedAlfrescoGroupName(createdProfile.getPrefixedAlfrescoGroupName());

        if (logger.isDebugEnabled()) {
            logger
                    .debug("Profile successfully created for the Interest Group " + selectedProfile.getName()
                            + "\n	ig: " + igNodeRef
                            + "\n	profile: " + name);
        }
    }

    /**
     * @throws IllegalStateException
     */
    @SuppressWarnings("unchecked")
    private void applyNewPermissions() throws IllegalStateException {
        final List<ServicePermissionWrapper> wrappers = (List<ServicePermissionWrapper>) this.permissionDataModel
                .getWrappedData();

        for (final ServicePermissionWrapper wrapper : wrappers) {
            final String permissionValue = wrapper.getPermissionValue();

            switch (wrapper.getService()) {
                case INFORMATION:
                    this.selectedProfile.setInfPermission(permissionValue);
                    break;
                case LIBRARY:
                    this.selectedProfile.setLibPermission(permissionValue);
                    break;
                case DIRECTORY:
                    this.selectedProfile.setDirPermission(permissionValue);
                    break;
                case EVENT:
                    this.selectedProfile.setEvePermission(permissionValue);
                    break;
                case NEWSGROUP:
                    this.selectedProfile.setNewPermission(permissionValue);
                    break;
                default:
                    throw new IllegalStateException("IG Service " + wrapper.getService() + " unknow");
            }
        }
    }

    public boolean isTitleEditable() {
        return this.selectedProfile.isImported() == false;
    }

    @Override
    public String getContainerTitle() {
        return translate(MSG_CONTAINER_TITLE, selectedProfile.getDisplayTitle());
    }

    public void addSelection(ActionEvent event) {
        final UIInput input = (UIInput) event.getComponent()
                .findComponent(COMPONENT_EDIT_PROFILE_VALUE);
        final UISelectOne select = (UISelectOne) event.getComponent()
                .findComponent(COMPONENT_EDIT_PROFILE_LANGUAGE);

        final String selLang = (String) select.getValue();
        final String selValue = ((String) input.getValue()).trim();

        boolean error = false;

        if (selValue.length() < 1) {
            Utils.addErrorMessage(translate(MSG_ERROR_VALUE_REQUIRED));
            error = true;
        }

        if (selLang.length() < 1) {
            Utils.addErrorMessage(translate(MSG_ERROR_LOCALE_REQUIRED));
            error = true;
        }

        for (Map.Entry<Locale, String> entry : selectedProfile.getTitles().entrySet()) {
            if (selLang.equalsIgnoreCase(entry.getKey().toString())) {
                Utils.addErrorMessage(translate(MSG_ERROR_LOCALE_DUPLICATED));
                error = true;
                break;
            }
        }

        if (!error) {
            selectedProfile.withTitle(new Locale(selLang), selValue);
            this.value = null;
        }
    }

    /**
     * Method calls by the dialog to get the available langages.
     */
    public SelectItem[] getLanguages() {
        // get the list of filter languages
        final List<String> languages = getContentFilterLanguagesService().getFilterLanguages();

        final List<String> filteredLanguages = new ArrayList<>(languages.size());
        filteredLanguages.addAll(languages);

        for (Map.Entry<Locale, String> entry : selectedProfile.getTitles().entrySet()) {
            filteredLanguages.remove(entry.getKey().getLanguage());
        }

        // set the item selection list
        final SelectItem[] items = new SelectItem[filteredLanguages.size()];
        int idx = 0;

        for (final String lang : filteredLanguages) {
            final String label = getContentFilterLanguagesService().getLabelByCode(lang);
            items[idx] = new SelectItem(lang, label);
            idx++;
        }

        return items;
    }

    /**
     * Returns the properties for current keywords translations JSF DataModel
     *
     * @return JSF DataModel representing the translation of the keyword
     */
    public DataModel getTranslationDataModel() {
        if (this.translationDataModel == null) {
            this.translationDataModel = new ListDataModel();
        }
        final Set<Entry<Locale, String>> entrySet = selectedProfile.getTitles().entrySet();
        final List<Entry<Locale, String>> entryList = new ArrayList<>(entrySet.size());
        entryList.addAll(entrySet);
        this.translationDataModel.setWrappedData(entryList);

        return this.translationDataModel;
    }

    public DataModel getPermissionDataModel() {
        if (this.permissionDataModel == null) {
            this.permissionDataModel = new ListDataModel();

            final List<ServicePermissionWrapper> wrappers = new ArrayList<>(5);

            final String profileName = selectedProfile.getName();
            boolean isSpecial = profileName != null &&
                    (profileName.equals(CircabcRootProfileManagerService.ALL_CIRCA_USERS_PROFILE_NAME)
                            || profileName.equals(CircabcConstant.GUEST_AUTHORITY));

            final InformationPermissions[] infPerm =
                    (isSpecial) ? InformationPermissions.minimalValues() : InformationPermissions.values();
            final LibraryPermissions[] libPerm =
                    (isSpecial) ? LibraryPermissions.minimalValues() : LibraryPermissions.values();
            final DirectoryPermissions[] dirPerm =
                    (isSpecial) ? DirectoryPermissions.minimalValues() : DirectoryPermissions.values();
            final EventPermissions[] evePerm =
                    (isSpecial) ? EventPermissions.minimalValues() : EventPermissions.values();
            final NewsGroupPermissions[] newPerm =
                    (isSpecial) ? NewsGroupPermissions.minimalValues() : NewsGroupPermissions.values();

            wrappers.add(new ServicePermissionWrapper(CircabcServices.INFORMATION,
                    this.selectedProfile.getInfPermissionValue(), infPerm));
            wrappers.add(new ServicePermissionWrapper(CircabcServices.LIBRARY,
                    this.selectedProfile.getLibPermissionValue(), libPerm));
            wrappers.add(new ServicePermissionWrapper(CircabcServices.DIRECTORY,
                    this.selectedProfile.getDirPermissionValue(), dirPerm, editPorfileOfCurrentUser));
            wrappers.add(new ServicePermissionWrapper(CircabcServices.EVENT,
                    this.selectedProfile.getEvePermissionValue(), evePerm));
            wrappers.add(new ServicePermissionWrapper(CircabcServices.NEWSGROUP,
                    this.selectedProfile.getNewPermissionValue(), newPerm));

            this.permissionDataModel.setWrappedData(wrappers);
        }

        return this.permissionDataModel;
    }

    protected String doPostCommitProcessing(FacesContext context, String outcome) {
        super.doPostCommitProcessing(context, outcome);
        if (getCircabcService().syncEnabled()) {
            getCircabcService().updateProfileTitles(super.actionNodeDatabaseID, profileName);
        }
        return outcome;
    }

    /**
     * Action handler called when the Remove button is pressed to remove a keyword translation
     */
    @SuppressWarnings("unchecked")
    public void removeSelection(ActionEvent event) {
        final Map.Entry<Locale, String> wrapper = (Map.Entry<Locale, String>) this.translationDataModel
                .getRowData();
        if (wrapper != null) {
            this.selectedProfile.getTitles().remove(wrapper.getKey());
        }
    }

    public String getBrowserTitle() {
        return translate("edit_access_profile_dialog_browser_title");
    }

    public String getPageIconAltText() {
        return translate("edit_access_profile_dialog_icon_tooltip");
    }

    /**
     * @return the value
     */
    public final String getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public final void setValue(String value) {
        this.value = value;
    }

    /**
     * @return the language
     */
    public final String getLanguage() {
        return language;
    }

    /**
     * @param language the language to set
     */
    public final void setLanguage(String language) {
        this.language = language;
    }

    /**
     * @return the contentFilterLanguagesService
     */
    protected final ContentFilterLanguagesService getContentFilterLanguagesService() {
        if (contentFilterLanguagesService == null) {
            contentFilterLanguagesService = Services
                    .getAlfrescoServiceRegistry(FacesContext.getCurrentInstance())
                    .getContentFilterLanguagesService();
        }
        return contentFilterLanguagesService;
    }

    /**
     * @param contentFilterLanguagesService the contentFilterLanguagesService to set
     */
    public final void setContentFilterLanguagesService(
            final ContentFilterLanguagesService contentFilterLanguagesService) {
        this.contentFilterLanguagesService = contentFilterLanguagesService;
    }

    public CircabcService getCircabcService() {
        if (circabcService == null) {
            circabcService = Services.getCircabcServiceRegistry(FacesContext.getCurrentInstance())
                    .getCircabcService();
        }
        return circabcService;
    }

    public void setCircabcService(CircabcService circabcService) {
        this.circabcService = circabcService;
    }
}

