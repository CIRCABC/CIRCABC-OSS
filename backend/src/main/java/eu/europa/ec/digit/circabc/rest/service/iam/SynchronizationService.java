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
package eu.europa.ec.digit.circabc.rest.service.iam;

import java.util.List;
import java.util.Set;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Service contract for synchronizing CIRCABC memberships with the external IAM (Identity and Access
 * Management) system.
 *
 * <p>Implementations propagate CIRCABC profile assignments to IAM by mapping an interest group to
 * one or more eCORDA themes and granting or revoking the corresponding theme roles for users. This
 * keeps the roles held in the IAM system aligned with the memberships managed inside CIRCABC.
 *
 * @author Slobodan Filipovic
 */
public interface SynchronizationService {
  /**
   * Default eCORDA role assigned to a user for a theme when no more specific role applies.
   */
  public static final String DEFAULT_ECORDA_ROLE = "THEME_MEMBER";

  /**
   * Grants a single user the IAM role corresponding to the given profile on the specified eCORDA
   * theme.
   *
   * @param userName the user (login/identifier) to grant the role to
   * @param themeID the eCORDA theme identifier the role applies to
   * @param profile the CIRCABC profile that maps to the IAM role to grant
   */
  void grantThemeRole(String userName, String themeID, String profile);

  /**
   * Grants a set of users the IAM role corresponding to the given profile on the specified eCORDA
   * theme.
   *
   * @param userName the set of users (logins/identifiers) to grant the role to
   * @param themeID the eCORDA theme identifier the role applies to
   * @param profile the CIRCABC profile that maps to the IAM role to grant
   */
  void grantThemeRoles(Set<String> userName, String themeID, String profile);

  /**
   * Revokes from a single user the IAM role corresponding to the given profile on the specified
   * eCORDA theme.
   *
   * @param userName the user (login/identifier) to revoke the role from
   * @param themeID the eCORDA theme identifier the role applies to
   * @param profile the CIRCABC profile that maps to the IAM role to revoke
   */
  void revokeThemeRole(String userName, String themeID, String profile);

  /**
   * Resolves the eCORDA theme identifiers associated with the given interest group.
   *
   * @param interestGroup node reference of the interest group
   * @return the eCORDA theme IDs if they exist, or an empty list otherwise
   */
  List<String> getEcordaThemeIds(NodeRef interestGroup);
}
