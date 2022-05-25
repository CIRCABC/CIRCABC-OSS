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

import eu.cec.digit.circabc.service.profile.CategoryProfileManagerService;
import eu.cec.digit.circabc.web.Beans;
import eu.cec.digit.circabc.web.repository.CategoryNode;
import org.alfresco.web.action.evaluator.BaseActionEvaluator;
import org.alfresco.web.bean.repository.Node;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Evaluate if current user has Delete Permission on an Interest Group (in other words: Is the
 * current user a CategoryAdmin?)
 *
 * @author Stephane Clinckart
 **/


public class DeleteIgNodeEvaluator extends BaseActionEvaluator {

    /**
     * A logger for the class
     */
    private static final Log logger = LogFactory.getLog(DeleteIgNodeEvaluator.class);

    private static final long serialVersionUID = -3468920341034630518L;

    /**
     *
     */


    public boolean evaluate(final Node node) {

        boolean isAdmin = false;

        final CategoryNode categoryNode = (CategoryNode) Beans.getWaiNavigator().getCurrentCategory();

        if (categoryNode != null) {
            if (categoryNode.hasPermission(CategoryProfileManagerService.Profiles.CIRCA_CATEGORY_ADMIN)) {
                isAdmin = true;
            }
        }
        if (logger.isTraceEnabled()) {
            logger.trace("return :" + isAdmin);
        }

        return isAdmin;
    }
}
