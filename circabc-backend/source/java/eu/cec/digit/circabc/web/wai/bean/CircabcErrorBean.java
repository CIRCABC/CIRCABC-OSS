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
package eu.cec.digit.circabc.web.wai.bean;

import eu.cec.digit.circabc.error.CircabcRuntimeException;
import eu.cec.digit.circabc.service.customisation.ApplicationCustomisationService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.web.bean.ErrorBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.event.ActionEvent;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


/**
 * Bean extends the not management error management.
 *
 * @author Yanick Pignot.
 */
public class CircabcErrorBean extends ErrorBean {

    public static final String BEAN_NAME = "ErrorBean";
    public static final String LAST_ERROR_TIMESTAMP = "LastErrorTimeStamp";
    public static final long MIN_MILLISEC_BETWEEN_TWO_ERRORS = 15000;
    public static final String MAIN_PAGE = "/jsp/extension/wai/error/error-wai.jsp";
    public static final String ERROR_REDIRECT_LOGOUT_PAGE = "/jsp/extension/wai/error/error-redirect-logout-wai.jsp";
    private static final long serialVersionUID = 68736574648784143L;
    private static final Log logger = LogFactory.getLog(CircabcErrorBean.class);
    private String errorMessageContent;
    private ApplicationCustomisationService applicationCustomisationService;

    private boolean expanded;

    public void initError(ServletRequest request) {

        Throwable lastError = (Throwable) request.getAttribute("javax.servlet.error.exception");
        if (lastError == null) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpSession session = httpRequest.getSession();
            ErrorBean errorBean = (ErrorBean) session.getAttribute(ErrorBean.ERROR_BEAN_NAME);
            lastError = errorBean.getLastError();
        }
        setLastError(lastError);

        final String returnPage = (String) request.getAttribute("javax.servlet.error.request_uri");
        setReturnPage(returnPage);

        logger.error("Unexpected and not managed error for user " + AuthenticationUtil
                .getFullyAuthenticatedUser(), getLastError());

        expanded = false;
    }

    public String getExceptionClass() {
        if (getLastError() == null) {
            return "Internal Error";
        } else {
            return getLastError().getClass().getName();
        }
    }

    /**
     * @return the expanded
     */
    public boolean isExpanded() {
        return expanded;
    }

    /**
     * @param expanded the expanded to set
     */
    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }


    /**
     * @param event to perform to see the details
     */
    public void expandDetails(ActionEvent event) {
        this.expanded = true;
    }

    /**
     * @param event to perform to hide the details
     */
    public void hideDetails(ActionEvent event) {
        this.expanded = false;
    }

    public boolean isCircabcRuntimeException() {
        return getLastError() instanceof CircabcRuntimeException;
    }


    public String getLastCircabcErrorMessage() {
        if (getLastError() instanceof CircabcRuntimeException) {
            return getLastError().getMessage();
        } else {
            return "";
        }
    }

    /**
     * @return the errorMessageContent
     */
    public String getErrorMessageContent() {

        if (applicationCustomisationService != null) {
            errorMessageContent = applicationCustomisationService.getErrorMessageContent();
        } else {
            errorMessageContent = null;
        }
        return errorMessageContent;
    }

    /**
     * @param errorMessageContent the errorMessageContent to set
     */
    public void setErrorMessageContent(String errorMessageContent) {
        this.errorMessageContent = errorMessageContent;
    }

    /**
     * @return the applicationCustomisationService
     */
    public ApplicationCustomisationService getApplicationCustomisationService() {
        return applicationCustomisationService;
    }

    /**
     * @param applicationCustomisationService the applicationCustomisationService to set
     */
    public void setApplicationCustomisationService(
            ApplicationCustomisationService applicationCustomisationService) {
        this.applicationCustomisationService = applicationCustomisationService;
    }

}
