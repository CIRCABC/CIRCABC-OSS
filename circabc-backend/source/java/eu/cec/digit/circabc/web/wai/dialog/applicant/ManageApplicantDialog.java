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

import eu.cec.digit.circabc.repo.applicant.Applicant;
import eu.cec.digit.circabc.service.profile.ProfileManagerService;
import eu.cec.digit.circabc.web.PermissionUtils;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.bean.repository.Node;

import javax.faces.context.FacesContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Bean that backs manage applicants dialog.
 *
 * @author Yanick Pignot
 */
public class ManageApplicantDialog extends BaseWaiDialog {

    public static final String MANAGE_APPLICANT_DIALOG_INFORMATION_MESSAGE = "ManageApplicantDialog. informationMessage";
    // the used confirmation/error message
    public static final String MESSAGE_APPLY_MEMBERSHIP_SUCCESS = "apply_membership_process_success";
    public static final String MESSAGE_APPLY_MEMBERSHIP_FAILED = "apply_membership_process_failed";
    public static final String MESSAGE_REFUSE_APPLICATION_SUCCESS = "refuse_applicant_process_success";
    public static final String MESSAGE_REFUSE_APPLICATION_FAILED = "refuse_applicant_process_failed";
    private static final long serialVersionUID = 881597868117140631L;
    /**
     * The default number of audit entries to display by screen
     */
    private static final int DEFAULT_PAGINATION_SIZE = 25;
    //	the used message keys
    private static final String MESSAGE_ID_TITLE = "manage_applicants_page_title";
    private static final String MESSAGE_ID_DESCRIPTION = "manage_applicants_page_description";
    private static final String MESSAGE_ID_CLOSE = "close";

    //public static String informationMessage;

    @Override
    public void init(Map<String, String> arg0) {
        super.init(arg0);

        if (getActionNode() == null) {
            throw new IllegalArgumentException("The node ID is a manadtory parameter");
        }
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception {
        return outcome;
    }

    /**
     * @return the current space where the user launch this dialog
     */
    public Node getCurrentSpace() {
        return getActionNode();
    }

    /**
     * @return the current space where the user launch this dialog
     */
    public String getCurrentSpaceName() {
        return (String) getActionNode().getName();
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
        return translate(MESSAGE_ID_CLOSE);
    }

    /**
     * @return the informationMessage
     */
    public String getInformationMessage() {
        // the message should be displayed only one time
        Map<Object, Object> session = FacesContext.getCurrentInstance().getExternalContext()
                .getSessionMap();
        String toReturn = (String) session.get(MANAGE_APPLICANT_DIALOG_INFORMATION_MESSAGE);
        session.put(MANAGE_APPLICANT_DIALOG_INFORMATION_MESSAGE, null);
        return toReturn;
    }

    /**
     * @param informationMessage the informationMessage to set
     */
    public void setInformationMessage(String informationMessage) {
        Map<Object, Object> session = FacesContext.getCurrentInstance().getExternalContext()
                .getSessionMap();
        session.put(MANAGE_APPLICANT_DIALOG_INFORMATION_MESSAGE, informationMessage);
    }

    /**
     * @return true if a message is set to be displayed to the screen
     */
    public boolean getHasInformationMessage() {
        Map<Object, Object> session = FacesContext.getCurrentInstance().getExternalContext()
                .getSessionMap();
        String informationMessage = (String) session.get(MANAGE_APPLICANT_DIALOG_INFORMATION_MESSAGE);
        return informationMessage != null && informationMessage.length() > 1;
    }

    /**
     * @return the pagination
     */
    public int getPagination() {
        return DEFAULT_PAGINATION_SIZE;
    }

    public boolean isListEmpty() {
        final ProfileManagerService profManagerService = getProfileManagerServiceFactory()
                .getProfileManagerService(getActionNode().getNodeRef());

        return profManagerService.getApplicantUsers(getActionNode().getNodeRef()).size() < 1;
    }

    /**
     * @return the list of applicants being asked to become a membership of the current Interest Group
     */
    public List<Applicant> getApplicants() {
        NodeRef space = getActionNode().getNodeRef();
        ProfileManagerService profManagerService = getProfileManagerServiceFactory()
                .getProfileManagerService(space);

        //	get the applicants
        Map<String, Applicant> applicantsMap = profManagerService.getApplicantUsers(space);

        // return them as a list
        if (applicantsMap.size() < 1) {
            return Collections.emptyList();
        } else {
            return new ArrayList<>(applicantsMap.values());
        }
    }

    public Integer getApplicantFilterIndex() {
        return PermissionUtils.getApplicantFilterIndex();
    }

    public String getBrowserTitle() {
        //TODO change me
        return getContainerTitle();
    }

    public String getPageIconAltText() {
        // TODO change me
        return getContainerTitle();
    }
}
