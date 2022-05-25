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
 * <p>DocumentUpdateRequest.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 * <p>
 * DocumentUpdateRequest.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
/**
 * DocumentUpdateRequest.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package eu.cec.digit.circabc.repo.hrs.ws;

/** Request for updating a saved (but unregistered) document */
public class DocumentUpdateRequest implements java.io.Serializable {

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
            new org.apache.axis.description.TypeDesc(DocumentUpdateRequest.class, true);

    static {
        typeDesc.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "DocumentUpdateRequest"));
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
        elemField.setFieldName("updateDocumentDetails");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "updateDocumentDetails"));
        elemField.setXmlType(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", ">DocumentUpdateRequest>updateDocumentDetails"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("updateSenders");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "updateSenders"));
        elemField.setXmlType(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", ">DocumentUpdateRequest>updateSenders"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("updateRecipients");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "updateRecipients"));
        elemField.setXmlType(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", ">DocumentUpdateRequest>updateRecipients"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("updateItems");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "updateItems"));
        elemField.setXmlType(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", ">DocumentUpdateRequest>updateItems"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /* The repository id of the document to update */
    private java.lang.String documentId;
    /* Update fields on the document. This works as follows:
     *                     <ul>
     *                         <li>To update a field, set the corresponding
     * element below to the updated value.</li>
     *                         <li>To leave a field unchanged omit the corresponding
     * element.</li>
     *                     </ul> */
    private eu.cec.digit.circabc.repo.hrs.ws.DocumentUpdateRequestUpdateDocumentDetails
            updateDocumentDetails;
    /* Add or remove document senders */
    private eu.cec.digit.circabc.repo.hrs.ws.DocumentUpdateRequestUpdateSenders updateSenders;
    /* Add or remove document recipients */
    private eu.cec.digit.circabc.repo.hrs.ws.DocumentUpdateRequestUpdateRecipients updateRecipients;
    /* Add/remove items or modify item metadata (including translations) */
    private eu.cec.digit.circabc.repo.hrs.ws.DocumentUpdateRequestUpdateItems updateItems;
    private java.lang.Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;

    public DocumentUpdateRequest() {
    }

    public DocumentUpdateRequest(
            java.lang.String documentId,
            eu.cec.digit.circabc.repo.hrs.ws.DocumentUpdateRequestUpdateDocumentDetails
                    updateDocumentDetails,
            eu.cec.digit.circabc.repo.hrs.ws.DocumentUpdateRequestUpdateSenders updateSenders,
            eu.cec.digit.circabc.repo.hrs.ws.DocumentUpdateRequestUpdateRecipients updateRecipients,
            eu.cec.digit.circabc.repo.hrs.ws.DocumentUpdateRequestUpdateItems updateItems) {
        this.documentId = documentId;
        this.updateDocumentDetails = updateDocumentDetails;
        this.updateSenders = updateSenders;
        this.updateRecipients = updateRecipients;
        this.updateItems = updateItems;
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
     * Gets the documentId value for this DocumentUpdateRequest.
     *
     * @return documentId * The repository id of the document to update
     */
    public java.lang.String getDocumentId() {
        return documentId;
    }

    /**
     * Sets the documentId value for this DocumentUpdateRequest.
     *
     * @param documentId * The repository id of the document to update
     */
    public void setDocumentId(java.lang.String documentId) {
        this.documentId = documentId;
    }

    /**
     * Gets the updateDocumentDetails value for this DocumentUpdateRequest.
     *
     * @return updateDocumentDetails * Update fields on the document. This works as follows:
     *     <ul>
     *       <li>To update a field, set the corresponding element below to the updated value.
     *       <li>To leave a field unchanged omit the corresponding element.
     *     </ul>
     */
    public eu.cec.digit.circabc.repo.hrs.ws.DocumentUpdateRequestUpdateDocumentDetails
    getUpdateDocumentDetails() {
        return updateDocumentDetails;
    }

    /**
     * Sets the updateDocumentDetails value for this DocumentUpdateRequest.
     *
     * @param updateDocumentDetails * Update fields on the document. This works as follows:
     *     <ul>
     *       <li>To update a field, set the corresponding element below to the updated value.
     *       <li>To leave a field unchanged omit the corresponding element.
     *     </ul>
     */
    public void setUpdateDocumentDetails(
            eu.cec.digit.circabc.repo.hrs.ws.DocumentUpdateRequestUpdateDocumentDetails
                    updateDocumentDetails) {
        this.updateDocumentDetails = updateDocumentDetails;
    }

    /**
     * Gets the updateSenders value for this DocumentUpdateRequest.
     *
     * @return updateSenders * Add or remove document senders
     */
    public eu.cec.digit.circabc.repo.hrs.ws.DocumentUpdateRequestUpdateSenders getUpdateSenders() {
        return updateSenders;
    }

    /**
     * Sets the updateSenders value for this DocumentUpdateRequest.
     *
     * @param updateSenders * Add or remove document senders
     */
    public void setUpdateSenders(
            eu.cec.digit.circabc.repo.hrs.ws.DocumentUpdateRequestUpdateSenders updateSenders) {
        this.updateSenders = updateSenders;
    }

    /**
     * Gets the updateRecipients value for this DocumentUpdateRequest.
     *
     * @return updateRecipients * Add or remove document recipients
     */
    public eu.cec.digit.circabc.repo.hrs.ws.DocumentUpdateRequestUpdateRecipients
    getUpdateRecipients() {
        return updateRecipients;
    }

    /**
     * Sets the updateRecipients value for this DocumentUpdateRequest.
     *
     * @param updateRecipients * Add or remove document recipients
     */
    public void setUpdateRecipients(
            eu.cec.digit.circabc.repo.hrs.ws.DocumentUpdateRequestUpdateRecipients updateRecipients) {
        this.updateRecipients = updateRecipients;
    }

    /**
     * Gets the updateItems value for this DocumentUpdateRequest.
     *
     * @return updateItems * Add/remove items or modify item metadata (including translations)
     */
    public eu.cec.digit.circabc.repo.hrs.ws.DocumentUpdateRequestUpdateItems getUpdateItems() {
        return updateItems;
    }

    /**
     * Sets the updateItems value for this DocumentUpdateRequest.
     *
     * @param updateItems * Add/remove items or modify item metadata (including translations)
     */
    public void setUpdateItems(
            eu.cec.digit.circabc.repo.hrs.ws.DocumentUpdateRequestUpdateItems updateItems) {
        this.updateItems = updateItems;
    }

    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof DocumentUpdateRequest)) {
            return false;
        }
        DocumentUpdateRequest other = (DocumentUpdateRequest) obj;
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
                        && ((this.updateDocumentDetails == null && other.getUpdateDocumentDetails() == null)
                        || (this.updateDocumentDetails != null
                        && this.updateDocumentDetails.equals(other.getUpdateDocumentDetails())))
                        && ((this.updateSenders == null && other.getUpdateSenders() == null)
                        || (this.updateSenders != null
                        && this.updateSenders.equals(other.getUpdateSenders())))
                        && ((this.updateRecipients == null && other.getUpdateRecipients() == null)
                        || (this.updateRecipients != null
                        && this.updateRecipients.equals(other.getUpdateRecipients())))
                        && ((this.updateItems == null && other.getUpdateItems() == null)
                        || (this.updateItems != null && this.updateItems.equals(other.getUpdateItems())));
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
        if (getUpdateDocumentDetails() != null) {
            _hashCode += getUpdateDocumentDetails().hashCode();
        }
        if (getUpdateSenders() != null) {
            _hashCode += getUpdateSenders().hashCode();
        }
        if (getUpdateRecipients() != null) {
            _hashCode += getUpdateRecipients().hashCode();
        }
        if (getUpdateItems() != null) {
            _hashCode += getUpdateItems().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }
}
