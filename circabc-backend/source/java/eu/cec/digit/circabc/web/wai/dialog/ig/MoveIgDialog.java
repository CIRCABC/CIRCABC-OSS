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
package eu.cec.digit.circabc.web.wai.dialog.ig;

import eu.cec.digit.circabc.business.api.link.LinksBusinessSrv;
import eu.cec.digit.circabc.model.UserModel;
import eu.cec.digit.circabc.service.app.CircabcService;
import eu.cec.digit.circabc.service.profile.CategoryProfileManagerService;
import eu.cec.digit.circabc.service.profile.IGRootProfileManagerService;
import eu.cec.digit.circabc.service.profile.Profile;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.repository.CategoryHeaderNode;
import eu.cec.digit.circabc.web.wai.bean.navigation.CategoryHeader;
import eu.cec.digit.circabc.web.wai.bean.navigation.CategoryHeadersBean;
import eu.cec.digit.circabc.web.wai.bean.navigation.CategoryItem;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.*;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("rawtypes")
public class MoveIgDialog extends BaseWaiDialog {

    private static final long serialVersionUID = -5400857267102221360L;
    private static final Log logger = LogFactory.getLog(MoveIgDialog.class);
    // Properties used in JSP:
    private String sourceHeader;
    private String sourceCategory;
    private String sourceIg;
    private String targetHeader;
    private String targetCategory;
    private List allHeaders;
    // Properties injected by JSF:
    private CategoryHeadersBean categoryHeadersBean;
    private AuthorityService authorityService;
    private PersonService personService;
    private CircabcService circabcService;
    private NodeRef igRef;

    private NodeRef newCategoryRef;

    private NodeRef oldCategoryRef;

    //***************************************************************
    //                                                 DIALOG METHODS
    //***************************************************************

    public String getPageIconAltText() {
        return translate("move_ig_dialog_icon_tooltip");
    }

    public String getBrowserTitle() {
        return translate("move_ig_dialog_browser_title");
    }

    @Override
    public void init(Map<String, String> parameters) {
        igRef = null;
        newCategoryRef = null;
        oldCategoryRef = null;

        super.init(parameters);
    }

    protected String finishImpl(FacesContext context, String outcome)
            throws Throwable {
        if (this.getTargetCategory() != null && this.getSourceIg() != null) {

            // do not allow to move if there is some share spaces, importet or exported profiles

            igRef = new NodeRef(this.getSourceIg());
            newCategoryRef = new NodeRef(this.getTargetCategory());
            oldCategoryRef = new NodeRef(this.getSourceCategory());

            IGRootProfileManagerService igRootProfileManagerService = getProfileManagerServiceFactory()
                    .getIGRootProfileManagerService();

            if (hasSharedSpaces(igRef) || hasExportedOrImportedProfiles(igRef,
                    igRootProfileManagerService)) {
                this.isFinished = false;
                Utils.addErrorMessage(translate("move_ig_dialog_msg_invalid"));

            } else {
                try {
                    CategoryProfileManagerService categoryProfileManagerService = getProfileManagerServiceFactory()
                            .getCategoryProfileManagerService();

                    String masterInvitedGroupName = igRootProfileManagerService
                            .getMasterInvitedGroupName(igRef);
                    String newSubsGroupName = categoryProfileManagerService.getSubsGroupName(newCategoryRef);
                    String oldSubsGroupName = categoryProfileManagerService.getSubsGroupName(oldCategoryRef);

                    Profile newCategoryProfile = categoryProfileManagerService.getProfile(newCategoryRef,
                            CategoryProfileManagerService.Profiles.CIRCA_CATEGORY_ADMIN);
                    Profile oldCategoryProfile = categoryProfileManagerService.getProfile(oldCategoryRef,
                            CategoryProfileManagerService.Profiles.CIRCA_CATEGORY_ADMIN);

                    moveUserGroups(oldSubsGroupName, newSubsGroupName, masterInvitedGroupName);

                    getRuleService().disableRules();
                    // move the node
                    this.getFileFolderService().move(igRef, newCategoryRef, null);

                    changeCategoryAdminPermissionsReqursivly(igRef,
                            oldCategoryProfile.getPrefixedAlfrescoGroupName(),
                            newCategoryProfile.getPrefixedAlfrescoGroupName());

                    // confirm
                    this.isFinished = true;
                    Utils.addStatusMessage(FacesMessage.SEVERITY_INFO,
                            translate("move_ig_dialog_msg_confirmation", getName(igRef)));
                } finally {
                    getRuleService().enableRules();
                }

            }
        } else if (this.getTargetCategory() == null) {
            this.isFinished = false;
            Utils.addErrorMessage(translate("move_ig_dialog_msg_nocat"));
        } else if (this.getSourceIg() == null) {
            this.isFinished = false;
            Utils.addErrorMessage(translate("move_ig_dialog_msg_noig"));
        }

        // stay in the dialog
        return null;
    }

    @Override
    protected String doPostCommitProcessing(FacesContext context, String outcome) {
        if (igRef != null && newCategoryRef != null && oldCategoryRef != null) {
            getCircabcService().moveIG(oldCategoryRef, newCategoryRef, igRef);
        }
        return super.doPostCommitProcessing(context, outcome);
    }

    private boolean hasExportedOrImportedProfiles(NodeRef igRef,
                                                  IGRootProfileManagerService igRootProfileManagerService) {
        boolean result = false;
        List<Profile> profiles = igRootProfileManagerService.getProfiles(igRef);
        for (Profile profile : profiles) {
            if (profile.isExported() || profile.isImported()) {
                result = true;
                break;
            }
        }
        return result;
    }

    private boolean hasSharedSpaces(NodeRef igRef) {
        return getLinksBusinessSrv().findSharedSpaces(igRef).size() > 0;
    }

    private void changeCategoryAdminPermissionsReqursivly(
            NodeRef nodeRef, String oldPrefixedGroupName, String newPrefixedGroupName) {
        movePermissionRecursively(nodeRef, oldPrefixedGroupName, newPrefixedGroupName);
    }


    private void movePermissionRecursively(final NodeRef nodeRef, final String oldAuthority,
                                           final String newAuthority) {
        final Set<AccessPermission> accessPermissions = getPermissionService()
                .getAllSetPermissions(nodeRef);
        for (final AccessPermission accessPermission : accessPermissions) {
            if (accessPermission.getAuthority().equals(oldAuthority)) {
                getPermissionService()
                        .setPermission(nodeRef, newAuthority, accessPermission.getPermission(),
                                accessPermission.getAccessStatus() == AccessStatus.ALLOWED);
                getPermissionService().deletePermission(nodeRef, accessPermission.getAuthority(),
                        accessPermission.getPermission());
            }
        }

        // clear permissions on files, discussions, ...
        final List<FileInfo> inList = getFileFolderService().list(nodeRef);
        for (final FileInfo fileInfo : inList) {
            movePermissionRecursively(fileInfo.getNodeRef(), oldAuthority, newAuthority);
        }
    }


    private void moveUserGroups(String oldParentName, String newParentName, String childName) {
        String newParentGroupName = authorityService.getName(AuthorityType.GROUP, newParentName);
        String oldParentGroupName = authorityService.getName(AuthorityType.GROUP, oldParentName);
        String childGroupName = authorityService.getName(AuthorityType.GROUP, childName);

        authorityService.addAuthority(newParentGroupName, childGroupName);
        authorityService.removeAuthority(oldParentGroupName, childGroupName);

    }

    //***************************************************************
    //                                                         HELPER
    //***************************************************************

    @SuppressWarnings("unchecked")
    private List getAllCategoryHeaders() {
        List result = new ArrayList();
        result.add(new SelectItem("null", "<Select Header>"));
        for (CategoryHeader h : this.getCategoryHeadersBean().getCategoryHeaders()) {
            CategoryItem header = h.getCategoryHeaderItem();
            result.add(new SelectItem(
                    header.getNodeRef().toString(),
                    header.getName()));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List getCategoriesOfHeader(String headerRefStr) {
        List result = new ArrayList();
        if (isNotEmpty(headerRefStr)) {
            NodeRef nodeRef = new NodeRef(headerRefStr);
            CategoryHeaderNode node = new CategoryHeaderNode(nodeRef);
            CategoryHeader header = new CategoryHeader(node);
            result.add(new SelectItem("null", "<Select Category>"));
            for (CategoryItem category : header.getCategories()) {
                NodeRef categoryRef = category.getNodeRef();
                result.add(new SelectItem(categoryRef.toString(), getName(categoryRef)));
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List getInterestGroupsOfCategory(String catRefStr) {
        List result = new ArrayList();
        if (isNotEmpty(catRefStr)) {
            NodeRef catRef = new NodeRef(catRefStr);
            List<NodeRef> igs = getManagementService().getInterestGroups(catRef);
            for (NodeRef igRef : igs) {
                result.add(new SelectItem(igRef.toString(), getName(igRef)));
            }
        }
        return result;
    }

    private String getName(NodeRef ref) {
        Map<QName, Serializable> props = this.getNodeService().getProperties(ref);
        if (props.containsKey(ContentModel.PROP_NAME)) {
            return props.get(ContentModel.PROP_NAME).toString();
        } else {
            return ref.toString();
        }
    }

    private boolean isNotEmpty(String s) {
        return !(s == null || s.equals("") || s.equalsIgnoreCase("null"));
    }

    public void syncIG() {
        if (this.getSourceIg() != null) {
            NodeRef igRef = new NodeRef(this.getSourceIg());
            circabcService.resyncInterestGroup(igRef);
        }
    }


    public void syncCat() {
        if (this.getSourceCategory() != null) {
            NodeRef catRef = new NodeRef(this.getSourceCategory());
            circabcService.resyncCategory(catRef);
        }
    }

    public void syncAll() {
        circabcService.resyncAll();
    }

    public void syncUsers() {
        circabcService.resyncUsers();
    }

    public void syncCircabcAdmins() {
        circabcService.resyncCircabcAdmins();
    }

    public void export() {
        if (this.getSourceIg() != null) {
            FacesContext context = FacesContext.getCurrentInstance();

            HttpServletResponse response = (HttpServletResponse) context.getExternalContext()
                    .getResponse();

            List<UserRecord> userList = getUserList();

            ServletOutputStream outStream = null;
            try {
                response.reset();
                outStream = response.getOutputStream();
                String interestGroupName = getInterestGroupName();
                response
                        .setHeader("Content-Disposition", "attachment;filename=" + interestGroupName + ".xls");
                response.setContentType("application/vnd.ms-excel;charset=UTF-8");
                writeXLS(userList, outStream);

                context.responseComplete();

            } catch (Exception ex) {
                logger.error("Error exporting members ", ex);
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

    private String getInterestGroupName() {
        NodeRef igRef = new NodeRef(this.getSourceIg());
        return (String) getNodeService().getProperty(igRef, ContentModel.PROP_NAME);

    }

    private List<UserRecord> getUserList() {
        List<UserRecord> result = new ArrayList<>();
        NodeRef igRef = new NodeRef(this.getSourceIg());
        IGRootProfileManagerService igRootProfileManagerService = getProfileManagerServiceFactory()
                .getIGRootProfileManagerService();
        Map<String, Profile> invitedUsersProfiles = igRootProfileManagerService
                .getInvitedUsersProfiles(igRef);
        for (Map.Entry<String, Profile> entry : invitedUsersProfiles.entrySet()) {

            String userName = entry.getKey();
            NodeRef person = getPersonService().getPerson(userName);

            String firstName = (String) getNodeService().getProperty(person, ContentModel.PROP_FIRSTNAME);
            String lastName = (String) getNodeService().getProperty(person, ContentModel.PROP_LASTNAME);
            String email = (String) getNodeService().getProperty(person, ContentModel.PROP_EMAIL);
            String ecasDomain = (String) getNodeService().getProperty(person, UserModel.PROP_DOMAIN);
            String moniker = (String) getNodeService().getProperty(person, UserModel.PROP_ECAS_USER_NAME);
            String orgdepnumber = (String) getNodeService()
                    .getProperty(person, UserModel.PROP_ORGDEPNUMBER);
            String profile = entry.getValue().getProfileDisplayName();
            UserRecord user = new UserRecord(userName, firstName, lastName, email, ecasDomain, moniker,
                    profile, orgdepnumber);
            result.add(user);

        }
        return result;
    }

    private void writeXLS(List<UserRecord> userList, ServletOutputStream outStream)
            throws IOException {

        Workbook workbook = new HSSFWorkbook();

        Sheet sheet = workbook.createSheet("Members");

        Row titleRow = sheet.createRow(0);
        titleRow.createCell(0).setCellValue("User Name");
        titleRow.createCell(1).setCellValue("First Name");
        titleRow.createCell(2).setCellValue("Last Name");
        titleRow.createCell(3).setCellValue("Email");
        titleRow.createCell(4).setCellValue("ECAS Domain");
        titleRow.createCell(5).setCellValue("ECAS User Name");
        titleRow.createCell(6).setCellValue("Profile");
        titleRow.createCell(7).setCellValue("Organization");

        int idx = 1;

        for (UserRecord user : userList) {
            Row row = sheet.createRow(idx);
            row.createCell(0).setCellValue(user.getUserName());
            row.createCell(1).setCellValue(user.getFirstName());
            row.createCell(2).setCellValue(user.getLastName());
            row.createCell(3).setCellValue(user.getEmail());
            row.createCell(4).setCellValue(user.getECASDomain());
            row.createCell(5).setCellValue(user.getECASMoniker());
            row.createCell(6).setCellValue(user.getProfile());
            row.createCell(7).setCellValue(user.getOrgdepnumber());
            idx++;
        }

        workbook.write(outStream);
    }

    //***************************************************************
    //                                            GETTERS AND SETTERS
    //***************************************************************

    public CategoryHeadersBean getCategoryHeadersBean() {
        return categoryHeadersBean;
    }

    public void setCategoryHeadersBean(CategoryHeadersBean categoryHeadersBean) {
        this.categoryHeadersBean = categoryHeadersBean;
    }

    public String getSourceHeader() {
        return sourceHeader;
    }

    public void setSourceHeader(String sourceHeader) {
        if (isNotEmpty(sourceHeader)) {
            this.sourceHeader = sourceHeader;
        }
    }

    public String getSourceCategory() {
        return sourceCategory;
    }

    public void setSourceCategory(String sourceCategory) {
        if (isNotEmpty(sourceCategory)) {
            this.sourceCategory = sourceCategory;
        }
    }

    public String getSourceIg() {
        return sourceIg;
    }

    public void setSourceIg(String sourceIg) {
        if (isNotEmpty(sourceIg)) {
            this.sourceIg = sourceIg;
        }
    }

    public String getTargetHeader() {
        return targetHeader;
    }

    public void setTargetHeader(String targetHeader) {
        if (isNotEmpty(targetHeader)) {
            this.targetHeader = targetHeader;
        }
    }

    public String getTargetCategory() {
        return targetCategory;
    }

    public void setTargetCategory(String targetCategory) {
        if (isNotEmpty(targetCategory)) {
            this.targetCategory = targetCategory;
        }
    }


    public List getAllHeaders() {
        if (this.allHeaders == null) {
            this.allHeaders = getAllCategoryHeaders();
        }
        return this.allHeaders;
    }

    public List getSourceCategories() {
        if (this.getSourceHeader() != null) {
            return this.getCategoriesOfHeader(this.getSourceHeader());
        } else {
            return new ArrayList();
        }
    }

    public List getSourceIgs() {
        if (this.getSourceCategory() != null) {
            return this.getInterestGroupsOfCategory(this.getSourceCategory());
        } else {
            return new ArrayList();
        }
    }

    public List getTargetCategories() {
        if (this.getTargetHeader() != null) {
            return this.getCategoriesOfHeader(this.getTargetHeader());
        } else {
            return new ArrayList();
        }
    }

    public AuthorityService getAuthorityService() {
        return authorityService;
    }

    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    /**
     * @return the linksBusinessSrv
     */
    protected LinksBusinessSrv getLinksBusinessSrv() {
        return getBusinessRegistry().getLinksBusinessSrv();
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
