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
 * <p>InternalEntity.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 * <p>
 * InternalEntity.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
/**
 * InternalEntity.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package eu.cec.digit.circabc.repo.hrs.ws;

/** An internal entity */
public class InternalEntity extends eu.cec.digit.circabc.repo.hrs.ws.Entity
        implements java.io.Serializable {

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
            new org.apache.axis.description.TypeDesc(InternalEntity.class, true);

    static {
        typeDesc.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "InternalEntity"));
        org.apache.axis.description.ElementDesc elemField =
                new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("internalOrganization");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "internalOrganization"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "InternalOrganization"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("internalPerson");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "internalPerson"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "InternalPerson"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /* Internal organization */
    private eu.cec.digit.circabc.repo.hrs.ws.InternalOrganization internalOrganization;
    /* Internal person */
    private eu.cec.digit.circabc.repo.hrs.ws.InternalPerson internalPerson;
    private java.lang.Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;

    public InternalEntity() {
    }

    public InternalEntity(
            java.lang.String entityId,
            eu.cec.digit.circabc.repo.hrs.ws.InternalOrganization internalOrganization,
            eu.cec.digit.circabc.repo.hrs.ws.InternalPerson internalPerson) {
        super(entityId);
        this.internalOrganization = internalOrganization;
        this.internalPerson = internalPerson;
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
     * Gets the internalOrganization value for this InternalEntity.
     *
     * @return internalOrganization * Internal organization
     */
    public eu.cec.digit.circabc.repo.hrs.ws.InternalOrganization getInternalOrganization() {
        return internalOrganization;
    }

    /**
     * Sets the internalOrganization value for this InternalEntity.
     *
     * @param internalOrganization * Internal organization
     */
    public void setInternalOrganization(
            eu.cec.digit.circabc.repo.hrs.ws.InternalOrganization internalOrganization) {
        this.internalOrganization = internalOrganization;
    }

    /**
     * Gets the internalPerson value for this InternalEntity.
     *
     * @return internalPerson * Internal person
     */
    public eu.cec.digit.circabc.repo.hrs.ws.InternalPerson getInternalPerson() {
        return internalPerson;
    }

    /**
     * Sets the internalPerson value for this InternalEntity.
     *
     * @param internalPerson * Internal person
     */
    public void setInternalPerson(eu.cec.digit.circabc.repo.hrs.ws.InternalPerson internalPerson) {
        this.internalPerson = internalPerson;
    }

    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof InternalEntity)) {
            return false;
        }
        InternalEntity other = (InternalEntity) obj;
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
                        && ((this.internalOrganization == null && other.getInternalOrganization() == null)
                        || (this.internalOrganization != null
                        && this.internalOrganization.equals(other.getInternalOrganization())))
                        && ((this.internalPerson == null && other.getInternalPerson() == null)
                        || (this.internalPerson != null
                        && this.internalPerson.equals(other.getInternalPerson())));
        __equalsCalc = null;
        return _equals;
    }

    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = super.hashCode();
        if (getInternalOrganization() != null) {
            _hashCode += getInternalOrganization().hashCode();
        }
        if (getInternalPerson() != null) {
            _hashCode += getInternalPerson().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }
}
