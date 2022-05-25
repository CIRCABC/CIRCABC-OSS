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
 * <p>CurrentExternalEntity.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 * <p>
 * CurrentExternalEntity.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
/**
 * CurrentExternalEntity.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package eu.cec.digit.circabc.repo.hrs.ws;

/** An external organization from the current base */
public class CurrentExternalEntity extends eu.cec.digit.circabc.repo.hrs.ws.CurrentEntity
        implements java.io.Serializable {

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
            new org.apache.axis.description.TypeDesc(CurrentExternalEntity.class, true);

    static {
        typeDesc.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "CurrentExternalEntity"));
        org.apache.axis.description.ElementDesc elemField =
                new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("currentExternalPerson");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "currentExternalPerson"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "CurrentExternalPerson"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("currentExternalOrganization");
        elemField.setXmlName(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "currentExternalOrganization"));
        elemField.setXmlType(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "CurrentExternalOrganization"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /* An external person */
    private eu.cec.digit.circabc.repo.hrs.ws.CurrentExternalPerson currentExternalPerson;
    /* An external organization */
    private eu.cec.digit.circabc.repo.hrs.ws.CurrentExternalOrganization currentExternalOrganization;
    private java.lang.Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;

    public CurrentExternalEntity() {
    }

    public CurrentExternalEntity(
            java.lang.String currentEntityId,
            eu.cec.digit.circabc.repo.hrs.ws.CurrentExternalPerson currentExternalPerson,
            eu.cec.digit.circabc.repo.hrs.ws.CurrentExternalOrganization currentExternalOrganization) {
        super(currentEntityId);
        this.currentExternalPerson = currentExternalPerson;
        this.currentExternalOrganization = currentExternalOrganization;
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
     * Gets the currentExternalPerson value for this CurrentExternalEntity.
     *
     * @return currentExternalPerson * An external person
     */
    public eu.cec.digit.circabc.repo.hrs.ws.CurrentExternalPerson getCurrentExternalPerson() {
        return currentExternalPerson;
    }

    /**
     * Sets the currentExternalPerson value for this CurrentExternalEntity.
     *
     * @param currentExternalPerson * An external person
     */
    public void setCurrentExternalPerson(
            eu.cec.digit.circabc.repo.hrs.ws.CurrentExternalPerson currentExternalPerson) {
        this.currentExternalPerson = currentExternalPerson;
    }

    /**
     * Gets the currentExternalOrganization value for this CurrentExternalEntity.
     *
     * @return currentExternalOrganization * An external organization
     */
    public eu.cec.digit.circabc.repo.hrs.ws.CurrentExternalOrganization
    getCurrentExternalOrganization() {
        return currentExternalOrganization;
    }

    /**
     * Sets the currentExternalOrganization value for this CurrentExternalEntity.
     *
     * @param currentExternalOrganization * An external organization
     */
    public void setCurrentExternalOrganization(
            eu.cec.digit.circabc.repo.hrs.ws.CurrentExternalOrganization currentExternalOrganization) {
        this.currentExternalOrganization = currentExternalOrganization;
    }

    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof CurrentExternalEntity)) {
            return false;
        }
        CurrentExternalEntity other = (CurrentExternalEntity) obj;
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
                super.equals(obj)
                        && ((this.currentExternalPerson == null && other.getCurrentExternalPerson() == null)
                        || (this.currentExternalPerson != null
                        && this.currentExternalPerson.equals(other.getCurrentExternalPerson())))
                        && ((this.currentExternalOrganization == null
                        && other.getCurrentExternalOrganization() == null)
                        || (this.currentExternalOrganization != null
                        && this.currentExternalOrganization.equals(
                        other.getCurrentExternalOrganization())));
        __equalsCalc = null;
        return _equals;
    }

    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = super.hashCode();
        if (getCurrentExternalPerson() != null) {
            _hashCode += getCurrentExternalPerson().hashCode();
        }
        if (getCurrentExternalOrganization() != null) {
            _hashCode += getCurrentExternalOrganization().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }
}
