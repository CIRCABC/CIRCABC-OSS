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
import java.util.List;
import org.alfresco.service.Auditable;

/**
 * Spring service bean that manages basic read/search operations on users stored in an LDAP
 * directory.
 *
 * <p>Implementations of this interface act as the bridge between CIRCABC and the configured LDAP
 * server, exposing lookups by user identifier as well as searches by moniker, e-mail, common name
 * and domain. The results are returned as CIRCABC domain objects ({@link CircabcUserDataBean} and
 * {@link SearchResultRecord}) rather than raw LDAP entries.
 *
 * @author stephane Clinckart
 * <p>Migration 3.1 -> 3.4.6 - 02/12/2011 Commented the key parameter of the @Auditable
 * annotation. Commented the deprecated @PublicService annotation.
 */
// @PublicService
public interface LdapUserService {
  /**
   * Retrieves the LDAP data of a single user identified by its unique LDAP identifier (uid),
   * applying the service's default filtering rules.
   *
   * @param ldapUserID the unique LDAP identifier (uid) of the user to look up
   * @return the user data bean for the matching LDAP entry, or {@code null} if no user matches
   */
  @Auditable(/*key = Auditable.Key.NO_KEY, */ parameters = { "ldapUserID" })
  CircabcUserDataBean getLDAPUserDataByUid(final String ldapUserID);

  /**
   * Retrieves the LDAP data of a single user identified by its unique LDAP identifier (uid) without
   * applying any additional filtering.
   *
   * @param userID the unique LDAP identifier (uid) of the user to look up
   * @return the user data bean for the matching LDAP entry, or {@code null} if no user matches
   */
  CircabcUserDataBean getLDAPUserDataNoFilterByUid(final String userID);

  /**
   * Searches for LDAP user identifiers matching the supplied criteria.
   *
   * @param uid the user identifier (uid) to match, may be {@code null} to ignore this criterion
   * @param moniker the moniker/login to match, may be {@code null} to ignore this criterion
   * @param email the e-mail address to match, may be {@code null} to ignore this criterion
   * @param cn the common name to match, may be {@code null} to ignore this criterion
   * @param conjunction if {@code true} the criteria are combined with a logical AND, otherwise they
   *     are combined with a logical OR
   * @return the list of matching LDAP user identifiers; an empty list if none match
   */
  @Auditable(
    /*key = Auditable.Key.NO_KEY, */ parameters = {
      "uid", "moniker", "email", "cn", "conjunction",
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
   * Searches for users within a given domain whose first name, last name or e-mail matches the
   * supplied free-text criteria.
   *
   * @param domain the domain within which to restrict the search
   * @param criteria the free-text search criteria matched against first name, last name and e-mail
   * @param filter whether the service's default filtering rules should be applied to the results
   * @return the list of matching search result records; an empty list if none match
   */
  @Auditable(
    /*key = Auditable.Key.NO_KEY, */ parameters = {
      "domain", "criteria", "filter",
    }
  )
  List<SearchResultRecord> getUsersByDomainFirstNameLastNameEmail(
    final String domain,
    final String criteria,
    boolean filter
  );

  /**
   * Searches for users within a given domain whose e-mail address matches the supplied value.
   *
   * @param mail the e-mail address to match
   * @param domain the domain within which to restrict the search
   * @param filter whether the service's default filtering rules should be applied to the results
   * @return the list of matching search result records; an empty list if none match
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
   * Initialises the service, allowing implementations to set up the LDAP connection and any related
   * resources. Typically invoked by the Spring container after the bean is constructed.
   */
  void init();
}
