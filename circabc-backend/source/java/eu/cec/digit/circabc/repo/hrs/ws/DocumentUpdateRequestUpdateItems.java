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
 * <p>DocumentUpdateRequestUpdateItems.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 * <p>
 * DocumentUpdateRequestUpdateItems.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
/**
 * DocumentUpdateRequestUpdateItems.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package eu.cec.digit.circabc.repo.hrs.ws;

public class DocumentUpdateRequestUpdateItems implements java.io.Serializable {

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
            new org.apache.axis.description.TypeDesc(DocumentUpdateRequestUpdateItems.class, true);

    static {
        typeDesc.setXmlType(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", ">DocumentUpdateRequest>updateItems"));
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
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "ItemsToAdd"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("updateDetails");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "updateDetails"));
        elemField.setXmlType(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types",
                        ">>DocumentUpdateRequest>updateItems>updateDetails"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
    }

    /* Remove all versions of the specified item from the document
     * and the repository. If the item is not found, it is silently ignored. */
    private java.lang.String[] remove;
    /* Add these items to the document */
    private eu.cec.digit.circabc.repo.hrs.ws.ItemsToAdd add;
    /* Update the metadata of the specified item (or translation).
     * If the item is not found,
     *                                 a failure is raised. <br/>
     *
     *                                 The update works as follows:
     *                                 <ul>
     *                                     <li>To update a field, set the
     * corresponding element below to the updated value.</li>
     *                                     <li>To leave a field unchanged
     * omit the corresponding element.</li>
     *                                 </ul> */
    private eu.cec.digit.circabc.repo.hrs.ws.DocumentUpdateRequestUpdateItemsUpdateDetails[]
            updateDetails;
    private java.lang.Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;

    public DocumentUpdateRequestUpdateItems() {
    }

    public DocumentUpdateRequestUpdateItems(
            java.lang.String[] remove,
            eu.cec.digit.circabc.repo.hrs.ws.ItemsToAdd add,
            eu.cec.digit.circabc.repo.hrs.ws.DocumentUpdateRequestUpdateItemsUpdateDetails[]
                    updateDetails) {
        this.remove = remove;
        this.add = add;
        this.updateDetails = updateDetails;
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
     * Gets the remove value for this DocumentUpdateRequestUpdateItems.
     *
     * @return remove * Remove all versions of the specified item from the document and the
     *     repository. If the item is not found, it is silently ignored.
     */
    public java.lang.String[] getRemove() {
        return remove;
    }

    /**
     * Sets the remove value for this DocumentUpdateRequestUpdateItems.
     *
     * @param remove * Remove all versions of the specified item from the document and the repository.
     *     If the item is not found, it is silently ignored.
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
     * Gets the add value for this DocumentUpdateRequestUpdateItems.
     *
     * @return add * Add these items to the document
     */
    public eu.cec.digit.circabc.repo.hrs.ws.ItemsToAdd getAdd() {
        return add;
    }

    /**
     * Sets the add value for this DocumentUpdateRequestUpdateItems.
     *
     * @param add * Add these items to the document
     */
    public void setAdd(eu.cec.digit.circabc.repo.hrs.ws.ItemsToAdd add) {
        this.add = add;
    }

    /**
     * Gets the updateDetails value for this DocumentUpdateRequestUpdateItems.
     *
     * @return updateDetails * Update the metadata of the specified item (or translation). If the item
     *     is not found, a failure is raised. <br>
     *     The update works as follows:
     *     <ul>
     *       <li>To update a field, set the corresponding element below to the updated value.
     *       <li>To leave a field unchanged omit the corresponding element.
     *     </ul>
     */
    public eu.cec.digit.circabc.repo.hrs.ws.DocumentUpdateRequestUpdateItemsUpdateDetails[]
    getUpdateDetails() {
        return updateDetails;
    }

    /**
     * Sets the updateDetails value for this DocumentUpdateRequestUpdateItems.
     *
     * @param updateDetails * Update the metadata of the specified item (or translation). If the item
     *     is not found, a failure is raised. <br>
     *     The update works as follows:
     *     <ul>
     *       <li>To update a field, set the corresponding element below to the updated value.
     *       <li>To leave a field unchanged omit the corresponding element.
     *     </ul>
     */
    public void setUpdateDetails(
            eu.cec.digit.circabc.repo.hrs.ws.DocumentUpdateRequestUpdateItemsUpdateDetails[]
                    updateDetails) {
        this.updateDetails = updateDetails;
    }

    public eu.cec.digit.circabc.repo.hrs.ws.DocumentUpdateRequestUpdateItemsUpdateDetails
    getUpdateDetails(int i) {
        return this.updateDetails[i];
    }

    public void setUpdateDetails(
            int i,
            eu.cec.digit.circabc.repo.hrs.ws.DocumentUpdateRequestUpdateItemsUpdateDetails _value) {
        this.updateDetails[i] = _value;
    }

    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof DocumentUpdateRequestUpdateItems)) {
            return false;
        }
        DocumentUpdateRequestUpdateItems other = (DocumentUpdateRequestUpdateItems) obj;
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
                        || (this.add != null && this.add.equals(other.getAdd())))
                        && ((this.updateDetails == null && other.getUpdateDetails() == null)
                        || (this.updateDetails != null
                        && java.util.Arrays.equals(this.updateDetails, other.getUpdateDetails())));
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
            _hashCode += getAdd().hashCode();
        }
        if (getUpdateDetails() != null) {
            for (int i = 0; i < java.lang.reflect.Array.getLength(getUpdateDetails()); i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getUpdateDetails(), i);
                if (obj != null && !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        __hashCodeCalc = false;
        return _hashCode;
    }
}
