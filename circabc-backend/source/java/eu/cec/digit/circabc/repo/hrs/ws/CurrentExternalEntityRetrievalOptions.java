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
 * <p>CurrentExternalEntityRetrievalOptions.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 * <p>
 * CurrentExternalEntityRetrievalOptions.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
/**
 * CurrentExternalEntityRetrievalOptions.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package eu.cec.digit.circabc.repo.hrs.ws;

/** Specifies which fields should be retrieved when retrieving a current external entity. */
public class CurrentExternalEntityRetrievalOptions implements java.io.Serializable {

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
            new org.apache.axis.description.TypeDesc(CurrentExternalEntityRetrievalOptions.class, true);

    static {
        typeDesc.setXmlType(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "CurrentExternalEntityRetrievalOptions"));
        org.apache.axis.description.ElementDesc elemField =
                new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("includeExtendedMetadata");
        elemField.setXmlName(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "includeExtendedMetadata"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("includeCreateUpdateMetadata");
        elemField.setXmlName(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "includeCreateUpdateMetadata"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /* Result will also include the deprecated fields: address,
     *                         internet, phone, fax, postal code, post office
     * code, title, VAT, language. */
    private java.lang.Boolean includeExtendedMetadata;
    /* Result will also include the following fields: creation date,
     * last change date, creator, creator's DG, last modificator. */
    private java.lang.Boolean includeCreateUpdateMetadata;
    private java.lang.Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;

    public CurrentExternalEntityRetrievalOptions() {
    }

    public CurrentExternalEntityRetrievalOptions(
            java.lang.Boolean includeExtendedMetadata, java.lang.Boolean includeCreateUpdateMetadata) {
        this.includeExtendedMetadata = includeExtendedMetadata;
        this.includeCreateUpdateMetadata = includeCreateUpdateMetadata;
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
     * Gets the includeExtendedMetadata value for this CurrentExternalEntityRetrievalOptions.
     *
     * @return includeExtendedMetadata * Result will also include the deprecated fields: address,
     *     internet, phone, fax, postal code, post office code, title, VAT, language.
     */
    public java.lang.Boolean getIncludeExtendedMetadata() {
        return includeExtendedMetadata;
    }

    /**
     * Sets the includeExtendedMetadata value for this CurrentExternalEntityRetrievalOptions.
     *
     * @param includeExtendedMetadata * Result will also include the deprecated fields: address,
     *     internet, phone, fax, postal code, post office code, title, VAT, language.
     */
    public void setIncludeExtendedMetadata(java.lang.Boolean includeExtendedMetadata) {
        this.includeExtendedMetadata = includeExtendedMetadata;
    }

    /**
     * Gets the includeCreateUpdateMetadata value for this CurrentExternalEntityRetrievalOptions.
     *
     * @return includeCreateUpdateMetadata * Result will also include the following fields: creation
     *     date, last change date, creator, creator's DG, last modificator.
     */
    public java.lang.Boolean getIncludeCreateUpdateMetadata() {
        return includeCreateUpdateMetadata;
    }

    /**
     * Sets the includeCreateUpdateMetadata value for this CurrentExternalEntityRetrievalOptions.
     *
     * @param includeCreateUpdateMetadata * Result will also include the following fields: creation
     *     date, last change date, creator, creator's DG, last modificator.
     */
    public void setIncludeCreateUpdateMetadata(java.lang.Boolean includeCreateUpdateMetadata) {
        this.includeCreateUpdateMetadata = includeCreateUpdateMetadata;
    }

    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof CurrentExternalEntityRetrievalOptions)) {
            return false;
        }
        CurrentExternalEntityRetrievalOptions other = (CurrentExternalEntityRetrievalOptions) obj;
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
                        && ((this.includeExtendedMetadata == null && other.getIncludeExtendedMetadata() == null)
                        || (this.includeExtendedMetadata != null
                        && this.includeExtendedMetadata.equals(other.getIncludeExtendedMetadata())))
                        && ((this.includeCreateUpdateMetadata == null
                        && other.getIncludeCreateUpdateMetadata() == null)
                        || (this.includeCreateUpdateMetadata != null
                        && this.includeCreateUpdateMetadata.equals(
                        other.getIncludeCreateUpdateMetadata())));
        __equalsCalc = null;
        return _equals;
    }

    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getIncludeExtendedMetadata() != null) {
            _hashCode += getIncludeExtendedMetadata().hashCode();
        }
        if (getIncludeCreateUpdateMetadata() != null) {
            _hashCode += getIncludeCreateUpdateMetadata().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }
}
