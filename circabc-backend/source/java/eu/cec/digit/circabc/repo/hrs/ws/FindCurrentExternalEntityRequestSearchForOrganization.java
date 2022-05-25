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
 * <p>FindCurrentExternalEntityRequestSearchForOrganization.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 * <p>
 * FindCurrentExternalEntityRequestSearchForOrganization.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
/**
 * FindCurrentExternalEntityRequestSearchForOrganization.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package eu.cec.digit.circabc.repo.hrs.ws;

public class FindCurrentExternalEntityRequestSearchForOrganization implements java.io.Serializable {

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
            new org.apache.axis.description.TypeDesc(
                    FindCurrentExternalEntityRequestSearchForOrganization.class, true);

    static {
        typeDesc.setXmlType(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types",
                        ">FindCurrentExternalEntityRequest>searchForOrganization"));
        org.apache.axis.description.ElementDesc elemField =
                new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("name");
        elemField.setXmlName(new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "name"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("acronym");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "acronym"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("country");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "country"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("email");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "email"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /* Name of the organization */
    private java.lang.String name;
    /* Acronym of the organization */
    private java.lang.String acronym;
    /* Country of the organization */
    private java.lang.String country;
    /* The organization's email */
    private java.lang.String email;
    private java.lang.Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;

    public FindCurrentExternalEntityRequestSearchForOrganization() {
    }

    public FindCurrentExternalEntityRequestSearchForOrganization(
            java.lang.String name,
            java.lang.String acronym,
            java.lang.String country,
            java.lang.String email) {
        this.name = name;
        this.acronym = acronym;
        this.country = country;
        this.email = email;
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
     * Gets the name value for this FindCurrentExternalEntityRequestSearchForOrganization.
     *
     * @return name * Name of the organization
     */
    public java.lang.String getName() {
        return name;
    }

    /**
     * Sets the name value for this FindCurrentExternalEntityRequestSearchForOrganization.
     *
     * @param name * Name of the organization
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }

    /**
     * Gets the acronym value for this FindCurrentExternalEntityRequestSearchForOrganization.
     *
     * @return acronym * Acronym of the organization
     */
    public java.lang.String getAcronym() {
        return acronym;
    }

    /**
     * Sets the acronym value for this FindCurrentExternalEntityRequestSearchForOrganization.
     *
     * @param acronym * Acronym of the organization
     */
    public void setAcronym(java.lang.String acronym) {
        this.acronym = acronym;
    }

    /**
     * Gets the country value for this FindCurrentExternalEntityRequestSearchForOrganization.
     *
     * @return country * Country of the organization
     */
    public java.lang.String getCountry() {
        return country;
    }

    /**
     * Sets the country value for this FindCurrentExternalEntityRequestSearchForOrganization.
     *
     * @param country * Country of the organization
     */
    public void setCountry(java.lang.String country) {
        this.country = country;
    }

    /**
     * Gets the email value for this FindCurrentExternalEntityRequestSearchForOrganization.
     *
     * @return email * The organization's email
     */
    public java.lang.String getEmail() {
        return email;
    }

    /**
     * Sets the email value for this FindCurrentExternalEntityRequestSearchForOrganization.
     *
     * @param email * The organization's email
     */
    public void setEmail(java.lang.String email) {
        this.email = email;
    }

    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof FindCurrentExternalEntityRequestSearchForOrganization)) {
            return false;
        }
        FindCurrentExternalEntityRequestSearchForOrganization other =
                (FindCurrentExternalEntityRequestSearchForOrganization) obj;
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
                        && ((this.acronym == null && other.getAcronym() == null)
                        || (this.acronym != null && this.acronym.equals(other.getAcronym())))
                        && ((this.country == null && other.getCountry() == null)
                        || (this.country != null && this.country.equals(other.getCountry())))
                        && ((this.email == null && other.getEmail() == null)
                        || (this.email != null && this.email.equals(other.getEmail())));
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
        if (getAcronym() != null) {
            _hashCode += getAcronym().hashCode();
        }
        if (getCountry() != null) {
            _hashCode += getCountry().hashCode();
        }
        if (getEmail() != null) {
            _hashCode += getEmail().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }
}
