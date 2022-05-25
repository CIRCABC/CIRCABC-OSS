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

import org.alfresco.web.action.evaluator.CancelWorkflowEvaluator;
import org.alfresco.web.bean.repository.Node;

public class CircabcCancelWorkflowEvaluator extends CancelWorkflowEvaluator {

    /**
     *
     */
    private static final long serialVersionUID = -3622412155643855677L;

    public boolean evaluate(Node node) {

        boolean result = super.evaluate(node);
        if (!result) {
            IgServicesAdminEvaluator igServicesAdminEvaluator = new IgServicesAdminEvaluator();
            result = igServicesAdminEvaluator.evaluate(node);
        }

        return result;
    }


}
