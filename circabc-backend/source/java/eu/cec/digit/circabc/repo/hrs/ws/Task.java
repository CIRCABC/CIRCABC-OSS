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
 * <p>Task.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 * <p>
 * Task.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
/**
 * Task.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package eu.cec.digit.circabc.repo.hrs.ws;

/** A workflow task */
public abstract class Task implements java.io.Serializable {

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
            new org.apache.axis.description.TypeDesc(Task.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "Task"));
        org.apache.axis.description.ElementDesc elemField =
                new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("taskId");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "taskId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("workflowId");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "workflowId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("status");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "status"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", ">Task>status"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("assignee");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "assignee"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "WorkflowUser"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("sender");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "sender"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "WorkflowUser"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("sentDate");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "sentDate"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "date"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("closer");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "closer"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "WorkflowUser"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("closeDate");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "closeDate"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "date"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("reader");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "reader"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "WorkflowUser"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("readDate");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "readDate"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "date"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("deadlineDate");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "deadlineDate"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "date"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("instructions");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "instructions"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("comments");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "comments"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("critical");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "critical"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("deleted");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "deleted"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /* Internal ID to uniquely identify a workflow task */
    private int taskId;
    /* Internal ID of the workflow to which the task belongs */
    private int workflowId;
    /* Current state of the workflow task */
    private eu.cec.digit.circabc.repo.hrs.ws.TaskStatus status;
    /* User to whom the task is assigned */
    private eu.cec.digit.circabc.repo.hrs.ws.WorkflowUser assignee;
    /* User that sent the task to the assignee */
    private eu.cec.digit.circabc.repo.hrs.ws.WorkflowUser sender;
    /* Day on which the task was sent to the assignee */
    private java.util.Date sentDate;
    /* User that closed the task */
    private eu.cec.digit.circabc.repo.hrs.ws.WorkflowUser closer;
    /* Day on which the task was closed */
    private java.util.Date closeDate;
    /* User that has read the task first */
    private eu.cec.digit.circabc.repo.hrs.ws.WorkflowUser reader;
    /* Day on which the task was first read */
    private java.util.Date readDate;
    /* Deadline day for this task, it should be closed by then */
    private java.util.Date deadlineDate;
    /* Instructions given by the sender of the task */
    private java.lang.String instructions;
    /* Comments given by the user that closed the task */
    private java.lang.String comments;
    /* Indicates if the task is critical */
    private boolean critical;
    /* Indicates that the task has been marked as deleted */
    private java.lang.Boolean deleted;
    private java.lang.Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;

    public Task() {
    }

    public Task(
            int taskId,
            int workflowId,
            eu.cec.digit.circabc.repo.hrs.ws.TaskStatus status,
            eu.cec.digit.circabc.repo.hrs.ws.WorkflowUser assignee,
            eu.cec.digit.circabc.repo.hrs.ws.WorkflowUser sender,
            java.util.Date sentDate,
            eu.cec.digit.circabc.repo.hrs.ws.WorkflowUser closer,
            java.util.Date closeDate,
            eu.cec.digit.circabc.repo.hrs.ws.WorkflowUser reader,
            java.util.Date readDate,
            java.util.Date deadlineDate,
            java.lang.String instructions,
            java.lang.String comments,
            boolean critical,
            java.lang.Boolean deleted) {
        this.taskId = taskId;
        this.workflowId = workflowId;
        this.status = status;
        this.assignee = assignee;
        this.sender = sender;
        this.sentDate = sentDate;
        this.closer = closer;
        this.closeDate = closeDate;
        this.reader = reader;
        this.readDate = readDate;
        this.deadlineDate = deadlineDate;
        this.instructions = instructions;
        this.comments = comments;
        this.critical = critical;
        this.deleted = deleted;
    }

    /** Return type metadata object */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /** Get Custom Serializer */
    public static org.apache.axis.encoding.Serializer getSerializer(
            java.lang.String mechType, java.lang.Class _javaType, javax.xml.namespace.QName _xmlType) {
        return new org.apache.axis.encoding.ser.BeanSerializer(_javaType, _xmlType, typeDesc);
    }

    /** Get Custom Deserializer */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
            java.lang.String mechType, java.lang.Class _javaType, javax.xml.namespace.QName _xmlType) {
        return new org.apache.axis.encoding.ser.BeanDeserializer(_javaType, _xmlType, typeDesc);
    }

    /**
     * Gets the taskId value for this Task.
     *
     * @return taskId * Internal ID to uniquely identify a workflow task
     */
    public int getTaskId() {
        return taskId;
    }

    /**
     * Sets the taskId value for this Task.
     *
     * @param taskId * Internal ID to uniquely identify a workflow task
     */
    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    /**
     * Gets the workflowId value for this Task.
     *
     * @return workflowId * Internal ID of the workflow to which the task belongs
     */
    public int getWorkflowId() {
        return workflowId;
    }

    /**
     * Sets the workflowId value for this Task.
     *
     * @param workflowId * Internal ID of the workflow to which the task belongs
     */
    public void setWorkflowId(int workflowId) {
        this.workflowId = workflowId;
    }

    /**
     * Gets the status value for this Task.
     *
     * @return status * Current state of the workflow task
     */
    public eu.cec.digit.circabc.repo.hrs.ws.TaskStatus getStatus() {
        return status;
    }

    /**
     * Sets the status value for this Task.
     *
     * @param status * Current state of the workflow task
     */
    public void setStatus(eu.cec.digit.circabc.repo.hrs.ws.TaskStatus status) {
        this.status = status;
    }

    /**
     * Gets the assignee value for this Task.
     *
     * @return assignee * User to whom the task is assigned
     */
    public eu.cec.digit.circabc.repo.hrs.ws.WorkflowUser getAssignee() {
        return assignee;
    }

    /**
     * Sets the assignee value for this Task.
     *
     * @param assignee * User to whom the task is assigned
     */
    public void setAssignee(eu.cec.digit.circabc.repo.hrs.ws.WorkflowUser assignee) {
        this.assignee = assignee;
    }

    /**
     * Gets the sender value for this Task.
     *
     * @return sender * User that sent the task to the assignee
     */
    public eu.cec.digit.circabc.repo.hrs.ws.WorkflowUser getSender() {
        return sender;
    }

    /**
     * Sets the sender value for this Task.
     *
     * @param sender * User that sent the task to the assignee
     */
    public void setSender(eu.cec.digit.circabc.repo.hrs.ws.WorkflowUser sender) {
        this.sender = sender;
    }

    /**
     * Gets the sentDate value for this Task.
     *
     * @return sentDate * Day on which the task was sent to the assignee
     */
    public java.util.Date getSentDate() {
        return sentDate;
    }

    /**
     * Sets the sentDate value for this Task.
     *
     * @param sentDate * Day on which the task was sent to the assignee
     */
    public void setSentDate(java.util.Date sentDate) {
        this.sentDate = sentDate;
    }

    /**
     * Gets the closer value for this Task.
     *
     * @return closer * User that closed the task
     */
    public eu.cec.digit.circabc.repo.hrs.ws.WorkflowUser getCloser() {
        return closer;
    }

    /**
     * Sets the closer value for this Task.
     *
     * @param closer * User that closed the task
     */
    public void setCloser(eu.cec.digit.circabc.repo.hrs.ws.WorkflowUser closer) {
        this.closer = closer;
    }

    /**
     * Gets the closeDate value for this Task.
     *
     * @return closeDate * Day on which the task was closed
     */
    public java.util.Date getCloseDate() {
        return closeDate;
    }

    /**
     * Sets the closeDate value for this Task.
     *
     * @param closeDate * Day on which the task was closed
     */
    public void setCloseDate(java.util.Date closeDate) {
        this.closeDate = closeDate;
    }

    /**
     * Gets the reader value for this Task.
     *
     * @return reader * User that has read the task first
     */
    public eu.cec.digit.circabc.repo.hrs.ws.WorkflowUser getReader() {
        return reader;
    }

    /**
     * Sets the reader value for this Task.
     *
     * @param reader * User that has read the task first
     */
    public void setReader(eu.cec.digit.circabc.repo.hrs.ws.WorkflowUser reader) {
        this.reader = reader;
    }

    /**
     * Gets the readDate value for this Task.
     *
     * @return readDate * Day on which the task was first read
     */
    public java.util.Date getReadDate() {
        return readDate;
    }

    /**
     * Sets the readDate value for this Task.
     *
     * @param readDate * Day on which the task was first read
     */
    public void setReadDate(java.util.Date readDate) {
        this.readDate = readDate;
    }

    /**
     * Gets the deadlineDate value for this Task.
     *
     * @return deadlineDate * Deadline day for this task, it should be closed by then
     */
    public java.util.Date getDeadlineDate() {
        return deadlineDate;
    }

    /**
     * Sets the deadlineDate value for this Task.
     *
     * @param deadlineDate * Deadline day for this task, it should be closed by then
     */
    public void setDeadlineDate(java.util.Date deadlineDate) {
        this.deadlineDate = deadlineDate;
    }

    /**
     * Gets the instructions value for this Task.
     *
     * @return instructions * Instructions given by the sender of the task
     */
    public java.lang.String getInstructions() {
        return instructions;
    }

    /**
     * Sets the instructions value for this Task.
     *
     * @param instructions * Instructions given by the sender of the task
     */
    public void setInstructions(java.lang.String instructions) {
        this.instructions = instructions;
    }

    /**
     * Gets the comments value for this Task.
     *
     * @return comments * Comments given by the user that closed the task
     */
    public java.lang.String getComments() {
        return comments;
    }

    /**
     * Sets the comments value for this Task.
     *
     * @param comments * Comments given by the user that closed the task
     */
    public void setComments(java.lang.String comments) {
        this.comments = comments;
    }

    /**
     * Gets the critical value for this Task.
     *
     * @return critical * Indicates if the task is critical
     */
    public boolean isCritical() {
        return critical;
    }

    /**
     * Sets the critical value for this Task.
     *
     * @param critical * Indicates if the task is critical
     */
    public void setCritical(boolean critical) {
        this.critical = critical;
    }

    /**
     * Gets the deleted value for this Task.
     *
     * @return deleted * Indicates that the task has been marked as deleted
     */
    public java.lang.Boolean getDeleted() {
        return deleted;
    }

    /**
     * Sets the deleted value for this Task.
     *
     * @param deleted * Indicates that the task has been marked as deleted
     */
    public void setDeleted(java.lang.Boolean deleted) {
        this.deleted = deleted;
    }

    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Task)) {
            return false;
        }
        Task other = (Task) obj;
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals =
                true
                        && this.taskId == other.getTaskId()
                        && this.workflowId == other.getWorkflowId()
                        && ((this.status == null && other.getStatus() == null)
                        || (this.status != null && this.status.equals(other.getStatus())))
                        && ((this.assignee == null && other.getAssignee() == null)
                        || (this.assignee != null && this.assignee.equals(other.getAssignee())))
                        && ((this.sender == null && other.getSender() == null)
                        || (this.sender != null && this.sender.equals(other.getSender())))
                        && ((this.sentDate == null && other.getSentDate() == null)
                        || (this.sentDate != null && this.sentDate.equals(other.getSentDate())))
                        && ((this.closer == null && other.getCloser() == null)
                        || (this.closer != null && this.closer.equals(other.getCloser())))
                        && ((this.closeDate == null && other.getCloseDate() == null)
                        || (this.closeDate != null && this.closeDate.equals(other.getCloseDate())))
                        && ((this.reader == null && other.getReader() == null)
                        || (this.reader != null && this.reader.equals(other.getReader())))
                        && ((this.readDate == null && other.getReadDate() == null)
                        || (this.readDate != null && this.readDate.equals(other.getReadDate())))
                        && ((this.deadlineDate == null && other.getDeadlineDate() == null)
                        || (this.deadlineDate != null && this.deadlineDate.equals(other.getDeadlineDate())))
                        && ((this.instructions == null && other.getInstructions() == null)
                        || (this.instructions != null && this.instructions.equals(other.getInstructions())))
                        && ((this.comments == null && other.getComments() == null)
                        || (this.comments != null && this.comments.equals(other.getComments())))
                        && this.critical == other.isCritical()
                        && ((this.deleted == null && other.getDeleted() == null)
                        || (this.deleted != null && this.deleted.equals(other.getDeleted())));
        __equalsCalc = null;
        return _equals;
    }

    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        _hashCode += getTaskId();
        _hashCode += getWorkflowId();
        if (getStatus() != null) {
            _hashCode += getStatus().hashCode();
        }
        if (getAssignee() != null) {
            _hashCode += getAssignee().hashCode();
        }
        if (getSender() != null) {
            _hashCode += getSender().hashCode();
        }
        if (getSentDate() != null) {
            _hashCode += getSentDate().hashCode();
        }
        if (getCloser() != null) {
            _hashCode += getCloser().hashCode();
        }
        if (getCloseDate() != null) {
            _hashCode += getCloseDate().hashCode();
        }
        if (getReader() != null) {
            _hashCode += getReader().hashCode();
        }
        if (getReadDate() != null) {
            _hashCode += getReadDate().hashCode();
        }
        if (getDeadlineDate() != null) {
            _hashCode += getDeadlineDate().hashCode();
        }
        if (getInstructions() != null) {
            _hashCode += getInstructions().hashCode();
        }
        if (getComments() != null) {
            _hashCode += getComments().hashCode();
        }
        _hashCode += (isCritical() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        if (getDeleted() != null) {
            _hashCode += getDeleted().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }
}
