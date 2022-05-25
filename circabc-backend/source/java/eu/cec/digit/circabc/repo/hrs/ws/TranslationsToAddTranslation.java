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
 * <p>TranslationsToAddTranslation.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 * <p>
 * TranslationsToAddTranslation.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
/**
 * TranslationsToAddTranslation.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package eu.cec.digit.circabc.repo.hrs.ws;

public class TranslationsToAddTranslation implements java.io.Serializable {

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
            new org.apache.axis.description.TypeDesc(TranslationsToAddTranslation.class, true);

    static {
        typeDesc.setXmlType(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", ">TranslationsToAdd>translation"));
        org.apache.axis.description.ElementDesc elemField =
                new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("name");
        elemField.setXmlName(new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "name"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("contentId");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "contentId"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("attachmentType");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "attachmentType"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "AttachmentTypeToAdd"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("language");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "language"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("externalReference");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "externalReference"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /* Name of the translation */
    private java.lang.String name;
    /* Content id that was obtained by uploading the content using
     * the
     *                                     data transfer service */
    private java.lang.String contentId;
    /* Attachment type (e.g. NATIVE_ELECTRONIC) */
    private eu.cec.digit.circabc.repo.hrs.ws.AttachmentTypeToAdd attachmentType;
    /* Language of the translation (e.g. : EN, FR) */
    private java.lang.String language;
    /* External reference associated with the translation */
    private java.lang.String externalReference;
    private java.lang.Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;

    public TranslationsToAddTranslation() {
    }

    public TranslationsToAddTranslation(
            java.lang.String name,
            java.lang.String contentId,
            eu.cec.digit.circabc.repo.hrs.ws.AttachmentTypeToAdd attachmentType,
            java.lang.String language,
            java.lang.String externalReference) {
        this.name = name;
        this.contentId = contentId;
        this.attachmentType = attachmentType;
        this.language = language;
        this.externalReference = externalReference;
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
     * Gets the name value for this TranslationsToAddTranslation.
     *
     * @return name * Name of the translation
     */
    public java.lang.String getName() {
        return name;
    }

    /**
     * Sets the name value for this TranslationsToAddTranslation.
     *
     * @param name * Name of the translation
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }

    /**
     * Gets the contentId value for this TranslationsToAddTranslation.
     *
     * @return contentId * Content id that was obtained by uploading the content using the data
     *     transfer service
     */
    public java.lang.String getContentId() {
        return contentId;
    }

    /**
     * Sets the contentId value for this TranslationsToAddTranslation.
     *
     * @param contentId * Content id that was obtained by uploading the content using the data
     *     transfer service
     */
    public void setContentId(java.lang.String contentId) {
        this.contentId = contentId;
    }

    /**
     * Gets the attachmentType value for this TranslationsToAddTranslation.
     *
     * @return attachmentType * Attachment type (e.g. NATIVE_ELECTRONIC)
     */
    public eu.cec.digit.circabc.repo.hrs.ws.AttachmentTypeToAdd getAttachmentType() {
        return attachmentType;
    }

    /**
     * Sets the attachmentType value for this TranslationsToAddTranslation.
     *
     * @param attachmentType * Attachment type (e.g. NATIVE_ELECTRONIC)
     */
    public void setAttachmentType(
            eu.cec.digit.circabc.repo.hrs.ws.AttachmentTypeToAdd attachmentType) {
        this.attachmentType = attachmentType;
    }

    /**
     * Gets the language value for this TranslationsToAddTranslation.
     *
     * @return language * Language of the translation (e.g. : EN, FR)
     */
    public java.lang.String getLanguage() {
        return language;
    }

    /**
     * Sets the language value for this TranslationsToAddTranslation.
     *
     * @param language * Language of the translation (e.g. : EN, FR)
     */
    public void setLanguage(java.lang.String language) {
        this.language = language;
    }

    /**
     * Gets the externalReference value for this TranslationsToAddTranslation.
     *
     * @return externalReference * External reference associated with the translation
     */
    public java.lang.String getExternalReference() {
        return externalReference;
    }

    /**
     * Sets the externalReference value for this TranslationsToAddTranslation.
     *
     * @param externalReference * External reference associated with the translation
     */
    public void setExternalReference(java.lang.String externalReference) {
        this.externalReference = externalReference;
    }

    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof TranslationsToAddTranslation)) {
            return false;
        }
        TranslationsToAddTranslation other = (TranslationsToAddTranslation) obj;
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
                        && ((this.name == null && other.getName() == null)
                        || (this.name != null && this.name.equals(other.getName())))
                        && ((this.contentId == null && other.getContentId() == null)
                        || (this.contentId != null && this.contentId.equals(other.getContentId())))
                        && ((this.attachmentType == null && other.getAttachmentType() == null)
                        || (this.attachmentType != null
                        && this.attachmentType.equals(other.getAttachmentType())))
                        && ((this.language == null && other.getLanguage() == null)
                        || (this.language != null && this.language.equals(other.getLanguage())))
                        && ((this.externalReference == null && other.getExternalReference() == null)
                        || (this.externalReference != null
                        && this.externalReference.equals(other.getExternalReference())));
        __equalsCalc = null;
        return _equals;
    }

    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getName() != null) {
            _hashCode += getName().hashCode();
        }
        if (getContentId() != null) {
            _hashCode += getContentId().hashCode();
        }
        if (getAttachmentType() != null) {
            _hashCode += getAttachmentType().hashCode();
        }
        if (getLanguage() != null) {
            _hashCode += getLanguage().hashCode();
        }
        if (getExternalReference() != null) {
            _hashCode += getExternalReference().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }
}
