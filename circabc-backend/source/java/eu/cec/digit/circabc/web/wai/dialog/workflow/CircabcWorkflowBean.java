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
import eu.cec.digit.circabc.web.WebClientHelper;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNode;
import eu.cec.digit.circabc.web.bean.override.CircabcNavigationBean;
import eu.cec.digit.circabc.web.repository.CategoryNode;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import eu.cec.digit.circabc.web.wai.dialog.WaiDialog;
import org.alfresco.repo.workflow.BPMEngineRegistry;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.workflow.WorkflowBean;
import org.alfresco.web.ui.common.component.data.UIRichList;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class CircabcWorkflowBean extends BaseWaiDialog implements WaiDialog {

    /**
     *
     */
    private static final long serialVersionUID = -8787169968098627140L;

    private WorkflowBean workflowBean;

    private WorkflowService workflowService;

    private String engineId;

    private UIRichList packageItemsRichList;


    @Override
    public String getCancelButtonLabel() {
        return WebClientHelper.translate("close");
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
    protected String finishImpl(FacesContext context, String outcome)
            throws Throwable {
        // TODO Auto-generated method stub
        return null;
    }

    public WorkflowBean getWorkflowBean() {
        return workflowBean;
    }

    public void setWorkflowBean(WorkflowBean workflowBean) {
        this.workflowBean = workflowBean;
    }


    /**
     * Returns a list of nodes representing the "all" active tasks.
     *
     * @return List of all active tasks
     */
    public List<Node> getAllActiveTasks() {

        return workflowBean.getAllActiveTasks();
    }


    /**
     * Returns a list of nodes representing the "all" active tasks.
     *
     * @return List of interest group active tasks
     */
    public List<Node> getInterestGroupActiveTasks() {

        List<Node> allActiveTasks = workflowBean.getAllActiveTasks();
        List<Node> result = new ArrayList<>();

        CircabcNavigationBean navigator = Beans.getWaiNavigator();
        if (navigator != null) {
            NavigableNode currentIGRoot = navigator.getCurrentIGRoot();
            if (currentIGRoot != null) {
                for (Node node : allActiveTasks) {
                    final Long taskId = (Long) node.getProperties().get(
                            WorkflowModel.PROP_TASK_ID.toString());
                    final WorkflowTask task = this.getWorkflowService()
                            .getTaskById(BPMEngineRegistry.createGlobalId(engineId, taskId.toString()));
                    final NodeRef workflowPackage = (NodeRef) task.properties
                            .get(WorkflowModel.ASSOC_PACKAGE);
                    if (getNodeService().hasAspect(workflowPackage,
                            CircabcModel.ASPECT_BELONG_TO_INTEREST_GROUP)) {
                        NodeRef igNodeRef = (NodeRef) getNodeService()
                                .getProperty(
                                        workflowPackage,
                                        CircabcModel.PROP_INTEREST_GROUP_NODE_REF);
                        if (currentIGRoot.getNodeRef().equals(igNodeRef)) {
                            result.add(node);
                        }
                    }

                }
            }
        }
        return result;

    }

    /**
     * Returns a list of nodes representing the "category" active tasks.
     *
     * @return List of interest group active tasks
     */
    public List<Node> getCategoryActiveTasks() {

        final CategoryNode cat = (CategoryNode) Beans.getWaiNavigator()
                .getCurrentCategory();
        List<Node> result = new ArrayList<>();
        if (cat != null) {
            List<Node> allActiveTasks = workflowBean.getAllActiveTasks();
            List<NodeRef> interestGroups = cat.getManagementService()
                    .getInterestGroups(cat.getNodeRef());

            CircabcNavigationBean navigator = Beans.getWaiNavigator();
            if (navigator != null) {
                for (Node node : allActiveTasks) {
                    final Long taskId = (Long) node.getProperties().get(
                            WorkflowModel.PROP_TASK_ID.toString());
                    final WorkflowTask task = this.getWorkflowService()
                            .getTaskById(
                                    BPMEngineRegistry.createGlobalId(engineId,
                                            taskId.toString()));
                    final NodeRef workflowPackage = (NodeRef) task.properties
                            .get(WorkflowModel.ASSOC_PACKAGE);
                    if (getNodeService().hasAspect(workflowPackage,
                            CircabcModel.ASPECT_BELONG_TO_INTEREST_GROUP)) {
                        NodeRef igNodeRef = (NodeRef) getNodeService()
                                .getProperty(
                                        workflowPackage,
                                        CircabcModel.PROP_INTEREST_GROUP_NODE_REF);
                        if (interestGroups.contains(igNodeRef)) {
                            result.add(node);
                        }

                    }
                }
            }
        }
        return result;

    }


    private WorkflowService getWorkflowService() {
        if (this.workflowService == null) {
            this.workflowService = Repository.getServiceRegistry(FacesContext.getCurrentInstance())
                    .getWorkflowService();
        }
        return this.workflowService;
    }


    /**
     * Returns a list of nodes representing the "pooled" to do tasks the current user has.
     *
     * @return List of to do tasks
     */
    public List<Node> getPooledTasks() {

        return workflowBean.getPooledTasks();
    }

    /**
     * Returns a list of nodes representing the to do tasks the current user has.
     *
     * @return List of to do tasks
     */
    public List<Node> getTasksToDo() {
        return workflowBean.getTasksToDo();
    }

    /**
     * Returns a list of nodes representing the completed tasks the current user has.
     *
     * @return List of completed tasks
     */
    public List<Node> getTasksCompleted() {
        return workflowBean.getTasksCompleted();
    }

    public void setupTaskDialog(ActionEvent event) {
        workflowBean.setupTaskDialog(event);
    }

    public void setupTaskDialog(String id, String type) {
        workflowBean.setupTaskDialog(id, type);
    }


    public String getEngineId() {
        return engineId;
    }


    public void setEngineId(String engineId) {
        this.engineId = engineId;
    }


    public UIRichList getPackageItemsRichList() {
        return packageItemsRichList;
    }


    public void setPackageItemsRichList(UIRichList packageItemsRichList) {
        this.packageItemsRichList = packageItemsRichList;
    }

}
