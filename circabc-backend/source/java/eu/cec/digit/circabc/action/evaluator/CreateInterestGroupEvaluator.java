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
import eu.cec.digit.circabc.service.profile.ProfileManagerServiceFactory;
import eu.cec.digit.circabc.service.profile.permissions.CategoryPermissions;
import eu.cec.digit.circabc.web.Beans;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNodeType;
import eu.cec.digit.circabc.web.bean.override.CircabcNavigationBean;
import org.alfresco.web.action.evaluator.BaseActionEvaluator;
import org.alfresco.web.bean.repository.Node;

import javax.faces.context.FacesContext;
import java.util.Set;

/**
 * Evaluate if the user can create an interest group.
 * <p>
 * The user must be In profile CategoryAmdin !!!
 *
 * @author Yanick Pignot
 */
public class CreateInterestGroupEvaluator extends BaseActionEvaluator {

    private static final long serialVersionUID = 308051234539899525L;

    /**
     * Return false if the given node is a dossier
     */
    public boolean evaluate(final Node node) {

        if (NavigableNodeType.CATEGORY.isNodeFromType(node) == false) {
            return false;
        } else if (node.hasPermission(CategoryPermissions.CIRCACATEGORYADMIN.toString()) == false) {
            return false;
        } else {
            final ProfileManagerServiceFactory factory = Services
                    .getCircabcServiceRegistry(FacesContext.getCurrentInstance())
                    .getProfileManagerServiceFactory();
            final CategoryProfileManagerService profileManager = factory
                    .getCategoryProfileManagerService();

            Set<String> admins = profileManager.getPersonInProfile(node.getNodeRef(),
                    CategoryProfileManagerService.Profiles.CIRCA_CATEGORY_ADMIN);

            return admins.contains(getCurrentUserName());
        }
    }

    private String getCurrentUserName() {
        final CircabcNavigationBean navigator = Beans.getWaiNavigator();

        return (navigator.isGuest()) ? null : navigator.getCurrentUser().getUserName();
    }
}
