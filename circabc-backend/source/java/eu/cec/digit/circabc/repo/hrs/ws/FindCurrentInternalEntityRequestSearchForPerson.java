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
 * <p>FindCurrentInternalEntityRequestSearchForPerson.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 * <p>
 * FindCurrentInternalEntityRequestSearchForPerson.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
/**
 * FindCurrentInternalEntityRequestSearchForPerson.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package eu.cec.digit.circabc.repo.hrs.ws;

public class FindCurrentInternalEntityRequestSearchForPerson implements java.io.Serializable {

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
            new org.apache.axis.description.TypeDesc(
                    FindCurrentInternalEntityRequestSearchForPerson.class, true);

    static {
        typeDesc.setXmlType(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types",
                        ">FindCurrentInternalEntityRequest>searchForPerson"));
        org.apache.axis.description.ElementDesc elemField =
                new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("lastName");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "lastName"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("firstName");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "firstName"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("ecasUserName");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "ecasUserName"));
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

    /* The person's last name */
    private java.lang.String lastName;
    /* First name(s) of the person */
    private java.lang.String firstName;
    /* The person's ecas user name */
    private java.lang.String ecasUserName;
    /* The person's email */
    private java.lang.String email;
    private java.lang.Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;

    public FindCurrentInternalEntityRequestSearchForPerson() {
    }

    public FindCurrentInternalEntityRequestSearchForPerson(
            java.lang.String lastName,
            java.lang.String firstName,
            java.lang.String ecasUserName,
            java.lang.String email) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.ecasUserName = ecasUserName;
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
     * Gets the lastName value for this FindCurrentInternalEntityRequestSearchForPerson.
     *
     * @return lastName * The person's last name
     */
    public java.lang.String getLastName() {
        return lastName;
    }

    /**
     * Sets the lastName value for this FindCurrentInternalEntityRequestSearchForPerson.
     *
     * @param lastName * The person's last name
     */
    public void setLastName(java.lang.String lastName) {
        this.lastName = lastName;
    }

    /**
     * Gets the firstName value for this FindCurrentInternalEntityRequestSearchForPerson.
     *
     * @return firstName * First name(s) of the person
     */
    public java.lang.String getFirstName() {
        return firstName;
    }

    /**
     * Sets the firstName value for this FindCurrentInternalEntityRequestSearchForPerson.
     *
     * @param firstName * First name(s) of the person
     */
    public void setFirstName(java.lang.String firstName) {
        this.firstName = firstName;
    }

    /**
     * Gets the ecasUserName value for this FindCurrentInternalEntityRequestSearchForPerson.
     *
     * @return ecasUserName * The person's ecas user name
     */
    public java.lang.String getEcasUserName() {
        return ecasUserName;
    }

    /**
     * Sets the ecasUserName value for this FindCurrentInternalEntityRequestSearchForPerson.
     *
     * @param ecasUserName * The person's ecas user name
     */
    public void setEcasUserName(java.lang.String ecasUserName) {
        this.ecasUserName = ecasUserName;
    }

    /**
     * Gets the email value for this FindCurrentInternalEntityRequestSearchForPerson.
     *
     * @return email * The person's email
     */
    public java.lang.String getEmail() {
        return email;
    }

    /**
     * Sets the email value for this FindCurrentInternalEntityRequestSearchForPerson.
     *
     * @param email * The person's email
     */
    public void setEmail(java.lang.String email) {
        this.email = email;
    }

    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof FindCurrentInternalEntityRequestSearchForPerson)) {
            return false;
        }
        FindCurrentInternalEntityRequestSearchForPerson other =
                (FindCurrentInternalEntityRequestSearchForPerson) obj;
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
                        && ((this.lastName == null && other.getLastName() == null)
                        || (this.lastName != null && this.lastName.equals(other.getLastName())))
                        && ((this.firstName == null && other.getFirstName() == null)
                        || (this.firstName != null && this.firstName.equals(other.getFirstName())))
                        && ((this.ecasUserName == null && other.getEcasUserName() == null)
                        || (this.ecasUserName != null && this.ecasUserName.equals(other.getEcasUserName())))
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
        if (getLastName() != null) {
            _hashCode += getLastName().hashCode();
        }
        if (getFirstName() != null) {
            _hashCode += getFirstName().hashCode();
        }
        if (getEcasUserName() != null) {
            _hashCode += getEcasUserName().hashCode();
        }
        if (getEmail() != null) {
            _hashCode += getEmail().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }
}
