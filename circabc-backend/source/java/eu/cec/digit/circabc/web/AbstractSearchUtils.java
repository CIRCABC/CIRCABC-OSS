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
package eu.cec.digit.circabc.web;

import eu.cec.digit.circabc.service.profile.ProfileManagerServiceFactory;
import eu.cec.digit.circabc.service.struct.ManagementService;
import eu.cec.digit.circabc.service.user.UserService;
import eu.cec.digit.circabc.web.bean.override.CircabcNavigationBean;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;

import javax.faces.context.FacesContext;

/*package*/ abstract class AbstractSearchUtils {

    /**
     * Minimum characters for a user search query
     */
    public static final int MIN_CHAR_ALLOWED_FOR_QUERY = 3;

    /**
     * Maximim number of elements put in the list
     */
    public static final int MAX_ELEMENTS_IN_LIST = 1000;

    public static final String ALL_PROFILE = "all_profile";

    protected final static String STAR_WILDCARD_REGEX = "[\\*\\ ]*";


    /**
     * @return the profileManagerServiceFactory
     */
    protected static CircabcNavigationBean getNavigator() {
        return Beans.getWaiNavigator();
    }


    /**
     * @return the profileManagerServiceFactory
     */
    protected static ProfileManagerServiceFactory getProfileManagerServiceFactory() {
        return Services.getCircabcServiceRegistry(
                FacesContext.getCurrentInstance())
                .getProfileManagerServiceFactory();
    }

    /**
     * @return the managementService
     */
    protected static ManagementService getManagementService() {
        return Services.getCircabcServiceRegistry(
                FacesContext.getCurrentInstance()).getManagementService();
    }

    /**
     * @return the userService
     */
    protected static UserService getUserService() {
        return Services.getCircabcServiceRegistry(
                FacesContext.getCurrentInstance()).getUserService();
    }

    /**
     * @return the person service
     */
    protected static PersonService getPersonService() {
        return Services.getAlfrescoServiceRegistry(
                FacesContext.getCurrentInstance()).getPersonService();
    }

    /**
     * @return the nodeService
     */
    protected static NodeService getNodeService() {
        return Services.getAlfrescoServiceRegistry(
                FacesContext.getCurrentInstance()).getNodeService();
    }

    /**
     * @return the permissionService
     */
    protected static PermissionService getPermissionService() {
        return Services.getAlfrescoServiceRegistry(
                FacesContext.getCurrentInstance()).getPermissionService();
    }

    /**
     * @return the Authority Service
     */
    protected static AuthorityService getAuthorityService() {
        return Services.getAlfrescoServiceRegistry(
                FacesContext.getCurrentInstance()).getAuthorityService();
    }
}
