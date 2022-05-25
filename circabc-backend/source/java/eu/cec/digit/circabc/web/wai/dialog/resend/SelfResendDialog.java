/*******************************************************************************
 * Copyright 2006 European Community
 *
 *  Licensed under the EUPL, Version 1.1 or – as soon they
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


package eu.cec.digit.circabc.web.wai.dialog.resend;

import eu.cec.digit.circabc.service.customisation.mail.MailTemplate;
import eu.cec.digit.circabc.service.customisation.mail.MailWrapper;
import eu.cec.digit.circabc.web.app.CircabcNavigationHandler;
import eu.cec.digit.circabc.web.validator.PasswordValidator;
import eu.cec.digit.circabc.web.validator.UserNameValidator;
import eu.cec.digit.circabc.web.wai.manager.ActionsListWrapper;
import eu.cec.digit.circabc.web.wai.wizard.users.CreateCircabcUserWizard;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.bean.repository.Repository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Bean that backs the self resend password dialog
 *
 * @author filipsl
 */
public class SelfResendDialog extends CreateCircabcUserWizard {

    /**
     * the name of this bean
     */
    public static final String BEAN_NAME = "SelfResendDialog";
    /**
     * The name of the page to display if the user is correctly register
     */
    public static final String CONGRATULATION_DIALOG = "wai:dialog:resendOwnPasswordSuccessWai";
    public static final String RESET_PSW_DIALOG = "wai:dialog:resendOwnPasswordWai";
    public static final String CAPTCHA_FIELD = "Captcha";
    public final static String SESSION_KEY_NAME = "SelfResendDialogCaptchaKeyName";
    private static final long serialVersionUID = -5338520906207919943L;
    private final static Log logger = LogFactory.getLog(SelfResendDialog.class);
    private static final String KEY_NEW_PASSWORD = "newPassword";
    private static final String KEY_FULL_NAME = "fullName";
    private static final String KEY_USER_NAME = "userName";
    /**
     * I18N messages
     */
    private static final String MAIL_ERROR = "resend_mail_error";
    private static final String CAPTCA_VALIDATION_ERROR = "resend_page_captcha_error";
    /**
     * The user captcha response for validation
     */

    private String captchaResponse;

    /**
     * Return the jsp name of the registration page and init the form. Sould be used for each
     * externale call !!!
     */
    @Override
    public void init(Map<String, String> params) {
        final String requireRefreshAll = (params != null) ? params.get("new") : null;

        editMode = true;

        if (requireRefreshAll != null && requireRefreshAll.equalsIgnoreCase("true")) {
            editMode = false;
        }

        super.init(params);
        editMode = false;

        super.getAuthenticationService().authenticateAsGuest();

        this.captchaResponse = "";
    }

    /**
     * @return the captchaResponse
     */
    public String getCaptchaResponse() {
        return captchaResponse;
    }

    /**
     * @param captchaResponse the captchaResponse to set
     */
    public void setCaptchaResponse(String captchaResponse) {
        this.captchaResponse = captchaResponse;
    }

    /**
     * @return the translated error launched by the captcha validation or null if validation
     * successfull
     */
    public String getCaptchaValidationError() {
        return getValidationErrorMessage(CAPTCHA_FIELD);
    }

    /**
     * Refresh the actual page with the regenerating of new captchas
     *
     * @return the actual page outcome
     */
    public String regenerateCaptchas() {
        this.init(Collections.<String, String>emptyMap());
        return CircabcNavigationHandler.CLOSE_WAI_DIALOG_OUTCOME
                + CircabcNavigationHandler.OUTCOME_SEPARATOR
                + RESET_PSW_DIALOG;
    }


    protected String finishImpl(FacesContext context, String outcome) throws Exception {
        if (getNavigator().getCurrentNodeId() == null) {
            getNavigator().setCurrentNodeId(getManagementService().getCircabcNodeRef().getId());
        }

        super.editMode = false;
        super.readOnly = false;

        outcome = CircabcNavigationHandler.CLOSE_WAI_DIALOG_OUTCOME
                + CircabcNavigationHandler.OUTCOME_SEPARATOR
                + RESET_PSW_DIALOG;

        if (validateFields(true, true)) {
            final RetryingTransactionHelper txnHelper = Repository.getRetryingTransactionHelper(context);
            final RetryingTransactionCallback<String> callback = new RetryingTransactionCallback<String>() {
                public String execute() throws Throwable {
                    char[] newPassword = null;
                    String fullName = null;

                    String outcome;

                    try {
                        String completeUserName = getUserName();
                        newPassword = new PasswordValidator().generate();
                        getUserService().setPassword(completeUserName, newPassword);
                        email = getUserService().getUserEmail(completeUserName);
                        fullName = getUserService().getUserFullName(completeUserName);
                    } catch (Exception e) {
                        logger.error(
                                "The user " + getUserName() + " can't resent password due to an error: " + e
                                        .getMessage());

                        addErrorMessage(FacesContext.getCurrentInstance(), e.getMessage());

                        return null;
                    }

                    //notify the user. This proccess can be blocking if the mail server is down.
                    if (notifyUser(email, fullName, getUserName(), newPassword)) {
                        if (logger.isDebugEnabled()) {
                            logger.info("New password was sent to user: " + getUserName());
                        }

                        // all is ok. Redirect the user to the congratulation page.
                        outcome = CircabcNavigationHandler.CLOSE_WAI_DIALOG_OUTCOME
                                + CircabcNavigationHandler.OUTCOME_SEPARATOR
                                + CONGRATULATION_DIALOG;
                    } else {
                        outcome = null;
                        //let admins know email is not getting sent
                        logger.warn("Failed to send the self registration email to " + getEmail());
                        addErrorMessage(FacesContext.getCurrentInstance(), translate(MAIL_ERROR));
                    }
                    return outcome;
                }
            };

            String result = txnHelper.doInTransaction(callback, false);
            if (result != null) {
                outcome = result;
            }

        } else {
            // if the fields are not valid, the validateFileds has already
            // fill the error messages
        }

        this.captchaResponse = "";

        return outcome;

    }

    /**
     * Send an email notification to the specified User authority
     *
     * @param to       user email for sending email
     * @param fullName User full name (first name and last name)
     * @param password New password to send user
     */
    protected boolean notifyUser(final String to, final String fullName, final String userName,
                                 final char[] password) throws Exception {
        if (to != null && to.length() != 0) {
            final NodeRef circabcRoot = getManagementService().getCircabcNodeRef();

            final Map<String, Object> model = getMailPreferencesService()
                    .buildDefaultModel(circabcRoot, null, null);

            model.put(KEY_NEW_PASSWORD, new String(password));
            model.put(KEY_USER_NAME, userName);
            model.put(KEY_FULL_NAME, fullName);

            final Locale currentLocale = I18NUtil.getLocale();
            if (contentFilterLanguage != null) {
                I18NUtil.setLocale(I18NUtil.parseLocale(contentFilterLanguage));
            }
            final MailWrapper mail = getMailPreferencesService()
                    .getDefaultMailTemplate(circabcRoot, MailTemplate.RESEND_OWN_PASSWORD);
            AuthenticationUtil.pushAuthentication();
            I18NUtil.setLocale(currentLocale);
            try {
                AuthenticationUtil.setRunAsUserSystem();
                return getMailService()
                        .send(getMailService().getNoReplyEmailAddress(), to, null, mail.getSubject(model),
                                mail.getBody(model), true, false);

            } finally {
                AuthenticationUtil.popAuthentication();
            }
        } else {
            return false;
        }
    }

    /**
     * Manually launch each validator to have messages correctly. Fill the error map with the field in
     * key and the translated error in value,
     *
     * @return if error was found
     */
    protected boolean validateFields(boolean validateUsername, boolean addFacesErrorMessages) {
        FacesContext context = FacesContext.getCurrentInstance();
        this.validationErrors = new HashMap<>(10);

        if (validateUsername) {
            try {
                final String completeUserName = getUserName();

                UserNameValidator.evaluateUserExists(getPersonService(), completeUserName);
            } catch (Exception ex) {
                String message = translate(MSG_USER_DOES_NOT_EXISTS, getUserName());

                validationErrors.put(USERNAME_FIELD, message);
            }

        }
        try {
            validateCaptcha(context, null, getCaptchaResponse());
        } catch (ValidatorException ex) {
            String msg = ex.getFacesMessage().getDetail();
            validationErrors.put(CAPTCHA_FIELD, msg);

        }

        if (addFacesErrorMessages) {
            for (String message : validationErrors.values()) {
                addErrorMessage(context, message);
            }
        }

        return validationErrors.size() == 0;
    }


    /**
     * Validate password field data is acceptable
     */
    public void validateCaptcha(FacesContext context, UIComponent component, Object value)
            throws ValidatorException {

        Boolean isResponseCorrect = Boolean.FALSE;

        final String captchaGeneratedValue = getCaptchaGeneratedValue();

        //retrieve the response
        final String response = captchaResponse.toUpperCase().trim();

        // Try if the response is correct for the image captcha
        isResponseCorrect = response.equalsIgnoreCase(captchaGeneratedValue);

        // if the response isn't correct, throws a validation exception
        if (isResponseCorrect == false) {
            throw new ValidatorException(new FacesMessage(translate(CAPTCA_VALIDATION_ERROR)));
        }
    }

    private String getCaptchaGeneratedValue() {
        String captchaValue = (String) ((HttpSession) FacesContext
                .getCurrentInstance().getExternalContext().getSession(true))
                .getAttribute(SESSION_KEY_NAME);
        return captchaValue;
    }

    public ActionsListWrapper getActionList() {
        return null;
    }

    public String getBrowserTitle() {
        return translate("resend_page_title");
    }

    public String getPageIconAltText() {
        return translate("resend_page_description");
    }

    public boolean isCancelButtonVisible() {
        return false;
    }

    public String getSessionKeyName() {
        return SESSION_KEY_NAME;
    }

}
