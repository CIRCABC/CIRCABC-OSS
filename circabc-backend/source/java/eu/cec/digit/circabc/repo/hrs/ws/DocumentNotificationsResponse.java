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
 * <p>DocumentNotificationsResponse.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 * <p>
 * DocumentNotificationsResponse.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
/**
 * DocumentNotificationsResponse.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package eu.cec.digit.circabc.repo.hrs.ws;

/** Response for operations that return a list of document notifications. */
public class DocumentNotificationsResponse implements java.io.Serializable {

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
            new org.apache.axis.description.TypeDesc(DocumentNotificationsResponse.class, true);

    static {
        typeDesc.setXmlType(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "DocumentNotificationsResponse"));
        org.apache.axis.description.ElementDesc elemField =
                new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("total");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "total"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("documentNotification");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "documentNotification"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "DocumentNotification"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /* Number of results returned. */
    private int total;
    /* List of document notifications */
    private eu.cec.digit.circabc.repo.hrs.ws.DocumentNotification[] documentNotification;
    private java.lang.Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;

    public DocumentNotificationsResponse() {
    }

    public DocumentNotificationsResponse(
            int total, eu.cec.digit.circabc.repo.hrs.ws.DocumentNotification[] documentNotification) {
        this.total = total;
        this.documentNotification = documentNotification;
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
     * Gets the total value for this DocumentNotificationsResponse.
     *
     * @return total * Number of results returned.
     */
    public int getTotal() {
        return total;
    }

    /**
     * Sets the total value for this DocumentNotificationsResponse.
     *
     * @param total * Number of results returned.
     */
    public void setTotal(int total) {
        this.total = total;
    }

    /**
     * Gets the documentNotification value for this DocumentNotificationsResponse.
     *
     * @return documentNotification * List of document notifications
     */
    public eu.cec.digit.circabc.repo.hrs.ws.DocumentNotification[] getDocumentNotification() {
        return documentNotification;
    }

    /**
     * Sets the documentNotification value for this DocumentNotificationsResponse.
     *
     * @param documentNotification * List of document notifications
     */
    public void setDocumentNotification(
            eu.cec.digit.circabc.repo.hrs.ws.DocumentNotification[] documentNotification) {
        this.documentNotification = documentNotification;
    }

    public eu.cec.digit.circabc.repo.hrs.ws.DocumentNotification getDocumentNotification(int i) {
        return this.documentNotification[i];
    }

    public void setDocumentNotification(
            int i, eu.cec.digit.circabc.repo.hrs.ws.DocumentNotification _value) {
        this.documentNotification[i] = _value;
    }

    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof DocumentNotificationsResponse)) {
            return false;
        }
        DocumentNotificationsResponse other = (DocumentNotificationsResponse) obj;
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
                        && this.total == other.getTotal()
                        && ((this.documentNotification == null && other.getDocumentNotification() == null)
                        || (this.documentNotification != null
                        && java.util.Arrays.equals(
                        this.documentNotification, other.getDocumentNotification())));
        __equalsCalc = null;
        return _equals;
    }

    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        _hashCode += getTotal();
        if (getDocumentNotification() != null) {
            for (int i = 0; i < java.lang.reflect.Array.getLength(getDocumentNotification()); i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getDocumentNotification(), i);
                if (obj != null && !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        __hashCodeCalc = false;
        return _hashCode;
    }
}
