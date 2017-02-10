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

package skillpro.asset.views.wizardpages;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import skillpro.ams.util.AMSServiceUtility;
import skillpro.model.assets.AMSCommType;
import skillpro.model.assets.MESCommType;
import skillpro.model.assets.SEE;
import skillpro.model.utils.Pair;
import skillpro.view.impl.CreateSEEComposite;
import amltransformation.dialogs.ImportAMLDialog;
import eu.skillpro.ams.pscm.connector.opcua.Activator;

public class ManualSEEPage extends WizardPage {
	private String transformationMappingPath;
	private SEE see;
	private boolean needRoleLibs = true;
	private boolean usesDefaultAddresses;
	private ArrayList<String> amlInputs = new ArrayList<>();
	private CreateSEEComposite createSEEComposite;
	
	public ManualSEEPage(String pageName, boolean usesDefaultAddresses) {
		super(pageName);
		setTitle(pageName);
		setPageComplete(false);
		this.usesDefaultAddresses = usesDefaultAddresses;
	}
	
	/**
	 * Used for editing SEEs
	 * @param pageName the page's title
	 * @param see the SEE that will be used as input for the widgets
	 * @param input the input SEE as AML description
	 */
	public ManualSEEPage(String pageName, SEE see, String input, String transformationMappingPath, boolean usesDefaultAddresses) {
		super(pageName);
		setTitle(pageName);
		setPageComplete(true);
		needRoleLibs = false;
		this.see = new SEE(see);
		amlInputs.add(input);
		this.transformationMappingPath = transformationMappingPath;
		this.usesDefaultAddresses = usesDefaultAddresses;
	}
	

	@Override
	public void createControl(Composite parent) {
		Composite top = new Composite(parent, SWT.NONE);
		top.setLayout(GridLayoutFactory.fillDefaults().numColumns(1)
				.margins(8, 5).create());
		top.setLayoutData(GridDataFactory.fillDefaults().create());
		createLoadRoleLibsComposite(top);
		if (usesDefaultAddresses && see != null) {
			if (see.getAMSCommunication().getFirstElement() == AMSCommType.WEBSERVICES) {
				see.getAMSCommunication().setSecondElement(AMSServiceUtility.serviceAddress);
			}
			if (see.getMESCommunication().getFirstElement() == MESCommType.OPCUA) {
				see.getMESCommunication().setSecondElement(Activator.getDefault().getCurrentUAaddress());
			}
			
		}
		createSEEComposite = new CreateSEEComposite(top, see, false) {
			@Override
			public boolean validate() {
				boolean isValid = getResource() != null
						&& isSet(getDefaultResourceConfigurationID()) 
						&& isSet(getAmsCommunication())
						&& isSet(getMesCommunication());
				setPageComplete(isValid);
				return isValid;
			}
			
			private boolean isSet(Pair<?, String> pair){
				return pair != null && pair.getFirstElement() != null && isSet(pair.getSecondElement());
			}
			
			private boolean isSet(String string){
				return string != null && !string.isEmpty();
			}
		};
		createSEEComposite.setLayoutData(GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.TOP).create());
		setControl(top);
	}
	
	private void createLoadRoleLibsComposite(Composite parent) {
		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.verticalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		
		Group groupContext = new Group(parent, SWT.NONE);
		groupContext.setLayout(GridLayoutFactory.fillDefaults().numColumns(2)
				.margins(4, 3).create());

		groupContext.setLayoutData(gridData);
		groupContext.setText("Role Libs");
		
		
		final Button loadButton = new Button(groupContext, SWT.PUSH);
		loadButton.setText("Load");
		loadButton.setLayoutData(GridDataFactory.fillDefaults().create());
		
		
		final Label loadLabel = new Label(groupContext, SWT.NONE);
		loadLabel.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		loadLabel.setText("Role Libs not set");
		
		loadButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ImportAMLDialog amlDialog = new ImportAMLDialog(getShell());
				if (amlDialog.open() == Dialog.OK) {
					if (amlDialog.hasParts()) {
						for (String input : amlDialog.getListOfLibraries()) {
							amlInputs.add(input);
						}
					}
					
					for (String input : amlDialog.getListOfProjects()) {
						amlInputs.add(input);
					}
				}
				loadLabel.setText("Role Libs set!");
				setPageComplete(checkPageComplete());
			}
		});
		
		//initial state
		if (!needRoleLibs) {
			loadLabel.setText("Role Libs set!");
			loadButton.setEnabled(false);
		}
	}
	
	private boolean checkPageComplete() {
		if (needRoleLibs){
			return createSEEComposite.validate() && amlInputs.isEmpty();
		}else{
			return createSEEComposite.validate();
		}
	}
	
	public void setTransformationMappingPath(String transformationMappingPath) {
		this.transformationMappingPath = transformationMappingPath;
	}
	
	public String getTransformationMappingPath() {
		return transformationMappingPath;
	}
	
	public List<String> getAMLInputs() {
		return amlInputs;
	}
	
	public SEE getSEE() {
		if (see == null) {
			see = new SEE(createSEEComposite.getResource(), 
					createSEEComposite.getDefaultResourceConfigurationID(), createSEEComposite.getDefaultProductQuantities(), 
					createSEEComposite.getAmsCommunication(), createSEEComposite.getMesCommunication(),
					createSEEComposite.getEsCommunication(), createSEEComposite.getSEEType());
		} else {
			see.addNotRegisteredResource(createSEEComposite.getResource());
			see.setDefaultResourceConfigurationID(createSEEComposite.getDefaultResourceConfigurationID());
			see.setDefaultInputProductQuantities(createSEEComposite.getDefaultProductQuantities());
			see.setAMSCommunication(new Pair<>(createSEEComposite.getAmsCommunication()));
			see.setMESCommunication(new Pair<>(createSEEComposite.getMesCommunication()));
			see.setESCommunication(new Pair<>(createSEEComposite.getEsCommunication()));
			see.setSEEType(createSEEComposite.getSEEType());
		}
		return see;
	}
}
