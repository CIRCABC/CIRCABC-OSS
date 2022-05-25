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

import eu.cec.digit.circabc.web.bean.surveys.SurveysBean;
import org.alfresco.web.app.servlet.BaseServlet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * When the user submit an IPM survey, the request is caught by this filter to get all parameters
 * which are the answers to the survey. These parameters are set in the SurveysBean backed bean.
 *
 * @author Matthieu Sprunck
 * @author Guillaume
 */
public class EncodeSurveyFilter implements Filter {

    /**
     * The logger class*
     */
    private static final Log logger = LogFactory
            .getLog(EncodeSurveyFilter.class);

    /**
     * The SurveysBean back bean name
     */
    private static final String SURVEYS_BEAN = "SurveysBean";

    /**
     * Path to survey jsp file
     */
    private static final String ENCODE_PAGE = "/jsp/extension/surveys/survey.jsp";

    /**
     * Path to survey jsp wai file
     */
    private static final String ENCODE_PAGE_WAI = "/jsp/extension/wai/navigation/surveys/survey.jsp";


    /**
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse,
     * javax.servlet.FilterChain)
     */
    @SuppressWarnings("unchecked")
    public void doFilter(ServletRequest req, ServletResponse res,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpReq = (HttpServletRequest) req;
        HttpServletResponse httpRes = (HttpServletResponse) res;

        if (logger.isDebugEnabled()) {
            logger.debug("filtering " + httpReq.getRequestURI());
        }

        SurveysBean surveysBean = (SurveysBean) httpReq.getSession().getAttribute(SURVEYS_BEAN);

        surveysBean.setParameters(httpReq.getParameterMap());

        // redirect to the encode page
        if (surveysBean.isInWAI()) {
            // TODO CircabcBrowseBean.clickSurvey
            httpRes.sendRedirect(httpReq.getContextPath() + BaseServlet.FACES_SERVLET + ENCODE_PAGE_WAI);
        } else {
            httpRes.sendRedirect(httpReq.getContextPath() + BaseServlet.FACES_SERVLET + ENCODE_PAGE);
        }
    }

    /**
     * @see javax.servlet.Filter#destroy()
     */
    public void destroy() {
        // nothing to do
    }

    /**
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    public void init(FilterConfig config) throws ServletException {
        // nothing to do
    }
}
