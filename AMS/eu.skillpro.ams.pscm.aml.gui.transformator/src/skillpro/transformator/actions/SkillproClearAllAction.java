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

package skillpro.transformator.actions;

import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.PlatformUI;

import skillpro.model.service.SkillproService;
import skillpro.model.update.UpdateType;
import transformation.interfaces.ITransformable;
import aml.amlparser.AMLParser;
import aml.domain.InternalElement;
import aml.model.Hierarchy;
import aml.transformation.repo.transformation.TransformationRepo;
import aml.transformation.service.AMLTransformationService;

public class SkillproClearAllAction extends Action {
	private static final String TOOLTIP = "Clear All Transformable Data";
	private static final String ICON = "icons/delete.gif";
	
	protected final TransformationRepo transformationRepo;
	protected final Map<Object, Class<? extends ITransformable>> transformables;
	
	public SkillproClearAllAction() {
		setToolTipText(TOOLTIP);
		setImageDescriptor(eu.skillpro.ams.pscm.gui.masterviews.Activator.getImageDescriptor(ICON));
		transformationRepo = AMLTransformationService.getTransformationProvider().getTransformationRepo();
		transformables = transformationRepo.getInterfaceTransformablesMapping();
	}

	@Override
	public void run() {
		//delete old transformation data
		System.out.println("==========================");
		System.out.println("WIPING DATA");
		System.out.println("==========================");
		transformationRepo.wipeAllData();
		
		MessageBox dialog = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
		
		dialog.setText("Delete all AML elements?");
		dialog.setMessage("Click 'Yes' if you want to delete all AML elements.\nClick 'No' if you only want to delete the InternalElement instances.");
		if (dialog.open() == SWT.YES) {
			AMLTransformationService.getAMLProvider().wipeAllData();
		} else {
			for (Hierarchy<InternalElement> hie : AMLTransformationService.getAMLProvider().getAMLModelRepo(InternalElement.class).getFlattenedHierarchies()) {
				transformationRepo.getAdapterTransformablesMapping().remove(hie.getElement());
			}
			AMLTransformationService.getAMLProvider().getAMLModelRepo(InternalElement.class).wipeAllData();
		}
		
		AMLParser.getInstance().wipeData();
		
		SkillproService.getUpdateManager().notify(UpdateType.NEW_DATA_IMPORTED, null);
		System.out.println("==========================");
		System.out.println("WIPING COMPLETED");
		System.out.println("==========================");
	}
}