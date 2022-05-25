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
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import java.io.Serializable;
import java.util.Map;

/**
 * Freemarker method to display the title of a node or its name if no title
 *
 * @author Yanick Pignot
 */
public class TitleOrNameMethod extends NodeRefBaseTemplateProcessorExtension
        implements TemplateMethodModelEx {

    @Override
    public String getResult(final NodeRef nodeRef) {

        final Map<QName, Serializable> props = getNodeService().getProperties(nodeRef);
        final Serializable titleObj = props.get(ContentModel.PROP_TITLE);
        String result = props.get(ContentModel.PROP_NAME).toString();

        if (titleObj != null && titleObj instanceof MLText) {
            MLText title = (MLText) titleObj;
            if (!"".equals(title.getDefaultValue())) {
                result = title.getDefaultValue();
            }
        } else if (titleObj != null && titleObj instanceof String) {
            if (!"".equals((String) titleObj)) {
                result = (String) titleObj;
            }
        }

        return result;
    }
}
