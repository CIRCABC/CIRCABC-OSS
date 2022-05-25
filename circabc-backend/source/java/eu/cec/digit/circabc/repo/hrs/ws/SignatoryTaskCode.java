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
 * <p>SignatoryTaskCode.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 * <p>
 * SignatoryTaskCode.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
/**
 * SignatoryTaskCode.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package eu.cec.digit.circabc.repo.hrs.ws;

public class SignatoryTaskCode implements java.io.Serializable {

    public static final java.lang.String _value1 = "RED";
    public static final java.lang.String _value2 = "CONTRIB";
    public static final java.lang.String _value3 = "VISA";
    public static final java.lang.String _value4 = "SIGN";
    public static final java.lang.String _value5 = "EXP";
    public static final java.lang.String _value6 = "CF (CBC)";
    public static final java.lang.String _value7 = "CF (CDP)";
    public static final java.lang.String _value8 = "CF (DAD)";
    public static final java.lang.String _value9 = "CF (QP)";
    public static final java.lang.String _value10 = "CF (CIS)";
    public static final java.lang.String _value11 = "CF (CAB)";
    public static final java.lang.String _value12 = "CF (PET)";
    public static final java.lang.String _value13 = "CONTR";
    public static final java.lang.String _value14 = "VALID";
    public static final java.lang.String _value15 = "EMET";
    public static final java.lang.String _value16 = "RET";
    public static final java.lang.String _value17 = "INFO";
    public static final java.lang.String _value18 = "CLASS";
    public static final java.lang.String _value19 = "AUT";
    public static final java.lang.String _value20 = "SIGNAT";
    public static final SignatoryTaskCode value1 = new SignatoryTaskCode(_value1);
    public static final SignatoryTaskCode value2 = new SignatoryTaskCode(_value2);
    public static final SignatoryTaskCode value3 = new SignatoryTaskCode(_value3);
    public static final SignatoryTaskCode value4 = new SignatoryTaskCode(_value4);
    public static final SignatoryTaskCode value5 = new SignatoryTaskCode(_value5);
    public static final SignatoryTaskCode value6 = new SignatoryTaskCode(_value6);
    public static final SignatoryTaskCode value7 = new SignatoryTaskCode(_value7);
    public static final SignatoryTaskCode value8 = new SignatoryTaskCode(_value8);
    public static final SignatoryTaskCode value9 = new SignatoryTaskCode(_value9);
    public static final SignatoryTaskCode value10 = new SignatoryTaskCode(_value10);
    public static final SignatoryTaskCode value11 = new SignatoryTaskCode(_value11);
    public static final SignatoryTaskCode value12 = new SignatoryTaskCode(_value12);
    public static final SignatoryTaskCode value13 = new SignatoryTaskCode(_value13);
    public static final SignatoryTaskCode value14 = new SignatoryTaskCode(_value14);
    public static final SignatoryTaskCode value15 = new SignatoryTaskCode(_value15);
    public static final SignatoryTaskCode value16 = new SignatoryTaskCode(_value16);
    public static final SignatoryTaskCode value17 = new SignatoryTaskCode(_value17);
    public static final SignatoryTaskCode value18 = new SignatoryTaskCode(_value18);
    public static final SignatoryTaskCode value19 = new SignatoryTaskCode(_value19);
    public static final SignatoryTaskCode value20 = new SignatoryTaskCode(_value20);
    private static java.util.HashMap _table_ = new java.util.HashMap();
    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
            new org.apache.axis.description.TypeDesc(SignatoryTaskCode.class);

    static {
        typeDesc.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", ">SignatoryTask>code"));
    }

    private java.lang.String _value_;

    // Constructor
    protected SignatoryTaskCode(java.lang.String value) {
        _value_ = value;
        _table_.put(_value_, this);
    }

    public static SignatoryTaskCode fromValue(java.lang.String value)
            throws java.lang.IllegalArgumentException {
        SignatoryTaskCode enumeration = (SignatoryTaskCode) _table_.get(value);
        if (enumeration == null) {
            throw new java.lang.IllegalArgumentException();
        }
        return enumeration;
    }

    public static SignatoryTaskCode fromString(java.lang.String value)
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
