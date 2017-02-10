/*****************************************************************************
 *
 * Copyright 2012-2016 SkillPro Consortium
 *
 * Author: PDE, FZI, pde@fzi.de
 *
 * Date of creation: 2012-2016
 *
 * Module: Production System Configuration Manager (PSCM)
 *
 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 *
 * This file is part of the AMS (Asset Management System), which has been developed
 * at the PDE department of the FZI, Karlsruhe. It is part of the SkillPro Framework,
 * which is is developed in the SkillPro project, funded by the European FP7
 * programme (Grant Agreement 287733).
 *
 * The SkillPro Framework is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The SkillPro Framework is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the SkillPro Framework. If not, see <http://www.gnu.org/licenses/>.
*****************************************************************************/

package eu.skillpro.ams.pscm.connector.amlservice.gen.v2;

public class AMLServerServiceProxy implements eu.skillpro.ams.pscm.connector.amlservice.gen.v2.AMLServerService_PortType {
  private String _endpoint = null;
  private eu.skillpro.ams.pscm.connector.amlservice.gen.v2.AMLServerService_PortType aMLServerService_PortType = null;
  
  public AMLServerServiceProxy() {
    _initAMLServerServiceProxy();
  }
  
  public AMLServerServiceProxy(String endpoint) {
    _endpoint = endpoint;
    _initAMLServerServiceProxy();
  }
  
  private void _initAMLServerServiceProxy() {
    try {
      aMLServerService_PortType = (new eu.skillpro.ams.pscm.connector.amlservice.gen.v2.AMLServerService_ServiceLocator()).getAMLServerServicePort();
      if (aMLServerService_PortType != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)aMLServerService_PortType)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)aMLServerService_PortType)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (aMLServerService_PortType != null)
      ((javax.xml.rpc.Stub)aMLServerService_PortType)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public eu.skillpro.ams.pscm.connector.amlservice.gen.v2.AMLServerService_PortType getAMLServerService_PortType() {
    if (aMLServerService_PortType == null)
      _initAMLServerServiceProxy();
    return aMLServerService_PortType;
  }
  
  public long saveAMLfileStr(java.lang.String amlFile, java.lang.String username, java.lang.String password, java.lang.String amlFileName) throws java.rmi.RemoteException{
    if (aMLServerService_PortType == null)
      _initAMLServerServiceProxy();
    return aMLServerService_PortType.saveAMLfileStr(amlFile, username, password, amlFileName);
  }
  
  public boolean updateAMLfileStr(java.lang.String newAmlFile, java.lang.String username, java.lang.String password, long amlfileid) throws java.rmi.RemoteException{
    if (aMLServerService_PortType == null)
      _initAMLServerServiceProxy();
    return aMLServerService_PortType.updateAMLfileStr(newAmlFile, username, password, amlfileid);
  }
  
  public java.lang.Long[] searchforAMLFilesByText(java.lang.String username, java.lang.String password, java.lang.String text) throws java.rmi.RemoteException{
    if (aMLServerService_PortType == null)
      _initAMLServerServiceProxy();
    return aMLServerService_PortType.searchforAMLFilesByText(username, password, text);
  }
  
  public java.lang.Long[] searchforAMLFilesByName(java.lang.String username, java.lang.String password, java.lang.String nameIncludingWildCards) throws java.rmi.RemoteException{
    if (aMLServerService_PortType == null)
      _initAMLServerServiceProxy();
    return aMLServerService_PortType.searchforAMLFilesByName(username, password, nameIncludingWildCards);
  }
  
  public java.lang.String searchforAMLFilesByNameStr(java.lang.String username, java.lang.String password, java.lang.String nameIncludingWildCards) throws java.rmi.RemoteException{
    if (aMLServerService_PortType == null)
      _initAMLServerServiceProxy();
    return aMLServerService_PortType.searchforAMLFilesByNameStr(username, password, nameIncludingWildCards);
  }
  
  public eu.skillpro.ams.pscm.connector.amlservice.gen.v2.schema.AMLProject[] searchforAMLFilesByNameXML(java.lang.String username, java.lang.String password, java.lang.String nameIncludingWildCards) throws java.rmi.RemoteException{
    if (aMLServerService_PortType == null)
      _initAMLServerServiceProxy();
    return aMLServerService_PortType.searchforAMLFilesByNameXML(username, password, nameIncludingWildCards);
  }
  
  public long insertLinkedFileToAML(byte[] newLinkedFile, java.lang.String username, java.lang.String password, java.lang.String linkedfileName, long amlfileid) throws java.rmi.RemoteException{
    if (aMLServerService_PortType == null)
      _initAMLServerServiceProxy();
    return aMLServerService_PortType.insertLinkedFileToAML(newLinkedFile, username, password, linkedfileName, amlfileid);
  }
  
  public long insertLinkedFileToAMLStr(java.lang.String newLinkedFile, java.lang.String username, java.lang.String password, java.lang.String linkedfileName, long amlfileid) throws java.rmi.RemoteException{
    if (aMLServerService_PortType == null)
      _initAMLServerServiceProxy();
    return aMLServerService_PortType.insertLinkedFileToAMLStr(newLinkedFile, username, password, linkedfileName, amlfileid);
  }
  
  public boolean updateLinkedFile(byte[] newLinkedFile, java.lang.String username, java.lang.String password, long linkedfileid) throws java.rmi.RemoteException{
    if (aMLServerService_PortType == null)
      _initAMLServerServiceProxy();
    return aMLServerService_PortType.updateLinkedFile(newLinkedFile, username, password, linkedfileid);
  }
  
  public boolean updateLinkedFileStr(java.lang.String newLinkedFile, java.lang.String username, java.lang.String password, long linkedfileid) throws java.rmi.RemoteException{
    if (aMLServerService_PortType == null)
      _initAMLServerServiceProxy();
    return aMLServerService_PortType.updateLinkedFileStr(newLinkedFile, username, password, linkedfileid);
  }
  
  public boolean deleteLinkedFile(java.lang.String username, java.lang.String password, long linkedfileid) throws java.rmi.RemoteException{
    if (aMLServerService_PortType == null)
      _initAMLServerServiceProxy();
    return aMLServerService_PortType.deleteLinkedFile(username, password, linkedfileid);
  }
  
  public java.lang.String getLinkedfileStr(java.lang.String username, java.lang.String password, long linkedfileid) throws java.rmi.RemoteException{
    if (aMLServerService_PortType == null)
      _initAMLServerServiceProxy();
    return aMLServerService_PortType.getLinkedfileStr(username, password, linkedfileid);
  }
  
  public java.lang.String getAMLFileName(long arg0) throws java.rmi.RemoteException, eu.skillpro.ams.pscm.connector.amlservice.gen.v2.Exception{
    if (aMLServerService_PortType == null)
      _initAMLServerServiceProxy();
    return aMLServerService_PortType.getAMLFileName(arg0);
  }
  
  public int countAllAttachementFiles() throws java.rmi.RemoteException, eu.skillpro.ams.pscm.connector.amlservice.gen.v2.Exception{
    if (aMLServerService_PortType == null)
      _initAMLServerServiceProxy();
    return aMLServerService_PortType.countAllAttachementFiles();
  }
  
  public java.lang.String getAttachmentFileName(long arg0) throws java.rmi.RemoteException, eu.skillpro.ams.pscm.connector.amlservice.gen.v2.Exception{
    if (aMLServerService_PortType == null)
      _initAMLServerServiceProxy();
    return aMLServerService_PortType.getAttachmentFileName(arg0);
  }
  
  public eu.skillpro.ams.pscm.connector.amlservice.gen.v2.schema.FileDescription[] getAttachmentsForAMLID(long arg0) throws java.rmi.RemoteException{
    if (aMLServerService_PortType == null)
      _initAMLServerServiceProxy();
    return aMLServerService_PortType.getAttachmentsForAMLID(arg0);
  }
  
  public java.lang.String hello(java.lang.String name) throws java.rmi.RemoteException{
    if (aMLServerService_PortType == null)
      _initAMLServerServiceProxy();
    return aMLServerService_PortType.hello(name);
  }
  
  public long saveAMLfile(byte[] amlFile, java.lang.String username, java.lang.String password, java.lang.String amlFileName) throws java.rmi.RemoteException{
    if (aMLServerService_PortType == null)
      _initAMLServerServiceProxy();
    return aMLServerService_PortType.saveAMLfile(amlFile, username, password, amlFileName);
  }
  
  public boolean deleteAMLfile(long amlfileid, java.lang.String username, java.lang.String password) throws java.rmi.RemoteException{
    if (aMLServerService_PortType == null)
      _initAMLServerServiceProxy();
    return aMLServerService_PortType.deleteAMLfile(amlfileid, username, password);
  }
  
  public boolean updateAMLfile(byte[] newAmlFile, java.lang.String username, java.lang.String password, long amlfileid) throws java.rmi.RemoteException{
    if (aMLServerService_PortType == null)
      _initAMLServerServiceProxy();
    return aMLServerService_PortType.updateAMLfile(newAmlFile, username, password, amlfileid);
  }
  
  public byte[] getAMLfile(java.lang.String username, java.lang.String password, long amlfileid) throws java.rmi.RemoteException{
    if (aMLServerService_PortType == null)
      _initAMLServerServiceProxy();
    return aMLServerService_PortType.getAMLfile(username, password, amlfileid);
  }
  
  public java.lang.String getAMLfileStr(java.lang.String username, java.lang.String password, long amlfileid) throws java.rmi.RemoteException{
    if (aMLServerService_PortType == null)
      _initAMLServerServiceProxy();
    return aMLServerService_PortType.getAMLfileStr(username, password, amlfileid);
  }
  
  public byte[] getLinkedfile(java.lang.String username, java.lang.String password, long linkedfileid) throws java.rmi.RemoteException{
    if (aMLServerService_PortType == null)
      _initAMLServerServiceProxy();
    return aMLServerService_PortType.getLinkedfile(username, password, linkedfileid);
  }
  
  public int countAMLFiles() throws java.rmi.RemoteException, eu.skillpro.ams.pscm.connector.amlservice.gen.v2.Exception{
    if (aMLServerService_PortType == null)
      _initAMLServerServiceProxy();
    return aMLServerService_PortType.countAMLFiles();
  }
  
  
}