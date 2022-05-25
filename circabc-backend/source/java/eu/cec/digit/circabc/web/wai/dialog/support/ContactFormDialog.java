/**
 * Copyright 2006 European Community
 * <p>
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 * <p>
 * https://joinup.ec.europa.eu/software/page/eupl
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence
 * is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the Licence for the specific language governing permissions and limitations under
 * the Licence.
 */
/**
 *
 */
package eu.cec.digit.circabc.web.wai.dialog.support;

import eu.cec.digit.circabc.CircabcConfig;
import eu.cec.digit.circabc.repo.support.SupportContact;
import eu.cec.digit.circabc.repo.support.SupportTypes;
import eu.cec.digit.circabc.service.support.SupportService;
import eu.cec.digit.circabc.util.CircabcUserDataBean;
import eu.cec.digit.circabc.web.Beans;
import eu.cec.digit.circabc.web.bean.override.CircabcNavigationBean;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.ui.common.Utils;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author beaurpi
 *
 */
public class ContactFormDialog extends BaseWaiDialog {

    /**
     *
     */
    private static final long serialVersionUID = 2308513408437610115L;
    private static final String INTERNAL_COM = "com";
    private static final String INTERNAL_EXT = "ext";
    private static final String CENTRAL_ID = "central";
    private static final String HELPDESK_ID = "digit";
    private static final String helpdesk_selection = "contact_dialog_request_helpdesk_selection";
    private static final String helpdesk_selection_guessed = "contact_dialog_request_helpdesk_selection_guessed";
    private static final String organisation_inside = "contact_dialog_request_helpdesk_selection_inside";
    private static final String organisation_outside = "contact_dialog_request_helpdesk_selection_outside";
    private List<SupportContact> contacts;
    private List<SelectItem> contactAsItems;
    private String selectedContact;
    private SupportService supportService;
    private CircabcUserDataBean circabcUser;
    private String guessedHelpdesk;
    private Boolean insideStaff;
    private String organisationHelpdesk;

    private String subject;
    private String mail;
    private String description;
    private Integer selectedType;
    private List<SelectItem> types;

    private String savedOutcome;
    private Boolean firstOutcome = true;

    private boolean loggedIn = false;

    /**
     * @return the supportService
     */
    public SupportService getSupportService() {
        return supportService;
    }

    /**
     * @param supportService the supportService to set
     */
    public void setSupportService(SupportService supportService) {
        this.supportService = supportService;
    }

    @Override
    public String getPageIconAltText() {

        return getBrowserTitle();
    }

    @Override
    public String getBrowserTitle() {

        return translate("contact_dialog_title");
    }

    @Override
    public void init(Map<String, String> parameters) {

        super.init(parameters);

        contacts = supportService.getAllSupportContacts();
        contactAsItems = new ArrayList<>();

        String username = getCurrentUserName();

        if (username != null) {
            circabcUser = this.getUserService().getLDAPUserDataByUid(username);
            String org = circabcUser.getSourceOrganisation();

            if (org.equalsIgnoreCase(INTERNAL_COM) || org.equalsIgnoreCase(INTERNAL_EXT)) {
                selectedContact = circabcUser.getDg().toLowerCase();
                insideStaff = true;

                if (!CircabcConfig.ECHA) {
                    // for normal CIRCABC
                    for (SupportContact contact : contacts) {
                        contactAsItems.add(new SelectItem(contact.getId(),
                                contact.getTitle() + " ( " + contact.getEmail() + " )"));
                    }
                } else {
                    // for ECHA (S-CIRCABC) and user logged in
                    selectedContact = HELPDESK_ID;
                    contactAsItems.add(new SelectItem(selectedContact, getHelpdeskContactAsString()));
                }
            } else {
                selectedContact = CENTRAL_ID;
                insideStaff = false;

                contactAsItems.add(new SelectItem(selectedContact, getCentralContactAsString()));
            }

            loggedIn = true;
        } else {
            selectedContact = CENTRAL_ID;
            insideStaff = false;

            contactAsItems.add(new SelectItem(selectedContact, getCentralContactAsString()));

            if (CircabcConfig.ECHA) {
                // for ECHA (S-CIRCABC) and user not logged in
                contactAsItems.add(new SelectItem(selectedContact, getHelpdeskContactAsString()));
            }

            loggedIn = false;
        }

        guessedHelpdesk = computeGuessedHelpdesk();
        setOrganisationHelpdesk(computeOrganisationHelpdesk());

        initTypes();

    }

    /**
     * @return the loggedIn
     */
    public boolean isLoggedIn() {
        return loggedIn;
    }

    private void initTypes() {

        types = new ArrayList<>();
        types.add(new SelectItem(SupportTypes.TYPE_ECAS_USER_ACCOUNT.getId(),
                translate(SupportTypes.TYPE_ECAS_USER_ACCOUNT.getDescription())));
        types.add(new SelectItem(SupportTypes.TYPE_DOWNLOAD_LIBRARY.getId(),
                translate(SupportTypes.TYPE_DOWNLOAD_LIBRARY.getDescription())));
        types.add(new SelectItem(SupportTypes.TYPE_UPLOAD_LIBRARY.getId(),
                translate(SupportTypes.TYPE_UPLOAD_LIBRARY.getDescription())));
        types.add(new SelectItem(SupportTypes.TYPE_EVENT.getId(),
                translate(SupportTypes.TYPE_EVENT.getDescription())));
        types.add(new SelectItem(SupportTypes.TYPE_IG_MANAGEMENT.getId(),
                translate(SupportTypes.TYPE_IG_MANAGEMENT.getDescription())));
        types.add(new SelectItem(SupportTypes.TYPE_MEMBERSHIP.getId(),
                translate(SupportTypes.TYPE_MEMBERSHIP.getDescription())));
        types.add(new SelectItem(SupportTypes.TYPE_NEWSGROUP.getId(),
                translate(SupportTypes.TYPE_NEWSGROUP.getDescription())));
        types.add(new SelectItem(SupportTypes.TYPE_OTHER.getId(),
                translate(SupportTypes.TYPE_OTHER.getDescription())));

    }

    private String getCurrentUserName() {
        final CircabcNavigationBean navigator = Beans.getWaiNavigator();

        return (navigator.isGuest()) ? null : navigator.getCurrentUser().getUserName();
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome)
            throws Throwable {

        if (firstOutcome) {
            savedOutcome = outcome;
            firstOutcome = false;
        }

        String newOutcome = outcome;

        if (validateFields()) {
            newOutcome = savedOutcome;
            NodeRef actionNode = null;
            if (this.getActionNode() != null) {
                actionNode = this.getActionNode().getNodeRef();
            } else {
                actionNode = this.getManagementService().getCircabcNodeRef();
            }

            Boolean result = false;

            if (getCurrentUserName() == null) {
                result = this.supportService
                        .sendSupportRequestAsGuest(subject, description, SupportTypes.getById(selectedType),
                                mail, supportService.getContactById(selectedContact), actionNode);
            } else {
                result = this.supportService
                        .sendSupportRequest(subject, description, SupportTypes.getById(selectedType),
                                getCurrentUserName(), supportService.getContactById(selectedContact), actionNode);
            }

            if (result) {
                Utils.addStatusMessage(FacesMessage.SEVERITY_INFO,
                        "Mail successfully sent to :" + supportService.getContactById(selectedContact)
                                .getEmail());
            } else {
                Utils.addStatusMessage(FacesMessage.SEVERITY_ERROR,
                        "Mail unccessfully sent to :" + supportService.getContactById(selectedContact)
                                .getEmail());
            }
            reinit();
        } else {
            newOutcome = null;
        }

        return newOutcome;
    }

    private void reinit() {
        firstOutcome = true;
        subject = "";
        description = "";
        mail = "";

    }

    private boolean validateFields() {

        Boolean result = true;

        if (this.subject.equals("")) {
            Utils.addErrorMessage(translate("contact_dialog_subject_empty"));
            result = false;
        }

        if (this.description.length() == 0 || this.description.equals("<p></p>")) {
            Utils.addErrorMessage(translate("contact_dialog_description_empty"));
            result = false;
        }
        if (getCurrentUserName() == null) {
            if (!mail.matches(".*@.*\\..*")) {
                Utils.addErrorMessage(translate("contact_dialog_mail_empty"));
                result = false;
            }
        }

        return result;
    }

    /**
     * @return the contacts
     */
    public List<SupportContact> getContacts() {
        return contacts;
    }

    /**
     * @param contacts the contacts to set
     */
    public void setContacts(List<SupportContact> contacts) {
        this.contacts = contacts;
    }

    public List<SelectItem> getContactAsItems() {
        return contactAsItems;
    }

    /**
     * @param contactAsItems the contactAsItems to set
     */
    public void setContactAsItems(List<SelectItem> contactAsItems) {
        this.contactAsItems = contactAsItems;
    }

    /**
     * @return the selectedContact
     */
    public String getSelectedContact() {
        return selectedContact;
    }

    /**
     * @param selectedContact the selectedContact to set
     */
    public void setSelectedContact(String selectedContact) {
        this.selectedContact = selectedContact;
    }

    public String computeOrganisationHelpdesk() {
        return translate(helpdesk_selection,
                (insideStaff == true ? translate(organisation_inside) : translate(organisation_outside)));
    }

    public String computeGuessedHelpdesk() {
        return translate(helpdesk_selection_guessed, guessHelpdesk());
    }

    /**
     * @return
     */
    private String guessHelpdesk() {
        String result = getCentralContactAsString();

        if (insideStaff) {
            String dg = circabcUser.getDg().toLowerCase();
            for (SupportContact contact : contacts) {
                if (contact.getId().equals(dg)) {
                    result = contact.getTitle() + " ( " + contact.getEmail() + " )";
                    break;
                }
            }
        }

        return result;
    }

    private String getCentralContactAsString() {
        String result = "";
        for (SupportContact contact : contacts) {
            if (contact.getId().equals(CENTRAL_ID)) {
                result = contact.getTitle() + " ( " + contact.getEmail() + " )";
            }
        }

        return result;
    }

    private String getHelpdeskContactAsString() {
        String result = "";
        for (SupportContact contact : contacts) {
            if (contact.getId().equals(HELPDESK_ID)) {
                result = contact.getTitle() + " ( " + contact.getEmail() + " )";
            }
        }

        return result;
    }

    /**
     * @return the guessedHelpdesk
     */
    public String getGuessedHelpdesk() {
        return guessedHelpdesk;
    }

    /**
     * @param guessedHelpdesk the guessedHelpdesk to set
     */
    public void setGuessedHelpdesk(String guessedHelpdesk) {
        this.guessedHelpdesk = guessedHelpdesk;
    }

    /**
     * @return the organistationHelpdesk
     */
    public String getOrganisationHelpdesk() {
        return organisationHelpdesk;
    }

    /**
     * @param organistationHelpdesk the organistationHelpdesk to set
     */
    public void setOrganisationHelpdesk(String organisationHelpdesk) {
        this.organisationHelpdesk = organisationHelpdesk;
    }

    /**
     * @return the subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * @param subject the subject to set
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * @return the selectedType
     */
    public Integer getSelectedType() {
        return selectedType;
    }

    /**
     * @param selectedType the selectedType to set
     */
    public void setSelectedType(Integer selectedType) {
        this.selectedType = selectedType;
    }

    /**
     * @return the types
     */
    public List<SelectItem> getTypes() {
        return types;
    }

    /**
     * @param types the types to set
     */
    public void setTypes(List<SelectItem> types) {
        this.types = types;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the mail
     */
    public String getMail() {
        return mail;
    }

    /**
     * @param mail the mail to set
     */
    public void setMail(String mail) {
        this.mail = mail;
    }
}
