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
import org.alfresco.model.ApplicationModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.action.evaluator.BaseActionEvaluator;
import org.alfresco.web.bean.repository.Node;

import javax.faces.context.FacesContext;

import static eu.cec.digit.circabc.web.bean.navigation.NavigableNodeType.LIBRARY_DOSSIER;

/**
 * Evaluate if the notification can be setted on the current node.
 *
 * @author Yanick Pignot
 */
public class ViewNotificationEvaluator extends BaseActionEvaluator {

    private static final long serialVersionUID = 308051234539899525L;

    /**
     * Return false if the given node is a dossier
     */
    public boolean evaluate(final Node node) {
        final DictionaryService dService = Services
                .getAlfrescoServiceRegistry(FacesContext.getCurrentInstance()).getDictionaryService();
        final QName type = node.getType();

        if (ApplicationModel.TYPE_FILELINK.equals(type) ||
                ApplicationModel.TYPE_FOLDERLINK.equals(type) ||
                dService.isSubClass(type, ApplicationModel.TYPE_FILELINK) ||
                dService.isSubClass(type, ApplicationModel.TYPE_FOLDERLINK) ||
                LIBRARY_DOSSIER.isNodeFromType(node)) {
            return false;
        } else {
            return true;
        }
    }
}
