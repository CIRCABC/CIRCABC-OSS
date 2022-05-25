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
 * <p>SortOptionsSortBy.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 * <p>
 * SortOptionsSortBy.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
/**
 * SortOptionsSortBy.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package eu.cec.digit.circabc.repo.hrs.ws;

public class SortOptionsSortBy implements java.io.Serializable {

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
            new org.apache.axis.description.TypeDesc(SortOptionsSortBy.class, true);

    static {
        typeDesc.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", ">SortOptions>sortBy"));
        org.apache.axis.description.ElementDesc elemField =
                new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("fieldName");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "fieldName"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("order");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "order"));
        elemField.setXmlType(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", ">>SortOptions>sortBy>order"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /* The name of the field to sort on */
    private java.lang.String fieldName;
    /* The sort order */
    private eu.cec.digit.circabc.repo.hrs.ws.SortOptionsSortByOrder order;
    private java.lang.Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;

    public SortOptionsSortBy() {
    }

    public SortOptionsSortBy(
            java.lang.String fieldName, eu.cec.digit.circabc.repo.hrs.ws.SortOptionsSortByOrder order) {
        this.fieldName = fieldName;
        this.order = order;
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
     * Gets the fieldName value for this SortOptionsSortBy.
     *
     * @return fieldName * The name of the field to sort on
     */
    public java.lang.String getFieldName() {
        return fieldName;
    }

    /**
     * Sets the fieldName value for this SortOptionsSortBy.
     *
     * @param fieldName * The name of the field to sort on
     */
    public void setFieldName(java.lang.String fieldName) {
        this.fieldName = fieldName;
    }

    /**
     * Gets the order value for this SortOptionsSortBy.
     *
     * @return order * The sort order
     */
    public eu.cec.digit.circabc.repo.hrs.ws.SortOptionsSortByOrder getOrder() {
        return order;
    }

    /**
     * Sets the order value for this SortOptionsSortBy.
     *
     * @param order * The sort order
     */
    public void setOrder(eu.cec.digit.circabc.repo.hrs.ws.SortOptionsSortByOrder order) {
        this.order = order;
    }

    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof SortOptionsSortBy)) {
            return false;
        }
        SortOptionsSortBy other = (SortOptionsSortBy) obj;
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
                        && ((this.fieldName == null && other.getFieldName() == null)
                        || (this.fieldName != null && this.fieldName.equals(other.getFieldName())))
                        && ((this.order == null && other.getOrder() == null)
                        || (this.order != null && this.order.equals(other.getOrder())));
        __equalsCalc = null;
        return _equals;
    }

    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getFieldName() != null) {
            _hashCode += getFieldName().hashCode();
        }
        if (getOrder() != null) {
            _hashCode += getOrder().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }
}
