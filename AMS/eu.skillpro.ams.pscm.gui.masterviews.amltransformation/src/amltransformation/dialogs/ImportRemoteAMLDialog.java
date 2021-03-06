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

package amltransformation.dialogs;

import java.rmi.RemoteException;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import eu.skillpro.ams.pscm.connector.amlservice.AMLClient;
import eu.skillpro.ams.pscm.connector.amlservice.gen.v2.schema.AMLProject;

/**
 * @author Kiril Aleksandrov
 * @author Abteilung ISPE/PDE, FZI Forschungszentrum Informatik 
 *
 * 18 Mar 2014
 *
 */
public class ImportRemoteAMLDialog extends TitleAreaDialog {

	private static final String AMLSERVERADDRESS = "http://syrios.mech.upatras.gr/skillpro/AMLServerService";
	
	private String amlContent;
	private String amlInfo;
	private boolean useDefaultCredentials = true;
	
	private Text fileNameText;
	private TableViewer viewer;

	private Text usernameText;
	private Text passwordText;
	private Text amlServerAddress;
	
	private String initialAMLFileSearchString="%";
	
	public ImportRemoteAMLDialog(Shell parentShell) {
		super(parentShell);
	}	
	
	public ImportRemoteAMLDialog(Shell parentShell, String initialAMLFileName) {
		super(parentShell);
		this.initialAMLFileSearchString=initialAMLFileName;
	}

	@Override
	public void create() {
		super.create();
		setTitle("Import AML file from AML-Server");
	}	

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		container.setLayout(GridLayoutFactory.fillDefaults().margins(5,5).equalWidth(false).numColumns(1).create());

		createCredentialsComposite(container);
		createFileNameComposite(container);
		createViewerContainer(container);
		
		return area;
	}

	private void createCredentialsComposite(Composite container) {
		Group credentialsGroup = new Group(container, SWT.NONE);
		credentialsGroup.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
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
	
	private void createFileNameComposite(Composite container) {
		Composite top = new Composite(container, SWT.NONE);
		top.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		top.setLayout(GridLayoutFactory.fillDefaults().margins(5,5).equalWidth(false).numColumns(4).create());
		
		Label lbtFirstName = new Label(top, SWT.NONE);
	    lbtFirstName.setText("File name:");
	    lbtFirstName.setLayoutData(GridDataFactory.swtDefaults().create());
	
		fileNameText = new Text(top,  SWT.BORDER);
		fileNameText.setLayoutData(GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).create());
		fileNameText.setText(initialAMLFileSearchString);
		
		Button button = new Button(top, SWT.PUSH);
		button.setLayoutData(GridDataFactory.swtDefaults().create());
		button.setText("Choose");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					viewer.setInput(getFileList());
				} catch (RemoteException e1) {
					setMessage("Couldn't obtain file list. Please check credentials! See log for more details.", IMessageProvider.ERROR); // TODO log!
					e1.printStackTrace();
				}
				viewer.refresh();
			}
		});
	}
	
	/**
	 * @return
	 * @throws RemoteException 
	 */
	protected AMLProject[] getFileList() throws RemoteException {
		if (useDefaultCredentials) {
			return AMLClient.getInstance().getAMLFilesByName(fileNameText.getText());
		} else {
			return AMLClient.createAMLClient(usernameText.getText(), passwordText.getText()).getAMLFilesByName(fileNameText.getText());
		}
	}

	private void createViewerContainer(Composite container) {
		Composite top = new Composite(container, SWT.NONE);
		top.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		top.setLayout(GridLayoutFactory.fillDefaults().margins(5,5).equalWidth(false).numColumns(4).create());
		
		Label lbtFirstName = new Label(top, SWT.NONE);
	    lbtFirstName.setText("Select file:");
	    lbtFirstName.setLayoutData(GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.FILL).create());

		viewer = new TableViewer(top, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION | SWT.SINGLE);
		viewer.getControl().setLayoutData(GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).hint(SWT.DEFAULT, 100).span(2, 1).create());
		
		TableViewerColumn col1 = new TableViewerColumn(viewer, SWT.NONE);
		col1.getColumn().setText("ID");
		col1.getColumn().setWidth(50);
		col1.getColumn().setResizable(false);
		col1.getColumn().setMoveable(false);
		
		TableViewerColumn col2 = new TableViewerColumn(viewer, SWT.NONE);
		col2.getColumn().setText("Name");
		col2.getColumn().setWidth(150);
		col2.getColumn().setResizable(true);
		col2.getColumn().setMoveable(false);
		
		TableViewerColumn col3 = new TableViewerColumn(viewer, SWT.NONE);
		col3.getColumn().setText("Date");
		col3.getColumn().setWidth(70);
		col3.getColumn().setResizable(true);
		col3.getColumn().setMoveable(false);
	
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLinesVisible(true);
		
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setLabelProvider(new AMLProjectLabelProvider());
		
	}

	// save content of the Text fields because they get disposed
	// as soon as the Dialog closes
	private boolean saveInput() {
		if (viewer.getSelection().isEmpty()) {
			return false;
		}
		
		AMLProject project = (AMLProject) ((StructuredSelection) (viewer.getSelection())).getFirstElement();
		Long fileID = Long.valueOf(project.getID());
		amlInfo = amlServerAddress.getText() + "#" + fileID;
		if (useDefaultCredentials) {
			try {
				amlContent = AMLClient.getInstance().getAMLFileInput(fileID);
			} catch (RemoteException e) {
				setErrorMessage("Coudn't obtain file from the AMLserver! Check log for more details!"); // TODO log!
				e.printStackTrace();
			}
		} else {
			try {
				amlContent = AMLClient.createAMLClient(usernameText.getText(), passwordText.getText()).getAMLFileInput(fileID);
			} catch (RemoteException e) {
				setErrorMessage("Coudn't obtain file from the AMLserver! Check credentials! Check log for more details!"); // TODO log!
				e.printStackTrace();
			}
		}
		return true;
	}

	
	@Override
	protected void okPressed() {
		if (saveInput()) {
			super.okPressed();
		} else {
			setErrorMessage("No file selected or selected file broken");
		}
	}

	public String getAmlContent() {
		return amlContent;
	}
	
	public String getAmlInfo() {
		return amlInfo;
	}
	
	private class AMLProjectLabelProvider extends LabelProvider implements ITableLabelProvider{

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			switch (columnIndex) {
			case 0:
				return ((AMLProject)element).getID();
			case 1:
				return ((AMLProject)element).getName();
			case 2:
				return ((AMLProject)element).getAMLFile()[0].getDate();
			default:
				return null;
			}
		}
		
	}
}
