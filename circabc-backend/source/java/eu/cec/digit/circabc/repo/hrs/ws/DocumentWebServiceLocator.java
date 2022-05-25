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
 * <p>DocumentWebServiceLocator.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 * <p>
 * DocumentWebServiceLocator.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
/**
 * DocumentWebServiceLocator.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT)
 * WSDL2Java emitter.
 */
package eu.cec.digit.circabc.repo.hrs.ws;

public class DocumentWebServiceLocator extends org.apache.axis.client.Service
        implements eu.cec.digit.circabc.repo.hrs.ws.DocumentWebService {

    // Use to get a proxy class for DocumentService
    private java.lang.String DocumentService_address =
            "http://dighbust.cc.cec.eu.int:11031/hermes/Proxy/1.16/DocumentWebServicePS";
    // The WSDD service name defaults to the port name.
    private java.lang.String DocumentServiceWSDDServiceName = "DocumentService";
    private java.util.HashSet ports = null;

    /**
     * The Document web service exposes basic operations on documents: create, update, register,
     * file/unfile and search
     */
    public DocumentWebServiceLocator() {
    }

    public DocumentWebServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public DocumentWebServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName)
            throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    public java.lang.String getDocumentServiceAddress() {
        return DocumentService_address;
    }

    public java.lang.String getDocumentServiceWSDDServiceName() {
        return DocumentServiceWSDDServiceName;
    }

    public void setDocumentServiceWSDDServiceName(java.lang.String name) {
        DocumentServiceWSDDServiceName = name;
    }

    public eu.cec.digit.circabc.repo.hrs.ws.DocumentService_PortType getDocumentService()
            throws javax.xml.rpc.ServiceException {
        java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(DocumentService_address);
        } catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getDocumentService(endpoint);
    }

    public eu.cec.digit.circabc.repo.hrs.ws.DocumentService_PortType getDocumentService(
            java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            eu.cec.digit.circabc.repo.hrs.ws.DocumentServiceBindingStub _stub =
                    new eu.cec.digit.circabc.repo.hrs.ws.DocumentServiceBindingStub(portAddress, this);
            _stub.setPortName(getDocumentServiceWSDDServiceName());
            return _stub;
        } catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setDocumentServiceEndpointAddress(java.lang.String address) {
        DocumentService_address = address;
    }

    /**
     * For the given interface, get the stub implementation. If this service has no port for the given
     * interface, then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface)
            throws javax.xml.rpc.ServiceException {
        try {
            if (eu.cec.digit.circabc.repo.hrs.ws.DocumentService_PortType.class.isAssignableFrom(
                    serviceEndpointInterface)) {
                eu.cec.digit.circabc.repo.hrs.ws.DocumentServiceBindingStub _stub =
                        new eu.cec.digit.circabc.repo.hrs.ws.DocumentServiceBindingStub(
                                new java.net.URL(DocumentService_address), this);
                _stub.setPortName(getDocumentServiceWSDDServiceName());
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
        if ("DocumentService".equals(inputPortName)) {
            return getDocumentService();
        } else {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs", "DocumentWebService");
    }

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://ec.europa.eu/sg/hrs", "DocumentService"));
        }
        return ports.iterator();
    }

    /** Set the endpoint address for the specified port name. */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address)
            throws javax.xml.rpc.ServiceException {

        if ("DocumentService".equals(portName)) {
            setDocumentServiceEndpointAddress(address);
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
