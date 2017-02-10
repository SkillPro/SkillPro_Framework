/**
 * VCServerLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package skillpro.vc.client.gen;

public class VCServerLocator extends org.apache.axis.client.Service implements skillpro.vc.client.gen.VCServer {

    public VCServerLocator() {
    }


    public VCServerLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public VCServerLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for BasicHttpBinding_VCService
//    private java.lang.String BasicHttpBinding_VCService_address = "http://localhost:8000/VCService";
    private java.lang.String BasicHttpBinding_VCService_address = "http://141.3.82.73:8000/VCService";

    public java.lang.String getBasicHttpBinding_VCServiceAddress() {
        return BasicHttpBinding_VCService_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String BasicHttpBinding_VCServiceWSDDServiceName = "BasicHttpBinding_VCService";

    public java.lang.String getBasicHttpBinding_VCServiceWSDDServiceName() {
        return BasicHttpBinding_VCServiceWSDDServiceName;
    }

    public void setBasicHttpBinding_VCServiceWSDDServiceName(java.lang.String name) {
        BasicHttpBinding_VCServiceWSDDServiceName = name;
    }

    public skillpro.vc.client.gen.VCService getBasicHttpBinding_VCService() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(BasicHttpBinding_VCService_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getBasicHttpBinding_VCService(endpoint);
    }

    public skillpro.vc.client.gen.VCService getBasicHttpBinding_VCService(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            skillpro.vc.client.gen.BasicHttpBinding_VCServiceStub _stub = new skillpro.vc.client.gen.BasicHttpBinding_VCServiceStub(portAddress, this);
            _stub.setPortName(getBasicHttpBinding_VCServiceWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setBasicHttpBinding_VCServiceEndpointAddress(java.lang.String address) {
        BasicHttpBinding_VCService_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (skillpro.vc.client.gen.VCService.class.isAssignableFrom(serviceEndpointInterface)) {
                skillpro.vc.client.gen.BasicHttpBinding_VCServiceStub _stub = new skillpro.vc.client.gen.BasicHttpBinding_VCServiceStub(new java.net.URL(BasicHttpBinding_VCService_address), this);
                _stub.setPortName(getBasicHttpBinding_VCServiceWSDDServiceName());
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
        if ("BasicHttpBinding_VCService".equals(inputPortName)) {
            return getBasicHttpBinding_VCService();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://tempuri.org/", "VCServer");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://tempuri.org/", "BasicHttpBinding_VCService"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("BasicHttpBinding_VCService".equals(portName)) {
            setBasicHttpBinding_VCServiceEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
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
