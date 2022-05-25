package io.swagger.api;

import eu.cec.digit.circabc.service.app.CircabcService;
import eu.cec.digit.circabc.business.api.BusinessRegistry;
import eu.cec.digit.circabc.business.api.user.UserDetails;
import eu.cec.digit.circabc.business.api.user.UserDetailsBusinessSrv;
import eu.cec.digit.circabc.business.helper.UserManager;
import eu.cec.digit.circabc.config.CircabcConfiguration;
import eu.cec.digit.circabc.model.UserModel;
import eu.cec.digit.circabc.repo.user.InvalidBulkImportFileFormatException;
import eu.cec.digit.circabc.service.profile.permissions.DirectoryPermissions;
import eu.cec.digit.circabc.service.struct.ManagementService;
import eu.cec.digit.circabc.service.user.*;
import eu.cec.digit.circabc.util.CircabcUserDataBean;
import eu.cec.digit.circabc.web.WebClientHelper;
import eu.cec.digit.circabc.web.wai.bean.content.CircabcUploadedFile;
import io.swagger.exception.AlreadyExistsException;
import io.swagger.model.*;
import io.swagger.util.ApiToolBox;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.UserJsonParser;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.configuration.ConfigurableService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.security.PersonService.PersonInfo;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.LocalDate;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.errors.IntrusionException;
import org.owasp.esapi.errors.ValidationException;
import org.springframework.extensions.webscripts.WebScriptResponse;

import javax.faces.application.FacesMessage;
import javax.imageio.ImageIO;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;
import java.util.*;

public class UsersApiImpl implements UsersApi {

    public static final String DEFAULT_AVATAR_CONF_PATH = "dictionary/{http://www.alfresco.org/model/content/1.0}CircaBC/{http://www.alfresco.org/model/content/1.0}users/{http://www.alfresco.org/model/content/1.0}preferences/{http://www.alfresco.org/model/content/1.0}avatar/{http://www.alfresco.org/model/content/1.0}default_avatar.png";
    private static final String TITLE = "title";
    private static final String ORGANISATION = "organisation";
    private static final List<String> definedColumns = new ArrayList<>(
            Arrays.asList(
                    "username",
                    TITLE,
                    "first name",
                    "last name",
                    "email",
                    "profile",
                    "profile title",
                    ORGANISATION,
                    "postal address"));
    private static final String URL_ADDRESS = "urlAddress";
    private static final String POSTAL_ADDRESS = "postalAddress";
    private static final String DESCRIPTION = "description";
    private static final String GLOBAL_NOTIFICATION_ENABLED = "globalNotificationEnabled";
    private static final String SIGNATURE = "signature";
    private static final String NO_ACCESS_ON_IG = "No access on IG:";
    private final Log logger = LogFactory.getLog(UsersApiImpl.class);
    private PersonService personService;
    private UserService userService;
    private LdapUserService ldapUserService;
    private GroupsApi groupsApi;
    private UserDetailsBusinessSrv userDetailsBusinessSrv;
    private NodeService secureNodeService;
    private BusinessRegistry businessRegistry;
    private AuthorityService authorityService;
    private PermissionService permissionService;
    private ManagementService managementService;
    private BulkUserImportService bulkUserImportService;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
    private ConfigurableService configurableService;
    private ProfilesApi profilesApi;
    private UserManager userManager;
    private CircabcService circabcService;

    @Override
    public UserDashboard getUserDashboard(String userId, LocalDate to, String language) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<User> usersGet(String query, boolean filter) {
        List<User> result = new ArrayList<>();

        List<SearchResultRecord> users;

        if (query.indexOf('@') == -1) {
            users = getUserService().getUsersByDomainFirstNameLastNameEmail("allusers", query, filter);
        } else {
            users = getUserService().getUsersByMailDomain(query, "allusers", filter);
        }

        for (SearchResultRecord user : users) {
            User newUser = new User();
            newUser.setEmail(user.getEmail());
            newUser.setFirstname(user.getFirstName());
            newUser.setLastname(user.getLastName());
            newUser.setUserId(user.getUserName());
            result.add(newUser);
        }

        return result;
    }

    @Override
    public User usersPost(User body) {

        if (userService.isUserExists(body.getUserId())) {
            throw new AlreadyExistsException("username.exists");
        }

        if (userManager.isEmailExists(body.getEmail())) {
            throw new AlreadyExistsException("email.exists");
        }

        Map<String, String> properties = (Map<String, String>) body.getProperties();
        CircabcUserDataBean circabcUserData = new CircabcUserDataBean();
        circabcUserData.setUserName(body.getUserId());
        circabcUserData.setFirstName(body.getFirstname());
        circabcUserData.setLastName(body.getLastname());
        circabcUserData.setTitle(properties.get(TITLE));
        circabcUserData.setEmail(body.getEmail());
        circabcUserData.setPhone(body.getPhone());
        circabcUserData.setFax(properties.get("fax"));
        circabcUserData.setURL(properties.get(URL_ADDRESS));
        circabcUserData.setPostalAddress(properties.get(POSTAL_ADDRESS));
        circabcUserData.setDescription(properties.get(DESCRIPTION));
        circabcUserData.setCompanyId(properties.get("companyId"));
        circabcUserData.setPassword(properties.get("password"));

        circabcUserData.setHomeSpaceNodeRef(managementService.getGuestHomeNodeRef());

        userService.createUser(circabcUserData, false);

        return body;
    }

    @Override
    public void usersUserIdDelete(String userId) {
        // TODO Auto-generated method stub

    }

    @Override
    public User usersUserIdGet(String userId) {

        User user;
        if (personService.personExists(userId)) {
            final NodeRef personNodeRef = personService.getPerson(userId);
            final UserDetails userDetails = userDetailsBusinessSrv.getUserDetails(personNodeRef);
            user = toUser(userDetails);
            // add domain to properties
            CircabcUserDataBean ldapUser = ldapUserService.getLDAPUserDataByUid(userId);
            if (ldapUser != null
                    && !Objects.equals(ldapUser.getDomain(), "")
                    && ldapUser.getDomain() != null) {
                ((Map<String, String>) user.getProperties()).put("domain", ldapUser.getDomain());
            }
        } else {
            CircabcUserDataBean ldapUser = ldapUserService.getLDAPUserDataByUid(userId);
            if (ldapUser != null
                    && !Objects.equals(ldapUser.getDomain(), "")
                    && ldapUser.getDomain() != null) {
                user = toSwaggerUser(ldapUser);
            } else {
                user = new User();
                user.setUserId(userId);
            }
        }

        return user;
    }

    private User toUser(final UserDetails userDetails) {

        User user = new User();

        user.setUserId(userDetails.getUserName());
        user.setFirstname(userDetails.getFirstName());
        user.setLastname(userDetails.getLastName());

        if (!currentUserPermissionCheckerService.isGuest()) {

            NodeRef avatarNodeRef = userDetails.getAvatar();
            Path path = secureNodeService.getPath(avatarNodeRef);
            if (path != null) {
                String avatarPath = path.toString();
                user.setDefaultAvatar(avatarPath != null && avatarPath.endsWith(DEFAULT_AVATAR_CONF_PATH));
            }

            user.setAvatar(avatarNodeRef.getId());
            user.setEmail(userDetails.getEmail());

            user.setPhone(userDetails.getPhone());
            user.setVisibility(userDetails.getVisibility());
            user.setUiLang(userDetails.getUserInterfaceLanguage());
            if (userDetails.getContentFilterLanguage() != null) {
                user.setContentFilterLang(userDetails.getContentFilterLanguage().getLanguage());
            } else {
                user.setContentFilterLang("all");
            }

            // add user's additional properties
            Map<String, String> properties = new HashMap<>();

            properties.put("ecMoniker", userDetails.getDisplayId());
            properties.put(TITLE, userDetails.getTitle());
            properties.put(ORGANISATION, userDetails.getOrganisation());
            properties.put(POSTAL_ADDRESS, userDetails.getPostalAddress());
            properties.put(DESCRIPTION, userDetails.getDescription());
            properties.put("fax", userDetails.getFax());
            properties.put(URL_ADDRESS, userDetails.getUrl());

            properties.put(
                    GLOBAL_NOTIFICATION_ENABLED, String.valueOf(userDetails.getGlobalNotification()));
            properties.put(SIGNATURE, userDetails.getSignature());
            properties.put("isAdmin", String.valueOf(authorityService.hasAdminAuthority()));

            properties.put("isCircabcAdmin", String.valueOf(isCircabcAdmin()));

            user.setProperties(properties);
        }

        return user;
    }

    private boolean isCircabcAdmin() {

        Set<String> authorities = authorityService.getAuthorities();

        for (String authority : authorities) {
            if (authority.startsWith("GROUP_CircaBCAdmin-")) {
                return true;
            }
        }

        return false;
    }

    /**
     * @see io.swagger.api.UsersApi#usersUserIdGetFromLdap(java.lang.String)
     */
    @Override
    public User usersUserIdGetFromLdap(String userId) {

        final NodeRef personNodeRef = personService.getPerson(userId);
        final UserDetails userDetails = userDetailsBusinessSrv.getUserDetails(personNodeRef);

        businessRegistry.getRemoteUserBusinessSrv().reloadDetails(userDetails);

        return toUser(userDetails);
    }

    /**
     * @see io.swagger.api.UsersApi#removeAvatar(java.lang.String)
     */
    @Override
    public void removeAvatar(String userId) {

        final NodeRef personNodeRef = personService.getPerson(userId);
        final UserDetails userDetails = userDetailsBusinessSrv.getUserDetails(personNodeRef);

        final UserDetailsBusinessSrv userDetailsBusinessSrv = businessRegistry.getUserDetailsBusinessSrv();

        userDetailsBusinessSrv.removeAvatar(userDetails.getNodeRef());
    }

    /**
     * @see io.swagger.api.UsersApi#updateAvatar(java.lang.String,
     *      java.io.InputStream,
     *      java.lang.String)
     */
    @Override
    public void updateAvatar(String userId, InputStream imageInputStream, String fileName) {

        try {
            ESAPI
                    .validator()
                    .getValidFileName(
                            "submitted file",
                            fileName,
                            new ArrayList<>(Arrays.asList(".jpg", ".jpeg", ".bmp", ".gif", ".png")),
                            false);
        } catch (ValidationException | IntrusionException vex) {
            throw new IllegalArgumentException("Invalid file type: " + fileName);
        }

        CircabcUploadedFile uploadedFile;

        try {
            uploadedFile = scaleImage(imageInputStream);
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not update avatar.", e);
        }

        final NodeRef personNodeRef = personService.getPerson(userId);
        userDetailsBusinessSrv.updateAvatar(
                personNodeRef, uploadedFile.getFileName(), uploadedFile.getFile());
    }

    private CircabcUploadedFile scaleImage(InputStream imageInputStream) throws IOException {

        CircabcUploadedFile uploadedFile = new CircabcUploadedFile();

        uploadedFile.setFileName("avatar.png");
        File file = new File(uploadedFile.getFileName());
        uploadedFile.setFile(file);

        long attachmentTotalSize = Long.parseLong(
                CircabcConfiguration.getProperty(CircabcConfiguration.LOGO_ALLOWED_SIZE_BYTES));

        long bytesRead = ApiToolBox.inputStreamToFile(imageInputStream, file, attachmentTotalSize);

        if (bytesRead * -1 > attachmentTotalSize) {
            throw new IllegalArgumentException("File exceeds 1Mb size: " + uploadedFile.getFileName());
        }

        BufferedImage bufferedImage = ImageIO.read(file);
        if (bufferedImage == null) {
            throw new IllegalArgumentException(
                    "Not an image, or image corrupted: " + uploadedFile.getFileName());
        }

        boolean couldWrite;

        double MAX_SIZE = 64.0;
        if (bufferedImage.getWidth() > MAX_SIZE || bufferedImage.getHeight() > MAX_SIZE) {

            // image needs to be scaled down, find scale factor
            double scaleFactor;
            if (bufferedImage.getWidth() > bufferedImage.getHeight()) {
                scaleFactor = (double) bufferedImage.getWidth() / MAX_SIZE;
            } else {
                scaleFactor = (double) bufferedImage.getHeight() / MAX_SIZE;
            }

            // scale it down
            int newWidth = (int) ((double) bufferedImage.getWidth() / scaleFactor);
            int newHeight = (int) ((double) bufferedImage.getHeight() / scaleFactor);
            Image image = bufferedImage.getScaledInstance(newWidth, newHeight, Image.SCALE_FAST);
            BufferedImage scaledImg = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphic = scaledImg.createGraphics();
            graphic.drawImage(image, 0, 0, null);

            // store the scaled image as png
            couldWrite = ImageIO.write(scaledImg, "png", uploadedFile.getFile());
        } else {
            // store the original image as png
            couldWrite = ImageIO.write(bufferedImage, "png", uploadedFile.getFile());
        }
        if (!couldWrite) {
            throw new IllegalArgumentException("Error saving avatar file.");
        }

        return uploadedFile;
    }

    @Override
    public User usersUserIdPut(String userId, User body) {
        User result = new User();
        final NodeRef personNodeRef = personService.getPerson(userId);
        final PersonInfo person = personService.getPerson(personNodeRef);
        final UserDetails userDetails = userDetailsBusinessSrv.getUserDetails(personNodeRef);

        if (configurableService.getConfigurationFolder(personNodeRef) == null) {
            configurableService.makeConfigurable(personNodeRef);
        }

        result.setAvatar(userDetails.getAvatar().getId());
        result.setEmail(userDetails.getEmail());
        result.setUserId(person.getUserName());
        result.setFirstname(person.getFirstName());
        result.setLastname(person.getLastName());
        result.setPhone(userDetails.getPhone());
        result.setVisibility(userDetails.getVisibility());
        result.setUiLang(userDetails.getUserInterfaceLanguage());
        if (userDetails.getContentFilterLanguage() != null) {
            result.setContentFilterLang(userDetails.getContentFilterLanguage().getLanguage());
        } else {
            result.setContentFilterLang("all");
        }

        if (body.getAvatar() != null) {
            userDetails.setAvatar(Converter.createNodeRefFromId(body.getAvatar()));
            result.setAvatar(body.getAvatar());
        }
        if (body.getEmail() != null) {
            userDetails.setEmail(body.getEmail());
            result.setEmail(body.getEmail());
        }
        if (body.getFirstname() != null) {
            userDetails.setFirstName(body.getFirstname());
            result.setFirstname(body.getFirstname());
        }
        if (body.getLastname() != null) {
            userDetails.setLastName(body.getLastname());
            result.setLastname(body.getLastname());
        }
        if (body.getPhone() != null) {
            userDetails.setPhone(body.getPhone());
            result.setPhone(body.getPhone());
        }
        if (body.getVisibility() != null) {
            userDetails.setVisibility(body.getVisibility());
            result.setVisibility(body.getVisibility());
        }
        if (body.getUiLang() != null) {
            userDetails.setUserInterfaceLanguage(body.getUiLang());
            result.setUiLang(body.getUiLang());
        }
        if (body.getContentFilterLang() != null) {
            userDetails.setContentFilterLanguage(new Locale(body.getContentFilterLang()));
            result.setContentFilterLang(body.getContentFilterLang());
        }

        // add user's additional properties
        if (body.getProperties() != null) {
            @SuppressWarnings("unchecked")
            Map<String, String> properties = (Map<String, String>) body.getProperties();

            if (properties.get(TITLE) != null) {
                userDetails.setTitle(properties.get(TITLE));
            }
            if (properties.get(ORGANISATION) != null) {
                userDetails.setOrganisation(properties.get(ORGANISATION));
            }
            if (properties.get(POSTAL_ADDRESS) != null) {
                userDetails.setPostalAddress(properties.get(POSTAL_ADDRESS));
            }
            if (properties.get(DESCRIPTION) != null) {
                userDetails.setDescription(properties.get(DESCRIPTION));
            }
            if (properties.get("fax") != null) {
                userDetails.setFax(properties.get("fax"));
            }
            if (properties.get(URL_ADDRESS) != null) {
                userDetails.setUrl(properties.get(URL_ADDRESS));
            }

            if (properties.get(GLOBAL_NOTIFICATION_ENABLED) != null) {
                userDetails.setGlobalNotification(
                        "true".equals(properties.get(GLOBAL_NOTIFICATION_ENABLED)));
            }
            if (properties.get(SIGNATURE) != null) {
                userDetails.setSignature(properties.get(SIGNATURE));
            }

            result.setProperties(properties);
        }

        userDetailsBusinessSrv.updateUserDetails(personNodeRef, userDetails);

        circabcService.updateUser(personNodeRef);

        return result;
    }

    public void setCircabcService(CircabcService circabcService) {
        this.circabcService = circabcService;
    }

    public PersonService getPersonService() {
        return personService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    @Override
    public List<InterestGroupProfile> getUserMembership(String userId) {
        return getUserMembershipInternal(userId, true);
    }

    @Override
    public List<InterestGroupProfile> getUserMembership(String userId, Boolean lightMode) {
        return getUserMembershipInternal(userId, lightMode);
    }

    private List<InterestGroupProfile> getUserMembershipInternal(String userId, Boolean lightMode) {
        List<InterestGroupProfile> result = new ArrayList<>();

        if (!"".equals(userId)) {
            List<UserIGMembershipRecord> interestGroupsMemberships = userService.getInterestGroups(userId);
            for (UserIGMembershipRecord membership : interestGroupsMemberships) {

                InterestGroupProfile interestGroupProfile = new InterestGroupProfile();
                interestGroupProfile.setInterestGroup(
                        groupsApi.getInterestGroup(membership.getInterestGroupNodeId(), lightMode));

                Profile p = new Profile();
                p.setId(membership.getprofileNodeRefId());
                p.setName(membership.getProfile());
                p.setTitle(Converter.toI18NProperty(membership.getProfileTitle()));
                p.setGroupName(
                        membership.getAlfrescoGroup().contains("GROUP_")
                                ? membership.getAlfrescoGroup()
                                : "GROUP_" + membership.getAlfrescoGroup());
                interestGroupProfile.setProfile(p);

                result.add(interestGroupProfile);
            }
        }

        return result;
    }

    /**
     * @return the userService
     */
    public UserService getUserService() {
        return userService;
    }

    /**
     * @param userService the userService to set
     */
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    /**
     * @return the groupsApi
     */
    public GroupsApi getGroupsApi() {
        return groupsApi;
    }

    /**
     * @param groupsApi the groupsApi to set
     */
    public void setGroupsApi(GroupsApi groupsApi) {
        this.groupsApi = groupsApi;
    }

    public UserDetailsBusinessSrv getUserDetailsBusinessSrv() {
        return userDetailsBusinessSrv;
    }

    public void setUserDetailsBusinessSrv(UserDetailsBusinessSrv userDetailsBusinessSrv) {
        this.userDetailsBusinessSrv = userDetailsBusinessSrv;
    }

    public NodeService getSecureNodeService() {
        return secureNodeService;
    }

    public void setSecureNodeService(NodeService secureNodeService) {
        this.secureNodeService = secureNodeService;
    }

    /**
     * @return the ldapUserService
     */
    public LdapUserService getLdapUserService() {
        return ldapUserService;
    }

    /**
     * @param ldapUserService the ldapUserService to set
     */
    public void setLdapUserService(LdapUserService ldapUserService) {
        this.ldapUserService = ldapUserService;
    }

    /**
     * @return the configurableService
     */
    public ConfigurableService getConfigurableService() {
        return configurableService;
    }

    /**
     * @param configurableService the configurableService to set
     */
    public void setConfigurableService(ConfigurableService configurableService) {
        this.configurableService = configurableService;
    }

    /**
     * @param businessRegistry the businessRegistry to set
     */
    public void setBusinessRegistry(BusinessRegistry businessRegistry) {
        this.businessRegistry = businessRegistry;
    }

    /**
     * @param authorityService the authorityService to set
     */
    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    /**
     * @param permissionService the permissionService to set
     */
    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    /**
     * @param managementService the managementService to set
     */
    public void setManagementService(ManagementService managementService) {
        this.managementService = managementService;
    }

    /**
     * @param bulkUserImportService the bulkUserImportService to set
     */
    public void setBulkUserImportService(BulkUserImportService bulkUserImportService) {
        this.bulkUserImportService = bulkUserImportService;
    }

    /**
     * @param currentUserPermissionCheckerService the
     *                                            currentUserPermissionCheckerService
     *                                            to set
     */
    public void setCurrentUserPermissionCheckerService(
            CurrentUserPermissionCheckerService currentUserPermissionCheckerService) {
        this.currentUserPermissionCheckerService = currentUserPermissionCheckerService;
    }

    /**
     * @param profilesApi the profilesApi to set
     */
    public void setProfilesApi(ProfilesApi profilesApi) {
        this.profilesApi = profilesApi;
    }

    @Override
    public List<Category> getUserCategories(String userId) {

        List<Category> result = new ArrayList<>();

        if (!"".equals(userId)) {
            List<UserCategoryMembershipRecord> categories = userService.getCategories(userId);
            for (UserCategoryMembershipRecord categAdmin : categories) {
                NodeRef categRef = Converter.createNodeRefFromId(categAdmin.getCategoryNodeId());
                Category categ = new Category();
                categ.setId(categAdmin.getCategoryNodeId());
                categ.setName(secureNodeService.getProperty(categRef, ContentModel.PROP_NAME).toString());

                Serializable title = secureNodeService.getProperty(categRef, ContentModel.PROP_TITLE);
                if (title instanceof String) {
                    categ.setTitle(Converter.toI18NProperty(title.toString()));
                } else if (title instanceof MLText) {
                    categ.setTitle(Converter.toI18NProperty((MLText) title));
                }

                result.add(categ);
            }
        }

        return result;
    }

    public void writeBulkInviteTemplate(WebScriptResponse response) {

        response.setContentType("application/vnd.ms-excel;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=template.xls");

        OutputStream outStream = null;

        try {
            outStream = response.getOutputStream();

            Workbook workbook = new HSSFWorkbook();

            Sheet sheet = workbook.createSheet("Members");

            Row titleRow = sheet.createRow(0);
            for (int idx = 0; idx < definedColumns.size(); idx++) {
                titleRow.createCell(idx).setCellValue(definedColumns.get(idx));
            }

            workbook.write(outStream);

        } catch (IOException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error during template generation", e);
            }
        } finally {
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (IOException e) {
                    logger.error("Error closing stream", e);
                }
            }
        }
    }

    public List<Category> getBulkInviteCategories(String username) {

        List<Category> availableCategories = new ArrayList<>();

        Set<String> included = new HashSet<>();

        for (UserCategoryMembershipRecord categoryAdminEntry : getUserService().getCategories(username)) {
            if (!included.contains(categoryAdminEntry.getCategoryNodeId())) {
                if (!currentUserPermissionCheckerService.hasAlfrescoReadPermission(
                        categoryAdminEntry.getCategoryNodeId())) {
                    throw new AccessDeniedException(
                            "No access on category:" + categoryAdminEntry.getCategoryNodeId());
                }
                availableCategories.add(
                        new Category(categoryAdminEntry.getCategoryNodeId(), categoryAdminEntry.getCategory()));
                included.add(categoryAdminEntry.getCategoryNodeId());
            }
        }

        for (UserIGMembershipRecord igEntry : getUserService().getInterestGroups(username)) {
            if (!included.contains(igEntry.getCategoryNodeId())) {
                if (!currentUserPermissionCheckerService.hasAlfrescoReadPermission(
                        igEntry.getCategoryNodeId())) {
                    throw new AccessDeniedException("No access on category:" + igEntry.getCategoryNodeId());
                }
                availableCategories.add(
                        new Category(igEntry.getCategoryNodeId(), igEntry.getCategoryTitle()));
                included.add(igEntry.getCategoryNodeId());
            }
        }

        return availableCategories;
    }

    public List<IGData> getBulkInviteIGs(String categoryId, String currentIgId) {

        Set<String> included = new HashSet<>();
        List<NodeRef> categRefs = new ArrayList<>();

        List<IGData> igs = new ArrayList<>();

        String username = AuthenticationUtil.getFullyAuthenticatedUser();

        NodeRef categoryNodeRef = Converter.createNodeRefFromId(categoryId);

        if (!secureNodeService.exists(categoryNodeRef)) {
            throw new IllegalArgumentException("Invalid Category id " + categoryId);
        }

        categRefs.add(categoryNodeRef);

        if (profilesApi.getInvitedUsers(categRefs.get(0), "circaCategory").contains(username)) {
            for (NodeRef igEntry : managementService.getInterestGroups(categRefs.get(0))) {
                if (!included.contains(igEntry.getId()) && !igEntry.getId().equals(currentIgId)) {
                    if (!currentUserPermissionCheckerService.hasAlfrescoReadPermission(igEntry.getId())) {
                        throw new AccessDeniedException(NO_ACCESS_ON_IG + igEntry.getId());
                    }
                    String title = (String) secureNodeService.getProperty(igEntry, ContentModel.PROP_TITLE);
                    if (title == null) {
                        title = (String) secureNodeService.getProperty(igEntry, ContentModel.PROP_NAME);
                    }
                    igs.add(new IGData(igEntry.getId(), title, categRefs.get(0).toString()));
                    included.add(igEntry.getId());
                }
            }
        } else {
            for (UserIGMembershipRecord igEntry : getUserService().getInterestGroups(username, categRefs)) {
                if (permissionService.hasPermission(
                        Converter.createNodeRefFromId(igEntry.getInterestGroupNodeId()),
                        DirectoryPermissions.DIRACCESS.toString()) == AccessStatus.ALLOWED) {
                    if (!included.contains(igEntry.getInterestGroupNodeId())
                            && !igEntry.getInterestGroupNodeId().equals(currentIgId)) {
                        if (!currentUserPermissionCheckerService.hasAlfrescoReadPermission(currentIgId)) {
                            throw new AccessDeniedException(NO_ACCESS_ON_IG + currentIgId);
                        }
                        igs.add(
                                new IGData(
                                        igEntry.getInterestGroupNodeId(),
                                        igEntry.getInterestGroupTitle(),
                                        igEntry.getCategoryNodeId()));
                        included.add(igEntry.getInterestGroupNodeId());
                    }
                }
            }
        }

        return igs;
    }

    public List<BulkImportUserData> getBulkInviteMembers(List<String> igIds, String destinationIGId) {

        if (igIds == null) {
            throw new IllegalArgumentException("IG ids cannot be null.");
        }

        if (destinationIGId == null) {
            throw new IllegalArgumentException("The destination IG id cannot be null.");
        }

        NodeRef destinationIGNodeRef = Converter.createNodeRefFromId(destinationIGId);

        List<BulkImportUserData> userData = new ArrayList<>();

        for (String igId : igIds) {

            NodeRef nodeRef = Converter.createNodeRefFromId(igId);

            if (!secureNodeService.exists(nodeRef)) {
                throw new IllegalArgumentException("Invalid IG id " + igId);
            }

            if (!currentUserPermissionCheckerService.hasAlfrescoReadPermission(igId)) {
                throw new AccessDeniedException(NO_ACCESS_ON_IG + igId);
            }

            bulkUserImportService.addAll(
                    userData, bulkUserImportService.listMembers(nodeRef, false), destinationIGNodeRef);
        }

        return userData;
    }

    public void bulkInviteUsers(
            String bulkInviteDataJson, String igId, boolean createNewProfiles, boolean notifyUsers) {

        if (!currentUserPermissionCheckerService.isGroupAdmin(igId)) {
            throw new AccessDeniedException("No Leader on IG:" + igId);
        }

        BulkInviteData bulkInviteData = parseBulkInviteBodyJSON(bulkInviteDataJson);

        NodeRef currentIGNodeRef = Converter.createNodeRefFromId(igId);

        List<BulkImportUserData> bulkImportUserData = new ArrayList<>();

        for (BulkImportUserDataModel model : bulkInviteData.getBulkImportUserData()) {
            BulkImportUserData userData = getBulkImportUserData(model);
            if (createNewProfiles && userData.getIgName() != null && userData.getIgName().length() > 0) {
                userData.setProfile(userData.getIgName());
            }
            bulkImportUserData.add(userData);
        }

        if (containsProfileBlankName(bulkImportUserData)) {
            throw new IllegalArgumentException(
                    "The supplied user list contains an user with an empty profile name.");
        }

        Map<String, String> igProfilesMap = new HashMap<>();

        for (NameValue nameValue : bulkInviteData.getIgProfiles()) {
            String key = nameValue.getName();
            if ("Registered".equals(key) || "Guest".equals(key)) {
                continue;
            }
            igProfilesMap.put(nameValue.getName(), nameValue.getValue());
        }

        bulkUserImportService.inviteUsers(
                bulkImportUserData, currentIGNodeRef, igProfilesMap, notifyUsers);
    }

    /**
     * @see io.swagger.api.UsersApi#bulkInviteUsersDigestFile(java.lang.String,
     *      java.io.InputStream,
     *      java.lang.String)
     */
    @Override
    public List<BulkImportUserData> bulkInviteUsersDigestFile(
            String igId, InputStream inputStream, String fileName) {

        NodeRef igNodeRef = Converter.createNodeRefFromId(igId);

        List<BulkImportUserData> userData = new ArrayList<>();
        List<BulkImportUserData> loadedUserData;

        try {

            if (fileName == null) {
                throw new IllegalArgumentException("Null file name.");
            } else if (fileName.toLowerCase().endsWith(".xls")
                    || fileName.toLowerCase().endsWith(".xlsx")) {
                loadedUserData = loadExcelFile(inputStream, fileName);
            } else if (fileName.toLowerCase().endsWith(".csv")) {
                loadedUserData = loadCSVFile(inputStream, fileName);
            } else if (fileName.toLowerCase().endsWith(".xml")) {
                loadedUserData = loadXMLFile(inputStream, fileName);
            } else {
                throw new IllegalArgumentException("Invalid file extension: " + fileName);
            }

            bulkUserImportService.addAll(userData, loadedUserData, igNodeRef);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Cannot import file: " + fileName + ", " + e.getMessage(), e);
        } finally {
            if (inputStream != null) {
                IOUtils.closeQuietly(inputStream);
            }
        }

        return userData;
    }

    private List<BulkImportUserData> loadExcelFile(InputStream inputStream, String fileName)
            throws InvalidBulkImportFileFormatException, IOException {

        List<BulkImportUserData> result = new ArrayList<>();

        Workbook book = null;
        if (fileName.toLowerCase().endsWith(".xls")) {
            book = new HSSFWorkbook(inputStream);
        } else if (fileName.toLowerCase().endsWith(".xlsx")) {
            book = new XSSFWorkbook(inputStream);
        }

        if (book.getNumberOfSheets() >= 1) {

            for (int iSheet = 0; iSheet < book.getNumberOfSheets(); iSheet++) {

                Sheet sheet = book.getSheetAt(iSheet);

                int iRow = sheet.getFirstRowNum();
                Row rTmp = sheet.getRow(0);

                // Check the case of trying to load an empty template (if rTmp
                // is null, it means that the template is empty because there
                // is no first row to read)
                if (rTmp == null) {
                    Utils.addStatusMessage(
                            FacesMessage.SEVERITY_WARN,
                            WebClientHelper.translate("bulk_invite_error_durring_file_reading_empty", iSheet));
                    continue;
                }

                int iCell = rTmp.getFirstCellNum();

                validateExcelColumns(sheet);

                try {
                    int nbRows = sheet.getPhysicalNumberOfRows();

                    for (int i = iRow + 1; i < nbRows; i++) {

                        Row row = sheet.getRow(i);

                        if (row != null) {

                            String username = (row.getCell(iCell) != null ? row.getCell(iCell).getStringCellValue()
                                    : "");
                            String lastname = (row.getCell(iCell + 3) != null
                                    ? row.getCell(iCell + 3).getStringCellValue()
                                    : "");
                            String email = (row.getCell(iCell + 4) != null
                                    ? row.getCell(iCell + 4).getStringCellValue()
                                    : "");
                            String profile = (row.getCell(iCell + 5) != null
                                    ? row.getCell(iCell + 5).getStringCellValue()
                                    : "");

                            BulkImportUserData tmpUser = buildBulkImportUserData(fileName, username, lastname, email,
                                    profile);
                            if (tmpUser != null) {
                                result.add(tmpUser);
                            }
                        }
                    }
                } catch (Exception e) {
                    throw new InvalidBulkImportFileFormatException(e.getMessage());
                }
            }
        }

        return result;
    }

    private void validateExcelColumns(Sheet sheet) throws InvalidBulkImportFileFormatException {

        int iRow = sheet.getFirstRowNum();

        Row row = sheet.getRow(iRow);

        int iCellFirst = row.getFirstCellNum();

        if (row.getPhysicalNumberOfCells() >= 9) {
            for (int idx = 0; idx < 9; idx++) {
                if (row.getCell(iCellFirst + idx) != null) {
                    if (!row.getCell(iCellFirst + idx).getStringCellValue().equals(definedColumns.get(idx))) {
                        throw new InvalidBulkImportFileFormatException(
                                "ERR::NoColumn:"
                                        + definedColumns.get(idx)
                                        + "|"
                                        + idx
                                        + "|"
                                        + row.getCell(iCellFirst + idx).getStringCellValue());
                        // ERR::NoColumn:<column_name>|<position>|<found>
                    }
                } else {
                    throw new InvalidBulkImportFileFormatException("ERR::NullColumn:" + idx);
                }
            }
        } else {
            throw new InvalidBulkImportFileFormatException(
                    "ERR::NumCol<9:" + row.getPhysicalNumberOfCells());
        }
    }

    private List<BulkImportUserData> loadXMLFile(InputStream inputStream, String fileName)
            throws InvalidBulkImportFileFormatException {

        XMLStreamReader xmlStreamReader = null;

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(BulkImportUsers.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();
            xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
            xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
            xmlStreamReader = xmlInputFactory.createXMLStreamReader(inputStream);
            BulkImportUsers bulkImportUsers = (BulkImportUsers) jaxbUnmarshaller.unmarshal(xmlStreamReader);

            List<BulkImportUserData> result = new ArrayList<>();

            for (BulkImportUser bulkUser : bulkImportUsers.getUserData()) {

                BulkImportUserData tmpUser = buildBulkImportUserData(
                        fileName,
                        bulkUser.getUserName(),
                        bulkUser.getLastName(),
                        bulkUser.getEmail(),
                        bulkUser.getProfile());
                if (tmpUser != null) {
                    result.add(tmpUser);
                }
            }

            return result;
        } catch (Exception e) {
            throw new InvalidBulkImportFileFormatException(e.getMessage());
        } finally {
            if (xmlStreamReader != null) {
                try {
                    xmlStreamReader.close();
                } catch (XMLStreamException e) {
                    logger.warn(
                            "Issue closing the XMLStreamReader while bulk importing users from a XML file.", e);
                }
            }
        }
    }

    private List<BulkImportUserData> loadCSVFile(InputStream inputStream, String fileName)
            throws InvalidBulkImportFileFormatException {

        BufferedReader bufferedReader = null;

        List<BulkImportUserData> result = new ArrayList<>();

        try {

            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String line = bufferedReader.readLine();

            if (line != null) {
                String[] elements = line.split(",");
                validateCSVColumns(elements);
            } else {
                throw new InvalidBulkImportFileFormatException("Cannot process null CSV file.");
            }

            while ((line = bufferedReader.readLine()) != null) {
                // AMO remove all the " from the line
                line = line.replace("\"", "");

                String[] elements = line.split(",");
                if (elements.length == 0) {
                    continue;
                }
                BulkImportUserData tmpUser = buildBulkImportUserData(fileName, elements[0], elements[3], elements[4],
                        elements[5]);
                if (tmpUser != null) {
                    result.add(tmpUser);
                }
            }

            return result;
        } catch (Exception e) {
            throw new InvalidBulkImportFileFormatException(e.getMessage());
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    logger.warn(
                            "Issue closing the BufferedReader while bulk importing users from a CSV file.", e);
                }
            }
        }
    }

    private void validateCSVColumns(String[] elements) throws InvalidBulkImportFileFormatException {

        if (elements.length < 9) {
            throw new InvalidBulkImportFileFormatException("ERR::NumCol<9:" + elements.length);
        }

        for (int idx = 0; idx < 9; idx++) {
            if (elements[idx] != null) {
                String element = elements[idx].toLowerCase().trim();
                String definedColumn = definedColumns.get(idx);
                if (!element.equals(definedColumn)) {
                    throw new InvalidBulkImportFileFormatException(
                            "ERR::NoColumn:" + definedColumns.get(idx) + "|" + idx + "|" + element);
                    // ERR::NoColumn:<column_name>|<position>|<found>
                }
            } else {
                throw new InvalidBulkImportFileFormatException("ERR::NullColumn:" + idx);
            }
        }
    }

    private BulkImportUserData buildBulkImportUserData(
            String fileName, String username, String lastname, String email, String profile) {

        BulkImportUserData tmpUser = new BulkImportUserData();

        tmpUser.setFromFile(fileName);
        tmpUser.setProfile(profile);

        if (Objects.equals(username, "") && Objects.equals(lastname, "") && Objects.equals(email, "")) {
            logger.debug("Ignoring blank line in bulk user invitation.");
            return null;
        } else {

            List<String> possibleUsers = ldapUserService.getLDAPUserIDByIdMonikerEmailCn(
                    username, username, email, lastname, true);

            if (possibleUsers != null && possibleUsers.size() == 1) {
                tmpUser.setUser(ldapUserService.getLDAPUserDataByUid(possibleUsers.get(0)));
                tmpUser.setStatus(BulkImportUserData.STATUS_OK);

                return tmpUser;
            } else {
                tmpUser.setUser(new CircabcUserDataBean());
                tmpUser.getUser().setUserName(username);
                tmpUser.getUser().setEcasUserName(username);
                tmpUser.getUser().setEmail(email);
                tmpUser.setStatus(BulkImportUserData.STATUS_IGNORE);

                return tmpUser;
            }
        }
    }

    private boolean containsProfileBlankName(List<BulkImportUserData> model) {

        boolean result = false;

        for (BulkImportUserData item : model) {
            if (item.getProfile() == null || item.getProfile().length() == 0) {
                result = true;
                break;
            }
        }

        return result;
    }

    private BulkImportUserData getBulkImportUserData(BulkImportUserDataModel model) {

        BulkImportUserData user = new BulkImportUserData();

        if (model.getIgRef() != null && !model.getIgRef().isEmpty()) {
            // in case the user comes from an IG
            NodeRef igRef = Converter.createNodeRefFromId(Converter.extractNodeRefId(model.getIgRef()));

            if (!secureNodeService.exists(igRef)) {
                throw new IllegalArgumentException(
                        "NodeRef '"
                                + model.getIgRef()
                                + "' given as origin for user '"
                                + model.getUsername()
                                + "' is invalid.");
            }

            String igTitle = secureNodeService.getProperty(igRef, ContentModel.PROP_TITLE).toString();
            String igName = secureNodeService.getProperty(igRef, ContentModel.PROP_NAME).toString();

            user.setIgRef(igRef);
            user.setIgName((igTitle != null ? igTitle : igName));
        } else if (model.getFromFile() != null && !model.getFromFile().isEmpty()) {
            // in case the user comes from an uploaded XLS(X), CSV or XML file
            user.setFromFile(model.getFromFile());
        } else {
            throw new IllegalArgumentException(
                    "User must come from an IG or file. Username: " + model.getUsername());
        }

        user.setExpectedUsername(model.getUsername());
        CircabcUserDataBean userDataBean = ldapUserService.getLDAPUserDataByUid(model.getUsername());

        // AMO - BUG 5031 - Fix issue if the user is not found in the LDAP
        // For example, when we use ecMoniker instead of UID!
        if (userDataBean != null) {
            user.setUser(userDataBean);
            user.setStatus(BulkImportUserData.STATUS_OK);
        } else {
            user.setUser(new CircabcUserDataBean());
            user.setStatus(BulkImportUserData.STATUS_ERROR);
        }
        user.setProfile(model.getProfileId());

        return user;
    }

    private BulkInviteData parseBulkInviteBodyJSON(String bulkInviteDataJson) {

        if (bulkInviteDataJson == null || bulkInviteDataJson.isEmpty()) {
            throw new IllegalArgumentException("The body (bulk invite data) cannot be empty.");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Provided JSON String: " + bulkInviteDataJson);
        }

        JSONParser parser = new JSONParser();

        JSONObject json;

        try {
            json = (JSONObject) parser.parse(bulkInviteDataJson);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Error when parsing the body (bulk invite data).", e);
        }

        List<BulkImportUserDataModel> bulkImportUserData;
        List<NameValue> igProfiles;

        try {
            bulkImportUserData = parseBulkImportUserDataArray((JSONArray) json.get("bulkImportUserData"));
        } catch (Exception e) {
            throw new IllegalArgumentException("'usersProfiles' must be a list of NameValue.", e);
        }

        try {
            igProfiles = parseNameValueArray((JSONArray) json.get("igProfiles"));
        } catch (Exception e) {
            throw new IllegalArgumentException("'igProfiles' must be a list of NameValue.", e);
        }

        return new BulkInviteData(bulkImportUserData, igProfiles);
    }

    private List<NameValue> parseNameValueArray(JSONArray array) {

        List<NameValue> list = new ArrayList<>();

        for (Object item : array) {
            JSONObject object = (JSONObject) item;
            list.add(new NameValue((String) object.get("name"), (String) object.get("value")));
        }

        return list;
    }

    private List<BulkImportUserDataModel> parseBulkImportUserDataArray(JSONArray array) {

        List<BulkImportUserDataModel> list = new ArrayList<>();

        for (Object item : array) {
            JSONObject object = (JSONObject) item;
            list.add(
                    new BulkImportUserDataModel(
                            (String) object.get("username"),
                            (String) object.get("igName"),
                            (String) object.get("igRef"),
                            (String) object.get("fromFile"),
                            (String) object.get("email"),
                            (String) object.get("status"),
                            (String) object.get("profileId")));
        }

        return list;
    }

    @Override
    public List<User> retrieveUserList(List<User> users) {
        List<User> result = new ArrayList<>();
        for (User user : users) {
            if (user.getUserId() != null && !Objects.equals(user.getUserId(), "")) {
                result.add(usersUserIdGet(user.getUserId()));
            }
        }

        return result;
    }

    private User toSwaggerUser(CircabcUserDataBean ldapUser) {
        User u = new User();
        u.setUserId(ldapUser.getUserName());
        u.setFirstname(ldapUser.getFirstName());
        u.setLastname(ldapUser.getLastName());
        u.setEmail(ldapUser.getEmail());
        u.setPhone(ldapUser.getPhone());

        Map<String, String> properties = new HashMap<>();
        properties.put("ecMoniker", ldapUser.getEcasUserName());
        properties.put(TITLE, ldapUser.getTitle());
        properties.put(ORGANISATION, ldapUser.getOrgdepnumber());
        properties.put(POSTAL_ADDRESS, ldapUser.getPostalAddress());
        properties.put(DESCRIPTION, ldapUser.getDescription());
        properties.put("fax", ldapUser.getFax());
        properties.put("domain", ldapUser.getDomain());
        u.setProperties(properties);

        return u;
    }

    @Override
    public void saveUserPreferenceConfiguration(String username, String preference) {
        NodeRef personRef = userService.getPerson(username);
        secureNodeService.setProperty(personRef, UserModel.PROP_PREFERENCE, preference);
    }

    @Override
    public PreferenceConfiguration getUserPreference(String username) {
        NodeRef personRef = userService.getPerson(username);
        PreferenceConfiguration preference = new PreferenceConfiguration();
        if (personRef != null) {
            Serializable preferenceObj = secureNodeService.getProperty(personRef, UserModel.PROP_PREFERENCE);
            if (preferenceObj != null) {
                String preferenceString = preferenceObj.toString();
                preference = UserJsonParser.parsePreference(preferenceString);
                if (!preferenceString.contains(TITLE)) {
                    preference.getLibrary().getColumn().setTitle(false);
                }
                if (!preferenceString.contains("securityRanking")) {
                    preference.getLibrary().getColumn().setSecurityRanking(false);
                }
            }
        }

        return preference;
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }
}
