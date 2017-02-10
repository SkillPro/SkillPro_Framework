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

package skillpro.vc.client.gen;

import skillpro.vc.client.gen.datacontract.Asset;

public class VCServiceProxy implements skillpro.vc.client.gen.VCService {
  private String _endpoint = null;
  private skillpro.vc.client.gen.VCService vCService = null;
  
  public VCServiceProxy() {
    _initVCServiceProxy();
  }
  
  public VCServiceProxy(String endpoint) {
    _endpoint = endpoint;
    _initVCServiceProxy();
  }
  
  private void _initVCServiceProxy() {
    try {
      vCService = (new skillpro.vc.client.gen.VCServerLocator()).getBasicHttpBinding_VCService();
      if (vCService != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)vCService)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)vCService)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (vCService != null)
      ((javax.xml.rpc.Stub)vCService)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public skillpro.vc.client.gen.VCService getVCService() {
    if (vCService == null)
      _initVCServiceProxy();
    return vCService;
  }
  
  public java.lang.String[] getAssetsNames() throws java.rmi.RemoteException{
    if (vCService == null)
      _initVCServiceProxy();
    return vCService.getAssetsNames();
  }
  
  public Asset[] getAssets() throws java.rmi.RemoteException{
    if (vCService == null)
      _initVCServiceProxy();
    return vCService.getAssets();
  }
  
  public void setWorldPositionMatrix(java.lang.Integer i, java.lang.Double x, java.lang.Double y, java.lang.Double z, java.lang.Double a, java.lang.Double b, java.lang.Double c) throws java.rmi.RemoteException{
    if (vCService == null)
      _initVCServiceProxy();
    vCService.setWorldPositionMatrix(i, x, y, z, a, b, c);
  }
  
  public void setWorldPositionMatrixName(java.lang.String name, java.lang.Double x, java.lang.Double y, java.lang.Double z, java.lang.Double a, java.lang.Double b, java.lang.Double c) throws java.rmi.RemoteException{
    if (vCService == null)
      _initVCServiceProxy();
    vCService.setWorldPositionMatrixName(name, x, y, z, a, b, c);
  }
  
  public void setWorldPostionMatrixSkillProName(java.lang.String name, java.lang.Double x, java.lang.Double y, java.lang.Double z) throws java.rmi.RemoteException{
    if (vCService == null)
      _initVCServiceProxy();
    vCService.setWorldPostionMatrixSkillProName(name, x, y, z);
  }
  
  public double[] getWorldPositionMatrixName(java.lang.String name) throws java.rmi.RemoteException{
    if (vCService == null)
      _initVCServiceProxy();
    return vCService.getWorldPositionMatrixName(name);
  }
  
  public double[] getBoundingBox(java.lang.String name) throws java.rmi.RemoteException{
    if (vCService == null)
      _initVCServiceProxy();
    return vCService.getBoundingBox(name);
  }
  
  public void updateFromLocalArray() throws java.rmi.RemoteException{
    if (vCService == null)
      _initVCServiceProxy();
    vCService.updateFromLocalArray();
  }
  
  public java.lang.Boolean registerSEEToOPCUA(java.lang.String dataToRegister) throws java.rmi.RemoteException{
    if (vCService == null)
      _initVCServiceProxy();
    return vCService.registerSEEToOPCUA(dataToRegister);
  }
  
  
}