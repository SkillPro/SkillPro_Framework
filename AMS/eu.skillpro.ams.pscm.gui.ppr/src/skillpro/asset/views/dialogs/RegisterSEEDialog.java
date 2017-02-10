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

package skillpro.asset.views.dialogs;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import skillpro.model.assets.Resource;
import skillpro.model.assets.SEE;
import eu.skillpro.ams.pscm.connector.opcua.Activator;
import eu.skillpro.ams.pscm.connector.opcua.OPCUAServerRepository;
import eu.skillpro.ams.pscm.connector.opcua.SkillProOPCUAException;
import eu.skillpro.ams.pscm.connector.opcua.ui.dialogs.OPCUACallDialog;

public class RegisterSEEDialog extends OPCUACallDialog {
	private static final String DIALOG_TITLE = "Register SEE to OPC-UA-server";
	private static final String SEE_NAME_LABEL = "SEE ID";
	private static final String WORKPLACE_LABEL = "SEE name";
	private static final String AML_SNIPPET_LABEL = "AML snippet";
	private static final String EDIT_AML = "Edit AML";
	
	private String seeName;
	private String seeID;
	private String amlString;
	private String nodeID = null;

	private SEE see;
	private Resource wp;

	public RegisterSEEDialog(Shell parentShell, SEE see, Resource wp) {
		super(parentShell);
		this.see = see;
		this.wp = wp;
	}
	
	protected void configureShell(Shell shell) {
	      super.configureShell(shell);
	      shell.setText(DIALOG_TITLE);
	}
	
	@Override
	protected boolean isResizable() {
		return true;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		// SEE name
		Label nameLabel = new Label(container, SWT.NONE);
		nameLabel.setLayoutData(getLabelGDF().create());
		nameLabel.setText(SEE_NAME_LABEL);
		//seeID
		final Text configurationIDText = new Text(container, SWT.BORDER);
		configurationIDText.setLayoutData(getSecondGDF().create());
		configurationIDText.setText(see.getSeeID());
		
		//Asset/Resource name
		Label workplaceLabel = new Label(container, SWT.NONE);
		workplaceLabel.setLayoutData(getLabelGDF().copy().align(SWT.FILL, SWT.FILL).grab(true, false).create());
		workplaceLabel.setText(WORKPLACE_LABEL);
		
		final Text workplaceText = new Text(container, SWT.BORDER);
		workplaceText.setLayoutData(getLabelGDF().create());
		workplaceText.setText(wp.getName());
		
		Text seeStateText = new Text(container, SWT.BORDER);
		seeStateText.setLayoutData(getLabelGDF().create());
		seeStateText.setText(see.getSEEState().name());
		// SEE aml
		final Label amlStringLabel = new Label(container, SWT.NONE);
		amlStringLabel.setLayoutData(getLabelGDF().create());
		amlStringLabel.setText(AML_SNIPPET_LABEL);
		
		Button changeAML = new Button(container, SWT.PUSH);
		changeAML.setLayoutData(getSecondGDF().create());
		changeAML.setText(EDIT_AML);

		final Text textViewer = new Text(container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		textViewer.setLayoutData(getViewerGDF().create());
		textViewer.setEditable(false);
		textViewer.setText(see.getAmlDescription());
	
		changeAML.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				textViewer.setEditable(!textViewer.getEditable());
				if (!textViewer.getEditable()) {
					see.setAmlDescription(textViewer.getText());
				}
			}
		});
		
		// register/unregister
		final Button registerAML = new Button(container, SWT.PUSH);
		registerAML.setLayoutData(getLabelGDF().create());
		registerAML.setText("Register SEE");
		registerAML.setEnabled(true);
		
		final Text nodeIDText = new Text(container, SWT.BORDER);
		nodeIDText.setLayoutData(getLabelGDF().create());
		nodeIDText.setEditable(false);
		
		final Button deregisterAML = new Button(container, SWT.PUSH);
		deregisterAML.setLayoutData(getLabelGDF().create());
		deregisterAML.setText("Deregister SEE");
		deregisterAML.setEnabled(false);
		
		//register
		registerAML.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				seeID = configurationIDText.getText();
				seeName = workplaceText.getText(); 
				amlString = textViewer.getText();
				try {
					nodeID = OPCUAServerRepository.registerSEE2(seeName, amlString, seeID, Activator.getDefault().getCurrentUAaddress());
					amlString = replaceOldMESNodeID(amlString, nodeID);
					see.setAmlDescription(amlString);
					textViewer.setText(amlString);
					nodeIDText.setText(nodeID);
					deregisterAML.setEnabled(true);
					registerAML.setEnabled(false);
				} catch (SkillProOPCUAException e1) {
					e1.printStackTrace();
					MessageDialog.openWarning(getShell(), "OPC-UA connection error", e1.getMessage());
				}
			}
		});
		//unregister
		deregisterAML.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					nodeID = null;
					nodeIDText.setText("");
					OPCUAServerRepository.deregisterSEE(nodeID, Activator.getDefault().getCurrentUAaddress());
					deregisterAML.setEnabled(false);
					registerAML.setEnabled(true);
				} catch (SkillProOPCUAException e1) {
					e1.printStackTrace();
					MessageDialog.openWarning(getShell(), "OPC-UA connection error", e1.getMessage());
				}
				
			}
		});
		return area;
	}

	private String replaceOldMESNodeID(String input, String newMESNodeID) {
		String[] tokens = newMESNodeID.split(";");
		if (tokens.length != 2) {
			throw new IllegalArgumentException("Unknown format of MES NodeID: " + newMESNodeID);
		}
		String namespace = tokens[0].replace("ns=", "");
		String identifier = tokens[1].replace("i=", "");
		String nodeIDString = "ns=" + namespace + ";i=" + identifier;
		
		String stringPattern = "(.+?)(MESCommType)(.+?)(OpcUa)(.+?)(nodeId)(.+?)(<Value>)(.+?)(</Value>)(.+?)(ESCommType)(.+)";
		Pattern pattern = Pattern.compile(stringPattern, Pattern.DOTALL);
		Matcher matcher = pattern.matcher(input);
		String replace = "";
		if (matcher.find()) {
			replace = "$1$2$3$4$5$6$7$8" + nodeIDString + "$10$11$12$13";
		}
		matcher.reset();
		return matcher.replaceFirst(replace);
	}
	
	public String getSeeName() {
		return seeName;
	}

	public String getAmlString() {
		return amlString;
	}

	public String getNodeID() {
		return nodeID;
	}
	
	@Override
	protected void cancelPressed() {
		super.cancelPressed();
	}
	
	@Override
	protected void okPressed() {
		if (nodeID != null) {
			super.okPressed();
		} else {
			MessageDialog.openWarning(getShell(), "SEE is not registered.", "Cancel instead!");
		}
	}
}