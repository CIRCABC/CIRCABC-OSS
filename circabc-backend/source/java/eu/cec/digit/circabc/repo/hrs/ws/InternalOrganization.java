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
 * <p>InternalOrganization.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 * <p>
 * InternalOrganization.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
/**
 * InternalOrganization.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package eu.cec.digit.circabc.repo.hrs.ws;

/** An organization internal in the Commission */
public class InternalOrganization implements java.io.Serializable {

  // Type metadata
  private static org.apache.axis.description.TypeDesc typeDesc =
    new org.apache.axis.description.TypeDesc(InternalOrganization.class, true);

  static {
    typeDesc.setXmlType(
      new javax.xml.namespace.QName(
        "http://ec.europa.eu/sg/hrs/types",
        "InternalOrganization"
      )
    );
    org.apache.axis.description.ElementDesc elemField =
      new org.apache.axis.description.ElementDesc();
    elemField.setFieldName("code");
    elemField.setXmlName(
      new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "code")
    );
    elemField.setXmlType(
      new javax.xml.namespace.QName(
        "http://www.w3.org/2001/XMLSchema",
        "string"
      )
    );
    elemField.setNillable(false);
    typeDesc.addFieldDesc(elemField);
  }

  /* Name of the organization e.g. DIGIT, AIDCO.G.5 */
  private java.lang.String code;
  private java.lang.Object __equalsCalc = null;
  private boolean __hashCodeCalc = false;

  public InternalOrganization() {}

  public InternalOrganization(java.lang.String code) {
    this.code = code;
  }

  /** Return type metadata object */
  public static org.apache.axis.description.TypeDesc getTypeDesc() {
    return typeDesc;
  }

  /** Get Custom Serializer */
  public static org.apache.axis.encoding.Serializer getSerializer(
    java.lang.String mechType,
    java.lang.Class _javaType,
    javax.xml.namespace.QName _xmlType
  ) {
    return new org.apache.axis.encoding.ser.BeanSerializer(
      _javaType,
      _xmlType,
      typeDesc
    );
  }

  /** Get Custom Deserializer */
  public static org.apache.axis.encoding.Deserializer getDeserializer(
    java.lang.String mechType,
    java.lang.Class _javaType,
    javax.xml.namespace.QName _xmlType
  ) {
    return new org.apache.axis.encoding.ser.BeanDeserializer(
      _javaType,
      _xmlType,
      typeDesc
    );
  }

  /**
   * Gets the code value for this InternalOrganization.
   *
   * @return code * Name of the organization e.g. DIGIT, AIDCO.G.5
   */
  public java.lang.String getCode() {
    return code;
  }

  /**
   * Sets the code value for this InternalOrganization.
   *
   * @param code * Name of the organization e.g. DIGIT, AIDCO.G.5
   */
  public void setCode(java.lang.String code) {
    this.code = code;
  }

  public synchronized boolean equals(java.lang.Object obj) {
    if (!(obj instanceof InternalOrganization)) {
      return false;
    }
    InternalOrganization other = (InternalOrganization) obj;
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
      true &&
      ((this.code == null && other.getCode() == null) ||
        (this.code != null && this.code.equals(other.getCode())));
    __equalsCalc = null;
    return _equals;
  }

  public synchronized int hashCode() {
    if (__hashCodeCalc) {
      return 0;
    }
    __hashCodeCalc = true;
    int _hashCode = 1;
    if (getCode() != null) {
      _hashCode += getCode().hashCode();
    }
    __hashCodeCalc = false;
    return _hashCode;
  }
}
