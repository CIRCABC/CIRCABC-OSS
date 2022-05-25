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
 * <p>EntityWebServiceLocator.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 * <p>
 * EntityWebServiceLocator.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
/**
 * EntityWebServiceLocator.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package eu.cec.digit.circabc.repo.hrs.ws;

public class EntityWebServiceLocator extends org.apache.axis.client.Service
        implements eu.cec.digit.circabc.repo.hrs.ws.EntityWebService {

    // Use to get a proxy class for EntityService
    private java.lang.String EntityService_address =
            "http://dighbust.cc.cec.eu.int:11031/hermes/Proxy/1.16/EntityWebServicePS";
    // The WSDD service name defaults to the port name.
    private java.lang.String EntityServiceWSDDServiceName = "EntityService";
    private java.util.HashSet ports = null;

    /**
     * This web service exposes operations related to entities that are stored in the common
     * repository.
     */
    public EntityWebServiceLocator() {
    }

    public EntityWebServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public EntityWebServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName)
            throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    public java.lang.String getEntityServiceAddress() {
        return EntityService_address;
    }

    public java.lang.String getEntityServiceWSDDServiceName() {
        return EntityServiceWSDDServiceName;
    }

    public void setEntityServiceWSDDServiceName(java.lang.String name) {
        EntityServiceWSDDServiceName = name;
    }

    public eu.cec.digit.circabc.repo.hrs.ws.EntityService_PortType getEntityService()
            throws javax.xml.rpc.ServiceException {
        java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(EntityService_address);
        } catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getEntityService(endpoint);
    }

    public eu.cec.digit.circabc.repo.hrs.ws.EntityService_PortType getEntityService(
            java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            eu.cec.digit.circabc.repo.hrs.ws.EntityServiceBindingStub _stub =
                    new eu.cec.digit.circabc.repo.hrs.ws.EntityServiceBindingStub(portAddress, this);
            _stub.setPortName(getEntityServiceWSDDServiceName());
            return _stub;
        } catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setEntityServiceEndpointAddress(java.lang.String address) {
        EntityService_address = address;
    }

    /**
     * For the given interface, get the stub implementation. If this service has no port for the given
     * interface, then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface)
            throws javax.xml.rpc.ServiceException {
        try {
            if (eu.cec.digit.circabc.repo.hrs.ws.EntityService_PortType.class.isAssignableFrom(
                    serviceEndpointInterface)) {
                eu.cec.digit.circabc.repo.hrs.ws.EntityServiceBindingStub _stub =
                        new eu.cec.digit.circabc.repo.hrs.ws.EntityServiceBindingStub(
                                new java.net.URL(EntityService_address), this);
                _stub.setPortName(getEntityServiceWSDDServiceName());
                return _stub;
            }
        } catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException(
                "There is no stub implementation for the interface:  "
                        + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation. If this service has no port for the given
     * interface, then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface)
            throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("EntityService".equals(inputPortName)) {
            return getEntityService();
        } else {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs", "EntityWebService");
    }

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs", "EntityService"));
        }
        return ports.iterator();
    }

    /** Set the endpoint address for the specified port name. */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address)
            throws javax.xml.rpc.ServiceException {

        if ("EntityService".equals(portName)) {
            setEntityServiceEndpointAddress(address);
        } else { // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(
                    " Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /** Set the endpoint address for the specified port name. */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address)
            throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }
}
