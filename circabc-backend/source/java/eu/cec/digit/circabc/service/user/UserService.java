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
package eu.cec.digit.circabc.service.user;

import eu.cec.digit.circabc.util.CircabcUserDataBean;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.service.Auditable;
import org.alfresco.service.NotAuditable;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.bean.users.UserPreferencesBean;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * It is a spring bean that manages basic operations over CircabcUsers like reading, updating,
 * deleting and creating.
 *
 * @author atadian - Trasys
 * @author Yanick Pignot
 * <p>Migration 3.1 -> 3.4.6 - 02/12/2011 Commented the key parameter of the @Auditable
 * annotation. Commented the deprecated @PublicService annotation.
 */
// @PublicService
public interface UserService {

    QName PREF_CONTENT_FILTER_LANGUAGE =
            QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "content-filter-language");
    QName PREF_INTERFACE_LANGUAGE =
            QName.createQName(
                    NamespaceService.APP_MODEL_1_0_URI, UserPreferencesBean.PREF_INTERFACELANGUAGE /*
                     * this durty trick is
                     * used to avoid
                     * problems during
                     * alfresco version
                     * migration
                     * "interface-language"
                     */);

    QName PREF_SIGNATURE = QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "signature");

    /**
     * Creates a new User in Alfresco with the Circabc Aspect
     *
     * @param user id
     */
    @Auditable(/*key = Auditable.Key.RETURN, */ parameters = {"circabcUser","enabled"})
    NodeRef createLdapUser(final String userId,final boolean enabled);

    /**
     * Creates a new User in Alfresco with the Circabc Aspect
     *
     * @param pCircabcUser the data of the new user
     */
    @Auditable(/*key = Auditable.Key.RETURN, */ parameters = {"circabcUser"})
    NodeRef createUser(final CircabcUserDataBean circabcUser);

    /**
     * Creates a new User in Alfresco with the Circabc Aspect
     *
     * @param pCircabcUser the data of the new user
     * @param enabled      if the user should be enbled or not. If not, the user can't authenticate itself
     */
    @Auditable(/*key = Auditable.Key.RETURN, */ parameters = {"circabcUser", "enabled"})
    NodeRef createUser(final CircabcUserDataBean circabcUser, final boolean enabled);

    /**
     * It deletes a Circabc user on alfresco
     *
     * @param pCircabcUser the user to delete
     * @throws AuthenticationException any error
     */
    @Auditable(/*key = Auditable.Key.NO_KEY, */ parameters = {"pUserName"})
    void deleteUser(final String pUserName) throws AuthenticationException;

    /**
     * @return the number of days in which the account will be expired if the account is not activated
     * after the self registration process
     */
    @NotAuditable
    int getAccountExpirationDays();

    /**
     * @return list of category, profile for given user
     */
    @Auditable(/*key = Auditable.Key.NO_KEY*/)
    List<UserCategoryMembershipRecord> getCategories(final String pUserName);

    @Auditable(/*key = Auditable.Key.NO_KEY*/)
    CircabcUserDataBean getCircabcUserDataBean(final String userName);

    /**
     * @return list event roots for given user
     */
    @Auditable(/*key = Auditable.Key.NO_KEY*/)
    List<NodeRef> getEventRootNodes(final String pUserName);

    /**
     * @return list of interest groups for given user
     */
    @Auditable(/*key = Auditable.Key.NO_KEY*/)
    List<UserIGMembershipRecord> getInterestGroups(final String pUserName);

    /**
     * @return list of interest groups for given user and categories
     */
    @Auditable(/*key = Auditable.Key.NO_KEY*/)
    List<UserIGMembershipRecord> getInterestGroups(final String userName, List<NodeRef> categories);

    /**
     * LDAP Implementation
     */
    @Auditable(/*key = Auditable.Key.NO_KEY, */ parameters = {"pLdapUserID"})
    CircabcUserDataBean getLDAPUserDataByUid(final String pLdapUserID);

    @Auditable(
            /*key = Auditable.Key.NO_KEY, */ parameters = {"mail", "uid", "moniker", "cn", "conjunction"})
    List<String> getLDAPUserIDByIdMonikerEmailCn(
            final String uid,
            final String moniker,
            final String email,
            final String cn,
            final boolean conjunction);

    @Auditable(/*key = Auditable.Key.NO_KEY, */ parameters = {"pMail"})
    List<String> getLDAPUserIDByMail(final String mail);

    @Auditable(/*key = Auditable.Key.NO_KEY, */ parameters = {"mail", "domain"})
    List<String> getLDAPUserIDByMailDomain(final String mail, final String domain);

    /**
     * Get the noderef of the user
     *
     * @param pUserName the user
     */
    @Auditable(/*key = Auditable.Key.NO_KEY, */ parameters = {"pUserName"})
    NodeRef getPerson(final String pUserName);

    /**
     * Get a setted preference of the given user
     */
    @Auditable(/*key = Auditable.Key.ARG_0, */ parameters = {"person", "preferenceQname"})
    Serializable getPreference(final NodeRef person, final QName preferenceQname);

    /**
     * Get the user by an email
     */
    @Auditable(/*key = Auditable.Key.NO_KEY, */ parameters = {"email"})
    String getUserByEmail(final String email);

    /**
     * Get the domain of the user or null if it is an alfresco user.
     *
     * @param pUserName the user
     */
    @Auditable(/*key = Auditable.Key.NO_KEY, */ parameters = {"pUserName"})
    String getUserDomain(final String pUserName);

    /**
     * Get email for Circabc user on alfresco
     *
     * @param pUserName the user
     */
    @Auditable(/*key = Auditable.Key.NO_KEY, */ parameters = {"pUserName"})
    String getUserEmail(final String pUserName);

    /**
     * Get first and last name for Circabc user concanate with space
     *
     * @param pUserName the user
     */
    @Auditable(/*key = Auditable.Key.NO_KEY, */ parameters = {"pUserName"})
    String getUserFullName(final String pUserName);

    /**
     * Return user name for given user email
     *
     * @return user name
     */
    @Auditable(/*key = Auditable.Key.NO_KEY*/)
    String getUserNameByEmail(final String email);

    @Auditable(/*key = Auditable.Key.NO_KEY, */ parameters = {"pDomain", "pCriteria", "filter"})
    List<SearchResultRecord> getUsersByDomainFirstNameLastNameEmail(
            final String pDomain, final String pCriteria, boolean filter);

    @Auditable(/*key = Auditable.Key.NO_KEY, */ parameters = {"mail", "domain", "filter"})
    List<SearchResultRecord> getUsersByMailDomain(final String mail, final String domain, boolean filter);

    /**
     * @param nodeRef
     * @param service
     * @param permission
     * @return
     */
    Set<String> getUsersWithPermission(final NodeRef nodeRef, final String permission);

    /**
     * Return if the given email is already used by another user in Circabc. This method is usefull to
     * garentee the unicity of the email inside alfresco.
     *
     * @param email                              the email to test
     * @param excludeDataInTheCurrentTransaction if the data setted in the current transaction must be
     *                                           excluded
     * @return true if the email is already used.
     */
    @NotAuditable
    boolean isEmailExists(final String email, final boolean excludeDataInTheCurrentTransaction);

    /**
     * Set password for Circabc user on alfresco
     *
     * @param pUserName the user
     */
    @Auditable(
            /*key = Auditable.Key.NO_KEY, */ parameters = {"pUserName", "pNewPassword"},
            recordable = {true, false})
    void setPassword(final String pUserName, final char[] pNewPassword);

    /**
     * set a preference of the given user
     */
    @Auditable(/*key = Auditable.Key.ARG_0, */ parameters = {"person", "preferenceQname", "value"})
    void setPreference(final NodeRef person, final QName preferenceQname, final Serializable value);

    @Auditable(/*key = Auditable.Key.NO_KEY*/)
    void updateMissingLastNamePersons();

    /**
     * Update all the data of a Circabc user
     *
     * @param pCircabcUser Circabc user
     */
    @Auditable(/*key = Auditable.Key.NO_KEY, */ parameters = {"pCircabcUser"})
    void updateUser(final CircabcUserDataBean pCircabcUser);

    /**
     * Update the data of a Circabc user
     *
     * @param pCircabcUser         Circabc user
     * @param pNonAspectProperties true if you want to udpate just the NonAspect Properties (the one
     *                             that a user is not allow change and are changed by the batch process)
     */
    @Auditable(/*key = Auditable.Key.NO_KEY, */ parameters = {"pCircabcUser", "pNonAspectProperties"})
    void updateUser(final CircabcUserDataBean pCircabcUser, final boolean pNonAspectProperties);

    /**
     * Return true if the user is connected
     */
    @Auditable(/*key =  Auditable.Key.NO_KEY*/)
    boolean isUserOnline(final String user);

    /**
     * @return list of online users
     */
    @Auditable(/*key =  Auditable.Key.NO_KEY*/)
    Set<String> getAllOnlineUsers();

    /**
     * @return list of online users for a specific InterestGroup
     */
    @Auditable(/*key = Auditable.Key.ARG_0*/)
    Set<String> getOnlineUsers(final NodeRef igNodeRef);

    /**
     * create user newUserName copy all properties from
     */
    @Auditable(/*key =  Auditable.Key.NO_KEY*/)
    void copyUser(
            final String oldUserName,
            final String newUserName,
            boolean deleteOldUser,
            boolean copyOwnership,
            boolean copyMembership);

    @Auditable(/*key =  Auditable.Key.NO_KEY*/)
    Map<String, String> getEcasUserDomains();

    /**
     * @return true if the user with given userName exists
     */
    @NotAuditable
    boolean isUserExists(final String userName);

    /**
     * get alfresco ticket for given user
     *
     * @param userName name of alfresco user
     * @return alfresco user ticket
     */
    @NotAuditable
    String getCurrentTicket(String userName);

    /**
     * delete users
     */
    @Auditable(/*key =  Auditable.Key.NO_KEY*/)
    void deleteUsers(List<String> userNames);

    /**
     * get redirect url after logout from ECAS
     */
    @Auditable(/*key =  Auditable.Key.NO_KEY*/)
    String getRedirectUrlAfterLogout();

    @Auditable(/*key = Auditable.Key.NO_KEY*/)
    Boolean userExists(String uid);

    @Auditable(/*key = Auditable.Key.NO_KEY*/)
    void setAuthenticationEnabled(String userName, boolean enabled);

    @Auditable(/*key = Auditable.Key.NO_KEY*/)
    boolean getAuthenticationEnabled(String userName);

    CircabcUserDataBean getLDAPUserDataNoFilterByUid(final String userID);
}
