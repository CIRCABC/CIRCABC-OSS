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
 * <p>Header.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 * <p>
 * Header.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
/**
 * Header.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package eu.cec.digit.circabc.repo.hrs.ws;

public class Header implements java.io.Serializable {

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
            new org.apache.axis.description.TypeDesc(Header.class, true);

    static {
        typeDesc.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "Header"));
        org.apache.axis.description.ElementDesc elemField =
                new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("userName");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "userName"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("onBehalfOf");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "onBehalfOf"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("ticket");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "ticket"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("applicationId");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "applicationId"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /* ECAS user name of the user / job that authenticates with HRS.
     *
     *                         For job users, you should use the ECAS job
     * user name (e.g. j00x000) and not
     *                         the internal job user name (e.g. je_xxxx). */
    private java.lang.String userName;
    /* User name of the user on which behalf the operation should
     * be performed.
     *                         Currently only delegations from normal users
     * to virtual entities are supported. */
    private java.lang.String onBehalfOf;
    /* ECAS ticket for the HRS service used for authentication. The
     * ticket should
     *                         belong to the user provided in userName. */
    private java.lang.String ticket;
    /* Application ID associated to the an application invoking HRS.
     * The application ID is used to identify and authorize the calling application */
    private java.lang.String applicationId;
    private java.lang.Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;

    public Header() {
    }

    public Header(
            java.lang.String userName,
            java.lang.String onBehalfOf,
            java.lang.String ticket,
            java.lang.String applicationId) {
        this.userName = userName;
        this.onBehalfOf = onBehalfOf;
        this.ticket = ticket;
        this.applicationId = applicationId;
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
     * Gets the userName value for this Header.
     *
     * @return userName * ECAS user name of the user / job that authenticates with HRS.
     *     <p>For job users, you should use the ECAS job user name (e.g. j00x000) and not the internal
     *     job user name (e.g. je_xxxx).
     */
    public java.lang.String getUserName() {
        return userName;
    }

    /**
     * Sets the userName value for this Header.
     *
     * @param userName * ECAS user name of the user / job that authenticates with HRS.
     *     <p>For job users, you should use the ECAS job user name (e.g. j00x000) and not the internal
     *     job user name (e.g. je_xxxx).
     */
    public void setUserName(java.lang.String userName) {
        this.userName = userName;
    }

    /**
     * Gets the onBehalfOf value for this Header.
     *
     * @return onBehalfOf * User name of the user on which behalf the operation should be performed.
     *     Currently only delegations from normal users to virtual entities are supported.
     */
    public java.lang.String getOnBehalfOf() {
        return onBehalfOf;
    }

    /**
     * Sets the onBehalfOf value for this Header.
     *
     * @param onBehalfOf * User name of the user on which behalf the operation should be performed.
     *     Currently only delegations from normal users to virtual entities are supported.
     */
    public void setOnBehalfOf(java.lang.String onBehalfOf) {
        this.onBehalfOf = onBehalfOf;
    }

    /**
     * Gets the ticket value for this Header.
     *
     * @return ticket * ECAS ticket for the HRS service used for authentication. The ticket should
     *     belong to the user provided in userName.
     */
    public java.lang.String getTicket() {
        return ticket;
    }

    /**
     * Sets the ticket value for this Header.
     *
     * @param ticket * ECAS ticket for the HRS service used for authentication. The ticket should
     *     belong to the user provided in userName.
     */
    public void setTicket(java.lang.String ticket) {
        this.ticket = ticket;
    }

    /**
     * Gets the applicationId value for this Header.
     *
     * @return applicationId * Application ID associated to the an application invoking HRS. The
     *     application ID is used to identify and authorize the calling application
     */
    public java.lang.String getApplicationId() {
        return applicationId;
    }

    /**
     * Sets the applicationId value for this Header.
     *
     * @param applicationId * Application ID associated to the an application invoking HRS. The
     *     application ID is used to identify and authorize the calling application
     */
    public void setApplicationId(java.lang.String applicationId) {
        this.applicationId = applicationId;
    }

    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Header)) {
            return false;
        }
        Header other = (Header) obj;
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
                        && ((this.userName == null && other.getUserName() == null)
                        || (this.userName != null && this.userName.equals(other.getUserName())))
                        && ((this.onBehalfOf == null && other.getOnBehalfOf() == null)
                        || (this.onBehalfOf != null && this.onBehalfOf.equals(other.getOnBehalfOf())))
                        && ((this.ticket == null && other.getTicket() == null)
                        || (this.ticket != null && this.ticket.equals(other.getTicket())))
                        && ((this.applicationId == null && other.getApplicationId() == null)
                        || (this.applicationId != null
                        && this.applicationId.equals(other.getApplicationId())));
        __equalsCalc = null;
        return _equals;
    }

    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getUserName() != null) {
            _hashCode += getUserName().hashCode();
        }
        if (getOnBehalfOf() != null) {
            _hashCode += getOnBehalfOf().hashCode();
        }
        if (getTicket() != null) {
            _hashCode += getTicket().hashCode();
        }
        if (getApplicationId() != null) {
            _hashCode += getApplicationId().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }
}
