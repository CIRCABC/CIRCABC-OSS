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
 * <p>ExternalPersonCreationRequest.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 * <p>
 * ExternalPersonCreationRequest.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
/**
 * ExternalPersonCreationRequest.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package eu.cec.digit.circabc.repo.hrs.ws;

/** Values used for creating an external person linked to an external organization or not. */
public class ExternalPersonCreationRequest implements java.io.Serializable {

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
            new org.apache.axis.description.TypeDesc(ExternalPersonCreationRequest.class, true);

    static {
        typeDesc.setXmlType(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "ExternalPersonCreationRequest"));
        org.apache.axis.description.ElementDesc elemField =
                new org.apache.axis.description.ElementDesc();
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
        elemField.setFieldName("comments");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "comments"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("linkedToOrganization");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "linkedToOrganization"));
        elemField.setXmlType(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types",
                        ">ExternalPersonCreationRequest>linkedToOrganization"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /* Length: 255 bytes */
    private java.lang.String lastName;
    /* Length: 255 bytes */
    private java.lang.String firstName;
    private java.lang.String country;
    /* Length: 255 bytes */
    private java.lang.String city;
    /* Length: 255 bytes */
    private java.lang.String email;
    /* Length: 255 bytes */
    private java.lang.String alternateEmail;
    /* Length: 4000 bytes */
    private java.lang.String comments;
    private eu.cec.digit.circabc.repo.hrs.ws.ExternalPersonCreationRequestLinkedToOrganization
            linkedToOrganization;
    private java.lang.Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;

    public ExternalPersonCreationRequest() {
    }

    public ExternalPersonCreationRequest(
            java.lang.String lastName,
            java.lang.String firstName,
            java.lang.String country,
            java.lang.String city,
            java.lang.String email,
            java.lang.String alternateEmail,
            java.lang.String comments,
            eu.cec.digit.circabc.repo.hrs.ws.ExternalPersonCreationRequestLinkedToOrganization
                    linkedToOrganization) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.country = country;
        this.city = city;
        this.email = email;
        this.alternateEmail = alternateEmail;
        this.comments = comments;
        this.linkedToOrganization = linkedToOrganization;
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
     * Gets the lastName value for this ExternalPersonCreationRequest.
     *
     * @return lastName * Length: 255 bytes
     */
    public java.lang.String getLastName() {
        return lastName;
    }

    /**
     * Sets the lastName value for this ExternalPersonCreationRequest.
     *
     * @param lastName * Length: 255 bytes
     */
    public void setLastName(java.lang.String lastName) {
        this.lastName = lastName;
    }

    /**
     * Gets the firstName value for this ExternalPersonCreationRequest.
     *
     * @return firstName * Length: 255 bytes
     */
    public java.lang.String getFirstName() {
        return firstName;
    }

    /**
     * Sets the firstName value for this ExternalPersonCreationRequest.
     *
     * @param firstName * Length: 255 bytes
     */
    public void setFirstName(java.lang.String firstName) {
        this.firstName = firstName;
    }

    /**
     * Gets the country value for this ExternalPersonCreationRequest.
     *
     * @return country
     */
    public java.lang.String getCountry() {
        return country;
    }

    /**
     * Sets the country value for this ExternalPersonCreationRequest.
     *
     * @param country
     */
    public void setCountry(java.lang.String country) {
        this.country = country;
    }

    /**
     * Gets the city value for this ExternalPersonCreationRequest.
     *
     * @return city * Length: 255 bytes
     */
    public java.lang.String getCity() {
        return city;
    }

    /**
     * Sets the city value for this ExternalPersonCreationRequest.
     *
     * @param city * Length: 255 bytes
     */
    public void setCity(java.lang.String city) {
        this.city = city;
    }

    /**
     * Gets the email value for this ExternalPersonCreationRequest.
     *
     * @return email * Length: 255 bytes
     */
    public java.lang.String getEmail() {
        return email;
    }

    /**
     * Sets the email value for this ExternalPersonCreationRequest.
     *
     * @param email * Length: 255 bytes
     */
    public void setEmail(java.lang.String email) {
        this.email = email;
    }

    /**
     * Gets the alternateEmail value for this ExternalPersonCreationRequest.
     *
     * @return alternateEmail * Length: 255 bytes
     */
    public java.lang.String getAlternateEmail() {
        return alternateEmail;
    }

    /**
     * Sets the alternateEmail value for this ExternalPersonCreationRequest.
     *
     * @param alternateEmail * Length: 255 bytes
     */
    public void setAlternateEmail(java.lang.String alternateEmail) {
        this.alternateEmail = alternateEmail;
    }

    /**
     * Gets the comments value for this ExternalPersonCreationRequest.
     *
     * @return comments * Length: 4000 bytes
     */
    public java.lang.String getComments() {
        return comments;
    }

    /**
     * Sets the comments value for this ExternalPersonCreationRequest.
     *
     * @param comments * Length: 4000 bytes
     */
    public void setComments(java.lang.String comments) {
        this.comments = comments;
    }

    /**
     * Gets the linkedToOrganization value for this ExternalPersonCreationRequest.
     *
     * @return linkedToOrganization
     */
    public eu.cec.digit.circabc.repo.hrs.ws.ExternalPersonCreationRequestLinkedToOrganization
    getLinkedToOrganization() {
        return linkedToOrganization;
    }

    /**
     * Sets the linkedToOrganization value for this ExternalPersonCreationRequest.
     *
     * @param linkedToOrganization
     */
    public void setLinkedToOrganization(
            eu.cec.digit.circabc.repo.hrs.ws.ExternalPersonCreationRequestLinkedToOrganization
                    linkedToOrganization) {
        this.linkedToOrganization = linkedToOrganization;
    }

    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ExternalPersonCreationRequest)) {
            return false;
        }
        ExternalPersonCreationRequest other = (ExternalPersonCreationRequest) obj;
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
                        && ((this.country == null && other.getCountry() == null)
                        || (this.country != null && this.country.equals(other.getCountry())))
                        && ((this.city == null && other.getCity() == null)
                        || (this.city != null && this.city.equals(other.getCity())))
                        && ((this.email == null && other.getEmail() == null)
                        || (this.email != null && this.email.equals(other.getEmail())))
                        && ((this.alternateEmail == null && other.getAlternateEmail() == null)
                        || (this.alternateEmail != null
                        && this.alternateEmail.equals(other.getAlternateEmail())))
                        && ((this.comments == null && other.getComments() == null)
                        || (this.comments != null && this.comments.equals(other.getComments())))
                        && ((this.linkedToOrganization == null && other.getLinkedToOrganization() == null)
                        || (this.linkedToOrganization != null
                        && this.linkedToOrganization.equals(other.getLinkedToOrganization())));
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
        if (getComments() != null) {
            _hashCode += getComments().hashCode();
        }
        if (getLinkedToOrganization() != null) {
            _hashCode += getLinkedToOrganization().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }
}
