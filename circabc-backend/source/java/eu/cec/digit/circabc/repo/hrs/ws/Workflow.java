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
 * <p>Workflow.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 * <p>
 * Workflow.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
/**
 * Workflow.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package eu.cec.digit.circabc.repo.hrs.ws;

/** A workflow */
public abstract class Workflow implements java.io.Serializable {

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
            new org.apache.axis.description.TypeDesc(Workflow.class, true);

    static {
        typeDesc.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "Workflow"));
        org.apache.axis.description.ElementDesc elemField =
                new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("workflowId");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "workflowId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("documentId");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "documentId"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("active");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "active"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /* Internal ID to uniquely identify a workflow */
    private int workflowId;
    /* Internal repository ID of the document for which this workflow
     * exists */
    private java.lang.String documentId;
    /* Indicates if the workflow is currently active. A workflow is
     * considered active if it has active/launched tasks. */
    private java.lang.Boolean active;
    private java.lang.Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;

    public Workflow() {
    }

    public Workflow(int workflowId, java.lang.String documentId, java.lang.Boolean active) {
        this.workflowId = workflowId;
        this.documentId = documentId;
        this.active = active;
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
     * Gets the workflowId value for this Workflow.
     *
     * @return workflowId * Internal ID to uniquely identify a workflow
     */
    public int getWorkflowId() {
        return workflowId;
    }

    /**
     * Sets the workflowId value for this Workflow.
     *
     * @param workflowId * Internal ID to uniquely identify a workflow
     */
    public void setWorkflowId(int workflowId) {
        this.workflowId = workflowId;
    }

    /**
     * Gets the documentId value for this Workflow.
     *
     * @return documentId * Internal repository ID of the document for which this workflow exists
     */
    public java.lang.String getDocumentId() {
        return documentId;
    }

    /**
     * Sets the documentId value for this Workflow.
     *
     * @param documentId * Internal repository ID of the document for which this workflow exists
     */
    public void setDocumentId(java.lang.String documentId) {
        this.documentId = documentId;
    }

    /**
     * Gets the active value for this Workflow.
     *
     * @return active * Indicates if the workflow is currently active. A workflow is considered active
     *     if it has active/launched tasks.
     */
    public java.lang.Boolean getActive() {
        return active;
    }

    /**
     * Sets the active value for this Workflow.
     *
     * @param active * Indicates if the workflow is currently active. A workflow is considered active
     *     if it has active/launched tasks.
     */
    public void setActive(java.lang.Boolean active) {
        this.active = active;
    }

    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Workflow)) {
            return false;
        }
        Workflow other = (Workflow) obj;
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
                        && this.workflowId == other.getWorkflowId()
                        && ((this.documentId == null && other.getDocumentId() == null)
                        || (this.documentId != null && this.documentId.equals(other.getDocumentId())))
                        && ((this.active == null && other.getActive() == null)
                        || (this.active != null && this.active.equals(other.getActive())));
        __equalsCalc = null;
        return _equals;
    }

    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        _hashCode += getWorkflowId();
        if (getDocumentId() != null) {
            _hashCode += getDocumentId().hashCode();
        }
        if (getActive() != null) {
            _hashCode += getActive().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }
}
