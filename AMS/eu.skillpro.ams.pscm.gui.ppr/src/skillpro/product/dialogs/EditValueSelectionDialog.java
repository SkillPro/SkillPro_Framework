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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.SelectionDialog;

import skillpro.model.properties.PropertyDesignator;
import skillpro.model.properties.PropertyType;

public class EditValueSelectionDialog extends SelectionDialog {
	private final static String TITLE = "Change Value";
	
	private PropertyDesignator propertyDesignator;
	private String value;
	private Text text;
	
	public EditValueSelectionDialog(Shell parentShell) {
		super(parentShell);
		setTitle(TITLE);
	}
	
	public EditValueSelectionDialog(Shell parentShell, PropertyDesignator propertyDesignator) {
		super(parentShell);
		this.propertyDesignator = propertyDesignator;
		value = propertyDesignator.getValue();
		setTitle(TITLE);
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
		selectedObjects.add(value);
		return selectedObjects.toArray();
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Control area = super.createDialogArea(parent);
		parent.setLayout(GridLayoutFactory.fillDefaults().margins(5, 5).numColumns(2).create());
		parent.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		Label valueLabel = new Label(parent, SWT.NONE);
		valueLabel.setText("Value");
		valueLabel.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());

		text = new Text(parent, SWT.BORDER);
		text.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		if (value != null) {
			text.setText(value);
		}
		
		text.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				value = text.getText();
				if (value.length() > 0) {
					if (propertyDesignator.getProperty().getType() == PropertyType.INTEGER) {
						getOkButton().setEnabled(isInteger(value));
					} else if (propertyDesignator.getProperty().getType() == PropertyType.DOUBLE) {
						getOkButton().setEnabled(isDouble(value));
					} else if (propertyDesignator.getProperty().getType() == PropertyType.BOOLEAN) {
						getOkButton().setEnabled(value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false"));
					} else {
						getOkButton().setEnabled(true);
					}
				} else {
					getOkButton().setEnabled(false);
				}
			}
		});
		return area;
	}
	
	public String getValue() {
		return value;
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