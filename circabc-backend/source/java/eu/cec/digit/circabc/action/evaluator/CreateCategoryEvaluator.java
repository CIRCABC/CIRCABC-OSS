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

import eu.cec.digit.circabc.web.Beans;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNodeType;
import eu.cec.digit.circabc.web.bean.override.CircabcNavigationBean;
import org.alfresco.web.action.evaluator.BaseActionEvaluator;
import org.alfresco.web.bean.repository.Node;

/**
 * Evaluate if the user can create a category.
 *
 * @author Yanick Pignot
 */
public class CreateCategoryEvaluator extends BaseActionEvaluator {

    private static final long serialVersionUID = 308051234539899525L;

    /**
     * Return false if the given node is a dossier
     */
    public boolean evaluate(final Node node) {
        if (NavigableNodeType.CIRCABC_ROOT.isNodeFromType(node) == true
                || NavigableNodeType.CATEGORY_HEADER.isNodeFromType(node) == true) {
            return isSuperAdmin() || isCircabcAdmin();
        } else {
            return false;
        }
    }

    private boolean isSuperAdmin() {
        final CircabcNavigationBean navigator = Beans.getWaiNavigator();

        return navigator.getCurrentUser().isAdmin();
    }

    private boolean isCircabcAdmin() {
        final CircabcNavigationBean navigator = Beans.getWaiNavigator();

        return navigator.getCircabcHomeNode().hasPermission("CircaBCAdmin");
    }
}
