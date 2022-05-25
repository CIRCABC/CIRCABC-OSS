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
 * <p>EntityServiceBindingStub.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 * <p>
 * EntityServiceBindingStub.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
/**
 * EntityServiceBindingStub.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package eu.cec.digit.circabc.repo.hrs.ws;

public class EntityServiceBindingStub extends org.apache.axis.client.Stub
        implements eu.cec.digit.circabc.repo.hrs.ws.EntityService_PortType {

    static org.apache.axis.description.OperationDesc[] _operations;

    static {
        _operations = new org.apache.axis.description.OperationDesc[4];
        _initOperationDesc1();
    }

    private java.util.Vector cachedSerClasses = new java.util.Vector();
    private java.util.Vector cachedSerQNames = new java.util.Vector();
    private java.util.Vector cachedSerFactories = new java.util.Vector();
    private java.util.Vector cachedDeserFactories = new java.util.Vector();

    public EntityServiceBindingStub() throws org.apache.axis.AxisFault {
        this(null);
    }

    public EntityServiceBindingStub(java.net.URL endpointURL, javax.xml.rpc.Service service)
            throws org.apache.axis.AxisFault {
        this(service);
        super.cachedEndpoint = endpointURL;
    }

    public EntityServiceBindingStub(javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
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
                        "http://ec.europa.eu/sg/hrs/types", ">>SortOptions>sortBy>order");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.SortOptionsSortByOrder.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(enumsf);
        cachedDeserFactories.add(enumdf);

        qName =
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types",
                        ">ExternalPersonCreationRequest>linkedToOrganization");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.ExternalPersonCreationRequestLinkedToOrganization.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName =
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", ">FindCurrentExternalEntityRequest>searchById");
        cachedSerQNames.add(qName);
        cls = java.lang.String[].class;
        cachedSerClasses.add(cls);
        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "CurrentEntityId");
        qName2 = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "currentEntityId");
        cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
        cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

        qName =
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types",
                        ">FindCurrentExternalEntityRequest>searchForOrganization");
        cachedSerQNames.add(qName);
        cls =
                eu.cec.digit.circabc.repo.hrs.ws.FindCurrentExternalEntityRequestSearchForOrganization
                        .class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName =
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types",
                        ">FindCurrentExternalEntityRequest>searchForPerson");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.FindCurrentExternalEntityRequestSearchForPerson.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName =
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", ">FindCurrentInternalEntityRequest>searchById");
        cachedSerQNames.add(qName);
        cls = java.lang.String[].class;
        cachedSerClasses.add(cls);
        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "CurrentEntityId");
        qName2 = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "currentEntityId");
        cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
        cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

        qName =
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types",
                        ">FindCurrentInternalEntityRequest>searchForOrganization");
        cachedSerQNames.add(qName);
        cls =
                eu.cec.digit.circabc.repo.hrs.ws.FindCurrentInternalEntityRequestSearchForOrganization
                        .class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName =
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types",
                        ">FindCurrentInternalEntityRequest>searchForPerson");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.FindCurrentInternalEntityRequestSearchForPerson.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName =
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", ">SortOptions>sortBy");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.SortOptionsSortBy.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

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

        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "CurrentEntity");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.CurrentEntity.class;
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

        qName =
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "CurrentExternalEntity");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.CurrentExternalEntity.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName =
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "CurrentExternalEntityCreationRequest");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.CurrentExternalEntityCreationRequest.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName =
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "CurrentExternalEntityRetrievalOptions");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.CurrentExternalEntityRetrievalOptions.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName =
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "CurrentExternalOrganization");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.CurrentExternalOrganization.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName =
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "CurrentExternalPerson");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.CurrentExternalPerson.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName =
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "CurrentInternalEntity");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.CurrentInternalEntity.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName =
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "CurrentInternalOrganization");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.CurrentInternalOrganization.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName =
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "CurrentInternalPerson");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.CurrentInternalPerson.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName =
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "EntitySearchByExpressionRequest");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.EntitySearchByExpressionRequest.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName =
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "ExternalOrganizationCreationRequest");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.ExternalOrganizationCreationRequest.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName =
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "ExternalPersonCreationRequest");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.ExternalPersonCreationRequest.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName =
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "FindCurrentExternalEntityRequest");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.FindCurrentExternalEntityRequest.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName =
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "FindCurrentInternalEntityRequest");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.FindCurrentInternalEntityRequest.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "Header");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.Header.class;
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

        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "SortOptions");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.SortOptionsSortBy[].class;
        cachedSerClasses.add(cls);
        qName =
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", ">SortOptions>sortBy");
        qName2 = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "sortBy");
        cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
        cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

        qName = new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "ValidationLevel");
        cachedSerQNames.add(qName);
        cls = eu.cec.digit.circabc.repo.hrs.ws.ValidationLevel.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(enumsf);
        cachedDeserFactories.add(enumdf);
    }

    private static void _initOperationDesc1() {
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("findCurrentInternalEntity");
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
                        new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "criteria"),
                        org.apache.axis.description.ParameterDesc.IN,
                        new javax.xml.namespace.QName(
                                "http://ec.europa.eu/sg/hrs/types", "FindCurrentInternalEntityRequest"),
                        eu.cec.digit.circabc.repo.hrs.ws.FindCurrentInternalEntityRequest.class,
                        false,
                        false);
        param.setOmittable(true);
        oper.addParameter(param);
        param =
                new org.apache.axis.description.ParameterDesc(
                        new javax.xml.namespace.QName(
                                "http://ec.europa.eu/sg/hrs/types", "searchByExpressionRequest"),
                        org.apache.axis.description.ParameterDesc.IN,
                        new javax.xml.namespace.QName(
                                "http://ec.europa.eu/sg/hrs/types", "EntitySearchByExpressionRequest"),
                        eu.cec.digit.circabc.repo.hrs.ws.EntitySearchByExpressionRequest.class,
                        false,
                        false);
        param.setOmittable(true);
        oper.addParameter(param);
        oper.setReturnType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "CurrentInternalEntity"));
        oper.setReturnClass(eu.cec.digit.circabc.repo.hrs.ws.CurrentInternalEntity[].class);
        oper.setReturnQName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "currentInternalEntity"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[0] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("findCurrentExternalEntity");
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
                        new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "criteria"),
                        org.apache.axis.description.ParameterDesc.IN,
                        new javax.xml.namespace.QName(
                                "http://ec.europa.eu/sg/hrs/types", "FindCurrentExternalEntityRequest"),
                        eu.cec.digit.circabc.repo.hrs.ws.FindCurrentExternalEntityRequest.class,
                        false,
                        false);
        param.setOmittable(true);
        oper.addParameter(param);
        param =
                new org.apache.axis.description.ParameterDesc(
                        new javax.xml.namespace.QName(
                                "http://ec.europa.eu/sg/hrs/types", "searchByExpressionRequest"),
                        org.apache.axis.description.ParameterDesc.IN,
                        new javax.xml.namespace.QName(
                                "http://ec.europa.eu/sg/hrs/types", "EntitySearchByExpressionRequest"),
                        eu.cec.digit.circabc.repo.hrs.ws.EntitySearchByExpressionRequest.class,
                        false,
                        false);
        param.setOmittable(true);
        oper.addParameter(param);
        param =
                new org.apache.axis.description.ParameterDesc(
                        new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "retrievalOptions"),
                        org.apache.axis.description.ParameterDesc.IN,
                        new javax.xml.namespace.QName(
                                "http://ec.europa.eu/sg/hrs/types", "CurrentExternalEntityRetrievalOptions"),
                        eu.cec.digit.circabc.repo.hrs.ws.CurrentExternalEntityRetrievalOptions.class,
                        false,
                        false);
        param.setOmittable(true);
        oper.addParameter(param);
        oper.setReturnType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "CurrentExternalEntity"));
        oper.setReturnClass(eu.cec.digit.circabc.repo.hrs.ws.CurrentExternalEntity[].class);
        oper.setReturnQName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "currentExternalEntity"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[1] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("findAllVirtualEntities");
        param =
                new org.apache.axis.description.ParameterDesc(
                        new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "header"),
                        org.apache.axis.description.ParameterDesc.IN,
                        new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "Header"),
                        eu.cec.digit.circabc.repo.hrs.ws.Header.class,
                        false,
                        false);
        oper.addParameter(param);
        oper.setReturnType(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "CurrentInternalEntity"));
        oper.setReturnClass(eu.cec.digit.circabc.repo.hrs.ws.CurrentInternalEntity[].class);
        oper.setReturnQName(
                new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs/types", "currentInternalEntity"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[3] = oper;
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

    public eu.cec.digit.circabc.repo.hrs.ws.CurrentInternalEntity[] findCurrentInternalEntity(
            eu.cec.digit.circabc.repo.hrs.ws.Header header,
            eu.cec.digit.circabc.repo.hrs.ws.FindCurrentInternalEntityRequest criteria,
            eu.cec.digit.circabc.repo.hrs.ws.EntitySearchByExpressionRequest searchByExpressionRequest)
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
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "findCurrentInternalEntity"));

        setRequestHeaders(_call);
        setAttachments(_call);
        try {
            java.lang.Object _resp =
                    _call.invoke(new java.lang.Object[]{header, criteria, searchByExpressionRequest});

            if (_resp instanceof java.rmi.RemoteException) {
                throw (java.rmi.RemoteException) _resp;
            } else {
                extractAttachments(_call);
                try {
                    return (eu.cec.digit.circabc.repo.hrs.ws.CurrentInternalEntity[]) _resp;
                } catch (java.lang.Exception _exception) {
                    return (eu.cec.digit.circabc.repo.hrs.ws.CurrentInternalEntity[])
                            org.apache.axis.utils.JavaUtils.convert(
                                    _resp, eu.cec.digit.circabc.repo.hrs.ws.CurrentInternalEntity[].class);
                }
            }
        } catch (org.apache.axis.AxisFault axisFaultException) {
            throw axisFaultException;
        }
    }

    public eu.cec.digit.circabc.repo.hrs.ws.CurrentExternalEntity[] findCurrentExternalEntity(
            eu.cec.digit.circabc.repo.hrs.ws.Header header,
            eu.cec.digit.circabc.repo.hrs.ws.FindCurrentExternalEntityRequest criteria,
            eu.cec.digit.circabc.repo.hrs.ws.EntitySearchByExpressionRequest searchByExpressionRequest,
            eu.cec.digit.circabc.repo.hrs.ws.CurrentExternalEntityRetrievalOptions retrievalOptions)
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
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "findCurrentExternalEntity"));

        setRequestHeaders(_call);
        setAttachments(_call);
        try {
            java.lang.Object _resp =
                    _call.invoke(
                            new java.lang.Object[]{
                                    header, criteria, searchByExpressionRequest, retrievalOptions
                            });

            if (_resp instanceof java.rmi.RemoteException) {
                throw (java.rmi.RemoteException) _resp;
            } else {
                extractAttachments(_call);
                try {
                    return (eu.cec.digit.circabc.repo.hrs.ws.CurrentExternalEntity[]) _resp;
                } catch (java.lang.Exception _exception) {
                    return (eu.cec.digit.circabc.repo.hrs.ws.CurrentExternalEntity[])
                            org.apache.axis.utils.JavaUtils.convert(
                                    _resp, eu.cec.digit.circabc.repo.hrs.ws.CurrentExternalEntity[].class);
                }
            }
        } catch (org.apache.axis.AxisFault axisFaultException) {
            throw axisFaultException;
        }
    }

    public eu.cec.digit.circabc.repo.hrs.ws.CurrentInternalEntity[] findAllVirtualEntities(
            eu.cec.digit.circabc.repo.hrs.ws.Header header) throws java.rmi.RemoteException {
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
                new javax.xml.namespace.QName(
                        "http://ec.europa.eu/sg/hrs/types", "findAllVirtualEntities"));

        setRequestHeaders(_call);
        setAttachments(_call);
        try {
            java.lang.Object _resp = _call.invoke(new java.lang.Object[]{header});

            if (_resp instanceof java.rmi.RemoteException) {
                throw (java.rmi.RemoteException) _resp;
            } else {
                extractAttachments(_call);
                try {
                    return (eu.cec.digit.circabc.repo.hrs.ws.CurrentInternalEntity[]) _resp;
                } catch (java.lang.Exception _exception) {
                    return (eu.cec.digit.circabc.repo.hrs.ws.CurrentInternalEntity[])
                            org.apache.axis.utils.JavaUtils.convert(
                                    _resp, eu.cec.digit.circabc.repo.hrs.ws.CurrentInternalEntity[].class);
                }
            }
        } catch (org.apache.axis.AxisFault axisFaultException) {
            throw axisFaultException;
        }
    }
}
