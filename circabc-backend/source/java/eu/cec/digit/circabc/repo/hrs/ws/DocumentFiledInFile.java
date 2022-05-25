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
 * <p>DocumentFiledInFile.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 * <p>
 * DocumentFiledInFile.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
/**
 * DocumentFiledInFile.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package eu.cec.digit.circabc.repo.hrs.ws;

public class DocumentFiledInFile implements java.io.Serializable {

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
            new org.apache.axis.description.TypeDesc(DocumentFiledInFile.class, true);

    static {
        typeDesc.setXmlType(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", ">>Document>filedIn>file"));
        org.apache.axis.description.ElementDesc elemField =
                new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("fileId");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "fileId"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("path");
        elemField.setXmlName(new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "path"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("chefDeFile");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "chefDeFile"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("fileCode");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "fileCode"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("specificCode");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "specificCode"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("status");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "status"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "FileStatus"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("multilingualNames");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "multilingualNames"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "Translation"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "multilingualName"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("filingDate");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "filingDate"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "dateTime"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /* Internal repository ID to uniquely identify the file */
    private java.lang.String fileId;
    /* Logical path to the file */
    private java.lang.String path;
    /* Name of 'chef de file' */
    private java.lang.String chefDeFile;
    /* The file's code */
    private java.lang.String fileCode;
    /* The file's specific code */
    private java.lang.String specificCode;
    /* The file's status */
    private eu.cec.digit.circabc.repo.hrs.ws.FileStatus status;
    /* Translations of the file's name */
    private eu.cec.digit.circabc.repo.hrs.ws.Translation[] multilingualNames;
    /* Date the document was filed in the file */
    private java.util.Calendar filingDate;
    private java.lang.Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;

    public DocumentFiledInFile() {
    }

    public DocumentFiledInFile(
            java.lang.String fileId,
            java.lang.String path,
            java.lang.String chefDeFile,
            java.lang.String fileCode,
            java.lang.String specificCode,
            eu.cec.digit.circabc.repo.hrs.ws.FileStatus status,
            eu.cec.digit.circabc.repo.hrs.ws.Translation[] multilingualNames,
            java.util.Calendar filingDate) {
        this.fileId = fileId;
        this.path = path;
        this.chefDeFile = chefDeFile;
        this.fileCode = fileCode;
        this.specificCode = specificCode;
        this.status = status;
        this.multilingualNames = multilingualNames;
        this.filingDate = filingDate;
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
     * Gets the fileId value for this DocumentFiledInFile.
     *
     * @return fileId * Internal repository ID to uniquely identify the file
     */
    public java.lang.String getFileId() {
        return fileId;
    }

    /**
     * Sets the fileId value for this DocumentFiledInFile.
     *
     * @param fileId * Internal repository ID to uniquely identify the file
     */
    public void setFileId(java.lang.String fileId) {
        this.fileId = fileId;
    }

    /**
     * Gets the path value for this DocumentFiledInFile.
     *
     * @return path * Logical path to the file
     */
    public java.lang.String getPath() {
        return path;
    }

    /**
     * Sets the path value for this DocumentFiledInFile.
     *
     * @param path * Logical path to the file
     */
    public void setPath(java.lang.String path) {
        this.path = path;
    }

    /**
     * Gets the chefDeFile value for this DocumentFiledInFile.
     *
     * @return chefDeFile * Name of 'chef de file'
     */
    public java.lang.String getChefDeFile() {
        return chefDeFile;
    }

    /**
     * Sets the chefDeFile value for this DocumentFiledInFile.
     *
     * @param chefDeFile * Name of 'chef de file'
     */
    public void setChefDeFile(java.lang.String chefDeFile) {
        this.chefDeFile = chefDeFile;
    }

    /**
     * Gets the fileCode value for this DocumentFiledInFile.
     *
     * @return fileCode * The file's code
     */
    public java.lang.String getFileCode() {
        return fileCode;
    }

    /**
     * Sets the fileCode value for this DocumentFiledInFile.
     *
     * @param fileCode * The file's code
     */
    public void setFileCode(java.lang.String fileCode) {
        this.fileCode = fileCode;
    }

    /**
     * Gets the specificCode value for this DocumentFiledInFile.
     *
     * @return specificCode * The file's specific code
     */
    public java.lang.String getSpecificCode() {
        return specificCode;
    }

    /**
     * Sets the specificCode value for this DocumentFiledInFile.
     *
     * @param specificCode * The file's specific code
     */
    public void setSpecificCode(java.lang.String specificCode) {
        this.specificCode = specificCode;
    }

    /**
     * Gets the status value for this DocumentFiledInFile.
     *
     * @return status * The file's status
     */
    public eu.cec.digit.circabc.repo.hrs.ws.FileStatus getStatus() {
        return status;
    }

    /**
     * Sets the status value for this DocumentFiledInFile.
     *
     * @param status * The file's status
     */
    public void setStatus(eu.cec.digit.circabc.repo.hrs.ws.FileStatus status) {
        this.status = status;
    }

    /**
     * Gets the multilingualNames value for this DocumentFiledInFile.
     *
     * @return multilingualNames * Translations of the file's name
     */
    public eu.cec.digit.circabc.repo.hrs.ws.Translation[] getMultilingualNames() {
        return multilingualNames;
    }

    /**
     * Sets the multilingualNames value for this DocumentFiledInFile.
     *
     * @param multilingualNames * Translations of the file's name
     */
    public void setMultilingualNames(
            eu.cec.digit.circabc.repo.hrs.ws.Translation[] multilingualNames) {
        this.multilingualNames = multilingualNames;
    }

    /**
     * Gets the filingDate value for this DocumentFiledInFile.
     *
     * @return filingDate * Date the document was filed in the file
     */
    public java.util.Calendar getFilingDate() {
        return filingDate;
    }

    /**
     * Sets the filingDate value for this DocumentFiledInFile.
     *
     * @param filingDate * Date the document was filed in the file
     */
    public void setFilingDate(java.util.Calendar filingDate) {
        this.filingDate = filingDate;
    }

    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof DocumentFiledInFile)) {
            return false;
        }
        DocumentFiledInFile other = (DocumentFiledInFile) obj;
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
                        && ((this.fileId == null && other.getFileId() == null)
                        || (this.fileId != null && this.fileId.equals(other.getFileId())))
                        && ((this.path == null && other.getPath() == null)
                        || (this.path != null && this.path.equals(other.getPath())))
                        && ((this.chefDeFile == null && other.getChefDeFile() == null)
                        || (this.chefDeFile != null && this.chefDeFile.equals(other.getChefDeFile())))
                        && ((this.fileCode == null && other.getFileCode() == null)
                        || (this.fileCode != null && this.fileCode.equals(other.getFileCode())))
                        && ((this.specificCode == null && other.getSpecificCode() == null)
                        || (this.specificCode != null && this.specificCode.equals(other.getSpecificCode())))
                        && ((this.status == null && other.getStatus() == null)
                        || (this.status != null && this.status.equals(other.getStatus())))
                        && ((this.multilingualNames == null && other.getMultilingualNames() == null)
                        || (this.multilingualNames != null
                        && java.util.Arrays.equals(
                        this.multilingualNames, other.getMultilingualNames())))
                        && ((this.filingDate == null && other.getFilingDate() == null)
                        || (this.filingDate != null && this.filingDate.equals(other.getFilingDate())));
        __equalsCalc = null;
        return _equals;
    }

    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getFileId() != null) {
            _hashCode += getFileId().hashCode();
        }
        if (getPath() != null) {
            _hashCode += getPath().hashCode();
        }
        if (getChefDeFile() != null) {
            _hashCode += getChefDeFile().hashCode();
        }
        if (getFileCode() != null) {
            _hashCode += getFileCode().hashCode();
        }
        if (getSpecificCode() != null) {
            _hashCode += getSpecificCode().hashCode();
        }
        if (getStatus() != null) {
            _hashCode += getStatus().hashCode();
        }
        if (getMultilingualNames() != null) {
            for (int i = 0; i < java.lang.reflect.Array.getLength(getMultilingualNames()); i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getMultilingualNames(), i);
                if (obj != null && !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getFilingDate() != null) {
            _hashCode += getFilingDate().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }
}
