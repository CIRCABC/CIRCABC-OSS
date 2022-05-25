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

import eu.cec.digit.circabc.web.Services;
import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.bean.repository.Node;

import javax.faces.context.FacesContext;


/**
 * Evalute if the user have rights (AnyIgServicesAdminEvaluator) to change the node preferences and
 * if the node is a container.
 *
 * @author yanick pignot
 **/


public class EditNodePrefrencesEvaluator extends AnyIgServicesAdminEvaluator {

    private static final long serialVersionUID = -1795135756859512399L;

    public boolean evaluate(final Node node) {
        if (super.evaluate(node)) {
            final QName type = node.getType();
            if (type.equals(ContentModel.TYPE_FOLDER)) {
                // the most of the cases
                return true;
            } else {
                final FacesContext context = FacesContext.getCurrentInstance();
                final ServiceRegistry registry = Services.getAlfrescoServiceRegistry(context);
                final DictionaryService dictionaryService = registry.getDictionaryService();

                return dictionaryService.isSubClass(type, ContentModel.TYPE_CONTAINER);
            }
        } else {
            return false;
        }
    }
}
