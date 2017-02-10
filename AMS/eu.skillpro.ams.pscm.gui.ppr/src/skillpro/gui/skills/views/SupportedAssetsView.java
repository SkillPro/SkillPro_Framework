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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import masterviews.dialogs.EditAllNameSelectionDialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

import skillpro.dialogs.AddRequirementPairDialog;
import skillpro.model.assets.FactoryNode;
import skillpro.model.service.SkillproService;
import skillpro.model.skills.PrePostRequirement;
import skillpro.model.skills.ResourceSkill;
import skillpro.model.skills.Skill;
import skillpro.model.skills.TemplateSkill;
import skillpro.model.update.Updatable;
import skillpro.model.update.UpdateType;
import skillpro.view.impl.SupportedAssetsTreeTableComposite;

public class SupportedAssetsView extends ViewPart implements Updatable {
	public static final String ID = SupportedAssetsView.class.getName();
	private SupportedAssetsTreeTableComposite supportedAssetsTreeTableComposite;
	
	@Override
	public void createPartControl(Composite parent) {
		createViewer(parent);
		getViewSite().getPage().addSelectionListener(supportedAssetsTreeTableComposite);
		getSite().setSelectionProvider(supportedAssetsTreeTableComposite.getTreeViewer());
		SkillproService.getUpdateManager().registerUpdatable(this, Skill.class);
		SkillproService.getUpdateManager().registerUpdatable(this, FactoryNode.class);
	}

	private void createViewer(Composite parent) {
		supportedAssetsTreeTableComposite =  new SupportedAssetsTreeTableComposite(parent, SWT.NULL) {
			@Override
			public void selectionChanged(IWorkbenchPart part, ISelection selection) {
				if (part instanceof SupportedAssetsView) {
					return;
				}
				Object currentSelection = ((IStructuredSelection) selection)
						.getFirstElement();
				Skill skill = null;
				if (currentSelection instanceof TemplateSkill) {
					skill = (TemplateSkill) currentSelection;
				} else if (currentSelection instanceof ResourceSkill) {
					skill = (ResourceSkill) currentSelection;
				}
				initInput(skill);
				viewer.refresh();
			}
			
			private void initInput(Skill skill) {
				if (skill == null) {
					return;
				}
				if (skill instanceof TemplateSkill) {
					TemplateSkill temp = (TemplateSkill) skill;
					getTreeViewer().setInput(SkillproService.getSkillproProvider()
							.getResourceSkillRepo().getCorrespondingResourceSkills(temp));
				} else if (skill instanceof ResourceSkill) {
					Set<ResourceSkill> resourceSkills = new HashSet<>();
					resourceSkills.add((ResourceSkill) skill);
					getTreeViewer().setInput(resourceSkills);
				}

			}
		};
		supportedAssetsTreeTableComposite.setLayoutData(GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).create());
		//listen to input (from AvailableSkillsView)
		supportedAssetsTreeTableComposite.setInput(new ArrayList<>());
		supportedAssetsTreeTableComposite.getTreeViewer().addDoubleClickListener(createDoubleClickListener());
	}
	
	private IDoubleClickListener createDoubleClickListener() {
		return new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				Object selection = ((StructuredSelection) supportedAssetsTreeTableComposite.getTreeViewer().getSelection()).getFirstElement();
				Shell shell = Display.getCurrent().getActiveShell();
				if (selection instanceof Skill) {
					EditAllNameSelectionDialog changeNameDialog = new EditAllNameSelectionDialog(shell, ((Skill) selection).getName());
					changeNameDialog.open();
					if (changeNameDialog.getReturnCode() == Dialog.OK) {
						((Skill) selection).setName((String) changeNameDialog.getName());
						SkillproService.getUpdateManager().notify(UpdateType.SKILL_UPDATED, Skill.class);
					}
				} else if (selection instanceof PrePostRequirement) {
					PrePostRequirement pair = (PrePostRequirement) selection;
    				AddRequirementPairDialog dialog = new AddRequirementPairDialog(shell, pair);
	        		ResourceSkill mainResourceSkill = pair.getPreRequirement().getMainResourceSkill();

    				if (dialog.open() == Window.OK) {
    					mainResourceSkill.getPrePostRequirements().remove(pair);
    					for (PrePostRequirement result : dialog.getResult()) {
    						mainResourceSkill.addPrePostRequirement(result);
    					}
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
		switch (type) {
		case NEW_DATA_IMPORTED:
			supportedAssetsTreeTableComposite.disposeAllItems();
			supportedAssetsTreeTableComposite.setInput(new ArrayList<>());
			supportedAssetsTreeTableComposite.getTreeViewer().refresh();
			break;
		case SKILL_CREATED:
			supportedAssetsTreeTableComposite.getTreeViewer().refresh();
			break;
		case SKILL_UPDATED:
			supportedAssetsTreeTableComposite.getTreeViewer().refresh();
			break;
		case SKILL_DELETED:
			supportedAssetsTreeTableComposite.disposeAllItems();
			supportedAssetsTreeTableComposite.setInput(new ArrayList<>());
			supportedAssetsTreeTableComposite.getTreeViewer().refresh();
			break;
		case ASSET_CREATED:
			supportedAssetsTreeTableComposite.disposeAllItems();
			supportedAssetsTreeTableComposite.setInput(new ArrayList<>());
			supportedAssetsTreeTableComposite.getTreeViewer().refresh();
		case ASSET_DELETED:
			supportedAssetsTreeTableComposite.disposeAllItems();
			supportedAssetsTreeTableComposite.setInput(new ArrayList<>());
			supportedAssetsTreeTableComposite.getTreeViewer().refresh();
		case ASSET_UPDATED:
			supportedAssetsTreeTableComposite.disposeAllItems();
			supportedAssetsTreeTableComposite.setInput(new ArrayList<>());
			supportedAssetsTreeTableComposite.getTreeViewer().refresh();
		case CONFIGURATION_UPDATED:
			supportedAssetsTreeTableComposite.disposeAllItems();
			supportedAssetsTreeTableComposite.setInput(new ArrayList<>());
			supportedAssetsTreeTableComposite.getTreeViewer().refresh();
		default:
			break;
		}
	}
}