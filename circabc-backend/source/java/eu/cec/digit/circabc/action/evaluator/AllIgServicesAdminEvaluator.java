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

import eu.cec.digit.circabc.service.profile.permissions.*;
import eu.cec.digit.circabc.web.Beans;
import eu.cec.digit.circabc.web.repository.IGServicesNode;
import eu.cec.digit.circabc.web.repository.InterestGroupNode;
import org.alfresco.web.action.evaluator.BaseActionEvaluator;
import org.alfresco.web.bean.repository.Node;


/**
 * Due to a lack of the Alfresco security model, this evaluator tests if the current user is
 * administrator on <b>each</b> ig services.
 *
 * @author yanick pignot
 **/
public class AllIgServicesAdminEvaluator extends BaseActionEvaluator {

    private static final long serialVersionUID = -1711532256856439999L;

    public boolean evaluate(final Node node) {

        boolean isAdmin = false;

        final InterestGroupNode ig = (InterestGroupNode) Beans.getWaiNavigator().getCurrentIGRoot();

        if (ig == null) {
            return false;
        }

        final IGServicesNode directory = ig.getDirectory();
        if (directory == null) {
            // if a service is set as null, it means that the user can't see it
            return false;
        }
        final IGServicesNode library = ig.getLibrary();
        if (library == null) {
            // if a service is set as null, it means that the user can't see it
            return false;
        }
        final IGServicesNode newsgroup = ig.getNewsgroup();
        if (newsgroup == null) {
            // if a service is set as null, it means that the user can't see it
            return false;
        }
        final IGServicesNode event = ig.getEvent();
        if (event == null) {
            // if a service is set as null, it means that the user can't see it
            return false;
        }
        final IGServicesNode information = ig.getInformation();
        if (information == null) {
            // if a service is set as null, it means that the user can't see it
            return false;
        }
        final IGServicesNode survey = ig.getSurvey();

        // The survey root can be null if it is not created.

        isAdmin = ig.hasPermission(DirectoryPermissions.DIRMANAGEMEMBERS.toString())
                && newsgroup.hasPermission(NewsGroupPermissions.NWSADMIN.toString())
                && library.hasPermission(LibraryPermissions.LIBADMIN.toString())
                && event.hasPermission(EventPermissions.EVEADMIN.toString())
                && information.hasPermission(InformationPermissions.INFADMIN.toString());

        //TODO test if survey == null ... we should test if it is 	null because
        //     the user has no access on it or it really doesn't access

        if (isAdmin && survey != null) {
            isAdmin = survey.hasPermission(SurveyPermissions.SURADMIN.toString());
        }

        return isAdmin;
    }
}
