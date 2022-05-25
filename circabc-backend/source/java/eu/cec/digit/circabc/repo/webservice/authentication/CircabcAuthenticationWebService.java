/**
 * ***************************************************************************** Copyright 2006
 * European Community
 *
 * <p>Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 *
 * <p>https://joinup.ec.europa.eu/software/page/eupl
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * ****************************************************************************
 */
package eu.cec.digit.circabc.repo.webservice.authentication;

import eu.cec.digit.circabc.repo.web.scripts.bean.TicketValidator;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.webservice.Utils;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.rmi.RemoteException;

public class CircabcAuthenticationWebService implements CircabcAuthenticationServiceSoapPort {

    private static Log logger = LogFactory.getLog(CircabcAuthenticationWebService.class);

    private AuthenticationService authenticationService;

    private AuthenticationComponent authenticationComponent;
    private TicketValidator ticketValidator;

    /**
     * Sets the AuthenticationService instance to use
     *
     * @param authenticationSvc The AuthenticationService
     */
    public void setAuthenticationService(AuthenticationService authenticationSvc) {
        this.authenticationService = authenticationSvc;
    }

    /**
     * Set the atuthentication component
     */
    public void setAuthenticationComponent(AuthenticationComponent authenticationComponent) {
        this.authenticationComponent = authenticationComponent;
    }

    /**
     * @see org.alfresco.repo.webservice.authentication.AuthenticationServiceSoapPort#startSession(java.lang.String,
     * java.lang.String)
     */
    public AuthenticationResult startSession(String username, String ecasProxyTicket)
            throws RemoteException {
        try {
            String userName = ticketValidator.validateTicket(ecasProxyTicket);

            if (userName != null) {
                final String user = userName;
                if (user.equalsIgnoreCase(username)) {
                    this.authenticationComponent.setCurrentUser(username);
                    String ticket = this.authenticationService.getCurrentTicket();

                    if (logger.isDebugEnabled()) {
                        logger.debug("Issued ticket '" + ticket + "' for '" + username + "'");
                    }

                    return new AuthenticationResult(username, ticket, Utils.getSessionId());
                } else {
                    if (logger.isErrorEnabled()) {
                        logger.error(
                                "Can not start session: ecas proxy ticket '"
                                        + ecasProxyTicket
                                        + "' is valid but user is invalid");
                    }
                    throw new AuthenticationFault(100, "Invalid userName and ecasProxyTicket");
                }
            } else {
                if (logger.isErrorEnabled()) {
                    logger.error(
                            "Can not start session ecas proxy ticket '"
                                    + ecasProxyTicket
                                    + "' for '"
                                    + username
                                    + "'");
                }
                throw new AuthenticationFault(100, "Invalid userName and ecasProxyTicket");
            }
        } catch (AuthenticationException ae) {
            if (logger.isErrorEnabled()) {
                logger.error(
                        "Can not start session ecas proxy ticket '"
                                + ecasProxyTicket
                                + "' for '"
                                + username
                                + "'",
                        ae);
            }
            throw new AuthenticationFault(100, ae.getMessage());
        } catch (Throwable e) {
            if (logger.isErrorEnabled()) {
                logger.error(
                        "Can not start session ecas proxy ticket '"
                                + ecasProxyTicket
                                + "' for '"
                                + username
                                + "'",
                        e);
            }
            throw new AuthenticationFault(0, e.getMessage());
        }
    }

    public TicketValidator getTicketValidator() {
        return ticketValidator;
    }

    public void setTicketValidator(TicketValidator ticketValidator) {
        this.ticketValidator = ticketValidator;
    }
}
