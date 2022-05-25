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

import eu.cec.digit.circabc.model.DocumentModel;
import eu.cec.digit.circabc.web.Beans;
import eu.cec.digit.circabc.web.Services;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.action.evaluator.BaseActionEvaluator;
import org.alfresco.web.bean.repository.Node;

import javax.faces.context.FacesContext;

/**
 * We can't email a document if it is an empty translation.
 *
 * @author Yanick Pignot
 */
public class MailMeNodeEvaluator extends BaseActionEvaluator {

    private static final long serialVersionUID = 2164369999290163419L;

    public boolean evaluate(final Node node) {
        if (Beans.getWaiNavigator().getIsGuest()) {
            return false;
        }
        if (node.hasAspect(org.alfresco.model.ContentModel.ASPECT_MULTILINGUAL_EMPTY_TRANSLATION)) {
            return false;
        }
        Node containerNode = null;
        if (node.hasAspect(org.alfresco.model.ContentModel.ASPECT_MULTILINGUAL_DOCUMENT)) {
            MultilingualContentService multilingualContentService = Services
                    .getAlfrescoServiceRegistry(FacesContext.getCurrentInstance())
                    .getMultilingualContentService();
            NodeRef containerNodeRef = multilingualContentService
                    .getTranslationContainer(node.getNodeRef());
            containerNode = new Node(containerNodeRef);
        } else {
            containerNode = node;
        }
        Object securityRanking = containerNode.getProperties()
                .get(DocumentModel.PROP_SECURITY_RANKING.toString());
        if (securityRanking != null) {
            final String securityRankingString = securityRanking.toString();
            if (!securityRankingString.equalsIgnoreCase("PUBLIC")) {
                return false;
            }

        }
        return true;


    }

}
