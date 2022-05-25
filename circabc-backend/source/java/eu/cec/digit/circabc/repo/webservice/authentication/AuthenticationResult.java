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
 * <p>AuthenticationResult.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 * <p>
 * AuthenticationResult.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
/**
 * AuthenticationResult.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package eu.cec.digit.circabc.repo.webservice.authentication;

public class AuthenticationResult implements java.io.Serializable {

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
            new org.apache.axis.description.TypeDesc(AuthenticationResult.class, true);

    static {
        typeDesc.setXmlType(
                new javax.xml.namespace.QName(
                        "http://www.cc.cec/circabc/ws/service/authentication/1.0", "AuthenticationResult"));
        org.apache.axis.description.ElementDesc elemField =
                new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("username");
        elemField.setXmlName(
                new javax.xml.namespace.QName(
                        "http://www.cc.cec/circabc/ws/service/authentication/1.0", "username"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("ticket");
        elemField.setXmlName(
                new javax.xml.namespace.QName(
                        "http://www.cc.cec/circabc/ws/service/authentication/1.0", "ticket"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("sessionid");
        elemField.setXmlName(
                new javax.xml.namespace.QName(
                        "http://www.cc.cec/circabc/ws/service/authentication/1.0", "sessionid"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
    }

    private java.lang.String username;
    private java.lang.String ticket;
    private java.lang.String sessionid;
    private java.lang.Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;

    public AuthenticationResult() {
    }

    public AuthenticationResult(
            java.lang.String username, java.lang.String ticket, java.lang.String sessionid) {
        this.username = username;
        this.ticket = ticket;
        this.sessionid = sessionid;
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
     * Gets the username value for this AuthenticationResult.
     *
     * @return username
     */
    public java.lang.String getUsername() {
        return username;
    }

    /**
     * Sets the username value for this AuthenticationResult.
     *
     * @param username
     */
    public void setUsername(java.lang.String username) {
        this.username = username;
    }

    /**
     * Gets the ticket value for this AuthenticationResult.
     *
     * @return ticket
     */
    public java.lang.String getTicket() {
        return ticket;
    }

    /**
     * Sets the ticket value for this AuthenticationResult.
     *
     * @param ticket
     */
    public void setTicket(java.lang.String ticket) {
        this.ticket = ticket;
    }

    /**
     * Gets the sessionid value for this AuthenticationResult.
     *
     * @return sessionid
     */
    public java.lang.String getSessionid() {
        return sessionid;
    }

    /**
     * Sets the sessionid value for this AuthenticationResult.
     *
     * @param sessionid
     */
    public void setSessionid(java.lang.String sessionid) {
        this.sessionid = sessionid;
    }

    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof AuthenticationResult)) {
            return false;
        }
        AuthenticationResult other = (AuthenticationResult) obj;
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
                        && ((this.username == null && other.getUsername() == null)
                        || (this.username != null && this.username.equals(other.getUsername())))
                        && ((this.ticket == null && other.getTicket() == null)
                        || (this.ticket != null && this.ticket.equals(other.getTicket())))
                        && ((this.sessionid == null && other.getSessionid() == null)
                        || (this.sessionid != null && this.sessionid.equals(other.getSessionid())));
        __equalsCalc = null;
        return _equals;
    }

    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getUsername() != null) {
            _hashCode += getUsername().hashCode();
        }
        if (getTicket() != null) {
            _hashCode += getTicket().hashCode();
        }
        if (getSessionid() != null) {
            _hashCode += getSessionid().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }
}
