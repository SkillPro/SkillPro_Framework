/**
 * de.fzi.skillpro.connector.amlservice.ui: 18 Mar 2014
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

package eu.skillpro.ams.pscm.connector.amlservice.ui;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import eu.skillpro.ams.pscm.connector.amlservice.ui.dialogs.SaveAMLDialog;

/**
 * @author Kiril Aleksandrov
 * @author Abteilung ISPE/PDE, FZI Forschungszentrum Informatik 
 *
 * 18 Mar 2014
 *
 */
public class SaveAMLHandler extends AbstractHandler implements IHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		saveAMLData(event);
		return null;
	}

	/**
	 * @param event
	 */
	private void saveAMLData(ExecutionEvent event) {
		SaveAMLDialog amlDialog = new SaveAMLDialog(HandlerUtil.getActiveShell(event));
		amlDialog.create();
		if (amlDialog.open() == Dialog.OK) {
			MessageDialog.openConfirm(HandlerUtil.getActiveShell(event), "File successfully saved", "The file was successfully saved. Filename: " + amlDialog.getFileName() + ", FileID: "+amlDialog.getFileID());
		}
	}
}
