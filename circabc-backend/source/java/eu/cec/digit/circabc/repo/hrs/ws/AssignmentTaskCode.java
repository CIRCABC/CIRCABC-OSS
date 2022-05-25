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
 * <p>AssignmentTaskCode.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 * <p>
 * AssignmentTaskCode.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
/**
 * AssignmentTaskCode.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package eu.cec.digit.circabc.repo.hrs.ws;

public class AssignmentTaskCode implements java.io.Serializable {

    public static final java.lang.String _value1 = "CF";
    public static final java.lang.String _value2 = "ASOC";
    public static final java.lang.String _value3 = "INFO";
    public static final java.lang.String _value4 = "CLASS";
    public static final java.lang.String _value5 = "CF (CBC)";
    public static final java.lang.String _value6 = "CF (CDP)";
    public static final java.lang.String _value7 = "CF (DAD)";
    public static final java.lang.String _value8 = "CF (QP)";
    public static final java.lang.String _value9 = "CF (CIS)";
    public static final java.lang.String _value10 = "CF (CAB)";
    public static final java.lang.String _value11 = "CF (PET)";
    public static final java.lang.String _value12 = "CONTR";
    public static final java.lang.String _value13 = "VALID";
    public static final java.lang.String _value14 = "EMET";
    public static final java.lang.String _value15 = "RET";
    public static final java.lang.String _value16 = "AUT";
    public static final java.lang.String _value17 = "SIGNAT";
    public static final AssignmentTaskCode value1 = new AssignmentTaskCode(_value1);
    public static final AssignmentTaskCode value2 = new AssignmentTaskCode(_value2);
    public static final AssignmentTaskCode value3 = new AssignmentTaskCode(_value3);
    public static final AssignmentTaskCode value4 = new AssignmentTaskCode(_value4);
    public static final AssignmentTaskCode value5 = new AssignmentTaskCode(_value5);
    public static final AssignmentTaskCode value6 = new AssignmentTaskCode(_value6);
    public static final AssignmentTaskCode value7 = new AssignmentTaskCode(_value7);
    public static final AssignmentTaskCode value8 = new AssignmentTaskCode(_value8);
    public static final AssignmentTaskCode value9 = new AssignmentTaskCode(_value9);
    public static final AssignmentTaskCode value10 = new AssignmentTaskCode(_value10);
    public static final AssignmentTaskCode value11 = new AssignmentTaskCode(_value11);
    public static final AssignmentTaskCode value12 = new AssignmentTaskCode(_value12);
    public static final AssignmentTaskCode value13 = new AssignmentTaskCode(_value13);
    public static final AssignmentTaskCode value14 = new AssignmentTaskCode(_value14);
    public static final AssignmentTaskCode value15 = new AssignmentTaskCode(_value15);
    public static final AssignmentTaskCode value16 = new AssignmentTaskCode(_value16);
    public static final AssignmentTaskCode value17 = new AssignmentTaskCode(_value17);
    private static java.util.HashMap _table_ = new java.util.HashMap();
    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
            new org.apache.axis.description.TypeDesc(AssignmentTaskCode.class);

    static {
        typeDesc.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", ">AssignmentTask>code"));
    }

    private java.lang.String _value_;

    // Constructor
    protected AssignmentTaskCode(java.lang.String value) {
        _value_ = value;
        _table_.put(_value_, this);
    }

    public static AssignmentTaskCode fromValue(java.lang.String value)
            throws java.lang.IllegalArgumentException {
        AssignmentTaskCode enumeration = (AssignmentTaskCode) _table_.get(value);
        if (enumeration == null) {
            throw new java.lang.IllegalArgumentException();
        }
        return enumeration;
    }

    public static AssignmentTaskCode fromString(java.lang.String value)
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
