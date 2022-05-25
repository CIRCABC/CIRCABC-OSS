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
 * <p>CurrentInternalOrganization.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 * <p>
 * CurrentInternalOrganization.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
/**
 * CurrentInternalOrganization.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package eu.cec.digit.circabc.repo.hrs.ws;

/** An internal organization from the current base */
public class CurrentInternalOrganization implements java.io.Serializable {

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
            new org.apache.axis.description.TypeDesc(CurrentInternalOrganization.class, true);

    static {
        typeDesc.setXmlType(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "CurrentInternalOrganization"));
        org.apache.axis.description.ElementDesc elemField =
                new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("id");
        elemField.setXmlName(new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "id"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("code");
        elemField.setXmlName(new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "code"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("shortCode");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "shortCode"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /* Internal repository ID that identifies the internal organization */
    private java.lang.String id;
    /* Name of the organization e.g. DIGIT, AIDCO.G.5 */
    private java.lang.String code;
    /* Shortened version of the code of the internal organization */
    private java.lang.String shortCode;
    private java.lang.Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;

    public CurrentInternalOrganization() {
    }

    public CurrentInternalOrganization(
            java.lang.String id, java.lang.String code, java.lang.String shortCode) {
        this.id = id;
        this.code = code;
        this.shortCode = shortCode;
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
     * Gets the id value for this CurrentInternalOrganization.
     *
     * @return id * Internal repository ID that identifies the internal organization
     */
    public java.lang.String getId() {
        return id;
    }

    /**
     * Sets the id value for this CurrentInternalOrganization.
     *
     * @param id * Internal repository ID that identifies the internal organization
     */
    public void setId(java.lang.String id) {
        this.id = id;
    }

    /**
     * Gets the code value for this CurrentInternalOrganization.
     *
     * @return code * Name of the organization e.g. DIGIT, AIDCO.G.5
     */
    public java.lang.String getCode() {
        return code;
    }

    /**
     * Sets the code value for this CurrentInternalOrganization.
     *
     * @param code * Name of the organization e.g. DIGIT, AIDCO.G.5
     */
    public void setCode(java.lang.String code) {
        this.code = code;
    }

    /**
     * Gets the shortCode value for this CurrentInternalOrganization.
     *
     * @return shortCode * Shortened version of the code of the internal organization
     */
    public java.lang.String getShortCode() {
        return shortCode;
    }

    /**
     * Sets the shortCode value for this CurrentInternalOrganization.
     *
     * @param shortCode * Shortened version of the code of the internal organization
     */
    public void setShortCode(java.lang.String shortCode) {
        this.shortCode = shortCode;
    }

    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof CurrentInternalOrganization)) {
            return false;
        }
        CurrentInternalOrganization other = (CurrentInternalOrganization) obj;
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
                        && ((this.id == null && other.getId() == null)
                        || (this.id != null && this.id.equals(other.getId())))
                        && ((this.code == null && other.getCode() == null)
                        || (this.code != null && this.code.equals(other.getCode())))
                        && ((this.shortCode == null && other.getShortCode() == null)
                        || (this.shortCode != null && this.shortCode.equals(other.getShortCode())));
        __equalsCalc = null;
        return _equals;
    }

    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getId() != null) {
            _hashCode += getId().hashCode();
        }
        if (getCode() != null) {
            _hashCode += getCode().hashCode();
        }
        if (getShortCode() != null) {
            _hashCode += getShortCode().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }
}
