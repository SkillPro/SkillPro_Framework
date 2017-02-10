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

package eu.skillpro.ams.pscm.connector.amsservice.ui;

import java.net.MalformedURLException;
import java.net.URL;

import masterviews.dialogs.ComboInputDialog;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.ui.handlers.HandlerUtil;

import skillpro.ams.util.AMSServiceUtility;

/**
 * @author Kiril Aleksandrov
 * 
 */
public class ConfigureConnection extends AbstractHandler implements IHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ComboInputDialog dialog = new ComboInputDialog (
				HandlerUtil.getActiveShell(event),
				"Configure AMS server address",
				"Please provide the server adresss for the AMS service. Allowed format: \"http://server:port/serviceName\"",
				new String[] { 
					AMSServiceUtility.SERVICE_ADDRESS_FUKUSHIMA,
					AMSServiceUtility.SERVICE_ADDRESS_OTHER_LOCALHOST, 
					AMSServiceUtility.SERVICE_ADDRESS_LOCALHOST,
					AMSServiceUtility.SERVICE_ADDRESS_OSAKA05,
					AMSServiceUtility.SERVICE_ADDRESS_ENDDEMO
				}, 
				AMSServiceUtility.serviceAddress, new IInputValidator() {
					public String isValid(String newText) {
						if (!isValidUrl(newText)) {
							return "Please consider the format!";
						}
						return null;
					}
				});
		if (dialog.open() == Dialog.OK) {
			AMSServiceUtility.serviceAddress=dialog.getValue();
		}
		return null;
	}

	private static boolean isValidUrl(String urlString) {
		try {
			new URL(urlString);
			return true;
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return false;
		}
	}
}
