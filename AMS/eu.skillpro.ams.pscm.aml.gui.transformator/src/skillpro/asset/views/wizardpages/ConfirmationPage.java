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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.xml.transform.TransformerException;

import nu.xom.Document;
import nu.xom.ParsingException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import skillpro.ams.util.AMSServiceUtility;
import skillpro.model.assets.AMSCommType;
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
import aml.skillpro.transformation.adapters.SEEAdapter;
import aml.skillpro.transformation.adapters.template.TransformableAdapterTemplate;
import aml.skillpro.transformer.ReverseTransformer;
import aml.skillpro.transformer.Transformer;
import aml.transformation.service.AMLTransformationService;
import eu.skillpro.ams.pscm.connector.opcua.Activator;

public class ConfirmationPage extends WizardPage {
	private String transformationMappingPath;
	private Map<SEE, Document> amlDocsMapping = new HashMap<>();
	private Map<File, SEE> amlSEEMapping = new HashMap<>();
	
	public ConfirmationPage(String pageName, Collection<File> amlFiles, String transformationMappingPath,
			boolean usesDefaultAddresses) {
		super(pageName);
		setTitle(pageName);
		this.transformationMappingPath = transformationMappingPath;
		for (File file : amlFiles) {
			try {
				amlSEEMapping.put(file, transformToSEE(file.getAbsolutePath()));
			} catch (Exception e) {
				amlSEEMapping.put(file, null);
				System.err.println("This file: [" + file.getAbsolutePath() + "] is not a valid SEE .aml file.");
				e.printStackTrace();
			}
		}
		
		if (usesDefaultAddresses) {
			for (Entry<File, SEE> entry : amlSEEMapping.entrySet()) {
				SEE see = entry.getValue();
				if (see != null) {
					if (see.getAMSCommunication().getFirstElement() == AMSCommType.WEBSERVICES) {
						see.getAMSCommunication().setSecondElement(AMSServiceUtility.serviceAddress);
					}
					if (see.getMESCommunication().getFirstElement() == MESCommType.OPCUA) {
						see.getMESCommunication().setSecondElement(Activator.getDefault().getCurrentUAaddress());
					}
					
					amlDocsMapping.put(see, reverseTransformSEEs(entry));
				}
			}
		} else {
			for (Entry<File, SEE> entry : amlSEEMapping.entrySet()) {
				SEE see = entry.getValue();
				if (see != null) {
					try {
						amlDocsMapping.put(see, AMLParser.getInstance().getDocumentFromFile(entry.getKey()));
					} catch (ParsingException | IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private Document reverseTransformSEEs(Entry<File, SEE> seeFileEntry) {
		AMLParser.getInstance().wipeData();
		AMLParser.getInstance().parseAMLLibrariesFromFile(seeFileEntry.getKey());
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
		reverseTransform(seeFileEntry.getValue(), roleHierarchies, interfaceHierarchies);
		//add reverse transformed object
		Root<InternalElement> defaultRoot = new Root<InternalElement>("Configuration", InternalElement.class);
		parsedObjects.add(defaultRoot);
		Collection<Object> objects = AMLTransformationService.getTransformationProvider()
				.getTransformationRepo().getReverseTransformedObjectsMap().values();
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
		return AMLExporter.getExportedAsDoc(parsedObjects, false);
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
		
		createAMLVerificationTableComposite(top);
		
		Composite saveAMLComposite = new Composite(top, SWT.NONE);
		saveAMLComposite.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).create());
		saveAMLComposite.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		
		Button saveAMLButton = new Button(saveAMLComposite, SWT.PUSH);
		saveAMLButton.setLayoutData(GridDataFactory.swtDefaults().create());
		saveAMLButton.setText("Save Batch In..");
		
		final Label savedLabel = new Label(saveAMLComposite, SWT.NONE);
		savedLabel.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).align(SWT.LEFT, SWT.CENTER).create());
		savedLabel.setText("Batch has not been saved.");

		Button uploadAMLButton = new Button(top, SWT.PUSH);
		uploadAMLButton.setLayoutData(GridDataFactory.swtDefaults().create());
		uploadAMLButton.setText("Upload to AMSService");
		
		saveAMLButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(Display.getDefault().getActiveShell(), SWT.SAVE | SWT.SINGLE);
				String directoryPath = dialog.open();
				boolean noErrors = true;
				for (Entry<SEE, Document> entry : amlDocsMapping.entrySet()) {
					SEE see = entry.getKey();
					String resourcesName = see.getResource() != null ? see.getResource().getName() : "";
					if (resourcesName.isEmpty()) {
						throw new IllegalArgumentException("This SEE, with the id:[" + see.getSeeID() + "] does not have any resources!");
					}
					String filename = directoryPath + "\\SEE_" + resourcesName + ".aml";
					try {
						if (filename != null && !filename.isEmpty()) {
							AMLExporter.saveFile(filename, entry.getValue());
						}
					} catch (TransformerException e1) {
						noErrors = false;
						e1.printStackTrace();
					}
				}
				if (noErrors) {
					savedLabel.setText("Saved to: \"" + directoryPath + "\"!");
					savedLabel.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_GREEN));
				} else {
					savedLabel.setText("There were problems during the saving process. Please check the saved files.");
					savedLabel.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_RED));
				}
			}
		});
		
		uploadAMLButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				for (Entry<SEE, Document> entry : amlDocsMapping.entrySet()) {
					SEE see = new SEE(entry.getKey());
					try {
						see.setAmlDescription(AMLExporter.getExportedAsString(entry.getValue()));
						registerSEE(see);
					} catch (TransformerException | IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		
		setControl(top);
	}
	
	private void createAMLVerificationTableComposite(Composite parent) {
		TableViewer viewer = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		
		Table table = viewer.getTable();
		table.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setLabelProvider(new AMLVerificationTableLabelProvider());

		TableColumn firstColumn = new TableColumn(table, SWT.CENTER, 0);
		firstColumn.setWidth(200);
		firstColumn.setText("File");
		
		TableColumn secondColumn = new TableColumn(table, SWT.CENTER, 1);
		secondColumn.setText("Verification");
		secondColumn.setWidth(200);
		
		viewer.setInput(amlSEEMapping.entrySet());
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		for (TableItem item : table.getItems()) {
			System.out.println("Item: " + item.getData());
			if (item.getData() instanceof Entry<?, ?>) {
				if (((Entry<?, ?>) item.getData()).getValue() == null) {
					item.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_YELLOW));
					item.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
				} else {
					item.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_GREEN));
				}
			}
		}
	}
	
	private void registerSEE(SEE see) throws ClientProtocolException, IOException {
		System.out.println("SEE[seeID: " + see.getSeeID() + ", opcuaMES: " + see.getMESCommunication().getSecondElement()
				+ "opcua: " + see.getOpcUAAddress() + ", amlFile: " + !see.getAmlDescription().isEmpty() + "]");
		String serviceName = "registerSEE";
		
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
		
		if (see.getAmlDescription() != null && !see.getAmlDescription().trim().isEmpty()) {
			jsonBuilder.add("amlFile", see.getAmlDescription());
		}
		
		HttpPost request = new HttpPost(AMSServiceUtility.serviceAddress + serviceName);
		
		request.setEntity(new StringEntity(jsonBuilder.build().toString(), "UTF-8"));
		System.out.println(request.getRequestLine() + " =====================================");
		request.setHeader("Content-type", "application/json");
		
		HttpClient client = HttpClientBuilder.create().build();;
		client.execute(request);
	}
	
	@SuppressWarnings("unchecked")
	private SEE transformToSEE(String seeFilepath)
			throws InstantiationException, IllegalAccessException {
		AMLParser.getInstance().wipeData();
		AMLParser.getInstance().parseAMLFromFile(seeFilepath);
		AMLTransformationService.getTransformationProvider().wipeAllData();
		Set<Role> roles = new HashSet<>();
		Set<Hierarchy<InternalElement>> hierarchies = new HashSet<>();
		for (Object obj : AMLParser.getInstance().getParsedObjects()) {
			if (obj instanceof Role) {
				roles.add((Role) obj);
			} else if (obj instanceof Hierarchy<?> && ((Hierarchy<?>) obj).getElement() instanceof InternalElement) {
				hierarchies.add((Hierarchy<InternalElement>) obj);
			}
		}
		try {
			TransformationMappingParser.loadTransformationMapping(transformationMappingPath, roles, hierarchies);
		} catch (ParsingException | IOException e) {
			AMLParser.getInstance().wipeData();
			AMLTransformationService.getTransformationProvider().wipeAllData();
			return null;
		}
		Set<Object> toTransform = new HashSet<>();
		for (Hierarchy<?> hie : hierarchies) {
			if (AMLTransformationService.getTransformationProvider().getTransformationRepo().getAdapterTransformablesMapping()
					.get(hie.getElement()) != null) {
				toTransform.add(hie);
			}
		}
		Transformer.getInstance().transform(toTransform);
		SEE see = null;
		for (Object obj : AMLTransformationService.getTransformationProvider()
				.getTransformationRepo().getTransformedObjectsMap().values()) {
			if (see == null && obj instanceof SEEAdapter) {
				see = (SEE) ((SEEAdapter) obj).getElement();
			} else if (see != null && obj instanceof SEEAdapter) {
				AMLTransformationService.getTransformationProvider().wipeAllData();
				AMLParser.getInstance().wipeData();
				return null;
			}
		}
		if (see == null) {
			AMLParser.getInstance().wipeData();
			AMLTransformationService.getTransformationProvider().wipeAllData();
			return null;
		}
		//clean up
		setErrorMessage(null);
		return see;
	}
	
	private class AMLVerificationTableLabelProvider implements ITableLabelProvider {

		@Override
		public void addListener(ILabelProviderListener listener) {
			
		}

		@Override
		public void dispose() {
			
		}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		@Override
		public void removeListener(ILabelProviderListener listener) {
			
		}

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			File file = null;
			SEE see = null;
			if (element instanceof Entry<?, ?>) {
				if (((Entry<?, ?>) element).getKey() instanceof File) {
					file = (File) ((Entry<?, ?>) element).getKey();
				}
				if (((Entry<?, ?>) element).getValue() instanceof SEE) {
					see = (SEE) ((Entry<?, ?>) element).getValue();
				}
			} else {
				throw new IllegalArgumentException("Unknown type, please re-implement this label provider");
			}
			switch (columnIndex) {
			case 0:
				if (file != null) {
					return file.getName();
				} else {
					return "File is null, shouldn't happen";
				}
			case 1:	
				if (see != null) {
					return "Successfully transformed!";
				} else {
					return "Could not transform aml file";
				}
			}
			
			return null;
		}
		
	}
}
