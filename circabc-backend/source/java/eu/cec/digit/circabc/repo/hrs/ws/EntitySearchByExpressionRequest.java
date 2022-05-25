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
 * <p>EntitySearchByExpressionRequest.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 * <p>
 * EntitySearchByExpressionRequest.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
/**
 * EntitySearchByExpressionRequest.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package eu.cec.digit.circabc.repo.hrs.ws;

/** Request for searching current external entities. */
public class EntitySearchByExpressionRequest implements java.io.Serializable {

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
            new org.apache.axis.description.TypeDesc(EntitySearchByExpressionRequest.class, true);

    static {
        typeDesc.setXmlType(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "EntitySearchByExpressionRequest"));
        org.apache.axis.description.ElementDesc elemField =
                new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("searchExpression");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "searchExpression"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("sortOptions");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "sortOptions"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", ">SortOptions>sortBy"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "sortBy"));
        typeDesc.addFieldDesc(elemField);
    }

    /* Expression to be used for searching */
    private java.lang.String searchExpression;
    private eu.cec.digit.circabc.repo.hrs.ws.SortOptionsSortBy[] sortOptions;
    private java.lang.Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;

    public EntitySearchByExpressionRequest() {
    }

    public EntitySearchByExpressionRequest(
            java.lang.String searchExpression,
            eu.cec.digit.circabc.repo.hrs.ws.SortOptionsSortBy[] sortOptions) {
        this.searchExpression = searchExpression;
        this.sortOptions = sortOptions;
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
     * Gets the searchExpression value for this EntitySearchByExpressionRequest.
     *
     * @return searchExpression * Expression to be used for searching
     */
    public java.lang.String getSearchExpression() {
        return searchExpression;
    }

    /**
     * Sets the searchExpression value for this EntitySearchByExpressionRequest.
     *
     * @param searchExpression * Expression to be used for searching
     */
    public void setSearchExpression(java.lang.String searchExpression) {
        this.searchExpression = searchExpression;
    }

    /**
     * Gets the sortOptions value for this EntitySearchByExpressionRequest.
     *
     * @return sortOptions
     */
    public eu.cec.digit.circabc.repo.hrs.ws.SortOptionsSortBy[] getSortOptions() {
        return sortOptions;
    }

    /**
     * Sets the sortOptions value for this EntitySearchByExpressionRequest.
     *
     * @param sortOptions
     */
    public void setSortOptions(eu.cec.digit.circabc.repo.hrs.ws.SortOptionsSortBy[] sortOptions) {
        this.sortOptions = sortOptions;
    }

    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof EntitySearchByExpressionRequest)) {
            return false;
        }
        EntitySearchByExpressionRequest other = (EntitySearchByExpressionRequest) obj;
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
                        && ((this.sortOptions == null && other.getSortOptions() == null)
                        || (this.sortOptions != null
                        && java.util.Arrays.equals(this.sortOptions, other.getSortOptions())));
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
        if (getSortOptions() != null) {
            for (int i = 0; i < java.lang.reflect.Array.getLength(getSortOptions()); i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getSortOptions(), i);
                if (obj != null && !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        __hashCodeCalc = false;
        return _hashCode;
    }
}
