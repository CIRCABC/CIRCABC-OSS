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
 * <p>DocumentSearchResponse.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 * <p>
 * DocumentSearchResponse.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
/**
 * DocumentSearchResponse.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package eu.cec.digit.circabc.repo.hrs.ws;

/** Common response for the operations searching for documents */
public class DocumentSearchResponse implements java.io.Serializable {

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
            new org.apache.axis.description.TypeDesc(DocumentSearchResponse.class, true);

    static {
        typeDesc.setXmlType(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "DocumentSearchResponse"));
        org.apache.axis.description.ElementDesc elemField =
                new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("totalRetrievable");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "totalRetrievable"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("document");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "document"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "Document"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /* The number of documents that can be retrieved.
     *                                    For performance reasons this is
     * limited to 2000 (even if there are
     *                                    more documents in the repository
     * to match the expression). */
    private int totalRetrievable;
    /* List of documents */
    private eu.cec.digit.circabc.repo.hrs.ws.Document[] document;
    private java.lang.Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;

    public DocumentSearchResponse() {
    }

    public DocumentSearchResponse(
            int totalRetrievable, eu.cec.digit.circabc.repo.hrs.ws.Document[] document) {
        this.totalRetrievable = totalRetrievable;
        this.document = document;
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
     * Gets the totalRetrievable value for this DocumentSearchResponse.
     *
     * @return totalRetrievable * The number of documents that can be retrieved. For performance
     *     reasons this is limited to 2000 (even if there are more documents in the repository to
     *     match the expression).
     */
    public int getTotalRetrievable() {
        return totalRetrievable;
    }

    /**
     * Sets the totalRetrievable value for this DocumentSearchResponse.
     *
     * @param totalRetrievable * The number of documents that can be retrieved. For performance
     *     reasons this is limited to 2000 (even if there are more documents in the repository to
     *     match the expression).
     */
    public void setTotalRetrievable(int totalRetrievable) {
        this.totalRetrievable = totalRetrievable;
    }

    /**
     * Gets the document value for this DocumentSearchResponse.
     *
     * @return document * List of documents
     */
    public eu.cec.digit.circabc.repo.hrs.ws.Document[] getDocument() {
        return document;
    }

    /**
     * Sets the document value for this DocumentSearchResponse.
     *
     * @param document * List of documents
     */
    public void setDocument(eu.cec.digit.circabc.repo.hrs.ws.Document[] document) {
        this.document = document;
    }

    public eu.cec.digit.circabc.repo.hrs.ws.Document getDocument(int i) {
        return this.document[i];
    }

    public void setDocument(int i, eu.cec.digit.circabc.repo.hrs.ws.Document _value) {
        this.document[i] = _value;
    }

    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof DocumentSearchResponse)) {
            return false;
        }
        DocumentSearchResponse other = (DocumentSearchResponse) obj;
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
                        && this.totalRetrievable == other.getTotalRetrievable()
                        && ((this.document == null && other.getDocument() == null)
                        || (this.document != null
                        && java.util.Arrays.equals(this.document, other.getDocument())));
        __equalsCalc = null;
        return _equals;
    }

    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        _hashCode += getTotalRetrievable();
        if (getDocument() != null) {
            for (int i = 0; i < java.lang.reflect.Array.getLength(getDocument()); i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getDocument(), i);
                if (obj != null && !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        __hashCodeCalc = false;
        return _hashCode;
    }
}
