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
 * <p>ExternalOrganization.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 * <p>
 * ExternalOrganization.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
/**
 * ExternalOrganization.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package eu.cec.digit.circabc.repo.hrs.ws;

/** An external organization */
public class ExternalOrganization implements java.io.Serializable {

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
            new org.apache.axis.description.TypeDesc(ExternalOrganization.class, true);

    static {
        typeDesc.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "ExternalOrganization"));
        org.apache.axis.description.ElementDesc elemField =
                new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("name");
        elemField.setXmlName(new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "name"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
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
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("VAT");
        elemField.setXmlName(new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "VAT"));
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
    /* Address of the external organization */
    private java.lang.String address;
    /* Postal code */
    private java.lang.String postalCode;
    /* PO box */
    private java.lang.String postOfficeBox;
    /* City where the external organization is based */
    private java.lang.String city;
    /* Country of the external organization */
    private java.lang.String country;
    /* Communication language of the external organization */
    private java.lang.String language;
    /* Telephone number of the external organization */
    private java.lang.String phone;
    /* Fax number of the external organization */
    private java.lang.String fax;
    /* Email address of the external organization */
    private java.lang.String email;
    /* Internet address of website of the external organization */
    private java.lang.String internet;
    /* VAT code of the external organization */
    private java.lang.String VAT;
    private java.lang.Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;

    public ExternalOrganization() {
    }

    public ExternalOrganization(
            java.lang.String name,
            java.lang.String acronym,
            java.lang.String address,
            java.lang.String postalCode,
            java.lang.String postOfficeBox,
            java.lang.String city,
            java.lang.String country,
            java.lang.String language,
            java.lang.String phone,
            java.lang.String fax,
            java.lang.String email,
            java.lang.String internet,
            java.lang.String VAT) {
        this.name = name;
        this.acronym = acronym;
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
        this.VAT = VAT;
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
     * Gets the name value for this ExternalOrganization.
     *
     * @return name * Name of the organization
     */
    public java.lang.String getName() {
        return name;
    }

    /**
     * Sets the name value for this ExternalOrganization.
     *
     * @param name * Name of the organization
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }

    /**
     * Gets the acronym value for this ExternalOrganization.
     *
     * @return acronym * Acronym of the organization
     */
    public java.lang.String getAcronym() {
        return acronym;
    }

    /**
     * Sets the acronym value for this ExternalOrganization.
     *
     * @param acronym * Acronym of the organization
     */
    public void setAcronym(java.lang.String acronym) {
        this.acronym = acronym;
    }

    /**
     * Gets the address value for this ExternalOrganization.
     *
     * @return address * Address of the external organization
     */
    public java.lang.String getAddress() {
        return address;
    }

    /**
     * Sets the address value for this ExternalOrganization.
     *
     * @param address * Address of the external organization
     */
    public void setAddress(java.lang.String address) {
        this.address = address;
    }

    /**
     * Gets the postalCode value for this ExternalOrganization.
     *
     * @return postalCode * Postal code
     */
    public java.lang.String getPostalCode() {
        return postalCode;
    }

    /**
     * Sets the postalCode value for this ExternalOrganization.
     *
     * @param postalCode * Postal code
     */
    public void setPostalCode(java.lang.String postalCode) {
        this.postalCode = postalCode;
    }

    /**
     * Gets the postOfficeBox value for this ExternalOrganization.
     *
     * @return postOfficeBox * PO box
     */
    public java.lang.String getPostOfficeBox() {
        return postOfficeBox;
    }

    /**
     * Sets the postOfficeBox value for this ExternalOrganization.
     *
     * @param postOfficeBox * PO box
     */
    public void setPostOfficeBox(java.lang.String postOfficeBox) {
        this.postOfficeBox = postOfficeBox;
    }

    /**
     * Gets the city value for this ExternalOrganization.
     *
     * @return city * City where the external organization is based
     */
    public java.lang.String getCity() {
        return city;
    }

    /**
     * Sets the city value for this ExternalOrganization.
     *
     * @param city * City where the external organization is based
     */
    public void setCity(java.lang.String city) {
        this.city = city;
    }

    /**
     * Gets the country value for this ExternalOrganization.
     *
     * @return country * Country of the external organization
     */
    public java.lang.String getCountry() {
        return country;
    }

    /**
     * Sets the country value for this ExternalOrganization.
     *
     * @param country * Country of the external organization
     */
    public void setCountry(java.lang.String country) {
        this.country = country;
    }

    /**
     * Gets the language value for this ExternalOrganization.
     *
     * @return language * Communication language of the external organization
     */
    public java.lang.String getLanguage() {
        return language;
    }

    /**
     * Sets the language value for this ExternalOrganization.
     *
     * @param language * Communication language of the external organization
     */
    public void setLanguage(java.lang.String language) {
        this.language = language;
    }

    /**
     * Gets the phone value for this ExternalOrganization.
     *
     * @return phone * Telephone number of the external organization
     */
    public java.lang.String getPhone() {
        return phone;
    }

    /**
     * Sets the phone value for this ExternalOrganization.
     *
     * @param phone * Telephone number of the external organization
     */
    public void setPhone(java.lang.String phone) {
        this.phone = phone;
    }

    /**
     * Gets the fax value for this ExternalOrganization.
     *
     * @return fax * Fax number of the external organization
     */
    public java.lang.String getFax() {
        return fax;
    }

    /**
     * Sets the fax value for this ExternalOrganization.
     *
     * @param fax * Fax number of the external organization
     */
    public void setFax(java.lang.String fax) {
        this.fax = fax;
    }

    /**
     * Gets the email value for this ExternalOrganization.
     *
     * @return email * Email address of the external organization
     */
    public java.lang.String getEmail() {
        return email;
    }

    /**
     * Sets the email value for this ExternalOrganization.
     *
     * @param email * Email address of the external organization
     */
    public void setEmail(java.lang.String email) {
        this.email = email;
    }

    /**
     * Gets the internet value for this ExternalOrganization.
     *
     * @return internet * Internet address of website of the external organization
     */
    public java.lang.String getInternet() {
        return internet;
    }

    /**
     * Sets the internet value for this ExternalOrganization.
     *
     * @param internet * Internet address of website of the external organization
     */
    public void setInternet(java.lang.String internet) {
        this.internet = internet;
    }

    /**
     * Gets the VAT value for this ExternalOrganization.
     *
     * @return VAT * VAT code of the external organization
     */
    public java.lang.String getVAT() {
        return VAT;
    }

    /**
     * Sets the VAT value for this ExternalOrganization.
     *
     * @param VAT * VAT code of the external organization
     */
    public void setVAT(java.lang.String VAT) {
        this.VAT = VAT;
    }

    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ExternalOrganization)) {
            return false;
        }
        ExternalOrganization other = (ExternalOrganization) obj;
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
                        || (this.internet != null && this.internet.equals(other.getInternet())))
                        && ((this.VAT == null && other.getVAT() == null)
                        || (this.VAT != null && this.VAT.equals(other.getVAT())));
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
        if (getVAT() != null) {
            _hashCode += getVAT().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }
}
