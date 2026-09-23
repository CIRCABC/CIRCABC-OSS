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

import freemarker.ext.beans.BeanModel;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import io.swagger.model.alfresco.UserModel;
import java.util.List;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.template.BaseTemplateProcessorExtension;
import org.alfresco.repo.template.TemplateNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * FreeMarker template extension that resolves and renders the login user name of a user.
 *
 * <p>This method is exposed to FreeMarker templates as a callable function. For a given user it
 * returns the appropriate login identifier: in the enterprise/ECAS deployment this is the ECAS user
 * name ({@link UserModel#PROP_ECAS_USER_NAME}), while in the open-source (OSS) deployment it falls
 * back to the standard Alfresco user name ({@link ContentModel#PROP_USERNAME}).
 *
 * <p>The method accepts a single argument that may either be a wrapped {@link TemplateNode}
 * (representing the person node directly) or a {@link SimpleScalar} holding a user name string. When
 * a user name string is supplied and the corresponding person exists, its login user name is
 * resolved; otherwise the supplied user name is returned unchanged.
 *
 * @author Slobodan Filipovic
 */
public class LoginUserNameMethod
  extends BaseTemplateProcessorExtension
  implements TemplateMethodModelEx
{

  /** Alfresco service registry used to access the person and node services. */
  private ServiceRegistry services;

  /**
   * Resolves the login user name for the user referenced by the first template argument.
   *
   * <p>The first argument is interpreted as follows:
   *
   * <ul>
   *   <li>a wrapped {@link TemplateNode} &ndash; the login user name is resolved from the node's
   *       {@link NodeRef};
   *   <li>a {@link SimpleScalar} user name &ndash; if the person exists, its login user name is
   *       resolved, otherwise the raw user name string is returned.
   * </ul>
   *
   * @param args the FreeMarker method arguments; the first element is used to identify the user
   * @return the resolved login user name, or an empty string if no usable argument is provided
   * @throws TemplateModelException if the arguments cannot be processed by the template engine
   * @see freemarker.template.TemplateMethodModel#exec(java.util.List)
   */
  public Object exec(@SuppressWarnings("rawtypes") List args)
    throws TemplateModelException {
    String result = "";

    if (!args.isEmpty()) {
      final Object arg0 = args.get(0);

      if (arg0 instanceof BeanModel) {
        final BeanModel arg0BeanModel = (BeanModel) args.get(0);

        if (
          arg0BeanModel.getWrappedObject() instanceof TemplateNode templateNode
        ) {
          final NodeRef user = templateNode.getNodeRef();
          result = getLoginUserName(user);
        }
      } else if (arg0 instanceof SimpleScalar simpleScalar) {
        final String userName = simpleScalar.getAsString();

        if (services.getPersonService().personExists(userName)) {
          result = getLoginUserName(
            services.getPersonService().getPerson(userName)
          );
        } else {
          result = userName;
        }
      }
    }

    return result;
  }

  /**
   * Resolves the login user name for the given person node, preferring the ECAS user name and
   * falling back to the standard Alfresco user name when the ECAS property is not set.
   *
   * @param user the {@link NodeRef} of the person node
   * @return the ECAS user name if present, otherwise the Alfresco user name
   */
  protected String getLoginUserName(NodeRef user) {
    String displayName = (String) services
      .getNodeService()
      .getProperty(user, UserModel.PROP_ECAS_USER_NAME);
    if (displayName == null) {
      displayName = (String) services
        .getNodeService()
        .getProperty(user, ContentModel.PROP_USERNAME);
    }
    return displayName;
  }

  /**
   * Returns the Alfresco service registry injected into this template extension.
   *
   * @return the services
   */
  protected final ServiceRegistry getServiceRegistry() {
    return services;
  }

  /**
   * Sets the Alfresco service registry used by this template extension.
   *
   * @param services the services to set
   */
  public final void setServiceRegistry(ServiceRegistry services) {
    this.services = services;
  }
}
