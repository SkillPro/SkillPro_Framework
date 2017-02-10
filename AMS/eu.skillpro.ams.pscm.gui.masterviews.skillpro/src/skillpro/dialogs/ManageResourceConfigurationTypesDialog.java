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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.SelectionDialog;

import skillpro.model.assets.Resource;
import skillpro.model.assets.ResourceConfigurationType;
import skillpro.model.properties.Property;
import skillpro.model.properties.PropertyType;

public class ManageResourceConfigurationTypesDialog extends SelectionDialog {
	
	private final static String TITLE = "Manage resource configuration types";
	
	private final List<ResourceConfigurationType> resourceConfigurationsTypes = new ArrayList<>();
	private ResourceConfigurationType resourceConfigurationType;
	private TableViewer tableViewer;
	private Table table;
	
	private List<String> columnNames = Arrays.asList("Name", "Type", "Unit");

	private Text nameText;

	private ListViewer resourceConfigurationListViewer;
	
	/**
	 * Creates a dialog for the given resource.
	 * 
	 * @param parentShell the parent shell for the dialog
	 * @param resource a resource
	 */
	public ManageResourceConfigurationTypesDialog(Shell parentShell, Resource resource) {
		super(parentShell);
		setTitle(TITLE);
		
		for (ResourceConfigurationType r : resource.getResourceConfigurationTypes()) {
			resourceConfigurationsTypes.add(new ResourceConfigurationType(r));
		}
		resourceConfigurationType = resourceConfigurationsTypes.isEmpty() ? null : resourceConfigurationsTypes.get(0);
	}
	
	@Override
	protected boolean isResizable() {
		return false;
	}
	
	@Override
	public Object[] getResult() {
		if (getReturnCode() == CANCEL) {
			return new ArrayList<>().toArray();
		}
		List<Object> selectedObjects = new ArrayList<>();
		selectedObjects.addAll(resourceConfigurationsTypes);
		
		return selectedObjects.toArray();
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		container.setLayout(GridLayoutFactory.fillDefaults().margins(5, 5).equalWidth(false).create());
		
		createResourceConfigurationComposite(container);
		createLowerComposite(container);
		
		return area;
	}
	
	private void createResourceConfigurationComposite(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		group.setLayout(GridLayoutFactory.fillDefaults().equalWidth(false).margins(10, 5).numColumns(2).create());
		group.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		group.setText("Resource Configuration Type");
		
		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		gd.minimumWidth = 500;
		gd.minimumHeight = 40;
		
		resourceConfigurationListViewer = new ListViewer(group);
		resourceConfigurationListViewer.getControl().setLayoutData(gd);
		resourceConfigurationListViewer.setContentProvider(new ArrayContentProvider());
		resourceConfigurationListViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((ResourceConfigurationType) element).getName();
			}
		});
		resourceConfigurationListViewer.setInput(resourceConfigurationsTypes);
		resourceConfigurationListViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				resourceConfigurationType = (ResourceConfigurationType) ((IStructuredSelection) resourceConfigurationListViewer.getSelection())
						.getFirstElement();
				if (tableViewer != null) {
					refreshEverything();
				}
			}

		});
		
		Composite buttonComposite = new Composite(group, SWT.NONE);
		buttonComposite.setLayout(GridLayoutFactory.fillDefaults().equalWidth(true).create());
		buttonComposite.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		
		Button addButton = new Button(buttonComposite, SWT.PUSH);
		addButton.setText("Add Resource Configuration");
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ResourceConfigurationType rct = new ResourceConfigurationType("New configuration type", new ArrayList<Property>());
				resourceConfigurationsTypes.add(rct);
				resourceConfigurationListViewer.refresh();
			}
		});
		
		Button removeButton = new Button(buttonComposite, SWT.PUSH);
		removeButton.setText("Remove Selected");
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				resourceConfigurationsTypes.remove(((IStructuredSelection) resourceConfigurationListViewer.getSelection()).getFirstElement());
				resourceConfigurationListViewer.refresh();
			}
		});
		
	}
	
	private void createLowerComposite(Composite parent) {
		createNameComposite(parent);
		// Create the table
		createTableViewer(createTable(parent));
		
		tableViewer.setContentProvider(new ExampleContentProvider());
		tableViewer.setLabelProvider(new ExampleLabelProvider());
		
		tableViewer.setInput(resourceConfigurationType);
		// Create and setup the TableViewer
		createAddButtonPropertyComposite(parent);
	}
	
	private void createAddButtonPropertyComposite(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(GridLayoutFactory.fillDefaults().equalWidth(false).margins(10, 5).numColumns(2).create());
		container.setLayoutData(GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.TOP).grab(true, true).create());
		
		final Button addButton = new Button(container, SWT.PUSH);
		addButton.setText("Add Property");
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				CreatePropertyDialog dialog = new CreatePropertyDialog(addButton.getShell());
				if (dialog.open() == Dialog.OK) {
					Property property = dialog.getResult()[0];
					resourceConfigurationType.addProperty(property);
				}
				refreshEverything();
			}
		});
		
		Button removeButton = new Button(container, SWT.PUSH);
		removeButton.setText("Remove Selected Property");
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				resourceConfigurationType.getProperties().remove(((IStructuredSelection) tableViewer.getSelection()).getFirstElement());
				refreshEverything();
			}
		});
	}
	
	private void createNameComposite(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(GridLayoutFactory.fillDefaults().equalWidth(false).numColumns(2).create());
		container.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		
		Label nameLabel = new Label(container, SWT.NONE);
		nameLabel.setLayoutData(GridDataFactory.fillDefaults().create());
		nameLabel.setText("Resource Configuration Type Name: ");
		
		nameText = new Text(container, SWT.BORDER);
		nameText.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		if (resourceConfigurationType != null) {
			nameText.setText(resourceConfigurationType.getName());
		}
		nameText.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				if (resourceConfigurationType != null) {
					resourceConfigurationType.setName(nameText.getText());
					resourceConfigurationListViewer.refresh();
				}
			}
		});
	}
	
	private Table createTable(Composite composite) {
		int style = SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | 
				SWT.FULL_SELECTION | SWT.HIDE_SELECTION;
		table = new Table(composite, style);

		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		
		TableColumn column1 = new TableColumn(table, SWT.LEFT, 0);
		column1.setText(columnNames.get(0));
		column1.setWidth(200);

		TableColumn column2 = new TableColumn(table, SWT.LEFT, 1);
		column2.setText(columnNames.get(1));
		column2.setWidth(200);
		
		TableColumn column3 = new TableColumn(table, SWT.LEFT, 2);
		column3.setText(columnNames.get(2));
		column3.setWidth(200);
		
		return table;
	}
	
	/**
	 * Create the TableViewer
	 */
	private void createTableViewer(Table table) {
		tableViewer = new TableViewer(table);
		tableViewer.setUseHashlookup(true);
		tableViewer.setColumnProperties(columnNames.toArray(new String[0]));
		tableViewer.getControl().setLayoutData(GridDataFactory.fillDefaults().hint(500, 160).grab(true, true).create());
		
		// Create the cell editors
		CellEditor[] editors = new CellEditor[columnNames.size()];
		// Column 1 : Completed (Checkbox)
		editors[0] = new TextCellEditor(table);
		// Column 2 : Description (Free text)
		TextCellEditor textEditor = new TextCellEditor(table);
		((Text) textEditor.getControl()).setTextLimit(60);
		String[] typeString = new String[PropertyType.values().length];
		for (int i = 0; i < typeString.length; i++) {
			typeString[i] = PropertyType.values()[i].toString();
		}
		
		editors[1] = new ComboBoxCellEditor(table, typeString, SWT.READ_ONLY);
		editors[2] = new TextCellEditor(table);
		
		// Assign the cell editors to the viewer
		tableViewer.setCellEditors(editors);
		// Set the cell modifier for the viewer
		tableViewer.setCellModifier(new ExampleCellModifier(this));
		// Set the default sorter for the viewer
	}
	
	private void refreshEverything() {
		tableViewer.setInput(resourceConfigurationType);
		tableViewer.refresh();
		if (resourceConfigurationType != null) {
			nameText.setText(resourceConfigurationType.getName());
		}
	}
	
	/**
	 * InnerClass that acts as a proxy for the ResourceConfigurationType
	 * providing content for the Table. It implements the ITaskListViewer
	 * interface since it must register changeListeners with the
	 * ResourceConfigurationType
	 */
	class ExampleContentProvider implements IStructuredContentProvider  {
		
		
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}
		
		public void dispose() {
		}
		
		// Return the tasks as an array of Objects
		public Object[] getElements(Object parent) {
			if (parent == null) {
				return new ArrayList<>().toArray();
			} else if (parent instanceof ResourceConfigurationType) {
				return ((ResourceConfigurationType) parent).getProperties().toArray();
			} else if (parent instanceof Collection<?>) {
				return ((Collection<?>) parent).toArray();
			}	
			return null;
		}
	}
	
	private class ExampleLabelProvider extends LabelProvider implements ITableLabelProvider{
		public String getColumnText(Object element, int columnIndex) {
			String result = "";
			Property property = (Property) element;
			switch (columnIndex) {
			case 0:
				result = property.getName();
				break;
			case 1:
				result = property.getType().toString();
				break;
			case 2:
				result = property.getUnit();
				break;
			}
			return result;
		}
		
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	}
	
	private static class ExampleCellModifier implements ICellModifier{
		ManageResourceConfigurationTypesDialog manageResourceConfigurationTypesDialog;
		
		public ExampleCellModifier(ManageResourceConfigurationTypesDialog manageResourceConfigurationTypesDialog) {
			this.manageResourceConfigurationTypesDialog = manageResourceConfigurationTypesDialog;
		}
		
		@Override
		public Object getValue(Object element, String property) {
			// Find the index of the column
			int columnIndex = manageResourceConfigurationTypesDialog.columnNames.indexOf(property);
			
			Object result = null;
			Property p = (Property) element;
			
			switch (columnIndex) {
			case 0:
				result = p.getName();
				break;
			case 1:
				int i = 0;
				for (PropertyType t : PropertyType.values()) {
					if (t == p.getType()) {
						break;
					}
					i++;
				}
				result = new Integer(i);
				break;
			case 2:
				result = p.getUnit();
			}
			return result;
		}
		
		@Override
		public boolean canModify(Object element, String property) {
			return true;
		}
		
		@Override
		public void modify(Object element, String property, Object value) {	
			// Find the index of the column 
			int columnIndex	= manageResourceConfigurationTypesDialog.columnNames.indexOf(property);
			
			if (element != null) {
				TableItem item = (TableItem) element;
				Property p = (Property) item.getData();
				
				switch (columnIndex) {
				case 0:
					p.setName((String) value);
					break;
				case 1:
					p.setType(PropertyType.values()[((Integer) value).intValue()]);
					break;
				case 2:
					p.setUnit((String) value);
					break;
				default:
				}
				manageResourceConfigurationTypesDialog.refreshEverything();
			}
		}
	}
	
	public List<ResourceConfigurationType> getNewResourceConfigurationTypes() {
		return resourceConfigurationsTypes;
	}
}
