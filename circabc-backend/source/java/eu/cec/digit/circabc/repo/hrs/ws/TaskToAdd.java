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
 * <p>TaskToAdd.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 * <p>
 * TaskToAdd.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
/**
 * TaskToAdd.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package eu.cec.digit.circabc.repo.hrs.ws;

/** Workflow task that is added to the assignments or signatory workflow. */
public abstract class TaskToAdd implements java.io.Serializable {

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
            new org.apache.axis.description.TypeDesc(TaskToAdd.class, true);

    static {
        typeDesc.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "TaskToAdd"));
        org.apache.axis.description.ElementDesc elemField =
                new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("assigneeId");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "assigneeId"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("deadline");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "deadline"));
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
        elemField.setFieldName("critical");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "critical"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /* Current entity id of the internal person
     *                         that has this task assigned. Organizations
     * or
     *                         external persons are not valid assignees. */
    private java.lang.String assigneeId;
    /* An optional deadline for the task */
    private java.util.Date deadline;
    /* Optional instructions given by the sender
     *                         to the assignee regarding what has to be done
     * as
     *                         part of this task. */
    private java.lang.String instructions;
    /* Whether this is a critical task or not */
    private java.lang.Boolean critical;
    private java.lang.Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;

    public TaskToAdd() {
    }

    public TaskToAdd(
            java.lang.String assigneeId,
            java.util.Date deadline,
            java.lang.String instructions,
            java.lang.Boolean critical) {
        this.assigneeId = assigneeId;
        this.deadline = deadline;
        this.instructions = instructions;
        this.critical = critical;
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
     * Gets the assigneeId value for this TaskToAdd.
     *
     * @return assigneeId * Current entity id of the internal person that has this task assigned.
     *     Organizations or external persons are not valid assignees.
     */
    public java.lang.String getAssigneeId() {
        return assigneeId;
    }

    /**
     * Sets the assigneeId value for this TaskToAdd.
     *
     * @param assigneeId * Current entity id of the internal person that has this task assigned.
     *     Organizations or external persons are not valid assignees.
     */
    public void setAssigneeId(java.lang.String assigneeId) {
        this.assigneeId = assigneeId;
    }

    /**
     * Gets the deadline value for this TaskToAdd.
     *
     * @return deadline * An optional deadline for the task
     */
    public java.util.Date getDeadline() {
        return deadline;
    }

    /**
     * Sets the deadline value for this TaskToAdd.
     *
     * @param deadline * An optional deadline for the task
     */
    public void setDeadline(java.util.Date deadline) {
        this.deadline = deadline;
    }

    /**
     * Gets the instructions value for this TaskToAdd.
     *
     * @return instructions * Optional instructions given by the sender to the assignee regarding what
     *     has to be done as part of this task.
     */
    public java.lang.String getInstructions() {
        return instructions;
    }

    /**
     * Sets the instructions value for this TaskToAdd.
     *
     * @param instructions * Optional instructions given by the sender to the assignee regarding what
     *     has to be done as part of this task.
     */
    public void setInstructions(java.lang.String instructions) {
        this.instructions = instructions;
    }

    /**
     * Gets the critical value for this TaskToAdd.
     *
     * @return critical * Whether this is a critical task or not
     */
    public java.lang.Boolean getCritical() {
        return critical;
    }

    /**
     * Sets the critical value for this TaskToAdd.
     *
     * @param critical * Whether this is a critical task or not
     */
    public void setCritical(java.lang.Boolean critical) {
        this.critical = critical;
    }

    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof TaskToAdd)) {
            return false;
        }
        TaskToAdd other = (TaskToAdd) obj;
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
                        && ((this.assigneeId == null && other.getAssigneeId() == null)
                        || (this.assigneeId != null && this.assigneeId.equals(other.getAssigneeId())))
                        && ((this.deadline == null && other.getDeadline() == null)
                        || (this.deadline != null && this.deadline.equals(other.getDeadline())))
                        && ((this.instructions == null && other.getInstructions() == null)
                        || (this.instructions != null && this.instructions.equals(other.getInstructions())))
                        && ((this.critical == null && other.getCritical() == null)
                        || (this.critical != null && this.critical.equals(other.getCritical())));
        __equalsCalc = null;
        return _equals;
    }

    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getAssigneeId() != null) {
            _hashCode += getAssigneeId().hashCode();
        }
        if (getDeadline() != null) {
            _hashCode += getDeadline().hashCode();
        }
        if (getInstructions() != null) {
            _hashCode += getInstructions().hashCode();
        }
        if (getCritical() != null) {
            _hashCode += getCritical().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }
}
