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

import eu.cec.digit.circabc.config.CircabcConfiguration;
import freemarker.template.TemplateMethodModelEx;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Used to generate a link to the new UI
 *
 * @author Pierre Beauregard
 */
public class CategoryGroupRequesLinkMethod extends NodeRefBaseTemplateProcessorExtension
        implements TemplateMethodModelEx {

    @Override
    public String getResult(NodeRef nodeRef) {

        String url =
                CircabcConfiguration.getProperty(CircabcConfiguration.NEW_UI_URL)
                        + CircabcConfiguration.getProperty(CircabcConfiguration.NEW_UI_CONTEXT)
                        + (CircabcConfiguration.getProperty(CircabcConfiguration.NEW_UI_CONTEXT).endsWith("/")
                        ? "category/"
                        : "/category/")
                        + (nodeRef != null ? nodeRef.getId() : "")
                        + "/group-requests";

        return url;
    }
}
