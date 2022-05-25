/**
 * Copyright 2006 European Community
 *
 * <p>Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 *
 * <p>https://joinup.ec.europa.eu/software/page/eupl
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * <p>WorkflowService_PortType.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 * <p>
 * WorkflowService_PortType.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
/**
 * WorkflowService_PortType.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package eu.cec.digit.circabc.repo.hrs.ws;

public interface WorkflowService_PortType extends java.rmi.Remote {

    /**
     * Adds assignments for a document. This creates an assignment workflow for the document if none
     * exists, or updates the existing assignment workflow by adding the specified tasks.
     */
    public eu.cec.digit.circabc.repo.hrs.ws.AssignmentWorkflow addAssignments(
            eu.cec.digit.circabc.repo.hrs.ws.Header header,
            eu.cec.digit.circabc.repo.hrs.ws.AddAssignmentsRequest request)
            throws java.rmi.RemoteException;

    /**
     * Creates a signatory workflow (either paper or eSignatory) for an unregistered document. A
     * document can have at most one signatory workflow. If a signatory workflow of the same type or
     * of a different type already exists, a failure is raised. <br>
     * If the workflow is an eSignatory, it has to be launched for it to become active. Once launched,
     * tasks execute (i.e. they become active and can be closed) in the order they have been added to
     * the workflow. At any moment, there will be exactly one active task in a launched eSignatory
     * workflow, until all tasks have been executed. <br>
     * This does not apply to paper signatories, which don't have the concept of being launched and
     * don't have active tasks (i.e. tasks are closed manually).<br>
     * After closing the last task in an eSignatory, it is recommended to perform either a
     * registration of the document, a filing or a trashing (because conceptually when the eSignatory
     * workflow finishes, the document is complete), so that documents don't remain unfiled and
     * unregistered.
     */
    public eu.cec.digit.circabc.repo.hrs.ws.SignatoryWorkflow createSignatory(
            eu.cec.digit.circabc.repo.hrs.ws.Header header,
            eu.cec.digit.circabc.repo.hrs.ws.CreateSignatoryRequest request)
            throws java.rmi.RemoteException;

    /**
     * Appends tasks to a document's signatory workflow (paper or eSignatory). <br>
     * Signatory tasks can only be added to unregistered documents. Once a document is registered, all
     * signatory tasks are automatically closed and no new signatory tasks may be added.<br>
     * If the document has no signatory, a failure is raised. <br>
     */
    public eu.cec.digit.circabc.repo.hrs.ws.SignatoryWorkflow addSignatoryTasks(
            eu.cec.digit.circabc.repo.hrs.ws.Header header,
            eu.cec.digit.circabc.repo.hrs.ws.AddSignatoryTasksRequest request)
            throws java.rmi.RemoteException;

    /**
     * Launches the eSignatory workflow of an unregistered document. After launching, the first task
     * in the workflow becomes active. Only eSignatories can (and need) to be launched, paper
     * signatories don't support this concept. <br>
     * If the eSignatory is already launched or if the document has no eSignatory, or if the document
     * is registered a failure is raised .
     */
    public eu.cec.digit.circabc.repo.hrs.ws.SignatoryWorkflow launchESignatory(
            eu.cec.digit.circabc.repo.hrs.ws.Header header, java.lang.String documentId)
            throws java.rmi.RemoteException;

    /**
     * Closes an active task in an assignment or eSignatory workflow. A task can only be closed by its
     * assignee. <br>
     * In an eSignatory workflow, after closing the active task, the next task in the workflow will be
     * activated. <br>
     * If the task to be closed is the last one in an eSignatory workflow (i.e. the eSignatory
     * finishes), you should afterwards perform a registration, filing or trashing of the document,
     * because conceptually the work on the document has completed.
     */
    public boolean closeTask(
            eu.cec.digit.circabc.repo.hrs.ws.Header header,
            eu.cec.digit.circabc.repo.hrs.ws.CloseTaskRequest request)
            throws java.rmi.RemoteException;

    /**
     * This operation allows the user to bypass a task not completed in an e-Signatory. The bypass is
     * available for read tasks, not yet closed. The 'Bypass' is used, for example, when a task is
     * blocked by someone who is absent. This moves the task on to the next workflow participant.
     */
    public boolean bypassSignatoryTask(
            eu.cec.digit.circabc.repo.hrs.ws.Header header,
            eu.cec.digit.circabc.repo.hrs.ws.BypassSignatoryTaskRequest request)
            throws java.rmi.RemoteException;
}
