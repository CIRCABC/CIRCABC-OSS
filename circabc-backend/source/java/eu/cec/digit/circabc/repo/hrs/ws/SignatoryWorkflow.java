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
 * <p>SignatoryWorkflow.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 * <p>
 * SignatoryWorkflow.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
/**
 * SignatoryWorkflow.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package eu.cec.digit.circabc.repo.hrs.ws;

/** A workflow of type Signatory, can be a Paper or e-Signatory workflow */
public class SignatoryWorkflow extends eu.cec.digit.circabc.repo.hrs.ws.Workflow
        implements java.io.Serializable {

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
            new org.apache.axis.description.TypeDesc(SignatoryWorkflow.class, true);

    static {
        typeDesc.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "SignatoryWorkflow"));
        org.apache.axis.description.ElementDesc elemField =
                new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("manager");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "manager"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "WorkflowUser"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("launcher");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "launcher"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "WorkflowUser"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("launchDate");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "launchDate"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "date"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("signatoryType");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "signatoryType"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "SignatoryType"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("lastESignatoryTaskId");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "lastESignatoryTaskId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("tasks");
        elemField.setXmlName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "tasks"));
        elemField.setXmlType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "SignatoryTask"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "task"));
        typeDesc.addFieldDesc(elemField);
    }

    /* Manager of the workflow */
    private eu.cec.digit.circabc.repo.hrs.ws.WorkflowUser manager;
    /* User that launched the workflow */
    private eu.cec.digit.circabc.repo.hrs.ws.WorkflowUser launcher;
    /* Day when the workflow has been launched */
    private java.util.Date launchDate;
    /* Type of workflow (paper or eSignatory) */
    private eu.cec.digit.circabc.repo.hrs.ws.SignatoryType signatoryType;
    /* Id of the last eSignatory task in the workflow. The field is
     * present only if the
     *                                 workflow type is eSignatory. */
    private java.lang.Integer lastESignatoryTaskId;
    /* The collection of tasks of the Signatory workflow */
    private eu.cec.digit.circabc.repo.hrs.ws.SignatoryTask[] tasks;
    private java.lang.Object __equalsCalc = null;
    private boolean __hashCodeCalc = false;

    public SignatoryWorkflow() {
    }

    public SignatoryWorkflow(
            int workflowId,
            java.lang.String documentId,
            java.lang.Boolean active,
            eu.cec.digit.circabc.repo.hrs.ws.WorkflowUser manager,
            eu.cec.digit.circabc.repo.hrs.ws.WorkflowUser launcher,
            java.util.Date launchDate,
            eu.cec.digit.circabc.repo.hrs.ws.SignatoryType signatoryType,
            java.lang.Integer lastESignatoryTaskId,
            eu.cec.digit.circabc.repo.hrs.ws.SignatoryTask[] tasks) {
        super(workflowId, documentId, active);
        this.manager = manager;
        this.launcher = launcher;
        this.launchDate = launchDate;
        this.signatoryType = signatoryType;
        this.lastESignatoryTaskId = lastESignatoryTaskId;
        this.tasks = tasks;
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
     * Gets the manager value for this SignatoryWorkflow.
     *
     * @return manager * Manager of the workflow
     */
    public eu.cec.digit.circabc.repo.hrs.ws.WorkflowUser getManager() {
        return manager;
    }

    /**
     * Sets the manager value for this SignatoryWorkflow.
     *
     * @param manager * Manager of the workflow
     */
    public void setManager(eu.cec.digit.circabc.repo.hrs.ws.WorkflowUser manager) {
        this.manager = manager;
    }

    /**
     * Gets the launcher value for this SignatoryWorkflow.
     *
     * @return launcher * User that launched the workflow
     */
    public eu.cec.digit.circabc.repo.hrs.ws.WorkflowUser getLauncher() {
        return launcher;
    }

    /**
     * Sets the launcher value for this SignatoryWorkflow.
     *
     * @param launcher * User that launched the workflow
     */
    public void setLauncher(eu.cec.digit.circabc.repo.hrs.ws.WorkflowUser launcher) {
        this.launcher = launcher;
    }

    /**
     * Gets the launchDate value for this SignatoryWorkflow.
     *
     * @return launchDate * Day when the workflow has been launched
     */
    public java.util.Date getLaunchDate() {
        return launchDate;
    }

    /**
     * Sets the launchDate value for this SignatoryWorkflow.
     *
     * @param launchDate * Day when the workflow has been launched
     */
    public void setLaunchDate(java.util.Date launchDate) {
        this.launchDate = launchDate;
    }

    /**
     * Gets the signatoryType value for this SignatoryWorkflow.
     *
     * @return signatoryType * Type of workflow (paper or eSignatory)
     */
    public eu.cec.digit.circabc.repo.hrs.ws.SignatoryType getSignatoryType() {
        return signatoryType;
    }

    /**
     * Sets the signatoryType value for this SignatoryWorkflow.
     *
     * @param signatoryType * Type of workflow (paper or eSignatory)
     */
    public void setSignatoryType(eu.cec.digit.circabc.repo.hrs.ws.SignatoryType signatoryType) {
        this.signatoryType = signatoryType;
    }

    /**
     * Gets the lastESignatoryTaskId value for this SignatoryWorkflow.
     *
     * @return lastESignatoryTaskId * Id of the last eSignatory task in the workflow. The field is
     *     present only if the workflow type is eSignatory.
     */
    public java.lang.Integer getLastESignatoryTaskId() {
        return lastESignatoryTaskId;
    }

    /**
     * Sets the lastESignatoryTaskId value for this SignatoryWorkflow.
     *
     * @param lastESignatoryTaskId * Id of the last eSignatory task in the workflow. The field is
     *     present only if the workflow type is eSignatory.
     */
    public void setLastESignatoryTaskId(java.lang.Integer lastESignatoryTaskId) {
        this.lastESignatoryTaskId = lastESignatoryTaskId;
    }

    /**
     * Gets the tasks value for this SignatoryWorkflow.
     *
     * @return tasks * The collection of tasks of the Signatory workflow
     */
    public eu.cec.digit.circabc.repo.hrs.ws.SignatoryTask[] getTasks() {
        return tasks;
    }

    /**
     * Sets the tasks value for this SignatoryWorkflow.
     *
     * @param tasks * The collection of tasks of the Signatory workflow
     */
    public void setTasks(eu.cec.digit.circabc.repo.hrs.ws.SignatoryTask[] tasks) {
        this.tasks = tasks;
    }

    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof SignatoryWorkflow)) {
            return false;
        }
        SignatoryWorkflow other = (SignatoryWorkflow) obj;
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
                super.equals(obj)
                        && ((this.manager == null && other.getManager() == null)
                        || (this.manager != null && this.manager.equals(other.getManager())))
                        && ((this.launcher == null && other.getLauncher() == null)
                        || (this.launcher != null && this.launcher.equals(other.getLauncher())))
                        && ((this.launchDate == null && other.getLaunchDate() == null)
                        || (this.launchDate != null && this.launchDate.equals(other.getLaunchDate())))
                        && ((this.signatoryType == null && other.getSignatoryType() == null)
                        || (this.signatoryType != null
                        && this.signatoryType.equals(other.getSignatoryType())))
                        && ((this.lastESignatoryTaskId == null && other.getLastESignatoryTaskId() == null)
                        || (this.lastESignatoryTaskId != null
                        && this.lastESignatoryTaskId.equals(other.getLastESignatoryTaskId())))
                        && ((this.tasks == null && other.getTasks() == null)
                        || (this.tasks != null && java.util.Arrays.equals(this.tasks, other.getTasks())));
        __equalsCalc = null;
        return _equals;
    }

    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = super.hashCode();
        if (getManager() != null) {
            _hashCode += getManager().hashCode();
        }
        if (getLauncher() != null) {
            _hashCode += getLauncher().hashCode();
        }
        if (getLaunchDate() != null) {
            _hashCode += getLaunchDate().hashCode();
        }
        if (getSignatoryType() != null) {
            _hashCode += getSignatoryType().hashCode();
        }
        if (getLastESignatoryTaskId() != null) {
            _hashCode += getLastESignatoryTaskId().hashCode();
        }
        if (getTasks() != null) {
            for (int i = 0; i < java.lang.reflect.Array.getLength(getTasks()); i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getTasks(), i);
                if (obj != null && !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        __hashCodeCalc = false;
        return _hashCode;
    }
}
