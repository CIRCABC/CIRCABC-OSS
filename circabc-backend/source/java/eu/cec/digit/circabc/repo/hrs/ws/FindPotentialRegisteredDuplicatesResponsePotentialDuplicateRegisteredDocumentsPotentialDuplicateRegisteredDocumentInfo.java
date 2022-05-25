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
 * <p>FindPotentialRegisteredDuplicatesResponsePotentialDuplicateRegisteredDocumentsPotentialDuplicateRegisteredDocumentInfo.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 * <p>
 * FindPotentialRegisteredDuplicatesResponsePotentialDuplicateRegisteredDocumentsPotentialDuplicateRegisteredDocumentInfo.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
/**
 * FindPotentialRegisteredDuplicatesResponsePotentialDuplicateRegisteredDocumentsPotentialDuplicateRegisteredDocumentInfo.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package eu.cec.digit.circabc.repo.hrs.ws;

public
class FindPotentialRegisteredDuplicatesResponsePotentialDuplicateRegisteredDocumentsPotentialDuplicateRegisteredDocumentInfo
        implements java.io.Serializable {

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
            new org.apache.axis.description.TypeDesc(
                    FindPotentialRegisteredDuplicatesResponsePotentialDuplicateRegisteredDocumentsPotentialDuplicateRegisteredDocumentInfo
                            .class,
                    true);

    static {
        typeDesc.setXmlType(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types",
                        ">>>findPotentialRegisteredDuplicatesResponse>potentialDuplicateRegisteredDocuments>potentialDuplicateRegisteredDocumentInfo"));
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
        elemField.setFieldName("registrationDate");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "registrationDate"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "date"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("registrationNumber");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "registrationNumber"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("registrationAuthor");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "registrationAuthor"));
        elemField.setXmlType(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types",
                        ">>>>findPotentialRegisteredDuplicatesResponse>potentialDuplicateRegisteredDocuments>potentialDuplicateRegisteredDocumentInfo>registrationAuthor"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("visible");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "visible"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("score");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "score"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "decimal"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /* Repository ID that uniquely identifies the document */
    private java.lang.String documentId;
    /* Date the potential duplicate was registered */
    private java.util.Date registrationDate;
    /* Unique registration number of the document */
    private java.lang.String registrationNumber;
    /* Registration author of the document */
    private eu.cec.digit.circabc.repo.hrs.ws
            .FindPotentialRegisteredDuplicatesResponsePotentialDuplicateRegisteredDocumentsPotentialDuplicateRegisteredDocumentInfoRegistrationAuthor
            registrationAuthor;
    /* Indicates if the document is visible to the operating user.
     * If the document is visible to the operating user, the user may retrieve
     * the document
     *                                                 and all the information
     * of it to find out if it's a real duplicate.
     *                                                 If the document is
     * not visible, the user should contact the registration author
     *                                                 to find out if the
     * document to be registered is a duplicate. */
    private boolean visible;
    /* The matching score. A percentage that indicates how much this
     * potential duplicate matches the
     *                                                 document for registration. */
    private java.math.BigDecimal score;
    private java.lang.Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;

    public FindPotentialRegisteredDuplicatesResponsePotentialDuplicateRegisteredDocumentsPotentialDuplicateRegisteredDocumentInfo() {
    }

    public FindPotentialRegisteredDuplicatesResponsePotentialDuplicateRegisteredDocumentsPotentialDuplicateRegisteredDocumentInfo(
            java.lang.String documentId,
            java.util.Date registrationDate,
            java.lang.String registrationNumber,
            eu.cec.digit.circabc.repo.hrs.ws
                    .FindPotentialRegisteredDuplicatesResponsePotentialDuplicateRegisteredDocumentsPotentialDuplicateRegisteredDocumentInfoRegistrationAuthor
                    registrationAuthor,
            boolean visible,
            java.math.BigDecimal score) {
        this.documentId = documentId;
        this.registrationDate = registrationDate;
        this.registrationNumber = registrationNumber;
        this.registrationAuthor = registrationAuthor;
        this.visible = visible;
        this.score = score;
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
     * Gets the documentId value for this
     * FindPotentialRegisteredDuplicatesResponsePotentialDuplicateRegisteredDocumentsPotentialDuplicateRegisteredDocumentInfo.
     *
     * @return documentId * Repository ID that uniquely identifies the document
     */
    public java.lang.String getDocumentId() {
        return documentId;
    }

    /**
     * Sets the documentId value for this
     * FindPotentialRegisteredDuplicatesResponsePotentialDuplicateRegisteredDocumentsPotentialDuplicateRegisteredDocumentInfo.
     *
     * @param documentId * Repository ID that uniquely identifies the document
     */
    public void setDocumentId(java.lang.String documentId) {
        this.documentId = documentId;
    }

    /**
     * Gets the registrationDate value for this
     * FindPotentialRegisteredDuplicatesResponsePotentialDuplicateRegisteredDocumentsPotentialDuplicateRegisteredDocumentInfo.
     *
     * @return registrationDate * Date the potential duplicate was registered
     */
    public java.util.Date getRegistrationDate() {
        return registrationDate;
    }

    /**
     * Sets the registrationDate value for this
     * FindPotentialRegisteredDuplicatesResponsePotentialDuplicateRegisteredDocumentsPotentialDuplicateRegisteredDocumentInfo.
     *
     * @param registrationDate * Date the potential duplicate was registered
     */
    public void setRegistrationDate(java.util.Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    /**
     * Gets the registrationNumber value for this
     * FindPotentialRegisteredDuplicatesResponsePotentialDuplicateRegisteredDocumentsPotentialDuplicateRegisteredDocumentInfo.
     *
     * @return registrationNumber * Unique registration number of the document
     */
    public java.lang.String getRegistrationNumber() {
        return registrationNumber;
    }

    /**
     * Sets the registrationNumber value for this
     * FindPotentialRegisteredDuplicatesResponsePotentialDuplicateRegisteredDocumentsPotentialDuplicateRegisteredDocumentInfo.
     *
     * @param registrationNumber * Unique registration number of the document
     */
    public void setRegistrationNumber(java.lang.String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    /**
     * Gets the registrationAuthor value for this
     * FindPotentialRegisteredDuplicatesResponsePotentialDuplicateRegisteredDocumentsPotentialDuplicateRegisteredDocumentInfo.
     *
     * @return registrationAuthor * Registration author of the document
     */
    public eu.cec.digit.circabc.repo.hrs.ws
            .FindPotentialRegisteredDuplicatesResponsePotentialDuplicateRegisteredDocumentsPotentialDuplicateRegisteredDocumentInfoRegistrationAuthor
    getRegistrationAuthor() {
        return registrationAuthor;
    }

    /**
     * Sets the registrationAuthor value for this
     * FindPotentialRegisteredDuplicatesResponsePotentialDuplicateRegisteredDocumentsPotentialDuplicateRegisteredDocumentInfo.
     *
     * @param registrationAuthor * Registration author of the document
     */
    public void setRegistrationAuthor(
            eu.cec.digit.circabc.repo.hrs.ws
                    .FindPotentialRegisteredDuplicatesResponsePotentialDuplicateRegisteredDocumentsPotentialDuplicateRegisteredDocumentInfoRegistrationAuthor
                    registrationAuthor) {
        this.registrationAuthor = registrationAuthor;
    }

    /**
     * Gets the visible value for this
     * FindPotentialRegisteredDuplicatesResponsePotentialDuplicateRegisteredDocumentsPotentialDuplicateRegisteredDocumentInfo.
     *
     * @return visible * Indicates if the document is visible to the operating user. If the document
     *     is visible to the operating user, the user may retrieve the document and all the
     *     information of it to find out if it's a real duplicate. If the document is not visible, the
     *     user should contact the registration author to find out if the document to be registered is
     *     a duplicate.
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Sets the visible value for this
     * FindPotentialRegisteredDuplicatesResponsePotentialDuplicateRegisteredDocumentsPotentialDuplicateRegisteredDocumentInfo.
     *
     * @param visible * Indicates if the document is visible to the operating user. If the document is
     *     visible to the operating user, the user may retrieve the document and all the information
     *     of it to find out if it's a real duplicate. If the document is not visible, the user should
     *     contact the registration author to find out if the document to be registered is a
     *     duplicate.
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * Gets the score value for this
     * FindPotentialRegisteredDuplicatesResponsePotentialDuplicateRegisteredDocumentsPotentialDuplicateRegisteredDocumentInfo.
     *
     * @return score * The matching score. A percentage that indicates how much this potential
     *     duplicate matches the document for registration.
     */
    public java.math.BigDecimal getScore() {
        return score;
    }

    /**
     * Sets the score value for this
     * FindPotentialRegisteredDuplicatesResponsePotentialDuplicateRegisteredDocumentsPotentialDuplicateRegisteredDocumentInfo.
     *
     * @param score * The matching score. A percentage that indicates how much this potential
     *     duplicate matches the document for registration.
     */
    public void setScore(java.math.BigDecimal score) {
        this.score = score;
    }

    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj
                instanceof
                FindPotentialRegisteredDuplicatesResponsePotentialDuplicateRegisteredDocumentsPotentialDuplicateRegisteredDocumentInfo)) {
            return false;
        }
        FindPotentialRegisteredDuplicatesResponsePotentialDuplicateRegisteredDocumentsPotentialDuplicateRegisteredDocumentInfo
                other =
                (FindPotentialRegisteredDuplicatesResponsePotentialDuplicateRegisteredDocumentsPotentialDuplicateRegisteredDocumentInfo)
                        obj;
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
                        && ((this.registrationDate == null && other.getRegistrationDate() == null)
                        || (this.registrationDate != null
                        && this.registrationDate.equals(other.getRegistrationDate())))
                        && ((this.registrationNumber == null && other.getRegistrationNumber() == null)
                        || (this.registrationNumber != null
                        && this.registrationNumber.equals(other.getRegistrationNumber())))
                        && ((this.registrationAuthor == null && other.getRegistrationAuthor() == null)
                        || (this.registrationAuthor != null
                        && this.registrationAuthor.equals(other.getRegistrationAuthor())))
                        && this.visible == other.isVisible()
                        && ((this.score == null && other.getScore() == null)
                        || (this.score != null && this.score.equals(other.getScore())));
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
        if (getRegistrationDate() != null) {
            _hashCode += getRegistrationDate().hashCode();
        }
        if (getRegistrationNumber() != null) {
            _hashCode += getRegistrationNumber().hashCode();
        }
        if (getRegistrationAuthor() != null) {
            _hashCode += getRegistrationAuthor().hashCode();
        }
        _hashCode += (isVisible() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        if (getScore() != null) {
            _hashCode += getScore().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }
}
