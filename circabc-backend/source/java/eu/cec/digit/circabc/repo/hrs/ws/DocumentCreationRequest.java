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
 * <p>DocumentCreationRequest.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 * <p>
 * DocumentCreationRequest.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
/**
 * DocumentCreationRequest.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package eu.cec.digit.circabc.repo.hrs.ws;

/** Request for the operation createDocument */
public class DocumentCreationRequest implements java.io.Serializable {

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
            new org.apache.axis.description.TypeDesc(DocumentCreationRequest.class, true);

    static {
        typeDesc.setXmlType(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "DocumentCreationRequest"));
        org.apache.axis.description.ElementDesc elemField =
                new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("title");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "title"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
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
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("senders");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "senders"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", ">SendersToAdd>sender"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "sender"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("recipients");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "recipients"));
        elemField.setXmlType(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", ">RecipientsToAdd>recipient"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "recipient"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("items");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "items"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "ItemsToAdd"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /* Title/subject of the document */
    private java.lang.String title;
    /* Date of the document */
    private java.util.Date documentDate;
    /* Date at which the document has been sent */
    private java.util.Date sentDate;
    /* Security classification to be applied to the document */
    private eu.cec.digit.circabc.repo.hrs.ws.SecurityClassification securityClassification;
    /* Marker to be applied to the document according to the security
     * classification */
    private eu.cec.digit.circabc.repo.hrs.ws.Marking marking;
    /* Indicates whether the content of the items should be stored
     * encrypted. The
     *                     encryption is done automatically when the content
     * is inserted into the
     *                     repository. No special action is required. Only
     * users having a dedicated role
     *                     are allowed to encrypt items. */
    private java.lang.Boolean encryptItems;
    /* Date when the encryption of the docuemnt's items expires. */
    private java.util.Date encryptItemsDeadline;
    /* Comments */
    private java.lang.String comments;
    /* Mail type */
    private eu.cec.digit.circabc.repo.hrs.ws.MailType mailType;
    /* Senders of the document */
    private eu.cec.digit.circabc.repo.hrs.ws.SendersToAddSender[] senders;
    /* Recipients of the document */
    private eu.cec.digit.circabc.repo.hrs.ws.RecipientsToAddRecipient[] recipients;
    /* List of attachments */
    private eu.cec.digit.circabc.repo.hrs.ws.ItemsToAdd items;
    private java.lang.Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;

    public DocumentCreationRequest() {
    }

    public DocumentCreationRequest(
            java.lang.String title,
            java.util.Date documentDate,
            java.util.Date sentDate,
            eu.cec.digit.circabc.repo.hrs.ws.SecurityClassification securityClassification,
            eu.cec.digit.circabc.repo.hrs.ws.Marking marking,
            java.lang.Boolean encryptItems,
            java.util.Date encryptItemsDeadline,
            java.lang.String comments,
            eu.cec.digit.circabc.repo.hrs.ws.MailType mailType,
            eu.cec.digit.circabc.repo.hrs.ws.SendersToAddSender[] senders,
            eu.cec.digit.circabc.repo.hrs.ws.RecipientsToAddRecipient[] recipients,
            eu.cec.digit.circabc.repo.hrs.ws.ItemsToAdd items) {
        this.title = title;
        this.documentDate = documentDate;
        this.sentDate = sentDate;
        this.securityClassification = securityClassification;
        this.marking = marking;
        this.encryptItems = encryptItems;
        this.encryptItemsDeadline = encryptItemsDeadline;
        this.comments = comments;
        this.mailType = mailType;
        this.senders = senders;
        this.recipients = recipients;
        this.items = items;
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
     * Gets the title value for this DocumentCreationRequest.
     *
     * @return title * Title/subject of the document
     */
    public java.lang.String getTitle() {
        return title;
    }

    /**
     * Sets the title value for this DocumentCreationRequest.
     *
     * @param title * Title/subject of the document
     */
    public void setTitle(java.lang.String title) {
        this.title = title;
    }

    /**
     * Gets the documentDate value for this DocumentCreationRequest.
     *
     * @return documentDate * Date of the document
     */
    public java.util.Date getDocumentDate() {
        return documentDate;
    }

    /**
     * Sets the documentDate value for this DocumentCreationRequest.
     *
     * @param documentDate * Date of the document
     */
    public void setDocumentDate(java.util.Date documentDate) {
        this.documentDate = documentDate;
    }

    /**
     * Gets the sentDate value for this DocumentCreationRequest.
     *
     * @return sentDate * Date at which the document has been sent
     */
    public java.util.Date getSentDate() {
        return sentDate;
    }

    /**
     * Sets the sentDate value for this DocumentCreationRequest.
     *
     * @param sentDate * Date at which the document has been sent
     */
    public void setSentDate(java.util.Date sentDate) {
        this.sentDate = sentDate;
    }

    /**
     * Gets the securityClassification value for this DocumentCreationRequest.
     *
     * @return securityClassification * Security classification to be applied to the document
     */
    public eu.cec.digit.circabc.repo.hrs.ws.SecurityClassification getSecurityClassification() {
        return securityClassification;
    }

    /**
     * Sets the securityClassification value for this DocumentCreationRequest.
     *
     * @param securityClassification * Security classification to be applied to the document
     */
    public void setSecurityClassification(
            eu.cec.digit.circabc.repo.hrs.ws.SecurityClassification securityClassification) {
        this.securityClassification = securityClassification;
    }

    /**
     * Gets the marking value for this DocumentCreationRequest.
     *
     * @return marking * Marker to be applied to the document according to the security classification
     */
    public eu.cec.digit.circabc.repo.hrs.ws.Marking getMarking() {
        return marking;
    }

    /**
     * Sets the marking value for this DocumentCreationRequest.
     *
     * @param marking * Marker to be applied to the document according to the security classification
     */
    public void setMarking(eu.cec.digit.circabc.repo.hrs.ws.Marking marking) {
        this.marking = marking;
    }

    /**
     * Gets the encryptItems value for this DocumentCreationRequest.
     *
     * @return encryptItems * Indicates whether the content of the items should be stored encrypted.
     *     The encryption is done automatically when the content is inserted into the repository. No
     *     special action is required. Only users having a dedicated role are allowed to encrypt
     *     items.
     */
    public java.lang.Boolean getEncryptItems() {
        return encryptItems;
    }

    /**
     * Sets the encryptItems value for this DocumentCreationRequest.
     *
     * @param encryptItems * Indicates whether the content of the items should be stored encrypted.
     *     The encryption is done automatically when the content is inserted into the repository. No
     *     special action is required. Only users having a dedicated role are allowed to encrypt
     *     items.
     */
    public void setEncryptItems(java.lang.Boolean encryptItems) {
        this.encryptItems = encryptItems;
    }

    /**
     * Gets the encryptItemsDeadline value for this DocumentCreationRequest.
     *
     * @return encryptItemsDeadline * Date when the encryption of the docuemnt's items expires.
     */
    public java.util.Date getEncryptItemsDeadline() {
        return encryptItemsDeadline;
    }

    /**
     * Sets the encryptItemsDeadline value for this DocumentCreationRequest.
     *
     * @param encryptItemsDeadline * Date when the encryption of the docuemnt's items expires.
     */
    public void setEncryptItemsDeadline(java.util.Date encryptItemsDeadline) {
        this.encryptItemsDeadline = encryptItemsDeadline;
    }

    /**
     * Gets the comments value for this DocumentCreationRequest.
     *
     * @return comments * Comments
     */
    public java.lang.String getComments() {
        return comments;
    }

    /**
     * Sets the comments value for this DocumentCreationRequest.
     *
     * @param comments * Comments
     */
    public void setComments(java.lang.String comments) {
        this.comments = comments;
    }

    /**
     * Gets the mailType value for this DocumentCreationRequest.
     *
     * @return mailType * Mail type
     */
    public eu.cec.digit.circabc.repo.hrs.ws.MailType getMailType() {
        return mailType;
    }

    /**
     * Sets the mailType value for this DocumentCreationRequest.
     *
     * @param mailType * Mail type
     */
    public void setMailType(eu.cec.digit.circabc.repo.hrs.ws.MailType mailType) {
        this.mailType = mailType;
    }

    /**
     * Gets the senders value for this DocumentCreationRequest.
     *
     * @return senders * Senders of the document
     */
    public eu.cec.digit.circabc.repo.hrs.ws.SendersToAddSender[] getSenders() {
        return senders;
    }

    /**
     * Sets the senders value for this DocumentCreationRequest.
     *
     * @param senders * Senders of the document
     */
    public void setSenders(eu.cec.digit.circabc.repo.hrs.ws.SendersToAddSender[] senders) {
        this.senders = senders;
    }

    /**
     * Gets the recipients value for this DocumentCreationRequest.
     *
     * @return recipients * Recipients of the document
     */
    public eu.cec.digit.circabc.repo.hrs.ws.RecipientsToAddRecipient[] getRecipients() {
        return recipients;
    }

    /**
     * Sets the recipients value for this DocumentCreationRequest.
     *
     * @param recipients * Recipients of the document
     */
    public void setRecipients(
            eu.cec.digit.circabc.repo.hrs.ws.RecipientsToAddRecipient[] recipients) {
        this.recipients = recipients;
    }

    /**
     * Gets the items value for this DocumentCreationRequest.
     *
     * @return items * List of attachments
     */
    public eu.cec.digit.circabc.repo.hrs.ws.ItemsToAdd getItems() {
        return items;
    }

    /**
     * Sets the items value for this DocumentCreationRequest.
     *
     * @param items * List of attachments
     */
    public void setItems(eu.cec.digit.circabc.repo.hrs.ws.ItemsToAdd items) {
        this.items = items;
    }

    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof DocumentCreationRequest)) {
            return false;
        }
        DocumentCreationRequest other = (DocumentCreationRequest) obj;
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
                        && ((this.encryptItems == null && other.getEncryptItems() == null)
                        || (this.encryptItems != null && this.encryptItems.equals(other.getEncryptItems())))
                        && ((this.encryptItemsDeadline == null && other.getEncryptItemsDeadline() == null)
                        || (this.encryptItemsDeadline != null
                        && this.encryptItemsDeadline.equals(other.getEncryptItemsDeadline())))
                        && ((this.comments == null && other.getComments() == null)
                        || (this.comments != null && this.comments.equals(other.getComments())))
                        && ((this.mailType == null && other.getMailType() == null)
                        || (this.mailType != null && this.mailType.equals(other.getMailType())))
                        && ((this.senders == null && other.getSenders() == null)
                        || (this.senders != null
                        && java.util.Arrays.equals(this.senders, other.getSenders())))
                        && ((this.recipients == null && other.getRecipients() == null)
                        || (this.recipients != null
                        && java.util.Arrays.equals(this.recipients, other.getRecipients())))
                        && ((this.items == null && other.getItems() == null)
                        || (this.items != null && this.items.equals(other.getItems())));
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
        if (getSenders() != null) {
            for (int i = 0; i < java.lang.reflect.Array.getLength(getSenders()); i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getSenders(), i);
                if (obj != null && !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getRecipients() != null) {
            for (int i = 0; i < java.lang.reflect.Array.getLength(getRecipients()); i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getRecipients(), i);
                if (obj != null && !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getItems() != null) {
            _hashCode += getItems().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }
}
