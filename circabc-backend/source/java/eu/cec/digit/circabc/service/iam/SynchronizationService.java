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
package eu.cec.digit.circabc.service.iam;

import java.util.List;
import java.util.Set;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author Slobodan Filipovic CIRCABC IAM synchronization
 */
public interface SynchronizationService {
  public static final String DEFAULT_ECORDA_ROLE = "THEME_MEMBER";

  /**
   * Call this method to grant user role to IAM
   */
  void grantThemeRole(String userName, String themeID, String profile);

  void grantThemeRoles(Set<String> userName, String themeID, String profile);

  /**
   * Call this method to revoke user role to IAM
   */
  void revokeThemeRole(String userName, String themeID, String profile);

  /**
   * @param interestGroup node reference of interest group
   * @return eCORDA Theme IDs if Exists or Empty list otherwise
   */
  List<String> getEcordaThemeIds(NodeRef interestGroup);
}
