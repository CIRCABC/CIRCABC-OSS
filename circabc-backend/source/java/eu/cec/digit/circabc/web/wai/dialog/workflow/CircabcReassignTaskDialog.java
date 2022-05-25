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
package eu.cec.digit.circabc.web.wai.dialog.workflow;

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.service.log.LogRecord;
import eu.cec.digit.circabc.service.log.LogService;
import eu.cec.digit.circabc.service.profile.IGRootProfileManagerService;
import eu.cec.digit.circabc.service.profile.ProfileManagerServiceFactory;
import eu.cec.digit.circabc.service.struct.ManagementService;
import eu.cec.digit.circabc.util.PathUtils;
import eu.cec.digit.circabc.web.Beans;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.WebClientHelper;
import eu.cec.digit.circabc.web.bean.override.CircabcNavigationBean;
import eu.cec.digit.circabc.web.wai.dialog.WaiDialog;
import eu.cec.digit.circabc.web.wai.manager.ActionsListWrapper;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.repository.User;
import org.alfresco.web.bean.workflow.ReassignTaskDialog;
import org.alfresco.web.ui.common.SortableSelectItem;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIGenericPicker;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.jsf.FacesContextUtils;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import javax.transaction.UserTransaction;
import java.io.Serializable;
import java.util.*;

public class CircabcReassignTaskDialog extends ReassignTaskDialog implements
        WaiDialog {

    private static final String PLEASE_SELECT_USER = "please_select_user";

    private static final String PLEASE_CLICK_OK_TO_REASSIGN_USER = "please_click_ok_to_reassign_user";
    private static final Log logger = LogFactory.getLog(CircabcCancelWorkflowDialog.class);
    /**
     *
     */
    private static final long serialVersionUID = -5447750051262620606L;
    private NodeRef igNodeRef;
    private String userName = null;
    private String owner = null;
    private IGRootProfileManagerService igRootProfileManagerService;
    private transient LogService logService;
    private LogRecord logRecord;
    private transient ManagementService managementService;
    private transient MultilingualContentService multilingualContentService;

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);
        logRecord = new LogRecord();
        igNodeRef = null;
        WorkflowTask task;
        task = this.getWorkflowService().getTaskById(taskId);
        if (task != null) {
            owner = (String) task.getProperties().get(ContentModel.PROP_OWNER);
            NodeRef workflowPackage = (NodeRef) task.properties
                    .get(WorkflowModel.ASSOC_PACKAGE);
            if (getNodeService().hasAspect(workflowPackage,
                    CircabcModel.ASPECT_BELONG_TO_INTEREST_GROUP)) {
                igNodeRef = (NodeRef) getNodeService().getProperty(
                        workflowPackage,
                        CircabcModel.PROP_INTEREST_GROUP_NODE_REF);
            }

        }

        final ProfileManagerServiceFactory factory = Services
                .getCircabcServiceRegistry(FacesContext.getCurrentInstance())
                .getProfileManagerServiceFactory();
        igRootProfileManagerService = factory.getIGRootProfileManagerService();

        CircabcNavigationBean navigator = Beans.getWaiNavigator();
        if (navigator != null) {
            logRecord.setService("Workflow");
            logRecord.setActivity("Reassign");
            updateLogDocument(navigator.getCurrentNode().getNodeRef(), logRecord);
        } else {
            if (logger.isErrorEnabled()) {
                logger.error("Can not get current node when try to cancel workflow");
            }
        }

    }

    private void updateLogDocument(final NodeRef nodeRef, final LogRecord logRecord) {

        logRecord
                .setDocumentID((Long) getNodeService().getProperty(nodeRef, ContentModel.PROP_NODE_DBID));
        final NodeRef igNodeRef = getManagementService().getCurrentInterestGroup(nodeRef);
        if (igNodeRef != null) {

            logRecord
                    .setIgID((Long) getNodeService().getProperty(igNodeRef, ContentModel.PROP_NODE_DBID));
            logRecord.setIgName((String) getNodeService().getProperty(igNodeRef, ContentModel.PROP_NAME));
        } else {
            final NodeRef categoryNodeRef = getManagementService().getCurrentCategory(nodeRef);
            if (categoryNodeRef != null) {
                logRecord.setIgID(
                        (Long) getNodeService().getProperty(categoryNodeRef, ContentModel.PROP_NODE_DBID));
                logRecord.setIgName(
                        (String) getNodeService().getProperty(categoryNodeRef, ContentModel.PROP_NAME));
            } else {
                final NodeRef circabcNodeRef = getManagementService().getCircabcNodeRef();
                logRecord.setIgID(
                        (Long) getNodeService().getProperty(circabcNodeRef, ContentModel.PROP_NODE_DBID));
                logRecord.setIgName(
                        (String) getNodeService().getProperty(circabcNodeRef, ContentModel.PROP_NAME));

            }
        }
        logRecord.setUser(AuthenticationUtil.getFullyAuthenticatedUser());
        final Path path;
        if (getNodeService().getType(nodeRef).equals(ContentModel.TYPE_MULTILINGUAL_CONTAINER)) {
            //tempNodeRef = getMultilingualContentService().getPivotTranslation(nodeRef);
            final NodeRef tempNodeRef = getMultilingualContentService().getPivotTranslation(nodeRef);
            path = getNodeService().getPath(tempNodeRef);

        } else {
            path = getNodeService().getPath(nodeRef);
        }

        logRecord.setPath(PathUtils.getCircabcPath(path, true));
    }

    @Override
    public SelectItem[] pickerCallback(int filterIndex, String contains) {

        FacesContext context = FacesContext.getCurrentInstance();
        String safeContains = null;
        if (contains != null && contains.length() > 0) {
            safeContains = Utils.remove(contains.trim(), "\"");
            safeContains = safeContains.toLowerCase();
        }

        SelectItem[] items = new SelectItem[0];
        UserTransaction tx = null;
        try {

            tx = Repository.getUserTransaction(context, true);
            tx.begin();

            Set<String> invitedUsers = igRootProfileManagerService.getInvitedUsersProfiles(igNodeRef)
                    .keySet();
            PersonService personService = (PersonService) FacesContextUtils
                    .getRequiredWebApplicationContext(context).getBean(
                            "PersonService");

            NodeService nodeService = (NodeService) FacesContextUtils
                    .getRequiredWebApplicationContext(context).getBean(
                            "NodeService");

            ArrayList<SelectItem> itemList = new ArrayList<>(
                    invitedUsers.size());
            for (String user : invitedUsers) {
                if (user.equalsIgnoreCase(owner)) {
                    continue;
                }

                NodeRef person = personService.getPerson(user);
                String fullNameAndUserId = User.getFullNameAndUserId(
                        nodeService, person);

                if (safeContains != null) {
                    if (fullNameAndUserId.toLowerCase().contains(safeContains)) {

                        String firstName = (String) this.getNodeService()
                                .getProperty(person,
                                        ContentModel.PROP_FIRSTNAME);
                        String lastName = (String) this
                                .getNodeService()
                                .getProperty(person, ContentModel.PROP_LASTNAME);
                        String name = (firstName != null ? firstName : "")
                                + ' ' + (lastName != null ? lastName : "");
                        SelectItem item = new SortableSelectItem(user, name
                                + " [" + user + "]",
                                lastName != null ? lastName : user);
                        itemList.add(item);

                    }
                } else {
                    String firstName = (String) this.getNodeService()
                            .getProperty(person, ContentModel.PROP_FIRSTNAME);
                    String lastName = (String) this.getNodeService()
                            .getProperty(person, ContentModel.PROP_LASTNAME);
                    String name = (firstName != null ? firstName : "") + ' '
                            + (lastName != null ? lastName : "");
                    SelectItem item = new SortableSelectItem(user, name + " ["
                            + user + "]", lastName != null ? lastName : user);
                    itemList.add(item);
                }

            }

            // commit the transaction
            tx.commit();

            items = new SelectItem[itemList.size()];
            itemList.toArray(items);
            Arrays.sort(items);

        } catch (Throwable err) {
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception tex) {
            }
        }
        return items;
    }

    /**
     * Action handler called when the Add button is pressed to process the current selection
     */
    public void addSelection(final ActionEvent event) {
        final UIGenericPicker picker = (UIGenericPicker) event.getComponent()
                .findComponent("picker");

        final String[] results = picker.getSelectedResults();
        if (results == null) {
            Utils.addErrorMessage(translate(PLEASE_SELECT_USER));
        } else {
            userName = picker.getSelectedResults()[0];
            Utils.addStatusMessage(FacesMessage.SEVERITY_INFO,
                    translate(PLEASE_CLICK_OK_TO_REASSIGN_USER));

        }

    }

    protected final String translate(final String key, final Object... params) {
        return WebClientHelper.translate(key, params);
    }

    public boolean getFinishButtonDisabled() {
        return (userName == null);
    }

    @Override
    public String getPageIconAltText() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getBrowserTitle() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ActionsListWrapper getActionList() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isCancelButtonVisible() {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean isFormProvided() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome)
            throws Exception {

        if (userName != null) {
            Map<QName, Serializable> params = new HashMap<>(
                    1);
            params.put(ContentModel.PROP_OWNER, userName);
            this.getWorkflowService().updateTask(this.taskId, params, null,
                    null);
            logRecord.addInfo("Reassign task id : ");
            logRecord.addInfo(this.taskId);
            logRecord.addInfo(" to user : ");
            logRecord.addInfo(userName);
        } else {
            Utils.addErrorMessage(translate(PLEASE_SELECT_USER));
            return null;
        }
        return outcome;
    }

    @Override
    protected String doPostCommitProcessing(FacesContext context, String outcome) {
        logRecord.setOK(true);
        getLogService().log(logRecord);
        return super.doPostCommitProcessing(context, outcome);
    }

    protected String getErrorOutcome(Throwable exception) {
        logRecord.setOK(false);
        getLogService().log(logRecord);
        return super.getErrorOutcome(exception);

    }


    /**
     * @return the logService
     */
    protected final LogService getLogService() {
        if (logService == null) {
            logService = Services.getCircabcServiceRegistry(FacesContext.getCurrentInstance())
                    .getLogService();
        }
        return logService;
    }

    /**
     * @param logService the logService to set
     */
    public final void setLogService(final LogService logService) {
        this.logService = logService;
    }

    /**
     * @return the ManagementService
     */
    protected final ManagementService getManagementService() {
        if (managementService == null) {
            managementService = Services.getCircabcServiceRegistry(FacesContext.getCurrentInstance())
                    .getManagementService();
        }
        return managementService;
    }

    /**
     * @return the multilingualContentService
     */
    public MultilingualContentService getMultilingualContentService() {
        if (multilingualContentService == null) {
            multilingualContentService = Services
                    .getAlfrescoServiceRegistry(FacesContext.getCurrentInstance())
                    .getMultilingualContentService();
        }
        return multilingualContentService;
    }


}
