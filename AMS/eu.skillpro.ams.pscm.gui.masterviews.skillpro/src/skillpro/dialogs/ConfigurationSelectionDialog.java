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

import masterviews.composite.abstracts.TreeComposite;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.SelectionDialog;

import skillpro.model.assets.Resource;
import skillpro.model.assets.ResourceConfiguration;

public class ConfigurationSelectionDialog extends SelectionDialog {
	private Resource resource;
	private ResourceConfiguration selectedResourceConfiguration;
	
	public ConfigurationSelectionDialog(Shell parentShell, Resource resource) {
		super(parentShell);
		this.resource = resource;
	}
	
	@Override
	public ResourceConfiguration[] getResult() {
		return new ResourceConfiguration[] { selectedResourceConfiguration };
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayout(GridLayoutFactory.fillDefaults().margins(5, 5).numColumns(1).create());
		container.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		
		final TreeComposite treeComp = new TreeComposite(container, SWT.NONE) {
			@Override
			protected LabelProvider initLabelProvider() {
				return new LabelProvider() {
					@Override
					public String getText(Object element) {
						if (element instanceof ResourceConfiguration) {
							return ((ResourceConfiguration) element).getName();
						}
						return "Fix this";
					}
				};
			}
			
			@Override
			protected IContentProvider initContentProvider() {
				return new ITreeContentProvider() {
					
					@Override
					public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
					}
					
					@Override
					public void dispose() {
					}
					
					@Override
					public boolean hasChildren(Object element) {
						return false;
					}
					
					@Override
					public Object getParent(Object element) {
						return null;
					}
					
					@Override
					public Object[] getElements(Object inputElement) {
						if (inputElement instanceof Collection<?>) {
							return ((Collection<?>) inputElement).toArray();
						}
						return null;
					}
					
					@Override
					public Object[] getChildren(Object parentElement) {
						return null;
					}
				};
			}
		};
		
		treeComp.setInput(resource.getResourceConfigurations());
		
		treeComp.getTreeViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				selectedResourceConfiguration = (ResourceConfiguration) ((IStructuredSelection) treeComp.getTreeViewer().getSelection()).getFirstElement();
			}
		});
		
		treeComp.getTreeViewer().addDoubleClickListener(new IDoubleClickListener() {
			
			@Override
			public void doubleClick(DoubleClickEvent event) {
				if (selectedResourceConfiguration != null) {
					okPressed();
				}
				
			}
		});
		
		return area;
	}
}
