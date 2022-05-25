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
 * <p>ItemCheckinRequest.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 * <p>
 * ItemCheckinRequest.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
/**
 * ItemCheckinRequest.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package eu.cec.digit.circabc.repo.hrs.ws;

/** Request for the operation checkinItem */
public class ItemCheckinRequest implements java.io.Serializable {

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
            new org.apache.axis.description.TypeDesc(ItemCheckinRequest.class, true);

    static {
        typeDesc.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "ItemCheckinRequest"));
        org.apache.axis.description.ElementDesc elemField =
                new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("itemId");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "itemId"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("contentId");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "contentId"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("name");
        elemField.setXmlName(new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "name"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("versioningStrategy");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "versioningStrategy"));
        elemField.setXmlType(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", ">ItemCheckinRequest>versioningStrategy"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("comments");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "comments"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /* Repository id of the item whose content is checked in */
    private java.lang.String itemId;
    /* Content id that was obtained by uploading the content
     *                         using the data transfer service */
    private java.lang.String contentId;
    /* New item name (name of attachment, including extension) */
    private java.lang.String name;
    /* Specifies whether the checkin operation should create a
     *                         new major version (e.g.  new version is 2.0),
     * a new minor version
     *                         (e.g. new version is 1.1), or should overwrite
     * the existing version
     *                         (e.g. version remains at 1.0).<br/>
     *
     *                         If you overwrite the existing version, the
     * previous content and attributes
     *                         cannot be recovered anymore, because they
     * have been overwritten. */
    private eu.cec.digit.circabc.repo.hrs.ws.ItemCheckinRequestVersioningStrategy versioningStrategy;
    /* Description of the change */
    private java.lang.String comments;
    private java.lang.Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;

    public ItemCheckinRequest() {
    }

    public ItemCheckinRequest(
            java.lang.String itemId,
            java.lang.String contentId,
            java.lang.String name,
            eu.cec.digit.circabc.repo.hrs.ws.ItemCheckinRequestVersioningStrategy versioningStrategy,
            java.lang.String comments) {
        this.itemId = itemId;
        this.contentId = contentId;
        this.name = name;
        this.versioningStrategy = versioningStrategy;
        this.comments = comments;
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
     * Gets the itemId value for this ItemCheckinRequest.
     *
     * @return itemId * Repository id of the item whose content is checked in
     */
    public java.lang.String getItemId() {
        return itemId;
    }

    /**
     * Sets the itemId value for this ItemCheckinRequest.
     *
     * @param itemId * Repository id of the item whose content is checked in
     */
    public void setItemId(java.lang.String itemId) {
        this.itemId = itemId;
    }

    /**
     * Gets the contentId value for this ItemCheckinRequest.
     *
     * @return contentId * Content id that was obtained by uploading the content using the data
     *     transfer service
     */
    public java.lang.String getContentId() {
        return contentId;
    }

    /**
     * Sets the contentId value for this ItemCheckinRequest.
     *
     * @param contentId * Content id that was obtained by uploading the content using the data
     *     transfer service
     */
    public void setContentId(java.lang.String contentId) {
        this.contentId = contentId;
    }

    /**
     * Gets the name value for this ItemCheckinRequest.
     *
     * @return name * New item name (name of attachment, including extension)
     */
    public java.lang.String getName() {
        return name;
    }

    /**
     * Sets the name value for this ItemCheckinRequest.
     *
     * @param name * New item name (name of attachment, including extension)
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }

    /**
     * Gets the versioningStrategy value for this ItemCheckinRequest.
     *
     * @return versioningStrategy * Specifies whether the checkin operation should create a new major
     *     version (e.g. new version is 2.0), a new minor version (e.g. new version is 1.1), or should
     *     overwrite the existing version (e.g. version remains at 1.0).<br>
     *     If you overwrite the existing version, the previous content and attributes cannot be
     *     recovered anymore, because they have been overwritten.
     */
    public eu.cec.digit.circabc.repo.hrs.ws.ItemCheckinRequestVersioningStrategy
    getVersioningStrategy() {
        return versioningStrategy;
    }

    /**
     * Sets the versioningStrategy value for this ItemCheckinRequest.
     *
     * @param versioningStrategy * Specifies whether the checkin operation should create a new major
     *     version (e.g. new version is 2.0), a new minor version (e.g. new version is 1.1), or should
     *     overwrite the existing version (e.g. version remains at 1.0).<br>
     *     If you overwrite the existing version, the previous content and attributes cannot be
     *     recovered anymore, because they have been overwritten.
     */
    public void setVersioningStrategy(
            eu.cec.digit.circabc.repo.hrs.ws.ItemCheckinRequestVersioningStrategy versioningStrategy) {
        this.versioningStrategy = versioningStrategy;
    }

    /**
     * Gets the comments value for this ItemCheckinRequest.
     *
     * @return comments * Description of the change
     */
    public java.lang.String getComments() {
        return comments;
    }

    /**
     * Sets the comments value for this ItemCheckinRequest.
     *
     * @param comments * Description of the change
     */
    public void setComments(java.lang.String comments) {
        this.comments = comments;
    }

    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ItemCheckinRequest)) {
            return false;
        }
        ItemCheckinRequest other = (ItemCheckinRequest) obj;
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
                        && ((this.itemId == null && other.getItemId() == null)
                        || (this.itemId != null && this.itemId.equals(other.getItemId())))
                        && ((this.contentId == null && other.getContentId() == null)
                        || (this.contentId != null && this.contentId.equals(other.getContentId())))
                        && ((this.name == null && other.getName() == null)
                        || (this.name != null && this.name.equals(other.getName())))
                        && ((this.versioningStrategy == null && other.getVersioningStrategy() == null)
                        || (this.versioningStrategy != null
                        && this.versioningStrategy.equals(other.getVersioningStrategy())))
                        && ((this.comments == null && other.getComments() == null)
                        || (this.comments != null && this.comments.equals(other.getComments())));
        __equalsCalc = null;
        return _equals;
    }

    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getItemId() != null) {
            _hashCode += getItemId().hashCode();
        }
        if (getContentId() != null) {
            _hashCode += getContentId().hashCode();
        }
        if (getName() != null) {
            _hashCode += getName().hashCode();
        }
        if (getVersioningStrategy() != null) {
            _hashCode += getVersioningStrategy().hashCode();
        }
        if (getComments() != null) {
            _hashCode += getComments().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }
}
