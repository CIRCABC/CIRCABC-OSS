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

import freemarker.ext.beans.BeanModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import org.alfresco.repo.template.BaseTemplateProcessorExtension;
import org.alfresco.repo.template.TemplateNode;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

import java.util.List;

public abstract class NodeRefBaseTemplateProcessorExtension extends BaseTemplateProcessorExtension
        implements TemplateMethodModelEx {

    private NodeService nodeService;

    /**
     * @return the nodeService
     */
    protected final NodeService getNodeService() {
        return nodeService;
    }

    /**
     * @param nodeService the nodeService to set
     */
    public final void setNodeService(NodeService services) {
        this.nodeService = services;
    }

    public Object exec(List args) throws TemplateModelException {
        String result = "";

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

    public abstract String getResult(NodeRef nodeRef) throws TemplateModelException;
}
