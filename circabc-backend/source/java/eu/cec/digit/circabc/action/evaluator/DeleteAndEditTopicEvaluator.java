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
 * Evaluates whether the current user can delete or edit a topic
 *
 * @author yanick pignot
 */
public class DeleteAndEditTopicEvaluator extends DeleteAndEditForumEvaluator {

    private static final long serialVersionUID = 6427824694092387176L;

    public boolean evaluate(final Node node) {
        return super.evaluate(node);
    }
}
