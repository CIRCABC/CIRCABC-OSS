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
 * <p>DocumentRecipientsRecipient.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 * <p>
 * DocumentRecipientsRecipient.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
/**
 * DocumentRecipientsRecipient.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package eu.cec.digit.circabc.repo.hrs.ws;

public class DocumentRecipientsRecipient implements java.io.Serializable {

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
            new org.apache.axis.description.TypeDesc(DocumentRecipientsRecipient.class, true);

    static {
        typeDesc.setXmlType(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", ">>Document>recipients>recipient"));
        org.apache.axis.description.ElementDesc elemField =
                new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("internalEntity");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "internalEntity"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "InternalEntity"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("externalEntity");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "externalEntity"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "ExternalEntity"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("code");
        elemField.setXmlName(new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "code"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "RecipientCode"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /* Internal entity */
    private eu.cec.digit.circabc.repo.hrs.ws.InternalEntity internalEntity;
    /* External entity */
    private eu.cec.digit.circabc.repo.hrs.ws.ExternalEntity externalEntity;
    /* The recipient code */
    private eu.cec.digit.circabc.repo.hrs.ws.RecipientCode code;
    private java.lang.Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;

    public DocumentRecipientsRecipient() {
    }

    public DocumentRecipientsRecipient(
            eu.cec.digit.circabc.repo.hrs.ws.InternalEntity internalEntity,
            eu.cec.digit.circabc.repo.hrs.ws.ExternalEntity externalEntity,
            eu.cec.digit.circabc.repo.hrs.ws.RecipientCode code) {
        this.internalEntity = internalEntity;
        this.externalEntity = externalEntity;
        this.code = code;
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
     * Gets the internalEntity value for this DocumentRecipientsRecipient.
     *
     * @return internalEntity * Internal entity
     */
    public eu.cec.digit.circabc.repo.hrs.ws.InternalEntity getInternalEntity() {
        return internalEntity;
    }

    /**
     * Sets the internalEntity value for this DocumentRecipientsRecipient.
     *
     * @param internalEntity * Internal entity
     */
    public void setInternalEntity(eu.cec.digit.circabc.repo.hrs.ws.InternalEntity internalEntity) {
        this.internalEntity = internalEntity;
    }

    /**
     * Gets the externalEntity value for this DocumentRecipientsRecipient.
     *
     * @return externalEntity * External entity
     */
    public eu.cec.digit.circabc.repo.hrs.ws.ExternalEntity getExternalEntity() {
        return externalEntity;
    }

    /**
     * Sets the externalEntity value for this DocumentRecipientsRecipient.
     *
     * @param externalEntity * External entity
     */
    public void setExternalEntity(eu.cec.digit.circabc.repo.hrs.ws.ExternalEntity externalEntity) {
        this.externalEntity = externalEntity;
    }

    /**
     * Gets the code value for this DocumentRecipientsRecipient.
     *
     * @return code * The recipient code
     */
    public eu.cec.digit.circabc.repo.hrs.ws.RecipientCode getCode() {
        return code;
    }

    /**
     * Sets the code value for this DocumentRecipientsRecipient.
     *
     * @param code * The recipient code
     */
    public void setCode(eu.cec.digit.circabc.repo.hrs.ws.RecipientCode code) {
        this.code = code;
    }

    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof DocumentRecipientsRecipient)) {
            return false;
        }
        DocumentRecipientsRecipient other = (DocumentRecipientsRecipient) obj;
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
                        && ((this.internalEntity == null && other.getInternalEntity() == null)
                        || (this.internalEntity != null
                        && this.internalEntity.equals(other.getInternalEntity())))
                        && ((this.externalEntity == null && other.getExternalEntity() == null)
                        || (this.externalEntity != null
                        && this.externalEntity.equals(other.getExternalEntity())))
                        && ((this.code == null && other.getCode() == null)
                        || (this.code != null && this.code.equals(other.getCode())));
        __equalsCalc = null;
        return _equals;
    }

    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getInternalEntity() != null) {
            _hashCode += getInternalEntity().hashCode();
        }
        if (getExternalEntity() != null) {
            _hashCode += getExternalEntity().hashCode();
        }
        if (getCode() != null) {
            _hashCode += getCode().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }
}
