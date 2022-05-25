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
 * <p>CurrentExternalOrganization.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 * <p>
 * CurrentExternalOrganization.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
/**
 * CurrentExternalOrganization.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package eu.cec.digit.circabc.repo.hrs.ws;

/** An external organization from the current base */
public class CurrentExternalOrganization implements java.io.Serializable {

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
            new org.apache.axis.description.TypeDesc(CurrentExternalOrganization.class, true);

    static {
        typeDesc.setXmlType(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "CurrentExternalOrganization"));
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
        elemField.setFieldName("VAT");
        elemField.setXmlName(new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "VAT"));
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

    /* Internal repository ID that identifies the external organization */
    private java.lang.String id;
    /* Indicates if this external organization has been validated
     * and at which level, e.g. CREATED_DG, VALIDATED_DG */
    private eu.cec.digit.circabc.repo.hrs.ws.ValidationLevel validationLevel;
    /* Name of the organization. */
    private java.lang.String name;
    /* Acronym of the organization */
    private java.lang.String acronym;
    /* City of the organization */
    private java.lang.String city;
    /* Country of the organization. */
    private java.lang.String country;
    /* DEPRECATED. Communication language of the organization */
    private java.lang.String language;
    /* DEPRECATED. Address of external organization */
    private java.lang.String address;
    /* Email of external organization */
    private java.lang.String email;
    /* Alternate email of external organization */
    private java.lang.String alternateEmail;
    /* Internet address (e.g. homepage) of organization */
    private java.lang.String internet;
    /* DEPRECATED. Phone number of external organization */
    private java.lang.String phone;
    /* DEPRECATED. Fax number of external organization */
    private java.lang.String fax;
    /* DEPRECATED. Post office box of external organization */
    private java.lang.String postOfficeBox;
    /* DEPRECATED. Postal code of external organization */
    private java.lang.String postalCode;
    /* DEPRECATED. VAT number of organization */
    private java.lang.String VAT;
    /* Custom user comments or deprecated data */
    private java.lang.String comments;
    /* Creation date of external organization. */
    private java.util.Date creationDate;
    /* User (ecas id) that created the external organization */
    private java.lang.String creator;
    /* DG of the user that created the external organization */
    private java.lang.String creatorDG;
    /* Date when the external organization was last modified */
    private java.util.Date lastChangeDate;
    /* User (ecas id) that modified the external organization */
    private java.lang.String lastChangedBy;
    private java.lang.Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;

    public CurrentExternalOrganization() {
    }

    public CurrentExternalOrganization(
            java.lang.String id,
            eu.cec.digit.circabc.repo.hrs.ws.ValidationLevel validationLevel,
            java.lang.String name,
            java.lang.String acronym,
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
            java.lang.String VAT,
            java.lang.String comments,
            java.util.Date creationDate,
            java.lang.String creator,
            java.lang.String creatorDG,
            java.util.Date lastChangeDate,
            java.lang.String lastChangedBy) {
        this.id = id;
        this.validationLevel = validationLevel;
        this.name = name;
        this.acronym = acronym;
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
        this.VAT = VAT;
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
     * Gets the id value for this CurrentExternalOrganization.
     *
     * @return id * Internal repository ID that identifies the external organization
     */
    public java.lang.String getId() {
        return id;
    }

    /**
     * Sets the id value for this CurrentExternalOrganization.
     *
     * @param id * Internal repository ID that identifies the external organization
     */
    public void setId(java.lang.String id) {
        this.id = id;
    }

    /**
     * Gets the validationLevel value for this CurrentExternalOrganization.
     *
     * @return validationLevel * Indicates if this external organization has been validated and at
     *     which level, e.g. CREATED_DG, VALIDATED_DG
     */
    public eu.cec.digit.circabc.repo.hrs.ws.ValidationLevel getValidationLevel() {
        return validationLevel;
    }

    /**
     * Sets the validationLevel value for this CurrentExternalOrganization.
     *
     * @param validationLevel * Indicates if this external organization has been validated and at
     *     which level, e.g. CREATED_DG, VALIDATED_DG
     */
    public void setValidationLevel(eu.cec.digit.circabc.repo.hrs.ws.ValidationLevel validationLevel) {
        this.validationLevel = validationLevel;
    }

    /**
     * Gets the name value for this CurrentExternalOrganization.
     *
     * @return name * Name of the organization.
     */
    public java.lang.String getName() {
        return name;
    }

    /**
     * Sets the name value for this CurrentExternalOrganization.
     *
     * @param name * Name of the organization.
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }

    /**
     * Gets the acronym value for this CurrentExternalOrganization.
     *
     * @return acronym * Acronym of the organization
     */
    public java.lang.String getAcronym() {
        return acronym;
    }

    /**
     * Sets the acronym value for this CurrentExternalOrganization.
     *
     * @param acronym * Acronym of the organization
     */
    public void setAcronym(java.lang.String acronym) {
        this.acronym = acronym;
    }

    /**
     * Gets the city value for this CurrentExternalOrganization.
     *
     * @return city * City of the organization
     */
    public java.lang.String getCity() {
        return city;
    }

    /**
     * Sets the city value for this CurrentExternalOrganization.
     *
     * @param city * City of the organization
     */
    public void setCity(java.lang.String city) {
        this.city = city;
    }

    /**
     * Gets the country value for this CurrentExternalOrganization.
     *
     * @return country * Country of the organization.
     */
    public java.lang.String getCountry() {
        return country;
    }

    /**
     * Sets the country value for this CurrentExternalOrganization.
     *
     * @param country * Country of the organization.
     */
    public void setCountry(java.lang.String country) {
        this.country = country;
    }

    /**
     * Gets the language value for this CurrentExternalOrganization.
     *
     * @return language * DEPRECATED. Communication language of the organization
     */
    public java.lang.String getLanguage() {
        return language;
    }

    /**
     * Sets the language value for this CurrentExternalOrganization.
     *
     * @param language * DEPRECATED. Communication language of the organization
     */
    public void setLanguage(java.lang.String language) {
        this.language = language;
    }

    /**
     * Gets the address value for this CurrentExternalOrganization.
     *
     * @return address * DEPRECATED. Address of external organization
     */
    public java.lang.String getAddress() {
        return address;
    }

    /**
     * Sets the address value for this CurrentExternalOrganization.
     *
     * @param address * DEPRECATED. Address of external organization
     */
    public void setAddress(java.lang.String address) {
        this.address = address;
    }

    /**
     * Gets the email value for this CurrentExternalOrganization.
     *
     * @return email * Email of external organization
     */
    public java.lang.String getEmail() {
        return email;
    }

    /**
     * Sets the email value for this CurrentExternalOrganization.
     *
     * @param email * Email of external organization
     */
    public void setEmail(java.lang.String email) {
        this.email = email;
    }

    /**
     * Gets the alternateEmail value for this CurrentExternalOrganization.
     *
     * @return alternateEmail * Alternate email of external organization
     */
    public java.lang.String getAlternateEmail() {
        return alternateEmail;
    }

    /**
     * Sets the alternateEmail value for this CurrentExternalOrganization.
     *
     * @param alternateEmail * Alternate email of external organization
     */
    public void setAlternateEmail(java.lang.String alternateEmail) {
        this.alternateEmail = alternateEmail;
    }

    /**
     * Gets the internet value for this CurrentExternalOrganization.
     *
     * @return internet * Internet address (e.g. homepage) of organization
     */
    public java.lang.String getInternet() {
        return internet;
    }

    /**
     * Sets the internet value for this CurrentExternalOrganization.
     *
     * @param internet * Internet address (e.g. homepage) of organization
     */
    public void setInternet(java.lang.String internet) {
        this.internet = internet;
    }

    /**
     * Gets the phone value for this CurrentExternalOrganization.
     *
     * @return phone * DEPRECATED. Phone number of external organization
     */
    public java.lang.String getPhone() {
        return phone;
    }

    /**
     * Sets the phone value for this CurrentExternalOrganization.
     *
     * @param phone * DEPRECATED. Phone number of external organization
     */
    public void setPhone(java.lang.String phone) {
        this.phone = phone;
    }

    /**
     * Gets the fax value for this CurrentExternalOrganization.
     *
     * @return fax * DEPRECATED. Fax number of external organization
     */
    public java.lang.String getFax() {
        return fax;
    }

    /**
     * Sets the fax value for this CurrentExternalOrganization.
     *
     * @param fax * DEPRECATED. Fax number of external organization
     */
    public void setFax(java.lang.String fax) {
        this.fax = fax;
    }

    /**
     * Gets the postOfficeBox value for this CurrentExternalOrganization.
     *
     * @return postOfficeBox * DEPRECATED. Post office box of external organization
     */
    public java.lang.String getPostOfficeBox() {
        return postOfficeBox;
    }

    /**
     * Sets the postOfficeBox value for this CurrentExternalOrganization.
     *
     * @param postOfficeBox * DEPRECATED. Post office box of external organization
     */
    public void setPostOfficeBox(java.lang.String postOfficeBox) {
        this.postOfficeBox = postOfficeBox;
    }

    /**
     * Gets the postalCode value for this CurrentExternalOrganization.
     *
     * @return postalCode * DEPRECATED. Postal code of external organization
     */
    public java.lang.String getPostalCode() {
        return postalCode;
    }

    /**
     * Sets the postalCode value for this CurrentExternalOrganization.
     *
     * @param postalCode * DEPRECATED. Postal code of external organization
     */
    public void setPostalCode(java.lang.String postalCode) {
        this.postalCode = postalCode;
    }

    /**
     * Gets the VAT value for this CurrentExternalOrganization.
     *
     * @return VAT * DEPRECATED. VAT number of organization
     */
    public java.lang.String getVAT() {
        return VAT;
    }

    /**
     * Sets the VAT value for this CurrentExternalOrganization.
     *
     * @param VAT * DEPRECATED. VAT number of organization
     */
    public void setVAT(java.lang.String VAT) {
        this.VAT = VAT;
    }

    /**
     * Gets the comments value for this CurrentExternalOrganization.
     *
     * @return comments * Custom user comments or deprecated data
     */
    public java.lang.String getComments() {
        return comments;
    }

    /**
     * Sets the comments value for this CurrentExternalOrganization.
     *
     * @param comments * Custom user comments or deprecated data
     */
    public void setComments(java.lang.String comments) {
        this.comments = comments;
    }

    /**
     * Gets the creationDate value for this CurrentExternalOrganization.
     *
     * @return creationDate * Creation date of external organization.
     */
    public java.util.Date getCreationDate() {
        return creationDate;
    }

    /**
     * Sets the creationDate value for this CurrentExternalOrganization.
     *
     * @param creationDate * Creation date of external organization.
     */
    public void setCreationDate(java.util.Date creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * Gets the creator value for this CurrentExternalOrganization.
     *
     * @return creator * User (ecas id) that created the external organization
     */
    public java.lang.String getCreator() {
        return creator;
    }

    /**
     * Sets the creator value for this CurrentExternalOrganization.
     *
     * @param creator * User (ecas id) that created the external organization
     */
    public void setCreator(java.lang.String creator) {
        this.creator = creator;
    }

    /**
     * Gets the creatorDG value for this CurrentExternalOrganization.
     *
     * @return creatorDG * DG of the user that created the external organization
     */
    public java.lang.String getCreatorDG() {
        return creatorDG;
    }

    /**
     * Sets the creatorDG value for this CurrentExternalOrganization.
     *
     * @param creatorDG * DG of the user that created the external organization
     */
    public void setCreatorDG(java.lang.String creatorDG) {
        this.creatorDG = creatorDG;
    }

    /**
     * Gets the lastChangeDate value for this CurrentExternalOrganization.
     *
     * @return lastChangeDate * Date when the external organization was last modified
     */
    public java.util.Date getLastChangeDate() {
        return lastChangeDate;
    }

    /**
     * Sets the lastChangeDate value for this CurrentExternalOrganization.
     *
     * @param lastChangeDate * Date when the external organization was last modified
     */
    public void setLastChangeDate(java.util.Date lastChangeDate) {
        this.lastChangeDate = lastChangeDate;
    }

    /**
     * Gets the lastChangedBy value for this CurrentExternalOrganization.
     *
     * @return lastChangedBy * User (ecas id) that modified the external organization
     */
    public java.lang.String getLastChangedBy() {
        return lastChangedBy;
    }

    /**
     * Sets the lastChangedBy value for this CurrentExternalOrganization.
     *
     * @param lastChangedBy * User (ecas id) that modified the external organization
     */
    public void setLastChangedBy(java.lang.String lastChangedBy) {
        this.lastChangedBy = lastChangedBy;
    }

    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof CurrentExternalOrganization)) {
            return false;
        }
        CurrentExternalOrganization other = (CurrentExternalOrganization) obj;
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
                        && ((this.name == null && other.getName() == null)
                        || (this.name != null && this.name.equals(other.getName())))
                        && ((this.acronym == null && other.getAcronym() == null)
                        || (this.acronym != null && this.acronym.equals(other.getAcronym())))
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
                        && ((this.VAT == null && other.getVAT() == null)
                        || (this.VAT != null && this.VAT.equals(other.getVAT())))
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
        if (getName() != null) {
            _hashCode += getName().hashCode();
        }
        if (getAcronym() != null) {
            _hashCode += getAcronym().hashCode();
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
        if (getVAT() != null) {
            _hashCode += getVAT().hashCode();
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
