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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.SelectionDialog;

import skillpro.model.assets.Factory;
import skillpro.model.assets.FactoryNode;
import skillpro.model.assets.Resource;

public class EditAssetDialog extends SelectionDialog {
	
	private String name;
	
	private boolean isLogical = false;
	private final boolean isFactoryNode;
	
	private FactoryNode factoryNode;
	
	public EditAssetDialog(Shell parentShell, FactoryNode factoryNode) {
		super(parentShell);
		
		this.factoryNode = factoryNode;
		this.name = factoryNode.getName();
		
		if (factoryNode instanceof Factory) {
			setTitle("Edit factory");
			isFactoryNode = false;
		} else if (factoryNode instanceof Resource) {
			setTitle("Edit resource");
			isFactoryNode = false;
		} else {
			isFactoryNode = true;
			isLogical = !factoryNode.isLayoutable();
			setTitle("Edit factory node");
		}
	}
	
	@Override
	protected boolean isResizable() {
		return false;
	}
	
	@Override
	public String[] getResult() {
		if (getReturnCode() == OK) {
			return new String[]{ name };
		}
		return new String[] { };
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		container.setLayout(GridLayoutFactory.fillDefaults().margins(5,5).equalWidth(false).create());
		
		createButtonsComposite(container);
		
		return area;
	}

	private void createButtonsComposite(Composite container) {
		GridDataFactory buttonGD = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).span(2,1);
		
		final Label nameLabel = new Label(container, SWT.NONE);
		nameLabel.setText("Name: ");
		nameLabel.setLayoutData(buttonGD.span(1,1).create());
		
		final Text nameText = new Text(container, SWT.BORDER);
		nameText.setText(factoryNode.getName());
		nameText.selectAll();
		nameText.setLayoutData(buttonGD.span(1,1).create());
		
		
		if (isFactoryNode) {
			final Label sep2 = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
			sep2.setLayoutData(buttonGD.create());
			
			final Button logicalButton = new Button(container, SWT.CHECK);
			logicalButton.setText("Logical?");
			logicalButton.setSelection(isLogical);
			logicalButton.setEnabled(isFactoryNode);
			logicalButton.setLayoutData(buttonGD.create());
			
			logicalButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					isLogical = logicalButton.getSelection();
				}
			});
		}
		
		nameText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				name = nameText.getText();
				getOkButton().setEnabled(name.length() > 0);
			}
		});
	}
	
	public boolean isLogical() {
		return isLogical;
	}
}
