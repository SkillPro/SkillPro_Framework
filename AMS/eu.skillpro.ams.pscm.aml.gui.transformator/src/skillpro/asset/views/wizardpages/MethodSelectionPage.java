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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import masterviews.dialogs.MasterFileDialog;
import masterviews.util.SupportedFileType;
import nu.xom.ParsingException;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;

import skillpro.ams.util.AMSServiceUtility;
import skillpro.model.assets.SEE;
import aml.amlparser.AMLParser;
import aml.domain.InternalElement;
import aml.domain.Role;
import aml.model.Hierarchy;
import aml.skillpro.mapping.TransformationMappingParser;
import aml.skillpro.transformation.adapters.SEEAdapter;
import aml.skillpro.transformer.Transformer;
import aml.transformation.service.AMLTransformationService;
import eu.skillpro.ams.pscm.connector.opcua.Activator;

public class MethodSelectionPage extends WizardPage {
	private static final String TITLE = "Select the method for creating an SEE";
	private SEECreationMethod creationMethod;
	
	private boolean defaultMapping = true;
	private boolean useDefaultAddresses = true;
	private String transformationMappingPath = "DefaultMapping.xml";
	private SEE see;
	private Map<String, File> batchFilesMapping = new HashMap<>();
	private boolean amlError = false;
	private String errorMessage = null;
	private String seeFileInput;
	
	private Button manualOption;
	private Button loadOption;
	private Button loadBatchOption;
	
	private Button defaultMappingOption;
	private Button loadMappingOption;
	private Text loadText;
	
	private boolean problemWithDefaultAddress = false;
	
	public MethodSelectionPage(String pageName) {
		super(pageName);
		setTitle(TITLE);
	}

	@Override
	public void createControl(Composite parent) {
		Composite top = new Composite(parent, SWT.NONE);
		top.setLayout(GridLayoutFactory.swtDefaults().numColumns(1)
				.margins(8, 5).create());
		top.setLayoutData(GridDataFactory.fillDefaults().create());
		createGroupSelection(top);
		createGroupDefaultMappingSelection(top);
		createUseDefaultAddressesCheckbox(top);
		Label seeLabel = new Label(top, SWT.NONE);
		seeLabel.setLayoutData(GridDataFactory.fillDefaults().create());
		
		setControl(top);
	}
	
	private void createUseDefaultAddressesCheckbox(Composite parent) {
		Button useDefaultAddressCheckbox = new Button(parent, SWT.CHECK);
		useDefaultAddressCheckbox.setLayoutData(GridDataFactory.fillDefaults().create());
		useDefaultAddressCheckbox.setText("Use default addresses?");
		useDefaultAddressCheckbox.setSelection(useDefaultAddresses);
		
		useDefaultAddressCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				useDefaultAddresses = !useDefaultAddresses;
				setPageComplete(isPageComplete());
			}
		});
		
		Composite currentAddressesComposite = new Composite(parent, SWT.NONE);
		currentAddressesComposite.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).create());
		currentAddressesComposite.setLayoutData(GridDataFactory.fillDefaults().create());
		
		Label amsString = new Label(currentAddressesComposite, SWT.NONE);
		amsString.setText("AMS Service: ");
		
		Label amsAddressLabel = new Label(currentAddressesComposite, SWT.NONE);
		amsAddressLabel.setText(AMSServiceUtility.serviceAddress);
		amsAddressLabel.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GREEN));
		
		Label mesString = new Label(currentAddressesComposite, SWT.NONE);
		mesString.setText("MES: ");
		
		Label mesAddressLabel = new Label(currentAddressesComposite, SWT.NONE);
		String currentUAAddress = Activator.getDefault().getCurrentUAaddress();
		int mesColorId = SWT.COLOR_DARK_GREEN;
		if (currentUAAddress == null) {
			currentUAAddress = "Empty - please set an address!";
			mesColorId = SWT.COLOR_DARK_RED;
			problemWithDefaultAddress = true;
		}
		mesAddressLabel.setText(currentUAAddress);
		mesAddressLabel.setForeground(Display.getCurrent().getSystemColor(mesColorId));
	}

	private void createGroupSelection(Composite parent) {
		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.verticalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		
		Group groupContext = new Group(parent, SWT.NONE);
		groupContext.setLayout(GridLayoutFactory.fillDefaults().numColumns(3)
				.margins(4, 3).create());

		groupContext.setLayoutData(gridData);
		
		manualOption = new Button(groupContext, SWT.RADIO);
		manualOption.setLayoutData(GridDataFactory.fillDefaults().span(3, 1).create());
		manualOption.setText("New");
		
		loadOption = new Button(groupContext, SWT.RADIO);
		loadOption.setLayoutData(GridDataFactory.fillDefaults().create());
		loadOption.setText("Load File");
		
		final Text loadText = new Text(groupContext, SWT.BORDER | SWT.READ_ONLY);
		loadText.setBackground(getShell().getDisplay().getSystemColor(SWT.COLOR_WHITE));
		loadText.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		loadText.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				setToLoadSEE();
			}
		});
		
		final Button loadButton = new Button(groupContext, SWT.PUSH);
		loadButton.setText("...");
		loadButton.setLayoutData(GridDataFactory.fillDefaults().create());
		loadButton.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				String seeFilepath = MasterFileDialog.getFilenameFromFileDialog(SupportedFileType.AML);
				if (seeFilepath == null) {
					return;
				}
				loadText.setText(seeFilepath);
				try {
					see = transformToSEE(seeFilepath);
					seeFileInput = AMLParser.getInstance().getContentStringFromFile(seeFilepath);
				} catch (InstantiationException e1) {
					e1.printStackTrace();
				} catch (IllegalAccessException e1) {
					e1.printStackTrace();
				}
				setToLoadSEE();
			}
		});
		
		loadBatchOption = new Button(groupContext, SWT.RADIO);
		loadBatchOption.setLayoutData(GridDataFactory.fillDefaults().span(3, 1).create());
		loadBatchOption.setText("Load Batch");
	
		final List filesList = new List(groupContext, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.LINE_SOLID);
		filesList.setLayoutData(GridDataFactory.fillDefaults().span(2, 6).grab(true, true).create());
		
		final Button loadBatchButton = new Button(groupContext, SWT.PUSH);
		loadBatchButton.setText("...");
		loadBatchButton.setLayoutData(GridDataFactory.fillDefaults().create());
		loadBatchButton.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				File[] files = MasterFileDialog.getLocalFilesFromFileDialog(SupportedFileType.AML);
				for (File file : files) {
					String fileName = file.getName();
					filesList.add(fileName);
					batchFilesMapping.put(fileName, file);
				}
				setToLoadBatchSEE();
			}
		});
		
		filesList.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				setToLoadBatchSEE();
			}
		});
		
		filesList.addKeyListener(new KeyAdapter() {
			
			@Override
			public void keyReleased(KeyEvent e) {
				int selectionIndex = filesList.getSelectionIndex();
				if (e.keyCode == SWT.DEL && selectionIndex > -1) {
					String path = filesList.getItem(selectionIndex);
					filesList.remove(selectionIndex);
					batchFilesMapping.remove(path);
				}
			}
			
		});
		
		manualOption.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				setToManual();
			}
		});
		loadOption.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				setToLoadSEE();
			}
		});
		loadBatchOption.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setToLoadBatchSEE();
			}
		});
		//initial state
		setToManual();
	}

	private void setToManual() {
		creationMethod = SEECreationMethod.MANUAL;
		setPageComplete(isPageComplete());
		if (amlError && getErrorMessage() != null) {
			errorMessage = getErrorMessage();
			setErrorMessage(null);
		}
		manualOption.setSelection(true);
		loadOption.setSelection(false);
		loadBatchOption.setSelection(false);
	}
	
	private void setToLoadSEE() {
		creationMethod = SEECreationMethod.LOAD_SINGLE;
		setPageComplete(isPageComplete());
		if (amlError) {
			if (getErrorMessage() != null) {
				errorMessage = getErrorMessage();
			}
			setErrorMessage(errorMessage);
		}
		manualOption.setSelection(false);
		loadOption.setSelection(true);
		loadBatchOption.setSelection(false);
	}
	
	private void setToLoadBatchSEE() {
		creationMethod = SEECreationMethod.LOAD_BATCH;
		setPageComplete(isPageComplete());
		if (amlError) {
			if (getErrorMessage() != null) {
				errorMessage = getErrorMessage();
			}
			setErrorMessage(errorMessage);
		}
		manualOption.setSelection(false);
		loadOption.setSelection(false);
		loadBatchOption.setSelection(true);
	}
	
	
	private void createGroupDefaultMappingSelection(Composite parent) {
		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.verticalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		
		Group groupContext = new Group(parent, SWT.NONE);
		groupContext.setLayout(GridLayoutFactory.fillDefaults().numColumns(3)
				.margins(4, 3).create());

		groupContext.setLayoutData(gridData);
		
		defaultMappingOption = new Button(groupContext, SWT.RADIO);
		defaultMappingOption.setLayoutData(GridDataFactory.fillDefaults().span(3, 1).create());
		defaultMappingOption.setText("Default Mapping");
		
		
		loadMappingOption = new Button(groupContext, SWT.RADIO);
		loadMappingOption.setLayoutData(GridDataFactory.fillDefaults().create());
		loadMappingOption.setText("Load Mapping");
		
		loadText = new Text(groupContext, SWT.BORDER | SWT.READ_ONLY);
		loadText.setBackground(getShell().getDisplay().getSystemColor(
				SWT.COLOR_WHITE));
		loadText.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		loadText.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				setToLoadMapping();
			}
		});
		
		final Button loadButton = new Button(groupContext, SWT.PUSH);
		loadButton.setText("...");
		loadButton.setLayoutData(GridDataFactory.fillDefaults().create());
		loadButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				transformationMappingPath = MasterFileDialog.getFilenameFromFileDialog(SupportedFileType.XML);
				loadText.setText(transformationMappingPath);
				setToLoadMapping();
			}
		});
		defaultMappingOption.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setToDefaultMapping();
			}
		});
		loadMappingOption.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setToLoadMapping();
			}
		});
		
		//initial state
		loadMappingOption.setSelection(false);
		defaultMappingOption.setSelection(true);
		setPageComplete(isPageComplete());
	}
	
	private void setToLoadMapping() {
		defaultMapping = false;
		transformationMappingPath = loadText.getText();
		setPageComplete(isPageComplete());
		loadMappingOption.setSelection(true);
		defaultMappingOption.setSelection(false);
	}
	
	private void setToDefaultMapping() {
		defaultMapping = true;
		transformationMappingPath = "DefaultMapping.xml";
		setPageComplete(isPageComplete());
		loadMappingOption.setSelection(false);
		defaultMappingOption.setSelection(true);
	}
	
	public SEECreationMethod getCreationMethod() {
		return creationMethod;
	}
	
	public boolean isDefaultMapping() {
		return defaultMapping;
	}

	public String getTransformationMappingPath() {
		return transformationMappingPath;
	}
	
	@Override
	public boolean isPageComplete() {
		return (((creationMethod == SEECreationMethod.MANUAL) 
				|| (creationMethod == SEECreationMethod.LOAD_SINGLE && see != null && !amlError))
				|| (creationMethod == SEECreationMethod.LOAD_BATCH && !batchFilesMapping.isEmpty()))
				&& (defaultMapping || !transformationMappingPath.isEmpty())
				&& (!useDefaultAddresses || !problemWithDefaultAddress);
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
			setErrorMessage("Not a valid transformation mapping data: " + transformationMappingPath);
			AMLParser.getInstance().wipeData();
			AMLTransformationService.getTransformationProvider().wipeAllData();
			amlError = true;
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
				setErrorMessage("Only 1 SEE is allowed in each AML file");
				AMLTransformationService.getTransformationProvider().wipeAllData();
				AMLParser.getInstance().wipeData();
				amlError = true;
				return null;
			}
		}
		if (see == null) {
			setErrorMessage("The data given is not a valid AML-SEE data: " + seeFilepath);
			AMLParser.getInstance().wipeData();
			AMLTransformationService.getTransformationProvider().wipeAllData();
			amlError = true;
			return null;
		}
		//clean up
		amlError = false;
		errorMessage = null;
		setErrorMessage(null);
		return see;
	}
	
	public SEE getSEE() {
		return see;
	}
	
	public String getSEEFileInput() {
		return seeFileInput;
	}
	
	public boolean usesDefaultAddresses() {
		return useDefaultAddresses;
	}
	
	public Map<String, File> getBatchFilesMapping() {
		return batchFilesMapping;
	}
}
