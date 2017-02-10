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
import java.io.IOException;
import java.util.List;

import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import skillpro.model.service.SkillproService;
import skillpro.model.update.UpdateType;
import skillpro.transformator.actions.SkillproTransformAction;
import utils.FileUtility;
import aml.amlparser.AMLParser;
import aml.skillpro.mapping.TransformationMappingParser;
import aml.transfer.repo.TransferParsedAMLToRepo;
import aml.transformation.service.AMLTransformationService;
import amltransformation.dialogs.ImportAMLDialog;

public class OpenPPRHandler extends AbstractHandler implements IHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		File defaultMapping = new File("DefaultMapping.xml");
		if (!defaultMapping.exists()) {
			MessageBox dialog = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.ICON_ERROR | SWT.OK| SWT.CANCEL);
			dialog.setText("Default mapping doesn't exist!");
			dialog.setMessage("Please create 'DefaultMapping.xml' first");
			dialog.open();
		} else {
			try {
				loadAMLData(event);
			} catch (ValidityException e) {
				e.printStackTrace();
			} catch (ParsingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private void loadAMLData(ExecutionEvent event) throws ValidityException, ParsingException, IOException {
		ImportAMLDialog amlDialog = new ImportAMLDialog(HandlerUtil.getActiveShell(event));
		amlDialog.create();
		if (amlDialog.open() == Dialog.OK) {
			if (AMLTransformationService.getAMLProvider().isDirty()) {
				MessageBox dialog = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.ICON_QUESTION | SWT.YES| SWT.NO);
				dialog.setText("Merge changes");
				dialog.setMessage("Do you want to overwrite your changes?");
				if (dialog.open() == SWT.YES) {
					AMLTransformationService.getAMLProvider().wipeAllData();
					AMLTransformationService.getTransformationProvider().wipeAllData();
					AMLParser.getInstance().wipeData();
					SkillproService.getUpdateManager().notify(UpdateType.AML_DOMAIN_DELETED, null);
				}
			}
			if (amlDialog.hasParts()) {
				for (String input : amlDialog.getListOfLibraries()) {
					AMLParser.getInstance().parseAMLFromString(input);
				}
			}
			//stores the filename to FileUtility only if it was opened by using projects
			List<String> listOfProjects = amlDialog.getListOfProjects();
			if (amlDialog.getListOfLibraries().isEmpty() && amlDialog.getProjectsMap().size() == 1) {
				for (String filename : amlDialog.getProjectsMap().keySet()) {
					FileUtility.getInstance().setFilename(filename);
					
				}
			}
			for (String input : listOfProjects) {
				AMLParser.getInstance().parseAMLFromString(input);
			}
			//transfer parsed aml elements to repo
			TransferParsedAMLToRepo.getInstance().transferToAMLRepoFromAMLParser();
			//load mapping configuration
			TransformationMappingParser.wipeData();
			AMLParser.getInstance().wipeData();
			TransformationMappingParser.loadConfiguration("DefaultMapping.xml");
			//transform to skillpro
			new SkillproTransformAction().run();
			SkillproService.getUpdateManager().notify(UpdateType.NEW_DATA_IMPORTED, null);
		}
	}
}
