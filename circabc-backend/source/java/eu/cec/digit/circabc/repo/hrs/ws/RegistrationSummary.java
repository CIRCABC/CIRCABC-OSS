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
 * <p>RegistrationSummary.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 * <p>
 * RegistrationSummary.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
/**
 * RegistrationSummary.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package eu.cec.digit.circabc.repo.hrs.ws;

public class RegistrationSummary implements java.io.Serializable {

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
            new org.apache.axis.description.TypeDesc(RegistrationSummary.class, true);

    static {
        typeDesc.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "RegistrationSummary"));
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
        elemField.setFieldName("encodingDate");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "encodingDate"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "date"));
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
        elemField.setFieldName("saveNumber");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "saveNumber"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /* Internal repository ID of a document */
    private java.lang.String documentId;
    /* Date at which the document has been encoded in the common repository */
    private java.util.Date encodingDate;
    /* Date at which the document has been registered */
    private java.util.Date registrationDate;
    /* Official document number */
    private java.lang.String registrationNumber;
    /* Internal document number */
    private java.lang.String saveNumber;
    private java.lang.Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;

    public RegistrationSummary() {
    }

    public RegistrationSummary(
            java.lang.String documentId,
            java.util.Date encodingDate,
            java.util.Date registrationDate,
            java.lang.String registrationNumber,
            java.lang.String saveNumber) {
        this.documentId = documentId;
        this.encodingDate = encodingDate;
        this.registrationDate = registrationDate;
        this.registrationNumber = registrationNumber;
        this.saveNumber = saveNumber;
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
     * Gets the documentId value for this RegistrationSummary.
     *
     * @return documentId * Internal repository ID of a document
     */
    public java.lang.String getDocumentId() {
        return documentId;
    }

    /**
     * Sets the documentId value for this RegistrationSummary.
     *
     * @param documentId * Internal repository ID of a document
     */
    public void setDocumentId(java.lang.String documentId) {
        this.documentId = documentId;
    }

    /**
     * Gets the encodingDate value for this RegistrationSummary.
     *
     * @return encodingDate * Date at which the document has been encoded in the common repository
     */
    public java.util.Date getEncodingDate() {
        return encodingDate;
    }

    /**
     * Sets the encodingDate value for this RegistrationSummary.
     *
     * @param encodingDate * Date at which the document has been encoded in the common repository
     */
    public void setEncodingDate(java.util.Date encodingDate) {
        this.encodingDate = encodingDate;
    }

    /**
     * Gets the registrationDate value for this RegistrationSummary.
     *
     * @return registrationDate * Date at which the document has been registered
     */
    public java.util.Date getRegistrationDate() {
        return registrationDate;
    }

    /**
     * Sets the registrationDate value for this RegistrationSummary.
     *
     * @param registrationDate * Date at which the document has been registered
     */
    public void setRegistrationDate(java.util.Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    /**
     * Gets the registrationNumber value for this RegistrationSummary.
     *
     * @return registrationNumber * Official document number
     */
    public java.lang.String getRegistrationNumber() {
        return registrationNumber;
    }

    /**
     * Sets the registrationNumber value for this RegistrationSummary.
     *
     * @param registrationNumber * Official document number
     */
    public void setRegistrationNumber(java.lang.String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    /**
     * Gets the saveNumber value for this RegistrationSummary.
     *
     * @return saveNumber * Internal document number
     */
    public java.lang.String getSaveNumber() {
        return saveNumber;
    }

    /**
     * Sets the saveNumber value for this RegistrationSummary.
     *
     * @param saveNumber * Internal document number
     */
    public void setSaveNumber(java.lang.String saveNumber) {
        this.saveNumber = saveNumber;
    }

    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof RegistrationSummary)) {
            return false;
        }
        RegistrationSummary other = (RegistrationSummary) obj;
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
                        && ((this.encodingDate == null && other.getEncodingDate() == null)
                        || (this.encodingDate != null && this.encodingDate.equals(other.getEncodingDate())))
                        && ((this.registrationDate == null && other.getRegistrationDate() == null)
                        || (this.registrationDate != null
                        && this.registrationDate.equals(other.getRegistrationDate())))
                        && ((this.registrationNumber == null && other.getRegistrationNumber() == null)
                        || (this.registrationNumber != null
                        && this.registrationNumber.equals(other.getRegistrationNumber())))
                        && ((this.saveNumber == null && other.getSaveNumber() == null)
                        || (this.saveNumber != null && this.saveNumber.equals(other.getSaveNumber())));
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
        if (getEncodingDate() != null) {
            _hashCode += getEncodingDate().hashCode();
        }
        if (getRegistrationDate() != null) {
            _hashCode += getRegistrationDate().hashCode();
        }
        if (getRegistrationNumber() != null) {
            _hashCode += getRegistrationNumber().hashCode();
        }
        if (getSaveNumber() != null) {
            _hashCode += getSaveNumber().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }
}
