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
import freemarker.template.TemplateScalarModel;
import org.alfresco.repo.template.BaseTemplateProcessorExtension;
import org.springframework.extensions.surf.util.I18NUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Freemarker method to translate a key and to apply some optional parameters
 *
 * @author Yanick Pignot
 * <p>Migration 3.1 -> 3.4.6 - 02/12/2011 I18NUtil was moved to Spring. This class seems to be
 * developed for CircaBC
 */
public class I18NFormatMessageMethod extends BaseTemplateProcessorExtension
        implements TemplateMethodModelEx {

    /**
     * @see freemarker.template.TemplateMethodModel#exec(java.util.List)
     */
    @SuppressWarnings("unchecked")
    public Object exec(List args) throws TemplateModelException {
        String result = "";

        final int size = args.size();

        String key = null;
        List params = null;

        if (size > 0) {
            boolean first = true;

            for (Object arg : args) {
                if (arg instanceof TemplateScalarModel) {
                    if (first) {
                        key = ((TemplateScalarModel) arg).getAsString();

                        first = false;
                    } else {
                        if (params == null) {
                            params = new ArrayList(size - 1);
                        }
                        params.add(((TemplateScalarModel) arg).getAsString());
                    }
                }
            }
        }

        if (key != null) {
            if (params != null) {
                result = I18NUtil.getMessage(key, params.toArray());
            } else {
                result = I18NUtil.getMessage(key);
            }
        }

        return result == null ? "" : result;
    }

    public void setWebMessageBundle(String bundle) {
        I18NUtil.registerResourceBundle(bundle);
    }
}
