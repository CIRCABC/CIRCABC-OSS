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
package eu.cec.digit.circabc.web.wai.dialog.ml;

import eu.cec.digit.circabc.aspect.ContentNotifyAspect;
import eu.cec.digit.circabc.service.log.LogRecord;
import eu.cec.digit.circabc.service.log.LogService;
import eu.cec.digit.circabc.service.notification.NotifiableUser;
import eu.cec.digit.circabc.service.notification.NotificationService;
import eu.cec.digit.circabc.service.notification.NotificationSubscriptionService;
import eu.cec.digit.circabc.service.struct.ManagementService;
import eu.cec.digit.circabc.util.PathUtils;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.wai.dialog.WaiDialog;
import eu.cec.digit.circabc.web.wai.manager.ActionsListWrapper;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.context.FacesContext;
import javax.faces.model.DataModel;
import javax.faces.model.SelectItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Dialog bean to create a new edition
 *
 * @author Guillaume
 */
public class NewEditionDialog extends org.alfresco.web.bean.ml.NewEditionWizard implements
        WaiDialog {

    public static final String BEAN_NAME = "CircabcNewEditionDialog";
    private static final long serialVersionUID = -5372740078894071426L;
    private static Log logger = LogFactory.getLog(NewEditionDialog.class);
    protected LogRecord logRecord = new LogRecord();
    private transient LogService logService;
    private transient ManagementService managementService;
    private transient NotificationSubscriptionService notificationSubscriptionService;
    private transient NotificationService notificationService;
    private boolean disableNotification;

    private BehaviourFilter policyBehaviourFilter;


    /**
     * @see org.alfresco.web.bean.ml.NewEditionWizard#init(Map<String, String>)
     */
    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);

        // To get the new edition ref via browseBean.getDocument();
        setOtherProperties(true);
        disableNotification = true;
    }

    /**
     * Get the list of the translated documents availiable for the next pivot language
     *
     * @return List of translated documents availiable for the next pivot language
     */
    public SelectItem[] getTranslatedDocuments() {
        List<SelectItem> items = new ArrayList<>();

        DataModel dataModel = getAvailableTranslationsDataModel();
        @SuppressWarnings("unchecked")
        ArrayList<TranslationWrapper> list = (ArrayList<TranslationWrapper>) dataModel.getWrappedData();

        for (TranslationWrapper wrapper : list) {
            items.add(new SelectItem(wrapper.getLanguage(), wrapper.getName()));
        }

        return items.toArray(new SelectItem[items.size()]);
    }

    /**
     * Returns the properties for checked out translations JSF DataModel
     * <p>
     * We need to destroy the list alfreco has constructed
     *
     * @return List representing the translations checked out
     */
    public List getListTranslationsCheckedOutDataModel() {

        return (List) getTranslationsCheckedOutDataModel().getWrappedData();
    }

    /**
     * @see org.alfresco.web.bean.ml.NewEditionWizard#finishImpl(FacesContext, String)
     */
    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception {
        //required to ignore notification of "notify content", as it is not possible to know if it is a new edition on the notification service
        this.policyBehaviourFilter.disableBehaviour(ContentNotifyAspect.ASPECT_CONTENT_NOTIFY);

        outcome = super.finishImpl(context, outcome);

        this.navigator.setCurrentNodeId(this.browseBean.getDocument().getId());

        return AlfrescoNavigationHandler.CLOSE_DIALOG_OUTCOME;
    }

    /**
     * @see org.alfresco.web.bean.dialog.BaseDialogBean#doPostCommitProcessing(javax.faces.context.FacesContext,
     * java.lang.String)
     */
    @Override
    protected String doPostCommitProcessing(FacesContext context, String outcome) {
        if (outcome == null) {
            // Previous error -> redisplay
            this.isFinished = false;
            return null;
        }

        if (!disableNotification) {
            final Set<NotifiableUser> users = getNotificationSubscriptionService()
                    .getNotifiableUsers(this.browseBean.getDocument().getNodeRef());
            try {
                getNotificationService()
                        .notifyNewEdition(this.browseBean.getDocument().getNodeRef(), users);
            } catch (Exception e) {
                if (logger.isErrorEnabled()) {
                    logger.error("Error when notifying usere ", e);
                }
            }
        }

        // todo fix path if possible
        setLogRecord(this.browseBean.getDocument());
        logRecord.setService("Library");
        logRecord.setActivity("Create new edition");
        logRecord.setOK(true);
        logRecord.setInfo("Created a new version based on " + this.browseBean.getDocument().getName());
        getLogService().log(logRecord);

        // close the dialog
        return outcome;
    }

    /**
     * Indicate if the finish button must be disable or not
     *
     * @return false all the time
     */
    @Override
    public boolean getFinishButtonDisabled() {
        // Must be only active if we can do the work
        return (getHasTranslationCheckedOut());
    }

    public String getBrowserTitle() {
        return Application.getMessage(FacesContext.getCurrentInstance(), "title_new_edition");
    }

    public String getPageIconAltText() {
        return Application
                .getMessage(FacesContext.getCurrentInstance(), "title_new_edition_icon_tooltip");
    }

    public ActionsListWrapper getActionList() {
        return null;
    }

    public boolean isCancelButtonVisible() {
        return true;
    }

    public boolean isFormProvided() {
        return false;
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
    public final void setLogService(LogService logService) {
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

    private void setLogRecord(Node node) {
        logRecord.setDocumentID(
                (Long) getNodeService().getProperty(node.getNodeRef(), ContentModel.PROP_NODE_DBID));
        final NodeRef igNodeRef = getManagementService().getCurrentInterestGroup(node.getNodeRef());
        logRecord.setIgID((Long) getNodeService().getProperty(igNodeRef, ContentModel.PROP_NODE_DBID));
        logRecord.setIgName((String) getNodeService().getProperty(igNodeRef, ContentModel.PROP_NAME));
        logRecord.setUser(AuthenticationUtil.getFullyAuthenticatedUser());
        Path path = node.getNodePath();
        logRecord.setPath(PathUtils.getCircabcPath(path, true));
    }


    public BehaviourFilter getPolicyBehaviourFilter() {
        return policyBehaviourFilter;
    }

    public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
        this.policyBehaviourFilter = policyBehaviourFilter;
    }

    public boolean isDisableNotification() {
        return disableNotification;
    }

    public void setDisableNotification(boolean disableNotification) {
        this.disableNotification = disableNotification;
    }

    /**
     * @return the notificationSubscriptionService
     */
    public NotificationSubscriptionService getNotificationSubscriptionService() {
        return notificationSubscriptionService;
    }

    /**
     * @param notificationSubscriptionService the notificationSubscriptionService to set
     */
    public void setNotificationSubscriptionService(
            NotificationSubscriptionService notificationSubscriptionService) {
        this.notificationSubscriptionService = notificationSubscriptionService;
    }

    /**
     * @return the notificationService
     */
    public NotificationService getNotificationService() {
        return notificationService;
    }

    /**
     * @param notificationService the notificationService to set
     */
    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
}
