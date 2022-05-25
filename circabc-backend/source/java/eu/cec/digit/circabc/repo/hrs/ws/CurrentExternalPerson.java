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
 * <p>CurrentExternalPerson.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 * <p>
 * CurrentExternalPerson.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
/**
 * CurrentExternalPerson.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package eu.cec.digit.circabc.repo.hrs.ws;

/** An external person from the current base */
public class CurrentExternalPerson implements java.io.Serializable {

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
            new org.apache.axis.description.TypeDesc(CurrentExternalPerson.class, true);

    static {
        typeDesc.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "CurrentExternalPerson"));
        org.apache.axis.description.ElementDesc elemField =
                new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("id");
        elemField.setXmlName(new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "id"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("validationLevel");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "validationLevel"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "ValidationLevel"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
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
        elemField.setFieldName("address");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "address"));
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
        elemField.setFieldName("postOfficeBox");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "postOfficeBox"));
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
        elemField.setFieldName("title");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "title"));
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
        elemField.setFieldName("creationDate");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "creationDate"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "date"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("creator");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "creator"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("creatorDG");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "creatorDG"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("lastChangeDate");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "lastChangeDate"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "date"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("lastChangedBy");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "lastChangedBy"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /* Internal repository ID that identifies the external person */
    private java.lang.String id;
    /* Indicates if this external person has been validated and at
     * which level, e.g. CREATED_DG, VALIDATED_DG */
    private eu.cec.digit.circabc.repo.hrs.ws.ValidationLevel validationLevel;
    /* Person's last name. */
    private java.lang.String lastName;
    /* Person's first name(s) */
    private java.lang.String firstName;
    /* City where the external person lives. */
    private java.lang.String city;
    /* Country where the external person lives */
    private java.lang.String country;
    /* DEPRECATED. Mother tongue of the person */
    private java.lang.String language;
    /* DEPRECATED. Address of external person */
    private java.lang.String address;
    /* Email of external person */
    private java.lang.String email;
    /* Alternate email of external person */
    private java.lang.String alternateEmail;
    /* DEPRECATED. Internet address (e.g. homepage) of external person */
    private java.lang.String internet;
    /* DEPRECATED. Phone number of external person */
    private java.lang.String phone;
    /* DEPRECATED. Fax number of external person */
    private java.lang.String fax;
    /* DEPRECATED. Post office box of external person */
    private java.lang.String postOfficeBox;
    /* DEPRECATED. Postal code of external person */
    private java.lang.String postalCode;
    /* DEPRECATED. Title (e.g. Dr., Mr. etc) of external person's
     * name */
    private java.lang.String title;
    /* Custom user comments or deprecated data */
    private java.lang.String comments;
    /* creation date of external person */
    private java.util.Date creationDate;
    /* User (ecas id) that created the external person */
    private java.lang.String creator;
    /* DG of the user that created the external person */
    private java.lang.String creatorDG;
    /* Date when the external person was last modified */
    private java.util.Date lastChangeDate;
    /* User (ecas id) that modified the external person */
    private java.lang.String lastChangedBy;
    private java.lang.Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;

    public CurrentExternalPerson() {
    }

    public CurrentExternalPerson(
            java.lang.String id,
            eu.cec.digit.circabc.repo.hrs.ws.ValidationLevel validationLevel,
            java.lang.String lastName,
            java.lang.String firstName,
            java.lang.String city,
            java.lang.String country,
            java.lang.String language,
            java.lang.String address,
            java.lang.String email,
            java.lang.String alternateEmail,
            java.lang.String internet,
            java.lang.String phone,
            java.lang.String fax,
            java.lang.String postOfficeBox,
            java.lang.String postalCode,
            java.lang.String title,
            java.lang.String comments,
            java.util.Date creationDate,
            java.lang.String creator,
            java.lang.String creatorDG,
            java.util.Date lastChangeDate,
            java.lang.String lastChangedBy) {
        this.id = id;
        this.validationLevel = validationLevel;
        this.lastName = lastName;
        this.firstName = firstName;
        this.city = city;
        this.country = country;
        this.language = language;
        this.address = address;
        this.email = email;
        this.alternateEmail = alternateEmail;
        this.internet = internet;
        this.phone = phone;
        this.fax = fax;
        this.postOfficeBox = postOfficeBox;
        this.postalCode = postalCode;
        this.title = title;
        this.comments = comments;
        this.creationDate = creationDate;
        this.creator = creator;
        this.creatorDG = creatorDG;
        this.lastChangeDate = lastChangeDate;
        this.lastChangedBy = lastChangedBy;
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
     * Gets the id value for this CurrentExternalPerson.
     *
     * @return id * Internal repository ID that identifies the external person
     */
    public java.lang.String getId() {
        return id;
    }

    /**
     * Sets the id value for this CurrentExternalPerson.
     *
     * @param id * Internal repository ID that identifies the external person
     */
    public void setId(java.lang.String id) {
        this.id = id;
    }

    /**
     * Gets the validationLevel value for this CurrentExternalPerson.
     *
     * @return validationLevel * Indicates if this external person has been validated and at which
     *     level, e.g. CREATED_DG, VALIDATED_DG
     */
    public eu.cec.digit.circabc.repo.hrs.ws.ValidationLevel getValidationLevel() {
        return validationLevel;
    }

    /**
     * Sets the validationLevel value for this CurrentExternalPerson.
     *
     * @param validationLevel * Indicates if this external person has been validated and at which
     *     level, e.g. CREATED_DG, VALIDATED_DG
     */
    public void setValidationLevel(eu.cec.digit.circabc.repo.hrs.ws.ValidationLevel validationLevel) {
        this.validationLevel = validationLevel;
    }

    /**
     * Gets the lastName value for this CurrentExternalPerson.
     *
     * @return lastName * Person's last name.
     */
    public java.lang.String getLastName() {
        return lastName;
    }

    /**
     * Sets the lastName value for this CurrentExternalPerson.
     *
     * @param lastName * Person's last name.
     */
    public void setLastName(java.lang.String lastName) {
        this.lastName = lastName;
    }

    /**
     * Gets the firstName value for this CurrentExternalPerson.
     *
     * @return firstName * Person's first name(s)
     */
    public java.lang.String getFirstName() {
        return firstName;
    }

    /**
     * Sets the firstName value for this CurrentExternalPerson.
     *
     * @param firstName * Person's first name(s)
     */
    public void setFirstName(java.lang.String firstName) {
        this.firstName = firstName;
    }

    /**
     * Gets the city value for this CurrentExternalPerson.
     *
     * @return city * City where the external person lives.
     */
    public java.lang.String getCity() {
        return city;
    }

    /**
     * Sets the city value for this CurrentExternalPerson.
     *
     * @param city * City where the external person lives.
     */
    public void setCity(java.lang.String city) {
        this.city = city;
    }

    /**
     * Gets the country value for this CurrentExternalPerson.
     *
     * @return country * Country where the external person lives
     */
    public java.lang.String getCountry() {
        return country;
    }

    /**
     * Sets the country value for this CurrentExternalPerson.
     *
     * @param country * Country where the external person lives
     */
    public void setCountry(java.lang.String country) {
        this.country = country;
    }

    /**
     * Gets the language value for this CurrentExternalPerson.
     *
     * @return language * DEPRECATED. Mother tongue of the person
     */
    public java.lang.String getLanguage() {
        return language;
    }

    /**
     * Sets the language value for this CurrentExternalPerson.
     *
     * @param language * DEPRECATED. Mother tongue of the person
     */
    public void setLanguage(java.lang.String language) {
        this.language = language;
    }

    /**
     * Gets the address value for this CurrentExternalPerson.
     *
     * @return address * DEPRECATED. Address of external person
     */
    public java.lang.String getAddress() {
        return address;
    }

    /**
     * Sets the address value for this CurrentExternalPerson.
     *
     * @param address * DEPRECATED. Address of external person
     */
    public void setAddress(java.lang.String address) {
        this.address = address;
    }

    /**
     * Gets the email value for this CurrentExternalPerson.
     *
     * @return email * Email of external person
     */
    public java.lang.String getEmail() {
        return email;
    }

    /**
     * Sets the email value for this CurrentExternalPerson.
     *
     * @param email * Email of external person
     */
    public void setEmail(java.lang.String email) {
        this.email = email;
    }

    /**
     * Gets the alternateEmail value for this CurrentExternalPerson.
     *
     * @return alternateEmail * Alternate email of external person
     */
    public java.lang.String getAlternateEmail() {
        return alternateEmail;
    }

    /**
     * Sets the alternateEmail value for this CurrentExternalPerson.
     *
     * @param alternateEmail * Alternate email of external person
     */
    public void setAlternateEmail(java.lang.String alternateEmail) {
        this.alternateEmail = alternateEmail;
    }

    /**
     * Gets the internet value for this CurrentExternalPerson.
     *
     * @return internet * DEPRECATED. Internet address (e.g. homepage) of external person
     */
    public java.lang.String getInternet() {
        return internet;
    }

    /**
     * Sets the internet value for this CurrentExternalPerson.
     *
     * @param internet * DEPRECATED. Internet address (e.g. homepage) of external person
     */
    public void setInternet(java.lang.String internet) {
        this.internet = internet;
    }

    /**
     * Gets the phone value for this CurrentExternalPerson.
     *
     * @return phone * DEPRECATED. Phone number of external person
     */
    public java.lang.String getPhone() {
        return phone;
    }

    /**
     * Sets the phone value for this CurrentExternalPerson.
     *
     * @param phone * DEPRECATED. Phone number of external person
     */
    public void setPhone(java.lang.String phone) {
        this.phone = phone;
    }

    /**
     * Gets the fax value for this CurrentExternalPerson.
     *
     * @return fax * DEPRECATED. Fax number of external person
     */
    public java.lang.String getFax() {
        return fax;
    }

    /**
     * Sets the fax value for this CurrentExternalPerson.
     *
     * @param fax * DEPRECATED. Fax number of external person
     */
    public void setFax(java.lang.String fax) {
        this.fax = fax;
    }

    /**
     * Gets the postOfficeBox value for this CurrentExternalPerson.
     *
     * @return postOfficeBox * DEPRECATED. Post office box of external person
     */
    public java.lang.String getPostOfficeBox() {
        return postOfficeBox;
    }

    /**
     * Sets the postOfficeBox value for this CurrentExternalPerson.
     *
     * @param postOfficeBox * DEPRECATED. Post office box of external person
     */
    public void setPostOfficeBox(java.lang.String postOfficeBox) {
        this.postOfficeBox = postOfficeBox;
    }

    /**
     * Gets the postalCode value for this CurrentExternalPerson.
     *
     * @return postalCode * DEPRECATED. Postal code of external person
     */
    public java.lang.String getPostalCode() {
        return postalCode;
    }

    /**
     * Sets the postalCode value for this CurrentExternalPerson.
     *
     * @param postalCode * DEPRECATED. Postal code of external person
     */
    public void setPostalCode(java.lang.String postalCode) {
        this.postalCode = postalCode;
    }

    /**
     * Gets the title value for this CurrentExternalPerson.
     *
     * @return title * DEPRECATED. Title (e.g. Dr., Mr. etc) of external person's name
     */
    public java.lang.String getTitle() {
        return title;
    }

    /**
     * Sets the title value for this CurrentExternalPerson.
     *
     * @param title * DEPRECATED. Title (e.g. Dr., Mr. etc) of external person's name
     */
    public void setTitle(java.lang.String title) {
        this.title = title;
    }

    /**
     * Gets the comments value for this CurrentExternalPerson.
     *
     * @return comments * Custom user comments or deprecated data
     */
    public java.lang.String getComments() {
        return comments;
    }

    /**
     * Sets the comments value for this CurrentExternalPerson.
     *
     * @param comments * Custom user comments or deprecated data
     */
    public void setComments(java.lang.String comments) {
        this.comments = comments;
    }

    /**
     * Gets the creationDate value for this CurrentExternalPerson.
     *
     * @return creationDate * creation date of external person
     */
    public java.util.Date getCreationDate() {
        return creationDate;
    }

    /**
     * Sets the creationDate value for this CurrentExternalPerson.
     *
     * @param creationDate * creation date of external person
     */
    public void setCreationDate(java.util.Date creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * Gets the creator value for this CurrentExternalPerson.
     *
     * @return creator * User (ecas id) that created the external person
     */
    public java.lang.String getCreator() {
        return creator;
    }

    /**
     * Sets the creator value for this CurrentExternalPerson.
     *
     * @param creator * User (ecas id) that created the external person
     */
    public void setCreator(java.lang.String creator) {
        this.creator = creator;
    }

    /**
     * Gets the creatorDG value for this CurrentExternalPerson.
     *
     * @return creatorDG * DG of the user that created the external person
     */
    public java.lang.String getCreatorDG() {
        return creatorDG;
    }

    /**
     * Sets the creatorDG value for this CurrentExternalPerson.
     *
     * @param creatorDG * DG of the user that created the external person
     */
    public void setCreatorDG(java.lang.String creatorDG) {
        this.creatorDG = creatorDG;
    }

    /**
     * Gets the lastChangeDate value for this CurrentExternalPerson.
     *
     * @return lastChangeDate * Date when the external person was last modified
     */
    public java.util.Date getLastChangeDate() {
        return lastChangeDate;
    }

    /**
     * Sets the lastChangeDate value for this CurrentExternalPerson.
     *
     * @param lastChangeDate * Date when the external person was last modified
     */
    public void setLastChangeDate(java.util.Date lastChangeDate) {
        this.lastChangeDate = lastChangeDate;
    }

    /**
     * Gets the lastChangedBy value for this CurrentExternalPerson.
     *
     * @return lastChangedBy * User (ecas id) that modified the external person
     */
    public java.lang.String getLastChangedBy() {
        return lastChangedBy;
    }

    /**
     * Sets the lastChangedBy value for this CurrentExternalPerson.
     *
     * @param lastChangedBy * User (ecas id) that modified the external person
     */
    public void setLastChangedBy(java.lang.String lastChangedBy) {
        this.lastChangedBy = lastChangedBy;
    }

    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof CurrentExternalPerson)) {
            return false;
        }
        CurrentExternalPerson other = (CurrentExternalPerson) obj;
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
                        && ((this.validationLevel == null && other.getValidationLevel() == null)
                        || (this.validationLevel != null
                        && this.validationLevel.equals(other.getValidationLevel())))
                        && ((this.lastName == null && other.getLastName() == null)
                        || (this.lastName != null && this.lastName.equals(other.getLastName())))
                        && ((this.firstName == null && other.getFirstName() == null)
                        || (this.firstName != null && this.firstName.equals(other.getFirstName())))
                        && ((this.city == null && other.getCity() == null)
                        || (this.city != null && this.city.equals(other.getCity())))
                        && ((this.country == null && other.getCountry() == null)
                        || (this.country != null && this.country.equals(other.getCountry())))
                        && ((this.language == null && other.getLanguage() == null)
                        || (this.language != null && this.language.equals(other.getLanguage())))
                        && ((this.address == null && other.getAddress() == null)
                        || (this.address != null && this.address.equals(other.getAddress())))
                        && ((this.email == null && other.getEmail() == null)
                        || (this.email != null && this.email.equals(other.getEmail())))
                        && ((this.alternateEmail == null && other.getAlternateEmail() == null)
                        || (this.alternateEmail != null
                        && this.alternateEmail.equals(other.getAlternateEmail())))
                        && ((this.internet == null && other.getInternet() == null)
                        || (this.internet != null && this.internet.equals(other.getInternet())))
                        && ((this.phone == null && other.getPhone() == null)
                        || (this.phone != null && this.phone.equals(other.getPhone())))
                        && ((this.fax == null && other.getFax() == null)
                        || (this.fax != null && this.fax.equals(other.getFax())))
                        && ((this.postOfficeBox == null && other.getPostOfficeBox() == null)
                        || (this.postOfficeBox != null
                        && this.postOfficeBox.equals(other.getPostOfficeBox())))
                        && ((this.postalCode == null && other.getPostalCode() == null)
                        || (this.postalCode != null && this.postalCode.equals(other.getPostalCode())))
                        && ((this.title == null && other.getTitle() == null)
                        || (this.title != null && this.title.equals(other.getTitle())))
                        && ((this.comments == null && other.getComments() == null)
                        || (this.comments != null && this.comments.equals(other.getComments())))
                        && ((this.creationDate == null && other.getCreationDate() == null)
                        || (this.creationDate != null && this.creationDate.equals(other.getCreationDate())))
                        && ((this.creator == null && other.getCreator() == null)
                        || (this.creator != null && this.creator.equals(other.getCreator())))
                        && ((this.creatorDG == null && other.getCreatorDG() == null)
                        || (this.creatorDG != null && this.creatorDG.equals(other.getCreatorDG())))
                        && ((this.lastChangeDate == null && other.getLastChangeDate() == null)
                        || (this.lastChangeDate != null
                        && this.lastChangeDate.equals(other.getLastChangeDate())))
                        && ((this.lastChangedBy == null && other.getLastChangedBy() == null)
                        || (this.lastChangedBy != null
                        && this.lastChangedBy.equals(other.getLastChangedBy())));
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
        if (getValidationLevel() != null) {
            _hashCode += getValidationLevel().hashCode();
        }
        if (getLastName() != null) {
            _hashCode += getLastName().hashCode();
        }
        if (getFirstName() != null) {
            _hashCode += getFirstName().hashCode();
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
        if (getAddress() != null) {
            _hashCode += getAddress().hashCode();
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
        if (getPhone() != null) {
            _hashCode += getPhone().hashCode();
        }
        if (getFax() != null) {
            _hashCode += getFax().hashCode();
        }
        if (getPostOfficeBox() != null) {
            _hashCode += getPostOfficeBox().hashCode();
        }
        if (getPostalCode() != null) {
            _hashCode += getPostalCode().hashCode();
        }
        if (getTitle() != null) {
            _hashCode += getTitle().hashCode();
        }
        if (getComments() != null) {
            _hashCode += getComments().hashCode();
        }
        if (getCreationDate() != null) {
            _hashCode += getCreationDate().hashCode();
        }
        if (getCreator() != null) {
            _hashCode += getCreator().hashCode();
        }
        if (getCreatorDG() != null) {
            _hashCode += getCreatorDG().hashCode();
        }
        if (getLastChangeDate() != null) {
            _hashCode += getLastChangeDate().hashCode();
        }
        if (getLastChangedBy() != null) {
            _hashCode += getLastChangedBy().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }
}
