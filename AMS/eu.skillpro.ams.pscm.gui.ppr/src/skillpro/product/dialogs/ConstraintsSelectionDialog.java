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

package skillpro.product.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.SelectionDialog;

import skillpro.model.properties.PropertyConstraint;
import skillpro.model.properties.PropertyConstraintNominal;
import skillpro.model.properties.PropertyConstraintOrdinal;
import skillpro.model.properties.PropertyDesignator;
import skillpro.model.properties.PropertyType;

public class ConstraintsSelectionDialog extends SelectionDialog {
	private final static String TITLE = "Add Constraints";
	
	private PropertyDesignator propertyDesignator;
	private PropertyConstraintNominal nominalConstraint;
	private PropertyConstraintOrdinal ordinalConstraint;
	private String nominalName = "";
	private String ordinalName = "";
	private String maxValue = "";
	private String minValue = "";
	private String requiredValue = "";
	private List<String> values = new ArrayList<>();
	
	boolean isOrdinal = false;
	boolean isNominal = false;
	
	public ConstraintsSelectionDialog(Shell parentShell, PropertyDesignator propertyDesignator) {
		super(parentShell);
		this.propertyDesignator = propertyDesignator;
		setTitle(TITLE);
	}
	
	@Override
	protected boolean isResizable() {
		return false;
	}
	
	@Override
	public PropertyDesignator[] getResult() {
		if (getReturnCode() == CANCEL) {
			return new PropertyDesignator[]{ };
		}
		PropertyDesignator designator = new PropertyDesignator(propertyDesignator.getProperty(), propertyDesignator.getSkill(), propertyDesignator.getValue());
		if (isOrdinal) {
			if (maxValue.isEmpty()) {
				maxValue = Double.MAX_VALUE + "";
			}
			
			if (minValue.isEmpty()) {
				minValue = Double.MIN_VALUE + "";
			}
			ordinalConstraint = new PropertyConstraintOrdinal(ordinalName, Double.parseDouble(maxValue), Double.parseDouble(minValue)
					, requiredValue);
			designator.addConstraint(ordinalConstraint);
			if (nominalConstraint != null) {
				designator.addConstraint(nominalConstraint);
			}
		} else {
			if (isNominal) {
				nominalConstraint = new PropertyConstraintNominal(nominalName, values);
				designator.addConstraint(nominalConstraint);
				if (ordinalConstraint != null) {
					designator.addConstraint(ordinalConstraint);
				}
			}
		}
		return new PropertyDesignator[] { designator };
	}
	
	private boolean stringNotEmptyAndNull(String text) {
		return text != null && !text.isEmpty();
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Control area = super.createDialogArea(parent);
		if (propertyDesignator.getConstraints().size() > 2) {
			throw new IllegalArgumentException("Should not be possible to have more than 2 constraints.");
		}
		for (PropertyConstraint cons : this.propertyDesignator.getConstraints()) {
			if (cons instanceof PropertyConstraintOrdinal) {
				ordinalConstraint = (PropertyConstraintOrdinal) cons;
			} else {
				nominalConstraint = (PropertyConstraintNominal) cons;
			}
		}
		
		parent.setLayout(GridLayoutFactory.fillDefaults().margins(5, 5).numColumns(1).create());
		GridData gridData = GridDataFactory.fillDefaults().grab(true, true).create();
		GridData optionData = GridDataFactory.fillDefaults().grab(true, true).create();
		parent.setLayoutData(gridData);
		
		PropertyType type = propertyDesignator.getProperty().getType();
		
		Composite optionButtonsComp = new Composite(parent, SWT.NONE);
		optionButtonsComp.setLayout(GridLayoutFactory.fillDefaults().margins(5, 5).numColumns(2).create());
		optionButtonsComp.setLayoutData(gridData);
		
		Button nominalButton = new Button(optionButtonsComp, SWT.RADIO);
		nominalButton.setText("Nominal Constraint");
		nominalButton.setLayoutData(optionData);
		
		Button ordinalButton = new Button(optionButtonsComp, SWT.RADIO);
		ordinalButton.setText("Ordinal Constraint");
		ordinalButton.setLayoutData(optionData);

		if (type == PropertyType.BOOLEAN || type == PropertyType.STRING) {
			nominalButton.setSelection(true);
			ordinalButton.setEnabled(false);
			isNominal = true;
			isOrdinal = false;
		} else {
			ordinalButton.setSelection(true);
			isOrdinal = true;
			isNominal = false;
		}
		
		final Composite placeholderComp = new Composite(parent, SWT.NONE);
		
		final StackLayout stackLayout = new StackLayout();
		placeholderComp.setLayout(stackLayout);
		placeholderComp.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());

		final Composite nominalComposite = getNominalComposite(placeholderComp);
		final Composite ordinalComposite = getOrdinalComposite(placeholderComp);
		
		if (isNominal) {
			stackLayout.topControl = nominalComposite;
			placeholderComp.layout();
		} else {
			stackLayout.topControl = ordinalComposite;
			placeholderComp.layout();
		}
		
		ordinalButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				isOrdinal = true;
				isNominal = false;
				stackLayout.topControl = ordinalComposite;
				placeholderComp.layout();
				refreshOkButton();
			}
		});

		nominalButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				isNominal = true;
				isOrdinal = false;
				stackLayout.topControl = nominalComposite;
				placeholderComp.layout();
				refreshOkButton();
			}
		});
		
		return area;
	}
	
	private Composite getOrdinalComposite(Composite parent) {
		GridData textData = GridDataFactory.fillDefaults().grab(true, false).minSize(120, 20).create();
		GridData gridData = GridDataFactory.fillDefaults().grab(true, false).create();

		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(GridLayoutFactory.fillDefaults().margins(5, 5).numColumns(2).create());
		comp.setLayoutData(gridData);
		
		Label nameLabel = new Label(comp, SWT.NONE);
		nameLabel.setText("Name");
		nameLabel.setLayoutData(gridData);
		final Text nameText = new Text(comp, SWT.BORDER);
		nameText.setLayoutData(textData);
		ordinalName = "Constraint";
		nameText.setText(ordinalName);
		
		Label maxValueLabel = new Label(comp, SWT.NONE);
		maxValueLabel.setText("Max Value");
		maxValueLabel.setLayoutData(gridData);
		final Text maxValueText = new Text(comp, SWT.BORDER);
		maxValueText.setLayoutData(textData);
		
		Label minValueLabel = new Label(comp, SWT.NONE);
		minValueLabel.setText("Min Value");
		minValueLabel.setLayoutData(gridData);
		final Text minValueText = new Text(comp, SWT.BORDER);
		minValueText.setLayoutData(textData);
		
		Label requiredValueLabel = new Label(comp, SWT.NONE);
		requiredValueLabel.setText("Required Value");
		requiredValueLabel.setLayoutData(gridData);
		final Text requiredValueText = new Text(comp, SWT.BORDER);
		requiredValueText.setLayoutData(textData);
		
		if (ordinalConstraint != null && isOrdinal) {
			ordinalName = ordinalConstraint.getName();
			maxValue = ordinalConstraint.getMaxValue() + "";
			minValue = ordinalConstraint.getMinValue() + "";
			requiredValue = ordinalConstraint.getRequiredValue();
			nameText.setText(ordinalName);
			maxValueText.setText(maxValue);
			minValueText.setText(minValue);
			requiredValueText.setText(requiredValue);
		}
		nameText.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				ordinalName = nameText.getText();
				refreshOkButton();
			}
		});
		maxValueText.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				maxValue = maxValueText.getText();
				refreshOkButton();
			}
		});
		minValueText.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				minValue = minValueText.getText();
				refreshOkButton();
			}
		});
		requiredValueText.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				requiredValue = requiredValueText.getText();
				refreshOkButton();
				
			}
		});
		return comp;
	}
	
	private Composite getNominalComposite(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(GridLayoutFactory.fillDefaults().margins(5, 5).numColumns(2).create());
		GridData gridData = GridDataFactory.fillDefaults().grab(true, true).create();
		comp.setLayoutData(gridData);
		
		Label nameLabel = new Label(comp, SWT.NONE);
		nameLabel.setText("Name");
		nameLabel.setLayoutData(gridData);
		final Text nameText = new Text(comp, SWT.BORDER);
		nameText.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).minSize(180, 20).create());
		nominalName = "Constraint";
		nameText.setText(nominalName);

		Composite valuesComposite = new Composite(comp, SWT.NONE);
		valuesComposite.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).create());
		valuesComposite.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).span(2, 1).create());
		
		Label valuesLabel = new Label(valuesComposite, SWT.NONE);
		valuesLabel.setText("Possible Values: ");
		valuesLabel.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).span(2, 1).create());
		final Text valuesText = new Text(valuesComposite, SWT.BORDER);
		valuesText.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).minSize(80, 20).create());
		final Button valuesButton = new Button(valuesComposite, SWT.NONE);
		valuesButton.setText("Add");
		valuesButton.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		
		
		final ListViewer valuesList = new ListViewer(comp);
		valuesList.getControl().setLayoutData(GridDataFactory.fillDefaults().grab(true, true).hint(comp.getSize().x, 60).minSize(comp.getSize().x, 60).span(2, 1).create());
		valuesList.getControl().addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
				ISelection selection = valuesList.getSelection();
				if (e.keyCode == SWT.DEL && (IStructuredSelection) selection != null) {
					Object selectedElement = ((IStructuredSelection) selection).getFirstElement();
					values.remove(selectedElement);
					valuesList.refresh();
					refreshOkButton();
				}
				
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
			}
		});
		valuesList.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return element.toString();
			}
		});
		valuesList.setContentProvider(new ArrayContentProvider());
		valuesList.setInput(values);
		
		if (nominalConstraint != null && isNominal) {
			nominalName = nominalConstraint.getName();
			nameText.setText(nominalName);
			values.addAll(nominalConstraint.getValues());
			valuesList.refresh();
		}
		nameText.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				nominalName = nameText.getText();
				refreshOkButton();
			}
		});
		if (valuesText.getText().isEmpty()) {
			valuesButton.setEnabled(false);
		}
		valuesText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (valuesText.getText().isEmpty()) {
					valuesButton.setEnabled(false);
				} else {
					valuesButton.setEnabled(true);
				}
			}
		});
		valuesButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				values.add(valuesText.getText());
				valuesList.refresh();
				refreshOkButton();
			}
		});
		
		return comp;
	}
	
	private void refreshOkButton() {
		if (isOrdinal) {
			boolean enable = false;
			enable = stringNotEmptyAndNull(ordinalName) && (stringNotEmptyAndNull(maxValue)
					|| stringNotEmptyAndNull(minValue) || stringNotEmptyAndNull(requiredValue));
			if (propertyDesignator.getProperty().getType() == PropertyType.INTEGER) {
				enable = enable && (emptyOrInteger(maxValue) && emptyOrInteger(minValue) && emptyOrInteger(requiredValue));
			} else {
				enable = enable && (emptyOrDouble(maxValue) && emptyOrDouble(minValue) && emptyOrDouble(requiredValue));
			}
			getOkButton().setEnabled(enable);
			
		} else {
			getOkButton().setEnabled(stringNotEmptyAndNull(nominalName) && !values.isEmpty());
		}
	}
	
	private boolean emptyOrInteger(String string) {
		return string.isEmpty() || isInteger(string);
	}
	
	private boolean emptyOrDouble(String string) {
		return string.isEmpty() || isDouble(string);
	}
	
	private boolean isInteger(String string) {
		try {
			Integer.parseInt(string);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}
	
	private boolean isDouble(String string) {
		try {
			Double.parseDouble(string);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}
}