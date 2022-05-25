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

import eu.cec.digit.circabc.business.api.BusinessRegistry;
import eu.cec.digit.circabc.service.CircabcServiceRegistry;
import org.alfresco.service.ServiceRegistry;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.jsf.FacesContextUtils;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

/**
 * Helper class for accessing Circabc and Alfresco Service utilities.
 *
 * @author Stephane Clinckart
 */
public class Services {

    /**
     * reference to the ServiceRegistry
     */
    private static CircabcServiceRegistry serviceRegistry = null;

    /**
     * reference to the ServiceRegistry
     */
    private static ServiceRegistry alfrescoServiceRegistry = null;

    /**
     * refernce to the BusinessRegistry
     */
    private static BusinessRegistry businessRegistry = null;

    private Services() {

    }


    /**
     * Return the Circabc Service Registry
     *
     * @param context Faces Context
     * @return the Service Registry
     */
    public static CircabcServiceRegistry getCircabcServiceRegistry(
            FacesContext context) {
        if (serviceRegistry == null) {
            serviceRegistry = (CircabcServiceRegistry) FacesContextUtils
                    .getRequiredWebApplicationContext(context).getBean(
                            CircabcServiceRegistry.CIRCABC_SERVICE_REGISTRY);
        }
        return serviceRegistry;
    }

    /**
     * Return the Repository Service Registry
     *
     * @param context Servlet Context
     * @return the Service Registry
     */
    public static CircabcServiceRegistry getCircabcServiceRegistry(ServletContext context) {
        if (serviceRegistry == null) {
            serviceRegistry = (CircabcServiceRegistry) WebApplicationContextUtils
                    .getRequiredWebApplicationContext(context).getBean(
                            CircabcServiceRegistry.CIRCABC_SERVICE_REGISTRY);
        }
        return serviceRegistry;
    }

    /**
     * Return the Native Service Registry provided by alfresco
     *
     * @param context Faces Context
     * @return the Native Service Registry provided by alfresco
     */
    public static ServiceRegistry getAlfrescoServiceRegistry(
            FacesContext context) {
        if (alfrescoServiceRegistry == null) {
            alfrescoServiceRegistry = (ServiceRegistry) FacesContextUtils
                    .getRequiredWebApplicationContext(context).getBean(
                            ServiceRegistry.SERVICE_REGISTRY);
        }
        return alfrescoServiceRegistry;
    }

    /**
     * Return the Native Service Registry provided by alfresco
     *
     * @param context Servlet Context
     * @return the Native Service Registry provided by alfresco
     */
    public static ServiceRegistry getAlfrescoServiceRegistry(ServletContext context) {
        if (alfrescoServiceRegistry == null) {
            alfrescoServiceRegistry = (ServiceRegistry) WebApplicationContextUtils
                    .getRequiredWebApplicationContext(context).getBean(
                            ServiceRegistry.SERVICE_REGISTRY);
        }
        return alfrescoServiceRegistry;
    }

    /**
     * Return the Circabc Business Service Registry
     *
     * @param context Faces Context
     * @return the Service Registry
     */
    public static BusinessRegistry getBusinessRegistry(FacesContext context) {
        if (businessRegistry == null) {
            businessRegistry = (BusinessRegistry) FacesContextUtils
                    .getRequiredWebApplicationContext(context).getBean(
                            BusinessRegistry.BUSINESS_REGISTRY);
        }
        return businessRegistry;
    }

    /**
     * Return the Repository Service Registry
     *
     * @param context Servlet Context
     * @return the Service Registry
     */
    public static BusinessRegistry getBusinessRegistry(ServletContext context) {
        if (businessRegistry == null) {
            businessRegistry = (BusinessRegistry) WebApplicationContextUtils
                    .getRequiredWebApplicationContext(context).getBean(
                            BusinessRegistry.BUSINESS_REGISTRY);
        }
        return businessRegistry;
    }

}
