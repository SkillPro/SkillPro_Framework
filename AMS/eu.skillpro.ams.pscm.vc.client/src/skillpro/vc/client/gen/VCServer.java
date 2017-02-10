/**
 * VCServer.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package skillpro.vc.client.gen;

public interface VCServer extends javax.xml.rpc.Service {
    public java.lang.String getBasicHttpBinding_VCServiceAddress();

    public skillpro.vc.client.gen.VCService getBasicHttpBinding_VCService() throws javax.xml.rpc.ServiceException;

    public skillpro.vc.client.gen.VCService getBasicHttpBinding_VCService(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
