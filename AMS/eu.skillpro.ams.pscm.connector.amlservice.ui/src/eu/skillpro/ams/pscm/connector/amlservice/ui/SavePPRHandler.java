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

import javax.xml.transform.TransformerException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

import skillpro.model.service.SkillproService;
import skillpro.model.update.UpdateType;
import skillpro.transformator.actions.SkillproReverseTransformAction;
import utils.FileUtility;
import aml.amlparser.AMLExporter;
import aml.domain.InternalElement;
import aml.transformation.service.AMLTransformationService;

public class SavePPRHandler extends AbstractHandler implements IHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (FileUtility.getInstance().getFilename() != null) {
			try {
				//wipe all the internal elements in repo
				AMLTransformationService.getAMLProvider().getAMLModelRepo(InternalElement.class).wipeAllData();
				AMLTransformationService.getAMLProvider().getAMLInternalElementRepo().wipeAllData();
				//overwrites the old data
				exportAMLData(event, FileUtility.getInstance().getFilename());
			} catch (TransformerException e) {
				e.printStackTrace();
			}
		} else {
			ICommandService commandService = (ICommandService)PlatformUI.getWorkbench().getAdapter(ICommandService.class);
			if (commandService != null) {
				try {
					commandService.getCommand(SavePPRAsHandler.class.getName()).executeWithChecks(event);
				} catch (NotDefinedException e) {
					e.printStackTrace();
				} catch (NotEnabledException e) {
					e.printStackTrace();
				} catch (NotHandledException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	private void exportAMLData(ExecutionEvent event, String filename) throws TransformerException {
		//Reverse transformer
		new SkillproReverseTransformAction().run();
		SkillproService.getUpdateManager().notify(UpdateType.NEW_DATA_IMPORTED, null);
		//export
		if (filename != null && !filename.equals("")) {
			AMLExporter.saveFile(filename);
		}
	}
}
