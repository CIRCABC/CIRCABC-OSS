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

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import java.util.List;
import org.alfresco.repo.template.BaseTemplateProcessorExtension;

/**
 * Freemarker method to concat any object as a String
 *
 * @author Yanick Pignot
 */
public class ConcatAsStringMethod
  extends BaseTemplateProcessorExtension
  implements TemplateMethodModelEx {

  /**
   * @see freemarker.template.TemplateMethodModel#exec(java.util.List)
   */
  @SuppressWarnings("unchecked")
  public Object exec(List args) throws TemplateModelException {
    StringBuilder result = new StringBuilder("");

    for (Object obj : args) {
      if (obj != null) {
        result.append(obj.toString());
      }
    }

    return result.toString();
  }
}
