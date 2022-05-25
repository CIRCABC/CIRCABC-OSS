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
 * <p>AssignmentWorkflow.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 * <p>
 * AssignmentWorkflow.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
/**
 * AssignmentWorkflow.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package eu.cec.digit.circabc.repo.hrs.ws;

/** A workflow of type Assignment */
public class AssignmentWorkflow extends eu.cec.digit.circabc.repo.hrs.ws.Workflow
        implements java.io.Serializable {

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
            new org.apache.axis.description.TypeDesc(AssignmentWorkflow.class, true);

    static {
        typeDesc.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "AssignmentWorkflow"));
        org.apache.axis.description.ElementDesc elemField =
                new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("tasks");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "tasks"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "AssignmentTask"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "task"));
        typeDesc.addFieldDesc(elemField);
    }

    /* The collection of tasks of the Assignment workflow */
    private eu.cec.digit.circabc.repo.hrs.ws.AssignmentTask[] tasks;
    private java.lang.Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;

    public AssignmentWorkflow() {
    }

    public AssignmentWorkflow(
            int workflowId,
            java.lang.String documentId,
            java.lang.Boolean active,
            eu.cec.digit.circabc.repo.hrs.ws.AssignmentTask[] tasks) {
        super(workflowId, documentId, active);
        this.tasks = tasks;
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
     * Gets the tasks value for this AssignmentWorkflow.
     *
     * @return tasks * The collection of tasks of the Assignment workflow
     */
    public eu.cec.digit.circabc.repo.hrs.ws.AssignmentTask[] getTasks() {
        return tasks;
    }

    /**
     * Sets the tasks value for this AssignmentWorkflow.
     *
     * @param tasks * The collection of tasks of the Assignment workflow
     */
    public void setTasks(eu.cec.digit.circabc.repo.hrs.ws.AssignmentTask[] tasks) {
        this.tasks = tasks;
    }

    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof AssignmentWorkflow)) {
            return false;
        }
        AssignmentWorkflow other = (AssignmentWorkflow) obj;
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
                super.equals(obj)
                        && ((this.tasks == null && other.getTasks() == null)
                        || (this.tasks != null && java.util.Arrays.equals(this.tasks, other.getTasks())));
        __equalsCalc = null;
        return _equals;
    }

    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = super.hashCode();
        if (getTasks() != null) {
            for (int i = 0; i < java.lang.reflect.Array.getLength(getTasks()); i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getTasks(), i);
                if (obj != null && !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        __hashCodeCalc = false;
        return _hashCode;
    }
}
