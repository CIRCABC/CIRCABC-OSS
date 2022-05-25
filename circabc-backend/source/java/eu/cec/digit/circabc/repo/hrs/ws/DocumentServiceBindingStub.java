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
 * <p>DocumentServiceBindingStub.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 * <p>
 * DocumentServiceBindingStub.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
/**
 * DocumentServiceBindingStub.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package eu.cec.digit.circabc.repo.hrs.ws;

public class DocumentServiceBindingStub extends org.apache.axis.client.Stub
        implements eu.cec.digit.circabc.repo.hrs.ws.DocumentService_PortType {

    static org.apache.axis.description.OperationDesc[] _operations;

    static {
        _operations = new org.apache.axis.description.OperationDesc[21];
        _initOperationDesc1();
        _initOperationDesc2();
        _initOperationDesc3();
    }

    private java.util.Vector cachedSerClasses = new java.util.Vector();
    private java.util.Vector cachedSerQNames = new java.util.Vector();
    private java.util.Vector cachedSerFactories = new java.util.Vector();
    private java.util.Vector cachedDeserFactories = new java.util.Vector();

    public DocumentServiceBindingStub() throws org.apache.axis.AxisFault {
        this(null);
    }

    public DocumentServiceBindingStub(java.net.URL endpointURL, javax.xml.rpc.Service service)
            throws org.apache.axis.AxisFault {
        this(service);
        super.cachedEndpoint = endpointURL;
    }

    public DocumentServiceBindingStub(javax.xml.rpc.Service service)
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
        addBindings0();
    }

    private static void _initOperationDesc1() {
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("fileDocument");
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
        param =
                new org.apache.axis.description.ParameterDesc(
                        new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "fileId"),
                        org.apache.axis.description.ParameterDesc.IN,
                        new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "ObjectId"),
                        java.lang.String.class,
                        false,
                        false);
        oper.addParameter(param);
        oper.setReturnType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "OperationResponse"));
        oper.setReturnClass(eu.cec.digit.circabc.repo.hrs.ws.OperationResponse.class);
        oper.setReturnQName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "result"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[0] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("unfileDocument");
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
        param =
                new org.apache.axis.description.ParameterDesc(
                        new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "fileId"),
                        org.apache.axis.description.ParameterDesc.IN,
                        new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "ObjectId"),
                        java.lang.String.class,
                        false,
                        false);
        oper.addParameter(param);
        oper.setReturnType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "OperationResponse"));
        oper.setReturnClass(eu.cec.digit.circabc.repo.hrs.ws.OperationResponse.class);
        oper.setReturnQName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "result"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[1] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("createDocument");
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
                                "http://ec.europa.eu/sg/hrs/types", "DocumentCreationRequest"),
                        eu.cec.digit.circabc.repo.hrs.ws.DocumentCreationRequest.class,
                        false,
                        false);
        oper.addParameter(param);
        oper.setReturnType(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "DocumentCreationSummary"));
        oper.setReturnClass(eu.cec.digit.circabc.repo.hrs.ws.DocumentCreationSummary.class);
        oper.setReturnQName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "result"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[2] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("updateDocument");
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
                                "http://ec.europa.eu/sg/hrs/types", "DocumentUpdateRequest"),
                        eu.cec.digit.circabc.repo.hrs.ws.DocumentUpdateRequest.class,
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
        _operations[3] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("trashDocument");
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
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        oper.setReturnClass(boolean.class);
        oper.setReturnQName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "success"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[4] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("registerDocumentById");
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
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "RegistrationByIdSummary"));
        oper.setReturnClass(eu.cec.digit.circabc.repo.hrs.ws.RegistrationByIdSummary.class);
        oper.setReturnQName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "result"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[5] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("registerDocument");
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
                                "http://ec.europa.eu/sg/hrs/types", "DocumentRegistrationRequest"),
                        eu.cec.digit.circabc.repo.hrs.ws.DocumentRegistrationRequest.class,
                        false,
                        false);
        oper.addParameter(param);
        oper.setReturnType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "RegistrationSummary"));
        oper.setReturnClass(eu.cec.digit.circabc.repo.hrs.ws.RegistrationSummary.class);
        oper.setReturnQName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "document"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[6] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("checkoutItem");
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
                        new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "itemId"),
                        org.apache.axis.description.ParameterDesc.IN,
                        new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "ObjectId"),
                        java.lang.String.class,
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
        _operations[7] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("cancelItemCheckout");
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
                        new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "itemId"),
                        org.apache.axis.description.ParameterDesc.IN,
                        new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "ObjectId"),
                        java.lang.String.class,
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
        _operations[8] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("checkinItem");
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
                        new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "ItemCheckinRequest"),
                        eu.cec.digit.circabc.repo.hrs.ws.ItemCheckinRequest.class,
                        false,
                        false);
        oper.addParameter(param);
        oper.setReturnType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "ObjectId"));
        oper.setReturnClass(java.lang.String.class);
        oper.setReturnQName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "itemId"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[9] = oper;
    }

    private static void _initOperationDesc2() {
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("addTranslations");
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
                        new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "itemId"),
                        org.apache.axis.description.ParameterDesc.IN,
                        new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "ObjectId"),
                        java.lang.String.class,
                        false,
                        false);
        oper.addParameter(param);
        param =
                new org.apache.axis.description.ParameterDesc(
                        new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "translations"),
                        org.apache.axis.description.ParameterDesc.IN,
                        new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "TranslationsToAdd"),
                        eu.cec.digit.circabc.repo.hrs.ws.TranslationsToAddTranslation[].class,
                        false,
                        false);
        param.setItemQName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "translation"));
        oper.addParameter(param);
        oper.setReturnType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        oper.setReturnClass(boolean.class);
        oper.setReturnQName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "success"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[10] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getDocument");
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
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "Document"));
        oper.setReturnClass(eu.cec.digit.circabc.repo.hrs.ws.Document.class);
        oper.setReturnQName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "document"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[11] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("searchDocumentsByExpression");
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
                                "http://ec.europa.eu/sg/hrs/types", "DocumentSearchByExpressionRequest"),
                        eu.cec.digit.circabc.repo.hrs.ws.DocumentSearchByExpressionRequest.class,
                        false,
                        false);
        oper.addParameter(param);
        param =
                new org.apache.axis.description.ParameterDesc(
                        new javax.xml.namespace.QName(
                                "http://ec.europa.eu/sg/hrs/types", "documentRetrievalOptions"),
                        org.apache.axis.description.ParameterDesc.IN,
                        new javax.xml.namespace.QName(
                                "http://ec.europa.eu/sg/hrs/types", "DocumentRetrievalOptions"),
                        eu.cec.digit.circabc.repo.hrs.ws.DocumentRetrievalOptions.class,
                        false,
                        false);
        param.setOmittable(true);
        oper.addParameter(param);
        param =
                new org.apache.axis.description.ParameterDesc(
                        new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "sortOptions"),
                        org.apache.axis.description.ParameterDesc.IN,
                        new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "SortOptions"),
                        eu.cec.digit.circabc.repo.hrs.ws.SortOptionsSortBy[].class,
                        false,
                        false);
        param.setItemQName(new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "sortBy"));
        param.setOmittable(true);
        oper.addParameter(param);
        oper.setReturnType(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "DocumentSearchResponse"));
        oper.setReturnClass(eu.cec.digit.circabc.repo.hrs.ws.DocumentSearchResponse.class);
        oper.setReturnQName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "return"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[12] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("linkDocuments");
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
                                "http://ec.europa.eu/sg/hrs/types", "LinkDocumentsRequest"),
                        eu.cec.digit.circabc.repo.hrs.ws.LinkDocumentsRequest.class,
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
        _operations[13] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("unlinkDocuments");
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
                                "http://ec.europa.eu/sg/hrs/types", "UnlinkDocumentsRequest"),
                        eu.cec.digit.circabc.repo.hrs.ws.UnlinkDocumentsRequest.class,
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
        _operations[14] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("findPotentialRegisteredDuplicates");
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
                                "http://ec.europa.eu/sg/hrs/types", "DocumentRegistrationRequest"),
                        eu.cec.digit.circabc.repo.hrs.ws.DocumentRegistrationRequest.class,
                        false,
                        false);
        oper.addParameter(param);
        oper.setReturnType(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types",
                        ">>findPotentialRegisteredDuplicatesResponse>potentialDuplicateRegisteredDocuments"));
        oper.setReturnClass(
                eu.cec.digit.circabc.repo.hrs.ws
                        .FindPotentialRegisteredDuplicatesResponsePotentialDuplicateRegisteredDocumentsPotentialDuplicateRegisteredDocumentInfo
                        []
                        .class);
        oper.setReturnQName(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "potentialDuplicateRegisteredDocuments"));
        param = oper.getReturnParamDesc();
        param.setItemQName(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "potentialDuplicateRegisteredDocumentInfo"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[15] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("findPotentialRegisteredDuplicatesById");
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
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types",
                        ">>findPotentialRegisteredDuplicatesResponse>potentialDuplicateRegisteredDocuments"));
        oper.setReturnClass(
                eu.cec.digit.circabc.repo.hrs.ws
                        .FindPotentialRegisteredDuplicatesResponsePotentialDuplicateRegisteredDocumentsPotentialDuplicateRegisteredDocumentInfo
                        []
                        .class);
        oper.setReturnQName(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "potentialDuplicateRegisteredDocuments"));
        param = oper.getReturnParamDesc();
        param.setItemQName(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "potentialDuplicateRegisteredDocumentInfo"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[16] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("encryptItems");
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
        param =
                new org.apache.axis.description.ParameterDesc(
                        new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "deadline"),
                        org.apache.axis.description.ParameterDesc.IN,
                        new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "date"),
                        java.util.Date.class,
                        false,
                        false);
        param.setOmittable(true);
        oper.addParameter(param);
        oper.setReturnType(
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        oper.setReturnClass(boolean.class);
        oper.setReturnQName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "success"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[17] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("decryptItems");
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
                new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        oper.setReturnClass(boolean.class);
        oper.setReturnQName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "success"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[18] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("findDocumentNotifications");
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
                        new javax.xml.namespace.QName(
                                "http://ec.europa.eu/sg/hrs/types", "documentRetrievalOptions"),
                        org.apache.axis.description.ParameterDesc.IN,
                        new javax.xml.namespace.QName(
                                "http://ec.europa.eu/sg/hrs/types", "DocumentRetrievalOptions"),
                        eu.cec.digit.circabc.repo.hrs.ws.DocumentRetrievalOptions.class,
                        false,
                        false);
        param.setOmittable(true);
        oper.addParameter(param);
        oper.setReturnType(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "DocumentNotificationsResponse"));
        oper.setReturnClass(eu.cec.digit.circabc.repo.hrs.ws.DocumentNotificationsResponse.class);
        oper.setReturnQName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "return"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[19] = oper;
    }

    private static void _initOperationDesc3() {
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("deleteDocumentNotifications");
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
                        new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "notificationId"),
                        org.apache.axis.description.ParameterDesc.IN,
                        new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"),
                        java.lang.String[].class,
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
        _operations[20] = oper;
    }

    private void addBindings0() {
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
                        "http://ec.europa.eu/sg/hrs/types",
                        ">>>>findPotentialRegisteredDuplicatesResponse>potentialDuplicateRegisteredDocuments>potentialDuplicateRegisteredDocumentInfo>registrationAuthor");
        cachedSerQNames.add(qName);
        cls =
                eu.cec.digit.circabc.repo.hrs.ws
                        .FindPotentialRegisteredDuplicatesResponsePotentialDuplicateRegisteredDocumentsPotentialDuplicateRegisteredDocumentInfoRegistrationAuthor
                        .class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName =
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", ">>>Document>filedIn>file>multilingualNames");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.Translation[].class;
        cachedSerClasses.add(cls);
        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "Translation");
        qName2 = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "multilingualName");
        cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
        cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

        qName =
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types",
                        ">>>findPotentialRegisteredDuplicatesResponse>potentialDuplicateRegisteredDocuments>potentialDuplicateRegisteredDocumentInfo");
        cachedSerQNames.add(qName);
        cls =
                eu.cec.digit.circabc.repo.hrs.ws
                        .FindPotentialRegisteredDuplicatesResponsePotentialDuplicateRegisteredDocumentsPotentialDuplicateRegisteredDocumentInfo
                        .class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName =
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", ">>Document>filedIn>file");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.DocumentFiledInFile.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName =
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", ">>Document>recipients>recipient");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.DocumentRecipientsRecipient.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName =
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", ">>Document>senders>sender");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.DocumentSendersSender.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName =
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types",
                        ">>DocumentUpdateRequest>updateItems>updateDetails");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.DocumentUpdateRequestUpdateItemsUpdateDetails.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName =
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", ">>DocumentUpdateRequest>updateRecipients>add");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.DocumentUpdateRequestUpdateRecipientsAdd.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName =
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types",
                        ">>findPotentialRegisteredDuplicatesResponse>potentialDuplicateRegisteredDocuments");
        cachedSerQNames.add(qName);
        cls =
                eu.cec.digit.circabc.repo.hrs.ws
                        .FindPotentialRegisteredDuplicatesResponsePotentialDuplicateRegisteredDocumentsPotentialDuplicateRegisteredDocumentInfo
                        []
                        .class;
        cachedSerClasses.add(cls);
        qName =
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types",
                        ">>>findPotentialRegisteredDuplicatesResponse>potentialDuplicateRegisteredDocuments>potentialDuplicateRegisteredDocumentInfo");
        qName2 =
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "potentialDuplicateRegisteredDocumentInfo");
        cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
        cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

        qName =
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", ">>SortOptions>sortBy>order");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.SortOptionsSortByOrder.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(enumsf);
        cachedDeserFactories.add(enumdf);

        qName =
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", ">AssignmentTask>code");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.AssignmentTaskCode.class;
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

        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", ">Document>filedIn");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.DocumentFiledInFile[].class;
        cachedSerClasses.add(cls);
        qName =
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", ">>Document>filedIn>file");
        qName2 = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "file");
        cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
        cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", ">Document>items");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.Item[].class;
        cachedSerClasses.add(cls);
        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "Item");
        qName2 = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "item");
        cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
        cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", ">Document>links");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.Link[].class;
        cachedSerClasses.add(cls);
        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "Link");
        qName2 = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "link");
        cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
        cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

        qName =
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", ">Document>personConcerned");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.DocumentPersonConcerned.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName =
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", ">Document>recipients");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.DocumentRecipientsRecipient[].class;
        cachedSerClasses.add(cls);
        qName =
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", ">>Document>recipients>recipient");
        qName2 = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "recipient");
        cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
        cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", ">Document>senders");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.DocumentSendersSender[].class;
        cachedSerClasses.add(cls);
        qName =
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", ">>Document>senders>sender");
        qName2 = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "sender");
        cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
        cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", ">Document>signedBy");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.DocumentSignedBy.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName =
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", ">DocumentRegistrationRequest>items");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.UploadedItemToAdd[].class;
        cachedSerClasses.add(cls);
        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "UploadedItemToAdd");
        qName2 = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "uploadedItem");
        cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
        cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

        qName =
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types",
                        ">DocumentSearchByExpressionRequest>additionalPartitions");
        cachedSerQNames.add(qName);
        cls = int[].class;
        cachedSerClasses.add(cls);
        qName = new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int");
        qName2 = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "partitionId");
        cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
        cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

        qName =
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", ">DocumentUpdateRequest>updateDocumentDetails");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.DocumentUpdateRequestUpdateDocumentDetails.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName =
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", ">DocumentUpdateRequest>updateItems");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.DocumentUpdateRequestUpdateItems.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName =
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", ">DocumentUpdateRequest>updateRecipients");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.DocumentUpdateRequestUpdateRecipients.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName =
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", ">DocumentUpdateRequest>updateSenders");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.DocumentUpdateRequestUpdateSenders.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", ">Item>checkedOut");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.ItemCheckedOut.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", ">Item>translations");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.Item[].class;
        cachedSerClasses.add(cls);
        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "Item");
        qName2 = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "translation");
        cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
        cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

        qName =
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", ">ItemCheckinRequest>versioningStrategy");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.ItemCheckinRequestVersioningStrategy.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(enumsf);
        cachedDeserFactories.add(enumdf);

        qName =
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", ">LinkDocumentsRequest>linkType");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.LinkDocumentsRequestLinkType.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(enumsf);
        cachedDeserFactories.add(enumdf);

        qName =
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", ">Marking>personConcerned");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.MarkingPersonConcerned.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName =
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", ">RecipientsToAdd>recipient");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.RecipientsToAddRecipient.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName =
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", ">SendersToAdd>sender");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.SendersToAddSender.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName =
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", ">SignatoryTask>code");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.SignatoryTaskCode.class;
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

        qName =
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", ">SortOptions>sortBy");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.SortOptionsSortBy.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", ">Task>status");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.TaskStatus.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(enumsf);
        cachedDeserFactories.add(enumdf);

        qName =
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", ">Translation>language");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.TranslationLanguage.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(enumsf);
        cachedDeserFactories.add(enumdf);

        qName =
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", ">TranslationsToAdd>translation");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.TranslationsToAddTranslation.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "AssignmentTask");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.AssignmentTask.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "AssignmentWorkflow");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.AssignmentWorkflow.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "AttachmentType");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.AttachmentType.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(enumsf);
        cachedDeserFactories.add(enumdf);

        qName =
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "AttachmentTypeToAdd");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.AttachmentTypeToAdd.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(enumsf);
        cachedDeserFactories.add(enumdf);

        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "CountryCode");
        cachedSerQNames.add(qName);
        cls = java.lang.String.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(
                org.apache.axis.encoding.ser.BaseSerializerFactory.createFactory(
                        org.apache.axis.encoding.ser.SimpleSerializerFactory.class, cls, qName));
        cachedDeserFactories.add(
                org.apache.axis.encoding.ser.BaseDeserializerFactory.createFactory(
                        org.apache.axis.encoding.ser.SimpleDeserializerFactory.class, cls, qName));

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

        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "DecimalPercentage");
        cachedSerQNames.add(qName);
        cls = java.math.BigDecimal.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(
                org.apache.axis.encoding.ser.BaseSerializerFactory.createFactory(
                        org.apache.axis.encoding.ser.SimpleSerializerFactory.class, cls, qName));
        cachedDeserFactories.add(
                org.apache.axis.encoding.ser.BaseDeserializerFactory.createFactory(
                        org.apache.axis.encoding.ser.SimpleDeserializerFactory.class, cls, qName));

        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "Document");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.Document.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName =
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "DocumentCreationRequest");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.DocumentCreationRequest.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName =
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "DocumentCreationSummary");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.DocumentCreationSummary.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName =
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "DocumentNotification");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.DocumentNotification.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName =
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "DocumentNotificationsResponse");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.DocumentNotificationsResponse.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName =
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "DocumentRegistrationRequest");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.DocumentRegistrationRequest.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName =
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "DocumentRetrievalOptions");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.DocumentRetrievalOptions.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName =
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "DocumentSearchByExpressionRequest");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.DocumentSearchByExpressionRequest.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName =
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "DocumentSearchResponse");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.DocumentSearchResponse.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName =
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "DocumentUpdateRequest");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.DocumentUpdateRequest.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "Entity");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.Entity.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "ExternalEntity");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.ExternalEntity.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName =
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "ExternalOrganization");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.ExternalOrganization.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "ExternalPerson");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.ExternalPerson.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "FileStatus");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.FileStatus.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(enumsf);
        cachedDeserFactories.add(enumdf);

        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "Header");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.Header.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "int0To2000");
        cachedSerQNames.add(qName);
        cls = int.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(
                org.apache.axis.encoding.ser.BaseSerializerFactory.createFactory(
                        org.apache.axis.encoding.ser.SimpleSerializerFactory.class, cls, qName));
        cachedDeserFactories.add(
                org.apache.axis.encoding.ser.BaseDeserializerFactory.createFactory(
                        org.apache.axis.encoding.ser.SimpleDeserializerFactory.class, cls, qName));

        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "int0To300");
        cachedSerQNames.add(qName);
        cls = int.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(
                org.apache.axis.encoding.ser.BaseSerializerFactory.createFactory(
                        org.apache.axis.encoding.ser.SimpleSerializerFactory.class, cls, qName));
        cachedDeserFactories.add(
                org.apache.axis.encoding.ser.BaseDeserializerFactory.createFactory(
                        org.apache.axis.encoding.ser.SimpleDeserializerFactory.class, cls, qName));

        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "InternalEntity");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.InternalEntity.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName =
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "InternalOrganization");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.InternalOrganization.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "InternalPerson");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.InternalPerson.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "Item");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.Item.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "ItemCheckinRequest");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.ItemCheckinRequest.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "ItemKind");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.ItemKind.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(enumsf);
        cachedDeserFactories.add(enumdf);

        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "ItemKindToAdd");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.ItemKindToAdd.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(enumsf);
        cachedDeserFactories.add(enumdf);

        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "ItemsToAdd");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.ItemsToAdd.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "LanguageCode");
        cachedSerQNames.add(qName);
        cls = java.lang.String.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(
                org.apache.axis.encoding.ser.BaseSerializerFactory.createFactory(
                        org.apache.axis.encoding.ser.SimpleSerializerFactory.class, cls, qName));
        cachedDeserFactories.add(
                org.apache.axis.encoding.ser.BaseDeserializerFactory.createFactory(
                        org.apache.axis.encoding.ser.SimpleDeserializerFactory.class, cls, qName));

        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "Link");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.Link.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName =
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "LinkDocumentsRequest");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.LinkDocumentsRequest.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "LinkType");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.LinkType.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(enumsf);
        cachedDeserFactories.add(enumdf);

        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "MailType");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.MailType.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(enumsf);
        cachedDeserFactories.add(enumdf);

        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "MarkerType");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.MarkerType.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(enumsf);
        cachedDeserFactories.add(enumdf);

        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "Marking");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.Marking.class;
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

        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "OperationResponse");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.OperationResponse.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "Procedure");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.Procedure.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "RecipientCode");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.RecipientCode.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(enumsf);
        cachedDeserFactories.add(enumdf);

        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "RecipientsToAdd");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.RecipientsToAddRecipient[].class;
        cachedSerClasses.add(cls);
        qName =
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", ">RecipientsToAdd>recipient");
        qName2 = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "recipient");
        cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
        cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

        qName =
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "RegistrationByIdSummary");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.RegistrationByIdSummary.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName =
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "RegistrationSummary");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.RegistrationSummary.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "RenditionStatus");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.RenditionStatus.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(enumsf);
        cachedDeserFactories.add(enumdf);

        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "ScannedItemToAdd");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.ScannedItemToAdd.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName =
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "SecurityClassification");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.SecurityClassification.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(enumsf);
        cachedDeserFactories.add(enumdf);

        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "SendersToAdd");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.SendersToAddSender[].class;
        cachedSerClasses.add(cls);
        qName =
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", ">SendersToAdd>sender");
        qName2 = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "sender");
        cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
        cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "SignatoryTask");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.SignatoryTask.class;
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

        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "SortOptions");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.SortOptionsSortBy[].class;
        cachedSerClasses.add(cls);
        qName =
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", ">SortOptions>sortBy");
        qName2 = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "sortBy");
        cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
        cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "Task");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.Task.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "Translation");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.Translation.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "TranslationsToAdd");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.TranslationsToAddTranslation[].class;
        cachedSerClasses.add(cls);
        qName =
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", ">TranslationsToAdd>translation");
        qName2 = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "translation");
        cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
        cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

        qName =
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "UnlinkDocumentsRequest");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.UnlinkDocumentsRequest.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "UploadedItemToAdd");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.UploadedItemToAdd.class;
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

    public eu.cec.digit.circabc.repo.hrs.ws.OperationResponse fileDocument(
            eu.cec.digit.circabc.repo.hrs.ws.Header header,
            java.lang.String documentId,
            java.lang.String fileId)
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
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "fileDocument"));

        setRequestHeaders(_call);
        setAttachments(_call);
        try {
            java.lang.Object _resp = _call.invoke(new java.lang.Object[]{header, documentId, fileId});

            if (_resp instanceof java.rmi.RemoteException) {
                throw (java.rmi.RemoteException) _resp;
            } else {
                extractAttachments(_call);
                try {
                    return (eu.cec.digit.circabc.repo.hrs.ws.OperationResponse) _resp;
                } catch (java.lang.Exception _exception) {
                    return (eu.cec.digit.circabc.repo.hrs.ws.OperationResponse)
                            org.apache.axis.utils.JavaUtils.convert(
                                    _resp, eu.cec.digit.circabc.repo.hrs.ws.OperationResponse.class);
                }
            }
        } catch (org.apache.axis.AxisFault axisFaultException) {
            throw axisFaultException;
        }
    }

    public eu.cec.digit.circabc.repo.hrs.ws.OperationResponse unfileDocument(
            eu.cec.digit.circabc.repo.hrs.ws.Header header,
            java.lang.String documentId,
            java.lang.String fileId)
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
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "unfileDocument"));

        setRequestHeaders(_call);
        setAttachments(_call);
        try {
            java.lang.Object _resp = _call.invoke(new java.lang.Object[]{header, documentId, fileId});

            if (_resp instanceof java.rmi.RemoteException) {
                throw (java.rmi.RemoteException) _resp;
            } else {
                extractAttachments(_call);
                try {
                    return (eu.cec.digit.circabc.repo.hrs.ws.OperationResponse) _resp;
                } catch (java.lang.Exception _exception) {
                    return (eu.cec.digit.circabc.repo.hrs.ws.OperationResponse)
                            org.apache.axis.utils.JavaUtils.convert(
                                    _resp, eu.cec.digit.circabc.repo.hrs.ws.OperationResponse.class);
                }
            }
        } catch (org.apache.axis.AxisFault axisFaultException) {
            throw axisFaultException;
        }
    }

    public eu.cec.digit.circabc.repo.hrs.ws.DocumentCreationSummary createDocument(
            eu.cec.digit.circabc.repo.hrs.ws.Header header,
            eu.cec.digit.circabc.repo.hrs.ws.DocumentCreationRequest request)
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
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "createDocument"));

        setRequestHeaders(_call);
        setAttachments(_call);
        try {
            java.lang.Object _resp = _call.invoke(new java.lang.Object[]{header, request});

            if (_resp instanceof java.rmi.RemoteException) {
                throw (java.rmi.RemoteException) _resp;
            } else {
                extractAttachments(_call);
                try {
                    return (eu.cec.digit.circabc.repo.hrs.ws.DocumentCreationSummary) _resp;
                } catch (java.lang.Exception _exception) {
                    return (eu.cec.digit.circabc.repo.hrs.ws.DocumentCreationSummary)
                            org.apache.axis.utils.JavaUtils.convert(
                                    _resp, eu.cec.digit.circabc.repo.hrs.ws.DocumentCreationSummary.class);
                }
            }
        } catch (org.apache.axis.AxisFault axisFaultException) {
            throw axisFaultException;
        }
    }

    public boolean updateDocument(
            eu.cec.digit.circabc.repo.hrs.ws.Header header,
            eu.cec.digit.circabc.repo.hrs.ws.DocumentUpdateRequest request)
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
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "updateDocument"));

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

    public boolean trashDocument(
            eu.cec.digit.circabc.repo.hrs.ws.Header header, java.lang.String documentId)
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
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "trashDocument"));

        setRequestHeaders(_call);
        setAttachments(_call);
        try {
            java.lang.Object _resp = _call.invoke(new java.lang.Object[]{header, documentId});

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

    public eu.cec.digit.circabc.repo.hrs.ws.RegistrationByIdSummary registerDocumentById(
            eu.cec.digit.circabc.repo.hrs.ws.Header header, java.lang.String documentId)
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
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "registerDocumentById"));

        setRequestHeaders(_call);
        setAttachments(_call);
        try {
            java.lang.Object _resp = _call.invoke(new java.lang.Object[]{header, documentId});

            if (_resp instanceof java.rmi.RemoteException) {
                throw (java.rmi.RemoteException) _resp;
            } else {
                extractAttachments(_call);
                try {
                    return (eu.cec.digit.circabc.repo.hrs.ws.RegistrationByIdSummary) _resp;
                } catch (java.lang.Exception _exception) {
                    return (eu.cec.digit.circabc.repo.hrs.ws.RegistrationByIdSummary)
                            org.apache.axis.utils.JavaUtils.convert(
                                    _resp, eu.cec.digit.circabc.repo.hrs.ws.RegistrationByIdSummary.class);
                }
            }
        } catch (org.apache.axis.AxisFault axisFaultException) {
            throw axisFaultException;
        }
    }

    public eu.cec.digit.circabc.repo.hrs.ws.RegistrationSummary registerDocument(
            eu.cec.digit.circabc.repo.hrs.ws.Header header,
            eu.cec.digit.circabc.repo.hrs.ws.DocumentRegistrationRequest request)
            throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[6]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "registerDocument"));

        setRequestHeaders(_call);
        setAttachments(_call);
        try {
            java.lang.Object _resp = _call.invoke(new java.lang.Object[]{header, request});

            if (_resp instanceof java.rmi.RemoteException) {
                throw (java.rmi.RemoteException) _resp;
            } else {
                extractAttachments(_call);
                try {
                    return (eu.cec.digit.circabc.repo.hrs.ws.RegistrationSummary) _resp;
                } catch (java.lang.Exception _exception) {
                    return (eu.cec.digit.circabc.repo.hrs.ws.RegistrationSummary)
                            org.apache.axis.utils.JavaUtils.convert(
                                    _resp, eu.cec.digit.circabc.repo.hrs.ws.RegistrationSummary.class);
                }
            }
        } catch (org.apache.axis.AxisFault axisFaultException) {
            throw axisFaultException;
        }
    }

    public boolean checkoutItem(
            eu.cec.digit.circabc.repo.hrs.ws.Header header, java.lang.String itemId)
            throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[7]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "checkoutItem"));

        setRequestHeaders(_call);
        setAttachments(_call);
        try {
            java.lang.Object _resp = _call.invoke(new java.lang.Object[]{header, itemId});

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

    public boolean cancelItemCheckout(
            eu.cec.digit.circabc.repo.hrs.ws.Header header, java.lang.String itemId)
            throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[8]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "cancelItemCheckout"));

        setRequestHeaders(_call);
        setAttachments(_call);
        try {
            java.lang.Object _resp = _call.invoke(new java.lang.Object[]{header, itemId});

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

    public java.lang.String checkinItem(
            eu.cec.digit.circabc.repo.hrs.ws.Header header,
            eu.cec.digit.circabc.repo.hrs.ws.ItemCheckinRequest request)
            throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[9]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "checkinItem"));

        setRequestHeaders(_call);
        setAttachments(_call);
        try {
            java.lang.Object _resp = _call.invoke(new java.lang.Object[]{header, request});

            if (_resp instanceof java.rmi.RemoteException) {
                throw (java.rmi.RemoteException) _resp;
            } else {
                extractAttachments(_call);
                try {
                    return (java.lang.String) _resp;
                } catch (java.lang.Exception _exception) {
                    return (java.lang.String)
                            org.apache.axis.utils.JavaUtils.convert(_resp, java.lang.String.class);
                }
            }
        } catch (org.apache.axis.AxisFault axisFaultException) {
            throw axisFaultException;
        }
    }

    public boolean addTranslations(
            eu.cec.digit.circabc.repo.hrs.ws.Header header,
            java.lang.String itemId,
            eu.cec.digit.circabc.repo.hrs.ws.TranslationsToAddTranslation[] translations)
            throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[10]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "addTranslations"));

        setRequestHeaders(_call);
        setAttachments(_call);
        try {
            java.lang.Object _resp = _call.invoke(new java.lang.Object[]{header, itemId, translations});

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

    public eu.cec.digit.circabc.repo.hrs.ws.Document getDocument(
            eu.cec.digit.circabc.repo.hrs.ws.Header header, java.lang.String documentId)
            throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[11]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "getDocument"));

        setRequestHeaders(_call);
        setAttachments(_call);
        try {
            java.lang.Object _resp = _call.invoke(new java.lang.Object[]{header, documentId});

            if (_resp instanceof java.rmi.RemoteException) {
                throw (java.rmi.RemoteException) _resp;
            } else {
                extractAttachments(_call);
                try {
                    return (eu.cec.digit.circabc.repo.hrs.ws.Document) _resp;
                } catch (java.lang.Exception _exception) {
                    return (eu.cec.digit.circabc.repo.hrs.ws.Document)
                            org.apache.axis.utils.JavaUtils.convert(
                                    _resp, eu.cec.digit.circabc.repo.hrs.ws.Document.class);
                }
            }
        } catch (org.apache.axis.AxisFault axisFaultException) {
            throw axisFaultException;
        }
    }

    public eu.cec.digit.circabc.repo.hrs.ws.DocumentSearchResponse searchDocumentsByExpression(
            eu.cec.digit.circabc.repo.hrs.ws.Header header,
            eu.cec.digit.circabc.repo.hrs.ws.DocumentSearchByExpressionRequest request,
            eu.cec.digit.circabc.repo.hrs.ws.DocumentRetrievalOptions documentRetrievalOptions,
            eu.cec.digit.circabc.repo.hrs.ws.SortOptionsSortBy[] sortOptions)
            throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[12]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "searchDocumentsByExpression"));

        setRequestHeaders(_call);
        setAttachments(_call);
        try {
            java.lang.Object _resp =
                    _call.invoke(
                            new java.lang.Object[]{header, request, documentRetrievalOptions, sortOptions});

            if (_resp instanceof java.rmi.RemoteException) {
                throw (java.rmi.RemoteException) _resp;
            } else {
                extractAttachments(_call);
                try {
                    return (eu.cec.digit.circabc.repo.hrs.ws.DocumentSearchResponse) _resp;
                } catch (java.lang.Exception _exception) {
                    return (eu.cec.digit.circabc.repo.hrs.ws.DocumentSearchResponse)
                            org.apache.axis.utils.JavaUtils.convert(
                                    _resp, eu.cec.digit.circabc.repo.hrs.ws.DocumentSearchResponse.class);
                }
            }
        } catch (org.apache.axis.AxisFault axisFaultException) {
            throw axisFaultException;
        }
    }

    public boolean linkDocuments(
            eu.cec.digit.circabc.repo.hrs.ws.Header header,
            eu.cec.digit.circabc.repo.hrs.ws.LinkDocumentsRequest request)
            throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[13]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "linkDocuments"));

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

    public boolean unlinkDocuments(
            eu.cec.digit.circabc.repo.hrs.ws.Header header,
            eu.cec.digit.circabc.repo.hrs.ws.UnlinkDocumentsRequest request)
            throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[14]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "unlinkDocuments"));

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

    public eu.cec.digit.circabc.repo.hrs.ws
            .FindPotentialRegisteredDuplicatesResponsePotentialDuplicateRegisteredDocumentsPotentialDuplicateRegisteredDocumentInfo
            [] findPotentialRegisteredDuplicates(
            eu.cec.digit.circabc.repo.hrs.ws.Header header,
            eu.cec.digit.circabc.repo.hrs.ws.DocumentRegistrationRequest request)
            throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[15]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "findPotentialRegisteredDuplicates"));

        setRequestHeaders(_call);
        setAttachments(_call);
        try {
            java.lang.Object _resp = _call.invoke(new java.lang.Object[]{header, request});

            if (_resp instanceof java.rmi.RemoteException) {
                throw (java.rmi.RemoteException) _resp;
            } else {
                extractAttachments(_call);
                try {
                    return (eu.cec.digit.circabc.repo.hrs.ws
                            .FindPotentialRegisteredDuplicatesResponsePotentialDuplicateRegisteredDocumentsPotentialDuplicateRegisteredDocumentInfo
                            [])
                            _resp;
                } catch (java.lang.Exception _exception) {
                    return (eu.cec.digit.circabc.repo.hrs.ws
                            .FindPotentialRegisteredDuplicatesResponsePotentialDuplicateRegisteredDocumentsPotentialDuplicateRegisteredDocumentInfo
                            [])
                            org.apache.axis.utils.JavaUtils.convert(
                                    _resp,
                                    eu.cec.digit.circabc.repo.hrs.ws
                                            .FindPotentialRegisteredDuplicatesResponsePotentialDuplicateRegisteredDocumentsPotentialDuplicateRegisteredDocumentInfo
                                            []
                                            .class);
                }
            }
        } catch (org.apache.axis.AxisFault axisFaultException) {
            throw axisFaultException;
        }
    }

    public eu.cec.digit.circabc.repo.hrs.ws
            .FindPotentialRegisteredDuplicatesResponsePotentialDuplicateRegisteredDocumentsPotentialDuplicateRegisteredDocumentInfo
            [] findPotentialRegisteredDuplicatesById(
            eu.cec.digit.circabc.repo.hrs.ws.Header header, java.lang.String documentId)
            throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[16]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "findPotentialRegisteredDuplicatesById"));

        setRequestHeaders(_call);
        setAttachments(_call);
        try {
            java.lang.Object _resp = _call.invoke(new java.lang.Object[]{header, documentId});

            if (_resp instanceof java.rmi.RemoteException) {
                throw (java.rmi.RemoteException) _resp;
            } else {
                extractAttachments(_call);
                try {
                    return (eu.cec.digit.circabc.repo.hrs.ws
                            .FindPotentialRegisteredDuplicatesResponsePotentialDuplicateRegisteredDocumentsPotentialDuplicateRegisteredDocumentInfo
                            [])
                            _resp;
                } catch (java.lang.Exception _exception) {
                    return (eu.cec.digit.circabc.repo.hrs.ws
                            .FindPotentialRegisteredDuplicatesResponsePotentialDuplicateRegisteredDocumentsPotentialDuplicateRegisteredDocumentInfo
                            [])
                            org.apache.axis.utils.JavaUtils.convert(
                                    _resp,
                                    eu.cec.digit.circabc.repo.hrs.ws
                                            .FindPotentialRegisteredDuplicatesResponsePotentialDuplicateRegisteredDocumentsPotentialDuplicateRegisteredDocumentInfo
                                            []
                                            .class);
                }
            }
        } catch (org.apache.axis.AxisFault axisFaultException) {
            throw axisFaultException;
        }
    }

    public boolean encryptItems(
            eu.cec.digit.circabc.repo.hrs.ws.Header header,
            java.lang.String documentId,
            java.util.Date deadline)
            throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[17]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "encryptItems"));

        setRequestHeaders(_call);
        setAttachments(_call);
        try {
            java.lang.Object _resp = _call.invoke(new java.lang.Object[]{header, documentId, deadline});

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

    public boolean decryptItems(
            eu.cec.digit.circabc.repo.hrs.ws.Header header, java.lang.String documentId)
            throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[18]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "decryptItems"));

        setRequestHeaders(_call);
        setAttachments(_call);
        try {
            java.lang.Object _resp = _call.invoke(new java.lang.Object[]{header, documentId});

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

    public eu.cec.digit.circabc.repo.hrs.ws.DocumentNotificationsResponse findDocumentNotifications(
            eu.cec.digit.circabc.repo.hrs.ws.Header header,
            eu.cec.digit.circabc.repo.hrs.ws.DocumentRetrievalOptions documentRetrievalOptions)
            throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[19]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "findDocumentNotifications"));

        setRequestHeaders(_call);
        setAttachments(_call);
        try {
            java.lang.Object _resp =
                    _call.invoke(new java.lang.Object[]{header, documentRetrievalOptions});

            if (_resp instanceof java.rmi.RemoteException) {
                throw (java.rmi.RemoteException) _resp;
            } else {
                extractAttachments(_call);
                try {
                    return (eu.cec.digit.circabc.repo.hrs.ws.DocumentNotificationsResponse) _resp;
                } catch (java.lang.Exception _exception) {
                    return (eu.cec.digit.circabc.repo.hrs.ws.DocumentNotificationsResponse)
                            org.apache.axis.utils.JavaUtils.convert(
                                    _resp, eu.cec.digit.circabc.repo.hrs.ws.DocumentNotificationsResponse.class);
                }
            }
        } catch (org.apache.axis.AxisFault axisFaultException) {
            throw axisFaultException;
        }
    }

    public boolean deleteDocumentNotifications(
            eu.cec.digit.circabc.repo.hrs.ws.Header header, java.lang.String[] notificationId)
            throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[20]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "deleteDocumentNotifications"));

        setRequestHeaders(_call);
        setAttachments(_call);
        try {
            java.lang.Object _resp = _call.invoke(new java.lang.Object[]{header, notificationId});

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
