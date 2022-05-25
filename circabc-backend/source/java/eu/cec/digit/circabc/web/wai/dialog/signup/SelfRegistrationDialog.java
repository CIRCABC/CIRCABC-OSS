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


package eu.cec.digit.circabc.web.wai.dialog.signup;

import eu.cec.digit.circabc.config.CircabcConfiguration;
import eu.cec.digit.circabc.service.customisation.mail.MailTemplate;
import eu.cec.digit.circabc.service.customisation.mail.MailWrapper;
import eu.cec.digit.circabc.web.app.CircabcNavigationHandler;
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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

/**
 * Bean that backs the self registration dialog
 *
 * @author Yanick Pignot
 */
public class SelfRegistrationDialog extends CreateCircabcUserWizard {

    /**
     * the name of this bean
     */
    public static final String BEAN_NAME = "SelfRegistrationDialog";
    /**
     * The mandatory url user parameters
     */
    public static final String USERNAME_URL_PARAM = "user";
    /**
     * The mandatory url activation key parameters
     */
    public static final String ACTIVATION_KEY_URL_PARAM = "akey";
    /**
     * The name of the page to display if the user is correctly register
     */
    public static final String CONGRATULATION_DIALOG = "wai:dialog:selfRegisterSuccessWai";
    public static final String REGISTRATION_DIALOG = "wai:dialog:selfRegisterWai";
    public static final String CAPTCHA_FIELD = "Captcha";
    public final static String SESSION_KEY_NAME = "SelfRegistrationCaptchaKeyName";
    /**
     * The url of the activation page
     */
    protected static final String REGISTRATION_PAGE = "/faces/jsp/extension/wai/activation.jsp";
    private static final long serialVersionUID = 626669531664749789L;
    private static final Log logger = LogFactory.getLog(SelfRegistrationDialog.class);
    private static final String KEY_EXPIRATION = "expiration";
    private static final String KEY_ACTIVATION_URL = "activationUrl";
    private static final String MAIL_ERROR = "self_registration_mail_error";
    private static final String CAPTCA_VALIDATION_ERROR = "self_registration_page_captcha_error";
    /**
     * The user captcha response for validation
     */
    private String captchaResponse;

    /**
     * Method called at the end of the registration dialog to create an inactive user after testing
     * the validity of the fields.
     *
     * @return the outcome of the next page to display (according the validity of the fields)
     **/
    protected String finishImpl(FacesContext context, String outcome) throws Exception {
        if (getNavigator().getCurrentNodeId() == null) {
            getNavigator().setCurrentNodeId(getManagementService().getCircabcNodeRef().getId());
        }

        super.editMode = false;
        super.readOnly = false;

        outcome = CircabcNavigationHandler.CLOSE_WAI_DIALOG_OUTCOME
                + CircabcNavigationHandler.OUTCOME_SEPARATOR
                + REGISTRATION_DIALOG;

        if (validateFields(true, true, true, true)) {

            final RetryingTransactionHelper txnHelper = Repository.getRetryingTransactionHelper(context);
            final RetryingTransactionCallback<String> callback = new RetryingTransactionCallback<String>() {
                public String execute() throws Throwable {
                    String outcome;

                    try {
                        // create the user
                        createPerson(FacesContext.getCurrentInstance(), "", false);
                    } catch (Exception e) {
                        if (logger.isErrorEnabled()) {
                            logger.error(
                                    "The user " + getUserName() + " can't register itself due to an error: " + e
                                            .getMessage(), e);
                        }
                        addErrorMessage(FacesContext.getCurrentInstance(), e.getMessage());

                        return null;
                    }

                    //get the activation url
                    String activationURL = generateActivationURL(getUserName(), createdPerson.getId());

                    if (logger.isDebugEnabled()) {
                        logger.debug(
                                "The user " + getUserName() + " has just registred itself. Its activation is "
                                        + activationURL);
                    }

                    // notify the user. This proccess can be blocking if the mail server is down.
                    if (notifyUser(activationURL)) {
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

    private String getCaptchaGeneratedValue() {
        String captchaValue = (String) ((HttpSession) FacesContext
                .getCurrentInstance().getExternalContext().getSession(true))
                .getAttribute(SESSION_KEY_NAME);
        return captchaValue;
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
                + REGISTRATION_DIALOG;
    }

    public String getSoundSource() {
        return FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath()
                + "/captchaSound.wav?bean=" + BEAN_NAME;
    }

    @Override
    protected boolean validateFields(boolean validateUsername, boolean validatePassword,
                                     boolean validateMadatotyProperties, boolean addFacesErrorMessages) {
        super.validateFields(validateUsername, validatePassword, validateMadatotyProperties,
                addFacesErrorMessages);

        FacesContext context = FacesContext.getCurrentInstance();

        try {
            validateCaptcha(context, null, getCaptchaResponse());
        } catch (ValidatorException ex) {
            String msg = ex.getFacesMessage().getDetail();
            super.validationErrors.put(CAPTCHA_FIELD, msg);

            if (addFacesErrorMessages) {
                addErrorMessage(context, msg);
            }
        }

        return super.validationErrors.size() == 0;
    }

    /**
     * @return the translated error launched by the captcha validation or null if validation
     * successfull
     */
    public String getCaptchaValidationError() {
        return getValidationErrorMessage(CAPTCHA_FIELD);
    }

    /**
     * Return the jsp name of the registration page and init the form. Sould be used for each
     * externale call !!!
     */
    public void init(Map<String, String> params) {
        final String requireRefreshAll = (params != null) ? params.get("new") : null;

        editMode = true;

        if (requireRefreshAll != null && requireRefreshAll.equalsIgnoreCase("true")) {
            editMode = false;
        }

        super.init(params);
        editMode = false;

        getAuthenticationService().authenticateAsGuest();

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


    /**
     * Send an email notification to the specified User authority
     *
     * @param URL The activation link to send to the user
     */
    @SuppressWarnings("unchecked")
    protected boolean notifyUser(String URL) throws Exception {
        final String to = getEmail();

        if (to != null && to.length() != 0) {

            final NodeRef circabcRoot = getManagementService().getCircabcNodeRef();

            final Map model = getMailPreferencesService().buildDefaultModel(
                    circabcRoot,
                    createdPerson, null);

            model.put(KEY_ACTIVATION_URL, URL);
            model.put(KEY_EXPIRATION, "" + getUserService().getAccountExpirationDays());

            final Locale currentLocale = I18NUtil.getLocale();
            if (contentFilterLanguage != null) {
                I18NUtil.setLocale(I18NUtil.parseLocale(contentFilterLanguage));
            }

            final MailWrapper mail = getMailPreferencesService()
                    .getDefaultMailTemplate(circabcRoot, MailTemplate.SELF_REGISTRATION);

            I18NUtil.setLocale(currentLocale);

            AuthenticationUtil.pushAuthentication();
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
     * Genereate the complete unique registration link (url and param) to send to the user
     *
     * @param userName      the username of the new registred user
     * @param activationKey the unique activation key
     */
    protected String generateActivationURL(String userName, String activationKey)
            throws UnsupportedEncodingException {

        final StringBuilder path = new StringBuilder();

        final String urlRoot = CircabcConfiguration.getProperty(CircabcConfiguration.WEB_ROOT_URL);

        path
                .append(urlRoot)
                .append(REGISTRATION_PAGE)
                .append('?')
                .append(USERNAME_URL_PARAM)
                .append('=')
                .append(URLEncoder.encode(userName, "UTF-8"))
                .append('&')
                .append(ACTIVATION_KEY_URL_PARAM)
                .append('=')
                .append(URLEncoder.encode(activationKey, "UTF-8"));

        return path.toString();
    }

    /**
     * @return the number of days in which the account will expire
     */
    public int getExpirationTime() {
        return getUserService().getAccountExpirationDays();
    }

    public ActionsListWrapper getActionList() {
        return null;
    }

    public String getBrowserTitle() {
        return translate("self_registration_page_title");
    }

    public String getPageIconAltText() {
        return translate("self_sign_up");
    }

    public boolean isCancelButtonVisible() {
        return false;
    }

    public String getSessionKeyName() {
        return SESSION_KEY_NAME;
    }
}
