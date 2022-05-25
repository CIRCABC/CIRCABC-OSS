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
 * <p>CurrentInternalPerson.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 * <p>
 * CurrentInternalPerson.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
/**
 * CurrentInternalPerson.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package eu.cec.digit.circabc.repo.hrs.ws;

/** An internal person (or VE) from the current base */
public class CurrentInternalPerson implements java.io.Serializable {

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
            new org.apache.axis.description.TypeDesc(CurrentInternalPerson.class, true);

    static {
        typeDesc.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "CurrentInternalPerson"));
        org.apache.axis.description.ElementDesc elemField =
                new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("id");
        elemField.setXmlName(new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "id"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("lastName");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "lastName"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("firstName");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "firstName"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("userName");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "userName"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("floor");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "floor"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("building");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "building"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("room");
        elemField.setXmlName(new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "room"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("email");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "email"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /* Internal repository ID that identifies the internal person
     * / VE */
    private java.lang.String id;
    /* The person's last name or the VE name */
    private java.lang.String lastName;
    /* First name(s) of the person */
    private java.lang.String firstName;
    /* Internal user name of the person.
     *                         This user name is also the ECAS user name
     * of the person or the
     *                         VE user name (e.g. ve_123) of the virtual
     * entity. */
    private java.lang.String userName;
    /* Floor on which the person is located */
    private java.lang.String floor;
    /* Building where the person is located */
    private java.lang.String building;
    /* Room in the building where the person is located. */
    private java.lang.String room;
    /* Email address of the internal person */
    private java.lang.String email;
    private java.lang.Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;

    public CurrentInternalPerson() {
    }

    public CurrentInternalPerson(
            java.lang.String id,
            java.lang.String lastName,
            java.lang.String firstName,
            java.lang.String userName,
            java.lang.String floor,
            java.lang.String building,
            java.lang.String room,
            java.lang.String email) {
        this.id = id;
        this.lastName = lastName;
        this.firstName = firstName;
        this.userName = userName;
        this.floor = floor;
        this.building = building;
        this.room = room;
        this.email = email;
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
     * Gets the id value for this CurrentInternalPerson.
     *
     * @return id * Internal repository ID that identifies the internal person / VE
     */
    public java.lang.String getId() {
        return id;
    }

    /**
     * Sets the id value for this CurrentInternalPerson.
     *
     * @param id * Internal repository ID that identifies the internal person / VE
     */
    public void setId(java.lang.String id) {
        this.id = id;
    }

    /**
     * Gets the lastName value for this CurrentInternalPerson.
     *
     * @return lastName * The person's last name or the VE name
     */
    public java.lang.String getLastName() {
        return lastName;
    }

    /**
     * Sets the lastName value for this CurrentInternalPerson.
     *
     * @param lastName * The person's last name or the VE name
     */
    public void setLastName(java.lang.String lastName) {
        this.lastName = lastName;
    }

    /**
     * Gets the firstName value for this CurrentInternalPerson.
     *
     * @return firstName * First name(s) of the person
     */
    public java.lang.String getFirstName() {
        return firstName;
    }

    /**
     * Sets the firstName value for this CurrentInternalPerson.
     *
     * @param firstName * First name(s) of the person
     */
    public void setFirstName(java.lang.String firstName) {
        this.firstName = firstName;
    }

    /**
     * Gets the userName value for this CurrentInternalPerson.
     *
     * @return userName * Internal user name of the person. This user name is also the ECAS user name
     *     of the person or the VE user name (e.g. ve_123) of the virtual entity.
     */
    public java.lang.String getUserName() {
        return userName;
    }

    /**
     * Sets the userName value for this CurrentInternalPerson.
     *
     * @param userName * Internal user name of the person. This user name is also the ECAS user name
     *     of the person or the VE user name (e.g. ve_123) of the virtual entity.
     */
    public void setUserName(java.lang.String userName) {
        this.userName = userName;
    }

    /**
     * Gets the floor value for this CurrentInternalPerson.
     *
     * @return floor * Floor on which the person is located
     */
    public java.lang.String getFloor() {
        return floor;
    }

    /**
     * Sets the floor value for this CurrentInternalPerson.
     *
     * @param floor * Floor on which the person is located
     */
    public void setFloor(java.lang.String floor) {
        this.floor = floor;
    }

    /**
     * Gets the building value for this CurrentInternalPerson.
     *
     * @return building * Building where the person is located
     */
    public java.lang.String getBuilding() {
        return building;
    }

    /**
     * Sets the building value for this CurrentInternalPerson.
     *
     * @param building * Building where the person is located
     */
    public void setBuilding(java.lang.String building) {
        this.building = building;
    }

    /**
     * Gets the room value for this CurrentInternalPerson.
     *
     * @return room * Room in the building where the person is located.
     */
    public java.lang.String getRoom() {
        return room;
    }

    /**
     * Sets the room value for this CurrentInternalPerson.
     *
     * @param room * Room in the building where the person is located.
     */
    public void setRoom(java.lang.String room) {
        this.room = room;
    }

    /**
     * Gets the email value for this CurrentInternalPerson.
     *
     * @return email * Email address of the internal person
     */
    public java.lang.String getEmail() {
        return email;
    }

    /**
     * Sets the email value for this CurrentInternalPerson.
     *
     * @param email * Email address of the internal person
     */
    public void setEmail(java.lang.String email) {
        this.email = email;
    }

    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof CurrentInternalPerson)) {
            return false;
        }
        CurrentInternalPerson other = (CurrentInternalPerson) obj;
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
                        && ((this.id == null && other.getId() == null)
                        || (this.id != null && this.id.equals(other.getId())))
                        && ((this.lastName == null && other.getLastName() == null)
                        || (this.lastName != null && this.lastName.equals(other.getLastName())))
                        && ((this.firstName == null && other.getFirstName() == null)
                        || (this.firstName != null && this.firstName.equals(other.getFirstName())))
                        && ((this.userName == null && other.getUserName() == null)
                        || (this.userName != null && this.userName.equals(other.getUserName())))
                        && ((this.floor == null && other.getFloor() == null)
                        || (this.floor != null && this.floor.equals(other.getFloor())))
                        && ((this.building == null && other.getBuilding() == null)
                        || (this.building != null && this.building.equals(other.getBuilding())))
                        && ((this.room == null && other.getRoom() == null)
                        || (this.room != null && this.room.equals(other.getRoom())))
                        && ((this.email == null && other.getEmail() == null)
                        || (this.email != null && this.email.equals(other.getEmail())));
        __equalsCalc = null;
        return _equals;
    }

    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getId() != null) {
            _hashCode += getId().hashCode();
        }
        if (getLastName() != null) {
            _hashCode += getLastName().hashCode();
        }
        if (getFirstName() != null) {
            _hashCode += getFirstName().hashCode();
        }
        if (getUserName() != null) {
            _hashCode += getUserName().hashCode();
        }
        if (getFloor() != null) {
            _hashCode += getFloor().hashCode();
        }
        if (getBuilding() != null) {
            _hashCode += getBuilding().hashCode();
        }
        if (getRoom() != null) {
            _hashCode += getRoom().hashCode();
        }
        if (getEmail() != null) {
            _hashCode += getEmail().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }
}
