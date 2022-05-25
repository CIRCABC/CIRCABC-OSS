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
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

public class CircabcSessionSynchronizedFilter implements Filter {

    public static final String SESSION_LOCK_ATTRIBUTE = "CIRCABC_LOCK";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        HttpSession session = null;
        if (request instanceof HttpServletRequest) {
            session = ((HttpServletRequest) request).getSession(false);

        }
        if (session != null) {
            final ReentrantLock lock = (ReentrantLock) session
                    .getAttribute(CircabcSessionSynchronizedFilter.SESSION_LOCK_ATTRIBUTE);
            AtomicBoolean isLocked = new AtomicBoolean(false);
            try {
                if (lock.tryLock(5, TimeUnit.SECONDS)) {
                    isLocked.set(true);
                    chain.doFilter(request, response);
                } else {
                    final PrintWriter writer = response.getWriter();

                    writer.append(
                            "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
                    writer.append("<html><head>");
                    writer.append("<meta http-equiv=\"Content-Type\" content=\"text/html;charset=UTF-8\" />");
                    writer.append("<meta name=\"Reference\" content=\"CIRCABC\" />");
                    writer.append("<meta name=\"Title\" content=\"CIRCABC\" />");
                    writer.append("<meta name=\"Creator\" content=\"European Commission\" />");
                    writer.append(
                            "<meta name=\"Type\" content=\"Numeric code given in the list of document types\" />");
                    writer.append(
                            "<meta name=\"Classification\" content=\"Numeric code from the alphabetical classification list common to all the institutions\" />");
                    writer.append(
                            "<meta name=\"Keywords\" content=\"European Commission, CIRCABC, documents, files\" />");
                    writer.append(
                            "<meta name=\"Description\" content=\"CIRCABC is a collaborative platform, which offers an easy distribution and management of documents.\" />");
                    writer.append("<title>CIRCABC</title>");
                    writer.append(
                            "<link rel=\"stylesheet\" href=\"/css/extension/circabc.css\" type=\"text/css\" />");
                    writer.append(
                            "<link rel=\"stylesheet\" href=\"/css/extension/commission2012.css\" type=\"text/css\" />");
                    writer.append("<link rel=\"icon\" type=\"image/gif\" href=\"/images/favicon.gif\"/>");
                    writer.append(
                            "<link rel=\"shortcut icon\" type=\"image/x-icon\" href=\"/images/favicon.ico\" />");
                    writer.append(
                            "<script src=\"/scripts/extension/language.js\" type=\"text/javascript\"></script>");
                    writer.append("</head>");
                    writer.append("<body><a name=\"top\"></a>");
                    writer.append(
                            "<div id=\"bannerBackground\"><div id=\"bannerTop\"><div id=\"langsSelector\"></div>");
                    writer.append("<div id=\"defaultBannerLogoDisplayer\">");
                    writer.append(
                            "<img id=\"logo\" src=\"/images/extension/banner/circabc-title-banner-en.png\" alt=\"European Commission CIRCABC logo\"/>");
                    writer.append(
                            "</div><div class=\"bannerRight\"><div id=\"linkBox\"><div id=\"linkBoxTools\">");
                    writer.append(
                            "<ul><li class=\"first\"><a accesskey=\"4\" href=\"http://ec.europa.eu/geninfo/query/search_en.html\" target=\"_blank\"><h:outputText id=\"searchtext\" value=\"#{cmsg.igroot_search}\"/></a></li>		");
                    writer.append(
                            "<li><a id=\"customContactLink\" href=\"mailto:EC-CENTRAL-HELPDESK@ec.europa.eu\">Contact</a></li>	");
                    writer.append(
                            "<li><a accesskey=\"8\" id=\"legalNotice\" href=\"http://ec.europa.eu/geninfo/legal_notices_en.htm\"  target=\"_blank\">Legal notice</a></li>");
                    writer.append("</ul></div>");
                    writer.append("</div></div>	");
                    writer.append("</div><div id=\"bannerBottom\"></div>");
                    writer.append("</div><div id=\"path\"><div><ul>");
                    writer.append(
                            "<li class=\"first-child\"><a id=\"firstTab\" href=\"http://europa.eu/index_en.htm\">Europa</a>&nbsp;&gt;&nbsp;</li>");
                    writer.append(
                            "<li><a href=\"http://ec.europa.eu/index_en.htm\">European Commission</a>&nbsp;&gt;&nbsp;</li>");
                    writer.append("<li>CIRCABC</li>");
                    writer.append("</ul></div>");
                    writer.append("</div><div id=\"maincontentGlobal\" class=\"errorMainContent\">");
                    writer.append(
                            "<div id=\"contentHeaderGlobal\" class=\"contentHeaderError\"><span class=\"contentHeaderSubTitle\"><br></span>");
                    writer.append(
                            "<span class=\"contentHeaderTitle\">Sorry, CIRCABC could not process your request. </span>");
                    writer.append("<span class=\"contentHeaderSubTitle\"><br><br></span>");
                    writer.append("</div><div id=\"contentMainErrorPageGlobal\" class=\"contentMain\">");
                    writer.append(
                            "<br><div style=\"float:left; width: 25%; height: 200px; text-align: right; padding-right: 35px;\">");
                    writer.append(
                            "<img src=\"/images/extension/icons/vintage-hourglass.png\" style=\"top: 50%; position: relative; margin: -64px auto 0;\"/>");
                    writer.append(
                            "</div><div style=\"float:left; width: 70%; height: 200px; text-align: left; padding-left: 35px;\">");
                    writer.append("Dear User,<br><br>");
                    writer.append("Your previous request has not been processed.<br>");
                    writer.append("Please wait and retry in a few moments. <br><br>");
                    writer.append(
                            "If the problem persists, please contact the Helpdesk: <a href=\"mailto:EC-CENTRAL-HELPDESK@ec.europa.eu\">EC-CENTRAL-HELPDESK@ec.europa.eu</a>");
                    writer.append("<br><br>Thank you for your cooperation.");
                    writer.append("<br>The CIRCABC Team");
                    writer.append("</div><br><br></div></div></body></html>");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                // lock.isHeldByCurrentThread()
                if (isLocked.get()) {
                    lock.unlock();
                }
            }
        } else {
            chain.doFilter(request, response);
        }
    }

}
