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

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.SelectionDialog;

import skillpro.model.properties.Property;
import skillpro.model.properties.PropertyType;

public class CreatePropertyDialog extends SelectionDialog {
	private final static String TITLE = "Create a Property";
	private String name;
	private PropertyType propertyType;
	private String unit;
	
	protected CreatePropertyDialog(Shell parentShell) {
		super(parentShell);
		setTitle(TITLE);
	}
	
	@Override
	public Property[] getResult() {
		if (getReturnCode() == OK) {
			return new Property[] { new Property(name, propertyType, unit) };
		}
		return new Property[] { };
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		container.setLayout(GridLayoutFactory.fillDefaults().margins(5,5).equalWidth(false).create());
		
		createPropertyFieldsComposite(container);
		
		return area;
	}

	private void createPropertyFieldsComposite(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(GridLayoutFactory.fillDefaults().equalWidth(false).numColumns(2).create());
		container.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		
		Label nameLabel = new Label(container, SWT.NONE);
		nameLabel.setLayoutData(GridDataFactory.fillDefaults().create());
		nameLabel.setText("Property Name: ");
		
		final Text nameText = new Text(container, SWT.BORDER);
		nameText.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		
		Label typeLabel = new Label(container, SWT.NONE);
		typeLabel.setLayoutData(GridDataFactory.fillDefaults().create());
		typeLabel.setText("Property Type: ");
		
		final ComboViewer propertyTypeComboViewer = new ComboViewer(container);
		propertyTypeComboViewer.getControl().setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		propertyTypeComboViewer.setContentProvider(new ArrayContentProvider());
		propertyTypeComboViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((PropertyType) element).toString();
			}
		});

		propertyTypeComboViewer.setInput(PropertyType.values());
		if (PropertyType.values().length > 0) {
			PropertyType element = PropertyType.values()[0];
			propertyTypeComboViewer.setSelection(new StructuredSelection(element));
			propertyType = element;
		}
		
		Label unitLabel = new Label(container, SWT.NONE);
		unitLabel.setLayoutData(GridDataFactory.fillDefaults().create());
		unitLabel.setText("Property Unit: ");
		
		final Text unitText = new Text(container, SWT.BORDER);
		unitText.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		
		//Listeners
		nameText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				name = nameText.getText();
				validate();
			}
		});
		
		propertyTypeComboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = propertyTypeComboViewer.getSelection();
				if (!selection.isEmpty()) {
					propertyType = (PropertyType) ((IStructuredSelection) selection).getFirstElement();
				}
				validate();
			}
		});
		
		unitText.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				unit = unitText.getText();
			}
		});
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		validate();
	}
	
	private void validate() {
		if (name != null && !name.isEmpty() && propertyType != null) {
			getOkButton().setEnabled(true);
		} else {
			getOkButton().setEnabled(false);
		}
	}
}
