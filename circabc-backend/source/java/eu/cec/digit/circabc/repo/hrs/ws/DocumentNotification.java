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
 * <p>DocumentNotification.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 * <p>
 * DocumentNotification.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
/**
 * DocumentNotification.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package eu.cec.digit.circabc.repo.hrs.ws;

/** A notification about a document */
public class DocumentNotification implements java.io.Serializable {

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
            new org.apache.axis.description.TypeDesc(DocumentNotification.class, true);

    static {
        typeDesc.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "DocumentNotification"));
        org.apache.axis.description.ElementDesc elemField =
                new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("notificationId");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "notificationId"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("read");
        elemField.setXmlName(new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "read"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("followUp");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "followUp"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("document");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "document"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "Document"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("readOn");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "readOn"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "date"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /* Unique notification id */
    private java.lang.String notificationId;
    /* Is the notification read? */
    private boolean read;
    /* Has it been been marked for followup? */
    private boolean followUp;
    /* The document */
    private eu.cec.digit.circabc.repo.hrs.ws.Document document;
    /* Date when the notification has been read */
    private java.util.Date readOn;
    private java.lang.Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;

    public DocumentNotification() {
    }

    public DocumentNotification(
            java.lang.String notificationId,
            boolean read,
            boolean followUp,
            eu.cec.digit.circabc.repo.hrs.ws.Document document,
            java.util.Date readOn) {
        this.notificationId = notificationId;
        this.read = read;
        this.followUp = followUp;
        this.document = document;
        this.readOn = readOn;
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
     * Gets the notificationId value for this DocumentNotification.
     *
     * @return notificationId * Unique notification id
     */
    public java.lang.String getNotificationId() {
        return notificationId;
    }

    /**
     * Sets the notificationId value for this DocumentNotification.
     *
     * @param notificationId * Unique notification id
     */
    public void setNotificationId(java.lang.String notificationId) {
        this.notificationId = notificationId;
    }

    /**
     * Gets the read value for this DocumentNotification.
     *
     * @return read * Is the notification read?
     */
    public boolean isRead() {
        return read;
    }

    /**
     * Sets the read value for this DocumentNotification.
     *
     * @param read * Is the notification read?
     */
    public void setRead(boolean read) {
        this.read = read;
    }

    /**
     * Gets the followUp value for this DocumentNotification.
     *
     * @return followUp * Has it been been marked for followup?
     */
    public boolean isFollowUp() {
        return followUp;
    }

    /**
     * Sets the followUp value for this DocumentNotification.
     *
     * @param followUp * Has it been been marked for followup?
     */
    public void setFollowUp(boolean followUp) {
        this.followUp = followUp;
    }

    /**
     * Gets the document value for this DocumentNotification.
     *
     * @return document * The document
     */
    public eu.cec.digit.circabc.repo.hrs.ws.Document getDocument() {
        return document;
    }

    /**
     * Sets the document value for this DocumentNotification.
     *
     * @param document * The document
     */
    public void setDocument(eu.cec.digit.circabc.repo.hrs.ws.Document document) {
        this.document = document;
    }

    /**
     * Gets the readOn value for this DocumentNotification.
     *
     * @return readOn * Date when the notification has been read
     */
    public java.util.Date getReadOn() {
        return readOn;
    }

    /**
     * Sets the readOn value for this DocumentNotification.
     *
     * @param readOn * Date when the notification has been read
     */
    public void setReadOn(java.util.Date readOn) {
        this.readOn = readOn;
    }

    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof DocumentNotification)) {
            return false;
        }
        DocumentNotification other = (DocumentNotification) obj;
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
                        && ((this.notificationId == null && other.getNotificationId() == null)
                        || (this.notificationId != null
                        && this.notificationId.equals(other.getNotificationId())))
                        && this.read == other.isRead()
                        && this.followUp == other.isFollowUp()
                        && ((this.document == null && other.getDocument() == null)
                        || (this.document != null && this.document.equals(other.getDocument())))
                        && ((this.readOn == null && other.getReadOn() == null)
                        || (this.readOn != null && this.readOn.equals(other.getReadOn())));
        __equalsCalc = null;
        return _equals;
    }

    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getNotificationId() != null) {
            _hashCode += getNotificationId().hashCode();
        }
        _hashCode += (isRead() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        _hashCode += (isFollowUp() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        if (getDocument() != null) {
            _hashCode += getDocument().hashCode();
        }
        if (getReadOn() != null) {
            _hashCode += getReadOn().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }
}
