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
 * <p>DocumentUpdateRequestUpdateItemsUpdateDetails.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 * <p>
 * DocumentUpdateRequestUpdateItemsUpdateDetails.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
/**
 * DocumentUpdateRequestUpdateItemsUpdateDetails.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package eu.cec.digit.circabc.repo.hrs.ws;

public class DocumentUpdateRequestUpdateItemsUpdateDetails implements java.io.Serializable {

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
            new org.apache.axis.description.TypeDesc(
                    DocumentUpdateRequestUpdateItemsUpdateDetails.class, true);

    static {
        typeDesc.setXmlType(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types",
                        ">>DocumentUpdateRequest>updateItems>updateDetails"));
        org.apache.axis.description.ElementDesc elemField =
                new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("itemId");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "itemId"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("name");
        elemField.setXmlName(new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "name"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("language");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "language"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("kind");
        elemField.setXmlName(new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "kind"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "ItemKind"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("attachmentType");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "attachmentType"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "AttachmentType"));
        elemField.setMinOccurs(0);
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

    /* Repository id of the item to update */
    private java.lang.String itemId;
    /* Updated item name. */
    private java.lang.String name;
    /* Updated item language */
    private java.lang.String language;
    /* Updated item kind. Changing from TRANSLATION to any of the
     * other item kinds or viceversa is not allowed. */
    private eu.cec.digit.circabc.repo.hrs.ws.ItemKind kind;
    /* Updated attachment type. Changing from ARES_SCANNED to any
     * of the other types or viceversa is not allowed. */
    private eu.cec.digit.circabc.repo.hrs.ws.AttachmentType attachmentType;
    /* Updated external reference */
    private java.lang.String externalReference;
    private java.lang.Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;

    public DocumentUpdateRequestUpdateItemsUpdateDetails() {
    }

    public DocumentUpdateRequestUpdateItemsUpdateDetails(
            java.lang.String itemId,
            java.lang.String name,
            java.lang.String language,
            eu.cec.digit.circabc.repo.hrs.ws.ItemKind kind,
            eu.cec.digit.circabc.repo.hrs.ws.AttachmentType attachmentType,
            java.lang.String externalReference) {
        this.itemId = itemId;
        this.name = name;
        this.language = language;
        this.kind = kind;
        this.attachmentType = attachmentType;
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
     * Gets the itemId value for this DocumentUpdateRequestUpdateItemsUpdateDetails.
     *
     * @return itemId * Repository id of the item to update
     */
    public java.lang.String getItemId() {
        return itemId;
    }

    /**
     * Sets the itemId value for this DocumentUpdateRequestUpdateItemsUpdateDetails.
     *
     * @param itemId * Repository id of the item to update
     */
    public void setItemId(java.lang.String itemId) {
        this.itemId = itemId;
    }

    /**
     * Gets the name value for this DocumentUpdateRequestUpdateItemsUpdateDetails.
     *
     * @return name * Updated item name.
     */
    public java.lang.String getName() {
        return name;
    }

    /**
     * Sets the name value for this DocumentUpdateRequestUpdateItemsUpdateDetails.
     *
     * @param name * Updated item name.
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }

    /**
     * Gets the language value for this DocumentUpdateRequestUpdateItemsUpdateDetails.
     *
     * @return language * Updated item language
     */
    public java.lang.String getLanguage() {
        return language;
    }

    /**
     * Sets the language value for this DocumentUpdateRequestUpdateItemsUpdateDetails.
     *
     * @param language * Updated item language
     */
    public void setLanguage(java.lang.String language) {
        this.language = language;
    }

    /**
     * Gets the kind value for this DocumentUpdateRequestUpdateItemsUpdateDetails.
     *
     * @return kind * Updated item kind. Changing from TRANSLATION to any of the other item kinds or
     *     viceversa is not allowed.
     */
    public eu.cec.digit.circabc.repo.hrs.ws.ItemKind getKind() {
        return kind;
    }

    /**
     * Sets the kind value for this DocumentUpdateRequestUpdateItemsUpdateDetails.
     *
     * @param kind * Updated item kind. Changing from TRANSLATION to any of the other item kinds or
     *     viceversa is not allowed.
     */
    public void setKind(eu.cec.digit.circabc.repo.hrs.ws.ItemKind kind) {
        this.kind = kind;
    }

    /**
     * Gets the attachmentType value for this DocumentUpdateRequestUpdateItemsUpdateDetails.
     *
     * @return attachmentType * Updated attachment type. Changing from ARES_SCANNED to any of the
     *     other types or viceversa is not allowed.
     */
    public eu.cec.digit.circabc.repo.hrs.ws.AttachmentType getAttachmentType() {
        return attachmentType;
    }

    /**
     * Sets the attachmentType value for this DocumentUpdateRequestUpdateItemsUpdateDetails.
     *
     * @param attachmentType * Updated attachment type. Changing from ARES_SCANNED to any of the other
     *     types or viceversa is not allowed.
     */
    public void setAttachmentType(eu.cec.digit.circabc.repo.hrs.ws.AttachmentType attachmentType) {
        this.attachmentType = attachmentType;
    }

    /**
     * Gets the externalReference value for this DocumentUpdateRequestUpdateItemsUpdateDetails.
     *
     * @return externalReference * Updated external reference
     */
    public java.lang.String getExternalReference() {
        return externalReference;
    }

    /**
     * Sets the externalReference value for this DocumentUpdateRequestUpdateItemsUpdateDetails.
     *
     * @param externalReference * Updated external reference
     */
    public void setExternalReference(java.lang.String externalReference) {
        this.externalReference = externalReference;
    }

    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof DocumentUpdateRequestUpdateItemsUpdateDetails)) {
            return false;
        }
        DocumentUpdateRequestUpdateItemsUpdateDetails other =
                (DocumentUpdateRequestUpdateItemsUpdateDetails) obj;
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
                        && ((this.itemId == null && other.getItemId() == null)
                        || (this.itemId != null && this.itemId.equals(other.getItemId())))
                        && ((this.name == null && other.getName() == null)
                        || (this.name != null && this.name.equals(other.getName())))
                        && ((this.language == null && other.getLanguage() == null)
                        || (this.language != null && this.language.equals(other.getLanguage())))
                        && ((this.kind == null && other.getKind() == null)
                        || (this.kind != null && this.kind.equals(other.getKind())))
                        && ((this.attachmentType == null && other.getAttachmentType() == null)
                        || (this.attachmentType != null
                        && this.attachmentType.equals(other.getAttachmentType())))
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
        if (getItemId() != null) {
            _hashCode += getItemId().hashCode();
        }
        if (getName() != null) {
            _hashCode += getName().hashCode();
        }
        if (getLanguage() != null) {
            _hashCode += getLanguage().hashCode();
        }
        if (getKind() != null) {
            _hashCode += getKind().hashCode();
        }
        if (getAttachmentType() != null) {
            _hashCode += getAttachmentType().hashCode();
        }
        if (getExternalReference() != null) {
            _hashCode += getExternalReference().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }
}
