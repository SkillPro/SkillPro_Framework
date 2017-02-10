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

package skillpro.dialogs;

import java.util.Collection;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.SelectionDialog;

import skillpro.view.impl.ExecutableSkillTreeComposite;

public class ExecutableSkillsSelectionDialog extends SelectionDialog {

	private TreeViewer treeViewer;
	private Collection<?> input;
	
	public ExecutableSkillsSelectionDialog(Shell parentShell, Collection<?> input, String title) {
		super(parentShell);
		this.input = input;
		setTitle(title);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		area.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(GridDataFactory.fillDefaults().hint(480, 480).grab(true, true).create());
		container.setLayout(GridLayoutFactory.fillDefaults().margins(5,5).equalWidth(false).numColumns(1).create());
		
		createExecutableSkillComposite(container);
		return container;
	}
	
	private void createExecutableSkillComposite(Composite parent) {
		ExecutableSkillTreeComposite exSkillComposite = new ExecutableSkillTreeComposite(parent, SWT.SINGLE);
		exSkillComposite.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		exSkillComposite.setInput(input);
		treeViewer = exSkillComposite.getTreeViewer();
		treeViewer.setComparator(null);
	}
	
	
	
	public TreeViewer getTreeViewer() {
		return treeViewer;
	}
}
