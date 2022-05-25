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

import eu.cec.digit.circabc.CircabcConfig;
import eu.cec.digit.circabc.service.app.CircabcService;
import eu.cec.digit.circabc.service.profile.Profile;
import eu.cec.digit.circabc.service.profile.ProfileManagerService;
import eu.cec.digit.circabc.service.user.SearchResultRecord;
import eu.cec.digit.circabc.web.PermissionUtils;
import eu.cec.digit.circabc.web.ProfileUtils;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.WebClientHelper;
import eu.cec.digit.circabc.web.WebClientHelper.ExportTypeEnum;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.web.bean.repository.MapNode;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.SortableSelectItem;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.repo.WebResources;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;

/**
 * Bean that back the manage profiles dialog.
 *
 * @author Yanick Pignot
 */
public class ManageUserProfilesDialog extends BaseWaiDialog {

    protected static final String VALUE_ALL_PROFILES = "__ALL_PROFILES";
    protected static final String MSG_ALL_PROFILE = "members_home_disable_profile_filter";
    private static final long serialVersionUID = -2476739780479536405L;
    private static final String PARAM_SELECTED_PROFILE = "profileName";
    @SuppressWarnings("unused")
    private static final Log logger = LogFactory.getLog(ManageUserProfilesDialog.class);

    private transient PersonService personService;
    private transient AuthenticationService authenticationService;
    private transient AuthorityService authorityService;
    private transient CircabcService circabcService;

    private String selectedProfile;
    private String newProfile;
    private String searchText;
    private List<Map> cachedUsers = Collections.emptyList();

    private ExportTypeEnum exportType = ExportTypeEnum.CSV;

    private boolean isImportedSelectedProfile;


    @Override
    public void init(final Map<String, String> parameters) {
        super.init(parameters);

        if (parameters != null) {
            if (parameters.containsKey(PARAM_SELECTED_PROFILE)) {
                selectedProfile = parameters.get(PARAM_SELECTED_PROFILE);
            } else {
                selectedProfile = VALUE_ALL_PROFILES;
            }

            if (getActionNode() == null) {
                throw new IllegalArgumentException("The node id is a mandatory parameter");
            }
        }
    }

    @Override
    protected String finishImpl(final FacesContext context, final String outcome) throws Exception {
        return outcome;
    }

    public List<SortableSelectItem> getProfiles() {
        final String allProfileText = translate(MSG_ALL_PROFILE);
        final List<SortableSelectItem> profiles = new ArrayList<>();

        profiles.add(
                new SortableSelectItem(VALUE_ALL_PROFILES, "<" + allProfileText + ">", allProfileText));
        profiles.addAll(ProfileUtils.buildMailableProfileItems(getActionNode(), logger));
        return profiles;
    }

    public List<SortableSelectItem> getProfileList() {
        final List<SortableSelectItem> profiles = new ArrayList<>();
        profiles.addAll(ProfileUtils.buildMailableProfileItems(getActionNode(), logger));
        return profiles;
    }

    public List<SortableSelectItem> getAssignableProfileList() {
        final List<SortableSelectItem> profiles = new ArrayList<>();
        profiles.addAll(ProfileUtils.buildAssignableProfileItems(getActionNode(), logger));
        return profiles;
    }

    public List<Map> getFilteredUsers() {
        setCachedUsers(getFilterUsers());
        return getCachedUsers();
    }

    protected List<Map> getCachedUsers() {
        return cachedUsers;
    }

    protected void setCachedUsers(List<Map> cu) {
        cachedUsers = cu;
    }

    /**
     * Filters for the users by the given filter options.<br/> Use the method only if you want to
     * apply a new filter.<br/> Otherwise you should use getCachedUsers().
     */
    public List<Map> getFilterUsers() {

        final FacesContext context = FacesContext.getCurrentInstance();
        final List<Map> personNodes = new ArrayList<>();

        final RetryingTransactionHelper txnHelper = Repository.getRetryingTransactionHelper(context);
        final RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>() {
            public Object execute() throws Throwable {
                final Set<String> allInvited = buildInvitedSet();

                ProfileManagerService profileManagerService;

                // count the number of admin invited in the current node. If they are only one admin
                // this last can't be univited !!!

                final NodeRef activeNode = getActionNode().getNodeRef();
                profileManagerService = getProfileManagerServiceFactory()
                        .getProfileManagerService(activeNode);
                int currentNodeAdminCount = 0;
                for (final String authority : allInvited) {
                    String personProfile;
                    Profile profile;
                    personProfile = profileManagerService.getPersonProfile(activeNode, authority);
                    if (personProfile == null) {
                        continue;
                    }
                    profile = profileManagerService.getProfile(activeNode, personProfile);
                    if (profile.isAdmin()) {
                        currentNodeAdminCount++;
                    }
                }

                String personProfile;
                Profile profile;

                if (searchText != null) {
                    searchText = searchText.toLowerCase().trim();
                }

                for (final String authority : allInvited) {
                    final NodeRef nodeRef = getPersonService().getPerson(authority);
                    final MapNode node = new MapNode(nodeRef);
                    final String firstName = (String) node.get(ContentModel.PROP_FIRSTNAME.toString());
                    final String lastName = (String) node.get(ContentModel.PROP_LASTNAME.toString());
                    final String email = (String) node.get(ContentModel.PROP_EMAIL.toString());

                    if (searchText != null && !searchText.isEmpty()) {

                        if (!firstName.toLowerCase().contains(searchText) && !lastName.toLowerCase()
                                .contains(searchText) && !email.toLowerCase().contains(searchText)) {
                            continue;
                        }
                    }
                    node.put(PermissionUtils.KEY_USER_FULL_NAME, firstName + " " + lastName);
                    node.put("firstName", firstName);
                    node.put("lastName", lastName);
                    node.put(PermissionUtils.KEY_EMAIL, email);
                    node.put(PermissionUtils.KEY_AUTHORITY, authority);
                    node.put(PermissionUtils.KEY_DISPLAY_NAME, authority);
                    node.put(PermissionUtils.KEY_USER_NAME, getCurrentUserName(node));

                    personProfile = profileManagerService.getPersonProfile(activeNode, authority);
                    if (personProfile == null) {
                        continue;
                    }
                    profile = profileManagerService.getProfile(activeNode, personProfile);
                    node.put(PermissionUtils.KEY_USER_PROFILE, profile.getProfileDisplayName());
                    node.put(PermissionUtils.KEY_ICON, WebResources.IMAGE_PERSON);
                    if (profile.isImported()) {
                        node.put("canBeRemoved", Boolean.FALSE);
                    } else if (authority.equals(getAuthenticationService().getCurrentUserName())) {
                        node.put("canBeRemoved", Boolean.FALSE);
                    } else {
                        //Check if this is last leader
                        if (currentNodeAdminCount == 1 && profile.isAdmin()) {
                            node.put("canBeRemoved", Boolean.FALSE);
                        } else {
                            node.put("canBeRemoved", Boolean.TRUE);
                        }
                    }
                    personNodes.add(node);
                }
                return null;
            }
        };

        try {
            txnHelper.doInTransaction(callback, true);
        } catch (final InvalidNodeRefException refErr) {
            Utils.addErrorMessage(translate(Repository.ERROR_NODEREF, refErr.getNodeRef()));
            return Collections.emptyList();
        } catch (final Throwable err) {
            Utils.addErrorMessage(translate(Repository.ERROR_GENERIC, err.getMessage()), err);
            return Collections.emptyList();
        }

        return personNodes;
    }

    public String getCurrentUserName(final MapNode node) {
        return PermissionUtils.computeUserLogin(node.getProperties());
    }

    /**
     * Build a set of users in the interest group. The link between the IG name and the IG_GROUP is
     * based on the name.
     */
    private Set<String> buildInvitedSet() {
        final FacesContext context = FacesContext.getCurrentInstance();
        final RetryingTransactionHelper txnHelper = Repository.getRetryingTransactionHelper(context);
        final RetryingTransactionCallback<Set<String>> callback = new RetryingTransactionCallback<Set<String>>() {
            public Set<String> execute() throws Throwable {
                final NodeRef nodeRef = getActionNode().getNodeRef();
                final ProfileManagerService profileManagerService = getProfileManagerServiceFactory()
                        .getProfileManagerService(nodeRef);

                final String selectedProf = getSelectedProfile();

                Set<String> invitedUsers = Collections.emptySet();

                if (selectedProf == null || selectedProf.length() < 1 || VALUE_ALL_PROFILES
                        .equals(selectedProf)) {
                    invitedUsers = profileManagerService.getInvitedUsersProfiles(nodeRef).keySet();
                } else {
                    if (authorityService.authorityExists(selectedProf)) {
                        invitedUsers = getAuthorityService().getContainedAuthorities(AuthorityType.USER,
                                selectedProf, false);
                    } else {
                        if (logger.isErrorEnabled()) {
                            logger.error("Authority does not exists: " + selectedProf);
                        }
                    }

                }

                // include groups and users and also groups included in group (subgroups)
                // limit the result to users
                return invitedUsers;

            }
        };

        return txnHelper.doInTransaction(callback, true);
    }

    public String applyNewProfile() {
        try {
            final NodeRef IGNodeRef = getActionNode().getNodeRef();
            final ProfileManagerService profileManagerService = getProfileManagerServiceFactory()
                    .getProfileManagerService(IGNodeRef);
            for (Map map : getCachedUsers()) {
                Map node = null;
                if (map instanceof MapNode) {
                    node = (MapNode) map;
                } else if (map instanceof HashMap<?, ?>) {
                    node = (HashMap) map;
                }
                if (node != null) {
                    String userID = (String) node.get(PermissionUtils.KEY_AUTHORITY);
                    final Profile newProfile = profileManagerService
                            .getProfileFromGroup(IGNodeRef, this.newProfile);
                    final String newProfileName = newProfile.getProfileName();
                    profileManagerService.changePersonProfile(IGNodeRef, userID, newProfileName);
                }
            }
        } catch (Throwable err) {
            if (logger.isErrorEnabled()) {
                logger.error("Unexpected error:" + err.getMessage(), err);
            }

            Utils.addErrorMessage(translate(Repository.ERROR_GENERIC, err.getMessage()), err);
        }

        return "wai:dialog:close:wai:dialog:manageUserProfilesDialogWai";
    }


    @Override
    public String getContainerTitle() {
        return translate("manage_invited_users_dialog_title", getActionNode().getName());
    }

    @Override
    public String getContainerDescription() {
        return translate("manage_invited_users_dialog_description");
    }


    public String getBrowserTitle() {
        return translate("manage_invited_users_browser_title");
    }

    public String getPageIconAltText() {
        return translate("manage_invited_users_icon_tooltip");
    }

    @Override
    public String getCancelButtonLabel() {
        return translate("close");
    }

    /**
     * @return the authenticationService
     */
    protected final AuthenticationService getAuthenticationService() {
        if (authenticationService == null) {
            authenticationService = Services.getAlfrescoServiceRegistry(FacesContext.getCurrentInstance())
                    .getAuthenticationService();
        }
        return authenticationService;
    }

    /**
     * @param authenticationService the authenticationService to set
     */
    public final void setAuthenticationService(final AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * @return the personService
     */
    protected final PersonService getPersonService() {
        if (personService == null) {
            personService = Services.getAlfrescoServiceRegistry(FacesContext.getCurrentInstance())
                    .getPersonService();
        }
        return personService;
    }

    /**
     * @param personService the personService to set
     */
    public final void setPersonService(final PersonService personService) {
        this.personService = personService;
    }

    /**
     * @return the authorityService
     */
    protected final AuthorityService getAuthorityService() {
        if (authorityService == null) {
            authorityService = Services.getAlfrescoServiceRegistry(FacesContext.getCurrentInstance())
                    .getAuthorityService();
        }
        return authorityService;
    }

    /**
     * @param authorityService the authorityService to set
     */
    public final void setAuthorityService(final AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    /**
     * @return the selectedProfile
     */
    public final String getSelectedProfile() {
        return selectedProfile;
    }

    /**
     * @param selectedProfile the selectedProfile to set
     */
    public final void setSelectedProfile(final String selectedProfile) {
        this.selectedProfile = selectedProfile;
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    /**
     * Change listener for the method select box
     */
    public void updateList(final ValueChangeEvent event) {
        this.selectedProfile = (String) event.getNewValue();
        isImportedSelectedProfile = false;
        if (this.selectedProfile != null) {
            if (!VALUE_ALL_PROFILES.equalsIgnoreCase(this.selectedProfile)) {
                final NodeRef IGNodeRef = getActionNode().getNodeRef();
                final ProfileManagerService profileManagerService = getProfileManagerServiceFactory()
                        .getProfileManagerService(IGNodeRef);
                final Profile selectedProfile = profileManagerService
                        .getProfileFromGroup(IGNodeRef, this.selectedProfile);
                if (selectedProfile.isImported()) {
                    isImportedSelectedProfile = true;
                }
            }
        }
    }


    /**
     * Change listener for the method select box
     */
    public void updateSearchText(final ValueChangeEvent event) {
        this.searchText = (String) event.getNewValue();
    }


    /**
     * @return the newProfile
     */
    public final String getNewProfile() {
        return newProfile;
    }

    /**
     * @param newProfile the new Profile to set
     */
    public final void setNewProfile(final String newProfile) {
        this.newProfile = newProfile;
    }


    public void updateNewProfileList(final ValueChangeEvent event) {
        this.newProfile = (String) event.getNewValue();
    }

    public Boolean getDisableApplyNewProfileButton() {
        boolean result = selectedProfile == null || selectedProfile.endsWith(VALUE_ALL_PROFILES) || (
                selectedProfile.length() == 0) || getCachedUsers() == null || getCachedUsers().isEmpty()
                || isImportedSelectedProfile;
        return result;
    }

    public void export() {
        FacesContext context = FacesContext.getCurrentInstance();

        HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();

        String extension =
                exportType.equals(ExportTypeEnum.Excel) ? "xls" : exportType.toString().toLowerCase();

        List<Map> filteredUsers = getFilteredUsers();

        List<SearchResultRecord> userList = new ArrayList<>(filteredUsers.size());
        for (Map props : filteredUsers) {
            String username = "";
            if (CircabcConfig.OSS) {
                username = props.get("userName").toString();
            } else {
                username = props.get("{http://www.cc.cec/circabc/model/user/1.0}ecasUserName") == null ? ""
                        : props.get("{http://www.cc.cec/circabc/model/user/1.0}ecasUserName").toString();
            }
            String firstName = props.get("firstName") == null ? "" : props.get("firstName").toString();
            String lastName = props.get("lastName") == null ? "" : props.get("lastName").toString();
            String email = props.get("email") == null ? "" : props.get("email").toString();
            String currentProfile =
                    props.get("userProfile") == null ? "" : props.get("userProfile").toString();
            SearchResultRecord srr = new SearchResultRecord("", "", "", "", "");
            srr.setEmail(email);
            srr.setFirstName(firstName);
            srr.setLastName(lastName);
            srr.setProfile(currentProfile);
            srr.setMoniker(username);
            userList.add(srr);
        }

        ServletOutputStream outStream = null;
        try {
            outStream = response.getOutputStream();
            response.setHeader("Content-Disposition", "attachment;filename=MemberList." + extension);
            switch (exportType) {
                case CSV:
                    response.setContentType("text/csv;charset=UTF-8");
                    writeCSV(userList, outStream);
                    break;
                case XML:
                    response.setContentType("text/xml;charset=UTF-8");
                    writeXML(userList, outStream);
                    break;
                case Excel:
                    response.setContentType("application/vnd.ms-excel;charset=UTF-8");
                    writeXLS(userList, outStream);
                    break;
            }

            context.responseComplete();

        } catch (Exception ex) {
            logger.error("Error exporting file of type " + exportType.toString(), ex);
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

    private void writeCSV(List<SearchResultRecord> userList, ServletOutputStream outStream)
            throws IOException {
        OutputStreamWriter outStreamWriter = null;

        try {
            outStreamWriter = new OutputStreamWriter(outStream, "UTF-8");

            // write byte order mark
            outStream.write(0xEF);
            outStream.write(0xBB);
            outStream.write(0xBF);

            outStreamWriter.write("Username");
            outStreamWriter.write(',');
            outStreamWriter.write("First Name");
            outStreamWriter.write(',');
            outStreamWriter.write("Last Name");
            outStreamWriter.write(',');
            outStreamWriter.write("Email");
            outStreamWriter.write(',');
            outStreamWriter.write("Profile");
            outStreamWriter.write('\n');

            for (SearchResultRecord user : userList) {
                outStreamWriter.write(user.getMoniker());
                outStreamWriter.write(',');
                outStreamWriter.write(user.getFirstName());
                outStreamWriter.write(',');
                outStreamWriter.write(user.getLastName());
                outStreamWriter.write(',');
                outStreamWriter.write(user.getEmail());
                outStreamWriter.write(',');
                outStreamWriter.write(user.getProfile());
                outStreamWriter.write('\n');
            }
        } finally {
            if (outStreamWriter != null) {
                try {
                    outStreamWriter.flush();
                } catch (IOException ignore) {

                }
                try {
                    outStreamWriter.close();
                } catch (IOException ignore) {

                }
            }
        }

    }


    private void writeXML(List<SearchResultRecord> userList, ServletOutputStream outStream)
            throws XMLStreamException {
        XMLOutputFactory xof = XMLOutputFactory.newInstance();
        XMLStreamWriter xtw = null;

        xtw = xof.createXMLStreamWriter(outStream, "UTF-8");
        xtw.writeStartDocument("utf-8", "1.0");
        xtw.writeCharacters("\n");
        xtw.writeStartElement("members");

        for (SearchResultRecord user : userList) {
            xtw.writeCharacters("\n  ");
            xtw.writeStartElement("member");
            xtw.writeAttribute("username", user.getMoniker());
            xtw.writeAttribute("firstname", user.getFirstName());
            xtw.writeAttribute("lastname", user.getLastName());
            xtw.writeAttribute("email", user.getEmail());
            xtw.writeAttribute("profile", user.getProfile());
            xtw.writeEndElement();
        }
        xtw.writeCharacters("\n");
        xtw.writeEndElement();
        xtw.writeEndDocument();
        xtw.flush();
        xtw.close();
    }

    private void writeXLS(List<SearchResultRecord> userList, ServletOutputStream outStream)
            throws IOException {

        Workbook workbook = new HSSFWorkbook();

        Sheet sheet = workbook.createSheet("Members");

        Row titleRow = sheet.createRow(0);
        titleRow.createCell(0).setCellValue("username");
        titleRow.createCell(1).setCellValue("firstname");
        titleRow.createCell(2).setCellValue("lastname");
        titleRow.createCell(3).setCellValue("email");
        titleRow.createCell(4).setCellValue("profile");

        int idx = 1;

        for (SearchResultRecord user : userList) {
            Row row = sheet.createRow(idx);
            row.createCell(0).setCellValue(user.getMoniker());
            row.createCell(1).setCellValue(user.getFirstName());
            row.createCell(2).setCellValue(user.getLastName());
            row.createCell(3).setCellValue(user.getEmail());
            row.createCell(4).setCellValue(user.getProfile());
            idx++;
        }

        workbook.write(outStream);
    }

    public List<SelectItem> getExportTypes() {

        return WebClientHelper.getExportedTypes();
    }

    public String getExportType() {
        return exportType.toString();
    }

    public void setExportType(String value) {
        if (value != null) {
            exportType = ExportTypeEnum.valueOf(value);
        }
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
