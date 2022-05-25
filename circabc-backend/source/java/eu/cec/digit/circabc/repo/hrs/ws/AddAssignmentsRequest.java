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
 * <p>AddAssignmentsRequest.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 * <p>
 * AddAssignmentsRequest.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
/**
 * AddAssignmentsRequest.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package eu.cec.digit.circabc.repo.hrs.ws;

/**
 * Request for adding assignment tasks to a document, by creating or updating its assignment
 * workflow.
 */
public class AddAssignmentsRequest implements java.io.Serializable {

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
            new org.apache.axis.description.TypeDesc(AddAssignmentsRequest.class, true);

    static {
        typeDesc.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "AddAssignmentsRequest"));
        org.apache.axis.description.ElementDesc elemField =
                new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("documentId");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "documentId"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("tasks");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "tasks"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "AssignmentTaskToAdd"));
        elemField.setNillable(false);
        elemField.setItemQName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "task"));
        typeDesc.addFieldDesc(elemField);
    }

    /* The document for which assignment tasks are added */
    private java.lang.String documentId;
    /* List of assignment tasks to be added to the document's assignment
     * workflow. */
    private eu.cec.digit.circabc.repo.hrs.ws.AssignmentTaskToAdd[] tasks;
    private java.lang.Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;

    public AddAssignmentsRequest() {
    }

    public AddAssignmentsRequest(
            java.lang.String documentId, eu.cec.digit.circabc.repo.hrs.ws.AssignmentTaskToAdd[] tasks) {
        this.documentId = documentId;
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
     * Gets the documentId value for this AddAssignmentsRequest.
     *
     * @return documentId * The document for which assignment tasks are added
     */
    public java.lang.String getDocumentId() {
        return documentId;
    }

    /**
     * Sets the documentId value for this AddAssignmentsRequest.
     *
     * @param documentId * The document for which assignment tasks are added
     */
    public void setDocumentId(java.lang.String documentId) {
        this.documentId = documentId;
    }

    /**
     * Gets the tasks value for this AddAssignmentsRequest.
     *
     * @return tasks * List of assignment tasks to be added to the document's assignment workflow.
     */
    public eu.cec.digit.circabc.repo.hrs.ws.AssignmentTaskToAdd[] getTasks() {
        return tasks;
    }

    /**
     * Sets the tasks value for this AddAssignmentsRequest.
     *
     * @param tasks * List of assignment tasks to be added to the document's assignment workflow.
     */
    public void setTasks(eu.cec.digit.circabc.repo.hrs.ws.AssignmentTaskToAdd[] tasks) {
        this.tasks = tasks;
    }

    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof AddAssignmentsRequest)) {
            return false;
        }
        AddAssignmentsRequest other = (AddAssignmentsRequest) obj;
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
                        && ((this.documentId == null && other.getDocumentId() == null)
                        || (this.documentId != null && this.documentId.equals(other.getDocumentId())))
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
        int _hashCode = 1;
        if (getDocumentId() != null) {
            _hashCode += getDocumentId().hashCode();
        }
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
