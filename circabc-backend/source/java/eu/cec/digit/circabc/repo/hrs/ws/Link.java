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
 * <p>Link.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 * <p>
 * Link.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
/**
 * Link.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package eu.cec.digit.circabc.repo.hrs.ws;

/** A link between 2 documents */
public class Link implements java.io.Serializable {

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
            new org.apache.axis.description.TypeDesc(Link.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "Link"));
        org.apache.axis.description.ElementDesc elemField =
                new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("sourceDocumentId");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "sourceDocumentId"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("targetDocumentId");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "targetDocumentId"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("type");
        elemField.setXmlName(new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "type"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "LinkType"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /* Internal repository ID of the source document */
    private java.lang.String sourceDocumentId;
    /* Internal repository ID of the target document */
    private java.lang.String targetDocumentId;
    /* The type of link */
    private eu.cec.digit.circabc.repo.hrs.ws.LinkType type;
    private java.lang.Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;

    public Link() {
    }

    public Link(
            java.lang.String sourceDocumentId,
            java.lang.String targetDocumentId,
            eu.cec.digit.circabc.repo.hrs.ws.LinkType type) {
        this.sourceDocumentId = sourceDocumentId;
        this.targetDocumentId = targetDocumentId;
        this.type = type;
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
     * Gets the sourceDocumentId value for this Link.
     *
     * @return sourceDocumentId * Internal repository ID of the source document
     */
    public java.lang.String getSourceDocumentId() {
        return sourceDocumentId;
    }

    /**
     * Sets the sourceDocumentId value for this Link.
     *
     * @param sourceDocumentId * Internal repository ID of the source document
     */
    public void setSourceDocumentId(java.lang.String sourceDocumentId) {
        this.sourceDocumentId = sourceDocumentId;
    }

    /**
     * Gets the targetDocumentId value for this Link.
     *
     * @return targetDocumentId * Internal repository ID of the target document
     */
    public java.lang.String getTargetDocumentId() {
        return targetDocumentId;
    }

    /**
     * Sets the targetDocumentId value for this Link.
     *
     * @param targetDocumentId * Internal repository ID of the target document
     */
    public void setTargetDocumentId(java.lang.String targetDocumentId) {
        this.targetDocumentId = targetDocumentId;
    }

    /**
     * Gets the type value for this Link.
     *
     * @return type * The type of link
     */
    public eu.cec.digit.circabc.repo.hrs.ws.LinkType getType() {
        return type;
    }

    /**
     * Sets the type value for this Link.
     *
     * @param type * The type of link
     */
    public void setType(eu.cec.digit.circabc.repo.hrs.ws.LinkType type) {
        this.type = type;
    }

    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Link)) {
            return false;
        }
        Link other = (Link) obj;
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
                        && ((this.sourceDocumentId == null && other.getSourceDocumentId() == null)
                        || (this.sourceDocumentId != null
                        && this.sourceDocumentId.equals(other.getSourceDocumentId())))
                        && ((this.targetDocumentId == null && other.getTargetDocumentId() == null)
                        || (this.targetDocumentId != null
                        && this.targetDocumentId.equals(other.getTargetDocumentId())))
                        && ((this.type == null && other.getType() == null)
                        || (this.type != null && this.type.equals(other.getType())));
        __equalsCalc = null;
        return _equals;
    }

    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getSourceDocumentId() != null) {
            _hashCode += getSourceDocumentId().hashCode();
        }
        if (getTargetDocumentId() != null) {
            _hashCode += getTargetDocumentId().hashCode();
        }
        if (getType() != null) {
            _hashCode += getType().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }
}
