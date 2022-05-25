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

import eu.cec.digit.circabc.CircabcConfig;
import eu.cec.digit.circabc.service.external.repositories.ExternalRepositoriesManagementService;
import org.alfresco.web.bean.repository.Node;

/**
 * Evaluates if the external repositories management action has to be shown in the admin page.
 *
 * @author schwerr
 */
public class ManageExternalRepositoriesActionEvaluator extends
        AllIgServicesAdminEvaluator {

    private static final long serialVersionUID = -4285205449710953828L;

    private static ExternalRepositoriesManagementService externalRepositoriesManagementService = null;

    /**
     * Sets the value of the externalRepositoriesManagementService
     *
     * @param externalRepositoriesManagementService the externalRepositoriesManagementService to set.
     */
    public static void setExternalRepositoriesManagementService(
            ExternalRepositoriesManagementService externalRepositoriesManagementService) {
        ManageExternalRepositoriesActionEvaluator.externalRepositoriesManagementService = externalRepositoriesManagementService;
    }

    /**
     * @see eu.cec.digit.circabc.action.evaluator.AllIgServicesAdminEvaluator#evaluate(org.alfresco.web.bean.repository.Node)
     */
    @Override
    public boolean evaluate(Node node) {
        return super.evaluate(node) && externalRepositoriesManagementService.isOperational()
                && CircabcConfig.ENT;
    }
}
