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
import eu.cec.digit.circabc.web.Beans;
import eu.cec.digit.circabc.web.bean.override.CircabcBrowseBean;
import eu.cec.digit.circabc.web.wai.dialog.WaiDialog;
import eu.cec.digit.circabc.web.wai.manager.ActionsListWrapper;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.cmr.workflow.WorkflowTransition;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.repository.MapNode;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.workflow.ManageTaskDialog;
import org.alfresco.web.bean.workflow.WorkflowUtil;
import org.alfresco.web.ui.common.Utils;

import javax.faces.context.FacesContext;
import javax.transaction.UserTransaction;
import java.io.Serializable;
import java.util.Map;

public class CircabcManageTaskDialog extends ManageTaskDialog implements
        WaiDialog {

    protected static final String CLIENT_ID_PREFIX =
            "FormPrincipal" + AlfrescoNavigationHandler.OUTCOME_SEPARATOR + ID_PREFIX;
    /**
     *
     */
    private static final long serialVersionUID = 7269656716977726537L;
    protected CircabcBrowseBean circabcBrowseBean;
    private String interestGroupNodeRef;

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);

        if (super.workflowPackage != null) {
            if (getNodeService()
                    .hasAspect(super.workflowPackage, CircabcModel.ASPECT_BELONG_TO_INTEREST_GROUP)) {
                interestGroupNodeRef = ((NodeRef) getNodeService()
                        .getProperty(workflowPackage, CircabcModel.PROP_INTEREST_GROUP_NODE_REF)).toString();
            }
        }


    }

    @SuppressWarnings("deprecation")
    @Override
    public String transition() {
        String outcome = getDefaultFinishOutcome();

        // before transitioning check the task still exists and is not completed
        FacesContext context = FacesContext.getCurrentInstance();
        WorkflowTask checkTask = this.getWorkflowService().getTaskById(this.getWorkflowTask().id);
        if (checkTask == null || checkTask.state == WorkflowTaskState.COMPLETED) {
            Utils.addErrorMessage(Application.getMessage(context, "invalid_task"));
            return outcome;
        }

        // to find out which transition button was pressed we need
        // to look for the button's id in the request parameters,
        // the first non-null result is the button that was pressed.
        Map<?, ?> reqParams = context.getExternalContext().getRequestParameterMap();

        String selectedTransition = null;
        for (WorkflowTransition trans : this.getWorkflowTransitions()) {
            Object result = reqParams.get(CLIENT_ID_PREFIX + FacesHelper.makeLegalId(trans.title));
            if (result != null) {
                // this was the button that was pressed
                selectedTransition = trans.id;
                break;
            }
        }

        UserTransaction tx = null;

        try {
            tx = Repository.getUserTransaction(context);
            tx.begin();

            // prepare the edited parameters for saving
            Map<QName, Serializable> params = WorkflowUtil.prepareTaskParams(this.taskNode);

            // update the task with the updated parameters and resources
            updateResources();
            this.getWorkflowService().updateTask(this.getWorkflowTask().id, params, null, null);

            // signal the selected transition to the workflow task
            this.getWorkflowService().endTask(this.getWorkflowTask().id, selectedTransition);

            // commit the changes
            tx.commit();

        } catch (Throwable e) {
            // rollback the transaction
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Exception ex) {
            }
            Utils.addErrorMessage(formatErrorMessage(e), e);
            outcome = this.getErrorOutcome(e);
        }

        return outcome;
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
    public String getCancelButtonLabel() {
        return Application.getMessage(FacesContext.getCurrentInstance(), "close");
    }

    public String getInterestGroupNodeRef() {
        return interestGroupNodeRef;
    }

    public void setInterestGroupNodeRef(String interestGroupNodeRef) {
        this.interestGroupNodeRef = interestGroupNodeRef;
    }

    @Override
    protected void createAndAddNode(NodeRef nodeRef) {
        if (!nodeExistsInResources(nodeRef)) {
            // create our Node representation
            MapNode node = new MapNode(nodeRef, this.getNodeService(), true);
            this.browseBean.setupCommonBindingProperties(node);
            node.addPropertyResolver("displayPath", getCircabcBrowseBean().resolverSimpleDisplayPath);
            node.addPropertyResolver("displayLigthPath",
                    getCircabcBrowseBean().resolverSimpleDisplayPathLight);

            // add a property resolver to indicate whether the item has been
            // completed or not
            // node.addPropertyResolver("completed", this.completeResolver);

            // add the id of the task being managed
            node.getProperties().put("taskId", this.getWorkflowTask().id);

            this.resources.add(node);
        }
    }

    private boolean nodeExistsInResources(NodeRef nodeRef) {
        boolean result = false;
        for (Node resource : this.resources) {
            if (resource.getNodeRef().equals(nodeRef)) {
                result = true;
                break;
            }
        }
        return result;
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
}
