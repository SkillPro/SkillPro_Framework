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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.TransformerException;

import masterviews.dialogs.MasterFileDialog;
import masterviews.util.SupportedFileType;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.PlatformUI;

import skillpro.model.service.SkillproService;
import skillpro.model.update.UpdateType;
import skillpro.transformator.actions.SkillproReverseTransformAction;
import aml.amlparser.AMLExporter;
import aml.domain.InternalElement;
import aml.model.Hierarchy;
import aml.model.Root;
import aml.transformation.repo.aml.AMLInternalElementRepo;
import aml.transformation.repo.aml.AMLModelRepo;
import aml.transformation.service.AMLTransformationService;

public class SavePPRAsHandler extends AbstractHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		File defaultMapping = new File("DefaultMapping.xml");
		if (!defaultMapping.exists()) {
			MessageBox dialog = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.ICON_ERROR | SWT.OK| SWT.CANCEL);
			dialog.setText("Default mapping doesn't exist!");
			dialog.setMessage("Please create 'DefaultMapping.xml' first");
			dialog.open();
		} else {
			//saves the old InternalElements before exporting
			AMLModelRepo<InternalElement> internalElementModelRepo = AMLTransformationService
					.getAMLProvider().getAMLModelRepo(InternalElement.class);
			List<Hierarchy<InternalElement>> flattenedHies = new ArrayList<>(internalElementModelRepo.getFlattenedHierarchies());
			List<Root<InternalElement>> ieRoots = new ArrayList<>(internalElementModelRepo.getEntities());
			AMLInternalElementRepo ieRepo = AMLTransformationService.getAMLProvider().getAMLInternalElementRepo();
			List<InternalElement> internalElements = new ArrayList<>(ieRepo.getEntities());
			//the internalElementModelRepo and ieRepo will be wiped before exporting
			internalElementModelRepo.wipeAllData();
			ieRepo.wipeAllData();
			try {
				exportAMLData(event);
			} catch (TransformerException e) {
				e.printStackTrace();
			}
			//the repos will be wiped again after exporting in order for us to retain the old repo state
			internalElementModelRepo.wipeAllData();
			ieRepo.wipeAllData();
			//copying old data
			ieRepo.getEntities().addAll(internalElements);
			internalElementModelRepo.getEntities().addAll(ieRoots);
			internalElementModelRepo.getFlattenedHierarchies().addAll(flattenedHies);
			//update
			SkillproService.getUpdateManager().notify(UpdateType.NEW_DATA_IMPORTED, null);
		}
		return null;
	}

	private void exportAMLData(ExecutionEvent event) throws TransformerException {
		
		//Reverse transformer
		new SkillproReverseTransformAction().run();
		SkillproService.getUpdateManager().notify(UpdateType.NEW_DATA_IMPORTED, null);
		//export
		
		String filename = MasterFileDialog.saveFile(SupportedFileType.AML);
		if (filename != null && !filename.equals("")) {
			AMLExporter.saveFile(filename);
		}
	}

}
