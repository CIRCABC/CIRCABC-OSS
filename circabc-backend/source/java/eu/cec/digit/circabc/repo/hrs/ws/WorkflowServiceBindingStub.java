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
 * <p>WorkflowServiceBindingStub.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 * <p>
 * WorkflowServiceBindingStub.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
/**
 * WorkflowServiceBindingStub.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package eu.cec.digit.circabc.repo.hrs.ws;

public class WorkflowServiceBindingStub extends org.apache.axis.client.Stub
        implements eu.cec.digit.circabc.repo.hrs.ws.WorkflowService_PortType {

    static org.apache.axis.description.OperationDesc[] _operations;

    static {
        _operations = new org.apache.axis.description.OperationDesc[6];
        _initOperationDesc1();
    }

    private java.util.Vector cachedSerClasses = new java.util.Vector();
    private java.util.Vector cachedSerQNames = new java.util.Vector();
    private java.util.Vector cachedSerFactories = new java.util.Vector();
    private java.util.Vector cachedDeserFactories = new java.util.Vector();

    public WorkflowServiceBindingStub() throws org.apache.axis.AxisFault {
        this(null);
    }

    public WorkflowServiceBindingStub(java.net.URL endpointURL, javax.xml.rpc.Service service)
            throws org.apache.axis.AxisFault {
        this(service);
        super.cachedEndpoint = endpointURL;
    }

    public WorkflowServiceBindingStub(javax.xml.rpc.Service service)
            throws org.apache.axis.AxisFault {
        if (service == null) {
            super.service = new org.apache.axis.client.Service();
        } else {
            super.service = service;
        }
        ((org.apache.axis.client.Service) super.service).setTypeMappingVersion("1.2");
        java.lang.Class cls;
        javax.xml.namespace.QName qName;
        javax.xml.namespace.QName qName2;
        java.lang.Class beansf = org.apache.axis.encoding.ser.BeanSerializerFactory.class;
        java.lang.Class beandf = org.apache.axis.encoding.ser.BeanDeserializerFactory.class;
        java.lang.Class enumsf = org.apache.axis.encoding.ser.EnumSerializerFactory.class;
        java.lang.Class enumdf = org.apache.axis.encoding.ser.EnumDeserializerFactory.class;
        java.lang.Class arraysf = org.apache.axis.encoding.ser.ArraySerializerFactory.class;
        java.lang.Class arraydf = org.apache.axis.encoding.ser.ArrayDeserializerFactory.class;
        java.lang.Class simplesf = org.apache.axis.encoding.ser.SimpleSerializerFactory.class;
        java.lang.Class simpledf = org.apache.axis.encoding.ser.SimpleDeserializerFactory.class;
        java.lang.Class simplelistsf = org.apache.axis.encoding.ser.SimpleListSerializerFactory.class;
        java.lang.Class simplelistdf = org.apache.axis.encoding.ser.SimpleListDeserializerFactory.class;
        qName =
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", ">AddAssignmentsRequest>tasks");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.AssignmentTaskToAdd[].class;
        cachedSerClasses.add(cls);
        qName =
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "AssignmentTaskToAdd");
        qName2 = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "task");
        cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
        cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

        qName =
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", ">AddSignatoryTasksRequest>tasks");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.SignatoryTaskToAdd[].class;
        cachedSerClasses.add(cls);
        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "SignatoryTaskToAdd");
        qName2 = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "task");
        cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
        cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

        qName =
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", ">AssignmentTask>code");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.AssignmentTaskCode.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(enumsf);
        cachedDeserFactories.add(enumdf);

        qName =
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", ">AssignmentTaskToAdd>code");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.AssignmentTaskToAddCode.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(enumsf);
        cachedDeserFactories.add(enumdf);

        qName =
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", ">AssignmentWorkflow>tasks");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.AssignmentTask[].class;
        cachedSerClasses.add(cls);
        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "AssignmentTask");
        qName2 = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "task");
        cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
        cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

        qName =
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", ">CreateSignatoryRequest>tasks");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.SignatoryTaskToAdd[].class;
        cachedSerClasses.add(cls);
        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "SignatoryTaskToAdd");
        qName2 = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "task");
        cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
        cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

        qName =
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", ">SignatoryTask>code");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.SignatoryTaskCode.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(enumsf);
        cachedDeserFactories.add(enumdf);

        qName =
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", ">SignatoryTaskToAdd>code");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.SignatoryTaskToAddCode.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(enumsf);
        cachedDeserFactories.add(enumdf);

        qName =
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", ">SignatoryWorkflow>tasks");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.SignatoryTask[].class;
        cachedSerClasses.add(cls);
        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "SignatoryTask");
        qName2 = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "task");
        cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
        cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", ">Task>status");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.TaskStatus.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(enumsf);
        cachedDeserFactories.add(enumdf);

        qName =
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "AddAssignmentsRequest");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.AddAssignmentsRequest.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName =
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "AddSignatoryTasksRequest");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.AddSignatoryTasksRequest.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "AssignmentTask");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.AssignmentTask.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName =
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "AssignmentTaskToAdd");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.AssignmentTaskToAdd.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "AssignmentWorkflow");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.AssignmentWorkflow.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName =
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "BypassSignatoryTaskRequest");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.BypassSignatoryTaskRequest.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "CloseTaskRequest");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.CloseTaskRequest.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName =
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "CreateSignatoryRequest");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.CreateSignatoryRequest.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "CurrentEntityId");
        cachedSerQNames.add(qName);
        cls = java.lang.String.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(
                org.apache.axis.encoding.ser.BaseSerializerFactory.createFactory(
                        org.apache.axis.encoding.ser.SimpleSerializerFactory.class, cls, qName));
        cachedDeserFactories.add(
                org.apache.axis.encoding.ser.BaseDeserializerFactory.createFactory(
                        org.apache.axis.encoding.ser.SimpleDeserializerFactory.class, cls, qName));

        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "Header");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.Header.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "ObjectId");
        cachedSerQNames.add(qName);
        cls = java.lang.String.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(
                org.apache.axis.encoding.ser.BaseSerializerFactory.createFactory(
                        org.apache.axis.encoding.ser.SimpleSerializerFactory.class, cls, qName));
        cachedDeserFactories.add(
                org.apache.axis.encoding.ser.BaseDeserializerFactory.createFactory(
                        org.apache.axis.encoding.ser.SimpleDeserializerFactory.class, cls, qName));

        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "SignatoryTask");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.SignatoryTask.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "SignatoryTaskToAdd");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.SignatoryTaskToAdd.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "SignatoryType");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.SignatoryType.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(enumsf);
        cachedDeserFactories.add(enumdf);

        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "SignatoryWorkflow");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.SignatoryWorkflow.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "Task");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.Task.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "TaskToAdd");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.TaskToAdd.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "Workflow");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.Workflow.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "WorkflowUser");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.WorkflowUser.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);
    }

    private static void _initOperationDesc1() {
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("createSignatory");
        param =
                new org.apache.axis.description.ParameterDesc(
                        new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "header"),
                        org.apache.axis.description.ParameterDesc.IN,
                        new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "Header"),
                        eu.cec.digit.circabc.repo.hrs.ws.Header.class,
                        false,
                        false);
        oper.addParameter(param);
        param =
                new org.apache.axis.description.ParameterDesc(
                        new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "request"),
                        org.apache.axis.description.ParameterDesc.IN,
                        new javax.xml.namespace.QName(
                                "http://ec.europa.eu/sg/hrs/types", "CreateSignatoryRequest"),
                        eu.cec.digit.circabc.repo.hrs.ws.CreateSignatoryRequest.class,
                        false,
                        false);
        oper.addParameter(param);
        oper.setReturnType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "SignatoryWorkflow"));
        oper.setReturnClass(eu.cec.digit.circabc.repo.hrs.ws.SignatoryWorkflow.class);
        oper.setReturnQName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "result"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[0] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("addAssignments");
        param =
                new org.apache.axis.description.ParameterDesc(
                        new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "header"),
                        org.apache.axis.description.ParameterDesc.IN,
                        new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "Header"),
                        eu.cec.digit.circabc.repo.hrs.ws.Header.class,
                        false,
                        false);
        oper.addParameter(param);
        param =
                new org.apache.axis.description.ParameterDesc(
                        new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "request"),
                        org.apache.axis.description.ParameterDesc.IN,
                        new javax.xml.namespace.QName(
                                "http://ec.europa.eu/sg/hrs/types", "AddAssignmentsRequest"),
                        eu.cec.digit.circabc.repo.hrs.ws.AddAssignmentsRequest.class,
                        false,
                        false);
        oper.addParameter(param);
        oper.setReturnType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "AssignmentWorkflow"));
        oper.setReturnClass(eu.cec.digit.circabc.repo.hrs.ws.AssignmentWorkflow.class);
        oper.setReturnQName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "result"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[1] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("addSignatoryTasks");
        param =
                new org.apache.axis.description.ParameterDesc(
                        new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "header"),
                        org.apache.axis.description.ParameterDesc.IN,
                        new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "Header"),
                        eu.cec.digit.circabc.repo.hrs.ws.Header.class,
                        false,
                        false);
        oper.addParameter(param);
        param =
                new org.apache.axis.description.ParameterDesc(
                        new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "request"),
                        org.apache.axis.description.ParameterDesc.IN,
                        new javax.xml.namespace.QName(
                                "http://ec.europa.eu/sg/hrs/types", "AddSignatoryTasksRequest"),
                        eu.cec.digit.circabc.repo.hrs.ws.AddSignatoryTasksRequest.class,
                        false,
                        false);
        oper.addParameter(param);
        oper.setReturnType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "SignatoryWorkflow"));
        oper.setReturnClass(eu.cec.digit.circabc.repo.hrs.ws.SignatoryWorkflow.class);
        oper.setReturnQName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "result"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[2] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("launchESignatory");
        param =
                new org.apache.axis.description.ParameterDesc(
                        new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "header"),
                        org.apache.axis.description.ParameterDesc.IN,
                        new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "Header"),
                        eu.cec.digit.circabc.repo.hrs.ws.Header.class,
                        false,
                        false);
        oper.addParameter(param);
        param =
                new org.apache.axis.description.ParameterDesc(
                        new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "documentId"),
                        org.apache.axis.description.ParameterDesc.IN,
                        new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "ObjectId"),
                        java.lang.String.class,
                        false,
                        false);
        oper.addParameter(param);
        oper.setReturnType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "SignatoryWorkflow"));
        oper.setReturnClass(eu.cec.digit.circabc.repo.hrs.ws.SignatoryWorkflow.class);
        oper.setReturnQName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "result"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[3] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("closeTask");
        param =
                new org.apache.axis.description.ParameterDesc(
                        new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "header"),
                        org.apache.axis.description.ParameterDesc.IN,
                        new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "Header"),
                        eu.cec.digit.circabc.repo.hrs.ws.Header.class,
                        false,
                        false);
        oper.addParameter(param);
        param =
                new org.apache.axis.description.ParameterDesc(
                        new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "request"),
                        org.apache.axis.description.ParameterDesc.IN,
                        new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "CloseTaskRequest"),
                        eu.cec.digit.circabc.repo.hrs.ws.CloseTaskRequest.class,
                        false,
                        false);
        oper.addParameter(param);
        oper.setReturnType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        oper.setReturnClass(boolean.class);
        oper.setReturnQName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "success"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[4] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("bypassSignatoryTask");
        param =
                new org.apache.axis.description.ParameterDesc(
                        new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "header"),
                        org.apache.axis.description.ParameterDesc.IN,
                        new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "Header"),
                        eu.cec.digit.circabc.repo.hrs.ws.Header.class,
                        false,
                        false);
        oper.addParameter(param);
        param =
                new org.apache.axis.description.ParameterDesc(
                        new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "request"),
                        org.apache.axis.description.ParameterDesc.IN,
                        new javax.xml.namespace.QName(
                                "http://ec.europa.eu/sg/hrs/types", "BypassSignatoryTaskRequest"),
                        eu.cec.digit.circabc.repo.hrs.ws.BypassSignatoryTaskRequest.class,
                        false,
                        false);
        oper.addParameter(param);
        oper.setReturnType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        oper.setReturnClass(boolean.class);
        oper.setReturnQName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "success"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[5] = oper;
    }

    protected org.apache.axis.client.Call createCall() throws java.rmi.RemoteException {
        try {
            org.apache.axis.client.Call _call = super._createCall();
            if (super.maintainSessionSet) {
                _call.setMaintainSession(super.maintainSession);
            }
            if (super.cachedUsername != null) {
                _call.setUsername(super.cachedUsername);
            }
            if (super.cachedPassword != null) {
                _call.setPassword(super.cachedPassword);
            }
            if (super.cachedEndpoint != null) {
                _call.setTargetEndpointAddress(super.cachedEndpoint);
            }
            if (super.cachedTimeout != null) {
                _call.setTimeout(super.cachedTimeout);
            }
            if (super.cachedPortName != null) {
                _call.setPortName(super.cachedPortName);
            }
            java.util.Enumeration keys = super.cachedProperties.keys();
            while (keys.hasMoreElements()) {
                java.lang.String key = (java.lang.String) keys.nextElement();
                _call.setProperty(key, super.cachedProperties.get(key));
            }
            // All the type mapping information is registered
            // when the first call is made.
            // The type mapping information is actually registered in
            // the TypeMappingRegistry of the service, which
            // is the reason why registration is only needed for the first call.
            synchronized (this) {
                if (firstCall()) {
                    // must set encoding style before registering serializers
                    _call.setEncodingStyle(null);
                    for (int i = 0; i < cachedSerFactories.size(); ++i) {
                        java.lang.Class cls = (java.lang.Class) cachedSerClasses.get(i);
                        javax.xml.namespace.QName qName = (javax.xml.namespace.QName) cachedSerQNames.get(i);
                        java.lang.Object x = cachedSerFactories.get(i);
                        if (x instanceof Class) {
                            java.lang.Class sf = (java.lang.Class) cachedSerFactories.get(i);
                            java.lang.Class df = (java.lang.Class) cachedDeserFactories.get(i);
                            _call.registerTypeMapping(cls, qName, sf, df, false);
                        } else if (x instanceof javax.xml.rpc.encoding.SerializerFactory) {
                            org.apache.axis.encoding.SerializerFactory sf =
                                    (org.apache.axis.encoding.SerializerFactory) cachedSerFactories.get(i);
                            org.apache.axis.encoding.DeserializerFactory df =
                                    (org.apache.axis.encoding.DeserializerFactory) cachedDeserFactories.get(i);
                            _call.registerTypeMapping(cls, qName, sf, df, false);
                        }
                    }
                }
            }
            return _call;
        } catch (java.lang.Throwable _t) {
            throw new org.apache.axis.AxisFault("Failure trying to get the Call object", _t);
        }
    }

    public eu.cec.digit.circabc.repo.hrs.ws.SignatoryWorkflow createSignatory(
            eu.cec.digit.circabc.repo.hrs.ws.Header header,
            eu.cec.digit.circabc.repo.hrs.ws.CreateSignatoryRequest request)
            throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[0]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "createSignatory"));

        setRequestHeaders(_call);
        setAttachments(_call);
        try {
            java.lang.Object _resp = _call.invoke(new java.lang.Object[]{header, request});

            if (_resp instanceof java.rmi.RemoteException) {
                throw (java.rmi.RemoteException) _resp;
            } else {
                extractAttachments(_call);
                try {
                    return (eu.cec.digit.circabc.repo.hrs.ws.SignatoryWorkflow) _resp;
                } catch (java.lang.Exception _exception) {
                    return (eu.cec.digit.circabc.repo.hrs.ws.SignatoryWorkflow)
                            org.apache.axis.utils.JavaUtils.convert(
                                    _resp, eu.cec.digit.circabc.repo.hrs.ws.SignatoryWorkflow.class);
                }
            }
        } catch (org.apache.axis.AxisFault axisFaultException) {
            throw axisFaultException;
        }
    }

    public eu.cec.digit.circabc.repo.hrs.ws.AssignmentWorkflow addAssignments(
            eu.cec.digit.circabc.repo.hrs.ws.Header header,
            eu.cec.digit.circabc.repo.hrs.ws.AddAssignmentsRequest request)
            throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[1]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "addAssignments"));

        setRequestHeaders(_call);
        setAttachments(_call);
        try {
            java.lang.Object _resp = _call.invoke(new java.lang.Object[]{header, request});

            if (_resp instanceof java.rmi.RemoteException) {
                throw (java.rmi.RemoteException) _resp;
            } else {
                extractAttachments(_call);
                try {
                    return (eu.cec.digit.circabc.repo.hrs.ws.AssignmentWorkflow) _resp;
                } catch (java.lang.Exception _exception) {
                    return (eu.cec.digit.circabc.repo.hrs.ws.AssignmentWorkflow)
                            org.apache.axis.utils.JavaUtils.convert(
                                    _resp, eu.cec.digit.circabc.repo.hrs.ws.AssignmentWorkflow.class);
                }
            }
        } catch (org.apache.axis.AxisFault axisFaultException) {
            throw axisFaultException;
        }
    }

    public eu.cec.digit.circabc.repo.hrs.ws.SignatoryWorkflow addSignatoryTasks(
            eu.cec.digit.circabc.repo.hrs.ws.Header header,
            eu.cec.digit.circabc.repo.hrs.ws.AddSignatoryTasksRequest request)
            throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[2]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "addSignatoryTasks"));

        setRequestHeaders(_call);
        setAttachments(_call);
        try {
            java.lang.Object _resp = _call.invoke(new java.lang.Object[]{header, request});

            if (_resp instanceof java.rmi.RemoteException) {
                throw (java.rmi.RemoteException) _resp;
            } else {
                extractAttachments(_call);
                try {
                    return (eu.cec.digit.circabc.repo.hrs.ws.SignatoryWorkflow) _resp;
                } catch (java.lang.Exception _exception) {
                    return (eu.cec.digit.circabc.repo.hrs.ws.SignatoryWorkflow)
                            org.apache.axis.utils.JavaUtils.convert(
                                    _resp, eu.cec.digit.circabc.repo.hrs.ws.SignatoryWorkflow.class);
                }
            }
        } catch (org.apache.axis.AxisFault axisFaultException) {
            throw axisFaultException;
        }
    }

    public eu.cec.digit.circabc.repo.hrs.ws.SignatoryWorkflow launchESignatory(
            eu.cec.digit.circabc.repo.hrs.ws.Header header, java.lang.String documentId)
            throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[3]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "launchESignatory"));

        setRequestHeaders(_call);
        setAttachments(_call);
        try {
            java.lang.Object _resp = _call.invoke(new java.lang.Object[]{header, documentId});

            if (_resp instanceof java.rmi.RemoteException) {
                throw (java.rmi.RemoteException) _resp;
            } else {
                extractAttachments(_call);
                try {
                    return (eu.cec.digit.circabc.repo.hrs.ws.SignatoryWorkflow) _resp;
                } catch (java.lang.Exception _exception) {
                    return (eu.cec.digit.circabc.repo.hrs.ws.SignatoryWorkflow)
                            org.apache.axis.utils.JavaUtils.convert(
                                    _resp, eu.cec.digit.circabc.repo.hrs.ws.SignatoryWorkflow.class);
                }
            }
        } catch (org.apache.axis.AxisFault axisFaultException) {
            throw axisFaultException;
        }
    }

    public boolean closeTask(
            eu.cec.digit.circabc.repo.hrs.ws.Header header,
            eu.cec.digit.circabc.repo.hrs.ws.CloseTaskRequest request)
            throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[4]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "closeTask"));

        setRequestHeaders(_call);
        setAttachments(_call);
        try {
            java.lang.Object _resp = _call.invoke(new java.lang.Object[]{header, request});

            if (_resp instanceof java.rmi.RemoteException) {
                throw (java.rmi.RemoteException) _resp;
            } else {
                extractAttachments(_call);
                try {
                    return ((java.lang.Boolean) _resp).booleanValue();
                } catch (java.lang.Exception _exception) {
                    return ((java.lang.Boolean) org.apache.axis.utils.JavaUtils.convert(_resp, boolean.class))
                            .booleanValue();
                }
            }
        } catch (org.apache.axis.AxisFault axisFaultException) {
            throw axisFaultException;
        }
    }

    public boolean bypassSignatoryTask(
            eu.cec.digit.circabc.repo.hrs.ws.Header header,
            eu.cec.digit.circabc.repo.hrs.ws.BypassSignatoryTaskRequest request)
            throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[5]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "bypassSignatoryTask"));

        setRequestHeaders(_call);
        setAttachments(_call);
        try {
            java.lang.Object _resp = _call.invoke(new java.lang.Object[]{header, request});

            if (_resp instanceof java.rmi.RemoteException) {
                throw (java.rmi.RemoteException) _resp;
            } else {
                extractAttachments(_call);
                try {
                    return ((java.lang.Boolean) _resp).booleanValue();
                } catch (java.lang.Exception _exception) {
                    return ((java.lang.Boolean) org.apache.axis.utils.JavaUtils.convert(_resp, boolean.class))
                            .booleanValue();
                }
            }
        } catch (org.apache.axis.AxisFault axisFaultException) {
            throw axisFaultException;
        }
    }
}
