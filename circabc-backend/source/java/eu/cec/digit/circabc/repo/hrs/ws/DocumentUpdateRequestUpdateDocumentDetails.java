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
 * <p>DocumentUpdateRequestUpdateDocumentDetails.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 * <p>
 * DocumentUpdateRequestUpdateDocumentDetails.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
/**
 * DocumentUpdateRequestUpdateDocumentDetails.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package eu.cec.digit.circabc.repo.hrs.ws;

public class DocumentUpdateRequestUpdateDocumentDetails implements java.io.Serializable {

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
            new org.apache.axis.description.TypeDesc(
                    DocumentUpdateRequestUpdateDocumentDetails.class, true);

    static {
        typeDesc.setXmlType(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", ">DocumentUpdateRequest>updateDocumentDetails"));
        org.apache.axis.description.ElementDesc elemField =
                new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("title");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "title"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("documentDate");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "documentDate"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "date"));
        elemField.setMinOccurs(0);
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
        elemField.setFieldName("securityClassification");
        elemField.setXmlName(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "securityClassification"));
        elemField.setXmlType(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "SecurityClassification"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("marking");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "marking"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "Marking"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("removeMarking");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "removeMarking"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("encryptItems");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "encryptItems"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("encryptItemsDeadline");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "encryptItemsDeadline"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "date"));
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
        elemField.setFieldName("mailType");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "mailType"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "MailType"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /* Updated title of the document */
    private java.lang.String title;
    /* Updated document date */
    private java.util.Date documentDate;
    /* Updated document sent date */
    private java.util.Date sentDate;
    /* Updated document security classification. When specifying 'EU_RESTREINED',
     * you also need to remove all items if any. */
    private eu.cec.digit.circabc.repo.hrs.ws.SecurityClassification securityClassification;
    /* Updated marker. When specifying COMP or OLAF markings, you
     * also need to remove all items. */
    private eu.cec.digit.circabc.repo.hrs.ws.Marking marking;
    /* Set this element to true if you want to remove the marking
     * associated with the document. This has precedence over the marking
     * element. */
    private java.lang.Boolean removeMarking;
    /* Indicates if the items' content should be encrypted or decrypted */
    private java.lang.Boolean encryptItems;
    /* Date when the encryption of the docuemnt's items expires. */
    private java.util.Date encryptItemsDeadline;
    /* Updated comments */
    private java.lang.String comments;
    /* Updated mail type */
    private eu.cec.digit.circabc.repo.hrs.ws.MailType mailType;
    private java.lang.Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;

    public DocumentUpdateRequestUpdateDocumentDetails() {
    }

    public DocumentUpdateRequestUpdateDocumentDetails(
            java.lang.String title,
            java.util.Date documentDate,
            java.util.Date sentDate,
            eu.cec.digit.circabc.repo.hrs.ws.SecurityClassification securityClassification,
            eu.cec.digit.circabc.repo.hrs.ws.Marking marking,
            java.lang.Boolean removeMarking,
            java.lang.Boolean encryptItems,
            java.util.Date encryptItemsDeadline,
            java.lang.String comments,
            eu.cec.digit.circabc.repo.hrs.ws.MailType mailType) {
        this.title = title;
        this.documentDate = documentDate;
        this.sentDate = sentDate;
        this.securityClassification = securityClassification;
        this.marking = marking;
        this.removeMarking = removeMarking;
        this.encryptItems = encryptItems;
        this.encryptItemsDeadline = encryptItemsDeadline;
        this.comments = comments;
        this.mailType = mailType;
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
     * Gets the title value for this DocumentUpdateRequestUpdateDocumentDetails.
     *
     * @return title * Updated title of the document
     */
    public java.lang.String getTitle() {
        return title;
    }

    /**
     * Sets the title value for this DocumentUpdateRequestUpdateDocumentDetails.
     *
     * @param title * Updated title of the document
     */
    public void setTitle(java.lang.String title) {
        this.title = title;
    }

    /**
     * Gets the documentDate value for this DocumentUpdateRequestUpdateDocumentDetails.
     *
     * @return documentDate * Updated document date
     */
    public java.util.Date getDocumentDate() {
        return documentDate;
    }

    /**
     * Sets the documentDate value for this DocumentUpdateRequestUpdateDocumentDetails.
     *
     * @param documentDate * Updated document date
     */
    public void setDocumentDate(java.util.Date documentDate) {
        this.documentDate = documentDate;
    }

    /**
     * Gets the sentDate value for this DocumentUpdateRequestUpdateDocumentDetails.
     *
     * @return sentDate * Updated document sent date
     */
    public java.util.Date getSentDate() {
        return sentDate;
    }

    /**
     * Sets the sentDate value for this DocumentUpdateRequestUpdateDocumentDetails.
     *
     * @param sentDate * Updated document sent date
     */
    public void setSentDate(java.util.Date sentDate) {
        this.sentDate = sentDate;
    }

    /**
     * Gets the securityClassification value for this DocumentUpdateRequestUpdateDocumentDetails.
     *
     * @return securityClassification * Updated document security classification. When specifying
     *     'EU_RESTREINED', you also need to remove all items if any.
     */
    public eu.cec.digit.circabc.repo.hrs.ws.SecurityClassification getSecurityClassification() {
        return securityClassification;
    }

    /**
     * Sets the securityClassification value for this DocumentUpdateRequestUpdateDocumentDetails.
     *
     * @param securityClassification * Updated document security classification. When specifying
     *     'EU_RESTREINED', you also need to remove all items if any.
     */
    public void setSecurityClassification(
            eu.cec.digit.circabc.repo.hrs.ws.SecurityClassification securityClassification) {
        this.securityClassification = securityClassification;
    }

    /**
     * Gets the marking value for this DocumentUpdateRequestUpdateDocumentDetails.
     *
     * @return marking * Updated marker. When specifying COMP or OLAF markings, you also need to
     *     remove all items.
     */
    public eu.cec.digit.circabc.repo.hrs.ws.Marking getMarking() {
        return marking;
    }

    /**
     * Sets the marking value for this DocumentUpdateRequestUpdateDocumentDetails.
     *
     * @param marking * Updated marker. When specifying COMP or OLAF markings, you also need to remove
     *     all items.
     */
    public void setMarking(eu.cec.digit.circabc.repo.hrs.ws.Marking marking) {
        this.marking = marking;
    }

    /**
     * Gets the removeMarking value for this DocumentUpdateRequestUpdateDocumentDetails.
     *
     * @return removeMarking * Set this element to true if you want to remove the marking associated
     *     with the document. This has precedence over the marking element.
     */
    public java.lang.Boolean getRemoveMarking() {
        return removeMarking;
    }

    /**
     * Sets the removeMarking value for this DocumentUpdateRequestUpdateDocumentDetails.
     *
     * @param removeMarking * Set this element to true if you want to remove the marking associated
     *     with the document. This has precedence over the marking element.
     */
    public void setRemoveMarking(java.lang.Boolean removeMarking) {
        this.removeMarking = removeMarking;
    }

    /**
     * Gets the encryptItems value for this DocumentUpdateRequestUpdateDocumentDetails.
     *
     * @return encryptItems * Indicates if the items' content should be encrypted or decrypted
     */
    public java.lang.Boolean getEncryptItems() {
        return encryptItems;
    }

    /**
     * Sets the encryptItems value for this DocumentUpdateRequestUpdateDocumentDetails.
     *
     * @param encryptItems * Indicates if the items' content should be encrypted or decrypted
     */
    public void setEncryptItems(java.lang.Boolean encryptItems) {
        this.encryptItems = encryptItems;
    }

    /**
     * Gets the encryptItemsDeadline value for this DocumentUpdateRequestUpdateDocumentDetails.
     *
     * @return encryptItemsDeadline * Date when the encryption of the docuemnt's items expires.
     */
    public java.util.Date getEncryptItemsDeadline() {
        return encryptItemsDeadline;
    }

    /**
     * Sets the encryptItemsDeadline value for this DocumentUpdateRequestUpdateDocumentDetails.
     *
     * @param encryptItemsDeadline * Date when the encryption of the docuemnt's items expires.
     */
    public void setEncryptItemsDeadline(java.util.Date encryptItemsDeadline) {
        this.encryptItemsDeadline = encryptItemsDeadline;
    }

    /**
     * Gets the comments value for this DocumentUpdateRequestUpdateDocumentDetails.
     *
     * @return comments * Updated comments
     */
    public java.lang.String getComments() {
        return comments;
    }

    /**
     * Sets the comments value for this DocumentUpdateRequestUpdateDocumentDetails.
     *
     * @param comments * Updated comments
     */
    public void setComments(java.lang.String comments) {
        this.comments = comments;
    }

    /**
     * Gets the mailType value for this DocumentUpdateRequestUpdateDocumentDetails.
     *
     * @return mailType * Updated mail type
     */
    public eu.cec.digit.circabc.repo.hrs.ws.MailType getMailType() {
        return mailType;
    }

    /**
     * Sets the mailType value for this DocumentUpdateRequestUpdateDocumentDetails.
     *
     * @param mailType * Updated mail type
     */
    public void setMailType(eu.cec.digit.circabc.repo.hrs.ws.MailType mailType) {
        this.mailType = mailType;
    }

    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof DocumentUpdateRequestUpdateDocumentDetails)) {
            return false;
        }
        DocumentUpdateRequestUpdateDocumentDetails other =
                (DocumentUpdateRequestUpdateDocumentDetails) obj;
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
                        && ((this.title == null && other.getTitle() == null)
                        || (this.title != null && this.title.equals(other.getTitle())))
                        && ((this.documentDate == null && other.getDocumentDate() == null)
                        || (this.documentDate != null && this.documentDate.equals(other.getDocumentDate())))
                        && ((this.sentDate == null && other.getSentDate() == null)
                        || (this.sentDate != null && this.sentDate.equals(other.getSentDate())))
                        && ((this.securityClassification == null && other.getSecurityClassification() == null)
                        || (this.securityClassification != null
                        && this.securityClassification.equals(other.getSecurityClassification())))
                        && ((this.marking == null && other.getMarking() == null)
                        || (this.marking != null && this.marking.equals(other.getMarking())))
                        && ((this.removeMarking == null && other.getRemoveMarking() == null)
                        || (this.removeMarking != null
                        && this.removeMarking.equals(other.getRemoveMarking())))
                        && ((this.encryptItems == null && other.getEncryptItems() == null)
                        || (this.encryptItems != null && this.encryptItems.equals(other.getEncryptItems())))
                        && ((this.encryptItemsDeadline == null && other.getEncryptItemsDeadline() == null)
                        || (this.encryptItemsDeadline != null
                        && this.encryptItemsDeadline.equals(other.getEncryptItemsDeadline())))
                        && ((this.comments == null && other.getComments() == null)
                        || (this.comments != null && this.comments.equals(other.getComments())))
                        && ((this.mailType == null && other.getMailType() == null)
                        || (this.mailType != null && this.mailType.equals(other.getMailType())));
        __equalsCalc = null;
        return _equals;
    }

    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getTitle() != null) {
            _hashCode += getTitle().hashCode();
        }
        if (getDocumentDate() != null) {
            _hashCode += getDocumentDate().hashCode();
        }
        if (getSentDate() != null) {
            _hashCode += getSentDate().hashCode();
        }
        if (getSecurityClassification() != null) {
            _hashCode += getSecurityClassification().hashCode();
        }
        if (getMarking() != null) {
            _hashCode += getMarking().hashCode();
        }
        if (getRemoveMarking() != null) {
            _hashCode += getRemoveMarking().hashCode();
        }
        if (getEncryptItems() != null) {
            _hashCode += getEncryptItems().hashCode();
        }
        if (getEncryptItemsDeadline() != null) {
            _hashCode += getEncryptItemsDeadline().hashCode();
        }
        if (getComments() != null) {
            _hashCode += getComments().hashCode();
        }
        if (getMailType() != null) {
            _hashCode += getMailType().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }
}
