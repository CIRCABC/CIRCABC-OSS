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
 * <p>WorkflowUser.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 * <p>
 * WorkflowUser.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
/**
 * WorkflowUser.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package eu.cec.digit.circabc.repo.hrs.ws;

/** A user that is a participant in a workflow */
public class WorkflowUser implements java.io.Serializable {

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
            new org.apache.axis.description.TypeDesc(WorkflowUser.class, true);

    static {
        typeDesc.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "WorkflowUser"));
        org.apache.axis.description.ElementDesc elemField =
                new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("userName");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "userName"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("fullName");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "fullName"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("organizationCode");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "organizationCode"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("phone");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "phone"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("address");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "address"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /* Identifier for the user */
    private java.lang.String userName;
    /* The user's full name (or a part of it) */
    private java.lang.String fullName;
    /* Organization to which the user belongs */
    private java.lang.String organizationCode;
    /* Telephone number to contact the user */
    private java.lang.String phone;
    /* Address of the user */
    private java.lang.String address;
    private java.lang.Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;

    public WorkflowUser() {
    }

    public WorkflowUser(
            java.lang.String userName,
            java.lang.String fullName,
            java.lang.String organizationCode,
            java.lang.String phone,
            java.lang.String address) {
        this.userName = userName;
        this.fullName = fullName;
        this.organizationCode = organizationCode;
        this.phone = phone;
        this.address = address;
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
     * Gets the userName value for this WorkflowUser.
     *
     * @return userName * Identifier for the user
     */
    public java.lang.String getUserName() {
        return userName;
    }

    /**
     * Sets the userName value for this WorkflowUser.
     *
     * @param userName * Identifier for the user
     */
    public void setUserName(java.lang.String userName) {
        this.userName = userName;
    }

    /**
     * Gets the fullName value for this WorkflowUser.
     *
     * @return fullName * The user's full name (or a part of it)
     */
    public java.lang.String getFullName() {
        return fullName;
    }

    /**
     * Sets the fullName value for this WorkflowUser.
     *
     * @param fullName * The user's full name (or a part of it)
     */
    public void setFullName(java.lang.String fullName) {
        this.fullName = fullName;
    }

    /**
     * Gets the organizationCode value for this WorkflowUser.
     *
     * @return organizationCode * Organization to which the user belongs
     */
    public java.lang.String getOrganizationCode() {
        return organizationCode;
    }

    /**
     * Sets the organizationCode value for this WorkflowUser.
     *
     * @param organizationCode * Organization to which the user belongs
     */
    public void setOrganizationCode(java.lang.String organizationCode) {
        this.organizationCode = organizationCode;
    }

    /**
     * Gets the phone value for this WorkflowUser.
     *
     * @return phone * Telephone number to contact the user
     */
    public java.lang.String getPhone() {
        return phone;
    }

    /**
     * Sets the phone value for this WorkflowUser.
     *
     * @param phone * Telephone number to contact the user
     */
    public void setPhone(java.lang.String phone) {
        this.phone = phone;
    }

    /**
     * Gets the address value for this WorkflowUser.
     *
     * @return address * Address of the user
     */
    public java.lang.String getAddress() {
        return address;
    }

    /**
     * Sets the address value for this WorkflowUser.
     *
     * @param address * Address of the user
     */
    public void setAddress(java.lang.String address) {
        this.address = address;
    }

    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof WorkflowUser)) {
            return false;
        }
        WorkflowUser other = (WorkflowUser) obj;
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
                        && ((this.userName == null && other.getUserName() == null)
                        || (this.userName != null && this.userName.equals(other.getUserName())))
                        && ((this.fullName == null && other.getFullName() == null)
                        || (this.fullName != null && this.fullName.equals(other.getFullName())))
                        && ((this.organizationCode == null && other.getOrganizationCode() == null)
                        || (this.organizationCode != null
                        && this.organizationCode.equals(other.getOrganizationCode())))
                        && ((this.phone == null && other.getPhone() == null)
                        || (this.phone != null && this.phone.equals(other.getPhone())))
                        && ((this.address == null && other.getAddress() == null)
                        || (this.address != null && this.address.equals(other.getAddress())));
        __equalsCalc = null;
        return _equals;
    }

    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getUserName() != null) {
            _hashCode += getUserName().hashCode();
        }
        if (getFullName() != null) {
            _hashCode += getFullName().hashCode();
        }
        if (getOrganizationCode() != null) {
            _hashCode += getOrganizationCode().hashCode();
        }
        if (getPhone() != null) {
            _hashCode += getPhone().hashCode();
        }
        if (getAddress() != null) {
            _hashCode += getAddress().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }
}
