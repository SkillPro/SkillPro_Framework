/**
 * AMLServerService_ServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package eu.skillpro.ams.pscm.connector.amlservice.gen;

public class AMLServerService_ServiceLocator extends org.apache.axis.client.Service implements eu.skillpro.ams.pscm.connector.amlservice.gen.AMLServerService_Service {

    public AMLServerService_ServiceLocator() {
    }


    public AMLServerService_ServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public AMLServerService_ServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for AMLServerServicePort
    private java.lang.String AMLServerServicePort_address = "http://syrios.mech.upatras.gr:80/skillpro/AMLServerService";

    public java.lang.String getAMLServerServicePortAddress() {
        return AMLServerServicePort_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String AMLServerServicePortWSDDServiceName = "AMLServerServicePort";

    public java.lang.String getAMLServerServicePortWSDDServiceName() {
        return AMLServerServicePortWSDDServiceName;
    }

    public void setAMLServerServicePortWSDDServiceName(java.lang.String name) {
        AMLServerServicePortWSDDServiceName = name;
    }

    public eu.skillpro.ams.pscm.connector.amlservice.gen.AMLServerService_PortType getAMLServerServicePort() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(AMLServerServicePort_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getAMLServerServicePort(endpoint);
    }

    public eu.skillpro.ams.pscm.connector.amlservice.gen.AMLServerService_PortType getAMLServerServicePort(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            eu.skillpro.ams.pscm.connector.amlservice.gen.AMLServerServicePortBindingStub _stub = new eu.skillpro.ams.pscm.connector.amlservice.gen.AMLServerServicePortBindingStub(portAddress, this);
            _stub.setPortName(getAMLServerServicePortWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setAMLServerServicePortEndpointAddress(java.lang.String address) {
        AMLServerServicePort_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (eu.skillpro.ams.pscm.connector.amlservice.gen.AMLServerService_PortType.class.isAssignableFrom(serviceEndpointInterface)) {
                eu.skillpro.ams.pscm.connector.amlservice.gen.AMLServerServicePortBindingStub _stub = new eu.skillpro.ams.pscm.connector.amlservice.gen.AMLServerServicePortBindingStub(new java.net.URL(AMLServerServicePort_address), this);
                _stub.setPortName(getAMLServerServicePortWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("AMLServerServicePort".equals(inputPortName)) {
            return getAMLServerServicePort();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://services/", "AMLServerService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://services/", "AMLServerServicePort"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
    	if ("AMLServerServicePort".equals(portName)) {
            setAMLServerServicePortEndpointAddress(address);
        } else { // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
