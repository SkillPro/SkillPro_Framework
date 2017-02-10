/**
 * AMLServerService_PortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package eu.skillpro.ams.pscm.connector.amlservice.gen;

public interface AMLServerService_PortType extends java.rmi.Remote {
    public long saveAMLfileStr(java.lang.String amlFile, java.lang.String username, java.lang.String password, java.lang.String amlFileName) throws java.rmi.RemoteException;
    public boolean updateAMLfileStr(java.lang.String newAmlFile, java.lang.String username, java.lang.String password, long amlfileid) throws java.rmi.RemoteException;
    public java.lang.Long[] searchforAMLFilesByText(java.lang.String username, java.lang.String password, java.lang.String text) throws java.rmi.RemoteException;
    public java.lang.Long[] searchforAMLFilesByName(java.lang.String username, java.lang.String password, java.lang.String nameIncludingWildCards) throws java.rmi.RemoteException;
    public long insertLinkedFileToAML(byte[] newLinkedFile, java.lang.String username, java.lang.String password, java.lang.String linkedfileName, long amlfileid) throws java.rmi.RemoteException;
    public long insertLinkedFileToAMLStr(java.lang.String newLinkedFile, java.lang.String username, java.lang.String password, java.lang.String linkedfileName, long amlfileid) throws java.rmi.RemoteException;
    public boolean updateLinkedFile(byte[] newLinkedFile, java.lang.String username, java.lang.String password, long linkedfileid) throws java.rmi.RemoteException;
    public boolean updateLinkedFileStr(java.lang.String newLinkedFile, java.lang.String username, java.lang.String password, long linkedfileid) throws java.rmi.RemoteException;
    public boolean deleteLinkedFile(java.lang.String username, java.lang.String password, long linkedfileid) throws java.rmi.RemoteException;
    public java.lang.String getLinkedfileStr(java.lang.String username, java.lang.String password, long linkedfileid) throws java.rmi.RemoteException;
    public java.lang.String hello(java.lang.String name) throws java.rmi.RemoteException;
    public long saveAMLfile(byte[] amlFile, java.lang.String username, java.lang.String password, java.lang.String amlFileName) throws java.rmi.RemoteException;
    public boolean deleteAMLfile(long amlfileid, java.lang.String username, java.lang.String password) throws java.rmi.RemoteException;
    public boolean updateAMLfile(byte[] newAmlFile, java.lang.String username, java.lang.String password, long amlfileid) throws java.rmi.RemoteException;
    public byte[] getAMLfile(java.lang.String username, java.lang.String password, long amlfileid) throws java.rmi.RemoteException;
    public java.lang.String getAMLfileStr(java.lang.String username, java.lang.String password, long amlfileid) throws java.rmi.RemoteException;
    public byte[] getLinkedfile(java.lang.String username, java.lang.String password, long linkedfileid) throws java.rmi.RemoteException;
}
