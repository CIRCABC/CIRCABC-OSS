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
package eu.europa.ec.digit.circabc.rest.service.mail;

import io.swagger.exception.CustomizationException;
import io.swagger.model.CircabcUserDataBean;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.alfresco.service.Auditable;
import org.alfresco.service.NotAuditable;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.TemplateImageResolver;

/**
 * Service contract for customising the e-mails sent within Interest Groups.
 *
 * <p>Implementations manage per-node mail templates (creation, retrieval and replacement of
 * localised subject/body pairs), build the default model used when processing those templates and
 * expose the branding resources (header logo, header background, EU logo and disclaimer logo)
 * configured for a given node. Template configuration is inherited: when a node does not define its
 * own templates the closest ancestor that does is used.
 *
 * @author Yanick Pignot
 * <p>Migration 3.1 -> 3.4.6 - 02/12/2011 Commented the key parameter of the @Auditable
 * annotation. Commented the deprecated @PublicService annotation.
 * @see eu.europa.ec.digit.circabc.rest.service.customization.NodePreferencesService
 */
// @PublicService
public interface MailPreferencesService {
  /**
   * Add a mail to a given <b>configurable node</b>. The method <code>makeConfigurable(ref)</code>
   * must be called on the node reference.
   *
   * <p>A new template will be created if the template doen't exists or translation will be added on
   * it.
   *
   * @param ref         the node on which we have to add a template
   * @param forTemplate the mandatory template to define the associated "mail group"
   * @param name        the unique name of the template
   * @param body        the mandatory body of the mail
   * @param subject     the mandatory subject of the mail
   * @param language    the mandatory language of the mail
   * @throws CustomizationException if a template already exist with this name and this Locale.
   */
  @Auditable(
    /*key = Auditable.Key.ARG_0, */ parameters = {
      "ref", "forTemplate", "name", "body", "subject", "language",
    }
  )
  void addMailTemplate(
    final NodeRef ref,
    final MailTemplate forTemplate,
    final String name,
    final String body,
    final String subject,
    final Locale language
  ) throws CustomizationException;

  /**
   * Build a default model for the mail template processing.
   *
   * @param currentRef    The current noderef
   * @param otherPerson   The person that will receive the email
   * @param imageResolver The image resolver
   * @return A map ti use as model for the template processing.
   */
  @NotAuditable
  Map<String, Object> buildDefaultModel(
    final NodeRef currentRef,
    final NodeRef otherPerson,
    final TemplateImageResolver imageResolver
  );

  /**
   * Return the default mail template defined on a given node.
   *
   * @param ref          the node for which the default template is resolved
   * @param mailTemplate the template kind (mail group) to retrieve
   * @return the default {@link MailWrapper} for the requested template on the given node
   * @see eu.europa.ec.digit.circabc.rest.service.customization.NodePreferencesService#getDefaultConfigurationFile(NodeRef,
   * String, String, String)
   */
  MailWrapper getDefaultMailTemplate(
    final NodeRef ref,
    final MailTemplate mailTemplate
  );

  /**
   * Get the disclaimer logo configuration for the given node.
   *
   * @param ref the node whose disclaimer logo configuration is requested
   * @return the {@link NodeRef} of the configured disclaimer logo, or {@code null} if none is set
   */
  NodeRef getDisclamerLogo(final NodeRef ref);

  /**
   * Return the list of mail templates for a given node. If templates not found, check recusivly
   * until a parent denine it.
   *
   * @param ref          The node on which we have to found prefernces
   * @param mailTemplate The mail template to return
   * @return The list of template defined for this node.
   */
  @Auditable(
    /*key = Auditable.Key.ARG_0, */ parameters = { "ref", "mailTemplate" }
  )
  List<MailWrapper> getMailTemplates(
    final NodeRef ref,
    final MailTemplate mailTemplate
  );

  /**
   * Build a default user data bean to be used for template edition needs.
   *
   * @return a {@link CircabcUserDataBean} pre-populated with default values for template editing
   */
  CircabcUserDataBean getTemplateUserDetails();

  /**
   * Replace the existing default mail to a given <b>configurable node</b>. The method <code>
   * makeConfigurable(ref)</code> must be called on the node reference.
   *
   * <p>A new template will be created if the template doen't exists or translation will be added on
   * it.
   *
   * @param ref         the node on which we have to add a template
   * @param forTemplate the mandatory template to define the associated "mail group"
   * @param body        the mandatory body of the mail
   * @param subject     the mandatory subject of the mail
   * @param language    the mandatory language of the mail
   * @throws CustomizationException if a template already exist with this name and this Locale.
   */
  @Auditable(
    /*key = Auditable.Key.ARG_0, */ parameters = {
      "ref", "forTemplate", "body", "subject", "language",
    }
  )
  void replaceDefaultMailTemplate(
    final NodeRef ref,
    final MailTemplate forTemplate,
    final String body,
    final String subject,
    final Locale language
  ) throws CustomizationException;

  /**
   * Get the header logo configuration for the given node.
   *
   * @param circabcRootRef the node (typically the CIRCABC root) whose header logo is requested
   * @return the {@link NodeRef} of the configured header logo, or {@code null} if none is set
   */
  NodeRef getHeaderLogo(NodeRef circabcRootRef);

  /**
   * Get the header background configuration for the given node.
   *
   * @param circabcRootRef the node (typically the CIRCABC root) whose header background is requested
   * @return the {@link NodeRef} of the configured header background, or {@code null} if none is set
   */
  NodeRef getHeaderBackground(NodeRef circabcRootRef);

  /**
   * Get the header EU logo configuration for the given node.
   *
   * @param circabcRootRef the node (typically the CIRCABC root) whose header EU logo is requested
   * @return the {@link NodeRef} of the configured header EU logo, or {@code null} if none is set
   */
  NodeRef getHeaderEULogo(NodeRef circabcRootRef);
}
