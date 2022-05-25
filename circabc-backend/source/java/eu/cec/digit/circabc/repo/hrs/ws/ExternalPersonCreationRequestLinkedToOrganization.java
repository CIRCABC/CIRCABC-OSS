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
 * <p>ExternalPersonCreationRequestLinkedToOrganization.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 * <p>
 * ExternalPersonCreationRequestLinkedToOrganization.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
/**
 * ExternalPersonCreationRequestLinkedToOrganization.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package eu.cec.digit.circabc.repo.hrs.ws;

public class ExternalPersonCreationRequestLinkedToOrganization implements java.io.Serializable {

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
            new org.apache.axis.description.TypeDesc(
                    ExternalPersonCreationRequestLinkedToOrganization.class, true);

    static {
        typeDesc.setXmlType(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types",
                        ">ExternalPersonCreationRequest>linkedToOrganization"));
        org.apache.axis.description.ElementDesc elemField =
                new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("currentEntityId");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "currentEntityId"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("externalOrganizationCreationRequest");
        elemField.setXmlName(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "externalOrganizationCreationRequest"));
        elemField.setXmlType(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "ExternalOrganizationCreationRequest"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /* The entity id of an existing external
     *                                         organization to link to */
    private java.lang.String currentEntityId;
    /* The organization to be created and linked to */
    private eu.cec.digit.circabc.repo.hrs.ws.ExternalOrganizationCreationRequest
            externalOrganizationCreationRequest;
    private java.lang.Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;

    public ExternalPersonCreationRequestLinkedToOrganization() {
    }

    public ExternalPersonCreationRequestLinkedToOrganization(
            java.lang.String currentEntityId,
            eu.cec.digit.circabc.repo.hrs.ws.ExternalOrganizationCreationRequest
                    externalOrganizationCreationRequest) {
        this.currentEntityId = currentEntityId;
        this.externalOrganizationCreationRequest = externalOrganizationCreationRequest;
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
     * Gets the currentEntityId value for this ExternalPersonCreationRequestLinkedToOrganization.
     *
     * @return currentEntityId * The entity id of an existing external organization to link to
     */
    public java.lang.String getCurrentEntityId() {
        return currentEntityId;
    }

    /**
     * Sets the currentEntityId value for this ExternalPersonCreationRequestLinkedToOrganization.
     *
     * @param currentEntityId * The entity id of an existing external organization to link to
     */
    public void setCurrentEntityId(java.lang.String currentEntityId) {
        this.currentEntityId = currentEntityId;
    }

    /**
     * Gets the externalOrganizationCreationRequest value for this
     * ExternalPersonCreationRequestLinkedToOrganization.
     *
     * @return externalOrganizationCreationRequest * The organization to be created and linked to
     */
    public eu.cec.digit.circabc.repo.hrs.ws.ExternalOrganizationCreationRequest
    getExternalOrganizationCreationRequest() {
        return externalOrganizationCreationRequest;
    }

    /**
     * Sets the externalOrganizationCreationRequest value for this
     * ExternalPersonCreationRequestLinkedToOrganization.
     *
     * @param externalOrganizationCreationRequest * The organization to be created and linked to
     */
    public void setExternalOrganizationCreationRequest(
            eu.cec.digit.circabc.repo.hrs.ws.ExternalOrganizationCreationRequest
                    externalOrganizationCreationRequest) {
        this.externalOrganizationCreationRequest = externalOrganizationCreationRequest;
    }

    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ExternalPersonCreationRequestLinkedToOrganization)) {
            return false;
        }
        ExternalPersonCreationRequestLinkedToOrganization other =
                (ExternalPersonCreationRequestLinkedToOrganization) obj;
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
                        && ((this.currentEntityId == null && other.getCurrentEntityId() == null)
                        || (this.currentEntityId != null
                        && this.currentEntityId.equals(other.getCurrentEntityId())))
                        && ((this.externalOrganizationCreationRequest == null
                        && other.getExternalOrganizationCreationRequest() == null)
                        || (this.externalOrganizationCreationRequest != null
                        && this.externalOrganizationCreationRequest.equals(
                        other.getExternalOrganizationCreationRequest())));
        __equalsCalc = null;
        return _equals;
    }

    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getCurrentEntityId() != null) {
            _hashCode += getCurrentEntityId().hashCode();
        }
        if (getExternalOrganizationCreationRequest() != null) {
            _hashCode += getExternalOrganizationCreationRequest().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }
}
