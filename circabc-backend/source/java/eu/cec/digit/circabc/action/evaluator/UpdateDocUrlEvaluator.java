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
import org.alfresco.web.action.evaluator.UpdateDocEvaluator;
import org.alfresco.web.bean.repository.Node;

/**
 * UpdateDocURL Evaluator - Extends the update evaluation for an URL content.
 *
 * @author David Ferraz
 */
public class UpdateDocUrlEvaluator extends UpdateDocEvaluator {

    private static final long serialVersionUID = 6040963610213633893L;

    /**
     * @see org.alfresco.web.action.ActionEvaluator#evaluate(org.alfresco.web.bean.repository.Node)
     */
    public boolean evaluate(Node node) {
        if (super.evaluate(node)) {
            return !node.hasAspect(DocumentModel.ASPECT_URLABLE);
        } else {
            return false;
        }
    }
}
