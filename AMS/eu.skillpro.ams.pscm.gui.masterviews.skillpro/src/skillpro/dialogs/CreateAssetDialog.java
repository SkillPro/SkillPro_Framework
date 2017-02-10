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

public class CreateAssetDialog extends SelectionDialog {
	private static enum AssetType {
		FACTORY(false),
		FACTORY_NODE(true),
		RESOURCE(false);
		
		final public boolean canBeLogical;
		
		AssetType(boolean canBeLogical) {
			this.canBeLogical = canBeLogical;
		}
	}
	
	private static final String TITLE = "Create a new asset";
	
	final boolean isNameEditable;
	
	private String name;
	
	private boolean isLogical = false;
	private AssetType assetType;
	
	public CreateAssetDialog(Shell parentShell) {
		this(parentShell, "New asset");
	}
	
	public CreateAssetDialog(Shell parentShell, String name) {
		this(parentShell, name, true);
	}
	
	public CreateAssetDialog(Shell parentShell, String name, boolean isNameEditable) {
		super(parentShell);
		this.name = name;
		this.assetType = AssetType.RESOURCE;
		this.isNameEditable = isNameEditable;

		setTitle(TITLE);
	}
	
	
	@Override
	protected boolean isResizable() {
		return false;
	}
	
	@Override
	public String[] getResult() {
		if (getReturnCode() == CANCEL) {
			return null;
		} else {
			return new String[]{ name };
		}
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
		final Button factoryButton = new Button(container, SWT.RADIO);
		factoryButton.setText("Factory");
		factoryButton.setSelection(isFactory());
		factoryButton.setLayoutData(buttonGD.create());
		
		final Button factoryNodeButton = new Button(container, SWT.RADIO);
		factoryNodeButton.setText("Factory Node");
		factoryNodeButton.setSelection(isFactoryNode());
		factoryNodeButton.setLayoutData(buttonGD.create());
		
		final Button assetButton = new Button(container, SWT.RADIO);
		assetButton.setText("Resource");
		assetButton.setSelection(isResource());
		assetButton.setLayoutData(buttonGD.create());
		
		final Label sep = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
		sep.setLayoutData(buttonGD.create());
		
		final Label nameLabel = new Label(container, SWT.NONE);
		nameLabel.setText("Name: ");
		nameLabel.setLayoutData(buttonGD.span(1,1).create());
		
		final Text nameText = new Text(container, SWT.BORDER);
		nameText.setEditable(isNameEditable);
		nameText.setText(name);
		nameText.selectAll();
		nameText.setLayoutData(buttonGD.span(1,1).create());
		
		final Label sep2 = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
		sep2.setLayoutData(buttonGD.create());
		
		final Button logicalButton = new Button(container, SWT.CHECK);
		logicalButton.setText("Logical?");
		logicalButton.setSelection(false);
		logicalButton.setEnabled(false);
		logicalButton.setLayoutData(buttonGD.create());
		
		
		factoryButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				assetType = AssetType.FACTORY;
				logicalButton.setEnabled(assetType.canBeLogical);
			}
		});
		
		factoryNodeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				assetType = AssetType.FACTORY_NODE;
				logicalButton.setEnabled(assetType.canBeLogical);
			}
		});
		
		assetButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				assetType = AssetType.RESOURCE;
				logicalButton.setEnabled(assetType.canBeLogical);
			}
		});
		
		logicalButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				isLogical = logicalButton.getSelection();
			}
		});
		
		nameText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				name = nameText.getText();
				getOkButton().setEnabled(name.length() > 0);
			}
		});
	}
	
	public boolean isFactory() {
		return assetType == AssetType.FACTORY;
	}
	
	public boolean isFactoryNode() {
		return assetType == AssetType.FACTORY_NODE;
	}
	
	public boolean isResource() {
		return assetType == AssetType.RESOURCE;
	}
	
	public boolean isLogical() {
		return isLogical;
	}
}
