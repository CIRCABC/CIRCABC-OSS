/*******************************************************************************
 * Copyright 2006 European Community
 *
 *  Licensed under the EUPL, Version 1.1 or - as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  You may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  https://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 ******************************************************************************/
package eu.cec.digit.circabc.action.evaluator;

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.web.Services;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.web.bean.repository.Node;

import javax.faces.context.FacesContext;
import java.util.List;

import static eu.cec.digit.circabc.web.bean.navigation.NavigableNodeType.IG_ROOT;

/**
 * Return true if the given node
 *
 * @author Yanick Pignot
 */
public class CreateSurveyEvaluator extends AllIgServicesAdminEvaluator {

    private static final long serialVersionUID = 308059674639899525L;

    /**
     * return true if the current node is a ig root and this ig has not a survey space yet
     */
    public boolean evaluate(final Node node) {
        boolean result = false;

        if (IG_ROOT.isNodeFromType(node) && super.evaluate(node)) {
            final FacesContext fc = FacesContext.getCurrentInstance();
            final NodeService nodeService = Services.getAlfrescoServiceRegistry(fc).getNodeService();

            final List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(node.getNodeRef());
            for (final ChildAssociationRef assoc : childAssocs) {
                if (nodeService.hasAspect(assoc.getChildRef(), CircabcModel.ASPECT_SURVEY_ROOT)) {
                    result = false;
                    break;
                } else {
                    result = true;
                }
            }
        }

        return result;
    }

}
