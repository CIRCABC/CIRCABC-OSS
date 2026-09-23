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
package eu.europa.ec.digit.circabc.rest.service.user;

import io.swagger.model.CircabcUserDataBean;
import io.swagger.model.SearchResultRecord;
import io.swagger.model.UserCategoryMembershipRecord;
import io.swagger.model.UserIGMembershipRecord;
import java.io.Serializable;
import java.util.List;
import java.util.Set;
import org.alfresco.service.Auditable;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Spring service bean that centralises the management of CIRCABC users on top of the underlying
 * Alfresco repository.
 *
 * <p>It exposes the basic lifecycle operations (create, read, update) over users carrying the
 * CIRCABC aspect, as well as helper look-ups such as resolving a user by e-mail, retrieving the
 * {@link NodeRef} of a person, reading/writing user preferences, managing passwords and toggling
 * authentication. It also offers queries that aggregate the categories and interest groups a user
 * belongs to, and LDAP-oriented look-ups used during synchronisation and migration.
 *
 * @author atadian - Trasys
 * @author Yanick Pignot
 * <p>Migration 3.1 -> 3.4.6 - 02/12/2011 Commented the key parameter of the @Auditable
 * annotation. Commented the deprecated @PublicService annotation.
 */
// @PublicService
public interface UserService {
  /** Preference QName holding the user's preferred content filter language. */
  QName PREF_CONTENT_FILTER_LANGUAGE = QName.createQName(
    NamespaceService.APP_MODEL_1_0_URI,
    "content-filter-language"
  );
  /** Preference QName holding the user's preferred interface (UI) language. */
  QName PREF_INTERFACE_LANGUAGE = QName.createQName(
    NamespaceService.APP_MODEL_1_0_URI,
    "interface-language"
    /*
     * this durty trick is
     * used to avoid
     * problems during
     * alfresco version
     * migration
     * "interface-language"
     */
  );

  /** Preference QName holding the user's e-mail signature. */
  QName PREF_SIGNATURE = QName.createQName(
    NamespaceService.APP_MODEL_1_0_URI,
    "signature"
  );

  /**
   * Creates a new LDAP-backed user in Alfresco with the CIRCABC aspect.
   *
   * @param userId  the id of the user to create
   * @param enabled {@code true} if the created user should be allowed to authenticate
   * @return the {@link NodeRef} of the newly created person node
   */
  @Auditable(
    /*key = Auditable.Key.RETURN, */ parameters = { "circabcUser", "enabled" }
  )
  NodeRef createLdapUser(final String userId, final boolean enabled);

  /**
   * Creates a new User in Alfresco with the Circabc Aspect
   *
   * @param circabcUser the data of the new user
   * @return the {@link NodeRef} of the newly created person node
   */
  @Auditable(/*key = Auditable.Key.RETURN, */ parameters = { "circabcUser" })
  NodeRef createUser(final CircabcUserDataBean circabcUser);

  /**
   * Creates a new User in Alfresco with the Circabc Aspect
   *
   * @param circabcUser the data of the new user
   * @param enabled      if the user should be enbled or not. If not, the user can't authenticate itself
   * @return the {@link NodeRef} of the newly created person node
   */
  @Auditable(
    /*key = Auditable.Key.RETURN, */ parameters = { "circabcUser", "enabled" }
  )
  NodeRef createUser(
    final CircabcUserDataBean circabcUser,
    final boolean enabled
  );

  /**
   * Returns the categories the given user is a member of, together with the profile held in each.
   *
   * @param pUserName the user name
   * @return list of category / profile membership records for the given user
   */
  @Auditable
  List<UserCategoryMembershipRecord> getCategories(final String pUserName);

  /**
   * Reads the full CIRCABC user data for the given user name.
   *
   * @param userName the user name
   * @return the {@link CircabcUserDataBean} describing the user, or {@code null} if not found
   */
  @Auditable
  CircabcUserDataBean getCircabcUserDataBean(final String userName);

  /**
   * Returns the event root nodes accessible to the given user.
   *
   * @param pUserName the user name
   * @return list of event root {@link NodeRef}s for the given user
   */
  @Auditable
  List<NodeRef> getEventRootNodes(final String pUserName);

  /**
   * Returns the interest groups the given user is a member of.
   *
   * @param pUserName the user name
   * @return list of interest group membership records for the given user
   */
  @Auditable
  List<UserIGMembershipRecord> getInterestGroups(final String pUserName);

  /**
   * Returns the interest groups the given user is a member of, restricted to the supplied
   * categories.
   *
   * @param userName   the user name
   * @param categories the categories to which the interest group look-up is restricted
   * @return list of interest group membership records for the given user and categories
   */
  @Auditable
  List<UserIGMembershipRecord> getInterestGroups(
    final String userName,
    List<NodeRef> categories
  );

  /**
   * LDAP implementation: retrieves the CIRCABC user data for the given LDAP user id.
   *
   * @param pLdapUserID the LDAP unique id (uid) of the user
   * @return the {@link CircabcUserDataBean} resolved from LDAP, or {@code null} if not found
   */
  @Auditable(/*key = Auditable.Key.NO_KEY, */ parameters = { "pLdapUserID" })
  CircabcUserDataBean getLDAPUserDataByUid(final String pLdapUserID);

  /**
   * Searches LDAP for user ids matching any (or all) of the supplied identity attributes.
   *
   * @param uid         the LDAP unique id to match
   * @param moniker     the moniker to match
   * @param email       the e-mail address to match
   * @param cn          the common name (cn) to match
   * @param conjunction {@code true} to combine the criteria with a logical AND, {@code false} to
   *                    combine them with a logical OR
   * @return the list of matching LDAP user ids
   */
  @Auditable(
    /*key = Auditable.Key.NO_KEY, */ parameters = {
      "mail", "uid", "moniker", "cn", "conjunction",
    }
  )
  List<String> getLDAPUserIDByIdMonikerEmailCn(
    final String uid,
    final String moniker,
    final String email,
    final String cn,
    final boolean conjunction
  );

  /**
   * Get the noderef of the user
   *
   * @param pUserName the user
   * @return the {@link NodeRef} of the person node, or {@code null} if the user does not exist
   */
  @Auditable(/*key = Auditable.Key.NO_KEY, */ parameters = { "pUserName" })
  NodeRef getPerson(final String pUserName);

  /**
   * Get a setted preference of the given user
   *
   * @param person         the {@link NodeRef} of the person whose preference is read
   * @param preferenceQname the {@link QName} identifying the preference to read
   * @return the stored preference value, or {@code null} if it has not been set
   */
  @Auditable(
    /*key = Auditable.Key.ARG_0, */ parameters = { "person", "preferenceQname" }
  )
  Serializable getPreference(final NodeRef person, final QName preferenceQname);

  /**
   * Get the user by an email
   *
   * @param email the e-mail address to resolve
   * @return the matching user name, or {@code null} if no user matches the e-mail
   */
  @Auditable(/*key = Auditable.Key.NO_KEY, */ parameters = { "email" })
  String getUserByEmail(final String email);

  /**
   * Get the domain of the user or null if it is an alfresco user.
   *
   * @param pUserName the user
   * @return the domain of the user, or {@code null} if the user is a plain Alfresco user
   */
  @Auditable(/*key = Auditable.Key.NO_KEY, */ parameters = { "pUserName" })
  String getUserDomain(final String pUserName);

  /**
   * Get email for Circabc user on alfresco
   *
   * @param pUserName the user
   * @return the e-mail address of the user
   */
  @Auditable(/*key = Auditable.Key.NO_KEY, */ parameters = { "pUserName" })
  String getUserEmail(final String pUserName);

  /**
   * Get first and last name for Circabc user concanate with space
   *
   * @param pUserName the user
   * @return the user's first and last name concatenated with a space
   */
  @Auditable(/*key = Auditable.Key.NO_KEY, */ parameters = { "pUserName" })
  String getUserFullName(final String pUserName);

  /**
   * Return user name for given user email
   *
   * @param email the e-mail address to resolve
   * @return user name
   */
  @Auditable
  String getUserNameByEmail(final String email);

  /**
   * Searches for users within a domain matching a criterion on first name, last name or e-mail.
   *
   * @param pDomain   the domain to search within
   * @param pCriteria the search criterion applied to first name, last name and e-mail
   * @param filter    {@code true} to apply the standard visibility/security filtering to results
   * @return the list of matching search result records
   */
  @Auditable(
    /*key = Auditable.Key.NO_KEY, */ parameters = {
      "pDomain", "pCriteria", "filter",
    }
  )
  List<SearchResultRecord> getUsersByDomainFirstNameLastNameEmail(
    final String pDomain,
    final String pCriteria,
    boolean filter
  );

  /**
   * Searches for users matching an e-mail within a given domain.
   *
   * @param mail   the e-mail address (or fragment) to match
   * @param domain the domain to search within
   * @param filter {@code true} to apply the standard visibility/security filtering to results
   * @return the list of matching search result records
   */
  @Auditable(
    /*key = Auditable.Key.NO_KEY, */ parameters = { "mail", "domain", "filter" }
  )
  List<SearchResultRecord> getUsersByMailDomain(
    final String mail,
    final String domain,
    boolean filter
  );

  /**
   * Returns the user names that hold the given permission on the supplied node.
   *
   * @param nodeRef    the {@link NodeRef} whose authorities are inspected
   * @param permission the permission to look for
   * @return the set of user names holding the permission on the node
   */
  Set<String> getUsersWithPermission(
    final NodeRef nodeRef,
    final String permission
  );

  /**
   * Set password for Circabc user on alfresco
   *
   * @param pUserName    the user
   * @param pNewPassword the new password to assign
   */
  @Auditable(
    /*key = Auditable.Key.NO_KEY, */ parameters = {
      "pUserName", "pNewPassword",
    },
    recordable = { true, false }
  )
  void setPassword(final String pUserName, final char[] pNewPassword);

  /**
   * Update all the data of a Circabc user
   *
   * @param pCircabcUser Circabc user
   */
  @Auditable(/*key = Auditable.Key.NO_KEY, */ parameters = { "pCircabcUser" })
  void updateUser(final CircabcUserDataBean pCircabcUser);

  /**
   * Update the data of a Circabc user
   *
   * @param pCircabcUser         Circabc user
   * @param pNonAspectProperties true if you want to udpate just the NonAspect Properties (the one
   *                             that a user is not allow change and are changed by the batch process)
   */
  @Auditable(
    /*key = Auditable.Key.NO_KEY, */ parameters = {
      "pCircabcUser", "pNonAspectProperties",
    }
  )
  void updateUser(
    final CircabcUserDataBean pCircabcUser,
    final boolean pNonAspectProperties
  );

  /**
   * Enables or disables authentication for the given user.
   *
   * @param userName the user name
   * @param enabled  {@code true} to allow the user to authenticate, {@code false} to prevent it
   */
  @Auditable
  void setAuthenticationEnabled(String userName, boolean enabled);

  /**
   * Indicates whether authentication is currently enabled for the given user.
   *
   * @param userName the user name
   * @return {@code true} if the user is allowed to authenticate, {@code false} otherwise
   */
  @Auditable
  boolean getAuthenticationEnabled(String userName);

  /**
   * Retrieves the CIRCABC user data for the given LDAP user id without applying any filtering.
   *
   * @param userID the LDAP unique id (uid) of the user
   * @return the unfiltered {@link CircabcUserDataBean} resolved from LDAP, or {@code null} if not
   *         found
   */
  CircabcUserDataBean getLDAPUserDataNoFilterByUid(final String userID);
}
