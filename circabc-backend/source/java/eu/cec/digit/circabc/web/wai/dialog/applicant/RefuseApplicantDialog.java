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
package eu.cec.digit.circabc.web.wai.dialog.applicant;

import eu.cec.digit.circabc.model.UserModel;
import eu.cec.digit.circabc.repo.applicant.Applicant;
import eu.cec.digit.circabc.service.customisation.mail.MailTemplate;
import eu.cec.digit.circabc.service.customisation.mail.MailWrapper;
import eu.cec.digit.circabc.service.profile.ProfileManagerService;
import eu.cec.digit.circabc.service.profile.permissions.DirectoryPermissions;
import eu.cec.digit.circabc.service.user.UserService;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIActionLink;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.mail.MessagingException;
import java.io.Serializable;
import java.util.*;

/**
 * Bean that backs manage the refuse applicant dialog.
 *
 * @author Yanick Pignot
 */
public class RefuseApplicantDialog extends BaseWaiDialog {

    /**
     * The name of this dialog
     */
    public static final String DIALOG_NAME = "RefuseApplicantDialog";
    public static final Locale DEFAULT_MAIL_LANGUAGE = I18NUtil.parseLocale("en");
    private static final long serialVersionUID = 4509470922669983727L;
    private static final Log logger = LogFactory.getLog(RefuseApplicantDialog.class);
    private static final String KEY_APPLICATION_DATE = "applicationDate";
    private static final String KEY_REASON = "reason";
    //	the used message keys
    private static final String MESSAGE_ID_TITLE = "refuse_applicant_page_title";
    private static final String MESSAGE_ID_DESCRIPTION = "refuse_applicant_page_description";
    private static final String MESSAGE_ID_OK = "ok";
    private static final String MESSAGE_ID_CANCEL = "cancel";
    private static final String NOTIFY_YES = "yes";
    private static final String NOTIFY_NO = "no";
    /**
     * PersonService bean reference
     */
    private transient PersonService personService;
    private String message;
    private Locale mailLanguage;
    private List<WebApplicant> applicants;
    private String notifyDirAdmins = NOTIFY_NO;
    private String notifyRefusedUsers = NOTIFY_YES;

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);

        if (getActionNode() == null) {
            throw new IllegalArgumentException("The node ID is a manadtory parameter");
        }

        // the mail will be sent in English by default.
        this.mailLanguage = DEFAULT_MAIL_LANGUAGE;
        this.message = "";
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception {
        final NodeRef currentSpace = getActionNode().getNodeRef();
        final ProfileManagerService profManagerService = getProfileManagerServiceFactory()
                .getProfileManagerService(currentSpace);

        try {
            // send the mails
            sendMail();
            int applicantsSize = applicants.size();
            if (applicantsSize > 1) {
                getLogRecord().addInfo("Refuse applicants: ");
            } else {
                getLogRecord().addInfo("Refuse applicant: ");
            }

            int applicantCount = 0;
            for (WebApplicant webApplicant : applicants) {
                // remove the currenty
                profManagerService
                        .removeApplicantPerson(currentSpace, webApplicant.getApplicant().getUserName());
                getLogRecord().addInfo(webApplicant.getApplicant().getDisplayName());
                ++applicantCount;
                if (applicantCount != applicantsSize) {
                    getLogRecord().addInfo(", ");
                }

            }

            applicants = new ArrayList<>();

            // All the applications is correctly refused, Add confirmation
            // message
            Map<Object, Object> session = FacesContext.getCurrentInstance().getExternalContext()
                    .getSessionMap();
            session.put(ManageApplicantDialog.MANAGE_APPLICANT_DIALOG_INFORMATION_MESSAGE,
                    translate(ManageApplicantDialog.MESSAGE_REFUSE_APPLICATION_SUCCESS));


        } catch (Exception e) {
            logger.error("Impossible to refuse this/these appliation(s): "
                    + applicants + " in the interest group "
                    + getCurrentSpaceName(), e);

            Utils
                    .addErrorMessage(translate(ManageApplicantDialog.MESSAGE_REFUSE_APPLICATION_FAILED), e);

        }

        return AlfrescoNavigationHandler.CLOSE_DIALOG_OUTCOME;
    }

    /**
     * @return the current space where the user launch this dialog
     */
    public String getCurrentSpaceName() {
        return getActionNode().getName();
    }

    /**
     * Set the detail view of the user as Read Only
     */
    public void refuseApplicantAction(ActionEvent event) {
        // Get the id of the node and the selected version
        UIActionLink link = (UIActionLink) event.getComponent();
        Map<String, String> params = link.getParameterMap();
        super.init(params);

        // get the user name
        String userName = params.get("userName");

        // check that the parameter is not null. It can be empty, it means to remove all applicants.
        if (userName == null) {
            throw new IllegalArgumentException("userName is a mandatory parameter");
        }

        applicants = new ArrayList<>();

        final ProfileManagerService profManagerService = getProfileManagerServiceFactory()
                .getProfileManagerService(getActionNode().getNodeRef());

        final Map<String, Applicant> allApplicants = profManagerService
                .getApplicantUsers(getActionNode().getNodeRef());

        // if empty string, refuse all applicants!
        if (userName.trim().length() < 1) {
            // add all applicants
            for (final Applicant applicant : allApplicants.values()) {
                applicants.add(new WebApplicant(applicant));
            }
        } else {
            // add only the selected user
            applicants.add(new WebApplicant(allApplicants.get(userName)));
        }
    }

    /**
     * Util method a preconfigured mail to notify to the user the reject of its application
     */
    private void sendMail() {
        final NodeRef circabcRoot = getManagementService().getCircabcNodeRef();
        // For each applicant, send a refusing email
        for (final WebApplicant applicant : applicants) {
            final NodeRef personNodeRef = getPersonService().getPerson(applicant.getUserName());
            final String receiverEmail = (String) getNodeService()
                    .getProperty(personNodeRef, ContentModel.PROP_EMAIL);

            final Map<String, Object> model = getMailPreferencesService()
                    .buildDefaultModel(getActionNode().getNodeRef(), personNodeRef, null);

            model.put(KEY_APPLICATION_DATE, applicant.getDate());
            model.put(KEY_REASON, this.message.trim());

            final MailWrapper mail = getMailPreferencesService()
                    .getDefaultMailTemplate(circabcRoot, MailTemplate.REFUSE_APPLICATION);

            final Serializable langObject = getUserService()
                    .getPreference(personNodeRef, UserService.PREF_INTERFACE_LANGUAGE);
            final Locale locale;
            if (langObject == null) {
                locale = null;
            } else if (langObject instanceof Locale) {
                locale = (Locale) langObject;
            } else {
                locale = new Locale(langObject.toString());
            }

            try {
                if (NOTIFY_YES.equals(this.notifyRefusedUsers)) {
                    // Send the message
                    getMailService().send(getMailService().getNoReplyEmailAddress(), receiverEmail, null,
                            mail.getSubject(model, locale), mail.getBody(model, locale), true, false);
                }

            } catch (Throwable t) {
                // don't stop the action but let admins know email is not getting sent
                logger.warn("Failed to send email to " + receiverEmail, t);
            }

            if (NOTIFY_YES.equals(this.notifyDirAdmins)) {
                final Set<String> dirAdmins = getUserService()
                        .getUsersWithPermission(getActionNode().getNodeRef(),
                                DirectoryPermissions.DIRADMIN.toString());
                for (String dirAdmin : dirAdmins) {
                    if (getPersonService().personExists(dirAdmin)) {
                        final NodeRef adminPerson = getPersonService()
                                .getPerson(dirAdmin);
                        final Boolean globalNotification = (Boolean) getNodeService()
                                .getProperty(adminPerson,
                                        UserModel.PROP_GLOBAL_NOTIFICATION);
                        if (globalNotification != null && globalNotification) {
                            MailWrapper mailWrapper = getMailPreferencesService()
                                    .getDefaultMailTemplate(
                                            circabcRoot,
                                            MailTemplate.REFUSE_APLICANT_NOTIFICATION);
                            String from = getMailService()
                                    .getNoReplyEmailAddress();
                            final Map<QName, Serializable> personProperties = getNodeService()
                                    .getProperties(adminPerson);
                            final String to = (String) personProperties
                                    .get(ContentModel.PROP_EMAIL);
                            boolean html = true;
                            try {
                                getMailService().send(from, to, null,
                                        mailWrapper.getSubject(model),
                                        mailWrapper.getBody(model), html, false);
                            } catch (MessagingException e) {
                                if (logger.isErrorEnabled()) {
                                    logger.error(e);
                                }
                            }
                        }
                    }
                }

            }


        }
    }

    @Override
    public Node getActionNode() {
        return getNavigator().getCurrentIGRoot();
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.bean.dialog.BaseDialogBean#getContainerDescription()
     */
    @Override
    public String getContainerDescription() {
        // get the I18N description of the dialog in the extension/webclient.properties
        return translate(MESSAGE_ID_DESCRIPTION, getBestTitle(getActionNode()));
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.bean.dialog.BaseDialogBean#getContainerTitle()
     */
    @Override
    public String getContainerTitle() {
        // get the I18N title of the dialog in the extension/webclient.properties
        return translate(MESSAGE_ID_TITLE);
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.bean.dialog.BaseDialogBean#getCancelButtonLabel()
     */
    @Override
    public String getCancelButtonLabel() {
        // The cancel button must be renamed as 'Close'. No sens to keep 'cancel'
        return translate(MESSAGE_ID_CANCEL);
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.bean.dialog.BaseDialogBean#getFinishButtonLabel()
     */
    @Override
    public String getFinishButtonLabel() {
        // The finish button must be renamed as 'OK'
        return translate(MESSAGE_ID_OK);
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.bean.dialog.BaseDialogBean#getFinishButtonDisabled()
     */
    @Override
    public boolean getFinishButtonDisabled() {
        // always available, the message is optional
        return false;
    }


    /* (non-Javadoc)
     * @see org.alfresco.web.bean.dialog.BaseDialogBean#getErrorMessageId()
     */
    @Override
    protected String getErrorMessageId() {
        return ManageApplicantDialog.MESSAGE_REFUSE_APPLICATION_FAILED;
    }

    /**
     * @return the applicants to be refused
     */
    public List<WebApplicant> getApplicants() {
        return applicants;
    }

    /**
     * @return the message to explain the reason of the refusing to sent to the applicant
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message the message to explain the reason of the refusing to sent to the applicant to
     *                set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return the Language in which the mail will be sent
     */
    public Locale getMailLanguage() {
        return mailLanguage;
    }


    /**
     * @param Language in which the mail will be sent to set
     */
    public void setMailLanguage(Locale mailLanguage) {
        this.mailLanguage = mailLanguage;
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
    public final void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    public String getBrowserTitle() {
        //TODO change me
        return getContainerTitle();
    }

    public String getPageIconAltText() {
        // TODO change me
        return getContainerTitle();
    }

    public String getNotifyDirAdmins() {
        return notifyDirAdmins;
    }

    public void setNotifyDirAdmins(String notifyDirAdmins) {
        this.notifyDirAdmins = notifyDirAdmins;
    }

    public String getNotifyRefusedUsers() {
        return notifyRefusedUsers;
    }

    public void setNotifyRefusedUsers(String notifyRefusedUsers) {
        this.notifyRefusedUsers = notifyRefusedUsers;
    }
}
