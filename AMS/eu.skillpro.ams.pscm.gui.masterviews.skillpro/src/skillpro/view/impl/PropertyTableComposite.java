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

package skillpro.view.impl;

import masterviews.composite.abstracts.TableComposite;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;

import skillpro.model.skills.ProductionSkill;
import skillpro.model.skills.ResourceSkill;
import skillpro.model.skills.Skill;
import skillpro.model.skills.TemplateSkill;
import skillpro.providers.property.PropertyTableContentProvider;
import skillpro.providers.property.PropertyTableLabelProvider;

public class PropertyTableComposite extends TableComposite implements ISelectionListener {
	private Skill currentSkill = null;
	
	public PropertyTableComposite(Composite parent, int style) {
		super(parent, style);
	}

	@Override
	protected void createColumns(TableViewer viewer) {
		String[] headers = { "Name", "Type", "Value", "Unit", "Constraint" };

		for (int i = 0; i < headers.length; i++) {
			TableViewerColumn col = new TableViewerColumn(viewer, SWT.NONE);
			col.getColumn().setText(headers[i]);
			switch (headers[i]) {
			case "Type":
				col.getColumn().setWidth(70);
			case "Value":
				col.getColumn().setWidth(100);
				break;
			case "Unit": 
				col.getColumn().setWidth(75);
				break;
			default:
				col.getColumn().setWidth(160);
			}
			col.getColumn().setResizable(true);
			col.getColumn().setMoveable(true);
		}

		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLinesVisible(true);
	}

	@Override
	protected ITableLabelProvider initLabelProvider() {
		return new PropertyTableLabelProvider();
	}

	@Override
	protected IContentProvider initContentProvider() {
		return new PropertyTableContentProvider();
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		Object currentSelection = ((IStructuredSelection) selection)
				.getFirstElement();
		if (currentSelection instanceof TemplateSkill) {
			currentSkill = (TemplateSkill) currentSelection;
		} else if (currentSelection instanceof ResourceSkill) {
			currentSkill = (ResourceSkill) currentSelection;
		} else if (currentSelection instanceof ProductionSkill) {
			currentSkill = (ProductionSkill) currentSelection;
		}
		disposeAllItems();
		initInput(currentSkill);
		viewer.refresh();
		
	}
	
	public Skill getCurrentSkill() {
		return currentSkill;
	}
	
	private void initInput(Skill skill) {
		viewer.setInput(skill);
	}
}
