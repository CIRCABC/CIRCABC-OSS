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

import eu.cec.digit.circabc.service.dynamic.property.DynamicProperty;
import eu.cec.digit.circabc.service.dynamic.property.DynamicPropertyService;
import freemarker.ext.beans.BeanModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import org.alfresco.repo.template.BaseTemplateProcessorExtension;
import org.alfresco.repo.template.TemplateNode;
import org.alfresco.service.cmr.repository.NodeRef;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DynamicPropertyDescriptionMethod extends BaseTemplateProcessorExtension
        implements TemplateMethodModelEx {

    private DynamicPropertyService dynamicPropertiesService;

    public Object exec(List args) throws TemplateModelException {
        List<String> result = Collections.emptyList();
        if (args.size() == 1) {
            // arg 0 must be a wrapped TemplateNode object
            final BeanModel arg0 = (BeanModel) args.get(0);

            NodeRef ref = null;
            if (arg0.getWrappedObject() instanceof NodeRef) {
                ref = (NodeRef) arg0.getWrappedObject();
            } else if (arg0.getWrappedObject() instanceof TemplateNode) {
                final TemplateNode templateNode = (TemplateNode) arg0.getWrappedObject();

                ref = templateNode.getNodeRef();
            }

            if (ref != null) {
                result = getResult(ref);
            }
        }

        return result;
    }

    private List<String> getResult(NodeRef ref) {
        List<String> result = new ArrayList<>();
        final List<DynamicProperty> dynamicProperties =
                dynamicPropertiesService.getDynamicProperties(ref);
        for (DynamicProperty dynamicProperty : dynamicProperties) {
            result.add(dynamicProperty.getLabel().getDefaultValue());
        }
        return result;
    }

    public DynamicPropertyService getDynamicPropertiesService() {
        return dynamicPropertiesService;
    }

    public void setDynamicPropertiesService(DynamicPropertyService dynamicPropertiesService) {
        this.dynamicPropertiesService = dynamicPropertiesService;
    }
}
