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
 * <p>Procedure.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 * <p>
 * Procedure.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
/**
 * Procedure.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package eu.cec.digit.circabc.repo.hrs.ws;

/** A procedure */
public class Procedure implements java.io.Serializable {

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
            new org.apache.axis.description.TypeDesc(Procedure.class, true);

    static {
        typeDesc.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "Procedure"));
        org.apache.axis.description.ElementDesc elemField =
                new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("id");
        elemField.setXmlName(new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "id"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("englishName");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "englishName"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("frenchName");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "frenchName"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /* Internal ID of the procedure */
    private int id;
    /* English name */
    private java.lang.String englishName;
    /* French name */
    private java.lang.String frenchName;
    private java.lang.Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;

    public Procedure() {
    }

    public Procedure(int id, java.lang.String englishName, java.lang.String frenchName) {
        this.id = id;
        this.englishName = englishName;
        this.frenchName = frenchName;
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
     * Gets the id value for this Procedure.
     *
     * @return id * Internal ID of the procedure
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the id value for this Procedure.
     *
     * @param id * Internal ID of the procedure
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the englishName value for this Procedure.
     *
     * @return englishName * English name
     */
    public java.lang.String getEnglishName() {
        return englishName;
    }

    /**
     * Sets the englishName value for this Procedure.
     *
     * @param englishName * English name
     */
    public void setEnglishName(java.lang.String englishName) {
        this.englishName = englishName;
    }

    /**
     * Gets the frenchName value for this Procedure.
     *
     * @return frenchName * French name
     */
    public java.lang.String getFrenchName() {
        return frenchName;
    }

    /**
     * Sets the frenchName value for this Procedure.
     *
     * @param frenchName * French name
     */
    public void setFrenchName(java.lang.String frenchName) {
        this.frenchName = frenchName;
    }

    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Procedure)) {
            return false;
        }
        Procedure other = (Procedure) obj;
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
                        && this.id == other.getId()
                        && ((this.englishName == null && other.getEnglishName() == null)
                        || (this.englishName != null && this.englishName.equals(other.getEnglishName())))
                        && ((this.frenchName == null && other.getFrenchName() == null)
                        || (this.frenchName != null && this.frenchName.equals(other.getFrenchName())));
        __equalsCalc = null;
        return _equals;
    }

    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        _hashCode += getId();
        if (getEnglishName() != null) {
            _hashCode += getEnglishName().hashCode();
        }
        if (getFrenchName() != null) {
            _hashCode += getFrenchName().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }
}
