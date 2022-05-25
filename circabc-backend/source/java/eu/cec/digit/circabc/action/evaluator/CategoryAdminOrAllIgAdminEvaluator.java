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
import eu.cec.digit.circabc.web.Beans;
import eu.cec.digit.circabc.web.repository.CategoryNode;
import org.alfresco.web.bean.repository.Node;


/**
 * Due to a lack of the Alfresco security model, this evaluator tests if the current user is
 * administrator on <b>each</b> ig services or CategorAdmin.
 *
 * @author Stephane Clinckart
 **/
public class CategoryAdminOrAllIgAdminEvaluator extends AllIgServicesAdminEvaluator {


    /**
     *
     */
    private static final long serialVersionUID = 5785343930963034329L;

    public boolean evaluate(final Node node) {
        boolean isAdmin = false;
        final CategoryNode categoryNode = (CategoryNode) Beans.getWaiNavigator().getCurrentCategory();
        if (categoryNode != null && categoryNode.getNodeRef().equals(node.getNodeRef())) {
            //Current node is the category
            //Chek if current user is categoryAdmin
            isAdmin = categoryNode.hasPermission(CategoryPermissions.CIRCACATEGORYADMIN.toString());
        }

        if (!isAdmin) {
            isAdmin = super.evaluate(node);
        }
        return isAdmin;
    }
}
