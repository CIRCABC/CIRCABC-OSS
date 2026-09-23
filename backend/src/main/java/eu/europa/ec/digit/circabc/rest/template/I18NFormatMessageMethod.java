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

import freemarker.template.TemplateDateModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateNumberModel;
import freemarker.template.TemplateScalarModel;
import java.util.List;
import org.alfresco.repo.template.BaseTemplateProcessorExtension;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * FreeMarker template extension that resolves and formats internationalized (i18n) messages from
 * within Alfresco/CIRCABC templates.
 *
 * <p>Registered as a template method, it can be invoked from a FreeMarker template (for example
 * {@code ${msg("some.message.id", arg1, arg2)}}) to look up a localized message by its resource
 * bundle key and optionally substitute positional parameters into the message pattern.
 *
 * <p>The first argument is always the message key. Any additional arguments are converted to their
 * underlying Java types (String, Number or Date) and passed as substitution parameters to
 * {@link org.springframework.extensions.surf.util.I18NUtil}. When no arguments are supplied, or the
 * key cannot be resolved to a scalar value, an empty string is returned.
 */
public class I18NFormatMessageMethod
  extends BaseTemplateProcessorExtension
  implements TemplateMethodModelEx
{

  /**
   * Resolves a localized message for the key supplied as the first template argument, optionally
   * applying the remaining arguments as substitution parameters.
   *
   * @param args the template method arguments; the first element is expected to be the message key
   *     (a {@link TemplateScalarModel}) and any subsequent elements are used as message parameters
   * @return the resolved and formatted localized message, or an empty string if no arguments are
   *     provided or the first argument is not a scalar message key
   * @throws TemplateModelException if any argument cannot be read from its template model
   */
  @SuppressWarnings({ "rawtypes", "java:S3740" })
  public Object exec(List args) throws TemplateModelException {
    if (args.isEmpty()) {
      return "";
    }

    String id = extractId(args.get(0));
    if (id == null) {
      return "";
    }

    if (args.size() == 1) {
      return I18NUtil.getMessage(id);
    }

    return I18NUtil.getMessage(id, extractParams(args));
  }

  /**
   * Extracts the message key from the first template argument.
   *
   * @param arg the first template method argument
   * @return the message key as a string, or {@code null} if the argument is not a scalar model
   * @throws TemplateModelException if the scalar value cannot be read
   */
  private String extractId(Object arg) throws TemplateModelException {
    if (arg instanceof TemplateScalarModel scalarModel) {
      return scalarModel.getAsString();
    }
    return null;
  }

  /**
   * Converts all arguments after the message key into an array of message substitution parameters.
   *
   * @param args the full list of template method arguments (the first element, the message key, is
   *     skipped)
   * @return an array of converted parameter values, in the order they were supplied
   * @throws TemplateModelException if any argument cannot be converted from its template model
   */
  private Object[] extractParams(List<?> args) throws TemplateModelException {
    Object[] params = new Object[args.size() - 1];
    for (int i = 0; i < params.length; i++) {
      params[i] = convertArg(args.get(i + 1));
    }
    return params;
  }

  /**
   * Converts a single template argument into its underlying Java value for use as a message
   * parameter.
   *
   * @param arg the template method argument to convert
   * @return the argument's underlying {@link String}, {@link Number} or {@link java.util.Date}
   *     value, or an empty string if the argument type is not supported
   * @throws TemplateModelException if the value cannot be read from its template model
   */
  private Object convertArg(Object arg) throws TemplateModelException {
    if (arg instanceof TemplateScalarModel scalarModel) {
      return scalarModel.getAsString();
    } else if (arg instanceof TemplateNumberModel numberModel) {
      return numberModel.getAsNumber();
    } else if (arg instanceof TemplateDateModel dateModel) {
      return dateModel.getAsDate();
    }
    return "";
  }
}
