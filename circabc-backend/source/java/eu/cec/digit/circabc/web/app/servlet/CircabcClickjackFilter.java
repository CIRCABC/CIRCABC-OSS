package eu.cec.digit.circabc.web.app.servlet;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author filipsl Same like ClickjackFilter just change to first add header and then call doFilter
 */
public class CircabcClickjackFilter implements Filter {

    private String mode = "DENY";
    private String from = "";

    /**
     * Initialize "mode" parameter from web.xml. Valid values are "DENY" and "SAMEORIGIN". If you
     * leave this parameter out, the default is to use the DENY mode.
     *
     * @param filterConfig A filter configuration object used by a servlet container to pass
     *                     information to a filter during initialization.
     */
    public void init(FilterConfig filterConfig) {
        String configMode = filterConfig.getInitParameter("mode");
        if (configMode != null && (configMode.equals("DENY") || configMode.equals("SAMEORIGIN"))) {
            mode = configMode;
        } else if (configMode != null && (configMode.equals("ALLOW-FROM"))) {
            mode = configMode;
            from = filterConfig.getInitParameter("from");
        }
    }

    /**
     * Add X-FRAME-OPTIONS response header to tell IE8 (and any other browsers who decide to
     * implement) not to display this content in a frame. For details, please refer to
     * <a href="http://blogs.msdn.com/sdl/archive/2009/02/05/clickjacking-defense-in-ie8.aspx">Clickjacking
     * defense</a>
     *
     * @param request  The request object.
     * @param response The response object.
     * @param chain    Refers to the {@code FilterChain} object to pass control to the next {@code
     *                 Filter}.
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse res = (HttpServletResponse) response;
        res.addHeader("X-Frame-Options", mode + " " + from);
        if (!res.containsHeader("Access-Control-Allow-Origin")) {
            res.addHeader("Access-Control-Allow-Origin", from);
        }
        chain.doFilter(request, response);
    }

    /**
     * {@inheritDoc}
     */
    public void destroy() {
    }
}
