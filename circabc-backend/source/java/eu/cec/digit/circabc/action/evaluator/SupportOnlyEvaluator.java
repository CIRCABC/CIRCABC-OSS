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

import eu.cec.digit.circabc.service.support.SupportService;
import eu.cec.digit.circabc.web.Beans;
import eu.cec.digit.circabc.web.Services;
import org.alfresco.web.action.evaluator.BaseActionEvaluator;
import org.alfresco.web.bean.repository.Node;

import javax.faces.context.FacesContext;


/**
 * Due to a lack of the Alfresco security model, this evaluator tests if the current user is
 * administrator on <b>each</b> ig services.
 *
 * @author Stephane Clinckart
 **/
public class SupportOnlyEvaluator extends BaseActionEvaluator {

    private static final long serialVersionUID = -3812188271951597814L;

    public boolean evaluate(final Node node) {
        final SupportService supportService = Services
                .getCircabcServiceRegistry(FacesContext.getCurrentInstance()).getSupportService();
        final String currentUser = Beans.getWaiNavigator().getCurrentUser().getUserName();
        if (supportService.isUserFromSupport(currentUser)) {
            return true;
        } else {
            return false;
        }
    }
}
