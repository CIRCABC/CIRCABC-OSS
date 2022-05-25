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
 * <p>ScannedItemToAdd.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 * <p>
 * ScannedItemToAdd.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
/**
 * ScannedItemToAdd.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package eu.cec.digit.circabc.repo.hrs.ws;

public class ScannedItemToAdd implements java.io.Serializable {

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
            new org.apache.axis.description.TypeDesc(ScannedItemToAdd.class, true);

    static {
        typeDesc.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "ScannedItemToAdd"));
        org.apache.axis.description.ElementDesc elemField =
                new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("name");
        elemField.setXmlName(new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "name"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
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
        elemField.setFieldName("kind");
        elemField.setXmlName(new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "kind"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "ItemKindToAdd"));
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
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("translations");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "translations"));
        elemField.setXmlType(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", ">TranslationsToAdd>translation"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "translation"));
        typeDesc.addFieldDesc(elemField);
    }

    /* Name of the item */
    private java.lang.String name;
    /* Language of the item (e.g. : EN, FR) */
    private java.lang.String language;
    /* Item kind such as MAIN, ANNEX etc. */
    private eu.cec.digit.circabc.repo.hrs.ws.ItemKindToAdd kind;
    /* External reference associated with the item */
    private java.lang.String externalReference;
    /* List of translations for the item */
    private eu.cec.digit.circabc.repo.hrs.ws.TranslationsToAddTranslation[] translations;
    private java.lang.Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;

    public ScannedItemToAdd() {
    }

    public ScannedItemToAdd(
            java.lang.String name,
            java.lang.String language,
            eu.cec.digit.circabc.repo.hrs.ws.ItemKindToAdd kind,
            java.lang.String externalReference,
            eu.cec.digit.circabc.repo.hrs.ws.TranslationsToAddTranslation[] translations) {
        this.name = name;
        this.language = language;
        this.kind = kind;
        this.externalReference = externalReference;
        this.translations = translations;
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
     * Gets the name value for this ScannedItemToAdd.
     *
     * @return name * Name of the item
     */
    public java.lang.String getName() {
        return name;
    }

    /**
     * Sets the name value for this ScannedItemToAdd.
     *
     * @param name * Name of the item
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }

    /**
     * Gets the language value for this ScannedItemToAdd.
     *
     * @return language * Language of the item (e.g. : EN, FR)
     */
    public java.lang.String getLanguage() {
        return language;
    }

    /**
     * Sets the language value for this ScannedItemToAdd.
     *
     * @param language * Language of the item (e.g. : EN, FR)
     */
    public void setLanguage(java.lang.String language) {
        this.language = language;
    }

    /**
     * Gets the kind value for this ScannedItemToAdd.
     *
     * @return kind * Item kind such as MAIN, ANNEX etc.
     */
    public eu.cec.digit.circabc.repo.hrs.ws.ItemKindToAdd getKind() {
        return kind;
    }

    /**
     * Sets the kind value for this ScannedItemToAdd.
     *
     * @param kind * Item kind such as MAIN, ANNEX etc.
     */
    public void setKind(eu.cec.digit.circabc.repo.hrs.ws.ItemKindToAdd kind) {
        this.kind = kind;
    }

    /**
     * Gets the externalReference value for this ScannedItemToAdd.
     *
     * @return externalReference * External reference associated with the item
     */
    public java.lang.String getExternalReference() {
        return externalReference;
    }

    /**
     * Sets the externalReference value for this ScannedItemToAdd.
     *
     * @param externalReference * External reference associated with the item
     */
    public void setExternalReference(java.lang.String externalReference) {
        this.externalReference = externalReference;
    }

    /**
     * Gets the translations value for this ScannedItemToAdd.
     *
     * @return translations * List of translations for the item
     */
    public eu.cec.digit.circabc.repo.hrs.ws.TranslationsToAddTranslation[] getTranslations() {
        return translations;
    }

    /**
     * Sets the translations value for this ScannedItemToAdd.
     *
     * @param translations * List of translations for the item
     */
    public void setTranslations(
            eu.cec.digit.circabc.repo.hrs.ws.TranslationsToAddTranslation[] translations) {
        this.translations = translations;
    }

    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ScannedItemToAdd)) {
            return false;
        }
        ScannedItemToAdd other = (ScannedItemToAdd) obj;
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
                        && ((this.language == null && other.getLanguage() == null)
                        || (this.language != null && this.language.equals(other.getLanguage())))
                        && ((this.kind == null && other.getKind() == null)
                        || (this.kind != null && this.kind.equals(other.getKind())))
                        && ((this.externalReference == null && other.getExternalReference() == null)
                        || (this.externalReference != null
                        && this.externalReference.equals(other.getExternalReference())))
                        && ((this.translations == null && other.getTranslations() == null)
                        || (this.translations != null
                        && java.util.Arrays.equals(this.translations, other.getTranslations())));
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
        if (getLanguage() != null) {
            _hashCode += getLanguage().hashCode();
        }
        if (getKind() != null) {
            _hashCode += getKind().hashCode();
        }
        if (getExternalReference() != null) {
            _hashCode += getExternalReference().hashCode();
        }
        if (getTranslations() != null) {
            for (int i = 0; i < java.lang.reflect.Array.getLength(getTranslations()); i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getTranslations(), i);
                if (obj != null && !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        __hashCodeCalc = false;
        return _hashCode;
    }
}
