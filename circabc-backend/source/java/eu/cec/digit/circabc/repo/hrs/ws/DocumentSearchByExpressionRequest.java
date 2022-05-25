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
 * <p>DocumentSearchByExpressionRequest.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 * <p>
 * DocumentSearchByExpressionRequest.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
/**
 * DocumentSearchByExpressionRequest.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package eu.cec.digit.circabc.repo.hrs.ws;

/** Request for searching documents. */
public class DocumentSearchByExpressionRequest implements java.io.Serializable {

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
            new org.apache.axis.description.TypeDesc(DocumentSearchByExpressionRequest.class, true);

    static {
        typeDesc.setXmlType(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "DocumentSearchByExpressionRequest"));
        org.apache.axis.description.ElementDesc elemField =
                new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("searchExpression");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "searchExpression"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("fulltextSearchExpression");
        elemField.setXmlName(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "fulltextSearchExpression"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("skip");
        elemField.setXmlName(new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "skip"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("max");
        elemField.setXmlName(new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "max"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("additionalPartitions");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "additionalPartitions"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "partitionId"));
        typeDesc.addFieldDesc(elemField);
    }

    /* Expression to be used for searching */
    private java.lang.String searchExpression;
    /* Fulltext search expression.
     *                         Fulltext search will not retrieve documents
     * beyond the
     *                         300 limit (e.g. skip=299, max=10 will always
     * produce at most one document
     *                         with fulltext). */
    private java.lang.String fulltextSearchExpression;
    /* Skip N documents. This field is used to implement a mechanism
     * of
     *                         pagination. */
    private java.lang.Integer skip;
    /* Maximum number of documents to be retrieved by the search.
     *                         If this value is greater than 300, only basic
     * metadata of the document
     *                         can be retrieved for performance reasons,
     * so specifying any options in
     *                         DocumentRetrievalOptions is not allowed. */
    private java.lang.Integer max;
    /* If omitted the search will be executed in the partition to
     * which the application
     *                     belongs.
     *                     If present it indicates that the search should
     * also be executed in the given
     *                     list of additional partitions. The search will
     * always be executed in the
     *                     partition to which the application belongs and
     * extended to the other partitions.
     *
     *                     When specifying additional partitions they must
     * be public. A SOAPFault will be
     *                     thrown if this condition is not met. */
    private int[] additionalPartitions;
    private java.lang.Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;

    public DocumentSearchByExpressionRequest() {
    }

    public DocumentSearchByExpressionRequest(
            java.lang.String searchExpression,
            java.lang.String fulltextSearchExpression,
            java.lang.Integer skip,
            java.lang.Integer max,
            int[] additionalPartitions) {
        this.searchExpression = searchExpression;
        this.fulltextSearchExpression = fulltextSearchExpression;
        this.skip = skip;
        this.max = max;
        this.additionalPartitions = additionalPartitions;
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
     * Gets the searchExpression value for this DocumentSearchByExpressionRequest.
     *
     * @return searchExpression * Expression to be used for searching
     */
    public java.lang.String getSearchExpression() {
        return searchExpression;
    }

    /**
     * Sets the searchExpression value for this DocumentSearchByExpressionRequest.
     *
     * @param searchExpression * Expression to be used for searching
     */
    public void setSearchExpression(java.lang.String searchExpression) {
        this.searchExpression = searchExpression;
    }

    /**
     * Gets the fulltextSearchExpression value for this DocumentSearchByExpressionRequest.
     *
     * @return fulltextSearchExpression * Fulltext search expression. Fulltext search will not
     *     retrieve documents beyond the 300 limit (e.g. skip=299, max=10 will always produce at most
     *     one document with fulltext).
     */
    public java.lang.String getFulltextSearchExpression() {
        return fulltextSearchExpression;
    }

    /**
     * Sets the fulltextSearchExpression value for this DocumentSearchByExpressionRequest.
     *
     * @param fulltextSearchExpression * Fulltext search expression. Fulltext search will not retrieve
     *     documents beyond the 300 limit (e.g. skip=299, max=10 will always produce at most one
     *     document with fulltext).
     */
    public void setFulltextSearchExpression(java.lang.String fulltextSearchExpression) {
        this.fulltextSearchExpression = fulltextSearchExpression;
    }

    /**
     * Gets the skip value for this DocumentSearchByExpressionRequest.
     *
     * @return skip * Skip N documents. This field is used to implement a mechanism of pagination.
     */
    public java.lang.Integer getSkip() {
        return skip;
    }

    /**
     * Sets the skip value for this DocumentSearchByExpressionRequest.
     *
     * @param skip * Skip N documents. This field is used to implement a mechanism of pagination.
     */
    public void setSkip(java.lang.Integer skip) {
        this.skip = skip;
    }

    /**
     * Gets the max value for this DocumentSearchByExpressionRequest.
     *
     * @return max * Maximum number of documents to be retrieved by the search. If this value is
     *     greater than 300, only basic metadata of the document can be retrieved for performance
     *     reasons, so specifying any options in DocumentRetrievalOptions is not allowed.
     */
    public java.lang.Integer getMax() {
        return max;
    }

    /**
     * Sets the max value for this DocumentSearchByExpressionRequest.
     *
     * @param max * Maximum number of documents to be retrieved by the search. If this value is
     *     greater than 300, only basic metadata of the document can be retrieved for performance
     *     reasons, so specifying any options in DocumentRetrievalOptions is not allowed.
     */
    public void setMax(java.lang.Integer max) {
        this.max = max;
    }

    /**
     * Gets the additionalPartitions value for this DocumentSearchByExpressionRequest.
     *
     * @return additionalPartitions * If omitted the search will be executed in the partition to which
     *     the application belongs. If present it indicates that the search should also be executed in
     *     the given list of additional partitions. The search will always be executed in the
     *     partition to which the application belongs and extended to the other partitions.
     *     <p>When specifying additional partitions they must be public. A SOAPFault will be thrown if
     *     this condition is not met.
     */
    public int[] getAdditionalPartitions() {
        return additionalPartitions;
    }

    /**
     * Sets the additionalPartitions value for this DocumentSearchByExpressionRequest.
     *
     * @param additionalPartitions * If omitted the search will be executed in the partition to which
     *     the application belongs. If present it indicates that the search should also be executed in
     *     the given list of additional partitions. The search will always be executed in the
     *     partition to which the application belongs and extended to the other partitions.
     *     <p>When specifying additional partitions they must be public. A SOAPFault will be thrown if
     *     this condition is not met.
     */
    public void setAdditionalPartitions(int[] additionalPartitions) {
        this.additionalPartitions = additionalPartitions;
    }

    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof DocumentSearchByExpressionRequest)) {
            return false;
        }
        DocumentSearchByExpressionRequest other = (DocumentSearchByExpressionRequest) obj;
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
                        && ((this.searchExpression == null && other.getSearchExpression() == null)
                        || (this.searchExpression != null
                        && this.searchExpression.equals(other.getSearchExpression())))
                        && ((this.fulltextSearchExpression == null
                        && other.getFulltextSearchExpression() == null)
                        || (this.fulltextSearchExpression != null
                        && this.fulltextSearchExpression.equals(other.getFulltextSearchExpression())))
                        && ((this.skip == null && other.getSkip() == null)
                        || (this.skip != null && this.skip.equals(other.getSkip())))
                        && ((this.max == null && other.getMax() == null)
                        || (this.max != null && this.max.equals(other.getMax())))
                        && ((this.additionalPartitions == null && other.getAdditionalPartitions() == null)
                        || (this.additionalPartitions != null
                        && java.util.Arrays.equals(
                        this.additionalPartitions, other.getAdditionalPartitions())));
        __equalsCalc = null;
        return _equals;
    }

    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getSearchExpression() != null) {
            _hashCode += getSearchExpression().hashCode();
        }
        if (getFulltextSearchExpression() != null) {
            _hashCode += getFulltextSearchExpression().hashCode();
        }
        if (getSkip() != null) {
            _hashCode += getSkip().hashCode();
        }
        if (getMax() != null) {
            _hashCode += getMax().hashCode();
        }
        if (getAdditionalPartitions() != null) {
            for (int i = 0; i < java.lang.reflect.Array.getLength(getAdditionalPartitions()); i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getAdditionalPartitions(), i);
                if (obj != null && !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        __hashCodeCalc = false;
        return _hashCode;
    }
}
