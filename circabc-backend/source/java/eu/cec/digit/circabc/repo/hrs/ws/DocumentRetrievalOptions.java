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
 * <p>DocumentRetrievalOptions.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 * <p>
 * DocumentRetrievalOptions.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
/**
 * DocumentRetrievalOptions.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package eu.cec.digit.circabc.repo.hrs.ws;

/**
 * Options allowing to precise the parts of a document we need to retrieve. It allows client
 * applications to define which metadata to retrieve for a document.
 */
public class DocumentRetrievalOptions implements java.io.Serializable {

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
            new org.apache.axis.description.TypeDesc(DocumentRetrievalOptions.class, true);

    static {
        typeDesc.setXmlType(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "DocumentRetrievalOptions"));
        org.apache.axis.description.ElementDesc elemField =
                new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("includeItemsMetadata");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "includeItemsMetadata"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("includeSendersAndRecipientsMetadata");
        elemField.setXmlName(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "includeSendersAndRecipientsMetadata"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("includeSenders");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "includeSenders"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("includeRecipients");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "includeRecipients"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("includeFilingInfo");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "includeFilingInfo"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("includeLinks");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "includeLinks"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("includeAssignmentWorkflow");
        elemField.setXmlName(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "includeAssignmentWorkflow"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("includeSignatoryWorkflow");
        elemField.setXmlName(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "includeSignatoryWorkflow"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("limitTasksToCurrentUser");
        elemField.setXmlName(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "limitTasksToCurrentUser"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("limitTasksToActiveTasks");
        elemField.setXmlName(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "limitTasksToActiveTasks"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("includeProcedure");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "includeProcedure"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("includeSignedBy");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "includeSignedBy"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /* Specifies that the document search response will contain the
     * metadata of the attachments */
    private java.lang.Boolean includeItemsMetadata;
    /* Specifies that the document search response will contain the
     * metadata of senders/receivers.
     *                             Equivalent to specifying both includeSenders
     * and includeRecipients. */
    private java.lang.Boolean includeSendersAndRecipientsMetadata;
    /* Specifies that the document search response will contain the
     * metadata of senders */
    private java.lang.Boolean includeSenders;
    /* Specifies that the document search response will contain the
     * metadata of recipients */
    private java.lang.Boolean includeRecipients;
    /* It specifies that the document search response will contain
     * filing information */
    private java.lang.Boolean includeFilingInfo;
    /* It specifies that the document search response will contain
     * the links of the document */
    private java.lang.Boolean includeLinks;
    /* It specifies that the document search response should contain
     * the Assignment workflow if existing */
    private java.lang.Boolean includeAssignmentWorkflow;
    /* It specifies that the document search response should contain
     * the Signatory Paper or e-Signatory workflow if existing */
    private java.lang.Boolean includeSignatoryWorkflow;
    /* When retrieving the workflow (Assignment or Signatory) limit
     * returned tasks to those
     *                         assigned to the current user.
     *                         This can be used as a performance optimization,
     * since the Assignment workflow of a document
     *                         (and to a lesser extent the Signatory workflow)
     * can include hundreds of tasks in Production. */
    private java.lang.Boolean limitTasksToCurrentUser;
    /* When retrieving the workflow of a document (Assignment or Signatory)
     * limit
     *                         returned tasks to those that are active.
     *                         This can be used as a performance optimization,
     * since the Assignment workflow of a document
     *                         (and to a lesser extent the Signatory workflow)
     * can include hundreds of tasks in Production. */
    private java.lang.Boolean limitTasksToActiveTasks;
    /* It specifies that the document search response should include
     * the procedure (if any) linked to the document */
    private java.lang.Boolean includeProcedure;
    /* Include the users that have signed the document (applies only
     * to registered documents). */
    private java.lang.Boolean includeSignedBy;
    private java.lang.Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;

    public DocumentRetrievalOptions() {
    }

    public DocumentRetrievalOptions(
            java.lang.Boolean includeItemsMetadata,
            java.lang.Boolean includeSendersAndRecipientsMetadata,
            java.lang.Boolean includeSenders,
            java.lang.Boolean includeRecipients,
            java.lang.Boolean includeFilingInfo,
            java.lang.Boolean includeLinks,
            java.lang.Boolean includeAssignmentWorkflow,
            java.lang.Boolean includeSignatoryWorkflow,
            java.lang.Boolean limitTasksToCurrentUser,
            java.lang.Boolean limitTasksToActiveTasks,
            java.lang.Boolean includeProcedure,
            java.lang.Boolean includeSignedBy) {
        this.includeItemsMetadata = includeItemsMetadata;
        this.includeSendersAndRecipientsMetadata = includeSendersAndRecipientsMetadata;
        this.includeSenders = includeSenders;
        this.includeRecipients = includeRecipients;
        this.includeFilingInfo = includeFilingInfo;
        this.includeLinks = includeLinks;
        this.includeAssignmentWorkflow = includeAssignmentWorkflow;
        this.includeSignatoryWorkflow = includeSignatoryWorkflow;
        this.limitTasksToCurrentUser = limitTasksToCurrentUser;
        this.limitTasksToActiveTasks = limitTasksToActiveTasks;
        this.includeProcedure = includeProcedure;
        this.includeSignedBy = includeSignedBy;
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
     * Gets the includeItemsMetadata value for this DocumentRetrievalOptions.
     *
     * @return includeItemsMetadata * Specifies that the document search response will contain the
     *     metadata of the attachments
     */
    public java.lang.Boolean getIncludeItemsMetadata() {
        return includeItemsMetadata;
    }

    /**
     * Sets the includeItemsMetadata value for this DocumentRetrievalOptions.
     *
     * @param includeItemsMetadata * Specifies that the document search response will contain the
     *     metadata of the attachments
     */
    public void setIncludeItemsMetadata(java.lang.Boolean includeItemsMetadata) {
        this.includeItemsMetadata = includeItemsMetadata;
    }

    /**
     * Gets the includeSendersAndRecipientsMetadata value for this DocumentRetrievalOptions.
     *
     * @return includeSendersAndRecipientsMetadata * Specifies that the document search response will
     *     contain the metadata of senders/receivers. Equivalent to specifying both includeSenders and
     *     includeRecipients.
     */
    public java.lang.Boolean getIncludeSendersAndRecipientsMetadata() {
        return includeSendersAndRecipientsMetadata;
    }

    /**
     * Sets the includeSendersAndRecipientsMetadata value for this DocumentRetrievalOptions.
     *
     * @param includeSendersAndRecipientsMetadata * Specifies that the document search response will
     *     contain the metadata of senders/receivers. Equivalent to specifying both includeSenders and
     *     includeRecipients.
     */
    public void setIncludeSendersAndRecipientsMetadata(
            java.lang.Boolean includeSendersAndRecipientsMetadata) {
        this.includeSendersAndRecipientsMetadata = includeSendersAndRecipientsMetadata;
    }

    /**
     * Gets the includeSenders value for this DocumentRetrievalOptions.
     *
     * @return includeSenders * Specifies that the document search response will contain the metadata
     *     of senders
     */
    public java.lang.Boolean getIncludeSenders() {
        return includeSenders;
    }

    /**
     * Sets the includeSenders value for this DocumentRetrievalOptions.
     *
     * @param includeSenders * Specifies that the document search response will contain the metadata
     *     of senders
     */
    public void setIncludeSenders(java.lang.Boolean includeSenders) {
        this.includeSenders = includeSenders;
    }

    /**
     * Gets the includeRecipients value for this DocumentRetrievalOptions.
     *
     * @return includeRecipients * Specifies that the document search response will contain the
     *     metadata of recipients
     */
    public java.lang.Boolean getIncludeRecipients() {
        return includeRecipients;
    }

    /**
     * Sets the includeRecipients value for this DocumentRetrievalOptions.
     *
     * @param includeRecipients * Specifies that the document search response will contain the
     *     metadata of recipients
     */
    public void setIncludeRecipients(java.lang.Boolean includeRecipients) {
        this.includeRecipients = includeRecipients;
    }

    /**
     * Gets the includeFilingInfo value for this DocumentRetrievalOptions.
     *
     * @return includeFilingInfo * It specifies that the document search response will contain filing
     *     information
     */
    public java.lang.Boolean getIncludeFilingInfo() {
        return includeFilingInfo;
    }

    /**
     * Sets the includeFilingInfo value for this DocumentRetrievalOptions.
     *
     * @param includeFilingInfo * It specifies that the document search response will contain filing
     *     information
     */
    public void setIncludeFilingInfo(java.lang.Boolean includeFilingInfo) {
        this.includeFilingInfo = includeFilingInfo;
    }

    /**
     * Gets the includeLinks value for this DocumentRetrievalOptions.
     *
     * @return includeLinks * It specifies that the document search response will contain the links of
     *     the document
     */
    public java.lang.Boolean getIncludeLinks() {
        return includeLinks;
    }

    /**
     * Sets the includeLinks value for this DocumentRetrievalOptions.
     *
     * @param includeLinks * It specifies that the document search response will contain the links of
     *     the document
     */
    public void setIncludeLinks(java.lang.Boolean includeLinks) {
        this.includeLinks = includeLinks;
    }

    /**
     * Gets the includeAssignmentWorkflow value for this DocumentRetrievalOptions.
     *
     * @return includeAssignmentWorkflow * It specifies that the document search response should
     *     contain the Assignment workflow if existing
     */
    public java.lang.Boolean getIncludeAssignmentWorkflow() {
        return includeAssignmentWorkflow;
    }

    /**
     * Sets the includeAssignmentWorkflow value for this DocumentRetrievalOptions.
     *
     * @param includeAssignmentWorkflow * It specifies that the document search response should
     *     contain the Assignment workflow if existing
     */
    public void setIncludeAssignmentWorkflow(java.lang.Boolean includeAssignmentWorkflow) {
        this.includeAssignmentWorkflow = includeAssignmentWorkflow;
    }

    /**
     * Gets the includeSignatoryWorkflow value for this DocumentRetrievalOptions.
     *
     * @return includeSignatoryWorkflow * It specifies that the document search response should
     *     contain the Signatory Paper or e-Signatory workflow if existing
     */
    public java.lang.Boolean getIncludeSignatoryWorkflow() {
        return includeSignatoryWorkflow;
    }

    /**
     * Sets the includeSignatoryWorkflow value for this DocumentRetrievalOptions.
     *
     * @param includeSignatoryWorkflow * It specifies that the document search response should contain
     *     the Signatory Paper or e-Signatory workflow if existing
     */
    public void setIncludeSignatoryWorkflow(java.lang.Boolean includeSignatoryWorkflow) {
        this.includeSignatoryWorkflow = includeSignatoryWorkflow;
    }

    /**
     * Gets the limitTasksToCurrentUser value for this DocumentRetrievalOptions.
     *
     * @return limitTasksToCurrentUser * When retrieving the workflow (Assignment or Signatory) limit
     *     returned tasks to those assigned to the current user. This can be used as a performance
     *     optimization, since the Assignment workflow of a document (and to a lesser extent the
     *     Signatory workflow) can include hundreds of tasks in Production.
     */
    public java.lang.Boolean getLimitTasksToCurrentUser() {
        return limitTasksToCurrentUser;
    }

    /**
     * Sets the limitTasksToCurrentUser value for this DocumentRetrievalOptions.
     *
     * @param limitTasksToCurrentUser * When retrieving the workflow (Assignment or Signatory) limit
     *     returned tasks to those assigned to the current user. This can be used as a performance
     *     optimization, since the Assignment workflow of a document (and to a lesser extent the
     *     Signatory workflow) can include hundreds of tasks in Production.
     */
    public void setLimitTasksToCurrentUser(java.lang.Boolean limitTasksToCurrentUser) {
        this.limitTasksToCurrentUser = limitTasksToCurrentUser;
    }

    /**
     * Gets the limitTasksToActiveTasks value for this DocumentRetrievalOptions.
     *
     * @return limitTasksToActiveTasks * When retrieving the workflow of a document (Assignment or
     *     Signatory) limit returned tasks to those that are active. This can be used as a performance
     *     optimization, since the Assignment workflow of a document (and to a lesser extent the
     *     Signatory workflow) can include hundreds of tasks in Production.
     */
    public java.lang.Boolean getLimitTasksToActiveTasks() {
        return limitTasksToActiveTasks;
    }

    /**
     * Sets the limitTasksToActiveTasks value for this DocumentRetrievalOptions.
     *
     * @param limitTasksToActiveTasks * When retrieving the workflow of a document (Assignment or
     *     Signatory) limit returned tasks to those that are active. This can be used as a performance
     *     optimization, since the Assignment workflow of a document (and to a lesser extent the
     *     Signatory workflow) can include hundreds of tasks in Production.
     */
    public void setLimitTasksToActiveTasks(java.lang.Boolean limitTasksToActiveTasks) {
        this.limitTasksToActiveTasks = limitTasksToActiveTasks;
    }

    /**
     * Gets the includeProcedure value for this DocumentRetrievalOptions.
     *
     * @return includeProcedure * It specifies that the document search response should include the
     *     procedure (if any) linked to the document
     */
    public java.lang.Boolean getIncludeProcedure() {
        return includeProcedure;
    }

    /**
     * Sets the includeProcedure value for this DocumentRetrievalOptions.
     *
     * @param includeProcedure * It specifies that the document search response should include the
     *     procedure (if any) linked to the document
     */
    public void setIncludeProcedure(java.lang.Boolean includeProcedure) {
        this.includeProcedure = includeProcedure;
    }

    /**
     * Gets the includeSignedBy value for this DocumentRetrievalOptions.
     *
     * @return includeSignedBy * Include the users that have signed the document (applies only to
     *     registered documents).
     */
    public java.lang.Boolean getIncludeSignedBy() {
        return includeSignedBy;
    }

    /**
     * Sets the includeSignedBy value for this DocumentRetrievalOptions.
     *
     * @param includeSignedBy * Include the users that have signed the document (applies only to
     *     registered documents).
     */
    public void setIncludeSignedBy(java.lang.Boolean includeSignedBy) {
        this.includeSignedBy = includeSignedBy;
    }

    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof DocumentRetrievalOptions)) {
            return false;
        }
        DocumentRetrievalOptions other = (DocumentRetrievalOptions) obj;
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
                        && ((this.includeItemsMetadata == null && other.getIncludeItemsMetadata() == null)
                        || (this.includeItemsMetadata != null
                        && this.includeItemsMetadata.equals(other.getIncludeItemsMetadata())))
                        && ((this.includeSendersAndRecipientsMetadata == null
                        && other.getIncludeSendersAndRecipientsMetadata() == null)
                        || (this.includeSendersAndRecipientsMetadata != null
                        && this.includeSendersAndRecipientsMetadata.equals(
                        other.getIncludeSendersAndRecipientsMetadata())))
                        && ((this.includeSenders == null && other.getIncludeSenders() == null)
                        || (this.includeSenders != null
                        && this.includeSenders.equals(other.getIncludeSenders())))
                        && ((this.includeRecipients == null && other.getIncludeRecipients() == null)
                        || (this.includeRecipients != null
                        && this.includeRecipients.equals(other.getIncludeRecipients())))
                        && ((this.includeFilingInfo == null && other.getIncludeFilingInfo() == null)
                        || (this.includeFilingInfo != null
                        && this.includeFilingInfo.equals(other.getIncludeFilingInfo())))
                        && ((this.includeLinks == null && other.getIncludeLinks() == null)
                        || (this.includeLinks != null && this.includeLinks.equals(other.getIncludeLinks())))
                        && ((this.includeAssignmentWorkflow == null
                        && other.getIncludeAssignmentWorkflow() == null)
                        || (this.includeAssignmentWorkflow != null
                        && this.includeAssignmentWorkflow.equals(other.getIncludeAssignmentWorkflow())))
                        && ((this.includeSignatoryWorkflow == null
                        && other.getIncludeSignatoryWorkflow() == null)
                        || (this.includeSignatoryWorkflow != null
                        && this.includeSignatoryWorkflow.equals(other.getIncludeSignatoryWorkflow())))
                        && ((this.limitTasksToCurrentUser == null && other.getLimitTasksToCurrentUser() == null)
                        || (this.limitTasksToCurrentUser != null
                        && this.limitTasksToCurrentUser.equals(other.getLimitTasksToCurrentUser())))
                        && ((this.limitTasksToActiveTasks == null && other.getLimitTasksToActiveTasks() == null)
                        || (this.limitTasksToActiveTasks != null
                        && this.limitTasksToActiveTasks.equals(other.getLimitTasksToActiveTasks())))
                        && ((this.includeProcedure == null && other.getIncludeProcedure() == null)
                        || (this.includeProcedure != null
                        && this.includeProcedure.equals(other.getIncludeProcedure())))
                        && ((this.includeSignedBy == null && other.getIncludeSignedBy() == null)
                        || (this.includeSignedBy != null
                        && this.includeSignedBy.equals(other.getIncludeSignedBy())));
        __equalsCalc = null;
        return _equals;
    }

    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getIncludeItemsMetadata() != null) {
            _hashCode += getIncludeItemsMetadata().hashCode();
        }
        if (getIncludeSendersAndRecipientsMetadata() != null) {
            _hashCode += getIncludeSendersAndRecipientsMetadata().hashCode();
        }
        if (getIncludeSenders() != null) {
            _hashCode += getIncludeSenders().hashCode();
        }
        if (getIncludeRecipients() != null) {
            _hashCode += getIncludeRecipients().hashCode();
        }
        if (getIncludeFilingInfo() != null) {
            _hashCode += getIncludeFilingInfo().hashCode();
        }
        if (getIncludeLinks() != null) {
            _hashCode += getIncludeLinks().hashCode();
        }
        if (getIncludeAssignmentWorkflow() != null) {
            _hashCode += getIncludeAssignmentWorkflow().hashCode();
        }
        if (getIncludeSignatoryWorkflow() != null) {
            _hashCode += getIncludeSignatoryWorkflow().hashCode();
        }
        if (getLimitTasksToCurrentUser() != null) {
            _hashCode += getLimitTasksToCurrentUser().hashCode();
        }
        if (getLimitTasksToActiveTasks() != null) {
            _hashCode += getLimitTasksToActiveTasks().hashCode();
        }
        if (getIncludeProcedure() != null) {
            _hashCode += getIncludeProcedure().hashCode();
        }
        if (getIncludeSignedBy() != null) {
            _hashCode += getIncludeSignedBy().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }
}
