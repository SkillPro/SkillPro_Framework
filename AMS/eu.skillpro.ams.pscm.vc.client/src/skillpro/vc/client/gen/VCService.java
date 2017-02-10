/**
 * VCService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package skillpro.vc.client.gen;

import skillpro.vc.client.gen.datacontract.Asset;

public interface VCService extends java.rmi.Remote {
    public java.lang.String[] getAssetsNames() throws java.rmi.RemoteException;
    public Asset[] getAssets() throws java.rmi.RemoteException;
    public void setWorldPositionMatrix(java.lang.Integer i, java.lang.Double x, java.lang.Double y, java.lang.Double z, java.lang.Double a, java.lang.Double b, java.lang.Double c) throws java.rmi.RemoteException;
    public void setWorldPositionMatrixName(java.lang.String name, java.lang.Double x, java.lang.Double y, java.lang.Double z, java.lang.Double a, java.lang.Double b, java.lang.Double c) throws java.rmi.RemoteException;
    public void setWorldPostionMatrixSkillProName(java.lang.String name, java.lang.Double x, java.lang.Double y, java.lang.Double z) throws java.rmi.RemoteException;
    public double[] getWorldPositionMatrixName(java.lang.String name) throws java.rmi.RemoteException;
    public double[] getBoundingBox(java.lang.String name) throws java.rmi.RemoteException;
    public void updateFromLocalArray() throws java.rmi.RemoteException;
    public java.lang.Boolean registerSEEToOPCUA(java.lang.String dataToRegister) throws java.rmi.RemoteException;
}
