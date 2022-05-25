package eu.cec.digit.circabc.web.servlet;

import eu.cec.digit.circabc.config.CircabcConfiguration;
import eu.cec.digit.circabc.service.CircabcServiceRegistry;
import eu.cec.digit.circabc.service.user.UserService;
import eu.cec.digit.circabc.web.Services;
import org.alfresco.web.app.servlet.BaseServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class EULogoutServlet extends BaseServlet {

    private static String WEB_ROOT_URL = CircabcConfiguration.getProperty(CircabcConfiguration.WEB_ROOT_URL);
    private UserService userService;

    @Override
    public void init() throws ServletException {

        super.init();

        userService = (UserService) Services.getCircabcServiceRegistry(getServletContext())
                .getService(CircabcServiceRegistry.CIRCABC_USER_SERVICE);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String redirectURLFallBack = userService.getRedirectUrlAfterLogout() + "?url=" + WEB_ROOT_URL;
        try {
            String redirectURL = userService.getRedirectUrlAfterLogout() + "?url=" + new URI(WEB_ROOT_URL).toASCIIString();

            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }

            response.sendRedirect(redirectURL);
        } catch (URISyntaxException e) {
            response.sendRedirect(redirectURLFallBack);
        }
    }
}