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
 * <p>Marking.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 * <p>
 * Marking.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
/**
 * Marking.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package eu.cec.digit.circabc.repo.hrs.ws;

public class Marking implements java.io.Serializable {

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
            new org.apache.axis.description.TypeDesc(Marking.class, true);

    static {
        typeDesc.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "Marking"));
        org.apache.axis.description.ElementDesc elemField =
                new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("value");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "value"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "MarkerType"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("deadline");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "deadline"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "date"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("personConcerned");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "personConcerned"));
        elemField.setXmlType(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", ">Marking>personConcerned"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("service");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "service"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /* Type of marker */
    private eu.cec.digit.circabc.repo.hrs.ws.MarkerType value;
    /* Marker deadline date at which the marker expires */
    private java.util.Date deadline;
    /* A recipient of the document to which this document is addressed.
     * This person
     *                         is considered the 'person concerned' and can
     * only be specified using a
     *                         marker.
     *                         This is element is valid if there is a marker
     * of type personal, staff
     *                         matter, medical matter and personal data.
     *                         It can be an internal or external person entity,
     * not an organization entity.
     *                         It should not be a virtual entity. */
    private eu.cec.digit.circabc.repo.hrs.ws.MarkingPersonConcerned personConcerned;
    /* Specifies the service (e.g. digit.b.1) when applying the LIMITED_SERVICE
     * markings. If ommitted, by default the service of the user is used.<br/>
     *
     *                         It has to be a valid service (i.e. organization). */
    private java.lang.String service;
    private java.lang.Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;

    public Marking() {
    }

    public Marking(
            eu.cec.digit.circabc.repo.hrs.ws.MarkerType value,
            java.util.Date deadline,
            eu.cec.digit.circabc.repo.hrs.ws.MarkingPersonConcerned personConcerned,
            java.lang.String service) {
        this.value = value;
        this.deadline = deadline;
        this.personConcerned = personConcerned;
        this.service = service;
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
     * Gets the value value for this Marking.
     *
     * @return value * Type of marker
     */
    public eu.cec.digit.circabc.repo.hrs.ws.MarkerType getValue() {
        return value;
    }

    /**
     * Sets the value value for this Marking.
     *
     * @param value * Type of marker
     */
    public void setValue(eu.cec.digit.circabc.repo.hrs.ws.MarkerType value) {
        this.value = value;
    }

    /**
     * Gets the deadline value for this Marking.
     *
     * @return deadline * Marker deadline date at which the marker expires
     */
    public java.util.Date getDeadline() {
        return deadline;
    }

    /**
     * Sets the deadline value for this Marking.
     *
     * @param deadline * Marker deadline date at which the marker expires
     */
    public void setDeadline(java.util.Date deadline) {
        this.deadline = deadline;
    }

    /**
     * Gets the personConcerned value for this Marking.
     *
     * @return personConcerned * A recipient of the document to which this document is addressed. This
     *     person is considered the 'person concerned' and can only be specified using a marker. This
     *     is element is valid if there is a marker of type personal, staff matter, medical matter and
     *     personal data. It can be an internal or external person entity, not an organization entity.
     *     It should not be a virtual entity.
     */
    public eu.cec.digit.circabc.repo.hrs.ws.MarkingPersonConcerned getPersonConcerned() {
        return personConcerned;
    }

    /**
     * Sets the personConcerned value for this Marking.
     *
     * @param personConcerned * A recipient of the document to which this document is addressed. This
     *     person is considered the 'person concerned' and can only be specified using a marker. This
     *     is element is valid if there is a marker of type personal, staff matter, medical matter and
     *     personal data. It can be an internal or external person entity, not an organization entity.
     *     It should not be a virtual entity.
     */
    public void setPersonConcerned(
            eu.cec.digit.circabc.repo.hrs.ws.MarkingPersonConcerned personConcerned) {
        this.personConcerned = personConcerned;
    }

    /**
     * Gets the service value for this Marking.
     *
     * @return service * Specifies the service (e.g. digit.b.1) when applying the LIMITED_SERVICE
     *     markings. If ommitted, by default the service of the user is used.<br>
     *     It has to be a valid service (i.e. organization).
     */
    public java.lang.String getService() {
        return service;
    }

    /**
     * Sets the service value for this Marking.
     *
     * @param service * Specifies the service (e.g. digit.b.1) when applying the LIMITED_SERVICE
     *     markings. If ommitted, by default the service of the user is used.<br>
     *     It has to be a valid service (i.e. organization).
     */
    public void setService(java.lang.String service) {
        this.service = service;
    }

    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Marking)) {
            return false;
        }
        Marking other = (Marking) obj;
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
                        && ((this.value == null && other.getValue() == null)
                        || (this.value != null && this.value.equals(other.getValue())))
                        && ((this.deadline == null && other.getDeadline() == null)
                        || (this.deadline != null && this.deadline.equals(other.getDeadline())))
                        && ((this.personConcerned == null && other.getPersonConcerned() == null)
                        || (this.personConcerned != null
                        && this.personConcerned.equals(other.getPersonConcerned())))
                        && ((this.service == null && other.getService() == null)
                        || (this.service != null && this.service.equals(other.getService())));
        __equalsCalc = null;
        return _equals;
    }

    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getValue() != null) {
            _hashCode += getValue().hashCode();
        }
        if (getDeadline() != null) {
            _hashCode += getDeadline().hashCode();
        }
        if (getPersonConcerned() != null) {
            _hashCode += getPersonConcerned().hashCode();
        }
        if (getService() != null) {
            _hashCode += getService().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }
}
