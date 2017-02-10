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

package amltransformation.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.SelectionDialog;

import transformation.interfaces.ITransformable;
import aml.skillpro.transformation.util.TransformationUtil;
import amltransformation.composites.TransformableTreeComposite;

public class SetElementTransformableDialog extends SelectionDialog {
	private final static String TITLE = "Select what to transform the element into";
	private List<Class<?>> transformables = new ArrayList<>();
	private Object selectedTransformable;
	
	public SetElementTransformableDialog(Shell parentShell, Class<? extends ITransformable> trans) {
		super(parentShell);
		setTitle(TITLE);
		transformables.addAll(TransformationUtil.getAllModelsImplementingTransformable(trans));
	}
	
	@Override
	protected boolean isResizable() {
		return false;
	}
	
	@Override
	public Object[] getResult() {
		if (getReturnCode() == OK) {
			return new Object[]{ selectedTransformable };
		}
		return new ArrayList<>().toArray();
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Control area = super.createDialogArea(parent);
		
		parent.setLayout(GridLayoutFactory.fillDefaults().margins(5, 5).numColumns(1).create());
		parent.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		
		TransformableTreeComposite transformableComposite = new TransformableTreeComposite(parent, SWT.NONE);
		transformableComposite.setInput(transformables);
		transformableComposite.getTreeViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				selectedTransformable = ((TreeSelection) event.getSelection()).getFirstElement();
				getOkButton().setEnabled(selectedTransformable != null);
			}
		});
		
		transformableComposite.getTreeViewer().addDoubleClickListener(new IDoubleClickListener() {
			
			@Override
			public void doubleClick(DoubleClickEvent event) {
				if (selectedTransformable instanceof Class<?>) {
					okPressed();
				}
			}
		});
		return area;
	}
	
	public Object getSelectedTransformable() {
		return selectedTransformable;
	}
}