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
package eu.cec.digit.circabc.web.filter;

import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Handle redirection to the correct url for the current context.
 * In order to decommission old user interface, this filter is used to redirect
 * to the new url.
 */
public class RedirectionFilter implements Filter {

  private static final Log logger = LogFactory.getLog(RedirectionFilter.class);

  private boolean enabled = true;

  @Override
  public void destroy() {
    if (logger.isInfoEnabled()) {
      logger.info("destroy");
    }
  }

  @Override
  public void doFilter(
    ServletRequest req,
    ServletResponse res,
    FilterChain chain
  ) throws IOException, ServletException {
    HttpServletRequest httpReq = (HttpServletRequest) req;
    HttpServletResponse httpRes = (HttpServletResponse) res;
    String requestURI = httpReq.getRequestURI();

    if (logger.isInfoEnabled()) {
      logger.info("filtering " + requestURI);
    }

    if (!this.enabled) {
      chain.doFilter(req, res);
      return;
    }

    if (
      requestURI.startsWith("/faces/jsp/extension/session_expired.jsp") ||
      requestURI.startsWith("/faces/jsp/extension/wai/ecas/ecaslogin.jsp") ||
      requestURI.startsWith("/faces/jsp/extension/wai/login.jsp") ||
      requestURI.startsWith("/faces/jsp/extension/wai/error/error-wai.jsp")
    ) {
      chain.doFilter(req, res);
    } else if (requestURI.startsWith("/faces/jsp/extension/")) {
      httpRes.sendRedirect(httpReq.getContextPath() + "/ui/index.html");
    } else if (requestURI.startsWith("/w/browse/")) {
      httpRes.sendRedirect(
        httpReq.getContextPath() +
        "/ui/w/browse/" +
        requestURI.replaceFirst("/w/browse/", "")
      );
    } else {
      chain.doFilter(req, res);
    }
  }

  @Override
  public void init(FilterConfig config) throws ServletException {
    if (logger.isInfoEnabled()) {
      logger.info("init");
    }
    String enabledParameter = config.getInitParameter("enabled");
    if (enabledParameter != null) {
      if (enabledParameter.equals("false")) {
        this.enabled = false;
      }
    }
  }
}
