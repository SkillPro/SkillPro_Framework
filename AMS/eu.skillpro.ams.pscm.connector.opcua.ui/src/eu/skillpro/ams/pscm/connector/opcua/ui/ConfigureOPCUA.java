/**
 * 
 */
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

package eu.skillpro.ams.pscm.connector.opcua.ui;

import masterviews.dialogs.ComboInputDialog;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import eu.skillpro.ams.pscm.connector.opcua.OPCUAServerRepository;
import eu.skillpro.ams.pscm.connector.opcua.SkillProOPCUAException;

/**
 * @author aleksa
 * 
 * @version: 07.10.2014
 *
 */
public class ConfigureOPCUA extends AbstractHandler implements
		IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		// FIXME (multiple properties such as security etc.)
		ComboInputDialog dialog = new ComboInputDialog (
				// default IOSB opc.tcp://192.44.1.52:51200
				HandlerUtil.getActiveShell(event),
				"Configure OPC-UA connector",
				"Please provide the server adress for the OPC-UA connector.\nAllowed format: \"opc.tcp://SEVERADDRESS:52520\".",
				new String[] { 
					OPCUAServerRepository.HMI_SERVER, 
					OPCUAServerRepository.IOSB_SERVER, 
					OPCUAServerRepository.LOCALHOST_SERVER, OPCUAServerRepository.EDO_SERVER, OPCUAServerRepository.IOSB_SERVER_2,
					OPCUAServerRepository.IPR_SERVER,
					OPCUAServerRepository.DEFAULT_EDO_SERVER}, OPCUAServerRepository.getSelectedServerUri(),
				new IInputValidator() {
					public String isValid(String newText) {
						return OPCUAServerRepository.verifyAddress(newText);
					}
				});
		
		if (dialog.open() == Dialog.OK) {
			try {
				OPCUAServerRepository.createClientForURL(dialog.getValue());
				eu.skillpro.ams.pscm.connector.opcua.Activator.getDefault().setCurrentUAaddress(dialog.getValue());
				OPCUAServerRepository.configureServer(dialog.getValue());
				
				//connect
				OPCUAServerRepository.connect(eu.skillpro.ams.pscm.connector.opcua.Activator.getDefault()
						.getCurrentUAaddress());
			} catch (SkillProOPCUAException e) {
				MessageDialog.openError(HandlerUtil.getActiveShell(event), "Error creating OPCUA client", e.getMessage());
				e.printStackTrace();
			}
		}

		return null;
	}
}
