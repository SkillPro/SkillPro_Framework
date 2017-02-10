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

package dummy.sendscenario;

import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.SelectionDialog;

import dummy.generate.executableskill.DummyScenario;

public class ScenarioSelectionDialog extends SelectionDialog {
	private DummyScenario chosenExSkillScenario = null;
	
	public ScenarioSelectionDialog(Shell parentShell) {
		super(parentShell);
	}
	
	//for testing purposes
	public static void main(String[] args) {
		Shell shell = new Shell();
		ScenarioSelectionDialog dialog = new ScenarioSelectionDialog(shell);
		dialog.open();
		shell.dispose();
	}
	
	public DummyScenario getChosenExSkillScenario() {
		return chosenExSkillScenario;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		container.setLayout(GridLayoutFactory.fillDefaults().margins(5, 5).equalWidth(false).create());
		
		createExecutableSkillsExportComposite(container);
		
		return area;
	}
	
	private void createExecutableSkillsExportComposite(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		group.setLayout(GridLayoutFactory.fillDefaults().equalWidth(false).margins(4, 3).create());
		group.setLayoutData(GridDataFactory.fillDefaults().hint(500, 300).grab(true, true).create());
		group.setText("Scenario List");
		
		final ListViewer availableScenarioListViewer = new ListViewer(group);
		availableScenarioListViewer.getControl().setLayoutData(GridDataFactory.fillDefaults().hint(500, 20).grab(true, true).create());
		availableScenarioListViewer.setContentProvider(new ArrayContentProvider());
		availableScenarioListViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((DummyScenario) element).getName();
			}
		});
		
		List<DummyScenario> allAvailableScenarios = DummyScenario.getAllScenarios();
		availableScenarioListViewer.setInput(allAvailableScenarios);
		
		//LISTENERS
		availableScenarioListViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) availableScenarioListViewer.getSelection();
				chosenExSkillScenario = (DummyScenario) selection.getFirstElement();
			}
		});
		if (allAvailableScenarios.size() > 0) {
			chosenExSkillScenario = allAvailableScenarios.get(0);
			availableScenarioListViewer.setSelection(new StructuredSelection(chosenExSkillScenario));
		}
	}
}