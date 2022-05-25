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
 * <p>Document.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 * <p>
 * Document.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
/**
 * Document.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package eu.cec.digit.circabc.repo.hrs.ws;

/** A document */
public class Document implements java.io.Serializable {

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
            new org.apache.axis.description.TypeDesc(Document.class, true);

    static {
        typeDesc.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "Document"));
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
        elemField.setFieldName("title");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "title"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
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
        elemField.setFieldName("encodingDate");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "encodingDate"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "date"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("modificationDate");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "modificationDate"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "dateTime"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("registrationDate");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "registrationDate"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "date"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("saveNumber");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "saveNumber"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("registrationNumber");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "registrationNumber"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("saverEcasId");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "saverEcasId"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("registererEcasId");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "registererEcasId"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("createdOnBehalfOf");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "createdOnBehalfOf"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
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
        elemField.setFieldName("markerType");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "markerType"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "MarkerType"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("markerDeadline");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "markerDeadline"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "date"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("personConcerned");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "personConcerned"));
        elemField.setXmlType(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", ">Document>personConcerned"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("markerService");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "markerService"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
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
        elemField.setFieldName("frozen");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "frozen"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("senders");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "senders"));
        elemField.setXmlType(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", ">>Document>senders>sender"));
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
                        "http://ec.europa.eu/sg/hrs/types", ">>Document>recipients>recipient"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "recipient"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("items");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "items"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "Item"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "item"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("filedIn");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "filedIn"));
        elemField.setXmlType(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", ">>Document>filedIn>file"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "file"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("links");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "links"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "Link"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "link"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("assignmentWorkflow");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "assignmentWorkflow"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "AssignmentWorkflow"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("signatoryWorkflow");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "signatoryWorkflow"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "SignatoryWorkflow"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("procedure");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "procedure"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "Procedure"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("signedBy");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "signedBy"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", ">Document>signedBy"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
    }

    /* Internal repository ID of a document */
    private java.lang.String documentId;
    /* Title/subject of the document */
    private java.lang.String title;
    /* Mail type */
    private eu.cec.digit.circabc.repo.hrs.ws.MailType mailType;
    /* Date of the document */
    private java.util.Date documentDate;
    /* Date at which the document has been sent */
    private java.util.Date sentDate;
    /* Date at which the document has been encoded in the common repository */
    private java.util.Date encodingDate;
    /* Date+Time when the document was last modified. A modification
     * is considered to be any change to the document's metadata, filing,
     * items, senders or recipients. */
    private java.util.Calendar modificationDate;
    /* Date at which the document has been registered in the common
     * repository */
    private java.util.Date registrationDate;
    /* Internal reference of a document */
    private java.lang.String saveNumber;
    /* Official reference of a document */
    private java.lang.String registrationNumber;
    /* ECAS user name of the user that encoded the document in the
     * common repository. If the user was acting on behalf of somebody else
     * (i.e. by using delegations in Ares), this field
     *                         contains the real user, not the on-behalf-of
     * user (e.g. if a secretary acts
     *                         on behalf of a head of unit, this field contains
     * the ecas id of the
     *                         secretary). */
    private java.lang.String saverEcasId;
    /* ECAS user name of the user that registered the document in
     * the
     *                         common repository. If the user was acting
     * on behalf of somebody else, this
     *                         field contains the real user, not the on-behalf-of
     * user (e.g. if a secretary acts
     *                         on behalf of a head of unit, this field contains
     * the ecas id of the
     *                         secretary, not of the head of unit). */
    private java.lang.String registererEcasId;
    /* User name of the user on whose behalf the document was encoded
     * in the
     *                         common repository. For a user that doesn't
     * use a delegation in Ares when creating the document, this
     *                         field will be the same as saverEcasId. For
     * a user that acts on behalf of somebody else,
     *                         the two fields will be different (e.g if a
     * secretary acts in Ares
     *                         on behalf of a head of unit, this field will
     * contain the ecas id of the head of unit). */
    private java.lang.String createdOnBehalfOf;
    /* Comments */
    private java.lang.String comments;
    /* Security classification to be applied to the document */
    private eu.cec.digit.circabc.repo.hrs.ws.SecurityClassification securityClassification;
    /* Marker to be applied to the document according to the security
     * classification */
    private eu.cec.digit.circabc.repo.hrs.ws.MarkerType markerType;
    /* Deadline associated to the marker. The marker will expire at
     * this date. */
    private java.util.Date markerDeadline;
    /* A recipient of the document to which this document is addressed.
     * This person
     *                     is considered the 'person concerned' and can only
     * be specified using a marker.
     *                     This is element is valid if there is a marker
     * of type personal, staff matter, medical matter and personal data.
     *                     It can be an internal or external person entity,
     * not an organization entity. It should not be a virtual entity. */
    private eu.cec.digit.circabc.repo.hrs.ws.DocumentPersonConcerned personConcerned;
    /* The service specified when the LIMITED_SERVICE marking was
     *                         applied (may be missing for older documents). */
    private java.lang.String markerService;
    /* Indicates whether the content of the items is stored encrypted
     * in
     *                         the repository. In case the content is stored
     * encrypted, the
     *                         decryption will be done automatically when
     * downloading the content. */
    private java.lang.Boolean encryptItems;
    /* Date when the encryption of the document's items expires. */
    private java.util.Date encryptItemsDeadline;
    /* Indicates if the document is frozen.
     *                     A frozen document cannot be updated any more (even
     * if it's not registered).
     *                     A document becomes frozen when it was filed in
     * a file that has been closed. */
    private boolean frozen;
    /* List of senders */
    private eu.cec.digit.circabc.repo.hrs.ws.DocumentSendersSender[] senders;
    /* List of recipients */
    private eu.cec.digit.circabc.repo.hrs.ws.DocumentRecipientsRecipient[] recipients;
    /* List of attachments */
    private eu.cec.digit.circabc.repo.hrs.ws.Item[] items;
    /* List of files where the document has been filed */
    private eu.cec.digit.circabc.repo.hrs.ws.DocumentFiledInFile[] filedIn;
    /* List of linked documents */
    private eu.cec.digit.circabc.repo.hrs.ws.Link[] links;
    /* Assignment workflow assigned to the document */
    private eu.cec.digit.circabc.repo.hrs.ws.AssignmentWorkflow assignmentWorkflow;
    /* Paper Signatory or e-Signatory workflow assigned to the document */
    private eu.cec.digit.circabc.repo.hrs.ws.SignatoryWorkflow signatoryWorkflow;
    /* Procedure linked to the document */
    private eu.cec.digit.circabc.repo.hrs.ws.Procedure procedure;
    /* A list of users that have 'signed' a registered document (i.e.
     * they had a SIGN task in the eSignatory workflow and have closed it).
     * The list is in reverse order compared to the eSignatory order (i.e.
     * the last
     *                         user who signed is first in the list).
     *                         For an unregistered document, the list is
     * empty even if some users have
     *                         already closed their SIGN tasks. */
    private eu.cec.digit.circabc.repo.hrs.ws.DocumentSignedBy[] signedBy;
    private java.lang.Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;

    public Document() {
    }

    public Document(
            java.lang.String documentId,
            java.lang.String title,
            eu.cec.digit.circabc.repo.hrs.ws.MailType mailType,
            java.util.Date documentDate,
            java.util.Date sentDate,
            java.util.Date encodingDate,
            java.util.Calendar modificationDate,
            java.util.Date registrationDate,
            java.lang.String saveNumber,
            java.lang.String registrationNumber,
            java.lang.String saverEcasId,
            java.lang.String registererEcasId,
            java.lang.String createdOnBehalfOf,
            java.lang.String comments,
            eu.cec.digit.circabc.repo.hrs.ws.SecurityClassification securityClassification,
            eu.cec.digit.circabc.repo.hrs.ws.MarkerType markerType,
            java.util.Date markerDeadline,
            eu.cec.digit.circabc.repo.hrs.ws.DocumentPersonConcerned personConcerned,
            java.lang.String markerService,
            java.lang.Boolean encryptItems,
            java.util.Date encryptItemsDeadline,
            boolean frozen,
            eu.cec.digit.circabc.repo.hrs.ws.DocumentSendersSender[] senders,
            eu.cec.digit.circabc.repo.hrs.ws.DocumentRecipientsRecipient[] recipients,
            eu.cec.digit.circabc.repo.hrs.ws.Item[] items,
            eu.cec.digit.circabc.repo.hrs.ws.DocumentFiledInFile[] filedIn,
            eu.cec.digit.circabc.repo.hrs.ws.Link[] links,
            eu.cec.digit.circabc.repo.hrs.ws.AssignmentWorkflow assignmentWorkflow,
            eu.cec.digit.circabc.repo.hrs.ws.SignatoryWorkflow signatoryWorkflow,
            eu.cec.digit.circabc.repo.hrs.ws.Procedure procedure,
            eu.cec.digit.circabc.repo.hrs.ws.DocumentSignedBy[] signedBy) {
        this.documentId = documentId;
        this.title = title;
        this.mailType = mailType;
        this.documentDate = documentDate;
        this.sentDate = sentDate;
        this.encodingDate = encodingDate;
        this.modificationDate = modificationDate;
        this.registrationDate = registrationDate;
        this.saveNumber = saveNumber;
        this.registrationNumber = registrationNumber;
        this.saverEcasId = saverEcasId;
        this.registererEcasId = registererEcasId;
        this.createdOnBehalfOf = createdOnBehalfOf;
        this.comments = comments;
        this.securityClassification = securityClassification;
        this.markerType = markerType;
        this.markerDeadline = markerDeadline;
        this.personConcerned = personConcerned;
        this.markerService = markerService;
        this.encryptItems = encryptItems;
        this.encryptItemsDeadline = encryptItemsDeadline;
        this.frozen = frozen;
        this.senders = senders;
        this.recipients = recipients;
        this.items = items;
        this.filedIn = filedIn;
        this.links = links;
        this.assignmentWorkflow = assignmentWorkflow;
        this.signatoryWorkflow = signatoryWorkflow;
        this.procedure = procedure;
        this.signedBy = signedBy;
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
     * Gets the documentId value for this Document.
     *
     * @return documentId * Internal repository ID of a document
     */
    public java.lang.String getDocumentId() {
        return documentId;
    }

    /**
     * Sets the documentId value for this Document.
     *
     * @param documentId * Internal repository ID of a document
     */
    public void setDocumentId(java.lang.String documentId) {
        this.documentId = documentId;
    }

    /**
     * Gets the title value for this Document.
     *
     * @return title * Title/subject of the document
     */
    public java.lang.String getTitle() {
        return title;
    }

    /**
     * Sets the title value for this Document.
     *
     * @param title * Title/subject of the document
     */
    public void setTitle(java.lang.String title) {
        this.title = title;
    }

    /**
     * Gets the mailType value for this Document.
     *
     * @return mailType * Mail type
     */
    public eu.cec.digit.circabc.repo.hrs.ws.MailType getMailType() {
        return mailType;
    }

    /**
     * Sets the mailType value for this Document.
     *
     * @param mailType * Mail type
     */
    public void setMailType(eu.cec.digit.circabc.repo.hrs.ws.MailType mailType) {
        this.mailType = mailType;
    }

    /**
     * Gets the documentDate value for this Document.
     *
     * @return documentDate * Date of the document
     */
    public java.util.Date getDocumentDate() {
        return documentDate;
    }

    /**
     * Sets the documentDate value for this Document.
     *
     * @param documentDate * Date of the document
     */
    public void setDocumentDate(java.util.Date documentDate) {
        this.documentDate = documentDate;
    }

    /**
     * Gets the sentDate value for this Document.
     *
     * @return sentDate * Date at which the document has been sent
     */
    public java.util.Date getSentDate() {
        return sentDate;
    }

    /**
     * Sets the sentDate value for this Document.
     *
     * @param sentDate * Date at which the document has been sent
     */
    public void setSentDate(java.util.Date sentDate) {
        this.sentDate = sentDate;
    }

    /**
     * Gets the encodingDate value for this Document.
     *
     * @return encodingDate * Date at which the document has been encoded in the common repository
     */
    public java.util.Date getEncodingDate() {
        return encodingDate;
    }

    /**
     * Sets the encodingDate value for this Document.
     *
     * @param encodingDate * Date at which the document has been encoded in the common repository
     */
    public void setEncodingDate(java.util.Date encodingDate) {
        this.encodingDate = encodingDate;
    }

    /**
     * Gets the modificationDate value for this Document.
     *
     * @return modificationDate * Date+Time when the document was last modified. A modification is
     *     considered to be any change to the document's metadata, filing, items, senders or
     *     recipients.
     */
    public java.util.Calendar getModificationDate() {
        return modificationDate;
    }

    /**
     * Sets the modificationDate value for this Document.
     *
     * @param modificationDate * Date+Time when the document was last modified. A modification is
     *     considered to be any change to the document's metadata, filing, items, senders or
     *     recipients.
     */
    public void setModificationDate(java.util.Calendar modificationDate) {
        this.modificationDate = modificationDate;
    }

    /**
     * Gets the registrationDate value for this Document.
     *
     * @return registrationDate * Date at which the document has been registered in the common
     *     repository
     */
    public java.util.Date getRegistrationDate() {
        return registrationDate;
    }

    /**
     * Sets the registrationDate value for this Document.
     *
     * @param registrationDate * Date at which the document has been registered in the common
     *     repository
     */
    public void setRegistrationDate(java.util.Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    /**
     * Gets the saveNumber value for this Document.
     *
     * @return saveNumber * Internal reference of a document
     */
    public java.lang.String getSaveNumber() {
        return saveNumber;
    }

    /**
     * Sets the saveNumber value for this Document.
     *
     * @param saveNumber * Internal reference of a document
     */
    public void setSaveNumber(java.lang.String saveNumber) {
        this.saveNumber = saveNumber;
    }

    /**
     * Gets the registrationNumber value for this Document.
     *
     * @return registrationNumber * Official reference of a document
     */
    public java.lang.String getRegistrationNumber() {
        return registrationNumber;
    }

    /**
     * Sets the registrationNumber value for this Document.
     *
     * @param registrationNumber * Official reference of a document
     */
    public void setRegistrationNumber(java.lang.String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    /**
     * Gets the saverEcasId value for this Document.
     *
     * @return saverEcasId * ECAS user name of the user that encoded the document in the common
     *     repository. If the user was acting on behalf of somebody else (i.e. by using delegations in
     *     Ares), this field contains the real user, not the on-behalf-of user (e.g. if a secretary
     *     acts on behalf of a head of unit, this field contains the ecas id of the secretary).
     */
    public java.lang.String getSaverEcasId() {
        return saverEcasId;
    }

    /**
     * Sets the saverEcasId value for this Document.
     *
     * @param saverEcasId * ECAS user name of the user that encoded the document in the common
     *     repository. If the user was acting on behalf of somebody else (i.e. by using delegations in
     *     Ares), this field contains the real user, not the on-behalf-of user (e.g. if a secretary
     *     acts on behalf of a head of unit, this field contains the ecas id of the secretary).
     */
    public void setSaverEcasId(java.lang.String saverEcasId) {
        this.saverEcasId = saverEcasId;
    }

    /**
     * Gets the registererEcasId value for this Document.
     *
     * @return registererEcasId * ECAS user name of the user that registered the document in the
     *     common repository. If the user was acting on behalf of somebody else, this field contains
     *     the real user, not the on-behalf-of user (e.g. if a secretary acts on behalf of a head of
     *     unit, this field contains the ecas id of the secretary, not of the head of unit).
     */
    public java.lang.String getRegistererEcasId() {
        return registererEcasId;
    }

    /**
     * Sets the registererEcasId value for this Document.
     *
     * @param registererEcasId * ECAS user name of the user that registered the document in the common
     *     repository. If the user was acting on behalf of somebody else, this field contains the real
     *     user, not the on-behalf-of user (e.g. if a secretary acts on behalf of a head of unit, this
     *     field contains the ecas id of the secretary, not of the head of unit).
     */
    public void setRegistererEcasId(java.lang.String registererEcasId) {
        this.registererEcasId = registererEcasId;
    }

    /**
     * Gets the createdOnBehalfOf value for this Document.
     *
     * @return createdOnBehalfOf * User name of the user on whose behalf the document was encoded in
     *     the common repository. For a user that doesn't use a delegation in Ares when creating the
     *     document, this field will be the same as saverEcasId. For a user that acts on behalf of
     *     somebody else, the two fields will be different (e.g if a secretary acts in Ares on behalf
     *     of a head of unit, this field will contain the ecas id of the head of unit).
     */
    public java.lang.String getCreatedOnBehalfOf() {
        return createdOnBehalfOf;
    }

    /**
     * Sets the createdOnBehalfOf value for this Document.
     *
     * @param createdOnBehalfOf * User name of the user on whose behalf the document was encoded in
     *     the common repository. For a user that doesn't use a delegation in Ares when creating the
     *     document, this field will be the same as saverEcasId. For a user that acts on behalf of
     *     somebody else, the two fields will be different (e.g if a secretary acts in Ares on behalf
     *     of a head of unit, this field will contain the ecas id of the head of unit).
     */
    public void setCreatedOnBehalfOf(java.lang.String createdOnBehalfOf) {
        this.createdOnBehalfOf = createdOnBehalfOf;
    }

    /**
     * Gets the comments value for this Document.
     *
     * @return comments * Comments
     */
    public java.lang.String getComments() {
        return comments;
    }

    /**
     * Sets the comments value for this Document.
     *
     * @param comments * Comments
     */
    public void setComments(java.lang.String comments) {
        this.comments = comments;
    }

    /**
     * Gets the securityClassification value for this Document.
     *
     * @return securityClassification * Security classification to be applied to the document
     */
    public eu.cec.digit.circabc.repo.hrs.ws.SecurityClassification getSecurityClassification() {
        return securityClassification;
    }

    /**
     * Sets the securityClassification value for this Document.
     *
     * @param securityClassification * Security classification to be applied to the document
     */
    public void setSecurityClassification(
            eu.cec.digit.circabc.repo.hrs.ws.SecurityClassification securityClassification) {
        this.securityClassification = securityClassification;
    }

    /**
     * Gets the markerType value for this Document.
     *
     * @return markerType * Marker to be applied to the document according to the security
     *     classification
     */
    public eu.cec.digit.circabc.repo.hrs.ws.MarkerType getMarkerType() {
        return markerType;
    }

    /**
     * Sets the markerType value for this Document.
     *
     * @param markerType * Marker to be applied to the document according to the security
     *     classification
     */
    public void setMarkerType(eu.cec.digit.circabc.repo.hrs.ws.MarkerType markerType) {
        this.markerType = markerType;
    }

    /**
     * Gets the markerDeadline value for this Document.
     *
     * @return markerDeadline * Deadline associated to the marker. The marker will expire at this
     *     date.
     */
    public java.util.Date getMarkerDeadline() {
        return markerDeadline;
    }

    /**
     * Sets the markerDeadline value for this Document.
     *
     * @param markerDeadline * Deadline associated to the marker. The marker will expire at this date.
     */
    public void setMarkerDeadline(java.util.Date markerDeadline) {
        this.markerDeadline = markerDeadline;
    }

    /**
     * Gets the personConcerned value for this Document.
     *
     * @return personConcerned * A recipient of the document to which this document is addressed. This
     *     person is considered the 'person concerned' and can only be specified using a marker. This
     *     is element is valid if there is a marker of type personal, staff matter, medical matter and
     *     personal data. It can be an internal or external person entity, not an organization entity.
     *     It should not be a virtual entity.
     */
    public eu.cec.digit.circabc.repo.hrs.ws.DocumentPersonConcerned getPersonConcerned() {
        return personConcerned;
    }

    /**
     * Sets the personConcerned value for this Document.
     *
     * @param personConcerned * A recipient of the document to which this document is addressed. This
     *     person is considered the 'person concerned' and can only be specified using a marker. This
     *     is element is valid if there is a marker of type personal, staff matter, medical matter and
     *     personal data. It can be an internal or external person entity, not an organization entity.
     *     It should not be a virtual entity.
     */
    public void setPersonConcerned(
            eu.cec.digit.circabc.repo.hrs.ws.DocumentPersonConcerned personConcerned) {
        this.personConcerned = personConcerned;
    }

    /**
     * Gets the markerService value for this Document.
     *
     * @return markerService * The service specified when the LIMITED_SERVICE marking was applied (may
     *     be missing for older documents).
     */
    public java.lang.String getMarkerService() {
        return markerService;
    }

    /**
     * Sets the markerService value for this Document.
     *
     * @param markerService * The service specified when the LIMITED_SERVICE marking was applied (may
     *     be missing for older documents).
     */
    public void setMarkerService(java.lang.String markerService) {
        this.markerService = markerService;
    }

    /**
     * Gets the encryptItems value for this Document.
     *
     * @return encryptItems * Indicates whether the content of the items is stored encrypted in the
     *     repository. In case the content is stored encrypted, the decryption will be done
     *     automatically when downloading the content.
     */
    public java.lang.Boolean getEncryptItems() {
        return encryptItems;
    }

    /**
     * Sets the encryptItems value for this Document.
     *
     * @param encryptItems * Indicates whether the content of the items is stored encrypted in the
     *     repository. In case the content is stored encrypted, the decryption will be done
     *     automatically when downloading the content.
     */
    public void setEncryptItems(java.lang.Boolean encryptItems) {
        this.encryptItems = encryptItems;
    }

    /**
     * Gets the encryptItemsDeadline value for this Document.
     *
     * @return encryptItemsDeadline * Date when the encryption of the document's items expires.
     */
    public java.util.Date getEncryptItemsDeadline() {
        return encryptItemsDeadline;
    }

    /**
     * Sets the encryptItemsDeadline value for this Document.
     *
     * @param encryptItemsDeadline * Date when the encryption of the document's items expires.
     */
    public void setEncryptItemsDeadline(java.util.Date encryptItemsDeadline) {
        this.encryptItemsDeadline = encryptItemsDeadline;
    }

    /**
     * Gets the frozen value for this Document.
     *
     * @return frozen * Indicates if the document is frozen. A frozen document cannot be updated any
     *     more (even if it's not registered). A document becomes frozen when it was filed in a file
     *     that has been closed.
     */
    public boolean isFrozen() {
        return frozen;
    }

    /**
     * Sets the frozen value for this Document.
     *
     * @param frozen * Indicates if the document is frozen. A frozen document cannot be updated any
     *     more (even if it's not registered). A document becomes frozen when it was filed in a file
     *     that has been closed.
     */
    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
    }

    /**
     * Gets the senders value for this Document.
     *
     * @return senders * List of senders
     */
    public eu.cec.digit.circabc.repo.hrs.ws.DocumentSendersSender[] getSenders() {
        return senders;
    }

    /**
     * Sets the senders value for this Document.
     *
     * @param senders * List of senders
     */
    public void setSenders(eu.cec.digit.circabc.repo.hrs.ws.DocumentSendersSender[] senders) {
        this.senders = senders;
    }

    /**
     * Gets the recipients value for this Document.
     *
     * @return recipients * List of recipients
     */
    public eu.cec.digit.circabc.repo.hrs.ws.DocumentRecipientsRecipient[] getRecipients() {
        return recipients;
    }

    /**
     * Sets the recipients value for this Document.
     *
     * @param recipients * List of recipients
     */
    public void setRecipients(
            eu.cec.digit.circabc.repo.hrs.ws.DocumentRecipientsRecipient[] recipients) {
        this.recipients = recipients;
    }

    /**
     * Gets the items value for this Document.
     *
     * @return items * List of attachments
     */
    public eu.cec.digit.circabc.repo.hrs.ws.Item[] getItems() {
        return items;
    }

    /**
     * Sets the items value for this Document.
     *
     * @param items * List of attachments
     */
    public void setItems(eu.cec.digit.circabc.repo.hrs.ws.Item[] items) {
        this.items = items;
    }

    /**
     * Gets the filedIn value for this Document.
     *
     * @return filedIn * List of files where the document has been filed
     */
    public eu.cec.digit.circabc.repo.hrs.ws.DocumentFiledInFile[] getFiledIn() {
        return filedIn;
    }

    /**
     * Sets the filedIn value for this Document.
     *
     * @param filedIn * List of files where the document has been filed
     */
    public void setFiledIn(eu.cec.digit.circabc.repo.hrs.ws.DocumentFiledInFile[] filedIn) {
        this.filedIn = filedIn;
    }

    /**
     * Gets the links value for this Document.
     *
     * @return links * List of linked documents
     */
    public eu.cec.digit.circabc.repo.hrs.ws.Link[] getLinks() {
        return links;
    }

    /**
     * Sets the links value for this Document.
     *
     * @param links * List of linked documents
     */
    public void setLinks(eu.cec.digit.circabc.repo.hrs.ws.Link[] links) {
        this.links = links;
    }

    /**
     * Gets the assignmentWorkflow value for this Document.
     *
     * @return assignmentWorkflow * Assignment workflow assigned to the document
     */
    public eu.cec.digit.circabc.repo.hrs.ws.AssignmentWorkflow getAssignmentWorkflow() {
        return assignmentWorkflow;
    }

    /**
     * Sets the assignmentWorkflow value for this Document.
     *
     * @param assignmentWorkflow * Assignment workflow assigned to the document
     */
    public void setAssignmentWorkflow(
            eu.cec.digit.circabc.repo.hrs.ws.AssignmentWorkflow assignmentWorkflow) {
        this.assignmentWorkflow = assignmentWorkflow;
    }

    /**
     * Gets the signatoryWorkflow value for this Document.
     *
     * @return signatoryWorkflow * Paper Signatory or e-Signatory workflow assigned to the document
     */
    public eu.cec.digit.circabc.repo.hrs.ws.SignatoryWorkflow getSignatoryWorkflow() {
        return signatoryWorkflow;
    }

    /**
     * Sets the signatoryWorkflow value for this Document.
     *
     * @param signatoryWorkflow * Paper Signatory or e-Signatory workflow assigned to the document
     */
    public void setSignatoryWorkflow(
            eu.cec.digit.circabc.repo.hrs.ws.SignatoryWorkflow signatoryWorkflow) {
        this.signatoryWorkflow = signatoryWorkflow;
    }

    /**
     * Gets the procedure value for this Document.
     *
     * @return procedure * Procedure linked to the document
     */
    public eu.cec.digit.circabc.repo.hrs.ws.Procedure getProcedure() {
        return procedure;
    }

    /**
     * Sets the procedure value for this Document.
     *
     * @param procedure * Procedure linked to the document
     */
    public void setProcedure(eu.cec.digit.circabc.repo.hrs.ws.Procedure procedure) {
        this.procedure = procedure;
    }

    /**
     * Gets the signedBy value for this Document.
     *
     * @return signedBy * A list of users that have 'signed' a registered document (i.e. they had a
     *     SIGN task in the eSignatory workflow and have closed it). The list is in reverse order
     *     compared to the eSignatory order (i.e. the last user who signed is first in the list). For
     *     an unregistered document, the list is empty even if some users have already closed their
     *     SIGN tasks.
     */
    public eu.cec.digit.circabc.repo.hrs.ws.DocumentSignedBy[] getSignedBy() {
        return signedBy;
    }

    /**
     * Sets the signedBy value for this Document.
     *
     * @param signedBy * A list of users that have 'signed' a registered document (i.e. they had a
     *     SIGN task in the eSignatory workflow and have closed it). The list is in reverse order
     *     compared to the eSignatory order (i.e. the last user who signed is first in the list). For
     *     an unregistered document, the list is empty even if some users have already closed their
     *     SIGN tasks.
     */
    public void setSignedBy(eu.cec.digit.circabc.repo.hrs.ws.DocumentSignedBy[] signedBy) {
        this.signedBy = signedBy;
    }

    public eu.cec.digit.circabc.repo.hrs.ws.DocumentSignedBy getSignedBy(int i) {
        return this.signedBy[i];
    }

    public void setSignedBy(int i, eu.cec.digit.circabc.repo.hrs.ws.DocumentSignedBy _value) {
        this.signedBy[i] = _value;
    }

    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Document)) {
            return false;
        }
        Document other = (Document) obj;
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
                        && ((this.title == null && other.getTitle() == null)
                        || (this.title != null && this.title.equals(other.getTitle())))
                        && ((this.mailType == null && other.getMailType() == null)
                        || (this.mailType != null && this.mailType.equals(other.getMailType())))
                        && ((this.documentDate == null && other.getDocumentDate() == null)
                        || (this.documentDate != null && this.documentDate.equals(other.getDocumentDate())))
                        && ((this.sentDate == null && other.getSentDate() == null)
                        || (this.sentDate != null && this.sentDate.equals(other.getSentDate())))
                        && ((this.encodingDate == null && other.getEncodingDate() == null)
                        || (this.encodingDate != null && this.encodingDate.equals(other.getEncodingDate())))
                        && ((this.modificationDate == null && other.getModificationDate() == null)
                        || (this.modificationDate != null
                        && this.modificationDate.equals(other.getModificationDate())))
                        && ((this.registrationDate == null && other.getRegistrationDate() == null)
                        || (this.registrationDate != null
                        && this.registrationDate.equals(other.getRegistrationDate())))
                        && ((this.saveNumber == null && other.getSaveNumber() == null)
                        || (this.saveNumber != null && this.saveNumber.equals(other.getSaveNumber())))
                        && ((this.registrationNumber == null && other.getRegistrationNumber() == null)
                        || (this.registrationNumber != null
                        && this.registrationNumber.equals(other.getRegistrationNumber())))
                        && ((this.saverEcasId == null && other.getSaverEcasId() == null)
                        || (this.saverEcasId != null && this.saverEcasId.equals(other.getSaverEcasId())))
                        && ((this.registererEcasId == null && other.getRegistererEcasId() == null)
                        || (this.registererEcasId != null
                        && this.registererEcasId.equals(other.getRegistererEcasId())))
                        && ((this.createdOnBehalfOf == null && other.getCreatedOnBehalfOf() == null)
                        || (this.createdOnBehalfOf != null
                        && this.createdOnBehalfOf.equals(other.getCreatedOnBehalfOf())))
                        && ((this.comments == null && other.getComments() == null)
                        || (this.comments != null && this.comments.equals(other.getComments())))
                        && ((this.securityClassification == null && other.getSecurityClassification() == null)
                        || (this.securityClassification != null
                        && this.securityClassification.equals(other.getSecurityClassification())))
                        && ((this.markerType == null && other.getMarkerType() == null)
                        || (this.markerType != null && this.markerType.equals(other.getMarkerType())))
                        && ((this.markerDeadline == null && other.getMarkerDeadline() == null)
                        || (this.markerDeadline != null
                        && this.markerDeadline.equals(other.getMarkerDeadline())))
                        && ((this.personConcerned == null && other.getPersonConcerned() == null)
                        || (this.personConcerned != null
                        && this.personConcerned.equals(other.getPersonConcerned())))
                        && ((this.markerService == null && other.getMarkerService() == null)
                        || (this.markerService != null
                        && this.markerService.equals(other.getMarkerService())))
                        && ((this.encryptItems == null && other.getEncryptItems() == null)
                        || (this.encryptItems != null && this.encryptItems.equals(other.getEncryptItems())))
                        && ((this.encryptItemsDeadline == null && other.getEncryptItemsDeadline() == null)
                        || (this.encryptItemsDeadline != null
                        && this.encryptItemsDeadline.equals(other.getEncryptItemsDeadline())))
                        && this.frozen == other.isFrozen()
                        && ((this.senders == null && other.getSenders() == null)
                        || (this.senders != null
                        && java.util.Arrays.equals(this.senders, other.getSenders())))
                        && ((this.recipients == null && other.getRecipients() == null)
                        || (this.recipients != null
                        && java.util.Arrays.equals(this.recipients, other.getRecipients())))
                        && ((this.items == null && other.getItems() == null)
                        || (this.items != null && java.util.Arrays.equals(this.items, other.getItems())))
                        && ((this.filedIn == null && other.getFiledIn() == null)
                        || (this.filedIn != null
                        && java.util.Arrays.equals(this.filedIn, other.getFiledIn())))
                        && ((this.links == null && other.getLinks() == null)
                        || (this.links != null && java.util.Arrays.equals(this.links, other.getLinks())))
                        && ((this.assignmentWorkflow == null && other.getAssignmentWorkflow() == null)
                        || (this.assignmentWorkflow != null
                        && this.assignmentWorkflow.equals(other.getAssignmentWorkflow())))
                        && ((this.signatoryWorkflow == null && other.getSignatoryWorkflow() == null)
                        || (this.signatoryWorkflow != null
                        && this.signatoryWorkflow.equals(other.getSignatoryWorkflow())))
                        && ((this.procedure == null && other.getProcedure() == null)
                        || (this.procedure != null && this.procedure.equals(other.getProcedure())))
                        && ((this.signedBy == null && other.getSignedBy() == null)
                        || (this.signedBy != null
                        && java.util.Arrays.equals(this.signedBy, other.getSignedBy())));
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
        if (getTitle() != null) {
            _hashCode += getTitle().hashCode();
        }
        if (getMailType() != null) {
            _hashCode += getMailType().hashCode();
        }
        if (getDocumentDate() != null) {
            _hashCode += getDocumentDate().hashCode();
        }
        if (getSentDate() != null) {
            _hashCode += getSentDate().hashCode();
        }
        if (getEncodingDate() != null) {
            _hashCode += getEncodingDate().hashCode();
        }
        if (getModificationDate() != null) {
            _hashCode += getModificationDate().hashCode();
        }
        if (getRegistrationDate() != null) {
            _hashCode += getRegistrationDate().hashCode();
        }
        if (getSaveNumber() != null) {
            _hashCode += getSaveNumber().hashCode();
        }
        if (getRegistrationNumber() != null) {
            _hashCode += getRegistrationNumber().hashCode();
        }
        if (getSaverEcasId() != null) {
            _hashCode += getSaverEcasId().hashCode();
        }
        if (getRegistererEcasId() != null) {
            _hashCode += getRegistererEcasId().hashCode();
        }
        if (getCreatedOnBehalfOf() != null) {
            _hashCode += getCreatedOnBehalfOf().hashCode();
        }
        if (getComments() != null) {
            _hashCode += getComments().hashCode();
        }
        if (getSecurityClassification() != null) {
            _hashCode += getSecurityClassification().hashCode();
        }
        if (getMarkerType() != null) {
            _hashCode += getMarkerType().hashCode();
        }
        if (getMarkerDeadline() != null) {
            _hashCode += getMarkerDeadline().hashCode();
        }
        if (getPersonConcerned() != null) {
            _hashCode += getPersonConcerned().hashCode();
        }
        if (getMarkerService() != null) {
            _hashCode += getMarkerService().hashCode();
        }
        if (getEncryptItems() != null) {
            _hashCode += getEncryptItems().hashCode();
        }
        if (getEncryptItemsDeadline() != null) {
            _hashCode += getEncryptItemsDeadline().hashCode();
        }
        _hashCode += (isFrozen() ? Boolean.TRUE : Boolean.FALSE).hashCode();
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
            for (int i = 0; i < java.lang.reflect.Array.getLength(getItems()); i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getItems(), i);
                if (obj != null && !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getFiledIn() != null) {
            for (int i = 0; i < java.lang.reflect.Array.getLength(getFiledIn()); i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getFiledIn(), i);
                if (obj != null && !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getLinks() != null) {
            for (int i = 0; i < java.lang.reflect.Array.getLength(getLinks()); i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getLinks(), i);
                if (obj != null && !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getAssignmentWorkflow() != null) {
            _hashCode += getAssignmentWorkflow().hashCode();
        }
        if (getSignatoryWorkflow() != null) {
            _hashCode += getSignatoryWorkflow().hashCode();
        }
        if (getProcedure() != null) {
            _hashCode += getProcedure().hashCode();
        }
        if (getSignedBy() != null) {
            for (int i = 0; i < java.lang.reflect.Array.getLength(getSignedBy()); i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getSignedBy(), i);
                if (obj != null && !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        __hashCodeCalc = false;
        return _hashCode;
    }
}
