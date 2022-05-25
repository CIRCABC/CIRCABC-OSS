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
 * <p>FindPotentialRegisteredDuplicatesResponsePotentialDuplicateRegisteredDocumentsPotentialDuplicateRegisteredDocumentInfoRegistrationAuthor.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 * <p>
 * FindPotentialRegisteredDuplicatesResponsePotentialDuplicateRegisteredDocumentsPotentialDuplicateRegisteredDocumentInfoRegistrationAuthor.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
/**
 * FindPotentialRegisteredDuplicatesResponsePotentialDuplicateRegisteredDocumentsPotentialDuplicateRegisteredDocumentInfoRegistrationAuthor.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package eu.cec.digit.circabc.repo.hrs.ws;

public
class FindPotentialRegisteredDuplicatesResponsePotentialDuplicateRegisteredDocumentsPotentialDuplicateRegisteredDocumentInfoRegistrationAuthor
        implements java.io.Serializable {

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
            new org.apache.axis.description.TypeDesc(
                    FindPotentialRegisteredDuplicatesResponsePotentialDuplicateRegisteredDocumentsPotentialDuplicateRegisteredDocumentInfoRegistrationAuthor
                            .class,
                    true);

    static {
        typeDesc.setXmlType(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types",
                        ">>>>findPotentialRegisteredDuplicatesResponse>potentialDuplicateRegisteredDocuments>potentialDuplicateRegisteredDocumentInfo>registrationAuthor"));
        org.apache.axis.description.ElementDesc elemField =
                new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("ecasId");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "ecasId"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("fullName");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "fullName"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("dg");
        elemField.setXmlName(new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "dg"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("service");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "service"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
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
        elemField.setFieldName("email");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "email"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /* ECAS user id of the registration author */
    private java.lang.String ecasId;
    /* The registration's author full name (or a part of it) */
    private java.lang.String fullName;
    /* DG to which the registration author belongs */
    private java.lang.String dg;
    /* Service to which the registration author belongs */
    private java.lang.String service;
    /* Telephone number to contact the registration author */
    private java.lang.String phone;
    /* Email address of the registration author */
    private java.lang.String email;
    private java.lang.Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;

    public FindPotentialRegisteredDuplicatesResponsePotentialDuplicateRegisteredDocumentsPotentialDuplicateRegisteredDocumentInfoRegistrationAuthor() {
    }

    public FindPotentialRegisteredDuplicatesResponsePotentialDuplicateRegisteredDocumentsPotentialDuplicateRegisteredDocumentInfoRegistrationAuthor(
            java.lang.String ecasId,
            java.lang.String fullName,
            java.lang.String dg,
            java.lang.String service,
            java.lang.String phone,
            java.lang.String email) {
        this.ecasId = ecasId;
        this.fullName = fullName;
        this.dg = dg;
        this.service = service;
        this.phone = phone;
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
     * Gets the ecasId value for this
     * FindPotentialRegisteredDuplicatesResponsePotentialDuplicateRegisteredDocumentsPotentialDuplicateRegisteredDocumentInfoRegistrationAuthor.
     *
     * @return ecasId * ECAS user id of the registration author
     */
    public java.lang.String getEcasId() {
        return ecasId;
    }

    /**
     * Sets the ecasId value for this
     * FindPotentialRegisteredDuplicatesResponsePotentialDuplicateRegisteredDocumentsPotentialDuplicateRegisteredDocumentInfoRegistrationAuthor.
     *
     * @param ecasId * ECAS user id of the registration author
     */
    public void setEcasId(java.lang.String ecasId) {
        this.ecasId = ecasId;
    }

    /**
     * Gets the fullName value for this
     * FindPotentialRegisteredDuplicatesResponsePotentialDuplicateRegisteredDocumentsPotentialDuplicateRegisteredDocumentInfoRegistrationAuthor.
     *
     * @return fullName * The registration's author full name (or a part of it)
     */
    public java.lang.String getFullName() {
        return fullName;
    }

    /**
     * Sets the fullName value for this
     * FindPotentialRegisteredDuplicatesResponsePotentialDuplicateRegisteredDocumentsPotentialDuplicateRegisteredDocumentInfoRegistrationAuthor.
     *
     * @param fullName * The registration's author full name (or a part of it)
     */
    public void setFullName(java.lang.String fullName) {
        this.fullName = fullName;
    }

    /**
     * Gets the dg value for this
     * FindPotentialRegisteredDuplicatesResponsePotentialDuplicateRegisteredDocumentsPotentialDuplicateRegisteredDocumentInfoRegistrationAuthor.
     *
     * @return dg * DG to which the registration author belongs
     */
    public java.lang.String getDg() {
        return dg;
    }

    /**
     * Sets the dg value for this
     * FindPotentialRegisteredDuplicatesResponsePotentialDuplicateRegisteredDocumentsPotentialDuplicateRegisteredDocumentInfoRegistrationAuthor.
     *
     * @param dg * DG to which the registration author belongs
     */
    public void setDg(java.lang.String dg) {
        this.dg = dg;
    }

    /**
     * Gets the service value for this
     * FindPotentialRegisteredDuplicatesResponsePotentialDuplicateRegisteredDocumentsPotentialDuplicateRegisteredDocumentInfoRegistrationAuthor.
     *
     * @return service * Service to which the registration author belongs
     */
    public java.lang.String getService() {
        return service;
    }

    /**
     * Sets the service value for this
     * FindPotentialRegisteredDuplicatesResponsePotentialDuplicateRegisteredDocumentsPotentialDuplicateRegisteredDocumentInfoRegistrationAuthor.
     *
     * @param service * Service to which the registration author belongs
     */
    public void setService(java.lang.String service) {
        this.service = service;
    }

    /**
     * Gets the phone value for this
     * FindPotentialRegisteredDuplicatesResponsePotentialDuplicateRegisteredDocumentsPotentialDuplicateRegisteredDocumentInfoRegistrationAuthor.
     *
     * @return phone * Telephone number to contact the registration author
     */
    public java.lang.String getPhone() {
        return phone;
    }

    /**
     * Sets the phone value for this
     * FindPotentialRegisteredDuplicatesResponsePotentialDuplicateRegisteredDocumentsPotentialDuplicateRegisteredDocumentInfoRegistrationAuthor.
     *
     * @param phone * Telephone number to contact the registration author
     */
    public void setPhone(java.lang.String phone) {
        this.phone = phone;
    }

    /**
     * Gets the email value for this
     * FindPotentialRegisteredDuplicatesResponsePotentialDuplicateRegisteredDocumentsPotentialDuplicateRegisteredDocumentInfoRegistrationAuthor.
     *
     * @return email * Email address of the registration author
     */
    public java.lang.String getEmail() {
        return email;
    }

    /**
     * Sets the email value for this
     * FindPotentialRegisteredDuplicatesResponsePotentialDuplicateRegisteredDocumentsPotentialDuplicateRegisteredDocumentInfoRegistrationAuthor.
     *
     * @param email * Email address of the registration author
     */
    public void setEmail(java.lang.String email) {
        this.email = email;
    }

    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj
                instanceof
                FindPotentialRegisteredDuplicatesResponsePotentialDuplicateRegisteredDocumentsPotentialDuplicateRegisteredDocumentInfoRegistrationAuthor)) {
            return false;
        }
        FindPotentialRegisteredDuplicatesResponsePotentialDuplicateRegisteredDocumentsPotentialDuplicateRegisteredDocumentInfoRegistrationAuthor
                other =
                (FindPotentialRegisteredDuplicatesResponsePotentialDuplicateRegisteredDocumentsPotentialDuplicateRegisteredDocumentInfoRegistrationAuthor)
                        obj;
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
                        && ((this.ecasId == null && other.getEcasId() == null)
                        || (this.ecasId != null && this.ecasId.equals(other.getEcasId())))
                        && ((this.fullName == null && other.getFullName() == null)
                        || (this.fullName != null && this.fullName.equals(other.getFullName())))
                        && ((this.dg == null && other.getDg() == null)
                        || (this.dg != null && this.dg.equals(other.getDg())))
                        && ((this.service == null && other.getService() == null)
                        || (this.service != null && this.service.equals(other.getService())))
                        && ((this.phone == null && other.getPhone() == null)
                        || (this.phone != null && this.phone.equals(other.getPhone())))
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
        if (getEcasId() != null) {
            _hashCode += getEcasId().hashCode();
        }
        if (getFullName() != null) {
            _hashCode += getFullName().hashCode();
        }
        if (getDg() != null) {
            _hashCode += getDg().hashCode();
        }
        if (getService() != null) {
            _hashCode += getService().hashCode();
        }
        if (getPhone() != null) {
            _hashCode += getPhone().hashCode();
        }
        if (getEmail() != null) {
            _hashCode += getEmail().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }
}
