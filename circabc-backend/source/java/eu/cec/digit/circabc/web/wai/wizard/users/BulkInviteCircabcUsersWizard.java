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
/*
 *
 */
package eu.cec.digit.circabc.web.wai.wizard.users;

import eu.cec.digit.circabc.config.CircabcConfiguration;
import eu.cec.digit.circabc.repo.user.InvalidBulkImportFileFormatException;
import eu.cec.digit.circabc.service.profile.Profile;
import eu.cec.digit.circabc.service.profile.ProfileManagerService;
import eu.cec.digit.circabc.service.profile.permissions.DirectoryPermissions;
import eu.cec.digit.circabc.service.user.BulkImportUserData;
import eu.cec.digit.circabc.service.user.BulkUserImportService;
import eu.cec.digit.circabc.service.user.UserCategoryMembershipRecord;
import eu.cec.digit.circabc.service.user.UserIGMembershipRecord;
import eu.cec.digit.circabc.web.ui.common.component.UIActionLink;
import eu.cec.digit.circabc.web.wai.wizard.BaseWaiWizard;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.custom.fileupload.UploadedFile;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

/**
 * @author beaurpi
 */
public class BulkInviteCircabcUsersWizard extends BaseWaiWizard {


    public final static String BLANK_PROFILE_DETECTED = "bulk_invite_user_blank_profile_detected";
    final static Log logger = LogFactory.getLog(BulkInviteCircabcUsersWizard.class);
    /**
     *
     */
    private static final long serialVersionUID = 5162448811337537051L;
    private List<String> allowedFileTypes;
    private String selectedCategory;
    private String selectedProfile;
    private List<SelectItem> availableCategories;
    private List<SelectItem> availableInterestGroups;
    private List<String> selectedAvailableInterestGroups;
    private List<String> selectedChosedInterestGroups;
    private Map<String, List<SelectItem>> chosenGroups;
    private List<BulkImportUserData> model;
    private UploadedFile submittedFile;
    private List<UploadedFile> uploadedTemplates;
    private Boolean ecasDepartmentNumberEnabled;
    private BulkUserImportService bulkUserImportService;
    private Boolean createIgProfileHelper;
    private Boolean createDepartmentNumberProfileHelper;
    private List<String> profilesToBeCreated;
    private Map<String, String> igProfiles;
    private List<SelectItem> igProfilesAvailable;
    private Boolean selectedAllUsers;
    private Boolean notifyInvitations;

    @Override
    public String getPageIconAltText() {

        return translate("bulk_invite_circabc_user_desc");
    }

    @Override
    public String getBrowserTitle() {

        return translate("bulk_invite_circabc_user_on_circabc");
    }

    @Override
    public void init(Map<String, String> parameters) {

        super.init(parameters);

        setNotifyInvitations(false);

        model = new ArrayList<>();

        uploadedTemplates = new ArrayList<>();

        ecasDepartmentNumberEnabled = Boolean.valueOf(CircabcConfiguration
                .getProperty(CircabcConfiguration.BULK_USER_IMPORT_DEPARTMENT_NUMBER_STATUS));

        createIgProfileHelper = false;
        createDepartmentNumberProfileHelper = false;
        profilesToBeCreated = new ArrayList<>();

        setSelectedAllUsers(false);

        this.availableCategories = new ArrayList<>();

        selectedAvailableInterestGroups = new ArrayList<>();
        selectedChosedInterestGroups = new ArrayList<>();
        chosenGroups = new HashMap<>();

        allowedFileTypes = new ArrayList<>();
        allowedFileTypes.add("application/vnd.ms-excel");
        allowedFileTypes.add("application/vnd.excel");

        Set<String> knownCateg = new HashSet<>();

        for (UserCategoryMembershipRecord categoryAdminEntry : getUserService()
                .getCategories(this.getNavigator().getCurrentUser().getUserName())) {
            if (!knownCateg.contains(categoryAdminEntry.getCategoryNodeId())) {
                this.availableCategories.add(new SelectItem(categoryAdminEntry.getCategoryNodeId(),
                        categoryAdminEntry.getCategory()));
                knownCateg.add(categoryAdminEntry.getCategoryNodeId());
            }
        }

        for (UserIGMembershipRecord igEntry : getUserService()
                .getInterestGroups(this.getNavigator().getCurrentUser().getUserName())) {
            if (!knownCateg.contains(igEntry.getCategoryNodeId())) {
                this.availableCategories
                        .add(new SelectItem(igEntry.getCategoryNodeId(), igEntry.getCategoryTitle()));
                knownCateg.add(igEntry.getCategoryNodeId());
            }
        }

        refreshAvailableInterestGroups(null);

        igProfiles = new HashMap<>();

        for (Profile profile : bulkUserImportService
                .listGroupProfiles(this.getActionNode().getNodeRef())) {
            igProfiles.put(profile.getProfileDisplayName(), profile.getProfileName());
        }
    }

    public void refreshAvailableInterestGroups(ValueChangeEvent event) {

        Set<String> knownIg = new HashSet<>();
        List<NodeRef> categRefs = new ArrayList<>();
        this.availableInterestGroups = new ArrayList<>();
        Boolean toContinue = false;

        // means it is initialisation
        if (event == null) {
            categRefs.add(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
                    availableCategories.get(0).getValue().toString()));
            toContinue = true;
        } else if (event.getNewValue() != null) {
            categRefs.add(
                    new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, event.getNewValue().toString()));
            toContinue = true;
        }

        if (toContinue) {
            ProfileManagerService profileManagerService = getProfileManagerServiceFactory()
                    .getProfileManagerService(categRefs.get(0));

            String currentIgId = getActionNode().getNodeRef().getId();

            if (profileManagerService.getInvitedUsers(categRefs.get(0))
                    .contains(this.getNavigator().getCurrentAlfrescoUserName())) {
                for (NodeRef igEntry : getManagementService().getInterestGroups(categRefs.get(0))) {
                    if (!knownIg.contains(igEntry.getId()) && !igEntry.getId().equals(currentIgId)) {
                        this.availableInterestGroups.add(new SelectItem(igEntry.getId(),
                                getNodeService().getProperty(igEntry, ContentModel.PROP_TITLE).toString(),
                                categRefs.get(0).toString()));
                        knownIg.add(igEntry.getId());
                    }
                }
            } else {
                for (UserIGMembershipRecord igEntry : getUserService()
                        .getInterestGroups(this.getNavigator().getCurrentUser().getUserName(), categRefs)) {

                    if (getPermissionService().hasPermission(
                            new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
                                    igEntry.getInterestGroupNodeId()), DirectoryPermissions.DIRACCESS.toString())
                            == AccessStatus.ALLOWED) {
                        if (!knownIg.contains(igEntry.getInterestGroupNodeId()) && !igEntry
                                .getInterestGroupNodeId().equals(currentIgId)) {
                            this.availableInterestGroups.add(
                                    new SelectItem(igEntry.getInterestGroupNodeId(), igEntry.getInterestGroupTitle(),
                                            igEntry.getCategoryNodeId()));
                            knownIg.add(igEntry.getInterestGroupNodeId());

                        }
                    }
                }
            }
        }

    }

    /**
     * Add one Interest Group to the selected list
     */
    public void addSelection(ActionEvent event) {

        for (String item : selectedAvailableInterestGroups) {

            if (!item.equals(this.getActionNode().getNodeRef().getId())) {

                String categTitle = findCategorySelectItem(selectedCategory).getLabel();

                if (chosenGroups.containsKey(categTitle)) {
                    if (!chosenGroups.get(categTitle).contains(findIgSelectItem(item))) {
                        chosenGroups.get(categTitle).add(findIgSelectItem(item));
                    }
                } else {
                    List<SelectItem> tmp = new ArrayList<>();
                    tmp.add(findIgSelectItem(item));
                    chosenGroups.put(categTitle, tmp);
                }

                bulkUserImportService.addAll(model, bulkUserImportService
                        .listMembers(new NodeRef(this.getActionNode().getNodeRef().getStoreRef(), item),
                                createIgProfileHelper), this.getActionNode().getNodeRef());
            }
        }
    }

    /**
     * Remove on Ig from the selected list
     */
    public void removeSelection(ActionEvent event) {
        for (String item : selectedChosedInterestGroups) {
            for (Entry<String, List<SelectItem>> entry : chosenGroups.entrySet()) {
                for (Iterator<SelectItem> tmp = entry.getValue().iterator(); tmp.hasNext(); ) {
                    SelectItem s = tmp.next();

                    if (s.getValue().equals(item)) {
                        tmp.remove();

                        removeAll(model, item);

                        break;
                    }
                }
            }
        }
    }

    private void removeAll(List<BulkImportUserData> model2, String item) {

        Iterator<BulkImportUserData> i = model2.iterator();

        while (i.hasNext()) {
            BulkImportUserData tmpUser = i.next();

            if (tmpUser.getIgRef() != null) {
                if (tmpUser.getIgRef().getId().equals(item)) {
                    i.remove();
                }
            }

            if (tmpUser.getDepartmentNumber() != null) {
                if (tmpUser.getDepartmentNumber().equals(item)) {
                    i.remove();
                }
            }

        }

    }

    private SelectItem findCategorySelectItem(String item) {

        SelectItem categ = null;

        for (SelectItem sItem : availableCategories) {
            if (item.equals(sItem.getValue())) {
                categ = sItem;
            }
        }

        return categ;
    }

    /**
     * to retrieve on Interest group in the available list
     */
    private SelectItem findIgSelectItem(String item) {
        SelectItem ig = null;

        for (SelectItem sItem : availableInterestGroups) {
            if (item.equals(sItem.getValue())) {
                ig = sItem;
            }
        }

        return ig;
    }

    @Override
    protected String finishImpl(FacesContext context, final String outcome)
            throws Throwable {

        if (!containsProfileBlankName()) {
            bulkUserImportService
                    .inviteUsers(model, getActionNode().getNodeRef(), igProfiles, notifyInvitations);

            getActionNode().reset();

            return outcome;
        } else {
            Utils.addStatusMessage(FacesMessage.SEVERITY_ERROR, translate(BLANK_PROFILE_DETECTED));
            isFinished = false;
            return null;
        }
    }

    private boolean containsProfileBlankName() {
        Boolean result = false;

        for (BulkImportUserData item : model) {
            if (item.getProfile() == null || item.getProfile().length() == 0) {
                result = true;
            }
        }

        return result;
    }

    public void uploadTemplate(ActionEvent event) {

        if (submittedFile != null) {
            if (allowedFileTypes.contains(submittedFile.getContentType()) || submittedFile.getName()
                    .matches(".*xls") || submittedFile.getName().contains(".*xlsx")) {
                try {

                    ByteArrayInputStream is = new ByteArrayInputStream(submittedFile.getBytes());
                    HSSFWorkbook wb = new HSSFWorkbook(is);

                    bulkUserImportService
                            .addAll(model, bulkUserImportService.loadWork(wb, submittedFile.getName()),
                                    this.getActionNode().getNodeRef());

                    uploadedTemplates.add(submittedFile);

                } catch (IOException e) {

                    Utils.addStatusMessage(FacesMessage.SEVERITY_ERROR,
                            translate("bulk_invite_error_durring_file_reading"));


                } catch (InvalidBulkImportFileFormatException e) {

                    Utils.addStatusMessage(FacesMessage.SEVERITY_ERROR,
                            translate("bulk_invite_error_file_content_format"));

                }
            } else {
                Utils.addStatusMessage(FacesMessage.SEVERITY_WARN,
                        translate("bulk_invite_unrecognised_file_type"));
            }
        }

    }

    public void removeTemplate(ActionEvent event) {
        UIActionLink uLink = (UIActionLink) event.getComponent();
        String fileName = uLink.getParameterMap().get("fileName");

        Iterator<BulkImportUserData> i = model.iterator();

        while (i.hasNext()) {
            BulkImportUserData tmp = i.next();

            if (tmp.getFromFile() != null) {
                if (tmp.getFromFile().equals(fileName)) {
                    i.remove();
                }
            }
        }

        Iterator<UploadedFile> s = uploadedTemplates.iterator();

        while (s.hasNext()) {
            UploadedFile tmp = s.next();

            if (tmp.getName().equals(fileName)) {
                s.remove();
            }
        }
    }

    public void saveWorkTemplate(ActionEvent event) {

        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
        HttpServletResponse response = (HttpServletResponse) externalContext.getResponse();

        response
                .reset(); // Some JSF component library or some Filter might have set some headers in the buffer beforehand. We want to get rid of them, else it may collide.
        response.setContentType(allowedFileTypes.get(
                0)); // Check http://www.w3schools.com/media/media_mimeref.asp for all types. Use if necessary ServletContext#getMimeType() for auto-detection based on filename.
        response.setHeader("Content-disposition",
                "attachment; filename=\"bulkUserImportWorkingCopy.xls\""); // The Save As popup magic is done here. You can give it any filename you want, this only won't work in MSIE, it will use current request URL as filename instead.

        BufferedOutputStream output = null;

        try {

            output = new BufferedOutputStream(response.getOutputStream());

            bulkUserImportService.saveWork(model).write(output);

            output.close();

        } catch (IOException e) {

            Utils.addStatusMessage(FacesMessage.SEVERITY_ERROR,
                    translate("bulk_invite_generating_file_error"));

        }

        facesContext.responseComplete();

    }

    @Override
    public String next() {

        bulkUserImportService
                .parseProfilesToBeCreated(model, createIgProfileHelper, createDepartmentNumberProfileHelper,
                        profilesToBeCreated);

        initProfilesSelectItems();

        return super.next();
    }

    /**
     *
     */
    public void refreshIgProfileHelper(ValueChangeEvent event) {

        createIgProfileHelper = Boolean.valueOf(event.getNewValue().toString());

        bulkUserImportService
                .parseProfilesToBeCreated(model, createIgProfileHelper, createDepartmentNumberProfileHelper,
                        profilesToBeCreated);

        initProfilesSelectItems();

    }

    /**
     *
     */
    public void refreshDepartmentNumberProfileHelper(ValueChangeEvent event) {

        createDepartmentNumberProfileHelper = Boolean.valueOf(event.getNewValue().toString());

        bulkUserImportService
                .parseProfilesToBeCreated(model, createIgProfileHelper, createDepartmentNumberProfileHelper,
                        profilesToBeCreated);

        initProfilesSelectItems();

    }

    public void updateProfilesForSelectedUsers(ActionEvent event) {

        for (BulkImportUserData tmpUser : model) {
            if (tmpUser.getSelected()) {
                tmpUser.setProfile(selectedProfile);
            }
        }
    }

    public void removeSelectedUsers(ActionEvent event) {
        Iterator<BulkImportUserData> i = model.iterator();

        while (i.hasNext()) {
            BulkImportUserData tmpUser = i.next();
            if (tmpUser.getSelected()) {
                i.remove();
            }
        }
    }

    public Integer getUploadedTemplatesSize() {
        return uploadedTemplates.size();
    }

    public Boolean getEcasDepartmentNumberEnabled() {
        return this.ecasDepartmentNumberEnabled;
    }

    /**
     * @param ecasDepartmentNumberEnabled the ecasDepartmentNumberEnabled to set
     */
    public void setEcasDepartmentNumberEnabled(
            Boolean ecasDepartmentNumberEnabled) {
        this.ecasDepartmentNumberEnabled = ecasDepartmentNumberEnabled;
    }

    /**
     * @return the selectedCategory
     */
    public String getSelectedCategory() {
        return selectedCategory;
    }

    /**
     * @param selectedCategory the selectedCategory to set
     */
    public void setSelectedCategory(String selectedCategory) {
        this.selectedCategory = selectedCategory;
    }

    /**
     * @return the availableCategories
     */
    public List<SelectItem> getAvailableCategories() {
        return availableCategories;
    }

    /**
     * @param availableCategories the availableCategories to set
     */
    public void setAvailableCategories(List<SelectItem> availableCategories) {
        this.availableCategories = availableCategories;
    }

    /**
     * @return the availableInterestGroups
     */
    public List<SelectItem> getAvailableInterestGroups() {
        return availableInterestGroups;
    }

    /**
     * @param availableInterestGroups the availableInterestGroups to set
     */
    public void setAvailableInterestGroups(List<SelectItem> availableInterestGroups) {
        this.availableInterestGroups = availableInterestGroups;
    }

    /**
     * @return the selectedInterestGroups
     */
    public List<String> getSelectedAvailableInterestGroups() {
        return selectedAvailableInterestGroups;
    }

    /**
     * @param selectedAvailableInterestGroups the selectedAvailableInterestGroups to set
     */
    public void setSelectedAvailableInterestGroups(List<String> selectedAvailableInterestGroups) {
        this.selectedAvailableInterestGroups = selectedAvailableInterestGroups;
    }

    /**
     * @return the chosenGroups
     */
    public Map<String, List<SelectItem>> getChosenGroups() {
        return chosenGroups;
    }

    /**
     * @param chosenGroups the chosenGroups to set
     */
    public void setChosenGroups(Map<String, List<SelectItem>> chosenGroups) {
        this.chosenGroups = chosenGroups;
    }

    /**
     * @return the chosenGroups
     */
    public List<SelectItem> getChosenConvertedGroups() {

        List<SelectItem> result = new ArrayList<>();

        for (String key : getChosenGroups().keySet()) {
            for (SelectItem chosenGroup : getChosenGroups().get(key)) {
                result.add(new SelectItem(chosenGroup.getValue(), key + " / " +
                        chosenGroup.getLabel()));
            }
        }

        return result;
    }

    /**
     * @return the selectedChosedInterestGroups
     */
    public List<String> getSelectedChosedInterestGroups() {
        return selectedChosedInterestGroups;
    }

    /**
     * @param selectedChosedInterestGroups the selectedChosedInterestGroups to set
     */
    public void setSelectedChosedInterestGroups(
            List<String> selectedChosedInterestGroups) {
        this.selectedChosedInterestGroups = selectedChosedInterestGroups;
    }

    /**
     * @return the submittedFile
     */
    public UploadedFile getSubmittedFile() {
        return submittedFile;
    }

    /**
     * @param submittedFile the uploadedTemplate to set
     */
    public void setSubmittedFile(UploadedFile submittedFile) {
        this.submittedFile = submittedFile;
    }

    /**
     * @return the createProfileHelper
     */
    public Boolean getCreateIgProfileHelper() {
        return createIgProfileHelper;
    }

    /**
     * @param createIgProfileHelper the createIgProfileHelper to set
     */
    public void setCreateIgProfileHelper(Boolean createIgProfileHelper) {
        this.createIgProfileHelper = createIgProfileHelper;
    }

    /**
     * @return the bulkuserImportService
     */
    public BulkUserImportService getBulkUserImportService() {
        return bulkUserImportService;
    }

    /**
     * @param bulkUserImportService the bulkUserImportService to set
     */
    public void setBulkUserImportService(BulkUserImportService bulkUserImportService) {
        this.bulkUserImportService = bulkUserImportService;
    }

    /**
     * @return the model
     */
    public List<BulkImportUserData> getModel() {
        return model;
    }

    /**
     * @param model the model to set
     */
    public void setModel(List<BulkImportUserData> model) {
        //this.model = model;
    }

    /**
     * @return the uploadedTemplates
     */
    public List<UploadedFile> getUploadedTemplates() {
        return uploadedTemplates;
    }

    /**
     * @param uploadedTemplates the uploadedTemplates to set
     */
    public void setUploadedTemplates(List<UploadedFile> uploadedTemplates) {
        this.uploadedTemplates = uploadedTemplates;
    }

    /**
     * @return the createDepartmentNumberProfileHelper
     */
    public Boolean getCreateDepartmentNumberProfileHelper() {
        return createDepartmentNumberProfileHelper;
    }

    /**
     * @param createDepartmentNumberProfileHelper the createDepartmentNumberProfileHelper to set
     */
    public void setCreateDepartmentNumberProfileHelper(
            Boolean createDepartmentNumberProfileHelper) {
        this.createDepartmentNumberProfileHelper = createDepartmentNumberProfileHelper;
    }

    /**
     * @return the profilesToBeCreated
     */
    public List<String> getProfilesToBeCreated() {
        return profilesToBeCreated;
    }

    /**
     * @param profilesToBeCreated the profilesToBeCreated to set
     */
    public void setProfilesToBeCreated(List<String> profilesToBeCreated) {
        this.profilesToBeCreated = profilesToBeCreated;
    }

    /**
     * @return the igProfilesAvailable
     */
    public List<SelectItem> getIgProfilesAvailable() {

        if (igProfilesAvailable == null) {
            initProfilesSelectItems();
        }

        return igProfilesAvailable;
    }

    /**
     * @param igProfilesAvailable the igProfilesAvailable to set
     */
    public void setIgProfilesAvailable(List<SelectItem> igProfilesAvailable) {
        this.igProfilesAvailable = igProfilesAvailable;
    }

    /**
     *
     */
    private void initProfilesSelectItems() {

        igProfilesAvailable = new ArrayList<>();

        for (String profile : igProfiles.keySet()) {
            // ignore guest,registered and imported profiles
            if (!(profile.equalsIgnoreCase("Guest") || profile.equalsIgnoreCase("Registered") || profile
                    .contains(":"))) {
                igProfilesAvailable.add(new SelectItem(profile, profile));
            }
        }

        for (String profile : profilesToBeCreated) {
            igProfilesAvailable.add(new SelectItem(profile, profile));
        }
    }

    /**
     * when user is removed from the list
     */
    public void removeUserSelection(ActionEvent event) {
        UIActionLink uLink = (UIActionLink) event.getComponent();

        String mail = "";
        if (uLink.getParameterMap().get("mail") != null) {
            mail = uLink.getParameterMap().get("mail");
        }

        String username = "";
        if (uLink.getParameterMap().get("username") != null) {
            username = uLink.getParameterMap().get("username");
        }

        Iterator<BulkImportUserData> i = model.iterator();

        while (i.hasNext()) {
            BulkImportUserData tmpUser = i.next();
            if (tmpUser.getUser() != null) {
                if (tmpUser.getUser().getEmail().equalsIgnoreCase(mail)) {
                    i.remove();
                    break;
                }
            } else {
                if (tmpUser.getExpectedUsername().equalsIgnoreCase(username)) {
                    i.remove();
                    break;
                }
            }
        }
    }

    public void selectUser(ActionEvent event) {
        UIActionLink uLink = (UIActionLink) event.getComponent();

        String mail = "";
        if (uLink.getParameterMap().get("mail") != null) {
            mail = uLink.getParameterMap().get("mail");
        }

        Boolean all = false;

        if (uLink.getParameterMap().get("all") != null) {
            all = Boolean.valueOf(uLink.getParameterMap().get("all"));
            selectedAllUsers = true;
        }

        for (BulkImportUserData tmpUser : model) {
            if (all) {
                tmpUser.setSelected(true);

            } else if (tmpUser.getUser() != null) {
                if (tmpUser.getUser().getEmail().equalsIgnoreCase(mail)) {
                    tmpUser.setSelected(true);
                    break;
                }
            }


        }
    }

    public void unselectUser(ActionEvent event) {
        UIActionLink uLink = (UIActionLink) event.getComponent();

        String mail = "";
        if (uLink.getParameterMap().get("mail") != null) {
            mail = uLink.getParameterMap().get("mail");
        }

        Boolean all = false;

        if (uLink.getParameterMap().get("all") != null) {
            all = Boolean.valueOf(uLink.getParameterMap().get("all"));
            selectedAllUsers = false;
        }

        for (BulkImportUserData tmpUser : model) {
            if (all) {
                tmpUser.setSelected(false);

            } else if (tmpUser.getUser() != null) {
                if (tmpUser.getUser().getEmail().equalsIgnoreCase(mail)) {
                    tmpUser.setSelected(false);
                    break;
                }
            }

        }
    }

    /**
     * @return the seletedProfile
     */
    public String getSelectedProfile() {
        return selectedProfile;
    }

    /**
     * @param selectedProfile the selectedProfile to set
     */
    public void setSelectedProfile(String selectedProfile) {
        this.selectedProfile = selectedProfile;
    }

    /**
     * @return the selectedAllUsers
     */
    public Boolean getSelectedAllUsers() {
        return selectedAllUsers;
    }

    /**
     * @param selectedAllUsers the selectedAllUsers to set
     */
    public void setSelectedAllUsers(Boolean selectedAllUsers) {
        this.selectedAllUsers = selectedAllUsers;
    }

    /**
     * @return the notifyInvitations
     */
    public Boolean getNotifyInvitations() {
        return notifyInvitations;
    }

    /**
     * @param notifyInvitations the notifyInvitations to set
     */
    public void setNotifyInvitations(Boolean notifyInvitations) {
        this.notifyInvitations = notifyInvitations;
    }

    public void downloadTemplate() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();

        response.setContentType("application/vnd.ms-excel;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=template.xls");

        ServletOutputStream outStream = null;
        try {

            outStream = response.getOutputStream();

            Workbook workbook = new HSSFWorkbook();

            Sheet sheet = workbook.createSheet("Members");

            Row titleRow = sheet.createRow(0);
            titleRow.createCell(0).setCellValue("username");
            titleRow.createCell(1).setCellValue("firstname");
            titleRow.createCell(2).setCellValue("lastname");
            titleRow.createCell(3).setCellValue("email");
            titleRow.createCell(4).setCellValue("profile");

            workbook.write(outStream);

            context.responseComplete();

        } catch (IOException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error during generating template", e);
            }
        } finally {
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (IOException ex) {
                    logger.error("Error closing stream", ex);
                }
            }
        }

    }
}
