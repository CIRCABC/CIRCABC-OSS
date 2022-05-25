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
import eu.cec.digit.circabc.web.repository.CategoryNode;
import eu.cec.digit.circabc.web.repository.IGServicesNode;
import eu.cec.digit.circabc.web.repository.InterestGroupNode;
import org.alfresco.web.action.evaluator.BaseActionEvaluator;
import org.alfresco.web.bean.repository.Node;


/**
 * Due to a lack of the Alfresco security model, this evaluator tests if the current user is
 * administrator on <b>at least one</b> ig services.
 *
 * @author yanick pignot
 **/


public class AnyIgServicesAdminEvaluator extends BaseActionEvaluator {

    private static final long serialVersionUID = -1795135756856439999L;

    public boolean evaluate(final Node node) {

        final InterestGroupNode ig = (InterestGroupNode) Beans.getWaiNavigator().getCurrentIGRoot();
        final CategoryNode cat = (CategoryNode) Beans.getWaiNavigator().getCurrentCategory();

        if (cat != null && cat.hasPermission(CategoryPermissions.CIRCACATEGORYADMIN.toString())) {
            return true;
        } else if (ig == null) {
            return false;
        }

        final IGServicesNode directory = ig.getDirectory();
        if (directory != null && directory.hasPermission(DirectoryPermissions.DIRADMIN.toString())) {
            return true;
        }

        final IGServicesNode library = ig.getLibrary();
        if (library != null && library.hasPermission(LibraryPermissions.LIBADMIN.toString())) {
            return true;
        }

        final IGServicesNode newsgroup = ig.getNewsgroup();
        if (newsgroup != null && newsgroup.hasPermission(NewsGroupPermissions.NWSADMIN.toString())) {
            return true;
        }

        final IGServicesNode event = ig.getEvent();
        if (event != null && event.hasPermission(EventPermissions.EVEADMIN.toString())) {
            return true;
        }

        final IGServicesNode information = ig.getInformation();
        if (information != null && information
                .hasPermission(InformationPermissions.INFADMIN.toString())) {
            return true;
        }

        return false;
    }
}
