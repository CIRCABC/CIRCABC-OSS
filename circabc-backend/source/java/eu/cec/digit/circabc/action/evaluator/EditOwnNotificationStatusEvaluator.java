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

import eu.cec.digit.circabc.service.struct.ManagementService;
import eu.cec.digit.circabc.web.Beans;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.bean.override.CircabcNavigationBean;
import javax.faces.context.FacesContext;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.User;

/**
 * Evaluate if the current user can set its own notification status for the given node
 *
 * @author yanick pignot
 **/

public class EditOwnNotificationStatusEvaluator
  extends ViewNotificationEvaluator {

  private static final long serialVersionUID = -1711532256856435555L;

  public boolean evaluate(final Node node) {
    if (!super.evaluate(node) || node.isLocked()) {
      return false;
    }

    final CircabcNavigationBean navigator = Beans.getWaiNavigator();
    final User user = navigator.getCurrentUser();
    if (user == null || user.isAdmin() || navigator.isGuest()) {
      // guest and alfresco admin can't
      return false;
    }

    final ManagementService managementService =
      Services.getCircabcServiceRegistry(
        FacesContext.getCurrentInstance()
      ).getManagementService();
    final NodeRef igRef = managementService.getCurrentInterestGroup(
      node.getNodeRef()
    );
    if (igRef == null) {
      // we are not under an interest group
      return false;
    }

    return true;
  }
}
