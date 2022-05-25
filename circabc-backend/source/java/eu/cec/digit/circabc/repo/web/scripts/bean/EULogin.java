package eu.cec.digit.circabc.repo.web.scripts.bean;

import eu.cec.digit.circabc.service.app.CircabcService;
import eu.cec.digit.circabc.service.user.UserService;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.*;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

public class EULogin extends DeclarativeWebScript {

    public static final String LOGIN_FAILED = "Login failed";
    /**
     * A logger for the class
     */
    static final Log logger = LogFactory.getLog(EULogin.class);
    // dependencies
    private AuthenticationService authenticationService;
    private AuthenticationComponent authenticationComponent;
    private TicketValidator ticketValidator;
    private UserService userService;
    private CircabcService circabcService;

    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public void setAuthenticationComponent(AuthenticationComponent authenticationComponent) {
        this.authenticationComponent = authenticationComponent;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.WebScriptResponse)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        // extract username
        String username = req.getParameter("u");
        if ((username == null) || username.isEmpty()) {
            throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "Username not specified");
        }
        // extract CAS ticket
        String ticket = req.getParameter("t");
        if (ticket == null) {
            throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "Ticket not specified");
        }

        try {

            String userName = this.ticketValidator.validateTicket(ticket);

            if (userName != null) {

                if (userName.equalsIgnoreCase(username)) {
                    if (!this.userService.isUserExists(userName) && !circabcService.isUserExists(userName)) {
                        final NodeRef userNodeRef = userService.createLdapUser(userName,true);
                        circabcService.addUser(userNodeRef);
                    }
                    // add ticket to model for javascript and template access
                    Map<String, Object> model = new HashMap<>(7, 1.0f);
                    // authenticate our user
                    this.authenticationComponent.setCurrentUser(username);
                    // create a new alfresco ticket
                    String alfticket = this.authenticationService.getCurrentTicket();
                    model.put("ticket", alfticket);
                    return model;
                } else {
                    throw new WebScriptException(HttpServletResponse.SC_FORBIDDEN, LOGIN_FAILED);
                }
            } else {
                throw new WebScriptException(HttpServletResponse.SC_FORBIDDEN, LOGIN_FAILED);
            }
        } catch (AuthenticationException e) {
            throw new WebScriptException(HttpServletResponse.SC_FORBIDDEN, LOGIN_FAILED);
        } finally {
            this.authenticationService.clearCurrentSecurityContext();
        }
    }

    public TicketValidator getTicketValidator() {
        return this.ticketValidator;
    }

    public void setTicketValidator(TicketValidator ticketValidator) {
        this.ticketValidator = ticketValidator;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void setCircabcService(CircabcService circabcService) {
        this.circabcService = circabcService;
    }
}
