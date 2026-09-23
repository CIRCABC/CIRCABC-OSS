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
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import java.io.Serializable;
import java.util.List;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.template.BaseTemplateProcessorExtension;
import org.alfresco.repo.template.TemplateNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * FreeMarker template extension that resolves a user's human-readable display name.
 *
 * <p>Registered as a template method (via Alfresco's {@link BaseTemplateProcessorExtension}),
 * this class can be invoked from {@code .ftl} templates to turn a user reference into a
 * formatted full name. The first argument may either be a {@link BeanModel} wrapping a
 * {@link TemplateNode} (a person node) or a {@link SimpleScalar} containing a user name.
 * An optional second boolean argument controls whether the user's email address is appended
 * to the returned name.
 *
 * <p>The produced string is one of:
 *
 * <ul>
 *   <li>{@code "First Last"} when first and/or last name are available,
 *   <li>{@code "First Last (email)"} when email inclusion is requested and an email exists,
 *   <li>the user name when neither first nor last name is set,
 *   <li>an empty string when no usable input is supplied.
 * </ul>
 */
public class UserDisplayNameMethod
  extends BaseTemplateProcessorExtension
  implements TemplateMethodModelEx
{

  /** Alfresco service registry used to look up person and node metadata. */
  private ServiceRegistry services;

  /**
   * Resolves the display name for the user described by the supplied template arguments.
   *
   * @param args the template method arguments; the first element is the user reference
   *     (a {@link BeanModel} wrapping a {@link TemplateNode} or a {@link SimpleScalar} user
   *     name) and the optional second element is a {@link TemplateBooleanModel} flag that,
   *     when {@code true}, requests the email address be appended
   * @return the formatted display name, or an empty string when no usable input is provided
   * @throws TemplateModelException if the arguments cannot be evaluated by FreeMarker
   */
  @Override
  @SuppressWarnings({ "rawtypes", "java:S3740" })
  public Object exec(List args) throws TemplateModelException {
    if (args.isEmpty()) {
      return "";
    }

    boolean addEmail = extractAddEmail(args);
    Object arg0 = args.get(0);

    if (arg0 instanceof BeanModel beanModel) {
      return handleBeanModel(beanModel, addEmail);
    } else if (arg0 instanceof SimpleScalar simpleScalar) {
      return handleSimpleScalar(simpleScalar, addEmail);
    }
    return "";
  }

  @SuppressWarnings({ "rawtypes", "java:S3740" })
  private boolean extractAddEmail(List args) throws TemplateModelException {
    if (
      args.size() > 1 &&
      args.get(1) instanceof TemplateBooleanModel booleanModel
    ) {
      return booleanModel.getAsBoolean();
    }
    return false;
  }

  private String handleBeanModel(BeanModel beanModel, boolean addEmail) {
    if (beanModel.getWrappedObject() instanceof TemplateNode templateNode) {
      NodeRef user = templateNode.getNodeRef();
      return getUserFullName(user, addEmail);
    }
    return "";
  }

  private String handleSimpleScalar(SimpleScalar scalar, boolean addEmail) {
    String userName = scalar.getAsString();
    if (services.getPersonService().personExists(userName)) {
      return getUserFullName(
        services.getPersonService().getPerson(userName),
        addEmail
      );
    }
    return userName;
  }

  /**
   * Builds the full name for a person node by reading its first name, last name and,
   * optionally, email properties.
   *
   * <p>When neither first nor last name is set, the user name property is returned instead.
   *
   * @param user the {@link NodeRef} of the person node to resolve
   * @param addEmail when {@code true}, the email address is appended in parentheses
   * @return the formatted full name, the user name as a fallback, or an empty string when no
   *     property can be resolved
   */
  protected String getUserFullName(NodeRef user, Boolean addEmail) {
    Serializable firstName = services
      .getNodeService()
      .getProperty(user, ContentModel.PROP_FIRSTNAME);
    Serializable lastName = services
      .getNodeService()
      .getProperty(user, ContentModel.PROP_LASTNAME);

    if (firstName == null && lastName == null) {
      Serializable username = services
        .getNodeService()
        .getProperty(user, ContentModel.PROP_USERNAME);
      return username != null ? username.toString() : "";
    }

    Serializable email = Boolean.TRUE.equals(addEmail)
      ? services.getNodeService().getProperty(user, ContentModel.PROP_EMAIL)
      : null;

    return buildFullName(firstName, lastName, email);
  }

  private String buildFullName(
    Serializable firstName,
    Serializable lastName,
    Serializable email
  ) {
    StringBuilder sb = new StringBuilder();
    if (firstName != null) sb.append(firstName).append(" ");
    if (lastName != null) sb.append(lastName);
    if (email != null) sb.append(" (").append(email).append(")");
    return sb.toString();
  }

  /**
   * Returns the Alfresco service registry used by this template extension.
   *
   * @return the configured {@link ServiceRegistry}
   */
  protected final ServiceRegistry getServiceRegistry() {
    return services;
  }

  /**
   * Injects the Alfresco service registry used to resolve person and node metadata.
   *
   * @param services the {@link ServiceRegistry} to use
   */
  public final void setServiceRegistry(ServiceRegistry services) {
    this.services = services;
  }
}
