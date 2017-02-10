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
import java.util.List;
import java.util.UUID;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
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
import skillpro.model.assets.ResourceConfiguration;
import skillpro.model.assets.ResourceConfigurationType;
import skillpro.model.assets.Setup;
import skillpro.model.properties.Property;
import skillpro.model.properties.PropertyDesignator;
import skillpro.model.properties.PropertyType;

public class ManageResourceConfigurationsDialog extends SelectionDialog {
	private final static String TITLE = "Manage resource configurations";
	
	private final List<ResourceConfiguration> resourceConfigurations;
	private ResourceConfiguration resourceConfiguration;
	private TableViewer tableViewer;
	private Table table;
	private ListViewer resourceConfigurationListViewer;
	
	private List<String> columnNames = Arrays.asList("Name", "Type", "Unit", "Value");
	private Text nameText;
	private final Resource resource;
	
	public static void main(String[] args) {
		Resource resource = new Resource("Resource", new ArrayList<Setup>(), null);
		
		resource.getResourceConfigurationTypes().add(new ResourceConfigurationType("Position", Arrays.asList(new Property("name", PropertyType.STRING, ""), new Property("x", PropertyType.DOUBLE, "m"), new Property("y", PropertyType.DOUBLE, "m"), new Property("z", PropertyType.DOUBLE, "m"))));
		
		resource.getResourceConfigurationTypes().add(new ResourceConfigurationType("Thing", Arrays.asList(new Property("isSplorteous", PropertyType.BOOLEAN, ""))));
		Shell parentShell = new Shell();
		ManageResourceConfigurationsDialog dialog = new ManageResourceConfigurationsDialog(parentShell, resource);
		dialog.open();
		parentShell.dispose();
	}
	
	/**
	 * Creates a dialog for the given resource.
	 * 
	 * @param parentShell the parent shell for the dialog
	 * @param resource a resource
	 */
	public ManageResourceConfigurationsDialog(Shell parentShell, Resource resource) {
		super(parentShell);
		this.resource = resource;
		setTitle(TITLE);
		resourceConfigurations = new ArrayList<>();
		for (ResourceConfiguration r : resource.getResourceConfigurations()) {
			resourceConfigurations.add(new ResourceConfiguration(r));
		}
		resourceConfiguration = resourceConfigurations.isEmpty() ? null : resourceConfigurations.get(0);
	}
	
	@Override
	protected boolean isResizable() {
		return false;
	}
	
	@Override
	public Object[] getResult() {
		if (getReturnCode() == CANCEL) {
			return new ArrayList<>().toArray();
		} else {
			List<Object> selectedObjects = new ArrayList<>();
			selectedObjects.addAll(resourceConfigurations);
			return selectedObjects.toArray();
		}
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
	
	private void createResourceConfigurationComposite(final Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		group.setLayout(GridLayoutFactory.fillDefaults().equalWidth(false).margins(10, 5).numColumns(2).create());
		group.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		group.setText("Resource Configurations");
		
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
				return ((ResourceConfiguration) element).getName();
			}
		});
		resourceConfigurationListViewer.setInput(resourceConfigurations);
		resourceConfigurationListViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				resourceConfiguration = (ResourceConfiguration) ((IStructuredSelection) resourceConfigurationListViewer.getSelection()).getFirstElement();
				if (tableViewer != null) {
					refreshEverything();
				}
			}
		});
		
		Composite buttonComposite = new Composite(group, SWT.NONE);
		buttonComposite.setLayout(GridLayoutFactory.fillDefaults().equalWidth(true).create());
		buttonComposite.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		
		Button addButton = new Button(buttonComposite, SWT.PUSH);
		addButton.setText("Add resource configuration");
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String id = UUID.randomUUID().toString();
				List<String> types = new ArrayList<>();
				for (ResourceConfigurationType t : resource.getResourceConfigurationTypes()) {
					types.add(t.getName());
				}
				SimpleDropdownDialog dialog = new SimpleDropdownDialog(parent.getShell(), "Choose a configuration type", "Configuration type:", types);
				if (dialog.open() == Window.OK) {
					ResourceConfigurationType rct = resource.getResourceConfigurationTypes().get(dialog.getResultIndex());
					ResourceConfiguration rc = new ResourceConfiguration(id, "New configuration", rct, resource);
					resourceConfigurations.add(rc);
					resourceConfiguration = rc;
					refreshEverything();
					resourceConfigurationListViewer.setSelection(new StructuredSelection(resourceConfiguration), true);
				}
			}
		});
		
		Button removeButton = new Button(buttonComposite, SWT.PUSH);
		removeButton.setText("Remove Selected");
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				resourceConfigurations.remove(((IStructuredSelection) resourceConfigurationListViewer.getSelection()).getFirstElement());
				resourceConfigurationListViewer.refresh();
			}
		});
	}
	
	private void createLowerComposite(Composite parent) {
		createNameComposite(parent);
		createTableViewer(createTable(parent));
		
		tableViewer.setContentProvider(new ContentProvider());
		tableViewer.setLabelProvider(new ExampleLabelProvider());
		tableViewer.setInput(resourceConfiguration);
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
					//FIXME delete? or use this feature later?
					@SuppressWarnings("unused")
					Property property = dialog.getResult()[0];
				}
				refreshEverything();
			}
		});
		
		Button removeButton = new Button(container, SWT.PUSH);
		removeButton.setText("Remove Selected Property");
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				resourceConfiguration.getPropertyDesignators().remove(((IStructuredSelection) tableViewer.getSelection()).getFirstElement());
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
		nameLabel.setText("Resource Configuration Name: ");
		
		nameText = new Text(container, SWT.BORDER);
		nameText.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		if (resourceConfiguration != null) {
			nameText.setText(resourceConfiguration.getName());
		}
		nameText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (resourceConfiguration != null) {
					resourceConfiguration.setName(nameText.getText());
					resourceConfigurationListViewer.refresh();
				}
			}
		});
	}
	
	private Table createTable(Composite composite) {
		int style = SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.HIDE_SELECTION;
		table = new Table(composite, style);
		
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		
		for (int i = 0; i < columnNames.size(); i++) {
			TableColumn column = new TableColumn(table, SWT.LEFT, i);
			column.setText(columnNames.get(i));
			column.setWidth(150);
		}
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
		
		CellEditor[] editors = {
			null,
			null,
			null,
			new TextCellEditor(table)
		};
		tableViewer.setCellEditors(editors);
		tableViewer.setCellModifier(new CellModifier(this));
	}
	
	private void refreshEverything() {
		tableViewer.setInput(resourceConfiguration);
		tableViewer.refresh();
		if (resourceConfiguration != null) {
			nameText.setText(resourceConfiguration.getName());
		}
	}
	
	/**
	 * InnerClass that acts as a proxy for the ResourceConfigurationType
	 * providing content for the Table. It implements the ITaskListViewer
	 * interface since it must register changeListeners with the
	 * ResourceConfiguration
	 */
	private static class ContentProvider implements IStructuredContentProvider{
		@Override
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}
		
		@Override
		public void dispose() {
		}
		
		@Override
		public Object[] getElements(Object parent) {
			if (parent == null) {
				return new ArrayList<>().toArray();
			} else {
				return ((ResourceConfiguration) parent).getPropertyDesignators().toArray();
			}
		}
	}
	
	private class ExampleLabelProvider extends LabelProvider implements ITableLabelProvider{
		@Override
		public String getColumnText(Object element, int columnIndex) {
			String result = "";
			PropertyDesignator propertyDesignator = (PropertyDesignator) element;
			switch (columnIndex) {
			case 0:
				result = propertyDesignator.getProperty().getName();
				break;
			case 1:
				result = propertyDesignator.getProperty().getType().toString();
				break;
			case 2:
				result = propertyDesignator.getProperty().getUnit();
				break;
			case 3:
				result = propertyDesignator.getValue();
				break;
			}
			return result;
		}

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	}
	
	private static class CellModifier implements ICellModifier{
		ManageResourceConfigurationsDialog manageResourceConfigurationsDialog;
		
		public CellModifier(ManageResourceConfigurationsDialog manageResourceConfigurationsDialog) {
			this.manageResourceConfigurationsDialog = manageResourceConfigurationsDialog;
		}
		
		@Override
		public Object getValue(Object element, String property) {
			int columnIndex = getColumnIndexByName(property);
			Object result = null;
			PropertyDesignator p = (PropertyDesignator) element;
			
			switch (columnIndex) {
			case 0:
				result = p.getProperty().getName();
				break;
			case 1:
				PropertyType type = p.getProperty().getType();
				result = new Integer(type == null ? 0 : type.ordinal());
				break;
			case 2:
				result = p.getProperty().getUnit();
				break;
			case 3:
				result = p.getValue();
				if (result == null) {
					result = "";
				}
			}
			return result;
		}
		
		@Override
		public boolean canModify(Object element, String property) {
			return true;
		}
		
		@Override
		public void modify(Object element, String property, Object value) {
			int columnIndex = getColumnIndexByName(property);
			
			if (element != null && columnIndex == 3) {
				TableItem item = (TableItem) element;
				PropertyDesignator p = (PropertyDesignator) item.getData();
				p.setValue((String) value);
				manageResourceConfigurationsDialog.refreshEverything();
			}
		}
		
		private int getColumnIndexByName(String columnName) {
			return manageResourceConfigurationsDialog.columnNames.indexOf(columnName);
		}
	}
	
	public List<ResourceConfiguration> getNewResourceConfigurations() {
		return resourceConfigurations;
	}
}
