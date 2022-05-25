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
import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.model.EventModel;
import freemarker.template.TemplateMethodModelEx;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Used to generate a link to the new UI
 *
 * @author Pierre Beauregard
 */
public class AgendaNodeLinkMethod extends NodeRefBaseTemplateProcessorExtension
        implements TemplateMethodModelEx {

    @Override
    public String getResult(NodeRef nodeRef) {

        NodeRef groupRef = findInterestGroupRoot(nodeRef);
        return CircabcConfiguration.getProperty(CircabcConfiguration.NEW_UI_URL)
                + CircabcConfiguration.getProperty(CircabcConfiguration.NEW_UI_CONTEXT)
                + (CircabcConfiguration.getProperty(CircabcConfiguration.NEW_UI_CONTEXT).endsWith("/")
                ? "group/"
                : "/group/")
                + (groupRef != null ? groupRef.getId() : "")
                + "/agenda/"
                + nodeRef.getId()
                + "/details";
    }

    private NodeRef findInterestGroupRoot(NodeRef nodeRef) {
        NodeRef parent = null;

        if (getNodeService().hasAspect(nodeRef, CircabcModel.ASPECT_EVENT)
                || getNodeService().getType(nodeRef).equals(EventModel.TYPE_EVENT)) {
            parent = getNodeService().getPrimaryParent(nodeRef).getParentRef();
            while (!getNodeService().hasAspect(parent, CircabcModel.ASPECT_IGROOT)) {
                parent = getNodeService().getPrimaryParent(parent).getParentRef();
            }
        }

        return parent;
    }
}
