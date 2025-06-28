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
package eu.cec.digit.circabc.repo.template;

import eu.cec.digit.circabc.CircabcConfig;
import eu.cec.digit.circabc.model.UserModel;
import freemarker.template.TemplateMethodModelEx;
import java.io.Serializable;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Freemarker method to display the ECAS user name or user nsame base on
 * CircabcConfig ENT or OSS
 *
 * @author filipsl
 */
public class UserNameOrEcasUserNameMethod
  extends NodeRefBaseTemplateProcessorExtension
  implements TemplateMethodModelEx {

  @Override
  public String getResult(final NodeRef nodeRef) {
    final Map<QName, Serializable> props = getNodeService()
      .getProperties(nodeRef);
    String result = null;
    if (CircabcConfig.USE_LDAP) {
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
