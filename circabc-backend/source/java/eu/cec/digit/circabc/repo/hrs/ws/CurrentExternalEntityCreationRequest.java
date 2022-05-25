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
 * <p>CurrentExternalEntityCreationRequest.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 * <p>
 * CurrentExternalEntityCreationRequest.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
/**
 * CurrentExternalEntityCreationRequest.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package eu.cec.digit.circabc.repo.hrs.ws;

/**
 * Request to create a new external entity. The external entity can be either a person or an
 * organization. The person can be linked to an organization or not. A person can be linked to at
 * most one organization.
 */
public class CurrentExternalEntityCreationRequest implements java.io.Serializable {

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
            new org.apache.axis.description.TypeDesc(CurrentExternalEntityCreationRequest.class, true);

    static {
        typeDesc.setXmlType(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "CurrentExternalEntityCreationRequest"));
        org.apache.axis.description.ElementDesc elemField =
                new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("externalPersonCreationRequest");
        elemField.setXmlName(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "externalPersonCreationRequest"));
        elemField.setXmlType(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "ExternalPersonCreationRequest"));
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

    private eu.cec.digit.circabc.repo.hrs.ws.ExternalPersonCreationRequest
            externalPersonCreationRequest;
    private eu.cec.digit.circabc.repo.hrs.ws.ExternalOrganizationCreationRequest
            externalOrganizationCreationRequest;
    private java.lang.Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;

    public CurrentExternalEntityCreationRequest() {
    }

    public CurrentExternalEntityCreationRequest(
            eu.cec.digit.circabc.repo.hrs.ws.ExternalPersonCreationRequest externalPersonCreationRequest,
            eu.cec.digit.circabc.repo.hrs.ws.ExternalOrganizationCreationRequest
                    externalOrganizationCreationRequest) {
        this.externalPersonCreationRequest = externalPersonCreationRequest;
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
     * Gets the externalPersonCreationRequest value for this CurrentExternalEntityCreationRequest.
     *
     * @return externalPersonCreationRequest
     */
    public eu.cec.digit.circabc.repo.hrs.ws.ExternalPersonCreationRequest
    getExternalPersonCreationRequest() {
        return externalPersonCreationRequest;
    }

    /**
     * Sets the externalPersonCreationRequest value for this CurrentExternalEntityCreationRequest.
     *
     * @param externalPersonCreationRequest
     */
    public void setExternalPersonCreationRequest(
            eu.cec.digit.circabc.repo.hrs.ws.ExternalPersonCreationRequest
                    externalPersonCreationRequest) {
        this.externalPersonCreationRequest = externalPersonCreationRequest;
    }

    /**
     * Gets the externalOrganizationCreationRequest value for this
     * CurrentExternalEntityCreationRequest.
     *
     * @return externalOrganizationCreationRequest
     */
    public eu.cec.digit.circabc.repo.hrs.ws.ExternalOrganizationCreationRequest
    getExternalOrganizationCreationRequest() {
        return externalOrganizationCreationRequest;
    }

    /**
     * Sets the externalOrganizationCreationRequest value for this
     * CurrentExternalEntityCreationRequest.
     *
     * @param externalOrganizationCreationRequest
     */
    public void setExternalOrganizationCreationRequest(
            eu.cec.digit.circabc.repo.hrs.ws.ExternalOrganizationCreationRequest
                    externalOrganizationCreationRequest) {
        this.externalOrganizationCreationRequest = externalOrganizationCreationRequest;
    }

    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof CurrentExternalEntityCreationRequest)) {
            return false;
        }
        CurrentExternalEntityCreationRequest other = (CurrentExternalEntityCreationRequest) obj;
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
                        && ((this.externalPersonCreationRequest == null
                        && other.getExternalPersonCreationRequest() == null)
                        || (this.externalPersonCreationRequest != null
                        && this.externalPersonCreationRequest.equals(
                        other.getExternalPersonCreationRequest())))
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
        if (getExternalPersonCreationRequest() != null) {
            _hashCode += getExternalPersonCreationRequest().hashCode();
        }
        if (getExternalOrganizationCreationRequest() != null) {
            _hashCode += getExternalOrganizationCreationRequest().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }
}
