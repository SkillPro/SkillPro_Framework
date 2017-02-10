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

package skillpro.gui.skills.views;

import masterviews.dialogs.EditAllNameSelectionDialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.part.ViewPart;

import skillpro.model.service.SkillproService;
import skillpro.model.skills.Skill;
import skillpro.model.update.Updatable;
import skillpro.model.update.UpdateType;
import skillpro.view.impl.CompleteSkillTreeComposite;

public class CompleteSkillTreeView extends ViewPart implements Updatable {
	public static final String ID = CompleteSkillTreeView.class.getName();
	private CompleteSkillTreeComposite completeSkillTreeComposite;
	
	@Override
	public void createPartControl(Composite parent) {
		createViewer(parent);
		getSite().setSelectionProvider(completeSkillTreeComposite.getTreeViewer());
		SkillproService.getUpdateManager().registerUpdatable(this, Skill.class);
	}

	private void createViewer(Composite parent) {
		completeSkillTreeComposite =  new CompleteSkillTreeComposite(parent, SWT.NULL);
		completeSkillTreeComposite.setLayoutData(GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).create());
		//no need for initInput()
		completeSkillTreeComposite.setInput(SkillproService.getSkillproProvider().getTemplateSkillRepo().getEntities());
		completeSkillTreeComposite.getViewer().addDoubleClickListener(createDoubleClickListener());
	}
	
	private IDoubleClickListener createDoubleClickListener() {
		return new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				Object selection = ((StructuredSelection) completeSkillTreeComposite.getViewer().getSelection()).getFirstElement();
				Shell shell = Display.getCurrent().getActiveShell();
				if (selection instanceof Skill) {
					EditAllNameSelectionDialog changeNameDialog = new EditAllNameSelectionDialog(shell, ((Skill) selection).getName());
					changeNameDialog.open();
					if (changeNameDialog.getReturnCode() == Dialog.OK) {
						((Skill) selection).setName((String) changeNameDialog.getName());
						SkillproService.getUpdateManager().notify(UpdateType.SKILL_UPDATED, Skill.class);
					}
				}
			}
		};
	}
	
	@Override
	public void setFocus() {
		
	}

	@Override
	public void update(UpdateType type) {
		//better update types? Like one for resource, one for templates etc??
		switch (type) {
		case NEW_DATA_IMPORTED:
			completeSkillTreeComposite.getTreeViewer().refresh();
			break;
		case SKILL_CREATED:
			completeSkillTreeComposite.getTreeViewer().refresh();
			break;
		case SKILL_UPDATED:
			completeSkillTreeComposite.getTreeViewer().refresh();
			break;
		case SKILL_DELETED:
			completeSkillTreeComposite.getTreeViewer().refresh();
			break;
		default:
			break;
		}
	}
}