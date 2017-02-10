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

package aml.gui.transformator.views;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import skillpro.model.service.SkillproService;
import skillpro.model.update.Updatable;
import skillpro.model.update.UpdateType;
import aml.domain.Domain;
import aml.transformation.service.AMLTransformationService;
import amltransformation.composites.RoleTreeTableComposite;

public class RoleView extends ViewPart implements Updatable {
	public static final String ID = RoleView.class.getName();
	private RoleTreeTableComposite domainTreeComposite;
	
	@Override
	public void createPartControl(Composite parent) {
		createViewer(parent);
		SkillproService.getUpdateManager().registerUpdatable(this, Domain.class);
	}
	
	private void createViewer(Composite parent) {
		domainTreeComposite =  new RoleTreeTableComposite(parent, SWT.NULL);
		domainTreeComposite.setLayoutData(GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).create());
		domainTreeComposite.setInput(AMLTransformationService.getAMLProvider().getAMLRoleRepo());
	}

	@Override
	public void setFocus() {
	}
	
	@Override
	public void update(UpdateType type) {
		switch (type) {
		case NEW_DATA_IMPORTED:
			domainTreeComposite.disposeAllItems();
			domainTreeComposite.setInput(AMLTransformationService.getAMLProvider().getAMLRoleRepo());
			domainTreeComposite.getTreeViewer().refresh();
		case AML_DOMAIN_CREATED:
			domainTreeComposite.getTreeViewer().refresh();
		case AML_DOMAIN_UPDATED:
			domainTreeComposite.getTreeViewer().refresh();
		case AML_DOMAIN_DELETED:
			domainTreeComposite.getTreeViewer().refresh();
		default:
			break;
		}
	}
}
