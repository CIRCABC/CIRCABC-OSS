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
 * <p>DocumentUpdateRequestUpdateRecipients.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 * <p>
 * DocumentUpdateRequestUpdateRecipients.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
/**
 * DocumentUpdateRequestUpdateRecipients.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package eu.cec.digit.circabc.repo.hrs.ws;

public class DocumentUpdateRequestUpdateRecipients implements java.io.Serializable {

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
            new org.apache.axis.description.TypeDesc(DocumentUpdateRequestUpdateRecipients.class, true);

    static {
        typeDesc.setXmlType(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", ">DocumentUpdateRequest>updateRecipients"));
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
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", ">>DocumentUpdateRequest>updateRecipients>add"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
    }

    /* Remove the specified entity from recipients */
    private java.lang.String[] remove;
    /* Add the specified current entity as a recipient */
    private eu.cec.digit.circabc.repo.hrs.ws.DocumentUpdateRequestUpdateRecipientsAdd[] add;
    private java.lang.Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;

    public DocumentUpdateRequestUpdateRecipients() {
    }

    public DocumentUpdateRequestUpdateRecipients(
            java.lang.String[] remove,
            eu.cec.digit.circabc.repo.hrs.ws.DocumentUpdateRequestUpdateRecipientsAdd[] add) {
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
     * Gets the remove value for this DocumentUpdateRequestUpdateRecipients.
     *
     * @return remove * Remove the specified entity from recipients
     */
    public java.lang.String[] getRemove() {
        return remove;
    }

    /**
     * Sets the remove value for this DocumentUpdateRequestUpdateRecipients.
     *
     * @param remove * Remove the specified entity from recipients
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
     * Gets the add value for this DocumentUpdateRequestUpdateRecipients.
     *
     * @return add * Add the specified current entity as a recipient
     */
    public eu.cec.digit.circabc.repo.hrs.ws.DocumentUpdateRequestUpdateRecipientsAdd[] getAdd() {
        return add;
    }

    /**
     * Sets the add value for this DocumentUpdateRequestUpdateRecipients.
     *
     * @param add * Add the specified current entity as a recipient
     */
    public void setAdd(
            eu.cec.digit.circabc.repo.hrs.ws.DocumentUpdateRequestUpdateRecipientsAdd[] add) {
        this.add = add;
    }

    public eu.cec.digit.circabc.repo.hrs.ws.DocumentUpdateRequestUpdateRecipientsAdd getAdd(int i) {
        return this.add[i];
    }

    public void setAdd(
            int i, eu.cec.digit.circabc.repo.hrs.ws.DocumentUpdateRequestUpdateRecipientsAdd _value) {
        this.add[i] = _value;
    }

    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof DocumentUpdateRequestUpdateRecipients)) {
            return false;
        }
        DocumentUpdateRequestUpdateRecipients other = (DocumentUpdateRequestUpdateRecipients) obj;
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
