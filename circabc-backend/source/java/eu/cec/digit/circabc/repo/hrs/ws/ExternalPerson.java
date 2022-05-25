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
 * <p>ExternalPerson.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 * <p>
 * ExternalPerson.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
/**
 * ExternalPerson.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package eu.cec.digit.circabc.repo.hrs.ws;

/** An external person */
public class ExternalPerson implements java.io.Serializable {

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
            new org.apache.axis.description.TypeDesc(ExternalPerson.class, true);

    static {
        typeDesc.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "ExternalPerson"));
        org.apache.axis.description.ElementDesc elemField =
                new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("title");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "title"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("lastName");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "lastName"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
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
        elemField.setFieldName("address");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "address"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("postalCode");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "postalCode"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("postOfficeBox");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "postOfficeBox"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("city");
        elemField.setXmlName(new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "city"));
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
        elemField.setFieldName("language");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "language"));
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
        elemField.setFieldName("fax");
        elemField.setXmlName(new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "fax"));
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
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("internet");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "internet"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /* The title e.g. Dr. Mr. etc. */
    private java.lang.String title;
    /* The person's last name */
    private java.lang.String lastName;
    /* The person's first name(s). */
    private java.lang.String firstName;
    /* Address of the external person */
    private java.lang.String address;
    /* Postal code */
    private java.lang.String postalCode;
    /* PO box */
    private java.lang.String postOfficeBox;
    /* City where the external person lives */
    private java.lang.String city;
    /* Country where the external person lives */
    private java.lang.String country;
    /* Mother tongue of the person */
    private java.lang.String language;
    /* Telephone number of the external person */
    private java.lang.String phone;
    /* Fax number of the external person */
    private java.lang.String fax;
    /* Email address of the external person */
    private java.lang.String email;
    /* Internet address of website of the external person */
    private java.lang.String internet;
    private java.lang.Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;

    public ExternalPerson() {
    }

    public ExternalPerson(
            java.lang.String title,
            java.lang.String lastName,
            java.lang.String firstName,
            java.lang.String address,
            java.lang.String postalCode,
            java.lang.String postOfficeBox,
            java.lang.String city,
            java.lang.String country,
            java.lang.String language,
            java.lang.String phone,
            java.lang.String fax,
            java.lang.String email,
            java.lang.String internet) {
        this.title = title;
        this.lastName = lastName;
        this.firstName = firstName;
        this.address = address;
        this.postalCode = postalCode;
        this.postOfficeBox = postOfficeBox;
        this.city = city;
        this.country = country;
        this.language = language;
        this.phone = phone;
        this.fax = fax;
        this.email = email;
        this.internet = internet;
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
     * Gets the title value for this ExternalPerson.
     *
     * @return title * The title e.g. Dr. Mr. etc.
     */
    public java.lang.String getTitle() {
        return title;
    }

    /**
     * Sets the title value for this ExternalPerson.
     *
     * @param title * The title e.g. Dr. Mr. etc.
     */
    public void setTitle(java.lang.String title) {
        this.title = title;
    }

    /**
     * Gets the lastName value for this ExternalPerson.
     *
     * @return lastName * The person's last name
     */
    public java.lang.String getLastName() {
        return lastName;
    }

    /**
     * Sets the lastName value for this ExternalPerson.
     *
     * @param lastName * The person's last name
     */
    public void setLastName(java.lang.String lastName) {
        this.lastName = lastName;
    }

    /**
     * Gets the firstName value for this ExternalPerson.
     *
     * @return firstName * The person's first name(s).
     */
    public java.lang.String getFirstName() {
        return firstName;
    }

    /**
     * Sets the firstName value for this ExternalPerson.
     *
     * @param firstName * The person's first name(s).
     */
    public void setFirstName(java.lang.String firstName) {
        this.firstName = firstName;
    }

    /**
     * Gets the address value for this ExternalPerson.
     *
     * @return address * Address of the external person
     */
    public java.lang.String getAddress() {
        return address;
    }

    /**
     * Sets the address value for this ExternalPerson.
     *
     * @param address * Address of the external person
     */
    public void setAddress(java.lang.String address) {
        this.address = address;
    }

    /**
     * Gets the postalCode value for this ExternalPerson.
     *
     * @return postalCode * Postal code
     */
    public java.lang.String getPostalCode() {
        return postalCode;
    }

    /**
     * Sets the postalCode value for this ExternalPerson.
     *
     * @param postalCode * Postal code
     */
    public void setPostalCode(java.lang.String postalCode) {
        this.postalCode = postalCode;
    }

    /**
     * Gets the postOfficeBox value for this ExternalPerson.
     *
     * @return postOfficeBox * PO box
     */
    public java.lang.String getPostOfficeBox() {
        return postOfficeBox;
    }

    /**
     * Sets the postOfficeBox value for this ExternalPerson.
     *
     * @param postOfficeBox * PO box
     */
    public void setPostOfficeBox(java.lang.String postOfficeBox) {
        this.postOfficeBox = postOfficeBox;
    }

    /**
     * Gets the city value for this ExternalPerson.
     *
     * @return city * City where the external person lives
     */
    public java.lang.String getCity() {
        return city;
    }

    /**
     * Sets the city value for this ExternalPerson.
     *
     * @param city * City where the external person lives
     */
    public void setCity(java.lang.String city) {
        this.city = city;
    }

    /**
     * Gets the country value for this ExternalPerson.
     *
     * @return country * Country where the external person lives
     */
    public java.lang.String getCountry() {
        return country;
    }

    /**
     * Sets the country value for this ExternalPerson.
     *
     * @param country * Country where the external person lives
     */
    public void setCountry(java.lang.String country) {
        this.country = country;
    }

    /**
     * Gets the language value for this ExternalPerson.
     *
     * @return language * Mother tongue of the person
     */
    public java.lang.String getLanguage() {
        return language;
    }

    /**
     * Sets the language value for this ExternalPerson.
     *
     * @param language * Mother tongue of the person
     */
    public void setLanguage(java.lang.String language) {
        this.language = language;
    }

    /**
     * Gets the phone value for this ExternalPerson.
     *
     * @return phone * Telephone number of the external person
     */
    public java.lang.String getPhone() {
        return phone;
    }

    /**
     * Sets the phone value for this ExternalPerson.
     *
     * @param phone * Telephone number of the external person
     */
    public void setPhone(java.lang.String phone) {
        this.phone = phone;
    }

    /**
     * Gets the fax value for this ExternalPerson.
     *
     * @return fax * Fax number of the external person
     */
    public java.lang.String getFax() {
        return fax;
    }

    /**
     * Sets the fax value for this ExternalPerson.
     *
     * @param fax * Fax number of the external person
     */
    public void setFax(java.lang.String fax) {
        this.fax = fax;
    }

    /**
     * Gets the email value for this ExternalPerson.
     *
     * @return email * Email address of the external person
     */
    public java.lang.String getEmail() {
        return email;
    }

    /**
     * Sets the email value for this ExternalPerson.
     *
     * @param email * Email address of the external person
     */
    public void setEmail(java.lang.String email) {
        this.email = email;
    }

    /**
     * Gets the internet value for this ExternalPerson.
     *
     * @return internet * Internet address of website of the external person
     */
    public java.lang.String getInternet() {
        return internet;
    }

    /**
     * Sets the internet value for this ExternalPerson.
     *
     * @param internet * Internet address of website of the external person
     */
    public void setInternet(java.lang.String internet) {
        this.internet = internet;
    }

    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ExternalPerson)) {
            return false;
        }
        ExternalPerson other = (ExternalPerson) obj;
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
                        && ((this.title == null && other.getTitle() == null)
                        || (this.title != null && this.title.equals(other.getTitle())))
                        && ((this.lastName == null && other.getLastName() == null)
                        || (this.lastName != null && this.lastName.equals(other.getLastName())))
                        && ((this.firstName == null && other.getFirstName() == null)
                        || (this.firstName != null && this.firstName.equals(other.getFirstName())))
                        && ((this.address == null && other.getAddress() == null)
                        || (this.address != null && this.address.equals(other.getAddress())))
                        && ((this.postalCode == null && other.getPostalCode() == null)
                        || (this.postalCode != null && this.postalCode.equals(other.getPostalCode())))
                        && ((this.postOfficeBox == null && other.getPostOfficeBox() == null)
                        || (this.postOfficeBox != null
                        && this.postOfficeBox.equals(other.getPostOfficeBox())))
                        && ((this.city == null && other.getCity() == null)
                        || (this.city != null && this.city.equals(other.getCity())))
                        && ((this.country == null && other.getCountry() == null)
                        || (this.country != null && this.country.equals(other.getCountry())))
                        && ((this.language == null && other.getLanguage() == null)
                        || (this.language != null && this.language.equals(other.getLanguage())))
                        && ((this.phone == null && other.getPhone() == null)
                        || (this.phone != null && this.phone.equals(other.getPhone())))
                        && ((this.fax == null && other.getFax() == null)
                        || (this.fax != null && this.fax.equals(other.getFax())))
                        && ((this.email == null && other.getEmail() == null)
                        || (this.email != null && this.email.equals(other.getEmail())))
                        && ((this.internet == null && other.getInternet() == null)
                        || (this.internet != null && this.internet.equals(other.getInternet())));
        __equalsCalc = null;
        return _equals;
    }

    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getTitle() != null) {
            _hashCode += getTitle().hashCode();
        }
        if (getLastName() != null) {
            _hashCode += getLastName().hashCode();
        }
        if (getFirstName() != null) {
            _hashCode += getFirstName().hashCode();
        }
        if (getAddress() != null) {
            _hashCode += getAddress().hashCode();
        }
        if (getPostalCode() != null) {
            _hashCode += getPostalCode().hashCode();
        }
        if (getPostOfficeBox() != null) {
            _hashCode += getPostOfficeBox().hashCode();
        }
        if (getCity() != null) {
            _hashCode += getCity().hashCode();
        }
        if (getCountry() != null) {
            _hashCode += getCountry().hashCode();
        }
        if (getLanguage() != null) {
            _hashCode += getLanguage().hashCode();
        }
        if (getPhone() != null) {
            _hashCode += getPhone().hashCode();
        }
        if (getFax() != null) {
            _hashCode += getFax().hashCode();
        }
        if (getEmail() != null) {
            _hashCode += getEmail().hashCode();
        }
        if (getInternet() != null) {
            _hashCode += getInternet().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }
}
