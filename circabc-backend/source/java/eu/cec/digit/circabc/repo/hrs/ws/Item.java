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
 * <p>Item.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 * <p>
 * Item.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
/**
 * Item.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package eu.cec.digit.circabc.repo.hrs.ws;

/** An item/attachment */
public class Item implements java.io.Serializable {

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
            new org.apache.axis.description.TypeDesc(Item.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "Item"));
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
        elemField.setFieldName("name");
        elemField.setXmlName(new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "name"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("language");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "language"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("attachmentType");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "attachmentType"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "AttachmentType"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("kind");
        elemField.setXmlName(new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "kind"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "ItemKind"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("isScanWithoutContent");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "isScanWithoutContent"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("modificationDate");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "modificationDate"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "dateTime"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("externalReference");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "externalReference"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("contentType");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "contentType"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("pageNo");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "pageNo"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("renditionStatus");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "renditionStatus"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "RenditionStatus"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("checkedOut");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "checkedOut"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", ">Item>checkedOut"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("translations");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "translations"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "Item"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "translation"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("contentSize");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "contentSize"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("versioned");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "versioned"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("versionLabel");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "versionLabel"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("mimeType");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "mimeType"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /* Internal repository ID to uniquely identify an item */
    private java.lang.String itemId;
    /* The item's name */
    private java.lang.String name;
    /* The item's language */
    private java.lang.String language;
    /* Type of the item such as NATIVE_ELECTRONIC, ARES_SCANNED etc. */
    private eu.cec.digit.circabc.repo.hrs.ws.AttachmentType attachmentType;
    /* Kind of item such as MAIN, ANNEX, TRANSLATION etc. */
    private eu.cec.digit.circabc.repo.hrs.ws.ItemKind kind;
    /* Is true for items of type ARES_SCANNED (i.e. scans) where the
     * content hasn't been scanned yet. Is false in all other cases. */
    private boolean isScanWithoutContent;
    /* Date+Time when the item was last modified. A modification is
     * considered to be any change to the item's metadata or to the item's
     * content such as a checkin or scan operation. */
    private java.util.Calendar modificationDate;
    /* External reference associated with the item */
    private java.lang.String externalReference;
    /* Content type of item/attachment content (e.g. pdf, doc, msg,
     * etc.) */
    private java.lang.String contentType;
    /* Number of pages that exist for the attachment's content */
    private java.lang.Integer pageNo;
    /* Status of the rendition of the item's content. In case the
     * rendition is successful
     *                         you can download the rendition PDF. In case
     * of failure, you can download an HTML
     *                         containing a description of the failure. */
    private eu.cec.digit.circabc.repo.hrs.ws.RenditionStatus renditionStatus;
    /* If the item is checked out, this element holds checkout
     *                         information */
    private eu.cec.digit.circabc.repo.hrs.ws.ItemCheckedOut checkedOut;
    /* This item's translations */
    private eu.cec.digit.circabc.repo.hrs.ws.Item[] translations;
    /* Size of item/attachment content in bytes */
    private java.lang.Integer contentSize;
    /* Indicates if more than one version of this item exist. Using
     * checkout /
     *                         checkin an item can be versioned and becomes
     * part of a version tree.
     *                         HRS always returns the current version of
     * the version tree. */
    private java.lang.Boolean versioned;
    /* The version label of this item. For example 1.0, 1.1, 2.0,
     * ... */
    private java.lang.String versionLabel;
    /* Mime type of item/attachment content (e.g. text/html, application/vnd.ms-excel,
     * etc.) */
    private java.lang.String mimeType;
    private java.lang.Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;

    public Item() {
    }

    public Item(
            java.lang.String itemId,
            java.lang.String name,
            java.lang.String language,
            eu.cec.digit.circabc.repo.hrs.ws.AttachmentType attachmentType,
            eu.cec.digit.circabc.repo.hrs.ws.ItemKind kind,
            boolean isScanWithoutContent,
            java.util.Calendar modificationDate,
            java.lang.String externalReference,
            java.lang.String contentType,
            java.lang.Integer pageNo,
            eu.cec.digit.circabc.repo.hrs.ws.RenditionStatus renditionStatus,
            eu.cec.digit.circabc.repo.hrs.ws.ItemCheckedOut checkedOut,
            eu.cec.digit.circabc.repo.hrs.ws.Item[] translations,
            java.lang.Integer contentSize,
            java.lang.Boolean versioned,
            java.lang.String versionLabel,
            java.lang.String mimeType) {
        this.itemId = itemId;
        this.name = name;
        this.language = language;
        this.attachmentType = attachmentType;
        this.kind = kind;
        this.isScanWithoutContent = isScanWithoutContent;
        this.modificationDate = modificationDate;
        this.externalReference = externalReference;
        this.contentType = contentType;
        this.pageNo = pageNo;
        this.renditionStatus = renditionStatus;
        this.checkedOut = checkedOut;
        this.translations = translations;
        this.contentSize = contentSize;
        this.versioned = versioned;
        this.versionLabel = versionLabel;
        this.mimeType = mimeType;
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
     * Gets the itemId value for this Item.
     *
     * @return itemId * Internal repository ID to uniquely identify an item
     */
    public java.lang.String getItemId() {
        return itemId;
    }

    /**
     * Sets the itemId value for this Item.
     *
     * @param itemId * Internal repository ID to uniquely identify an item
     */
    public void setItemId(java.lang.String itemId) {
        this.itemId = itemId;
    }

    /**
     * Gets the name value for this Item.
     *
     * @return name * The item's name
     */
    public java.lang.String getName() {
        return name;
    }

    /**
     * Sets the name value for this Item.
     *
     * @param name * The item's name
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }

    /**
     * Gets the language value for this Item.
     *
     * @return language * The item's language
     */
    public java.lang.String getLanguage() {
        return language;
    }

    /**
     * Sets the language value for this Item.
     *
     * @param language * The item's language
     */
    public void setLanguage(java.lang.String language) {
        this.language = language;
    }

    /**
     * Gets the attachmentType value for this Item.
     *
     * @return attachmentType * Type of the item such as NATIVE_ELECTRONIC, ARES_SCANNED etc.
     */
    public eu.cec.digit.circabc.repo.hrs.ws.AttachmentType getAttachmentType() {
        return attachmentType;
    }

    /**
     * Sets the attachmentType value for this Item.
     *
     * @param attachmentType * Type of the item such as NATIVE_ELECTRONIC, ARES_SCANNED etc.
     */
    public void setAttachmentType(eu.cec.digit.circabc.repo.hrs.ws.AttachmentType attachmentType) {
        this.attachmentType = attachmentType;
    }

    /**
     * Gets the kind value for this Item.
     *
     * @return kind * Kind of item such as MAIN, ANNEX, TRANSLATION etc.
     */
    public eu.cec.digit.circabc.repo.hrs.ws.ItemKind getKind() {
        return kind;
    }

    /**
     * Sets the kind value for this Item.
     *
     * @param kind * Kind of item such as MAIN, ANNEX, TRANSLATION etc.
     */
    public void setKind(eu.cec.digit.circabc.repo.hrs.ws.ItemKind kind) {
        this.kind = kind;
    }

    /**
     * Gets the isScanWithoutContent value for this Item.
     *
     * @return isScanWithoutContent * Is true for items of type ARES_SCANNED (i.e. scans) where the
     *     content hasn't been scanned yet. Is false in all other cases.
     */
    public boolean isIsScanWithoutContent() {
        return isScanWithoutContent;
    }

    /**
     * Sets the isScanWithoutContent value for this Item.
     *
     * @param isScanWithoutContent * Is true for items of type ARES_SCANNED (i.e. scans) where the
     *     content hasn't been scanned yet. Is false in all other cases.
     */
    public void setIsScanWithoutContent(boolean isScanWithoutContent) {
        this.isScanWithoutContent = isScanWithoutContent;
    }

    /**
     * Gets the modificationDate value for this Item.
     *
     * @return modificationDate * Date+Time when the item was last modified. A modification is
     *     considered to be any change to the item's metadata or to the item's content such as a
     *     checkin or scan operation.
     */
    public java.util.Calendar getModificationDate() {
        return modificationDate;
    }

    /**
     * Sets the modificationDate value for this Item.
     *
     * @param modificationDate * Date+Time when the item was last modified. A modification is
     *     considered to be any change to the item's metadata or to the item's content such as a
     *     checkin or scan operation.
     */
    public void setModificationDate(java.util.Calendar modificationDate) {
        this.modificationDate = modificationDate;
    }

    /**
     * Gets the externalReference value for this Item.
     *
     * @return externalReference * External reference associated with the item
     */
    public java.lang.String getExternalReference() {
        return externalReference;
    }

    /**
     * Sets the externalReference value for this Item.
     *
     * @param externalReference * External reference associated with the item
     */
    public void setExternalReference(java.lang.String externalReference) {
        this.externalReference = externalReference;
    }

    /**
     * Gets the contentType value for this Item.
     *
     * @return contentType * Content type of item/attachment content (e.g. pdf, doc, msg, etc.)
     */
    public java.lang.String getContentType() {
        return contentType;
    }

    /**
     * Sets the contentType value for this Item.
     *
     * @param contentType * Content type of item/attachment content (e.g. pdf, doc, msg, etc.)
     */
    public void setContentType(java.lang.String contentType) {
        this.contentType = contentType;
    }

    /**
     * Gets the pageNo value for this Item.
     *
     * @return pageNo * Number of pages that exist for the attachment's content
     */
    public java.lang.Integer getPageNo() {
        return pageNo;
    }

    /**
     * Sets the pageNo value for this Item.
     *
     * @param pageNo * Number of pages that exist for the attachment's content
     */
    public void setPageNo(java.lang.Integer pageNo) {
        this.pageNo = pageNo;
    }

    /**
     * Gets the renditionStatus value for this Item.
     *
     * @return renditionStatus * Status of the rendition of the item's content. In case the rendition
     *     is successful you can download the rendition PDF. In case of failure, you can download an
     *     HTML containing a description of the failure.
     */
    public eu.cec.digit.circabc.repo.hrs.ws.RenditionStatus getRenditionStatus() {
        return renditionStatus;
    }

    /**
     * Sets the renditionStatus value for this Item.
     *
     * @param renditionStatus * Status of the rendition of the item's content. In case the rendition
     *     is successful you can download the rendition PDF. In case of failure, you can download an
     *     HTML containing a description of the failure.
     */
    public void setRenditionStatus(eu.cec.digit.circabc.repo.hrs.ws.RenditionStatus renditionStatus) {
        this.renditionStatus = renditionStatus;
    }

    /**
     * Gets the checkedOut value for this Item.
     *
     * @return checkedOut * If the item is checked out, this element holds checkout information
     */
    public eu.cec.digit.circabc.repo.hrs.ws.ItemCheckedOut getCheckedOut() {
        return checkedOut;
    }

    /**
     * Sets the checkedOut value for this Item.
     *
     * @param checkedOut * If the item is checked out, this element holds checkout information
     */
    public void setCheckedOut(eu.cec.digit.circabc.repo.hrs.ws.ItemCheckedOut checkedOut) {
        this.checkedOut = checkedOut;
    }

    /**
     * Gets the translations value for this Item.
     *
     * @return translations * This item's translations
     */
    public eu.cec.digit.circabc.repo.hrs.ws.Item[] getTranslations() {
        return translations;
    }

    /**
     * Sets the translations value for this Item.
     *
     * @param translations * This item's translations
     */
    public void setTranslations(eu.cec.digit.circabc.repo.hrs.ws.Item[] translations) {
        this.translations = translations;
    }

    /**
     * Gets the contentSize value for this Item.
     *
     * @return contentSize * Size of item/attachment content in bytes
     */
    public java.lang.Integer getContentSize() {
        return contentSize;
    }

    /**
     * Sets the contentSize value for this Item.
     *
     * @param contentSize * Size of item/attachment content in bytes
     */
    public void setContentSize(java.lang.Integer contentSize) {
        this.contentSize = contentSize;
    }

    /**
     * Gets the versioned value for this Item.
     *
     * @return versioned * Indicates if more than one version of this item exist. Using checkout /
     *     checkin an item can be versioned and becomes part of a version tree. HRS always returns the
     *     current version of the version tree.
     */
    public java.lang.Boolean getVersioned() {
        return versioned;
    }

    /**
     * Sets the versioned value for this Item.
     *
     * @param versioned * Indicates if more than one version of this item exist. Using checkout /
     *     checkin an item can be versioned and becomes part of a version tree. HRS always returns the
     *     current version of the version tree.
     */
    public void setVersioned(java.lang.Boolean versioned) {
        this.versioned = versioned;
    }

    /**
     * Gets the versionLabel value for this Item.
     *
     * @return versionLabel * The version label of this item. For example 1.0, 1.1, 2.0, ...
     */
    public java.lang.String getVersionLabel() {
        return versionLabel;
    }

    /**
     * Sets the versionLabel value for this Item.
     *
     * @param versionLabel * The version label of this item. For example 1.0, 1.1, 2.0, ...
     */
    public void setVersionLabel(java.lang.String versionLabel) {
        this.versionLabel = versionLabel;
    }

    /**
     * Gets the mimeType value for this Item.
     *
     * @return mimeType * Mime type of item/attachment content (e.g. text/html,
     *     application/vnd.ms-excel, etc.)
     */
    public java.lang.String getMimeType() {
        return mimeType;
    }

    /**
     * Sets the mimeType value for this Item.
     *
     * @param mimeType * Mime type of item/attachment content (e.g. text/html,
     *     application/vnd.ms-excel, etc.)
     */
    public void setMimeType(java.lang.String mimeType) {
        this.mimeType = mimeType;
    }

    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Item)) {
            return false;
        }
        Item other = (Item) obj;
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
                        && ((this.name == null && other.getName() == null)
                        || (this.name != null && this.name.equals(other.getName())))
                        && ((this.language == null && other.getLanguage() == null)
                        || (this.language != null && this.language.equals(other.getLanguage())))
                        && ((this.attachmentType == null && other.getAttachmentType() == null)
                        || (this.attachmentType != null
                        && this.attachmentType.equals(other.getAttachmentType())))
                        && ((this.kind == null && other.getKind() == null)
                        || (this.kind != null && this.kind.equals(other.getKind())))
                        && this.isScanWithoutContent == other.isIsScanWithoutContent()
                        && ((this.modificationDate == null && other.getModificationDate() == null)
                        || (this.modificationDate != null
                        && this.modificationDate.equals(other.getModificationDate())))
                        && ((this.externalReference == null && other.getExternalReference() == null)
                        || (this.externalReference != null
                        && this.externalReference.equals(other.getExternalReference())))
                        && ((this.contentType == null && other.getContentType() == null)
                        || (this.contentType != null && this.contentType.equals(other.getContentType())))
                        && ((this.pageNo == null && other.getPageNo() == null)
                        || (this.pageNo != null && this.pageNo.equals(other.getPageNo())))
                        && ((this.renditionStatus == null && other.getRenditionStatus() == null)
                        || (this.renditionStatus != null
                        && this.renditionStatus.equals(other.getRenditionStatus())))
                        && ((this.checkedOut == null && other.getCheckedOut() == null)
                        || (this.checkedOut != null && this.checkedOut.equals(other.getCheckedOut())))
                        && ((this.translations == null && other.getTranslations() == null)
                        || (this.translations != null
                        && java.util.Arrays.equals(this.translations, other.getTranslations())))
                        && ((this.contentSize == null && other.getContentSize() == null)
                        || (this.contentSize != null && this.contentSize.equals(other.getContentSize())))
                        && ((this.versioned == null && other.getVersioned() == null)
                        || (this.versioned != null && this.versioned.equals(other.getVersioned())))
                        && ((this.versionLabel == null && other.getVersionLabel() == null)
                        || (this.versionLabel != null && this.versionLabel.equals(other.getVersionLabel())))
                        && ((this.mimeType == null && other.getMimeType() == null)
                        || (this.mimeType != null && this.mimeType.equals(other.getMimeType())));
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
        if (getName() != null) {
            _hashCode += getName().hashCode();
        }
        if (getLanguage() != null) {
            _hashCode += getLanguage().hashCode();
        }
        if (getAttachmentType() != null) {
            _hashCode += getAttachmentType().hashCode();
        }
        if (getKind() != null) {
            _hashCode += getKind().hashCode();
        }
        _hashCode += (isIsScanWithoutContent() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        if (getModificationDate() != null) {
            _hashCode += getModificationDate().hashCode();
        }
        if (getExternalReference() != null) {
            _hashCode += getExternalReference().hashCode();
        }
        if (getContentType() != null) {
            _hashCode += getContentType().hashCode();
        }
        if (getPageNo() != null) {
            _hashCode += getPageNo().hashCode();
        }
        if (getRenditionStatus() != null) {
            _hashCode += getRenditionStatus().hashCode();
        }
        if (getCheckedOut() != null) {
            _hashCode += getCheckedOut().hashCode();
        }
        if (getTranslations() != null) {
            for (int i = 0; i < java.lang.reflect.Array.getLength(getTranslations()); i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getTranslations(), i);
                if (obj != null && !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getContentSize() != null) {
            _hashCode += getContentSize().hashCode();
        }
        if (getVersioned() != null) {
            _hashCode += getVersioned().hashCode();
        }
        if (getVersionLabel() != null) {
            _hashCode += getVersionLabel().hashCode();
        }
        if (getMimeType() != null) {
            _hashCode += getMimeType().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }
}
