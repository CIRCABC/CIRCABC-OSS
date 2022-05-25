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
 * <p>DocumentUpdateRequestUpdateSenders.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 * <p>
 * DocumentUpdateRequestUpdateSenders.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
/**
 * DocumentUpdateRequestUpdateSenders.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package eu.cec.digit.circabc.repo.hrs.ws;

public class DocumentUpdateRequestUpdateSenders implements java.io.Serializable {

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
            new org.apache.axis.description.TypeDesc(DocumentUpdateRequestUpdateSenders.class, true);

    static {
        typeDesc.setXmlType(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", ">DocumentUpdateRequest>updateSenders"));
        org.apache.axis.description.ElementDesc elemField =
                new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("remove");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "remove"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "ObjectId"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("add");
        elemField.setXmlName(new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "add"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "CurrentEntityId"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
    }

    /* Remove the specified entity from senders */
    private java.lang.String[] remove;
    /* Add the specified current entity as a sender */
    private java.lang.String[] add;
    private java.lang.Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;

    public DocumentUpdateRequestUpdateSenders() {
    }

    public DocumentUpdateRequestUpdateSenders(java.lang.String[] remove, java.lang.String[] add) {
        this.remove = remove;
        this.add = add;
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
     * Gets the remove value for this DocumentUpdateRequestUpdateSenders.
     *
     * @return remove * Remove the specified entity from senders
     */
    public java.lang.String[] getRemove() {
        return remove;
    }

    /**
     * Sets the remove value for this DocumentUpdateRequestUpdateSenders.
     *
     * @param remove * Remove the specified entity from senders
     */
    public void setRemove(java.lang.String[] remove) {
        this.remove = remove;
    }

    public java.lang.String getRemove(int i) {
        return this.remove[i];
    }

    public void setRemove(int i, java.lang.String _value) {
        this.remove[i] = _value;
    }

    /**
     * Gets the add value for this DocumentUpdateRequestUpdateSenders.
     *
     * @return add * Add the specified current entity as a sender
     */
    public java.lang.String[] getAdd() {
        return add;
    }

    /**
     * Sets the add value for this DocumentUpdateRequestUpdateSenders.
     *
     * @param add * Add the specified current entity as a sender
     */
    public void setAdd(java.lang.String[] add) {
        this.add = add;
    }

    public java.lang.String getAdd(int i) {
        return this.add[i];
    }

    public void setAdd(int i, java.lang.String _value) {
        this.add[i] = _value;
    }

    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof DocumentUpdateRequestUpdateSenders)) {
            return false;
        }
        DocumentUpdateRequestUpdateSenders other = (DocumentUpdateRequestUpdateSenders) obj;
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
                        && ((this.remove == null && other.getRemove() == null)
                        || (this.remove != null && java.util.Arrays.equals(this.remove, other.getRemove())))
                        && ((this.add == null && other.getAdd() == null)
                        || (this.add != null && java.util.Arrays.equals(this.add, other.getAdd())));
        __equalsCalc = null;
        return _equals;
    }

    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getRemove() != null) {
            for (int i = 0; i < java.lang.reflect.Array.getLength(getRemove()); i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getRemove(), i);
                if (obj != null && !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getAdd() != null) {
            for (int i = 0; i < java.lang.reflect.Array.getLength(getAdd()); i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getAdd(), i);
                if (obj != null && !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        __hashCodeCalc = false;
        return _hashCode;
    }
}
