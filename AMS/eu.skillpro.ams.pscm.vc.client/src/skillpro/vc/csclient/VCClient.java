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

package skillpro.vc.csclient;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import skillpro.model.assets.SEE;
import skillpro.vc.client.gen.VCService;
import skillpro.vc.client.gen.VCServiceProxy;
import skillpro.vc.client.gen.datacontract.Asset;


public class VCClient {
	private VCService service = null;
	private static final VCClient INSTANCE = new VCClient();

	private VCClient() {
		service = new VCServiceProxy();
	}
	
	public static VCClient getInstance() {
		return INSTANCE;
	}
	
	public void setAssetPosition(String name, double x, double y, double z, double a, double b, double c) {
		try {
			service.setWorldPostionMatrixSkillProName(name, x * 10, y * 10, z * 10);
		} catch (RemoteException e) {
			handleError();
		}
	}
	
	public double[] getAssetPosition(String name) {
		try {
			return service.getWorldPositionMatrixName(name);
		} catch (RemoteException e) {
			handleError();
		}
		return null;
		
	}
	
	public List<Asset> getAssets() {
		try {
			return Arrays.asList(service.getAssets());
		} catch (RemoteException e) {
			handleError();
		}
		return null;
	}
	
	public void synchronize() {
		try {
			service.updateFromLocalArray();
		} catch (RemoteException e) {
			handleError();
		}
	}
	
	public boolean registerSEEToVIS(SEE see) {
			String runtimeID = see.getMESNodeID();
			runtimeID = runtimeID.substring(runtimeID.lastIndexOf('=') + 1);
			String registerMessage = "\n#" + see.getSeeID() + " - " + see.getResource()
					+ "\n" + runtimeID + ".Call:string"
					+ "\n" + runtimeID + ".Mode:int";
			try {
				if (service.registerSEEToOPCUA(registerMessage)) {
					return true;
				}				
			} catch (RemoteException e) {
				handleError();
			}	
			return false;
	}
	private void handleError() {
		MessageDialog dialog = new MessageDialog(Display.getCurrent().getActiveShell() , "VIS-Server not running!", null,
				"Please launch the VIS-Server", MessageDialog.ERROR, new String[] { "OK"}, 0);
		dialog.open();
	}
}
