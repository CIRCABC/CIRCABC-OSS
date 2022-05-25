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
package eu.cec.digit.circabc.web.wai.dialog.ig;

import eu.cec.digit.circabc.repo.external.repositories.RegistrationRequest;
import eu.cec.digit.circabc.service.external.repositories.ExternalRepositoriesManagementService;
import eu.cec.digit.circabc.service.external.repositories.PublishResponse;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import eu.cec.digit.circabc.web.wai.wizard.users.InviteCircabcUsersWizard.UserGroupProfile;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIGenericPicker;
import org.alfresco.web.ui.common.component.UIGenericPicker.PickerEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Dialog that backs the external repository publication process.
 * <p>
 * Page: publish-in-external-repositories.jsp
 *
 * @author schwerr
 */
public class PublishInExternalRepositoriesDialog extends BaseWaiDialog {

    private static final long serialVersionUID = 474135268940475915L;

    // Taken from org.alfresco.web.ui.common.component.UIGenericPicker
    private final static int PICKER_ACTION_CLEAR = 1;

    private static final String INTERNAL_SENDER = "publish_in_external_repositories_dialog_select_internal_sender";
    private static final String EXTERNAL_SENDER = "publish_in_external_repositories_dialog_select_external_sender";
    private static final String INTERNAL_RECIPIENT = "publish_in_external_repositories_dialog_select_internal_recipient";
    private static final String EXTERNAL_RECIPIENT = "publish_in_external_repositories_dialog_select_external_recipient";

    private static final String SENDER_NOT_SELECTED = "sender_not_selected";
    private static final String RECIPIENT_NOT_SELECTED = "recipient_not_selected";
    private static final String SENDER_RECIPIENT_NOT_SELECTED = "sender_recipient_not_selected";
    private static final String ERROR_SUBJECT_NULL = "error_subject_null";
    private static final String PUBLISH_SUCCESS = "publish_success";

    private static final String MAIL_TYPE_INTERNAL = "INTERNAL";
    private static final String MAIL_TYPE_INCOMING = "INCOMING";
    private static final String MAIL_TYPE_OUTGOING = "OUTGOING";

    private static final Log logger = LogFactory.getLog(PublishInExternalRepositoriesDialog.class);


    private ExternalRepositoriesManagementService externalRepositoriesManagementService = null;

    private transient List<UserGroupProfile> selectedRecipients = new ArrayList<>();
    private transient DataModel selectedRecipientsDataModel = null;

    private transient List<UserGroupProfile> selectedSenders = new ArrayList<>();
    private transient DataModel selectedSendersDataModel = null;

    private SelectItem[] mailTypes = {new SelectItem(MAIL_TYPE_INTERNAL, MAIL_TYPE_INTERNAL),
            new SelectItem(MAIL_TYPE_INCOMING, MAIL_TYPE_INCOMING),
            new SelectItem(MAIL_TYPE_OUTGOING, MAIL_TYPE_OUTGOING)};

    private transient String selectedMailType = MAIL_TYPE_INTERNAL;

    private String subject = "";
    private String comment = "";


    /**
     * @see eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog#init(java.util.Map)
     */
    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);
        subject = getActionNode().getName();
    }

    /**
     * Fills the picker with the recipients to chose from.
     */
    public SelectItem[] pickerSendersCallback(final int filterIndex, final String contains) {

        List<String> senders = null;

        if (MAIL_TYPE_INCOMING.equals(selectedMailType)) {
            senders = externalRepositoriesManagementService.
                    getExternalEntities(null, contains, null);
        } else {
            senders = externalRepositoriesManagementService.
                    getInternalEntities(null, contains, null);
        }

        List<SelectItem> result = new ArrayList<>();

        for (String sender : senders) {
            result.add(new SelectItem(sender));
        }

        return result.toArray(new SelectItem[result.size()]);
    }

    /**
     * Fills the picker with the recipients to chose from.
     */
    public SelectItem[] pickerRecipientsCallback(final int filterIndex, final String contains) {

        List<String> recipients = null;

        if (MAIL_TYPE_OUTGOING.equals(selectedMailType)) {
            recipients = externalRepositoriesManagementService.
                    getExternalEntities(null, contains, null);
        } else {
            recipients = externalRepositoriesManagementService.
                    getInternalEntities(null, contains, null);
        }

        List<SelectItem> result = new ArrayList<>();

        for (String recipient : recipients) {
            result.add(new SelectItem(recipient));
        }

        return result.toArray(new SelectItem[result.size()]);
    }

    /**
     * Changes the select senders text according to the mail type.
     */
    public String getSendersSelectText() {
        if (MAIL_TYPE_INCOMING.equals(selectedMailType)) {
            return translate(EXTERNAL_SENDER);
        }
        return translate(INTERNAL_SENDER);
    }

    /**
     * Changes the select recipients text according to the mail type.
     */
    public String getRecipientsSelectText() {
        if (MAIL_TYPE_OUTGOING.equals(selectedMailType)) {
            return translate(EXTERNAL_RECIPIENT);
        }
        return translate(INTERNAL_RECIPIENT);
    }

    /**
     * Returns the properties for the selected user JSF DataModel
     *
     * @return JSF DataModel representing the selected user
     */
    public DataModel getSelectedSendersDataModel() {

        if (selectedSendersDataModel == null) {
            selectedSendersDataModel = new ListDataModel();
        }

        selectedSendersDataModel.setWrappedData(this.selectedSenders);

        return selectedSendersDataModel;
    }

    /**
     * Returns the properties for the selected user JSF DataModel
     *
     * @return JSF DataModel representing the selected user
     */
    public DataModel getSelectedRecipientsDataModel() {

        if (selectedRecipientsDataModel == null) {
            selectedRecipientsDataModel = new ListDataModel();
        }

        selectedRecipientsDataModel.setWrappedData(this.selectedRecipients);

        return selectedRecipientsDataModel;
    }

    /**
     * Adds the selected user to the list of senders.
     */
    public void addSelectedSenders(final ActionEvent event) {

        UIGenericPicker picker = (UIGenericPicker)
                event.getComponent().findComponent("sendersPicker");

        String[] results = picker.getSelectedResults();

        if (results == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(translate(SENDER_NOT_SELECTED)));
            return;
        }

        String userName = results[0];

        if (userName.contains("@")) {
            userName = userName.substring(0, userName.indexOf('@'));
        }

        UserGroupProfile profile = new UserGroupProfile(userName, null, null);

        if (!containsProfile(selectedSenders, profile)) {
            selectedSenders.add(profile);
        }
    }

    /**
     * Adds the selected user to the list of recipients.
     */
    public void addSelectedRecipients(final ActionEvent event) {

        UIGenericPicker picker = (UIGenericPicker)
                event.getComponent().findComponent("recipientsPicker");

        String[] results = picker.getSelectedResults();

        if (results == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(translate(RECIPIENT_NOT_SELECTED)));
            return;
        }

        String userName = results[0];

        if (userName.contains("@")) {
            userName = userName.substring(0, userName.indexOf('@'));
        }

        UserGroupProfile profile = new UserGroupProfile(userName, null, null);

        if (!containsProfile(selectedRecipients, profile)) {
            selectedRecipients.add(profile);
        }
    }

    /**
     * Checks if the user is already added to the list.
     */
    private boolean containsProfile(List<UserGroupProfile> users, UserGroupProfile profile) {
        for (UserGroupProfile user : users) {
            if (user.getAuthority().equals(profile.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Action handler called when the Remove button is pressed to remove a user selection
     */
    public void removeSelectedSender(final ActionEvent event) {

        final UserGroupProfile wrapper =
                (UserGroupProfile) selectedSendersDataModel.getRowData();

        if (wrapper != null) {
            selectedSenders.remove(wrapper);
        }
    }

    /**
     * Action handler called when the Remove button is pressed to remove a user selection
     */
    public void removeSelectedRecipient(final ActionEvent event) {

        final UserGroupProfile wrapper =
                (UserGroupProfile) selectedRecipientsDataModel.getRowData();

        if (wrapper != null) {
            selectedRecipients.remove(wrapper);
        }
    }

    /**
     * Gets the value of the mailTypes
     *
     * @return the mailTypes
     */
    public SelectItem[] getMailTypes() {
        return mailTypes;
    }

    /**
     * Gets the value of the selectedMailType
     *
     * @return the selectedMailType
     */
    public String getSelectedMailType() {
        return selectedMailType;
    }

    /**
     * Sets the value of the selectedMailType
     *
     * @param selectedMailType the selectedMailType to set.
     */
    public void setSelectedMailType(String selectedMailType) {
        this.selectedMailType = selectedMailType;
    }

    /**
     * Gets the value of the subject
     *
     * @return the subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Sets the value of the subject
     *
     * @param subject the subject to set.
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * Gets the value of the comment
     *
     * @return the comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * Sets the value of the comment
     *
     * @param comment the comment to set.
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * If the value changed, prepare everything to set the right labels and call the right methods for
     * senders and recipients.
     */
    public void changedMailType(ValueChangeEvent event) {

        // Get new select value
        selectedMailType = (String) event.getNewValue();

        // Clear selected elements
        selectedRecipients.clear();
        selectedSenders.clear();

        // Clear lists to select
        UIGenericPicker sendersPicker = (UIGenericPicker)
                event.getComponent().findComponent("sendersPicker");
        PickerEvent sendersPickerEvent = new PickerEvent(sendersPicker,
                PICKER_ACTION_CLEAR, 0, null, null);
        sendersPicker.broadcast(sendersPickerEvent);

        UIGenericPicker recipientsPicker = (UIGenericPicker)
                event.getComponent().findComponent("recipientsPicker");
        PickerEvent receiversPickerEvent = new PickerEvent(recipientsPicker,
                PICKER_ACTION_CLEAR, 0, null, null);
        recipientsPicker.broadcast(receiversPickerEvent);
    }

    /**
     * @see eu.cec.digit.circabc.web.wai.dialog.WaiDialog#getPageIconAltText()
     */
    @Override
    public String getPageIconAltText() {
        return translate("publish_in_external_repositories_dialog_description");
    }

    /**
     * @see eu.cec.digit.circabc.web.wai.dialog.WaiDialog#getBrowserTitle()
     */
    @Override
    public String getBrowserTitle() {
        return translate("publish_in_external_repositories_dialog_title");
    }

    /**
     * @see org.alfresco.web.bean.dialog.BaseDialogBean#finishImpl(javax.faces.context.FacesContext,
     * java.lang.String)
     */
    @Override
    protected String finishImpl(FacesContext context, String outcome)
            throws Throwable {

        if (selectedSenders.isEmpty() && selectedRecipients.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(translate(SENDER_RECIPIENT_NOT_SELECTED)));
            return null;
        }

        if (selectedSenders.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(translate(SENDER_NOT_SELECTED)));
            return null;
        }

        if (selectedRecipients.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(translate(RECIPIENT_NOT_SELECTED)));
            return null;
        }

        // Test parameters not null
        if (subject == null || subject.length() == 0) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(translate(ERROR_SUBJECT_NULL)));
            return null;
        }
        if (comment == null) {
            comment = "";
        }

        // Fill the registration data
        RegistrationRequest registrationRequest = new RegistrationRequest();
        registrationRequest.setSubject(subject);
        registrationRequest.setMailType(selectedMailType);
        registrationRequest.setComments(comment);

        // Senders
        List<String> senders = new ArrayList<>();
        for (UserGroupProfile selectedSender : selectedSenders) {
            String userId = selectedSender.getAuthority();
            int idx = userId.lastIndexOf("-");
            senders.add(userId.substring(idx + 2));
        }
        registrationRequest.setSenderIds(senders);

        // Recipients
        List<String> recipients = new ArrayList<>();
        for (UserGroupProfile selectedRecipient : selectedRecipients) {
            String userId = selectedRecipient.getAuthority();
            int idx = userId.lastIndexOf("-");
            recipients.add(userId.substring(idx + 2));
        }
        registrationRequest.setRecipientIds(recipients);

        // Publish
        PublishResponse response = externalRepositoriesManagementService.
                publishDocument(ExternalRepositoriesManagementService.
                        EXTERNAL_REPOSITORY_NAME, getActionNode().getNodeRef().
                        toString(), registrationRequest);

        if (!response.isSuccess()) {

            String error = response.getMessage();

            // Another error, we can stay on the page
            String errorMsg = "";

            int idx = error.indexOf(" ");

            if (idx > 0) {
                errorMsg = error.substring(idx + 1);
                error = error.substring(0, idx);
            }

            String message = null;

            // If workflow fails
            if (ExternalRepositoriesManagementService.
                    PUBLISH_SUCCESS_WORKFLOW_FAILURE.equals(error)) {

                clear();

                message = translate(error) + ". Server replied: " + errorMsg;

                Utils.addStatusMessage(FacesMessage.SEVERITY_INFO, message);

                log(false, message);

                return outcome;
            }

            message = translate(error) + " " + errorMsg;

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(message));
            log(false, message);

            return null;
        }

        clear();

        // Info message to inform that the document was published successfully
        Utils.addStatusMessage(FacesMessage.SEVERITY_INFO, translate(PUBLISH_SUCCESS));

        log(true, response.getMessage());

        return outcome;
    }

    /**
     * Log registration and workflow
     */
    protected void log(boolean success, String info) {

        logRecord.setDate(new Date());
        logRecord.setOK(success);
        logRecord.setUser(AuthenticationUtil.getFullyAuthenticatedUser());
        logRecord.setInfo(info);

        getLogService().log(logRecord);

        if (success && logger.isInfoEnabled()) {
            logger.info(info);
        } else if (!success) {
            logger.error(info);
        }
    }

    /**
     * @see org.alfresco.web.bean.dialog.BaseDialogBean#cancel()
     */
    @Override
    public String cancel() {
        clear();
        return super.cancel();
    }

    /**
     * Clears the content of the input fields.
     */
    private void clear() {
        selectedRecipients.clear();
        selectedSenders.clear();
        subject = "";
        comment = "";
    }

    /**
     * Sets the value of the externalRepositoriesManagementService
     *
     * @param externalRepositoriesManagementService the externalRepositoriesManagementService to set.
     */
    public void setExternalRepositoriesManagementService(
            ExternalRepositoriesManagementService externalRepositoriesManagementService) {
        this.externalRepositoriesManagementService = externalRepositoriesManagementService;
    }
}
