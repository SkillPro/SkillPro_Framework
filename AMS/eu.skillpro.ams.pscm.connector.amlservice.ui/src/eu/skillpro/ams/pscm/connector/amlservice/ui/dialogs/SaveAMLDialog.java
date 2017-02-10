/**
 * de.fzi.skillpro.connector.amlservice.ui: 18 Mar 2014
 */
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

package eu.skillpro.ams.pscm.connector.amlservice.ui.dialogs;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;

import skillpro.model.assets.Factory;
import skillpro.model.service.SkillproService;
import skillpro.providers.asset.AssetTreeContentProvider;
import skillpro.providers.asset.AssetTreeLabelProvider;
import skillpro.transformator.actions.SkillproReverseTransformAction;
import aml.amlparser.AMLExporter;
import aml.amlparser.AMLParser;
import eu.skillpro.ams.pscm.connector.amlservice.AMLClient;
import eu.skillpro.ams.pscm.icons.IconActivator;

public class SaveAMLDialog extends TitleAreaDialog {

	private static final String AMLSERVERADDRESS = "http://syrios.mech.upatras.gr/skillpro/SkillPro/AMLServerService";
	
	private boolean useDefaultCredentials=true;

	private Text usernameText;
	private Text passwordText;
	private Text amlServerAddress;
	
	private Text fileNameText;
	private Label contentOKIcon;
	private Label contentOKText;

	private String fileName;
	private String amlContent;
	
	private Long fileID;

	private Button selectFileButton;

	private Button selectElementButton;

	public SaveAMLDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	public void create() {
		super.create();
		setTitle("Save data (file) to AML server");
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(GridDataFactory.fillDefaults().create());
		container.setLayout(GridLayoutFactory.fillDefaults().margins(5, 5)
				.equalWidth(false).numColumns(1).create());

		createCredentialsComposite(container);
		createSelectContentComposite(container);
		createFileNameComposite(container);

		return area;
	}

	private void createCredentialsComposite(Composite container) {
		Group credentialsGroup = new Group(container, SWT.NONE);
		credentialsGroup.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		credentialsGroup.setLayout(GridLayoutFactory.fillDefaults().margins(5,5).equalWidth(false).numColumns(4).create());
		credentialsGroup.setText("Connection settings");

		Button useDefault = new Button(credentialsGroup,  SWT.CHECK);
		useDefault.setLayoutData(GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).create());
		useDefault.setText("Use default?");
		useDefault.setSelection(useDefaultCredentials);
		useDefault.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				useDefaultCredentials=!useDefaultCredentials;
				usernameText.setEnabled(!useDefaultCredentials);
				passwordText.setEnabled(!useDefaultCredentials);
				amlServerAddress.setEnabled(!useDefaultCredentials);
			}
		});
		Label empty = new Label(credentialsGroup, SWT.NONE);
	    empty.setLayoutData(GridDataFactory.swtDefaults().span(3, 1).create());
		
		Label serverAddressLabel = new Label(credentialsGroup, SWT.NONE);
		serverAddressLabel.setText("AML-Server:");
		serverAddressLabel.setLayoutData(GridDataFactory.swtDefaults().create());
	
		amlServerAddress = new Text(credentialsGroup,  SWT.BORDER);
		amlServerAddress.setLayoutData(GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).span(3, 1).grab(true, false).create());
		amlServerAddress.setText(AMLSERVERADDRESS);
		amlServerAddress.setEnabled(!useDefaultCredentials);
		
	
	    Label usernameLabel = new Label(credentialsGroup, SWT.NONE);
	    usernameLabel.setLayoutData(GridDataFactory.swtDefaults().span(1, 1).create());
	    usernameLabel.setText("Username:");
	
		usernameText = new Text(credentialsGroup,  SWT.BORDER);
		usernameText.setLayoutData(GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).create());
		usernameText.setEnabled(!useDefaultCredentials);
		usernameText.setText("Skillpro");
		
		Label passwordLabel = new Label(credentialsGroup, SWT.NONE);
		passwordLabel.setText("Password:");
		passwordLabel.setLayoutData(GridDataFactory.swtDefaults().create());
	
		passwordText = new Text(credentialsGroup,  SWT.BORDER|SWT.PASSWORD);
		passwordText.setLayoutData(GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).create());
		passwordText.setEnabled(!useDefaultCredentials);
		passwordText.setText("Skillpro");
	}

	private void createSelectContentComposite(Composite container) {
		Group top = new Group(container, SWT.NONE);
		top.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		top.setLayout(GridLayoutFactory.fillDefaults().margins(5,5).equalWidth(true).numColumns(4).create());
		top.setText("AML content");
		
		final Button fromFileButton = new Button(top, SWT.RADIO);
		fromFileButton.setLayoutData(GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).create());
		fromFileButton.setText("From file");

		selectFileButton = new Button(top, SWT.PUSH);
		selectFileButton.setLayoutData(GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).create());
		selectFileButton.setText("Select file");
		selectFileButton.setEnabled(false);

		fromFileButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectFileButton.setEnabled(fromFileButton.getSelection());
			}
		});
		selectFileButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog amlDialog = new FileDialog(getShell(), SWT.OPEN);
				amlDialog.setText("Select .aml file");
				amlDialog.setFilterPath("./");
				amlDialog.setFilterExtensions(new String[] { "*.aml" });
				String filename = amlDialog.open();
				if (filename != null && !filename.equals("")) {
					AMLParser amlParser = AMLParser.getInstance();
					amlContent = amlParser.getContentStringFromFile(filename);
				}
				updateContentOK();
			}
		});
		
		
		Button fromStructureButton = new Button(top, SWT.RADIO);
		fromStructureButton.setLayoutData(GridDataFactory.swtDefaults().create());
		fromStructureButton.setText("From structure");

		selectElementButton = new Button(top, SWT.PUSH);
		selectElementButton.setLayoutData(GridDataFactory.swtDefaults().create());
		selectElementButton.setText("Select element");
		selectElementButton.setEnabled(false);
		selectElementButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				openFactoryNodeSelectionDialog();
			}
		});
		
		SelectionAdapter sa = new UpdateButtons(fromFileButton, fromStructureButton);
		fromFileButton.addSelectionListener(sa);
		fromStructureButton.addSelectionListener(sa);
	
		contentOKIcon = new Label(top, SWT.NONE);
		contentOKIcon.setLayoutData(GridDataFactory.swtDefaults().span(2,1).create());
		
		contentOKText = new Label(top, SWT.NONE);
		contentOKText.setLayoutData(GridDataFactory.swtDefaults().span(2,1).create());

	}

	protected void openFactoryNodeSelectionDialog() {
		ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(getShell(), new AssetTreeLabelProvider(), new AssetTreeContentProvider());
		dialog.setTitle("Select factory node to save");
		dialog.setInput(SkillproService.getSkillproProvider().getAssetRepo());
		if (dialog.open() == Window.OK) {
			List<Factory> selectedFactories = new ArrayList<>();
			Object[] selectedNodes = dialog.getResult();
			for (int i = 0; i < selectedNodes.length; i++) {
				Object selectedNode = selectedNodes[i];
				if (selectedNode instanceof Factory) {
					selectedFactories.add((Factory) selectedNode);
				}
			}
			exportSelectedFactoryNode(selectedFactories);
		}
		updateContentOK();
	}
	
	private void exportSelectedFactoryNode(List<Factory> selectedFactories) {
		File defaultMapping = new File("DefaultMapping.xml");
		if (!defaultMapping.exists()) {
			MessageBox dialog = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.ICON_ERROR | SWT.OK| SWT.CANCEL);
			dialog.setText("Default mapping doesn't exist!");
			dialog.setMessage("Please create 'DefaultMapping.xml' first");
			dialog.open();
		} else {
			try {
				new SkillproReverseTransformAction().reverseTransform(selectedFactories);
				//this is the important part.
				amlContent = AMLExporter.getExportedAsString();
			} catch (TransformerException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void createFileNameComposite(Composite container) {
		Composite top = new Composite(container, SWT.NONE);
		top.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		top.setLayout(GridLayoutFactory.fillDefaults().margins(5,5).equalWidth(false).numColumns(4).create());
		
		Label lbtFirstName = new Label(top, SWT.NONE);
		lbtFirstName.setText("File name:");
		lbtFirstName.setLayoutData(GridDataFactory.swtDefaults().create());

		fileNameText = new Text(top, SWT.BORDER);
		fileNameText.setLayoutData(GridDataFactory.swtDefaults()
				.align(SWT.FILL, SWT.FILL).grab(true, false).create());
	}

	private void updateContentOK() {
		if (amlContent != null) {
			contentOKIcon.setImage(IconActivator.getImageDescriptor("icons/asset/fn.png").createImage());
			contentOKText.setText("AML content set.");
		} else {
			contentOKText.setText("No AML content set yet!");
		}
		contentOKText.getParent().layout();
	}
	
	@Override
	protected void okPressed() {
		if (amlContent == null) {
			setErrorMessage("AML content not defined!");
			return;
		}
		if (fileNameText.getText()== null||fileNameText.getText().isEmpty()) {
			setErrorMessage("File name not defined!");
			return; 
		} else {
			fileName = fileNameText.getText();
		}
		if (useDefaultCredentials) {
			try {
				fileID = AMLClient.getInstance().saveAMLFile(fileName, amlContent);
				super.okPressed();
			} catch (RemoteException e) {
				setErrorMessage("Couldn't save the file on server!");
				e.printStackTrace();
			}
		} else {
			try {
				fileID = AMLClient.createAMLClient(usernameText.getText(), passwordText.getText()).saveAMLFile(fileName, amlContent);
				super.okPressed();
			} catch (RemoteException e) {
				setErrorMessage("Couldn't save the file on server!");
				e.printStackTrace();
			}
		}
		
	}

	/**
	 * @return the name of the file to save
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @return the fileID
	 */
	public Long getFileID() {
		return fileID;
	}
	
	private class UpdateButtons extends SelectionAdapter {
		
		private Button fromFileButton;
		private Button fromStructureButton;

		/**
		 * @param fromFileButton
		 * @param fromFileButton2
		 */
		public UpdateButtons(Button fromFileButton, Button fromStructureButton) {
			this.fromFileButton = fromFileButton;
			this.fromStructureButton = fromStructureButton;
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			selectFileButton.setEnabled(fromFileButton.getSelection());
			selectElementButton.setEnabled(fromStructureButton.getSelection());
			amlContent = null;
			updateContentOK();
		}

	}
	
	@Override
	protected boolean isResizable() {
		return true;
	}
}
