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

import org.alfresco.web.bean.repository.Node;


/**
 * @author Yanick Pignot
 */
public class DiscussionCopyEvaluator extends
        org.alfresco.web.action.evaluator.DiscussionCopyEvaluator {

    private static final long serialVersionUID = 216431234585621419L;

    public boolean evaluate(final Node node) {
        return !node.isLocked() && super.evaluate(node);
    }

}
