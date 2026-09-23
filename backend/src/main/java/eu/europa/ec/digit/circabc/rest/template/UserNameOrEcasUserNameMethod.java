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
package eu.europa.ec.digit.circabc.rest.template;

import io.swagger.config.CircabcConfig;
import io.swagger.model.alfresco.UserModel;
import java.io.Serializable;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * FreeMarker template method that resolves the display user name for a person node, choosing
 * between the ECAS user name and the standard Alfresco user name depending on the active CIRCABC
 * deployment mode.
 *
 * <p>This extension is invoked from FreeMarker templates with a single wrapped node reference (see
 * {@link NodeRefBaseTemplateProcessorExtension}). When CIRCABC is configured to use LDAP/ECAS (the
 * ENT deployment, {@link CircabcConfig#isUseLDAP()} returns {@code true}) the ECAS user name is
 * preferred; otherwise, or when no ECAS user name is available (for example for a technical user
 * that is not known in LDAP, or in the OSS deployment where LDAP is disabled), the standard
 * {@link ContentModel#PROP_USERNAME} property is used as a fallback.
 *
 * @author filipsl
 */
public class UserNameOrEcasUserNameMethod
  extends NodeRefBaseTemplateProcessorExtension
{

  /**
   * CIRCABC configuration used to determine the deployment mode (ENT/LDAP vs OSS) and thus which
   * user-name property should be displayed. Injected by Spring.
   */
  @Autowired
  private CircabcConfig circabcConfig;

  /**
   * Resolves the user name to display for the given person node.
   *
   * <p>When LDAP/ECAS usage is enabled the ECAS user name
   * ({@link UserModel#PROP_ECAS_USER_NAME}) is returned when present. If LDAP is disabled, or the
   * ECAS user name is not set (e.g. for a technical user unknown in LDAP), the standard Alfresco
   * user name ({@link ContentModel#PROP_USERNAME}) is returned instead.
   *
   * @param nodeRef the repository node reference of the person whose user name is required
   * @return the ECAS user name when available and LDAP is enabled, otherwise the standard user
   *     name; may be {@code null} if neither property is set on the node
   */
  @Override
  public String getResult(final NodeRef nodeRef) {
    final Map<QName, Serializable> props = getNodeService().getProperties(
      nodeRef
    );

    String result = null;
    if (circabcConfig.isUseLDAP()) {
      result = (String) props.get(UserModel.PROP_ECAS_USER_NAME);
    }
    if (result == null) {
      // it could be the case when CircabcConfig.USE_LDAP is true but we use a
      // technical user not known in the ldap
      // or when CircabcConfig.USE_LDAP is false
      result = (String) props.get(ContentModel.PROP_USERNAME);
    }
    return result;
  }
}
