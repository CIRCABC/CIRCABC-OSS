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
package eu.cec.digit.circabc.web.wai.wizard.workflow;

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.service.log.LogRecord;
import eu.cec.digit.circabc.service.log.LogService;
import eu.cec.digit.circabc.service.struct.ManagementService;
import eu.cec.digit.circabc.util.PathUtils;
import eu.cec.digit.circabc.web.Beans;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNode;
import eu.cec.digit.circabc.web.bean.override.CircabcBrowseBean;
import eu.cec.digit.circabc.web.bean.override.CircabcNavigationBean;
import eu.cec.digit.circabc.web.wai.manager.ActionsListWrapper;
import eu.cec.digit.circabc.web.wai.wizard.WaiWizard;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.MapNode;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.workflow.StartWorkflowWizard;
import org.alfresco.web.bean.workflow.WorkflowUtil;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.transaction.UserTransaction;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CircabcStartWorkflowWizard extends StartWorkflowWizard implements WaiWizard {

    /**
     *
     */
    private static final long serialVersionUID = -8762878935240097115L;
    private static final Log logger = LogFactory.getLog(CircabcStartWorkflowWizard.class);
    protected CircabcBrowseBean circabcBrowseBean;
    private NodeRef workflowPackage;
    private transient LogService logService;
    private LogRecord logRecord;
    private transient ManagementService managementService;
    private transient MultilingualContentService multilingualContentService;


    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);
        logRecord = new LogRecord();
        CircabcNavigationBean navigator = Beans.getWaiNavigator();
        if (navigator != null) {
            updateLogDocument(navigator.getCurrentNode().getNodeRef(), logRecord);
            logRecord.setService("Workflow");
            logRecord.setActivity("Start");
        } else {
            if (logger.isErrorEnabled()) {
                logger.error("Can not get current node when try to start workflow");
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
        return false;
    }

    @Override
    public boolean isFormProvided() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    protected String doPostCommitProcessing(FacesContext context, String outcome) {
        CircabcNavigationBean navigator = Beans.getWaiNavigator();
        if (navigator != null) {
            NavigableNode currentIGRoot = navigator.getCurrentIGRoot();
            if ((currentIGRoot != null)
                    && (!getNodeService().hasAspect(workflowPackage,
                    CircabcModel.ASPECT_BELONG_TO_INTEREST_GROUP))) {

                getNodeService().addAspect(workflowPackage,
                        CircabcModel.ASPECT_BELONG_TO_INTEREST_GROUP, null);
                getNodeService().setProperty(workflowPackage,
                        CircabcModel.PROP_INTEREST_GROUP_NODE_REF,
                        currentIGRoot.getNodeRef());
            }
        }
        logRecord.setOK(true);
        getLogService().log(logRecord);
        return super.doPostCommitProcessing(context, outcome);
    }

    protected String getErrorOutcome(Throwable exception) {
        logRecord.setOK(false);
        getLogService().log(logRecord);
        return super.getErrorOutcome(exception);

    }

    // same as alfresco  method 	just change  workflowPackage to be field
    // so we can add interest group where workflow belongs
    @Override
    protected String finishImpl(FacesContext context, String outcome)
            throws Exception {
        // TODO: Deal with workflows that don't require any data

        if (logger.isDebugEnabled()) {
            logger.debug("Starting workflow: " + this.selectedWorkflow);
        }
        addLogRecoordInfo();

        // prepare the parameters from the current state of the property sheet
        Map<QName, Serializable> params = WorkflowUtil.prepareTaskParams(this.startTaskNode);

        if (logger.isDebugEnabled()) {
            logger.debug("Starting workflow with parameters: " + params);
        }

        // create a workflow package for the attached items and add them
        workflowPackage = this.getWorkflowService().createPackage(null);
        params.put(WorkflowModel.ASSOC_PACKAGE, workflowPackage);

        for (String addedItem : this.packageItemsToAdd) {
            NodeRef addedNodeRef = new NodeRef(addedItem);
            this.getUnprotectedNodeService().addChild(workflowPackage, addedNodeRef,
                    WorkflowModel.ASSOC_PACKAGE_CONTAINS,
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
                            QName.createValidLocalName((String) this.getNodeService().getProperty(
                                    addedNodeRef, ContentModel.PROP_NAME))));
        }

        // setup the context for the workflow (this is the space the workflow was launched from)
        Node workflowContext = this.navigator.getCurrentNode();
        if (workflowContext != null) {
            params.put(WorkflowModel.PROP_CONTEXT, (Serializable) workflowContext.getNodeRef());
        }

        // start the workflow to get access to the start task
        WorkflowPath path = this.getWorkflowService().startWorkflow(this.selectedWorkflow, params);
        if (path != null) {
            // extract the start task
            List<WorkflowTask> tasks = this.getWorkflowService().getTasksForWorkflowPath(path.id);
            if (tasks.size() == 1) {
                WorkflowTask startTask = tasks.get(0);

                if (logger.isDebugEnabled()) {
                    logger.debug("Found start task:" + startTask);
                }

                if (startTask.state == WorkflowTaskState.IN_PROGRESS) {
                    // end the start task to trigger the first 'proper'
                    // task in the workflow
                    this.getWorkflowService().endTask(startTask.id, null);
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Started workflow: " + this.selectedWorkflow);
            }
        }

        return outcome;
    }

    private void addLogRecoordInfo() {
        logRecord.addInfo(this.selectedWorkflow);
        for (SelectItem item : availableWorkflows) {
            if (item.getValue().equals(selectedWorkflow)) {
                logRecord.addInfo(":");
                logRecord.addInfo(item.getLabel());
            }
        }
    }


    @Override
    public List<Node> getResources() {
        this.resources = new ArrayList<>(4);

        UserTransaction tx = null;
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            tx = Repository.getUserTransaction(context, true);
            tx.begin();

            for (String newItem : this.packageItemsToAdd) {
                NodeRef nodeRef = new NodeRef(newItem);
                if (this.getNodeService().exists(nodeRef)) {
                    // create our Node representation
                    MapNode node = new MapNode(nodeRef, this.getNodeService(), true);
                    this.browseBean.setupCommonBindingProperties(node);

                    node.addPropertyResolver("displayPath", getCircabcBrowseBean().resolverSimpleDisplayPath);
                    node.addPropertyResolver("displayLigthPath",
                            getCircabcBrowseBean().resolverSimpleDisplayPathLight);

                    this.resources.add(node);
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Ignoring " + nodeRef + " as it has been removed from the repository");
                    }
                }
            }

            // commit the transaction
            tx.commit();
        } catch (Throwable err) {
            Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
                    FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), err.getMessage()), err);
            this.resources = Collections.emptyList();
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception tex) {
            }
        }

        return this.resources;
    }

    /**
     * @return the browseBean
     */
    protected final CircabcBrowseBean getCircabcBrowseBean() {
        if (circabcBrowseBean == null) {
            circabcBrowseBean = Beans.getWaiBrowseBean();
        }
        return circabcBrowseBean;
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
