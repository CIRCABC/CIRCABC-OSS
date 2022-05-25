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
package eu.cec.digit.circabc.web.servlet;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Slobodan Filipovic
 * <p>
 * Filter to solve problem when user click to CIRCABC link from MS Word or MS Excel wrong page is
 * shown
 * <p>
 * http://support.microsoft.com/kb/899927
 * <p>
 * "Implement for an HTTP request that may be a multiple-session client request, issue a client-side
 * redirect response instead of a server-side redirect response. For example, send an HTTP script or
 * a META REFRESH tag instead of an HTTP 302 response. This change forces the client back into the
 * default Web browser of the user. Therefore, the default browser session can handle the call and
 * can keep the call in a single, read-only session."
 */
public class OfficeLinkFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequset = (HttpServletRequest) req;
        HttpServletResponse httpResponse = (HttpServletResponse) resp;

        String userAgent = httpRequset.getHeader("User-Agent");
        if (userAgent != null
                && (userAgent.contains("Word") || userAgent.contains("Excel")
                || userAgent.contains("PowerPoint") || userAgent
                .contains("ms-office"))
                && !userAgent.contains("Microsoft Outlook")) {
            httpResponse.setContentType("text/html");
            httpResponse
                    .getOutputStream()
                    .println(
                            "<html><head><meta http-equiv='refresh' content='0'/></head><body></body></html>");
            httpResponse.setStatus(HttpServletResponse.SC_OK);
        } else {
            chain.doFilter(req, resp);
        }
    }

    @Override
    public void init(FilterConfig conf) throws ServletException {
        // TODO Auto-generated method stub

    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub

    }

}
