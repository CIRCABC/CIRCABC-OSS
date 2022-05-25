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
package eu.cec.digit.circabc.web.wai.wizard.users;

import eu.cec.digit.circabc.model.UserModel;
import eu.cec.digit.circabc.service.user.UserService;
import eu.cec.digit.circabc.util.CircabcUserDataBean;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.validator.*;
import eu.cec.digit.circabc.web.wai.wizard.BaseWaiWizard;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.context.UIContextService;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.users.UserPreferencesBean;
import org.alfresco.web.bean.users.UsersDialog;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIActionLink;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * @author Ph Dubois
 */

public class CreateCircabcUserWizard extends BaseWaiWizard {

    public static final String USERNAME_FIELD = "UserName";
    public static final String OLD_PASSWORD_FIELD = "OldPassword";
    public static final String PASSWORD_FIELD = "Password";
    public static final String CONFIRM_FIELD = "Confirm";
    public static final String FIRSTNAME_FIELD = "FirstName";
    public static final String LASTNAME_FIELD = "LastName";
    public static final String EMAIL_FIELD = "Email";
    public static final String PHONE_FIELD = "PhoneNumber";
    public static final String POSTAL_FIELD = "PostalAddress";
    protected static final String ERROR = "error_person";
    protected static final String MSG_ERR_USERNAME_SIZE = "err_valid_username_size";
    protected static final String MSG_ERR_USERNAME_DIGIT = "err_valid_username_digit";
    protected static final String MSG_ERR_USERNAME_DUPLICATE = "err_duplicate_username";
    protected static final String MSG_ERR_FIRSTNAME_VALID = "err_valid_firstname";
    protected static final String MSG_ERR_LASTNAME_VALID = "err_valid_lastname";
    protected static final String MSG_ERR_POSTAL_VALID = "err_valid_postaladr";
    protected static final String MSG_ERR_PHONE_VALID = "err_valid_telnumber";
    protected static final String MSG_ERR_EMAIL_VALID = "err_valid_email";
    protected static final String MSG_ERR_EMAIL_DUPLICATE = "err_valid_duplicate_email";
    protected static final String MSG_WRONG_OLD_PASSWORD = "change_own_password_error_old_password_incorrect";
    protected static final String MSG_USER_DOES_NOT_EXISTS = "resend_page_user_does_not_exists_error";
    protected static final String MSG_VIEW_APPLICANT_USER_DETAILS_PAGE_DESC = "view_applicant_details_page_description";
    private static final long serialVersionUID = -7514679421319409121L;
    private static final Log logger = LogFactory.getLog(CreateCircabcUserWizard.class);
    protected Map<String, String> validationErrors = null;

    /**
     * if true, the information page displaying the user will be open in Read only Mode
     */
    protected boolean readOnly = false;

    /**
     * form variables
     */
    protected String firstName = null;
    protected String lastName = null;
    protected String userName = null;
    protected String oldPassword;
    protected String password = null;
    protected String confirm = null;

    /* previous email is used to check email
     * has changed when editing (edit mode)
     * */
    protected String previousEmail = null;

    protected String email = null;
    protected String companyId = null;

    /* complementary fields for Circabc user */
    protected String phone = "";
    protected String fax = "";
    protected String url = "";
    protected String postalAddress = "";
    protected String description = "";
    protected String orgDepNumber = "";
    protected String title = "";
    protected String domain = "";

    /**
     * action context
     */
    protected Node person = null;

    /**
     * the new created person (null if edit mode)
     */
    protected NodeRef createdPerson = null;

    /**
     * If it is in editMode
     */
    protected boolean editMode;

    //protected Map<String, Object> populatedProps = null;


    /**
     * Content language locale selection
     */
    protected String contentFilterLanguage = null;
    /**
     * Content language locale selection
     */
    protected String userInterfaceLanguage = null;

    //depedencies

    /**
     * The unprotected node service
     */
    private transient NodeService internalNodeService;
    /**
     * AuthenticationService bean reference
     */
    private transient MutableAuthenticationService authenticationService;
    /**
     * PersonService bean reference
     */
    private transient PersonService personService;


    @Override
    public void init(Map<String, String> params) {
        super.init(params);

        final String editParam = (params == null) ? null : params.get("editMode");

        if (editParam != null
                && Boolean.valueOf(editParam)) {
            if (getActionNode() == null) {
                // else edit current user
                NodeRef ref = getPersonService().getPerson(getAuthenticationService().getCurrentUserName());

                Node node = new Node(ref);

                // remember the Person node
                setPerson(node);
            } else {
                // the user is passed as parameter
                setPerson(getActionNode());
            }

            // populate the wizard's default values with the current value
            // from the node being edited
            reset();
            // set the wizard in edit mode
            this.editMode = true;
            populate();

            // clear the UI state in preparation for finishing the action
            // and returning to the main page
            invalidateUserList();
        } else if (!this.editMode) {
            reset();
        }
    }

    @Override
    public String next() {
        String setpName = Application.getWizardManager().getCurrentStepName();

        boolean errorFound = false;

        // validation launched fo
        if (setpName.equals("mod-user-step2") || setpName.equals("new-user-step2")) {
            if (!validateFields(false, false, true, false)) {
                errorFound = true;
            }
        }
        // only for create user
        else if (setpName.equals("mod-user-summary") || setpName.equals("new-user-summary")) {
            if (!validateFields(!editMode, true, false, false)) {
                errorFound = true;
            }
        }

        // if the fields are not valid, stay in the current step
        if (errorFound) {
            // get the current step
            int step = Application.getWizardManager().getState().getCurrentStep();
            //stay in the current step
            Application.getWizardManager().getState().setCurrentStep(step - 1);

            for (String msg : validationErrors.values()) {
                Utils.addErrorMessage(msg);
            }
        }

        return super.next();
    }


    /**
     * @return current content filter language, or <tt>null</tt> if all languages was selected
     */
    public String getContentFilterLanguage() {
        return this.contentFilterLanguage;
    }

    public void setContentFilterLanguage(String contentFilterLanguage) {
        this.contentFilterLanguage = contentFilterLanguage;
    }

    /**
     * @return list of items for the content filtering language selection
     */
    public SelectItem[] getContentFilterLanguages() {
        return getUserPreferencesBean().getContentFilterLanguages(true);
    }

    /**
     * @return list of items for the user interface language selection
     */
    public SelectItem[] getLanguages() {
        return getUserPreferencesBean().getLanguages();
    }

    /**
     * @return the userInterfaceLanguage
     */
    public String getUserInterfaceLanguage() {
        return this.userInterfaceLanguage;
    }


    /**
     * @param userInterfaceLanguage the userInterfaceLanguage to set
     */
    public void setUserInterfaceLanguage(String interfaceLanguage) {
        this.userInterfaceLanguage = interfaceLanguage;
    }

    /**
     * Initialises the wizard
     */
    public void reset() {
        if (!readOnly) {
            // reset all variables
            this.firstName = "";
            this.lastName = "";
            this.userName = "";
            this.password = "";
            this.confirm = "";
            this.previousEmail = "";
            this.email = "";
            this.companyId = "";

            this.editMode = false;

            this.phone = "";
            this.fax = "";
            this.url = "";
            this.postalAddress = "";
            this.description = "";
            this.orgDepNumber = "";
            this.title = "";
            domain = "";

            contentFilterLanguage = UserPreferencesBean.MSG_CONTENTALLLANGUAGES;
            userInterfaceLanguage = getUserPreferencesBean().getLanguage();

            this.createdPerson = null;

            validationErrors = null;
        }
    }

    /**
     * @see org.alfresco.web.bean.wizard.AbstractWizardBean#populate()
     */
    public void populate() {
        // set values for edit mode
        Map<String, Object> props = getPerson().getProperties();

        this.firstName = (String) props.get(ContentModel.PROP_FIRSTNAME.toString());
        this.lastName = (String) props.get(ContentModel.PROP_LASTNAME.toString());
        this.userName = (String) props.get(ContentModel.PROP_USERNAME.toString());
        this.password = "";
        this.confirm = "";
        this.oldPassword = "";
        this.email = (String) props.get(ContentModel.PROP_EMAIL.toString());
        this.previousEmail = this.email;
        this.companyId = (String) props.get(ContentModel.PROP_ORGID.toString());

        // additionnal
        this.phone = (String) props.get(UserModel.PROP_PHONE.toString());
        this.fax = (String) props.get(UserModel.PROP_FAX.toString());
        this.url = (String) props.get(UserModel.PROP_URL.toString());
        this.postalAddress = (String) props.get(UserModel.PROP_POSTAL_ADDRESS.toString());
        this.description = (String) props.get(UserModel.PROP_DESCRIPTION.toString());
        this.orgDepNumber = (String) props.get(UserModel.PROP_ORGDEPNUMBER.toString());
        this.title = (String) props.get(UserModel.PROP_TITLE.toString());
        this.domain = (String) props.get(UserModel.PROP_DOMAIN.toString());

        this.contentFilterLanguage = getUserPreferencesBean().getContentFilterLanguage();
        this.userInterfaceLanguage = getUserPreferencesBean().getLanguage();

        this.readOnly = false;

        // set as null if edition mode
        this.createdPerson = null;

    }

    /**
     * @see org.alfresco.web.bean.wizard.AbstractWizardBean#finish()
     */
    protected String finishImpl(FacesContext context, String outcome) throws Exception {
        String newOutcome = outcome;
        // if the page is set as read only, they are nothig to do
        if (readOnly) {
            readOnly = false;

            return newOutcome;
        }

        try {
            if (this.editMode) {
                // edition mode
                newOutcome = editPerson(outcome);

                // check if changing password
                if (this.oldPassword != null && this.oldPassword.length() != 0) {
                    if (confirm != null && confirm.length() != 0 && password.equals(confirm)) {
                        try {
                            getAuthenticationService().updateAuthentication(
                                    (String) person.getProperties().get(ContentModel.PROP_USERNAME.toString()),
                                    oldPassword.toCharArray(),
                                    password.toCharArray());

                        } catch (final Exception e) {
                            newOutcome = null;
                            Utils.addErrorMessage(translate(MSG_WRONG_OLD_PASSWORD));
                        }
                    } else {
                        newOutcome = null;
                        Utils.addErrorMessage(translate(UsersDialog.ERROR_PASSWORD_MATCH));
                    }

                }
            } else {   //creation mode
                newOutcome = createPerson(context, outcome, true);
            }

            this.reset();
            // reset the richlist component so it rebinds to the users list
            invalidateUserList();
        } catch (final RuntimeException e) {
            Utils.addErrorMessage(translate(ERROR, e.getMessage()), e);
            newOutcome = null;
        }

        return newOutcome;
    }

    protected String editPerson(final String outcome) throws Exception {
        final RetryingTransactionHelper txnHelper = Repository
                .getRetryingTransactionHelper(FacesContext.getCurrentInstance());
        final RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>() {
            public Object execute() throws Throwable {
                if (previousEmail != null && !previousEmail.equals(email)) {
                    //check that email address is unique
                    EmailValidator.evaluateUnicity(getUserService(), email);
                }

                // update the existing node in the repository
                getUserService().updateUser(createUserDataBean(true));

                final NodeRef person = getPersonService().getPerson(userName);

                //save the preference
                Locale language = null;
                if (contentFilterLanguage.equals(UserPreferencesBean.MSG_CONTENTALLLANGUAGES)) {
                    // The generic "All Languages" was selected - persist this as a null
                    language = null;
                } else {
                    // It should be a proper locale string
                    language = I18NUtil.parseLocale(contentFilterLanguage);
                }

                getUserService().setPreference(person, UserService.PREF_CONTENT_FILTER_LANGUAGE, language);
                getUserService()
                        .setPreference(person, UserService.PREF_INTERFACE_LANGUAGE, userInterfaceLanguage);

                return null;
            }
        };
        txnHelper.doInTransaction(callback);
        return outcome;
    }

    protected String createPerson(final FacesContext context, final String outcome,
                                  final boolean enabled) throws Exception {
        String newOutcome = outcome;
        //	check if email is unique
        if (this.password.equals(this.confirm)) {

            final RetryingTransactionHelper txnHelper = Repository
                    .getRetryingTransactionHelper(FacesContext.getCurrentInstance());
            final RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>() {
                public Object execute() throws Throwable {
                    if (!getPersonService().getUserNamesAreCaseSensitive()) {
                        userName = userName.toLowerCase();
                    }

                    EmailValidator.evaluateUnicity(getUserService(), email);

                    //createUserDataBean(false)with update = false
                    final NodeRef newPerson = getUserService().createUser(createUserDataBean(false), enabled);
                    // remeber the new created user
                    createdPerson = newPerson;

                    if (logger.isDebugEnabled()) {
                        logger.debug("Created User Authentication instance for username: " + userName);
                    }
                    Locale language = null;
                    if (contentFilterLanguage.equals(UserPreferencesBean.MSG_CONTENTALLLANGUAGES)) {
                        // The generic "All Languages" was selected - persist this as a null
                        language = null;
                    } else {
                        // It should be a proper locale string
                        language = I18NUtil.parseLocale(contentFilterLanguage);
                    }

                    getUserService()
                            .setPreference(newPerson, UserService.PREF_CONTENT_FILTER_LANGUAGE, language);
                    getUserService()
                            .setPreference(newPerson, UserService.PREF_INTERFACE_LANGUAGE, userInterfaceLanguage);

                    return null;
                }
            };
            txnHelper.doInTransaction(callback);
        } else {
            newOutcome = null;
            throw new Exception(translate(UsersDialog.ERROR_PASSWORD_MATCH));
        }

        return newOutcome;
    }


    /* (non-Javadoc)
     * @see org.alfresco.web.bean.dialog.BaseDialogBean#getContainerDescription()
     */
    @Override
    public String getContainerDescription() {
        // if the dialog is set as read only, get the corresponding description
        if (this.readOnly) {
            final ResourceBundle bundle = Application.getBundle(FacesContext.getCurrentInstance());

            return bundle.getString(MSG_VIEW_APPLICANT_USER_DETAILS_PAGE_DESC);
        }
        // else return the default description
        else {
            return super.getContainerDescription();
        }
    }


    /**
     * Save a preference in the preference node
     *
     * @param name name of the preference
     */
    public void setPrefValue(final NodeRef preferenceRef, final String name,
                             final Serializable value) {
        final QName qname = QName.createQName(NamespaceService.APP_MODEL_1_0_URI, name);

        final RetryingTransactionHelper txnHelper = Repository
                .getRetryingTransactionHelper(FacesContext.getCurrentInstance());
        final RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>() {
            public Object execute() throws Throwable {
                getInternalNodeService().setProperty(preferenceRef, qname, value);
                return null;
            }
        };
        txnHelper.doInTransaction(callback);
    }

    /**
     * Creates the user data bean from the wizard informations
     *
     * @param update true if it's an update
     * @return the created bean
     */
    protected CircabcUserDataBean createUserDataBean(final boolean update) {
        final CircabcUserDataBean newUserData = new CircabcUserDataBean();
        newUserData.setFirstName(this.firstName);
        newUserData.setLastName(this.lastName);

        if (update) {
            newUserData.setUserName(this.userName);
            newUserData.setDomain(this.domain);
        } else {
            newUserData.setUserName(this.userName);
            //newUserData.setDomain(UserModel.DOMAIN_CIRCA);
        }

        newUserData.setEmail(this.email);
        newUserData.setCompanyId(this.companyId);

        //TODO Remove the ownership for the new user on CircaBC

        final NodeRef guestHome = getManagementService().getGuestHomeNodeRef();

        if (guestHome != null) {
            newUserData.setHomeSpaceNodeRef(guestHome);
        }

        /* complementary fields for Circabc user */
        newUserData.setPhone(this.phone);
        newUserData.setFax(this.fax);
        newUserData.setURL(this.url);
        newUserData.setPostalAddress(this.postalAddress);
        newUserData.setDescription(this.description);
        newUserData.setOrgdepnumber(this.orgDepNumber);
        newUserData.setTitle(this.title);

        newUserData.setPassword(this.password);
        return newUserData;
    }

    /**
     * @return Returns the summary data for the wizard.
     */
    public String getSummary() {
        final FacesContext context = FacesContext.getCurrentInstance();
        final RetryingTransactionHelper txnHelper = Repository.getRetryingTransactionHelper(context);
        final RetryingTransactionCallback<String> callback = new RetryingTransactionCallback<String>() {
            public String execute() throws Throwable {
                final NodeRef homeRef = getProfileManagerServiceFactory()
                        .getProfileManagerService(getActionNode().getNodeRef())
                        .getCircaHome(getActionNode().getNodeRef());
                final String homeSpaceLabel = (String) getNodeService()
                        .getProperty(homeRef, ContentModel.PROP_NAME);

                final ResourceBundle bundle = Application.getBundle(FacesContext.getCurrentInstance());

                final String summary = buildSummary(
                        new String[]{
                                bundle.getString("name"),
                                bundle.getString("username"),
                                bundle.getString("password"),
                                bundle.getString("homespace")},
                        new String[]{
                                firstName + " " + lastName,
                                userName,
                                "********",
                                homeSpaceLabel});
                return summary;
            }
        };

        String summary = null;

        try {
            summary = txnHelper.doInTransaction(callback, true);
        } catch (final RuntimeException e) {
            Utils.addErrorMessage(translate(ERROR, e.getMessage()), e);
        }

        return summary;
    }

    /**
     * Init the users screen
     */
    public void setupUsers(final ActionEvent event) {
        invalidateUserList();
    }

    /**
     * @return Returns the companyId.
     */
    public String getCompanyId() {
        return this.companyId;
    }

    /**
     * @param companyId The companyId to set.
     */
    public void setCompanyId(final String companyId) {
        this.companyId = companyId;
    }

    /**
     * @return Returns the email.
     */
    public String getEmail() {
        return this.email;
    }

    /**
     * @param email The email to set.
     */
    public void setEmail(final String email) {
        this.email = email;
    }

    /**
     * @return Returns the firstName.
     */
    public String getFirstName() {
        return this.firstName;
    }

    /**
     * @param firstName The firstName to set.
     */
    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    /**
     * @return Returns the lastName.
     */
    public String getLastName() {
        return this.lastName;
    }

    /**
     * @param lastName The lastName to set.
     */
    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    /**
     * @return Returns the userName.
     */
    public String getUserName() {
        if (this.userName == null) {
            return "";
        } else {
            return this.userName.toLowerCase();
        }
    }

    /**
     * @param userName The userName to set.
     */
    public void setUserName(final String userName) {
        this.userName = userName.toLowerCase();
    }

    /**
     * @return Returns the password.
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * @param password The password to set.
     */
    public void setPassword(final String password) {
        this.password = password;
    }

    public String getOldpassword() {
        return "";
    }

    public void setOldpassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    /**
     * @return Returns the confirm password.
     */
    public String getConfirm() {
        return this.confirm;
    }

    /**
     * @param confirm The confirm password to set.
     */
    public void setConfirm(final String confirm) {
        this.confirm = confirm;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    /**
     * @return Returns the person context.
     */
    public Node getPerson() {
        return this.person;
    }

    /**
     * @param person The person context to set.
     */
    public void setPerson(final Node person) {
        this.person = person;
    }

    public boolean getEditMode() {
        return this.editMode;
    }

    public boolean setEditMode(final boolean editMode) {
        return this.editMode;
    }

    // ------------------------------------------------------------------------------
    // Validator methods

    /**
     * Set the detail view of the user as Read Only
     */
    public void setReadOnlyUser(final ActionEvent event) {
        // Get the id of the node and the selected version
        final UIActionLink link = (UIActionLink) event.getComponent();
        final Map<String, String> params = link.getParameterMap();

        // get the user name
        final String userName = params.get("userName");

        // test if the mandatories parameter are valid
        ParameterCheck.mandatoryString("The user name", userName);

        // get the person to display as read only
        final NodeRef personNodeRef = getPersonService().getPerson(userName);
        this.person = new Node(personNodeRef);

        populate();

        // set the dialog as read only
        readOnly = true;
    }

    /**
     * Manually launch each validator to have messages correctly. Fill the error map with the field in
     * key and the translated error in value,
     *
     * @return if error was found
     */
    protected boolean validateFields(final boolean validateUsername, final boolean validatePassword,
                                     final boolean validateMadatotyProperties, final boolean addFacesErrorMessages) {
        final FacesContext context = FacesContext.getCurrentInstance();
        this.validationErrors = new HashMap<>(10);

        if (validateUsername) {
            try {
                UserNameValidator.evaluate(getUserName());

                try {
                    final String completeUserName = getUserName();

                    UserNameValidator.evaluateUnicity(getPersonService(), completeUserName);
                } catch (final Exception ex) {
                    validationErrors.put(USERNAME_FIELD, translate(MSG_ERR_USERNAME_DUPLICATE));
                }

            } catch (final Exception ex) {
                if (ex.getMessage().equals(UserNameValidator.getErrorBadCharacters())) {
                    validationErrors.put(USERNAME_FIELD, translate(MSG_ERR_USERNAME_DIGIT));
                } else {
                    final String message = translate(MSG_ERR_USERNAME_SIZE,
                            UserNameValidator.getMinLength(),
                            UserNameValidator.getMaxLength()
                    );

                    validationErrors.put(USERNAME_FIELD, message);
                }
            }
        }

        if (validatePassword) {
            try {
                if (!editMode || (this.oldPassword != null && this.oldPassword.length() != 0)) {
                    new PasswordValidator().evaluate(getPassword());

                    if (!getConfirm().equals(getPassword())) {
                        validationErrors.put(CONFIRM_FIELD, translate(UsersDialog.ERROR_PASSWORD_MATCH));
                    } else if (editMode) {
                        try {
                            final RetryingTransactionHelper txnHelper = Repository
                                    .getRetryingTransactionHelper(FacesContext.getCurrentInstance());
                            final RetryingTransactionCallback<String> callback = new RetryingTransactionCallback<String>() {
                                public String execute() throws Throwable {
                                    getAuthenticationService().updateAuthentication(
                                            (String) person.getProperties().get(ContentModel.PROP_USERNAME.toString()),
                                            oldPassword.toCharArray(),
                                            oldPassword.toCharArray());

                                    return null;
                                }
                            };
                            txnHelper.doInTransaction(callback);
                        } catch (final Exception e) {
                            validationErrors.put(PASSWORD_FIELD, translate(MSG_WRONG_OLD_PASSWORD));
                        }
                    }
                }

            } catch (final ValidatorException ex) {
                // the message is already translated
                validationErrors.put(PASSWORD_FIELD, ex.getFacesMessage().getDetail());
            }
        }

        if (validateMadatotyProperties) {
            try {
                FirstNameValidator.evaluate(getFirstName());
            } catch (final Exception ex) {
                final String message = translate(MSG_ERR_FIRSTNAME_VALID,
                        FirstNameValidator.getMinLength(),
                        FirstNameValidator.getMaxLength()
                );

                validationErrors.put(FIRSTNAME_FIELD, message);
            }

            try {
                LastNameValidator.evaluate(getLastName());
            } catch (Exception ex) {
                final String message = translate(MSG_ERR_LASTNAME_VALID,
                        LastNameValidator.getMinLength(),
                        LastNameValidator.getMaxLength()
                );

                validationErrors.put(LASTNAME_FIELD, message);
            }

            try {
                EmailValidator.evaluate(getEmail());

                try {
                    if (!editMode || !previousEmail.equalsIgnoreCase(getEmail().trim())) {
                        EmailValidator.evaluateUnicity(getUserService(), getEmail());
                    }
                } catch (final Exception ex) {
                    validationErrors.put(EMAIL_FIELD, translate(MSG_ERR_EMAIL_DUPLICATE));
                }
            } catch (final Exception ex) {
                validationErrors.put(EMAIL_FIELD, translate(MSG_ERR_EMAIL_VALID));
            }

            try {
                PhoneValidator.evaluate(getPhone());
            } catch (final Exception ex) {
                validationErrors.put(PHONE_FIELD, translate(MSG_ERR_PHONE_VALID));
            }

            try {
                PostalAddressValidator.evaluate(getPostalAddress());
            } catch (final Exception ex) {
                validationErrors.put(POSTAL_FIELD, translate(MSG_ERR_POSTAL_VALID));
            }
        }

        if (addFacesErrorMessages) {
            for (final String message : validationErrors.values()) {
                addErrorMessage(context, message);
            }
        }

        return validationErrors.size() == 0;
    }

    public Map<String, String> getAllValidationErrors() {
        return this.validationErrors;
    }

    public String getUserNameValidationError() {
        return getValidationErrorMessage(USERNAME_FIELD);
    }

    public String getPasswordValidationError() {
        return getValidationErrorMessage(PASSWORD_FIELD);
    }

    public String getConfirmValidationError() {
        return getValidationErrorMessage(CONFIRM_FIELD);
    }

    public String getFirstNameValidationError() {
        return getValidationErrorMessage(FIRSTNAME_FIELD);
    }

    public String getLastNameValidationError() {
        return getValidationErrorMessage(LASTNAME_FIELD);
    }

    public String getEmailValidationError() {
        return getValidationErrorMessage(EMAIL_FIELD);
    }

    public String getPhoneNumberValidationError() {
        return getValidationErrorMessage(PHONE_FIELD);
    }

    public String getPostalAddressValidationError() {
        return getValidationErrorMessage(POSTAL_FIELD);
    }

    public String getOldPasswordValidationError() {
        return getValidationErrorMessage(OLD_PASSWORD_FIELD);
    }

    protected String getValidationErrorMessage(final String key) {
        if (getAllValidationErrors() == null) {
            return null;
        } else {
            return this.validationErrors.get(key);
        }
    }

    // ------------------------------------------------------------------------------
    // Helper methods

    protected void addErrorMessage(final FacesContext context, final String message) {
        final FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, null, message);
        context.addMessage(null, facesMsg);
    }

    protected void invalidateUserList() {
        UIContextService.getInstance(FacesContext.getCurrentInstance())
                .notifyBeans();
    }

    /**
     * call the init method to reinitialise the fields of the wizard (it is a session wiward)
     */
    protected String getDefaultCancelOutcome() {
        this.reset();
        return super.getDefaultCancelOutcome();
    }

    public String getPhone() {
        return phone;
    }

    /*
     * Setters and acessors for Circabc users fields
     */
    public void setPhone(final String phone) {
        this.phone = phone;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(final String fax) {
        this.fax = fax;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public String getPostalAddress() {
        return this.postalAddress;
    }

    public void setPostalAddress(final String postalAddress) {
        this.postalAddress = postalAddress;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getorgDepNumber() {
        return getOrgDepNumber();
    }

    public String getOrgDepNumber() {
        return this.orgDepNumber;
    }

    public void setOrgDepNumber(final String orgDepNumber) {
        this.orgDepNumber = orgDepNumber;
    }

    public String getDomain() {
        return this.domain;
    }

    public void setDomain(final String domain) {
        this.domain = domain;
    }

    public boolean getIscircadomain() {
        return true;
    }


    /**
     * @return true if the user details should be displayed in Read Only mode
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * @return the authenticationService
     */
    protected final MutableAuthenticationService getAuthenticationService() {
        if (authenticationService == null) {
            authenticationService = Services.getAlfrescoServiceRegistry(FacesContext.getCurrentInstance())
                    .getAuthenticationService();
        }
        return authenticationService;
    }

    /**
     * @param authenticationService the authenticationService to set
     */
    public final void setAuthenticationService(
            final MutableAuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * @return the internalNodeService
     */
    protected final NodeService getInternalNodeService() {
        if (internalNodeService == null) {
            internalNodeService = Services.getCircabcServiceRegistry(FacesContext.getCurrentInstance())
                    .getNonSecuredNodeService();
        }
        return internalNodeService;
    }

    /**
     * @param internalNodeService the internalNodeService to set
     */
    public final void setInternalNodeService(final NodeService internalNodeService) {
        this.internalNodeService = internalNodeService;
    }

    /**
     * @return the personService
     */
    protected final PersonService getPersonService() {
        if (personService == null) {
            personService = Services.getAlfrescoServiceRegistry(FacesContext.getCurrentInstance())
                    .getPersonService();
        }
        return personService;
    }

    /**
     * @param personService the personService to set
     */
    public final void setPersonService(final PersonService personService) {
        this.personService = personService;
    }


    @Override
    protected String doPostCommitProcessing(final FacesContext context, final String outcome) {
        readOnly = false;
        return super.doPostCommitProcessing(context, outcome);
    }

    @Override
    public String cancel() {
        readOnly = false;
        return super.cancel();
    }


    public String getBrowserTitle() {
        return translate("create_circabc_user_browser_title");
    }

    public String getPageIconAltText() {
        return translate("create_circabc_user_icon_tooltip");
    }

    public boolean isFormProvided() {
        return false;
    }
}
