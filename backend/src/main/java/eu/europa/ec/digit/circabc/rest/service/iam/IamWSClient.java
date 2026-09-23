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

/**
 * Client abstraction for communicating with the external IAM (Identity and Access Management) web
 * service.
 *
 * <p>Implementations synchronize CIRCABC theme-level role assignments with the IAM system by
 * granting and revoking a user's role on a given theme. This keeps the external identity provider
 * aligned with the permission changes performed within CIRCABC.
 */
public interface IamWSClient {
  /**
   * Grants a theme-level role to a user in the IAM system.
   *
   * @param userID the identifier of the user receiving the role
   * @param themeID the identifier of the theme the role applies to
   * @param roleID the identifier of the role to grant
   */
  void grantThemeRole(String userID, String themeID, String roleID);

  /**
   * Revokes a previously granted theme-level role from a user in the IAM system.
   *
   * @param userID the identifier of the user whose role is being revoked
   * @param themeID the identifier of the theme the role applies to
   * @param roleID the identifier of the role to revoke
   */
  void revokeThemeRole(String userID, String themeID, String roleID);
}
