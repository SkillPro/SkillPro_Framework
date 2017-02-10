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
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;

import skillpro.model.service.SkillproService;
import skillpro.providers.problems.ProblemsTableContentProvider;
import skillpro.providers.problems.ProblemsTableLabelProvider;
import eu.skillpro.ams.pscm.gui.masterviews.Activator;

public class ProblemsTableComposite extends TableComposite {
	public ProblemsTableComposite(Composite parent, int style) {
		super(parent, SWT.MULTI);
		
	}
	
	@Override
	protected void addCoolbarItems(Composite parent) {
		ToolBar coolToolBar = new ToolBar(parent, SWT.VERTICAL);
		// the default cool bar items.
		createToolItem(coolToolBar, SWT.VERTICAL, "R", Activator.getImageDescriptor("icons/refresh.png").createImage(), "Refreshes the Viewer", refreshSelectionListener());
	}
	
	private SelectionListener refreshSelectionListener() {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				refreshViewer();
			}
		};
	}
	
	@Override
	public void refreshViewer() {
		disposeAllItems();
		SkillproService.getSkillproProvider().getProblemRepo().checkForProblems();
		viewer.refresh();
	}

	@Override
	protected void createColumns(TableViewer viewer) {
		String[] headers = { "Description", "TBD" };

		for (int i = 0; i < headers.length; i++) {
			TableViewerColumn col = new TableViewerColumn(viewer, SWT.NONE);
			col.getColumn().setText(headers[i]);
			col.getColumn().setWidth(150);
			col.getColumn().setResizable(true);
			col.getColumn().setMoveable(true);
		}

		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLinesVisible(true);
		
	}

	@Override
	protected ITableLabelProvider initLabelProvider() {
		return new ProblemsTableLabelProvider();
	}

	@Override
	protected IContentProvider initContentProvider() {
		return new ProblemsTableContentProvider();
	}

	@Override
	protected void initInput() {
		viewer.setInput(SkillproService.getSkillproProvider().getProblemRepo().getEntities());
	}
}
