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
 * <p>FindCurrentInternalEntityRequest.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 * <p>
 * FindCurrentInternalEntityRequest.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
/**
 * FindCurrentInternalEntityRequest.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package eu.cec.digit.circabc.repo.hrs.ws;

/** Request the operation findCurrentInternalEntity */
public class FindCurrentInternalEntityRequest implements java.io.Serializable {

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
            new org.apache.axis.description.TypeDesc(FindCurrentInternalEntityRequest.class, true);

    static {
        typeDesc.setXmlType(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "FindCurrentInternalEntityRequest"));
        org.apache.axis.description.ElementDesc elemField =
                new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("searchForPerson");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "searchForPerson"));
        elemField.setXmlType(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types",
                        ">FindCurrentInternalEntityRequest>searchForPerson"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("searchForOrganization");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "searchForOrganization"));
        elemField.setXmlType(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types",
                        ">FindCurrentInternalEntityRequest>searchForOrganization"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("searchById");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "searchById"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "CurrentEntityId"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "currentEntityId"));
        typeDesc.addFieldDesc(elemField);
    }

    /* Search for an internal person */
    private eu.cec.digit.circabc.repo.hrs.ws.FindCurrentInternalEntityRequestSearchForPerson
            searchForPerson;
    /* Search for an internal organization */
    private eu.cec.digit.circabc.repo.hrs.ws.FindCurrentInternalEntityRequestSearchForOrganization
            searchForOrganization;
    /* Search for up to 50 current entities given their repository
     * ids */
    private java.lang.String[] searchById;
    private java.lang.Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;

    public FindCurrentInternalEntityRequest() {
    }

    public FindCurrentInternalEntityRequest(
            eu.cec.digit.circabc.repo.hrs.ws.FindCurrentInternalEntityRequestSearchForPerson
                    searchForPerson,
            eu.cec.digit.circabc.repo.hrs.ws.FindCurrentInternalEntityRequestSearchForOrganization
                    searchForOrganization,
            java.lang.String[] searchById) {
        this.searchForPerson = searchForPerson;
        this.searchForOrganization = searchForOrganization;
        this.searchById = searchById;
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
     * Gets the searchForPerson value for this FindCurrentInternalEntityRequest.
     *
     * @return searchForPerson * Search for an internal person
     */
    public eu.cec.digit.circabc.repo.hrs.ws.FindCurrentInternalEntityRequestSearchForPerson
    getSearchForPerson() {
        return searchForPerson;
    }

    /**
     * Sets the searchForPerson value for this FindCurrentInternalEntityRequest.
     *
     * @param searchForPerson * Search for an internal person
     */
    public void setSearchForPerson(
            eu.cec.digit.circabc.repo.hrs.ws.FindCurrentInternalEntityRequestSearchForPerson
                    searchForPerson) {
        this.searchForPerson = searchForPerson;
    }

    /**
     * Gets the searchForOrganization value for this FindCurrentInternalEntityRequest.
     *
     * @return searchForOrganization * Search for an internal organization
     */
    public eu.cec.digit.circabc.repo.hrs.ws.FindCurrentInternalEntityRequestSearchForOrganization
    getSearchForOrganization() {
        return searchForOrganization;
    }

    /**
     * Sets the searchForOrganization value for this FindCurrentInternalEntityRequest.
     *
     * @param searchForOrganization * Search for an internal organization
     */
    public void setSearchForOrganization(
            eu.cec.digit.circabc.repo.hrs.ws.FindCurrentInternalEntityRequestSearchForOrganization
                    searchForOrganization) {
        this.searchForOrganization = searchForOrganization;
    }

    /**
     * Gets the searchById value for this FindCurrentInternalEntityRequest.
     *
     * @return searchById * Search for up to 50 current entities given their repository ids
     */
    public java.lang.String[] getSearchById() {
        return searchById;
    }

    /**
     * Sets the searchById value for this FindCurrentInternalEntityRequest.
     *
     * @param searchById * Search for up to 50 current entities given their repository ids
     */
    public void setSearchById(java.lang.String[] searchById) {
        this.searchById = searchById;
    }

    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof FindCurrentInternalEntityRequest)) {
            return false;
        }
        FindCurrentInternalEntityRequest other = (FindCurrentInternalEntityRequest) obj;
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
                        && ((this.searchForPerson == null && other.getSearchForPerson() == null)
                        || (this.searchForPerson != null
                        && this.searchForPerson.equals(other.getSearchForPerson())))
                        && ((this.searchForOrganization == null && other.getSearchForOrganization() == null)
                        || (this.searchForOrganization != null
                        && this.searchForOrganization.equals(other.getSearchForOrganization())))
                        && ((this.searchById == null && other.getSearchById() == null)
                        || (this.searchById != null
                        && java.util.Arrays.equals(this.searchById, other.getSearchById())));
        __equalsCalc = null;
        return _equals;
    }

    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getSearchForPerson() != null) {
            _hashCode += getSearchForPerson().hashCode();
        }
        if (getSearchForOrganization() != null) {
            _hashCode += getSearchForOrganization().hashCode();
        }
        if (getSearchById() != null) {
            for (int i = 0; i < java.lang.reflect.Array.getLength(getSearchById()); i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getSearchById(), i);
                if (obj != null && !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        __hashCodeCalc = false;
        return _hashCode;
    }
}
