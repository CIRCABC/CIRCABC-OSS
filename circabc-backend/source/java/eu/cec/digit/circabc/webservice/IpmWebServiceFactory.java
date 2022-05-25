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
/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.webservice;

import eu.cec.digit.circabc.config.CircabcConfiguration;
import ipm.webservice.IpmServiceServiceLocator;
import ipm.webservice.IpmServiceSoapBindingStub;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.rpc.ServiceException;
import java.util.Properties;

/**
 * The factory which creates all web services concerning IPM. The location of the IPM server to
 * connect to is specified in the file circabc-settings.properties located in /alfresco/extension.
 *
 * @author Matthieu Sprunck
 */
public class IpmWebServiceFactory {

    /**
     * Log
     */
    private static final Log logger = LogFactory.getLog(IpmWebServiceFactory.class);

    private static final String IPM_LOCATION = "ipm.location";
    private static final String IPM_LOGIN = "ipm.login";
    private static final String IPM_PASSWORD = "ipm.password";

    /**
     * Default endpoint address *
     */
    private static final String DEFAULT_ENDPOINT_ADDRESS = "http://localhost:7001/ipm";

    /**
     * Service addresses
     */
    private static final String IPM_SERVICE_ADDRESS = "/services/IpmService";

    /**
     *
     */
    private static Properties props = null;

    /**
     * Services
     */
    private static IpmServiceSoapBindingStub ipmService = null;


    static {
        String ipmLogin, ipmPassword;

        if (ipmService == null) {
            try {
                // Get the authentication service
                IpmServiceServiceLocator locator = new IpmServiceServiceLocator();
                locator.setIpmServiceEndpointAddress(getEndpointAddress()
                        + IPM_SERVICE_ADDRESS);
                if (logger.isDebugEnabled()) {
                    logger.debug("Get the IPM webservice from the end point: "
                            + locator.getIpmServiceAddress());
                }
                ipmLogin = props.getProperty(IPM_LOGIN, "circabc");
                ipmPassword = props.getProperty(IPM_PASSWORD, "changeit");

                ipmService = (IpmServiceSoapBindingStub) locator.getIpmService();
                ipmService.setUsername(ipmLogin);
                ipmService.setPassword(ipmPassword);
            } catch (final ServiceException jre) {
                if (logger.isErrorEnabled() == true) {
                    if (jre.getLinkedCause() != null) {
                        if (logger.isErrorEnabled()) {
                            logger.error("Error", jre.getLinkedCause());
                        }
                    } else {
                        if (logger.isErrorEnabled()) {
                            logger.error("Error", jre);
                        }
                    }
                }

                throw new RuntimeException(
                        "Error creating authentication service: "
                                + jre.getMessage(), jre);
            }

            // Time out after a minute
            ipmService.setTimeout(60000);
        }
    }


    /**
     * Gets the Circabc user service
     */
    public static IpmServiceSoapBindingStub getIpmService() {
        return ipmService;
    }

    /**
     * Gets the end point address from the properties file
     */
    private static String getEndpointAddress() {
        String endPoint = DEFAULT_ENDPOINT_ADDRESS;

        if (props == null) {
            props = CircabcConfiguration.getProperties();
        }
        endPoint = props.getProperty(IPM_LOCATION, DEFAULT_ENDPOINT_ADDRESS);

        if (logger.isDebugEnabled()) {
            logger.debug("Using endpoint " + endPoint);
        }

        return endPoint;
    }
}
