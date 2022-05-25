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
 * <p>ExternalOrganizationCreationRequest.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 * <p>
 * ExternalOrganizationCreationRequest.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
/**
 * ExternalOrganizationCreationRequest.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package eu.cec.digit.circabc.repo.hrs.ws;

/** Values used for creating an external organization. */
public class ExternalOrganizationCreationRequest implements java.io.Serializable {

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
            new org.apache.axis.description.TypeDesc(ExternalOrganizationCreationRequest.class, true);

    static {
        typeDesc.setXmlType(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "ExternalOrganizationCreationRequest"));
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
        elemField.setFieldName("country");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "country"));
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
        elemField.setFieldName("email");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "email"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("alternateEmail");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "alternateEmail"));
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
        elemField.setFieldName("comments");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "comments"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /* Length: 600 bytes */
    private java.lang.String name;
    /* Length: 255 bytes */
    private java.lang.String acronym;
    private java.lang.String country;
    /* Length: 100 bytes */
    private java.lang.String city;
    /* Length: 255 bytes */
    private java.lang.String email;
    /* Length: 255 bytes */
    private java.lang.String alternateEmail;
    /* Length: 255 bytes */
    private java.lang.String internet;
    /* Length: 4000 bytes */
    private java.lang.String comments;
    private java.lang.Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;

    public ExternalOrganizationCreationRequest() {
    }

    public ExternalOrganizationCreationRequest(
            java.lang.String name,
            java.lang.String acronym,
            java.lang.String country,
            java.lang.String city,
            java.lang.String email,
            java.lang.String alternateEmail,
            java.lang.String internet,
            java.lang.String comments) {
        this.name = name;
        this.acronym = acronym;
        this.country = country;
        this.city = city;
        this.email = email;
        this.alternateEmail = alternateEmail;
        this.internet = internet;
        this.comments = comments;
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
     * Gets the name value for this ExternalOrganizationCreationRequest.
     *
     * @return name * Length: 600 bytes
     */
    public java.lang.String getName() {
        return name;
    }

    /**
     * Sets the name value for this ExternalOrganizationCreationRequest.
     *
     * @param name * Length: 600 bytes
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }

    /**
     * Gets the acronym value for this ExternalOrganizationCreationRequest.
     *
     * @return acronym * Length: 255 bytes
     */
    public java.lang.String getAcronym() {
        return acronym;
    }

    /**
     * Sets the acronym value for this ExternalOrganizationCreationRequest.
     *
     * @param acronym * Length: 255 bytes
     */
    public void setAcronym(java.lang.String acronym) {
        this.acronym = acronym;
    }

    /**
     * Gets the country value for this ExternalOrganizationCreationRequest.
     *
     * @return country
     */
    public java.lang.String getCountry() {
        return country;
    }

    /**
     * Sets the country value for this ExternalOrganizationCreationRequest.
     *
     * @param country
     */
    public void setCountry(java.lang.String country) {
        this.country = country;
    }

    /**
     * Gets the city value for this ExternalOrganizationCreationRequest.
     *
     * @return city * Length: 100 bytes
     */
    public java.lang.String getCity() {
        return city;
    }

    /**
     * Sets the city value for this ExternalOrganizationCreationRequest.
     *
     * @param city * Length: 100 bytes
     */
    public void setCity(java.lang.String city) {
        this.city = city;
    }

    /**
     * Gets the email value for this ExternalOrganizationCreationRequest.
     *
     * @return email * Length: 255 bytes
     */
    public java.lang.String getEmail() {
        return email;
    }

    /**
     * Sets the email value for this ExternalOrganizationCreationRequest.
     *
     * @param email * Length: 255 bytes
     */
    public void setEmail(java.lang.String email) {
        this.email = email;
    }

    /**
     * Gets the alternateEmail value for this ExternalOrganizationCreationRequest.
     *
     * @return alternateEmail * Length: 255 bytes
     */
    public java.lang.String getAlternateEmail() {
        return alternateEmail;
    }

    /**
     * Sets the alternateEmail value for this ExternalOrganizationCreationRequest.
     *
     * @param alternateEmail * Length: 255 bytes
     */
    public void setAlternateEmail(java.lang.String alternateEmail) {
        this.alternateEmail = alternateEmail;
    }

    /**
     * Gets the internet value for this ExternalOrganizationCreationRequest.
     *
     * @return internet * Length: 255 bytes
     */
    public java.lang.String getInternet() {
        return internet;
    }

    /**
     * Sets the internet value for this ExternalOrganizationCreationRequest.
     *
     * @param internet * Length: 255 bytes
     */
    public void setInternet(java.lang.String internet) {
        this.internet = internet;
    }

    /**
     * Gets the comments value for this ExternalOrganizationCreationRequest.
     *
     * @return comments * Length: 4000 bytes
     */
    public java.lang.String getComments() {
        return comments;
    }

    /**
     * Sets the comments value for this ExternalOrganizationCreationRequest.
     *
     * @param comments * Length: 4000 bytes
     */
    public void setComments(java.lang.String comments) {
        this.comments = comments;
    }

    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ExternalOrganizationCreationRequest)) {
            return false;
        }
        ExternalOrganizationCreationRequest other = (ExternalOrganizationCreationRequest) obj;
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
                        && ((this.city == null && other.getCity() == null)
                        || (this.city != null && this.city.equals(other.getCity())))
                        && ((this.email == null && other.getEmail() == null)
                        || (this.email != null && this.email.equals(other.getEmail())))
                        && ((this.alternateEmail == null && other.getAlternateEmail() == null)
                        || (this.alternateEmail != null
                        && this.alternateEmail.equals(other.getAlternateEmail())))
                        && ((this.internet == null && other.getInternet() == null)
                        || (this.internet != null && this.internet.equals(other.getInternet())))
                        && ((this.comments == null && other.getComments() == null)
                        || (this.comments != null && this.comments.equals(other.getComments())));
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
        if (getCity() != null) {
            _hashCode += getCity().hashCode();
        }
        if (getEmail() != null) {
            _hashCode += getEmail().hashCode();
        }
        if (getAlternateEmail() != null) {
            _hashCode += getAlternateEmail().hashCode();
        }
        if (getInternet() != null) {
            _hashCode += getInternet().hashCode();
        }
        if (getComments() != null) {
            _hashCode += getComments().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }
}
