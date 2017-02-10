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

package masterviews.dialogs;

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

public class EditAllNameSelectionDialog extends SelectionDialog {
	private final static String TITLE = "Change Name";
	private String name;
	
	public EditAllNameSelectionDialog(Shell parentShell) {
		this(parentShell, "");
	}
	
	public EditAllNameSelectionDialog(Shell parentShell, String oldName) {
		super(parentShell);
		this.name = oldName;
		setTitle(TITLE);
	}
	
	@Override
	protected boolean isResizable() {
		return false;
	}
	
	@Override
	public Object[] getResult() {
		if (getReturnCode() == CANCEL) {
			return null;
		} else {
			return new Object[]{name};
		}
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite result = (Composite) super.createDialogArea(parent);
		
		parent.setLayout(GridLayoutFactory.fillDefaults().margins(5, 5).numColumns(1).create());
		result.setLayout(GridLayoutFactory.fillDefaults().margins(5, 5).numColumns(1).create());
		result.setLayoutData(GridDataFactory.fillDefaults().create());
		
		final Label nameLabel = new Label(result, SWT.NONE);
		nameLabel.setText("Name: ");
		nameLabel.setLayoutData(GridDataFactory.fillDefaults().span(1,1).create());
		
		final Text text = new Text(result, SWT.BORDER);
		text.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		text.setText(name);
		text.selectAll();
		text.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				name = text.getText();
				boolean enabled = name.length() > 0;
				getOkButton().setEnabled(enabled);
			}
		});
		return result;
	}
	
	public String getName() {
		return name;
	}
}
