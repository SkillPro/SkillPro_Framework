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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandler2;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import eu.skillpro.ams.pscm.connector.opcua.OPCUAServerRepository;

/**
 * @author aleksa
 * 
 * @version: 07.10.2014
 * 
 */
public class Connect extends AbstractHandler implements IHandler, IHandler2 {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		if (eu.skillpro.ams.pscm.connector.opcua.Activator.getDefault().getCurrentUAaddress() == null) {
			MessageDialog
					.openWarning(
							HandlerUtil.getActiveShell(event),
							"OPCUA connector not configured",
							"The OPC-UA connector is not configured yet. Please configure it first from the configuration menu!");
		}

		try {
			OPCUAServerRepository.connect(eu.skillpro.ams.pscm.connector.opcua.Activator.getDefault()
					.getCurrentUAaddress());
		} catch (Exception e) {
			e.printStackTrace();
			MessageDialog.openWarning(HandlerUtil.getActiveShell(event),
					"Error connecting",e.getMessage()+"\n"+"Beginning of the error trace:\n"+e.getStackTrace()[0].toString());
		}
		return null;
	}

}
