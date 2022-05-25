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
package eu.cec.digit.circabc.repo.user;

import static org.alfresco.model.ContentModel.PROP_USERNAME;

import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.configuration.ConfigurableService;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.TicketComponent;
import org.alfresco.service.Auditable;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.web.bean.repository.Repository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import eu.cec.digit.circabc.config.CircabcConfiguration;
import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.model.ProfileModel;
import eu.cec.digit.circabc.model.UserModel;
import eu.cec.digit.circabc.service.app.CircabcService;
import eu.cec.digit.circabc.service.cmr.security.CircabcConstant;
import eu.cec.digit.circabc.service.profile.CategoryProfileManagerService;
import eu.cec.digit.circabc.service.profile.CircabcRootProfileManagerService;
import eu.cec.digit.circabc.service.profile.IGRootProfileManagerService;
import eu.cec.digit.circabc.service.profile.Profile;
import eu.cec.digit.circabc.service.profile.ProfileException;
import eu.cec.digit.circabc.service.profile.ProfileManagerServiceFactory;
import eu.cec.digit.circabc.service.profile.permissions.UserPermissions;
import eu.cec.digit.circabc.service.struct.ManagementService;
import eu.cec.digit.circabc.service.user.LdapUserService;
import eu.cec.digit.circabc.service.user.SearchResultRecord;
import eu.cec.digit.circabc.service.user.UserCategoryMembershipRecord;
import eu.cec.digit.circabc.service.user.UserCategoryMembershipRecordComparator;
import eu.cec.digit.circabc.service.user.UserIGMembershipRecord;
import eu.cec.digit.circabc.service.user.UserIGMembershipRecordComparator;
import eu.cec.digit.circabc.service.user.UserService;
import eu.cec.digit.circabc.util.CircabcUserDataBean;
import eu.cec.digit.circabc.util.CircabcUserDataFillerUtil;
import eu.cec.digit.circabc.web.validator.PasswordValidator;

/**
 * It is a spring bean that manages basic operations over CircaUsers like reading, updating,
 * deleting and creating.
 *
 * @author atadian - Trasys @TODO redefine the password for the new user created
 * <p>Migration 3.1 -> 3.4.6 - 02/12/2011 I18NUtil was moved to Spring. This class seems to be
 * developed for CircaBC
 */
public class UserServiceImpl implements UserService {

    /**
     * Logger
     */
    private static final Log logger = LogFactory.getLog(UserServiceImpl.class);
    /**
     * The default expiration number of days after the self registration process. This value should be
     * used only if the related properties is not found
     */
    private static final int DEFAULT_EXIPRATION_NUMBER_OF_DAYS = 60;
    private static final String CI_CIRCA_IGROOT_MASTER_GROUP = "@ci\\:circaIGRootMasterGroup:";
    private static final String CI_CIRCA_CATEGORY_MASTER_GROUP = "@ci\\:circaCategoryMasterGroup:";
    private static final String GROUP_PREFIX = "GROUP_";
    private static final String GROUP_CIRCA_BC_MASTER_GROUP = "GROUP_CircaBC--MasterGroup";
    private static final String MASTER_GROUP = "--MasterGroup--";
    private static final String MASTER_GROUP_REVERSE = "--puorGretsaM--";
    /**
     * PersonService reference
     */
    private PersonService personService;
    /**
     * NodeService reference (not secure)
     */
    private NodeService nodeService;
    /**
     * PermissionService reference
     */
    private PermissionService permissionService;
    /**
     * AuthenticationService reference Migration 3.1 -> 3.4.6 - 02/12/2011 Changed to the
     * MutableAuthenticationService
     */
    private MutableAuthenticationService authenticationService;
    /**
     * ticketComponent
     */
    private TicketComponent ticketComponent;
    /**
     * The search service reference
     */
    private SearchService searchService;
    /**
     * ManagementService reference
     */
    private ManagementService managementService;
    /**
     * configurableService reference
     */
    private ConfigurableService configurableService;
    /**
     * namespaceService reference
     */
    private NamespaceService namespaceService;
    /**
     * ldapService reference
     */
    private LdapUserService ldapUserService;
    
    /**
     * luceneUserService reference
     */
    private LdapUserService luceneUserService;
    /**
     * ownableService reference
     */
    private OwnableService ownableService;
    private String redirectUrlAfterLogout;
    /**
     * profileManagerServiceFactory reference
     */
    private transient ProfileManagerServiceFactory profileManagerServiceFactory;
    private NodeRef peopleRef = null;
    private AuthorityService authorityService;
    private CircabcService circabcService;

    public void updateMissingLastNamePersons() {
        String nullLastNamePersons =
                "ISNULL:\"cm:lastName\" AND TYPE:\"{http://www.alfresco.org/model/content/1.0}person\"";
        ResultSet resultSet = null;
        try {
            final SearchParameters sp = new SearchParameters();
            sp.setLanguage(SearchService.LANGUAGE_LUCENE);
            sp.setQuery(nullLastNamePersons);
            sp.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
            resultSet = searchService.query(sp);
            for (final ResultSetRow row : resultSet) {
                final String name = (String) row.getValue(ContentModel.PROP_USERNAME);
                // do not wont to update alfresco technical account guest and admin
                if (!(name.equalsIgnoreCase(CircabcConstant.GUEST_AUTHORITY)
                        || name.equalsIgnoreCase("admin"))) {
                    final CircabcUserDataBean user = getLDAPUserDataByUid(name);
                    if (user == null) {
                        if (logger.isWarnEnabled()) {
                            logger.warn("user " + name + " does not exists");
                        }
                    } else {
                        updateUser(user);
                    }
                }
            }
        } catch (final Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error when updating missing last name persons", e);
            }
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }
    }

    /**
     * Get or create the node used to store user preferences. Utilises the 'configurable' aspect on
     * the Person linked to this user.
     */
    @Deprecated // see eu.cec.digit.circabc.business.helper.UserManager
    private synchronized NodeRef getUserPreferencesRefCreate(final NodeRef person) {
        NodeRef prefRef = null;

        if (!nodeService.hasAspect(person, ApplicationModel.ASPECT_CONFIGURABLE)) {

            // create the configuration folder for this Person node
            configurableService.makeConfigurable(person);
        }

        // target of the assoc is the configurations folder ref
        final NodeRef configRef = configurableService.getConfigurationFolder(person);
        if (configRef == null) {
            throw new IllegalStateException(
                    "Unable to find associated 'configurations' folder for node: " + person);
        }

        final String xpath = NamespaceService.APP_MODEL_PREFIX + ":" + "preferences";
        final List<NodeRef> nodes =
                searchService.selectNodes(configRef, xpath, null, namespaceService, false);

        if (nodes.size() == 1) {
            prefRef = nodes.get(0);
        } else {
            // create the preferences Node for this user
            final ChildAssociationRef childRef =
                    nodeService.createNode(
                            configRef,
                            ContentModel.ASSOC_CONTAINS,
                            QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "preferences"),
                            ContentModel.TYPE_CMOBJECT);

            prefRef = childRef.getChildRef();
        }
        return prefRef;
    }

    /**
     * Get or create the node used to store user preferences. Utilises the 'configurable' aspect on
     * the Person linked to this user.
     */
    @Deprecated // see eu.cec.digit.circabc.business.helper.UserManager
    private NodeRef getUserPreferencesRefDoNotCreate(final NodeRef person) {
        NodeRef prefRef = null;

        if (!nodeService.hasAspect(person, ApplicationModel.ASPECT_CONFIGURABLE)) {
            return null;
        }

        // target of the assoc is the configurations folder ref
        final NodeRef configRef = configurableService.getConfigurationFolder(person);
        if (configRef == null) {
            throw new IllegalStateException(
                    "Unable to find associated 'configurations' folder for node: " + person);
        }

        final String xpath = NamespaceService.APP_MODEL_PREFIX + ":" + "preferences";
        final List<NodeRef> nodes =
                searchService.selectNodes(configRef, xpath, null, namespaceService, false);

        if (nodes.size() == 1) {
            prefRef = nodes.get(0);
        } else {
            // create the preferences Node for this user
            final ChildAssociationRef childRef =
                    nodeService.createNode(
                            configRef,
                            ContentModel.ASSOC_CONTAINS,
                            QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "preferences"),
                            ContentModel.TYPE_CMOBJECT);

            prefRef = childRef.getChildRef();
        }

        return prefRef;
    }

    @Deprecated // see eu.cec.digit.circabc.business.helper.UserManager
    public Serializable getPreference(final NodeRef person, final QName preferenceQname) {
        final NodeRef prefRef = getUserPreferencesRefDoNotCreate(person);

        if (prefRef == null) {
            return null;
        } else {
            if (UserService.PREF_INTERFACE_LANGUAGE.equals(preferenceQname)) {
                final Serializable value = nodeService.getProperty(prefRef, preferenceQname);
                if (value instanceof Locale) {
                    final Locale locale = (Locale) value;
                    return locale.getLanguage();
                } else {
                    return value;
                }
            }
            return nodeService.getProperty(prefRef, preferenceQname);
        }
    }

    @Deprecated // see eu.cec.digit.circabc.business.helper.UserManager
    public void setPreference(
            final NodeRef person, final QName preferenceQname, final Serializable value) {
        final NodeRef prefRef = getUserPreferencesRefCreate(person);

        if (UserService.PREF_INTERFACE_LANGUAGE.equals(preferenceQname)) {
            if (value instanceof Locale) {
                final Locale locale = (Locale) value;
                nodeService.setProperty(prefRef, preferenceQname, locale.getLanguage());
            } else {
                nodeService.setProperty(prefRef, preferenceQname, value);
            }
        } else {
            nodeService.setProperty(prefRef, preferenceQname, value);
        }
    }

    /**
     * Creates a new User in Alfresco with the Circabc Aspect
     *
     * @param pCircabcUser the data of the new user
     */
    public NodeRef createUser(final CircabcUserDataBean pCircabcUser) {
        // TODO : Manage correctly the creation Date in circauseraspect -
        // DIGIT-CIRCABC-213

        // create the node to represent the Person
        final NodeRef newPerson = this.personService.createPerson(pCircabcUser.getAttributesAsMap());

        // fix DIGIT-CIRCABC-407
        char[] newPassword = null;
        try {
            newPassword = new PasswordValidator().generate();
        } catch (final NoSuchAlgorithmException e) {
            if (logger.isErrorEnabled()) {
                logger.error("NoSuchAlgorithmException");
            }
            throw new RuntimeException("Can not generate password Algoritam does not exists");
        }

        final String password =
                (pCircabcUser.getPassword() == null) ? new String(newPassword) : pCircabcUser.getPassword();

        if (!authenticationService.authenticationExists(pCircabcUser.getUserName())) {
            authenticationService.createAuthentication(
                    pCircabcUser.getUserName(), password.toCharArray());
        }

        // ensure the user can access their own Person object
        this.permissionService.setPermission(
                newPerson, pCircabcUser.getUserName(), permissionService.getAllPermission(), true);

        // add the circa aspect
        nodeService.addAspect(
                newPerson, UserModel.TYPE_CIRCA_ASPECT, pCircabcUser.getAspectAttributesInMap());

        return newPerson;
    }

    public NodeRef createUser(final CircabcUserDataBean pCircabcUser, final boolean enabled) {
        // create the user
        final NodeRef userNodeRef = createUser(pCircabcUser);

        // Allow guest to read circabc profile
        permissionService.setPermission(
                userNodeRef,
                CircabcConstant.GUEST_AUTHORITY,
                UserPermissions.PERSONINFOREAD.toString(),
                true);

        // set him enbled or not. If not the user can't authenticate himself.
        authenticationService.setAuthenticationEnabled(pCircabcUser.getUserName(), enabled);

        return userNodeRef;
    }

    /**
     * Creates a new User in Alfresco with the Circabc Aspect
     *
     * @param user id
     * 
     * @param enabled
     * 
     */
    public NodeRef createLdapUser(final String userId, final boolean enabled ) {
        String authority = userId;

        // clean user id
        if (authority.contains("@")) {
            authority = authority.substring(0, authority.indexOf('@'));
        }

        final CircabcUserDataBean user = new CircabcUserDataBean();
        user.setUserName(authority);
        final CircabcUserDataBean ldapUserDetail = ldapUserService.getLDAPUserDataByUid(authority);
        user.copyLdapProperties(ldapUserDetail);

        // initialize other properties.
        user.setCompanyId("");
        user.setURL("");
        user.setVisibility(Boolean.FALSE);
        user.setGlobalNotification(Boolean.TRUE);
        user.setLastLoginTime(new Date());
        user.setLastModificationDetailsTime(new Date());
        user.setCreationDate(new Date());

        return createUser(user, enabled);
    }

    /**
     * Update the data of a Circabc user
     *
     * @param pCircabcUser         Circabc user
     * @param pNonAspectProperties true if you want to udpate just the NonAspect Properties (the one
     *                             that a user is not allow change and are changed by the batch process)
     */
    public void updateUser(
            final CircabcUserDataBean pCircabcUser, final boolean pNonAspectProperties) {
        // Get the node to represent the Person
        final NodeRef person = this.personService.getPerson(pCircabcUser.getUserName());

        // update the properties
        Map<QName, Serializable> prop = null;
        if (pNonAspectProperties) {
            // modify only if modify time is before time from batch
            final Date lastModify =
                    (Date) nodeService.getProperty(person, UserModel.PROP_LAST_MODIFICATION_DETAILS_TIME);
            if (lastModify.before(pCircabcUser.getLastModificationDetailsTime())) {
                prop = nodeService.getProperties(person);
                prop.putAll(pCircabcUser.getAttributesAsMap());
                prop.put(UserModel.PROP_LAST_MODIFICATION_DETAILS_TIME, new Date());
                nodeService.setProperties(person, prop);
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug(
                            pCircabcUser.getUserName()
                                    + " not updated. Last modify: "
                                    + lastModify
                                    + " . (date in import file("
                                    + pCircabcUser.getLastModificationDetailsTime()
                                    + ")");
                }
            }
        } else {
            prop = nodeService.getProperties(person);
            prop.putAll(pCircabcUser.getAttributesAsMap());
            prop.put(UserModel.PROP_LAST_MODIFICATION_DETAILS_TIME, new Date());
            nodeService.setProperties(person, prop);
        }
    }

    /**
     * Update all the data of a Circabc user
     *
     * @param pCircabcUser Circabc user
     */
    public void updateUser(final CircabcUserDataBean pCircabcUser) {
        this.updateUser(pCircabcUser, false);
    }

    /**
     * It deletes a Circabc user on alfresco
     *
     * @param pUserName the user to delete
     * @throws AuthenticationException any error
     */
    public void deleteUser(final String pUserName)
            throws AuthenticationException { // TODO check if consistent
        try {
            authenticationService.deleteAuthentication(pUserName);
        } catch (final AuthenticationException authErr) {
            if (logger.isErrorEnabled()) {
                logger.error(authErr);
            }
        }
        this.personService.deletePerson(pUserName);
    }

    @Deprecated // see eu.cec.digit.circabc.business.helper.UserManager
    public boolean isEmailExists(
            final String email, final boolean excludeDataInTheCurrentTransaction) {
        if (email == null || email.length() < 1) {
            return false;
        }

        boolean emailExists = false;

        final SearchParameters sp = new SearchParameters();
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery(
                "TYPE:\\{http\\://www.alfresco.org/model/content/1.0\\}person +@cm\\:email:\""
                        + email
                        + "\"");

        if (peopleRef == null) {
            peopleRef = personService.getPeopleContainer();
        }

        sp.addStore(peopleRef.getStoreRef());
        sp.excludeDataInTheCurrentTransaction(excludeDataInTheCurrentTransaction);

        ResultSet rs = null;
        try {
            rs = searchService.query(sp);

            if (rs.length() != 0) {
                emailExists = true;
            }
        } catch (final Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error(e);
            }
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (final Exception e2) {
                    if (logger.isWarnEnabled()) {
                        logger.warn("ResultSet Close Exception", e2);
                    }
                }
            }
        }

        return emailExists;
    }

    @Deprecated // not longer a production feature
    public int getAccountExpirationDays() {
        final String numberStr =
                CircabcConfiguration.getProperty(CircabcConfiguration.EXPIRATION_TIME_PROPERTIES);

        int response = DEFAULT_EXIPRATION_NUMBER_OF_DAYS;

        if (numberStr != null && numberStr.length() > 1) {
            try {
                response = Integer.parseInt(numberStr);
            } catch (final NumberFormatException ex) {
                if (logger.isErrorEnabled()) {
                    logger.error(
                            "The property file "
                                    + CircabcConfiguration.DEFAULT_PROPERTY_FILE
                                    + " is corrupted. The value of the key "
                                    + CircabcConfiguration.EXPIRATION_TIME_PROPERTIES
                                    + " must be a valid integer and not "
                                    + numberStr
                                    + "\nPlease correct the problem and restart the server. For instance the value used by default is "
                                    + DEFAULT_EXIPRATION_NUMBER_OF_DAYS);
                }
            }
        } else {
            if (logger.isErrorEnabled()) {
                logger.error(
                        "The property file "
                                + CircabcConfiguration.DEFAULT_PROPERTY_FILE
                                + " not complete. The value of the key "
                                + CircabcConfiguration.EXPIRATION_TIME_PROPERTIES
                                + " must be set and must be a valid integer and not "
                                + "\nPlease correct the problem and restart the server. For instance the value used by default is "
                                + DEFAULT_EXIPRATION_NUMBER_OF_DAYS);
            }
        }

        return response;
    }

    public void setAuthenticationService(final MutableAuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public void setNodeService(final NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setPermissionService(final PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    public void setPersonService(final PersonService personService) {
        this.personService = personService;
    }

    /**
     * @param ManagementService the ManagementService to set
     */
    public final void setManagementService(final ManagementService managementService) {
        this.managementService = managementService;
    }

    /**
     * @param authorityService the authorityService to set
     */
    public final void setAuthorityService(final AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    /**
     * @return the searchService
     */
    public SearchService getSearchService() {
        return searchService;
    }

    /**
     * @param searchService the searchService to set
     */
    public void setSearchService(final SearchService searchService) {
        this.searchService = searchService;
    }

    /**
     * @param configurableService the configurableService to set
     */
    public void setConfigurableService(final ConfigurableService configurableService) {
        this.configurableService = configurableService;
    }

    /**
     * @param namespaceService the namespaceService to set
     */
    public void setNamespaceService(final NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

    /**
     * @param LdapUserService the ldapService to set
     */
    public void setLdapService(final LdapUserService ldapService) {
        this.ldapUserService = ldapService;
    }

    public void setPassword(final String pUserName, final char[] pNewPassword)
            throws AuthenticationException {
        this.authenticationService.setAuthentication(pUserName, pNewPassword);
    }

    public String getUserEmail(final String pUserName) {
        final NodeRef nodeRef = getPerson(pUserName);
        return DefaultTypeConverter.INSTANCE.convert(
                String.class, nodeService.getProperty(nodeRef, ContentModel.PROP_EMAIL));
    }

    public String getUserDomain(final String pUserName) {
        final NodeRef nodeRef = getPerson(pUserName);
        final Serializable domain = nodeService.getProperty(nodeRef, UserModel.PROP_DOMAIN);
        return (domain == null) ? null : domain.toString();
    }

    public String getUserFullName(final String pUserName) {
        final NodeRef nodeRef = getPerson(pUserName);
        final String firstName =
                DefaultTypeConverter.INSTANCE.convert(
                        String.class, nodeService.getProperty(nodeRef, ContentModel.PROP_FIRSTNAME));
        final String lastName =
                DefaultTypeConverter.INSTANCE.convert(
                        String.class, nodeService.getProperty(nodeRef, ContentModel.PROP_LASTNAME));
        return ((firstName == null) ? "" : firstName + " ") + ((lastName == null) ? "" : lastName);
    }

    public NodeRef getPerson(final String pUserName) {
        return personService.getPerson(pUserName);
    }

    public String getUserByEmail(final String email) {
        String userName = null;

        final SearchParameters sp = new SearchParameters();
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery(
                "TYPE:\\{http\\://www.alfresco.org/model/content/1.0\\}person +@cm\\:email:\""
                        + email
                        + "\"");

        sp.addStore(personService.getPeopleContainer().getStoreRef());
        sp.excludeDataInTheCurrentTransaction(false);

        ResultSet rs = null;
        try {
            rs = searchService.query(sp);

            if (rs.length() != 0) {
                final NodeRef person = rs.getNodeRef(0);
                userName = (String) nodeService.getProperty(person, ContentModel.PROP_USERNAME);
            }
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (final Exception e2) {
                    if (logger.isWarnEnabled()) {
                        logger.warn("ResultSet Close Exception", e2);
                    }
                }
            }
        }

        return userName;
    }

    public List<UserCategoryMembershipRecord> getCategories(final String pUserName) {
        if (circabcService.readFromDatabase()) {
            final List<UserCategoryMembershipRecord> result = circabcService.getCategories(pUserName);
            Collections.sort(result, UserCategoryMembershipRecordComparator.getInstance());
            return result;
        } else {
            return getCategoriesOldWay(pUserName);
        }
    }

    private List<UserCategoryMembershipRecord> getCategoriesOldWay(final String pUserName) {
        final List<UserCategoryMembershipRecord> result = new ArrayList<>();
        final Set<String> authorities = authorityService.getAuthoritiesForUser(pUserName);
        for (final String authority : authorities) {
            if (authority.contains(MASTER_GROUP) || authority.contains(MASTER_GROUP_REVERSE)) {
                // ignore CIRCABC root
                if (authority.startsWith(GROUP_CIRCA_BC_MASTER_GROUP)) {
                    continue;
                }
                final String searchItem = authority.replace(GROUP_PREFIX, "");
                final NodeRef categoNodeRef = getCategoryNoderef(searchItem);
                if (categoNodeRef != null) {
                    String category =
                            upFirstLetterInString(nodeService.getProperty(categoNodeRef, ContentModel.PROP_NAME));
                    final String categoryTitle =
                            (String) nodeService.getProperty(categoNodeRef, ContentModel.PROP_TITLE);
                    if (categoryTitle != null && categoryTitle.length() > 0) {
                        category = upFirstLetterInString(categoryTitle);
                    }
                    final String profile =
                            getProfileManagerServiceFactory()
                                    .getCategoryProfileManagerService()
                                    .getPersonProfile(categoNodeRef, pUserName);
                    if ((profile != null)
                            && !profile.equals(CircabcRootProfileManagerService.ALL_CIRCA_USERS_PROFILE_NAME)) {
                        result.add(new UserCategoryMembershipRecord(category, profile, categoNodeRef.getId()));
                    }
                }
            }
        }

        Collections.sort(result, UserCategoryMembershipRecordComparator.getInstance());
        return result;
    }

    private NodeRef getCategoryNoderef(final String searchItem) {

        NodeRef result = null;
        final SearchParameters sp = new SearchParameters();
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.addStore(Repository.getStoreRef());
        sp.setQuery(CI_CIRCA_CATEGORY_MASTER_GROUP + "\"" + searchItem + "\"");

        ResultSet rs = null;
        try {
            rs = searchService.query(sp);
            if (rs.length() != 0) {
                result = rs.getRow(0).getNodeRef();
            }
        } catch (final Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error when serching for  interest group " + searchItem, e);
            }
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (final Exception e2) {
                    if (logger.isWarnEnabled()) {
                        logger.warn("ResultSet Close Exception", e2);
                    }
                }
            }
        }
        return result;
    }

    public List<UserIGMembershipRecord> getInterestGroups(
            final String pUserName, List<NodeRef> categories) {
        return getFileteredIGs(pUserName, categories);
    }

    public List<UserIGMembershipRecord> getInterestGroups(final String pUserName) {
        if (circabcService.readFromDatabase()) {
            final List<UserIGMembershipRecord> interestGroups =
                    circabcService.getInterestGroups(pUserName);
            Collections.sort(interestGroups, UserIGMembershipRecordComparator.getInstance());
            return interestGroups;
        } else {
            return getFileteredIGs(pUserName, null);
        }
    }

    private List<UserIGMembershipRecord> getFileteredIGs(
            final String pUserName, final List<NodeRef> categories) {
        final List<UserIGMembershipRecord> result = new ArrayList<>();

        AuthenticationUtil.runAs(
                new AuthenticationUtil.RunAsWork<Object>() {
                    public Object doWork() {

                        final Set<String> authorities = authorityService.getAuthoritiesForUser(pUserName);

                        final IGRootProfileManagerService rootProfileManagerService =
                                getProfileManagerServiceFactory().getIGRootProfileManagerService();

                        for (final String authority : authorities) {
                            if (authority.contains(MASTER_GROUP) || authority.contains(MASTER_GROUP_REVERSE)) {
                                // ignore CIRCABC root
                                if (authority.startsWith(GROUP_CIRCA_BC_MASTER_GROUP)) {
                                    continue;
                                }
                                final String searchItem = authority.replace(GROUP_PREFIX, "");
                                final NodeRef igRootNodeRef = getIGNoderef(searchItem);
                                if (igRootNodeRef != null) {
                                    final NodeRef categoNodeRef =
                                            nodeService.getPrimaryParent(igRootNodeRef).getParentRef();
                                    boolean isFilterMatch = true;

                                    if (categories != null
                                            && !categories.isEmpty()
                                            && !categories.contains(categoNodeRef)) {
                                        // a category filter was applied but this category does not match
                                        isFilterMatch = false;
                                    }

                                    if (isFilterMatch) {
                                        // TODO: Consider the provided language, if present
                                        // If language is invalid, return English as default
                                        // String language = null; // Remove this when parameter is provided

                                        final String category =
                                                upFirstLetterInString(
                                                        nodeService.getProperty(categoNodeRef, ContentModel.PROP_NAME));
                                        final String categoryTitle =
                                                upFirstLetterInString(
                                                        nodeService.getProperty(categoNodeRef, ContentModel.PROP_TITLE));
                                        final String interesGroup =
                                                upFirstLetterInString(
                                                        nodeService.getProperty(igRootNodeRef, ContentModel.PROP_NAME));
                                        final String interesGroupTitle =
                                                upFirstLetterInString(
                                                        nodeService.getProperty(igRootNodeRef, ContentModel.PROP_TITLE));
                                        final String profileName =
                                                rootProfileManagerService.getPersonProfile(igRootNodeRef, pUserName);
                                        final Profile profile =
                                                rootProfileManagerService.getProfile(igRootNodeRef, profileName);
                                        final String profileDisplayName = profile.getProfileDisplayName();
                                        result.add(
                                                new UserIGMembershipRecord(
                                                        igRootNodeRef.getId(),
                                                        interesGroup,
                                                        categoNodeRef.getId(),
                                                        category,
                                                        profileName,
                                                        categoryTitle,
                                                        interesGroupTitle,
                                                        profileDisplayName));
                                        if (profile.isExported()) {
                                            List<AssociationRef> targetAssocs =
                                                    nodeService.getTargetAssocs(
                                                            profile.getNodeRef(), ProfileModel.ASSOC_PROFILE_IMPORTED_TO);
                                            for (AssociationRef item : targetAssocs) {
                                                NodeRef importedNodeRef = item.getTargetRef();

                                                final String importedProfileName =
                                                        (String)
                                                                nodeService.getProperty(
                                                                        importedNodeRef, ProfileModel.PROP_IG_ROOT_PROFILE_NAME);
                                                List<ChildAssociationRef> parentAssocs =
                                                        nodeService.getParentAssocs(
                                                                importedNodeRef,
                                                                ProfileModel.ASSOC_IG_ROOT_PROFILE,
                                                                RegexQNamePattern.MATCH_ALL);
                                                if (parentAssocs.size() > 0) {
                                                    final NodeRef importedIgNodeRef = parentAssocs.get(0).getParentRef();
                                                    final String importedInteresGroup =
                                                            upFirstLetterInString(
                                                                    (String)
                                                                            nodeService.getProperty(
                                                                                    importedIgNodeRef, ContentModel.PROP_NAME));
                                                    final String importedInteresGroupTitle =
                                                            upFirstLetterInString(
                                                                    nodeService.getProperty(
                                                                            importedIgNodeRef, ContentModel.PROP_TITLE));
                                                    result.add(
                                                            new UserIGMembershipRecord(
                                                                    importedIgNodeRef.getId(),
                                                                    importedInteresGroup,
                                                                    categoNodeRef.getId(),
                                                                    category,
                                                                    importedProfileName,
                                                                    categoryTitle,
                                                                    importedInteresGroupTitle,
                                                                    interesGroup + ":" + profileDisplayName));
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        return null;
                    }
                },
                AuthenticationUtil.getSystemUserName());
        Collections.sort(result, UserIGMembershipRecordComparator.getInstance());
        return result;
    }

    private String upFirstLetterInString(Serializable stringOrMLText) {
        String myString = null;
        if (stringOrMLText instanceof String) {
            myString = (String) stringOrMLText;
        } else if (stringOrMLText instanceof MLText) {
            myString = ((MLText) stringOrMLText).getDefaultValue();
        }

        String result = "";
        if (myString != null && myString.length() > 0) {
            result = myString.substring(0, 1).toUpperCase() + myString.substring(1);
        }
        return result;
    }

    private NodeRef getIGNoderef(final String searchItem) {

        NodeRef result = null;

        Locale previousLocal = I18NUtil.getLocale();
        I18NUtil.setLocale(new Locale("en"));
        final SearchParameters sp = new SearchParameters();
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.addStore(new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore"));
        sp.setQuery(CI_CIRCA_IGROOT_MASTER_GROUP + "\"" + searchItem + "\"");

        ResultSet rs = null;
        try {
            rs = searchService.query(sp);
            if (rs.length() != 0) {
                result = rs.getRow(0).getNodeRef();
            }
        } catch (final Exception e) {

            if (logger.isErrorEnabled()) {
                logger.error("Error when serching for  interest group " + searchItem, e);
            }

        } finally {
            I18NUtil.setLocale(previousLocal);
            if (rs != null) {
                try {
                    rs.close();
                } catch (final Exception e2) {
                    if (logger.isWarnEnabled()) {
                        logger.warn("ResultSet Close Exception", e2);
                    }
                }
            }
        }
        return result;
    }

    public List<NodeRef> getEventRootNodes(final String pUserName) {
        final List<NodeRef> result = new ArrayList<>();
        final Set<String> authorities = authorityService.getAuthoritiesForUser(pUserName);
        for (String authority : authorities) {
            if (authority.contains(MASTER_GROUP) || authority.contains(MASTER_GROUP_REVERSE)) {
                // ignore CIRCABC root
                if (authority.startsWith(GROUP_CIRCA_BC_MASTER_GROUP)) {
                    continue;
                }
                final String searchItem = authority.replace(GROUP_PREFIX, "");
                final NodeRef igRootNodeRef = getIGNoderef(searchItem);
                if (igRootNodeRef != null) {
                    final NodeRef eventRoot = getChildByAspect(igRootNodeRef, CircabcModel.ASPECT_EVENT_ROOT);
                    if (eventRoot != null) {
                        result.add(eventRoot);
                    }
                }
            }
        }
        return result;
    }

    /**
     * @param nodeRef
     * @param service
     * @param permission
     * @return
     */
    public Set<String> getUsersWithPermission(final NodeRef nodeRef, final String permission) {
        final Set<String> users = new HashSet<>();

        final Set<AccessPermission> accessPermissions = permissionService.getAllSetPermissions(nodeRef);

        String authority;
        for (final AccessPermission accessPermission : accessPermissions) {
            if (accessPermission.getPermission().equals(permission)
                    && accessPermission.getAccessStatus().equals(AccessStatus.ALLOWED)) {
                authority = accessPermission.getAuthority();

                final AuthorityType authType = AuthorityType.getAuthorityType(authority);

                if (authType.equals(AuthorityType.USER)) {
                    users.add(authority);
                } else if (authType.equals(AuthorityType.GROUP)
                        && authority.equals(CircabcRootProfileManagerService.ALL_CIRCA_USERS_AUTHORITY)
                        == false) {
                    if (authorityService.authorityExists(authority)) {
                        users.addAll(
                                authorityService.getContainedAuthorities(AuthorityType.USER, authority, false));
                    } else {
                        if (logger.isErrorEnabled()) {
                            logger.error("Authority does bot exists: " + authority);
                        }
                    }
                }
            }
        }

        return users;
    }

    public boolean isUserOnline(final String user) {
        return ticketComponent.getUsersWithTickets(true).contains(user);
    }

    public Set<String> getAllOnlineUsers() {
        return ticketComponent.getUsersWithTickets(true);
    }

    public Set<String> getOnlineUsers(final NodeRef igNodeRef) {
        final Set<String> onlineUsers = getAllOnlineUsers();
        final Set<String> igOnlineUser =
                getProfileManagerServiceFactory()
                        .getProfileManagerService(igNodeRef)
                        .getInvitedUsers(igNodeRef);
        onlineUsers.retainAll(igOnlineUser);
        return onlineUsers;
    }

    private NodeRef getChildByAspect(final NodeRef parent, final QName aspect) {
        final List<ChildAssociationRef> assocs = nodeService.getChildAssocs(parent);
        NodeRef ref;
        for (final ChildAssociationRef assoc : assocs) {
            ref = assoc.getChildRef();
            if (nodeService.hasAspect(ref, aspect)) {
                return ref;
            }
        }

        return null;
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
            final ProfileManagerServiceFactory profileManagerServiceFactory) {
        this.profileManagerServiceFactory = profileManagerServiceFactory;
    }

    public String getUserNameByEmail(final String email) {
        String result = null;
        final SearchParameters sp = new SearchParameters();
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.addStore(new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore"));

        sp.setQuery(
                "TYPE:\"{http://www.alfresco.org/model/content/1.0}person\" AND @cm\\:email:"
                        + "\""
                        + email
                        + "\"");

        ResultSet rs = null;
        try {
            rs = searchService.query(sp);
            if (rs.length() != 0) {
                result = nodeService.getProperty(rs.getRow(0).getNodeRef(), PROP_USERNAME).toString();
            }
        } catch (final Exception e) {

            if (logger.isErrorEnabled()) {
                logger.error("Error when serching for user with email:" + email, e);
            }

        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (final Exception e2) {
                    if (logger.isWarnEnabled()) {
                        logger.warn("ResultSet Close Exception", e2);
                    }
                }
            }
        }

        return result;
    }

    public CircabcUserDataBean getCircabcUserDataBean(final String userName) {
        // Get the node to represent the Person
        final NodeRef person = this.personService.getPerson(userName);
        final Map<QName, Serializable> properties = nodeService.getProperties(person);
        return CircabcUserDataFillerUtil.getCircabcUserDataBean(properties);
    }

    public CircabcUserDataBean getLDAPUserDataByUid(final String pLdapUserID) {
        return ldapUserService.getLDAPUserDataByUid(pLdapUserID);
    }

    public List<String> getLDAPUserIDByMail(final String mail) {
        return ldapUserService.getLDAPUserIDByMail(mail);
    }

    public List<String> getLDAPUserIDByMailDomain(final String mail, final String domain) {
        return ldapUserService.getLDAPUserIDByMailDomain(mail, domain);
    }
    
    public List<SearchResultRecord> getUsersByMailDomain(final String mail, final String domain, boolean filter) {
    	List<SearchResultRecord> result = ldapUserService.getUsersByMailDomain(mail, domain, filter);
    	  //DIGITCIRCABC-5063 if we do a global search, search also the user in Alfresco Repo to retrieve non EULogin/Ldap users
    	if(!filter) {
    		List<SearchResultRecord> resultLucene = luceneUserService.getUsersByMailDomain(mail, domain, filter);
    		//merge the 2 lists
    		for (SearchResultRecord x: resultLucene) {
    			if(!result.contains(x)) {
    				result.add(x);
    			}
			}
        }
    	return result;
    }
      
    public List<SearchResultRecord> getUsersByDomainFirstNameLastNameEmail(
            final String pDomain, final String pCriteria, boolean filter) {
    	List<SearchResultRecord> result = ldapUserService.getUsersByDomainFirstNameLastNameEmail(pDomain, pCriteria, filter);
        //DIGITCIRCABC-5063 if we do a global search, search also the user in Alfresco Repo to retrieve non EULogin/Ldap users
    	if(!filter) {
    		List<SearchResultRecord> resultLucene = luceneUserService.getUsersByDomainFirstNameLastNameEmail(pDomain, pCriteria, filter);
    		//merge the 2 lists
    		for (SearchResultRecord x: resultLucene) {
    			if(!result.contains(x)) {
    				result.add(x);
    			}
			}
        }
    	return result;
    }

    public List<String> getLDAPUserIDByIdMonikerEmailCn(
            final String uid,
            final String moniker,
            final String email,
            final String cn,
            final boolean conjunction) {
        return ldapUserService.getLDAPUserIDByIdMonikerEmailCn(uid, moniker, email, cn, conjunction);
    }

    public LdapUserService getLdapUserService() {
        return ldapUserService;
    }
    
    public LdapUserService getLuceneUserService() {
    	return luceneUserService;
    }

    public void setLdapUserService(final LdapUserService ldapUserService) {
        this.ldapUserService = ldapUserService;
    }

    public void setLuceneUserService(final LdapUserService ldapUserService) {
        this.luceneUserService = ldapUserService;
    }
    
    public void setTicketComponent(final TicketComponent ticketComponent) {
        this.ticketComponent = ticketComponent;
    }

    public void copyUser(
            final String oldUserName,
            final String newUserName,
            boolean deleteOldUser,
            boolean copyOwnership,
            boolean copyMembership)
            throws ProfileException {
        // final boolean oldAuthorityExists =
        // authorityService.authorityExists(oldUserName);
        final boolean oldPersonExists = personService.personExists(oldUserName);
        if (!oldPersonExists) {
            throw new IllegalStateException("Person does not exists : " + oldUserName);
        }
        // final boolean newAuthorityExists =
        // authorityService.authorityExists(newUserName);
        final boolean newPersonExists = personService.personExists(newUserName);

        if (!newPersonExists) {
            CircabcUserDataBean circabcUserDataBean = getCircabcUserDataBean(oldUserName);
            circabcUserDataBean.setUserName(newUserName);
            circabcUserDataBean = getLDAPUserDataByUid(newUserName);
            createUser(circabcUserDataBean, true);
        }

        copyGroupMebership(oldUserName, newUserName);

        copyOwnership(oldUserName, newUserName);

        if (deleteOldUser) {
            personService.deletePerson(oldUserName);
        }
    }

    private void copyOwnership(String oldUserName, String newUserName) {

        String luceneQuery = null;
        luceneQuery = "@cm\\:owner:\"" + oldUserName + "\"";

        ResultSet resultSet = null;
        try {
            resultSet = executeLuceneQuery(luceneQuery);
            for (final ResultSetRow row : resultSet) {
                try {
                    ownableService.setOwner(row.getNodeRef(), newUserName);
                } catch (Exception e) {
                    if (logger.isErrorEnabled()) {
                        logger.error(
                                "Could not set owner from user : "
                                        + oldUserName
                                        + " to user :"
                                        + newUserName
                                        + " on node "
                                        + row.getNodeRef(),
                                e);
                    }
                }
            }
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }
    }

    private ResultSet executeLuceneQuery(final String query) {
        final SearchParameters sp = new SearchParameters();
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery(query);
        sp.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        return searchService.query(sp);
    }

    /**
     * @param oldUserName
     * @param newUserName
     */
    private void copyGroupMebership(String oldUserName, String newUserName) {
        IGRootProfileManagerService igs =
                getProfileManagerServiceFactory().getIGRootProfileManagerService();
        CategoryProfileManagerService categs =
                getProfileManagerServiceFactory().getCategoryProfileManagerService();

        List<UserIGMembershipRecord> igListOld = getInterestGroups(oldUserName);
        List<UserCategoryMembershipRecord> categListOld = getCategories(oldUserName);

        List<UserIGMembershipRecord> igListNew = getInterestGroups(newUserName);
        List<UserCategoryMembershipRecord> categListNew = getCategories(newUserName);

        // IGs
        for (UserIGMembershipRecord igMembershipOld : igListOld) {
            Boolean alreadyMember = false;

            for (UserIGMembershipRecord igMembershipNew : igListNew) {
                if (igMembershipNew
                        .getInterestGroupNodeId()
                        .equals(igMembershipOld.getInterestGroupNodeId())) {
                    alreadyMember = true;
                    break;
                }
            }

            if (!alreadyMember) {
                try {
                    igs.addPersonToProfile(
                            new NodeRef("workspace://SpacesStore/" + igMembershipOld.getInterestGroupNodeId()),
                            newUserName,
                            igMembershipOld.getProfile());
                } catch (ProfileException e) {
                    if (logger.isErrorEnabled()) {
                        logger.error(
                                "Problem during the copy of user from" + oldUserName + " to " + newUserName, e);
                    }
                }
            }
        }

        // categories
        for (UserCategoryMembershipRecord categMembershipOld : categListOld) {
            Boolean alreadyMember = false;

            for (UserCategoryMembershipRecord categMembershipNew : categListNew) {
                if (categMembershipNew.getCategoryNodeId().equals(categMembershipOld.getCategoryNodeId())) {
                    alreadyMember = true;
                    break;
                }
            }

            if (!alreadyMember) {
                try {
                    categs.addPersonToProfile(
                            new NodeRef("workspace://SpacesStore/" + categMembershipOld.getCategoryNodeId()),
                            newUserName,
                            categMembershipOld.getProfile());
                } catch (ProfileException e) {
                    if (logger.isErrorEnabled()) {
                        logger.error(
                                "Problem during the copy of user from" + oldUserName + " to " + newUserName, e);
                    }
                }
            }
        }
    }

    public Map<String, String> getEcasUserDomains() {
        Map<String, String> map = ldapUserService.getEcasUserDomains();
        ValueComparator bvc = new ValueComparator(map);
        TreeMap<String, String> result = new TreeMap<>(bvc);
        result.putAll(map);
        return result;
    }

    public OwnableService getOwnableService() {
        return ownableService;
    }

    public void setOwnableService(OwnableService ownableService) {
        this.ownableService = ownableService;
    }

    public boolean isUserExists(String userName) {
        return personService.personExists(userName);
    }

    /**
     * Migration 3.1 -> 3.4.6 - 02/12/2011 Added true to the getCurrentTicket method.
     */
    public String getCurrentTicket(String userName) {
        return ticketComponent.getCurrentTicket(userName, true);
    }

    @Override
    @Auditable
    public void deleteUsers(List<String> userNames) {
        for (String userName : userNames) {
            personService.deletePerson(userName);
        }
    }

    public String getRedirectUrlAfterLogout() {
        return redirectUrlAfterLogout;
    }

    public void setRedirectUrlAfterLogout(String redirectUrlAfterLogout) {
        this.redirectUrlAfterLogout = redirectUrlAfterLogout;
    }

    public CircabcService getCircabcService() {
        return circabcService;
    }

    public void setCircabcService(CircabcService circabcService) {
        this.circabcService = circabcService;
    }

    @Override
    @Auditable
    public Boolean userExists(String uid) {

        return ldapUserService.userExists(uid);
    }

    @Override
    @Auditable
    public void setAuthenticationEnabled(String userName, boolean enabled) {
        authenticationService.setAuthenticationEnabled(userName, enabled);
    }

    @Override
    @Auditable
    public boolean getAuthenticationEnabled(String userName) {
        return authenticationService.getAuthenticationEnabled(userName);
    }

    static class ValueComparator implements Comparator<String>, Serializable {

        /**
         *
         */
        private static final long serialVersionUID = 5317757060955275939L;

        Map<String, String> base;

        public ValueComparator(Map<String, String> base) {
            this.base = base;
        }

        // Note: this comparator imposes orderings that are inconsistent with equals.
        public int compare(String first, String second) {

            final String left = base.get(first);
            final String right = base.get(second);
            final String ec = "European Commission";
            // force European Commission to be first other sort as natural string
            if (left.equalsIgnoreCase(ec)) {
                return -1;
            } else if (right.equalsIgnoreCase(ec)) {
                return 1;
            } else if (left.compareTo(right) < 0) {
                return -1;
            } else {
                return 1;
            } // returning 0 would merge keys
        }
    }

    @Override
    public CircabcUserDataBean getLDAPUserDataNoFilterByUid(String userID) {
        
        return ldapUserService.getLDAPUserDataNoFilterByUid(userID);
    }
}
