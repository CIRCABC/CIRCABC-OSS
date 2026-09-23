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

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import java.util.List;
import org.alfresco.repo.template.BaseTemplateProcessorExtension;

/**
 * FreeMarker template method extension that concatenates any number of arguments into a single
 * {@link String}.
 *
 * <p>Registered as a template processor extension, this method can be invoked from FreeMarker
 * templates (e.g. {@code ${concatAsString(arg1, arg2, ...)}}). Each argument is converted to its
 * string representation via {@link Object#toString()} and appended in order; {@code null} arguments
 * are skipped.
 *
 * @author Yanick Pignot
 */
public class ConcatAsStringMethod
  extends BaseTemplateProcessorExtension
  implements TemplateMethodModelEx
{

  /**
   * Concatenates the string representation of all supplied arguments, in order, into a single
   * string. Arguments that are {@code null} are ignored.
   *
   * @param args the list of arguments passed from the FreeMarker template; may contain {@code null}
   *     elements, which are skipped
   * @return the concatenation of the {@code toString()} value of every non-null argument; an empty
   *     string when {@code args} is empty or contains only {@code null} values
   * @throws TemplateModelException if the method cannot be evaluated within the template model
   * @see freemarker.template.TemplateMethodModel#exec(java.util.List)
   */
  public Object exec(@SuppressWarnings("rawtypes") List args)
    throws TemplateModelException {
    StringBuilder result = new StringBuilder("");

    for (Object obj : args) {
      if (obj != null) {
        result.append(obj.toString());
      }
    }

    return result.toString();
  }
}
