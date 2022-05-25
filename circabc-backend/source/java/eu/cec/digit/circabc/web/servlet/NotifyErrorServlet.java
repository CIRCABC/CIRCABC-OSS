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

import eu.cec.digit.circabc.repo.translation.TranslationDaoService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author filipsl Callback Servlets used by machine translation service to notify CIRCABC about
 * error during translation translation
 */
public class NotifyErrorServlet extends HttpServlet {

    /**
     *
     */
    private static final long serialVersionUID = 6426846930671976111L;
    private static Log logger = LogFactory.getLog(NotifyErrorServlet.class);

    @Autowired
    private TranslationDaoService translationDaoService;

    public void init(ServletConfig config) {
        try {
            super.init(config);
        } catch (ServletException e) {
            if (logger.isErrorEnabled()) {
                logger.error("can not init servlet NotifySuccessServlet", e);
            }
        }
        SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this,
                config.getServletContext());
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest
     * , javax.servlet.http.HttpServletResponse) called in case an error occured
     * during the translation process. This URL can be a HTTP address. In this
     * case, this URL is called with the HTTP POST method. The following
     * parameters will be included in the POST request: errorCode: numeric code
     * of the error. errorMessage: the error description. targetLanguage: target
     * language code of the failed translation process.
     */
    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String externalReference = request.getParameter("external-reference");
        String requestId = request.getParameter("requestId");
        if (requestId == null) {
            requestId = request.getParameter("request-id");
        }
        String targetLanguage = request.getParameter("target-languages");
        String errorCode = request.getParameter("error-code");
        String errorMessage = request.getParameter("error-message");

        if (logger.isInfoEnabled()) {
            logger.info("***** ERROR RECEIVED *****");
            logger.info("Request Id : " + requestId);
            logger.info("Target Language : " + targetLanguage);
            logger.info("Error Code : " + errorCode);
            logger.info("Error Message : " + errorMessage);
            logger.info("External Reference" + externalReference);
        }

        try {
            translationDaoService.saveErrorResponse(requestId, targetLanguage, errorCode, errorMessage);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("can not save record to database", e);
            }
        }
    }
}
