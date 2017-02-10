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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ViewPart;

import skillpro.model.properties.PropertyDesignator;
import skillpro.model.service.SkillproService;
import skillpro.model.skills.ProductionSkill;
import skillpro.model.skills.ResourceSkill;
import skillpro.model.skills.Skill;
import skillpro.model.update.UpdateType;
import skillpro.product.dialogs.ConstraintsSelectionDialog;
import skillpro.product.dialogs.EditValueSelectionDialog;
import skillpro.view.impl.PropertyTableComposite;

public class AttributeTableView extends ViewPart {
	public static final String ID = AttributeTableView.class.getName();
	private PropertyTableComposite propertyTableComposite;
	
	@Override
	public void createPartControl(Composite parent) {
		propertyTableComposite = new PropertyTableComposite(parent, SWT.NONE);
		propertyTableComposite.getViewer().addDoubleClickListener(createDoubleClickListener());
		
		getViewSite().getPage().addSelectionListener(propertyTableComposite);
	}

	private IDoubleClickListener createDoubleClickListener() {
		return new IDoubleClickListener() {
			
			@Override
			public void doubleClick(DoubleClickEvent event) {
				Object selection = ((StructuredSelection) propertyTableComposite.getViewer().getSelection()).getFirstElement();
				if (selection instanceof PropertyDesignator 
						&& propertyTableComposite.getCurrentSkill() instanceof ProductionSkill) {
					PropertyDesignator des = (PropertyDesignator) selection;
					EditValueSelectionDialog valueDialog = new EditValueSelectionDialog(Display.getCurrent().getActiveShell(), des);
					
					if (valueDialog.open() == Dialog.OK) {
						des.setValue(valueDialog.getValue());
					}
				} else if (selection instanceof PropertyDesignator 
						&& propertyTableComposite.getCurrentSkill() instanceof ResourceSkill) {
					PropertyDesignator des = (PropertyDesignator) selection;
					ConstraintsSelectionDialog constraintDialog = new ConstraintsSelectionDialog(Display.getCurrent().getActiveShell(), des);
					if (constraintDialog.open() == Dialog.OK) {
						if (constraintDialog.getResult() != null && constraintDialog.getResult().length == 1) {
							PropertyDesignator resultDes = constraintDialog.getResult()[0];
							des.getConstraints().clear();
							des.getConstraints().addAll(resultDes.getConstraints());
							des.setSkill(resultDes.getSkill());
							des.setProperty(resultDes.getProperty());
							SkillproService.getUpdateManager().notify(UpdateType.SKILL_UPDATED, Skill.class);
						} else {
							throw new IllegalArgumentException("Please fix code");
						}
					}
				}
				propertyTableComposite.getViewer().refresh();
			}
		};
	}

	@Override
	public void setFocus() {
	}
}