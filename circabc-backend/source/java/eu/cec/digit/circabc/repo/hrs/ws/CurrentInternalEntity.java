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
 * <p>CurrentInternalEntity.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 * <p>
 * CurrentInternalEntity.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
/**
 * CurrentInternalEntity.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package eu.cec.digit.circabc.repo.hrs.ws;

/** An internal entity from the current base */
public class CurrentInternalEntity extends eu.cec.digit.circabc.repo.hrs.ws.CurrentEntity
        implements java.io.Serializable {

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
            new org.apache.axis.description.TypeDesc(CurrentInternalEntity.class, true);

    static {
        typeDesc.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "CurrentInternalEntity"));
        org.apache.axis.description.ElementDesc elemField =
                new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("currentInternalPerson");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "currentInternalPerson"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "CurrentInternalPerson"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("currentInternalOrganization");
        elemField.setXmlName(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "currentInternalOrganization"));
        elemField.setXmlType(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "CurrentInternalOrganization"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /* An internal person */
    private eu.cec.digit.circabc.repo.hrs.ws.CurrentInternalPerson currentInternalPerson;
    /* An internal organization */
    private eu.cec.digit.circabc.repo.hrs.ws.CurrentInternalOrganization currentInternalOrganization;
    private java.lang.Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;

    public CurrentInternalEntity() {
    }

    public CurrentInternalEntity(
            java.lang.String currentEntityId,
            eu.cec.digit.circabc.repo.hrs.ws.CurrentInternalPerson currentInternalPerson,
            eu.cec.digit.circabc.repo.hrs.ws.CurrentInternalOrganization currentInternalOrganization) {
        super(currentEntityId);
        this.currentInternalPerson = currentInternalPerson;
        this.currentInternalOrganization = currentInternalOrganization;
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
     * Gets the currentInternalPerson value for this CurrentInternalEntity.
     *
     * @return currentInternalPerson * An internal person
     */
    public eu.cec.digit.circabc.repo.hrs.ws.CurrentInternalPerson getCurrentInternalPerson() {
        return currentInternalPerson;
    }

    /**
     * Sets the currentInternalPerson value for this CurrentInternalEntity.
     *
     * @param currentInternalPerson * An internal person
     */
    public void setCurrentInternalPerson(
            eu.cec.digit.circabc.repo.hrs.ws.CurrentInternalPerson currentInternalPerson) {
        this.currentInternalPerson = currentInternalPerson;
    }

    /**
     * Gets the currentInternalOrganization value for this CurrentInternalEntity.
     *
     * @return currentInternalOrganization * An internal organization
     */
    public eu.cec.digit.circabc.repo.hrs.ws.CurrentInternalOrganization
    getCurrentInternalOrganization() {
        return currentInternalOrganization;
    }

    /**
     * Sets the currentInternalOrganization value for this CurrentInternalEntity.
     *
     * @param currentInternalOrganization * An internal organization
     */
    public void setCurrentInternalOrganization(
            eu.cec.digit.circabc.repo.hrs.ws.CurrentInternalOrganization currentInternalOrganization) {
        this.currentInternalOrganization = currentInternalOrganization;
    }

    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof CurrentInternalEntity)) {
            return false;
        }
        CurrentInternalEntity other = (CurrentInternalEntity) obj;
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
                        && ((this.currentInternalPerson == null && other.getCurrentInternalPerson() == null)
                        || (this.currentInternalPerson != null
                        && this.currentInternalPerson.equals(other.getCurrentInternalPerson())))
                        && ((this.currentInternalOrganization == null
                        && other.getCurrentInternalOrganization() == null)
                        || (this.currentInternalOrganization != null
                        && this.currentInternalOrganization.equals(
                        other.getCurrentInternalOrganization())));
        __equalsCalc = null;
        return _equals;
    }

    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = super.hashCode();
        if (getCurrentInternalPerson() != null) {
            _hashCode += getCurrentInternalPerson().hashCode();
        }
        if (getCurrentInternalOrganization() != null) {
            _hashCode += getCurrentInternalOrganization().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }
}
