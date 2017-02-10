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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.SelectionDialog;

public class SimpleDropdownDialog extends SelectionDialog {
	
	private String[] values;
	private String caption;
	private Combo list;
	
	private int resultIndex;

	public SimpleDropdownDialog(Shell parentShell, String title, String caption, String[] values) {
		super(parentShell);
		setTitle(title);
		this.values = values;
		this.caption = caption;
		resultIndex = values.length > 0 ? 0 : -1;
	}

	public SimpleDropdownDialog(Shell parentShell, String title, String caption, Collection<String> values) {
		this(parentShell, title, caption, values.toArray(new String[values.size()]));
	}
	
	@Override
	protected boolean isResizable() {
		return false;
	}
	
	@Override
	public String[] getResult() {
		if (getReturnCode() == CANCEL) {
			return null;
		} else if (resultIndex == -1) {
			return null;
		} else {
			return new String[]{ values[resultIndex] };
		}
	}
	
	/**
	 * The index that the user has selected in the list. If there was no
	 * selection or the user has cancelled, <code>-1</code> is returned.
	 * 
	 * @return the selected index
	 */
	public int getResultIndex() {
		if (getReturnCode() == CANCEL) {
			return -1;
		} else {
			return resultIndex;
		}
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		container.setLayout(GridLayoutFactory.fillDefaults().margins(5, 5).equalWidth(false).numColumns(2).create());
		
		Label label = new Label(container, SWT.NONE);
		label.setText(caption);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		
		list = new Combo(container, SWT.READ_ONLY);
		list.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		for (String v : values) {
			list.add(v);
		}
		list.pack();
		list.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				resultIndex = list.getSelectionIndex();
				updateOkButton();
			}
		});
		list.select(resultIndex);
		return area;
	}
	
	private void updateOkButton() {
		getOkButton().setEnabled(resultIndex >= 0);
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		updateOkButton();
	}
}
