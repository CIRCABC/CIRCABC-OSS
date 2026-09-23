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

import java.util.Locale;
import java.util.Map;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Abstraction over a single mail template used to build notification e-mails.
 *
 * <p>A {@code MailWrapper} exposes the metadata of a mail template (its name, {@link MailTemplate
 * type} and backing {@link NodeRef}) and is responsible for rendering the mail subject and body by
 * merging a supplied data model into the template. Rendering can be performed either with the
 * default locale or with an explicitly provided {@link Locale} so that localized notifications can
 * be produced.
 *
 * @author yanick pignot
 */
public interface MailWrapper {
  /**
   * Returns the logical name of the underlying mail template.
   *
   * @return the name of the template
   */
  String getName();

  /**
   * Renders the mail subject by merging the given model into the template using the default locale.
   *
   * @param model the data model whose values are substituted into the template
   * @return the rendered subject of the mail
   */
  String getSubject(final Map<String, Object> model);

  /**
   * Renders the mail subject by merging the given model into the template for the specified locale.
   *
   * @param model the data model whose values are substituted into the template
   * @param language the locale used to select the localized template and format values
   * @return the rendered subject of the mail
   */
  String getSubject(final Map<String, Object> model, final Locale language);

  /**
   * Renders the mail body by merging the given model into the template using the default locale.
   *
   * @param model the data model whose values are substituted into the template
   * @return the rendered body of the mail
   */
  String getBody(final Map<String, Object> model);

  /**
   * Renders the mail body by merging the given model into the template for the specified locale.
   *
   * @param model the data model whose values are substituted into the template
   * @param language the locale used to select the localized template and format values
   * @return the rendered body of the mail
   */
  String getBody(final Map<String, Object> model, final Locale language);

  /**
   * Indicates whether this wrapper represents the original (default) template rather than a
   * user-customized one.
   *
   * @return {@code true} if this is the default template, {@code false} otherwise
   */
  boolean isOriginalTemplate();

  /**
   * Returns the template type describing the kind of notification this mail represents.
   *
   * @return the template type of the mail
   */
  MailTemplate getMailTemplate();

  /**
   * Returns the repository reference of the node backing this mail template.
   *
   * @return the {@link NodeRef} of the mail template
   */
  NodeRef getTemplateNodeRef();
}
