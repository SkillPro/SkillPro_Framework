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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.xml.transform.TransformerException;

import masterviews.dialogs.MasterFileDialog;
import masterviews.util.SupportedFileType;
import nu.xom.Document;
import nu.xom.ParsingException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import skillpro.ams.util.AMSServiceUtility;
import skillpro.model.assets.MESCommType;
import skillpro.model.assets.SEE;
import aml.amlparser.AMLExporter;
import aml.amlparser.AMLParser;
import aml.domain.Interface;
import aml.domain.InternalElement;
import aml.domain.Role;
import aml.domain.SystemUnit;
import aml.model.Hierarchy;
import aml.model.Root;
import aml.skillpro.mapping.TransformationMappingParser;
import aml.skillpro.transformation.adapters.template.TransformableAdapterTemplate;
import aml.skillpro.transformer.ReverseTransformer;
import aml.transformation.service.AMLTransformationService;

public class SEEResultPage extends WizardPage {
	private Document amlDoc;
	private SEE see;
	
	@SuppressWarnings("unchecked")
	public SEEResultPage(String pageName, List<String> amlLibs, SEE see, String transformationMappingPath) {
		super(pageName);
		this.see = new SEE(see);
		setTitle(pageName);
		AMLParser.getInstance().wipeData();
		for (String input : amlLibs) {
			AMLParser.getInstance().parseAMLLibrariesFromString(input);
		}
		Set<Role> roles = new HashSet<>();
		Set<Hierarchy<InternalElement>> hierarchies = new HashSet<>();
		List<Hierarchy<Role>> roleHierarchies = new ArrayList<>();
		List<Hierarchy<Interface>> interfaceHierarchies = new ArrayList<>();
		Set<Object> parsedObjects = AMLParser.getInstance().getParsedObjects();
		for (Object obj : parsedObjects) {
			if (obj instanceof Role) {
				roles.add((Role) obj);
			} else if (obj instanceof Hierarchy<?>) {
				if (((Hierarchy<?>) obj).getElement() instanceof InternalElement) {
					hierarchies.add((Hierarchy<InternalElement>) obj);
				} else if (((Hierarchy<?>) obj).getElement() instanceof Role) {
					roleHierarchies.add((Hierarchy<Role>) obj);
				} else if (((Hierarchy<?>) obj).getElement() instanceof Interface) {
					interfaceHierarchies.add((Hierarchy<Interface>) obj);
				}
			}
		}
		parseTransformationMapping(transformationMappingPath, roles, hierarchies);
		reverseTransform(see, roleHierarchies, interfaceHierarchies);
		//add reverse transformed object
		Root<InternalElement> defaultRoot = new Root<InternalElement>("Configuration", InternalElement.class);
		parsedObjects.add(defaultRoot);
		Collection<Object> objects = AMLTransformationService.getTransformationProvider().getTransformationRepo().getReverseTransformedObjectsMap().values();
		for (Object obj : objects) {
			if (obj instanceof Interface) {
				parsedObjects.add((Interface) obj);
			} else if (obj instanceof Role) {
				parsedObjects.add((Role) obj);
			} else if (obj instanceof InternalElement) {
				parsedObjects.add((InternalElement) obj);
			} else if (obj instanceof SystemUnit) {
				parsedObjects.add((SystemUnit) obj);
			} else if (obj instanceof Root<?>) {
				parsedObjects.add((Root<?>) obj);
			} else if (obj instanceof Hierarchy<?>) {
				Hierarchy<?> hie = (Hierarchy<?>) obj;
				if (hie.getElement() instanceof InternalElement && hie.getParent() == null) {
					defaultRoot.addChild((Hierarchy<InternalElement>) hie);
				}
				parsedObjects.add(hie);
			} else {
				//do nothing
			}
		}
		amlDoc = AMLExporter.getExportedAsDoc(parsedObjects, false);
	}

	private void parseTransformationMapping(String transformationMappingPath, Set<Role> roles,
			Set<Hierarchy<InternalElement>> hierarchies) {
		try {
			TransformationMappingParser.loadTransformationMapping(transformationMappingPath, roles, hierarchies);
		} catch (ParsingException | IOException e) {
			setErrorMessage("Not a valid transformation mapping data: " + transformationMappingPath);
			AMLParser.getInstance().wipeData();
			AMLTransformationService.getTransformationProvider().wipeAllData();
			throw new IllegalArgumentException("Transformation didn't function correctly");
		}
	}

	private void reverseTransform(SEE see, List<Hierarchy<Role>> roleHierarchies,
			List<Hierarchy<Interface>> interfaceHierarchies) {
		//Set role and interface hierarchies before performing reverse transformation
		TransformableAdapterTemplate.setCurrentRoleHierarchies(roleHierarchies);
		TransformableAdapterTemplate.setCurrentInterfaceHierarchies(interfaceHierarchies);
		//reverse transform SEE
		ReverseTransformer.getInstance().reverseTransformSEE(see);
		//REVERT
		TransformableAdapterTemplate.revertEverything();
	}

	@Override
	public void createControl(Composite parent) {
		Composite top = new Composite(parent, SWT.NONE);
		top.setLayout(GridLayoutFactory.fillDefaults().equalWidth(false).numColumns(1)
				.margins(8, 5).create());
		top.setLayoutData(GridDataFactory.fillDefaults().create());
		
		Label amlLabel = new Label(top, SWT.NONE);
		amlLabel.setLayoutData(GridDataFactory.swtDefaults().create());
		amlLabel.setText("AML Description: ");
		
		Text amlText = new Text(top, SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY | SWT.BORDER);
		amlText.setBackground(getShell().getDisplay().getSystemColor(SWT.COLOR_WHITE));
		amlText.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		if (amlDoc != null) {
			try {
				String amlAsString = getAMLAsString();
				amlText.setText(amlAsString);
			} catch (TransformerException | IOException e1) {
				e1.printStackTrace();
			}
		}
		
		Button saveAMLButton = new Button(top, SWT.PUSH);
		saveAMLButton.setLayoutData(GridDataFactory.swtDefaults().create());
		saveAMLButton.setText("Save As..");
		
		Button uploadAMLButton = new Button(top, SWT.PUSH);
		uploadAMLButton.setLayoutData(GridDataFactory.swtDefaults().create());
		uploadAMLButton.setText("Upload to AMSService");
		
		saveAMLButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					String filename = MasterFileDialog.saveFile(SupportedFileType.AML);
					if (filename != null && !filename.isEmpty()) {
						AMLExporter.saveFile(filename, amlDoc);
					}
				} catch (TransformerException e1) {
					e1.printStackTrace();
				}
			}
		});
		
		uploadAMLButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					see.setAmlDescription(getAMLAsString());
					registerSEE(see);
				} catch (TransformerException | IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		
		setControl(top);
	}
	
	private void registerSEE(SEE see) throws ClientProtocolException, IOException {
		JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
		if (see.getSeeID() != null && !see.getSeeID().trim().isEmpty()) {
			jsonBuilder.add("seeId", see.getSeeID() == null ? UUID.randomUUID().toString() : see.getSeeID());
		}
		
		if (see.getMESCommunication().getFirstElement() == MESCommType.OPCUA) {
			jsonBuilder.add("opcuaAddress", see.getMESCommunication().getSecondElement());
		}
	
		if (see.getResource() != null) {
			jsonBuilder.add("assetTypeNames", see.getResource().getName());
		}

		jsonBuilder.add("simulation", see.isSimulation() + "");

		String amlDescription = see.getAmlDescription();
		if (amlDescription != null && !amlDescription.trim().isEmpty()) {
			jsonBuilder.add("amlFile", amlDescription);
		}

		HttpPost request = new HttpPost(AMSServiceUtility.serviceAddress + "registerSEE");
		
		request.setEntity(new StringEntity(jsonBuilder.build().toString(), "UTF-8"));
		request.setHeader("Content-type", "application/json");
		
		HttpClient client = HttpClientBuilder.create().build();;
		client.execute(request);
	}
	
	private String getAMLAsString() throws TransformerException, IOException {
		return AMLExporter.getExportedAsString(amlDoc);
	}
}
