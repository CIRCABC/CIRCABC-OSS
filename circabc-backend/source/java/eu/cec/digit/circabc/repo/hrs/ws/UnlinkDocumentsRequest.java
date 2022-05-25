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
 * <p>UnlinkDocumentsRequest.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 * <p>
 * UnlinkDocumentsRequest.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
/**
 * UnlinkDocumentsRequest.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package eu.cec.digit.circabc.repo.hrs.ws;

/** Request for removing a link between 2 documents. */
public class UnlinkDocumentsRequest implements java.io.Serializable {

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
            new org.apache.axis.description.TypeDesc(UnlinkDocumentsRequest.class, true);

    static {
        typeDesc.setXmlType(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "UnlinkDocumentsRequest"));
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
        elemField.setFieldName("linkType");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "linkType"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "LinkType"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    private java.lang.String sourceDocumentId;
    private java.lang.String targetDocumentId;
    private eu.cec.digit.circabc.repo.hrs.ws.LinkType linkType;
    private java.lang.Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;

    public UnlinkDocumentsRequest() {
    }

    public UnlinkDocumentsRequest(
            java.lang.String sourceDocumentId,
            java.lang.String targetDocumentId,
            eu.cec.digit.circabc.repo.hrs.ws.LinkType linkType) {
        this.sourceDocumentId = sourceDocumentId;
        this.targetDocumentId = targetDocumentId;
        this.linkType = linkType;
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
     * Gets the sourceDocumentId value for this UnlinkDocumentsRequest.
     *
     * @return sourceDocumentId
     */
    public java.lang.String getSourceDocumentId() {
        return sourceDocumentId;
    }

    /**
     * Sets the sourceDocumentId value for this UnlinkDocumentsRequest.
     *
     * @param sourceDocumentId
     */
    public void setSourceDocumentId(java.lang.String sourceDocumentId) {
        this.sourceDocumentId = sourceDocumentId;
    }

    /**
     * Gets the targetDocumentId value for this UnlinkDocumentsRequest.
     *
     * @return targetDocumentId
     */
    public java.lang.String getTargetDocumentId() {
        return targetDocumentId;
    }

    /**
     * Sets the targetDocumentId value for this UnlinkDocumentsRequest.
     *
     * @param targetDocumentId
     */
    public void setTargetDocumentId(java.lang.String targetDocumentId) {
        this.targetDocumentId = targetDocumentId;
    }

    /**
     * Gets the linkType value for this UnlinkDocumentsRequest.
     *
     * @return linkType
     */
    public eu.cec.digit.circabc.repo.hrs.ws.LinkType getLinkType() {
        return linkType;
    }

    /**
     * Sets the linkType value for this UnlinkDocumentsRequest.
     *
     * @param linkType
     */
    public void setLinkType(eu.cec.digit.circabc.repo.hrs.ws.LinkType linkType) {
        this.linkType = linkType;
    }

    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof UnlinkDocumentsRequest)) {
            return false;
        }
        UnlinkDocumentsRequest other = (UnlinkDocumentsRequest) obj;
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
                        && ((this.linkType == null && other.getLinkType() == null)
                        || (this.linkType != null && this.linkType.equals(other.getLinkType())));
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
        if (getLinkType() != null) {
            _hashCode += getLinkType().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }
}
