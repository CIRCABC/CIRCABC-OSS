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
package eu.cec.digit.circabc.web.wai.bean.navigation;

import eu.cec.digit.circabc.CircabcConfig;
import eu.cec.digit.circabc.model.UserModel;
import eu.cec.digit.circabc.service.cmr.security.CircabcConstant;
import eu.cec.digit.circabc.service.profile.CircabcRootProfileManagerService;
import eu.cec.digit.circabc.service.profile.IGRootProfileManagerService;
import eu.cec.digit.circabc.service.profile.Profile;
import eu.cec.digit.circabc.service.profile.permissions.DirectoryPermissions;
import eu.cec.digit.circabc.service.user.SearchResultRecord;
import eu.cec.digit.circabc.service.user.UserService;
import eu.cec.digit.circabc.web.PermissionUtils;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.WebClientHelper;
import eu.cec.digit.circabc.web.WebClientHelper.ExportTypeEnum;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNode;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNodeType;
import eu.cec.digit.circabc.web.bean.override.CircabcBrowseBean;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.NavigationBean;
import org.alfresco.web.bean.repository.MapNode;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.ui.common.SortableSelectItem;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.component.html.ext.HtmlDataTable;
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
 * Bean that backs the navigation inside the directory service
 *
 * @author Guillaume
 * @author yanick pignot
 * <p>
 * Migration 3.1 -> 3.4.6 - 02/12/2011 This class seems to be developed for CircaBC
 */
public class DirectoryBean extends InterestGroupBean {

    public static final String JSP_NAME = "directory-home.jsp";
    public static final String BEAN_NAME = "DirectoryBean";
    public static final String MSG_PAGE_TITLE = "members_home_title";
    public static final String MSG_PAGE_DESCRIPTION = "members_home_title_desc";
    public static final String MSG_PAGE_ICON_ALT = "members_home_icon_tooltip";
    public static final String MSG_NUMBER_OF_MEMBERS = "directory_number_of_members_search";
    private static final String EMPTY_STRING = "";
    private static final String MEMBERS = "members";
    private static final String IMAGES_ICONS_USERS_LARGE_GIF = "/images/icons/users_large.gif";
    /**
     *
     */
    private static final long serialVersionUID = -6967164595499663893L;
    /**
     * Logger
     */
    private static final Log logger = LogFactory.getLog(DirectoryBean.class);
    private static final String OUTCOME = CircabcBrowseBean.WAI_BROWSE_OUTCOME;
    /**
     * Error message if use enter 0,1 or characters for search
     */
    private static final String ERROR_CIRCABC_MIN_SEARCH_CHARS = "error_circabc_min_search_chars";
    /**
     * The I18N message string for size error on member request
     */
    protected String err_member_too_long = "members_home_error_too_many_member";
    /**
     * Transient list of profiles
     */
    protected SortableSelectItem[] profiles = null;
    protected SelectItem[] pageSizes = new SelectItem[]{new SelectItem(10), new SelectItem(20),
            new SelectItem(50), new SelectItem(100), new SelectItem(200)};
    /**
     * Transient list of profileMap
     */
    protected Map<String, Profile> profileMap = null;
    /**
     * Transient value for advanced option
     */
    protected boolean hasAdvancedOption = false;
    /**
     * Transient list of users for display purpose
     */
    protected List<Node> usersDisplay = Collections.emptyList();
    protected List<SearchResultRecord> ldapUsers = Collections.emptyList();
    /**
     * The IG Root nodeRef
     */
    protected NodeRef igRootNodeRef = null;
    protected transient HtmlDataTable dataTable;
    /**
     * The authorityService service reference
     */
    transient private AuthorityService authorityService;
    /**
     * The personService service reference
     */
    transient private PersonService personService;
    /**
     * The domain filter. Possible values : 'allusers' ; 'cec' or 'circabc'
     */
    private String domain = null;
    /**
     * The member filter. Possible values : 'members' or 'allcircabcuser'
     */
    private String member = null;
    /**
     * The text to search for
     */
    private String name = null;
    /**
     * The profile filter
     */
    private String profile = null;
    private Integer pageSize = null;
    /**
     * Flag to see if it is first time jsp page is displayed
     */
    private boolean isFirstTime;
    private Boolean userAllowedToExport;
    transient private UserService userService;
    private List searchResult;
    private SelectItem[] filters;
    private ExportTypeEnum exportType = ExportTypeEnum.CSV;

    public NavigableNodeType getManagedNodeType() {
        return NavigableNodeType.DIRECTORY;
    }

    public String getRelatedJsp() {
        return NAVIGATION_JSP_FOLDER + JSP_NAME;
    }

    public String getPageDescription() {
        return translate(MSG_PAGE_DESCRIPTION, getCurrentNode().getName());
    }

    public String getPageTitle() {
        return translate(MSG_PAGE_TITLE);
    }

    public String getBrowserTitle() {
        return translate(MSG_PAGE_TITLE);
    }

    public String getPageIcon() {
        return IMAGES_ICONS_USERS_LARGE_GIF;
    }

    public String getPageIconAltText() {
        return translate(MSG_PAGE_ICON_ALT);
    }

    @Override
    public String getWebdavUrl() {
        // not relevant for directory
        return null;
    }

    /**
     * Init any state that may be held by this bean
     */
    public void init(final Map<String, String> parameters) {
        final NavigableNode currentIg = getNavigator().getCurrentIGRoot();
        // Set 'the ig root'
        this.igRootNodeRef = getNavigator().getCurrentIGRoot().getNodeRef();
        // set the permissions
        this.hasAdvancedOption = currentIg
                .hasPermission(DirectoryPermissions.DIRMANAGEMEMBERS.toString());
        // Reset var
        this.domain = PermissionUtils.ALLUSERS;
        this.member = MEMBERS;
        this.name = EMPTY_STRING;
        this.profile = EMPTY_STRING;

        // Initialize the profile list
        this.profileMap = getProfileManagerServiceFactory().getIGRootProfileManagerService()
                .getProfileMap(igRootNodeRef);
        if (logger.isInfoEnabled()) {
            logger.info("init : " + profileMap.size() + " found");
        }

        final List<SortableSelectItem> items = new ArrayList<>();
        String element;
        Profile value;
        for (String s : profileMap.keySet()) {
            element = s;
            // Remove the generic profile
            if (element.equals(CircabcConstant.GUEST_AUTHORITY) || element
                    .equals(CircabcRootProfileManagerService.ALL_CIRCA_USERS_PROFILE_NAME)) {
                continue;
            }
            value = (Profile) profileMap.get(element);
            items.add(new SortableSelectItem(element, value.getProfileDisplayName(),
                    value.getProfileDisplayName()));
        }
        Collections.sort(items);
        profiles = items.toArray(new SortableSelectItem[items.size()]);

        isFirstTime = true;

        // fill the default list
        search();
    }

    @Override
    public boolean getInit() {
        // don't call this.search... that is the default behaviour of the parent classes
        return false;
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
                } finally {
                    outStreamWriter.close();
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

    public HtmlDataTable getDataTable() {
        if (dataTable == null) {
            dataTable = new HtmlDataTable();
        }
        return dataTable;
    }

    public void setDataTable(HtmlDataTable dataTable) {
        this.dataTable = dataTable;
    }

    public void export() {
        FacesContext context = FacesContext.getCurrentInstance();

        HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();

        String extension =
                exportType.equals(ExportTypeEnum.Excel) ? "xls" : exportType.toString().toLowerCase();

        List<SearchResultRecord> userList;

        if (this.member.equals("allcircabcuser")) {
            userList = getUsers();
        } else {

            List<Node> users = getUsers();
            userList = new ArrayList<>(users.size());
            for (Node node : users) {
                final Map<String, Object> props = node.getProperties();
                String username = "";
                if (CircabcConfig.OSS) {
                    username = props.get("userName").toString();
                } else {
                    username =
                            props.get("{http://www.cc.cec/circabc/model/user/1.0}ecasUserName") == null ? ""
                                    : props.get("{http://www.cc.cec/circabc/model/user/1.0}ecasUserName").toString();
                }
                String firstName = props.get("firstName") == null ? "" : props.get("firstName").toString();
                String lastName = props.get("lastName") == null ? "" : props.get("lastName").toString();
                String email = props.get("email") == null ? "" : props.get("email").toString();
                String currentProfile = props.get("profile") == null ? "" : props.get("profile").toString();
                SearchResultRecord srr = new SearchResultRecord("", "", "", "", "");
                srr.setEmail(email);
                srr.setFirstName(firstName);
                srr.setLastName(lastName);
                srr.setProfile(currentProfile);
                srr.setMoniker(username);
                userList.add(srr);
            }
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


    /**
     * Perform the search with filter criteria
     *
     * @return null (Always as we stay on the same page)
     */
    public String search() {
        //TODO this code is awfull... please to reduce the number of return ...

        // If null, the page is not evaluated
        // TODO : Performance : mettre dans une transaction
        // Reset the previous result set
        this.usersDisplay = Collections.emptyList();
        this.ldapUsers = Collections.emptyList();
        this.searchResult = Collections.EMPTY_LIST;

        // force user to enter more than 3 chars
        if (!this.isFirstTime && !MEMBERS.equals(this.member)) {
            if (this.name.trim().length() < 3) {
                ResourceBundle bundle = Application.getBundle(FacesContext.getCurrentInstance());
                String errorMessage = bundle.getString(ERROR_CIRCABC_MIN_SEARCH_CHARS);
                Utils.addErrorMessage(errorMessage);
                return OUTCOME;
            }
        }
        this.isFirstTime = false;

        if (this.domain == null || this.member == null || this.name == null) {
            if (logger.isErrorEnabled()) {
                logger.error("search : Piracy warning from |" + ((NavigationBean) FacesHelper
                        .getManagedBean(FacesContext.getCurrentInstance(), NavigationBean.BEAN_NAME))
                        .getCurrentUser().getUserName() + "|");
            }
            return OUTCOME;
        } else {
            // Determine the filter member or not
            switch (this.member) {
                case MEMBERS:

                    return searchMembers();

                case "allcircabcuser":
                    return searchAllUsers();

                default:
                    if (logger.isErrorEnabled()) {
                        logger.error("search |3| : Piracy warning from |" + ((NavigationBean) FacesHelper
                                .getManagedBean(FacesContext.getCurrentInstance(), NavigationBean.BEAN_NAME))
                                .getCurrentUser().getUserName() + "|");
                    }
                    return OUTCOME;
            }
        }
    }

    private String searchAllUsers() {

        this.ldapUsers = getUserService()
                .getUsersByDomainFirstNameLastNameEmail(this.domain, this.name.trim(), true);

        // Stay on same page
        return OUTCOME;
        // ************************************** Part FINAL - END ************************************
    }

    private String searchMembers() {
        List<MapNode> usersDomain;

        final IGRootProfileManagerService igRootProfileManagerService = getProfileManagerServiceFactory()
                .getIGRootProfileManagerService();

        // ****************************** Step one - Restrict to member (or more) - BEGIN ***************************
        Set<String> authorities = Collections.emptySet();
        Map<String, Profile> invitedUsersProfiles = igRootProfileManagerService
                .getInvitedUsersProfiles(this.igRootNodeRef);

        if (this.profile == null) {
            if (logger.isErrorEnabled()) {
                logger.error("search : Piracy warning from |" + ((NavigationBean) FacesHelper
                        .getManagedBean(FacesContext.getCurrentInstance(), NavigationBean.BEAN_NAME))
                        .getCurrentUser().getUserName() + "| Get a profile value null with members search");
            }
            return OUTCOME;
        }
        if (!this.profile.equals(EMPTY_STRING)) {
            // We are searching for members only -> get the corresponding list
            boolean isfilterProfileOk = false;
            Profile emptyProfile;

            isfilterProfileOk = true;
            // Set the filter
            emptyProfile = (Profile) profileMap.get(this.profile);
            // Get list of authorities
            if (authorityService.authorityExists(emptyProfile.getPrefixedAlfrescoGroupName())) {
                authorities = getAuthorityService().getContainedAuthorities(AuthorityType.USER,
                        emptyProfile.getPrefixedAlfrescoGroupName(), false);
            } else {
                if (logger.isErrorEnabled()) {
                    logger.error("Authority does bot exists: " + emptyProfile.getPrefixedAlfrescoGroupName());
                }
            }

            // Stop the for

            if (!isfilterProfileOk) {
                if (logger.isErrorEnabled()) {
                    logger.error("search |2| : Piracy warning from |" + ((NavigationBean) FacesHelper
                            .getManagedBean(FacesContext.getCurrentInstance(), NavigationBean.BEAN_NAME))
                            .getCurrentUser().getUserName() + "|");
                }
                return OUTCOME;
            }
        } else {
            // No profile filter
            //authorities = igRootProfileManagerService.getInvitedUsers(this.igRootNodeRef);
            authorities = invitedUsersProfiles.keySet();
        }

        if (logger.isDebugEnabled()) {
            logger.debug("QueryMembers:Found " + authorities.size() + " users");

        }
        // ****************************** Step one - Restrict to member (or more) - END *****************************

        // ****************************** Step two - Build node list - BEGIN ****************************************

        // Generic part - build the list

        // Intersect both list
        int authoritiesSize = authorities.size();

        if (logger.isDebugEnabled()) {
            logger.debug("Intersect list size: " + authoritiesSize + " users");
        }

        usersDomain = new ArrayList<>(authoritiesSize);

        if (authoritiesSize > 0) {
            MapNode node = null;
            Profile profile;
            for (final String authority : authorities) {
                // create our Node representation
                node = new MapNode(getPersonService().getPerson(authority));
                profile = invitedUsersProfiles.get(authority);
                final String profileDisplayName = profile.getProfileDisplayName();
                node.put("profile", profileDisplayName);
                usersDomain.add(node);
            }

        }

        // ****************************** Step two - Build node list - END ******************************************

        // ****************************** Step three - Restrict to search text criteria (or more) - END *************
        final String search = this.name.trim().toUpperCase();
        this.usersDisplay = new ArrayList<>(authoritiesSize);
        if ((search.equals(EMPTY_STRING))) {
            if (this.domain.equalsIgnoreCase(PermissionUtils.ALLUSERS)) {
                for (final MapNode node : usersDomain) {
                    this.usersDisplay.add(node);
                }
            } else {
                String currentDomain;
                for (final MapNode node : usersDomain) {
                    currentDomain = node.get(UserModel.PROP_DOMAIN.toString()).toString();
                    if (this.domain.equalsIgnoreCase(currentDomain)) {
                        this.usersDisplay.add(node);
                    }
                }

            }

        } else {
            // User requires contraints

            for (final MapNode node : usersDomain) {
                final String firstName = node.get(ContentModel.PROP_FIRSTNAME.toString()) == null ? ""
                        : node.get(ContentModel.PROP_FIRSTNAME.toString()).toString().toUpperCase();
                final String lastName = node.get(ContentModel.PROP_LASTNAME.toString()) == null ? ""
                        : node.get(ContentModel.PROP_LASTNAME.toString()).toString().toUpperCase();
                final String userName = node.get(ContentModel.PROP_USERNAME.toString()) == null ? ""
                        : node.get(ContentModel.PROP_USERNAME.toString()).toString().toUpperCase();
                final String email = node.get(ContentModel.PROP_EMAIL.toString()) == null ? ""
                        : node.get(ContentModel.PROP_EMAIL.toString()).toString().toUpperCase();

                if (this.domain.equalsIgnoreCase(PermissionUtils.ALLUSERS)) {

                    if ((firstName.contains(search)) || (lastName.contains(search)) || (userName
                            .contains(search)) || (email.contains(search))) {
                        // do not allow cec user to reset password
                        this.usersDisplay.add(node);
                    }
                } else {
                    final String currentDomain = node.get(UserModel.PROP_DOMAIN.toString()).toString();
                    if (this.domain.equalsIgnoreCase(currentDomain)) {
                        if ((firstName.contains(search)) || (lastName.contains(search)) || (userName
                                .contains(search)) || (email.contains(search))) {
                            // do not allow cec user to reset password
                            this.usersDisplay.add(node);
                        }
                    }


                }
            }

        }
        // ****************************** Step three - Restrict to search text criteria (or more) - END *************
        if (logger.isDebugEnabled()) {
            logger.debug("Final items : " + this.usersDisplay.size() + " users");
        }

        return OUTCOME;
    }

    /**
     * Tell if the current user has advanced search option
     *
     * @return True if the current user has advanced option
     */
    public boolean isAdvanced() {
        return this.hasAdvancedOption;
    }


    /**
     * Get the list of profile for filter use
     *
     * @return List of profiles
     */
    public SelectItem[] getProfiles() {
        return profiles;
    }

    public SelectItem[] getFilters() {
        if (filters == null) {
            filters = PermissionUtils.getDomainFilters(false, false, false);
        }
        return filters;
    }

    public SelectItem[] getPageSizes() {
        return pageSizes;
    }

    public Integer getPageSize() {
        if (pageSize == null) {
            return getBrowseBean().getListElementNumber();
        }
        return pageSize;
    }

    public void setPageSize(Integer number) {
        if (number != null) {
            pageSize = number;
        }
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


    /**
     * Change listener for the page size select box
     */
    public void updatePageSize(final ValueChangeEvent event) {
        setPageSize((Integer) event.getNewValue());
    }


    /**
     * Get the list of results users
     *
     * @return List of results users
     */
    public List getUsers() {
        if (this.member != null) {
            if (this.member.equals(MEMBERS)) {
                searchResult = (List) this.usersDisplay;

            } else if (this.member.equals("allcircabcuser")) {
                searchResult = (List) this.ldapUsers;

            }
        }
        return searchResult;

    }

    /***
     *
     * @return number of members in this IG
     */
    public String getUserCount() {
        return translate(MSG_NUMBER_OF_MEMBERS, getUsers().size());
    }

    /**
     * Getter for domain
     *
     * @return The domain value
     */
    public String getDomain() {
        return this.domain;
    }

    /**
     * Setter for domain
     *
     * @param domain The domain to set
     */
    public void setDomain(final String domain) {
        this.domain = domain;
    }

    /**
     * Getter for member
     *
     * @return The member value
     */
    public String getMember() {
        return this.member;
    }

    /**
     * Setter for member
     *
     * @param member The member to set
     */
    public void setMember(final String member) {
        this.member = member;
    }

    /**
     * Getter for name
     *
     * @return The name value
     */
    public String getName() {
        return this.name;
    }

    /**
     * Setter for name
     *
     * @param name The name to set
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Getter for profile
     *
     * @return The profile value
     */
    public String getProfile() {
        return this.profile;
    }

    /**
     * Setter for profile
     *
     * @param profile The profile to set
     */
    public void setProfile(final String profile) {
        this.profile = profile;
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
     * @return the UserService
     */
    public UserService getUserService() {

        if (userService == null) {
            userService = (UserService) Services
                    .getCircabcServiceRegistry(FacesContext.getCurrentInstance()).getUserService();
        }
        return userService;
    }

    /**
     * @param userService the UserService to set
     */
    public void setUserService(final UserService userService) {
        this.userService = userService;
    }

    public Boolean getUserAllowedToExport() {
        Boolean userAllowedToExport = false;

        if (getPermissionService().hasPermission(this.getCurrentInterstGroup().getNodeRef(),
                DirectoryPermissions.DIRMANAGEMEMBERS.toString()) == AccessStatus.ALLOWED
                ||
                getPermissionService().hasPermission(this.getCurrentInterstGroup().getNodeRef(),
                        DirectoryPermissions.DIRADMIN.toString()) == AccessStatus.ALLOWED) {
            userAllowedToExport = true;
        }

        return userAllowedToExport;
    }
}
