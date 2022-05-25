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
 * <p>ItemKind.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 * <p>
 * ItemKind.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
/**
 * ItemKind.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package eu.cec.digit.circabc.repo.hrs.ws;

public class ItemKind implements java.io.Serializable {

    public static final java.lang.String _MAIN = "MAIN";
    public static final java.lang.String _COVER_LETTER = "COVER_LETTER";
    public static final java.lang.String _ANNEX = "ANNEX";
    public static final java.lang.String _TRANSLATION = "TRANSLATION";
    public static final ItemKind MAIN = new ItemKind(_MAIN);
    public static final ItemKind COVER_LETTER = new ItemKind(_COVER_LETTER);
    public static final ItemKind ANNEX = new ItemKind(_ANNEX);
    public static final ItemKind TRANSLATION = new ItemKind(_TRANSLATION);
    private static java.util.HashMap _table_ = new java.util.HashMap();
    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
            new org.apache.axis.description.TypeDesc(ItemKind.class);

    static {
        typeDesc.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "ItemKind"));
    }

    private java.lang.String _value_;

    // Constructor
    protected ItemKind(java.lang.String value) {
        _value_ = value;
        _table_.put(_value_, this);
    }

    public static ItemKind fromValue(java.lang.String value)
            throws java.lang.IllegalArgumentException {
        ItemKind enumeration = (ItemKind) _table_.get(value);
        if (enumeration == null) {
            throw new java.lang.IllegalArgumentException();
        }
        return enumeration;
    }

    public static ItemKind fromString(java.lang.String value)
            throws java.lang.IllegalArgumentException {
        return fromValue(value);
    }

    public static org.apache.axis.encoding.Serializer getSerializer(
            java.lang.String mechType, java.lang.Class _javaType, javax.xml.namespace.QName _xmlType) {
        return new org.apache.axis.encoding.ser.EnumSerializer(_javaType, _xmlType);
    }

    public static org.apache.axis.encoding.Deserializer getDeserializer(
            java.lang.String mechType, java.lang.Class _javaType, javax.xml.namespace.QName _xmlType) {
        return new org.apache.axis.encoding.ser.EnumDeserializer(_javaType, _xmlType);
    }

    /** Return type metadata object */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    public java.lang.String getValue() {
        return _value_;
    }

    public boolean equals(java.lang.Object obj) {
        return (obj == this);
    }

    public int hashCode() {
        return toString().hashCode();
    }

    public java.lang.String toString() {
        return _value_;
    }

    public java.lang.Object readResolve() throws java.io.ObjectStreamException {
        return fromValue(_value_);
    }
}
