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

import eu.cec.digit.circabc.service.profile.permissions.CategoryPermissions;
import eu.cec.digit.circabc.service.profile.permissions.CircabcRootPermissions;
import eu.cec.digit.circabc.service.profile.permissions.DirectoryPermissions;
import eu.cec.digit.circabc.web.Beans;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNodeType;
import org.alfresco.web.action.evaluator.BaseActionEvaluator;
import org.alfresco.web.bean.repository.Node;


/**
 * Evaluate if the current user can send an email to the members.
 *
 * @author Yanick Pignot
 **/
public class MailToMembersEvaluator extends BaseActionEvaluator {

    private static final long serialVersionUID = 969643930963034329L;

    public boolean evaluate(final Node node) {
        if (NavigableNodeType.CIRCABC_ROOT.isNodeFromType(node)) {
            if (Beans.getWaiNavigator().getCurrentUser().isAdmin()) {
                return true;
            } else {
                return node.hasPermission(CircabcRootPermissions.CIRCABCMANAGEMEMBERS.toString());
            }
        } else if (NavigableNodeType.CATEGORY.isNodeFromType(node)) {
            return node.hasPermission(CategoryPermissions.CIRCACATEGORYMANAGEMEMBERS.toString());
        } else {
            return node.hasPermission(DirectoryPermissions.DIRMANAGEMEMBERS.toString());
        }
    }
}
