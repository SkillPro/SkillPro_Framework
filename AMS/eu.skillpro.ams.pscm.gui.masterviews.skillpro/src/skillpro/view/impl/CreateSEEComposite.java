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

package skillpro.view.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import skillpro.ams.util.AMSServiceUtility;
import skillpro.dialogs.ChooseAssetFromConfigurationDialog;
import skillpro.dialogs.ProductConfigurationDialog;
import skillpro.model.assets.AMSCommType;
import skillpro.model.assets.ESCommType;
import skillpro.model.assets.MESCommType;
import skillpro.model.assets.Resource;
import skillpro.model.assets.ResourceConfiguration;
import skillpro.model.assets.SEE;
import skillpro.model.products.ProductQuantity;
import skillpro.model.service.SkillproService;
import skillpro.model.utils.Pair;
import eu.skillpro.ams.pscm.connector.opcua.Activator;
import eu.skillpro.ams.pscm.connector.opcua.OPCUAServerRepository;
import eu.skillpro.ams.pscm.icons.IconActivator;

public class CreateSEEComposite extends Composite {
	private Resource resource;
	private boolean simulation;
	private String defaultResourceConfigurationID = "";
	private Set<ProductQuantity> defaultInputProductQuantities = new HashSet<>();
	private Pair<AMSCommType, String> amsCommunication;
	private Pair<MESCommType, String> mesCommunication;
	private Pair<ESCommType, String> esCommunication;
	private String seeType = "";
	
	private final GridDataFactory buttonGD = GridDataFactory.swtDefaults()
			.align(SWT.FILL, SWT.FILL).grab(true, false).span(2,1);

	private boolean withSimulation;
	private Text resourceText;
	private ComboViewer defaultResourceConfigurationIDComboViewer;
	
	public CreateSEEComposite(Composite parent, SEE see, boolean withSimulation) {
		super(parent, SWT.NONE);
		this.setLayout(GridLayoutFactory.fillDefaults().create());
		if (see != null) {
			resource = see.getResource();
			simulation = see.isSimulation();
			defaultResourceConfigurationID = see.getDefaultResourceConfigurationID();
			defaultInputProductQuantities.addAll(see.getDefaultInputProductQuantities());
			amsCommunication = new Pair<AMSCommType, String>(see.getAMSCommunication());
			mesCommunication = new Pair<MESCommType, String>(see.getMESCommunication());
			esCommunication = new Pair<ESCommType, String>(see.getESCommunication());
			seeType = see.getSEEType();
		} else {
			amsCommunication = new Pair<AMSCommType, String>(AMSCommType.WEBSERVICES, AMSServiceUtility.serviceAddress);
			mesCommunication = new Pair<MESCommType, String>(MESCommType.OPCUA, Activator.getDefault().getCurrentUAaddress());
		}
		this.withSimulation = withSimulation;
		
		Composite container = new Composite(this, SWT.NONE);
		container.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		container.setLayout(GridLayoutFactory.fillDefaults().numColumns(1).margins(5,5).equalWidth(false).create());
		
		createDialogComposite(container);
	}
	
	protected ToolItem createToolItem(ToolBar bar, int style, String text,
			Image image, String tooltip, SelectionListener listener) {
		if (image != null && (text == null && tooltip == null)) {
			throw new IllegalArgumentException(
					"image only items require a tool tip");
		}
		ToolItem ti = new ToolItem(bar, style);
		if (image != null) {
			ti.setImage(image);
		} else {
			if (text != null) {
				ti.setText(text);
			}
		}
		if (tooltip != null) {
			ti.setToolTipText(tooltip);
		}
		if (listener != null) {
			ti.addSelectionListener(listener);
		}
		return ti;
	}

	private void createDialogComposite(Composite parent) {
		createResourceComposite(parent);
		createDefaultIDComposites(parent);
		
		final Label sep = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		sep.setLayoutData(buttonGD.span(1, 1).create());
		
		createConnectionComposite(parent);
		
		final Label sep2 = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		sep2.setLayoutData(buttonGD.span(1, 1).create());
		
		if (withSimulation) {
			final Button simulationButton = new Button(parent, SWT.CHECK);
			simulationButton.setText("Simulation");
			simulationButton.setSelection(false);
			simulationButton.setEnabled(true);
			simulationButton.setLayoutData(buttonGD.create());
			simulationButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					simulation = simulationButton.getSelection();
				}
			});
			
			final Label sep3 = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
			sep3.setLayoutData(buttonGD.span(1, 1).create());
		}
	}
	
	private void createResourceComposite(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).create());
		container.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		
		Label resourceLabel = new Label(container, SWT.NONE);
		resourceLabel.setText("Resource:");
		resourceLabel.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		
		Composite otherContainer = new Composite(container, SWT.NONE);
		otherContainer.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).create());
		otherContainer.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		
		resourceText = new Text(otherContainer, SWT.BORDER | SWT.READ_ONLY);
		resourceText.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		resourceText.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		if (resource != null) {
			resourceText.setText(resource.getName());
		}
		
		Button addResourceButton = new Button(otherContainer, SWT.PUSH);
		addResourceButton.setLayoutData(GridDataFactory.swtDefaults().create());
		addResourceButton.setImage(IconActivator.getImageDescriptor("icons/asset/add.png").createImage());
		
		addResourceButton.addSelectionListener(addResourceListener());
	}

	private void createConnectionComposite(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).create());
		container.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		final Label amsComLabel = new Label(container, SWT.NONE);
		amsComLabel.setText("AMS Communication: ");
		amsComLabel.setLayoutData(buttonGD.span(2, 1).create());
		
		final ComboViewer amsComCV = new ComboViewer(container);
		amsComCV.getControl().setLayoutData(buttonGD.span(1, 1).create());
		amsComCV.setContentProvider(new ArrayContentProvider());
		amsComCV.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((AMSCommType) element).toString();
			}
		});

		amsComCV.setInput(AMSCommType.values());
		if (AMSCommType.values().length > 0) {
			AMSCommType element = AMSCommType.values()[0];
			amsComCV.setSelection(new StructuredSelection(element));
			if (amsCommunication == null) {
				amsCommunication = new Pair<AMSCommType, String>(element, null);
			} else {
				amsCommunication.setFirstElement(element);
			}
		}
		
		final Text amsComText = new Text(container, SWT.BORDER);
		if (amsCommunication != null && amsCommunication.getFirstElement() == AMSCommType.WEBSERVICES && amsCommunication.getSecondElement() == null) {
			String serviceAddress = AMSServiceUtility.serviceAddress;
			amsComText.setText(serviceAddress);
			amsCommunication.setSecondElement(serviceAddress);
		} else {
			amsComText.setText("");
		}
		amsComText.setLayoutData(buttonGD.span(1, 1).create());
		
		final Label mesComLabel = new Label(container, SWT.NONE);
		mesComLabel.setText("MES Communication: ");
		mesComLabel.setLayoutData(buttonGD.span(2, 1).create());
		
		final ComboViewer mesComCV = new ComboViewer(container);
		mesComCV.getControl().setLayoutData(buttonGD.span(1, 1).create());
		mesComCV.setContentProvider(new ArrayContentProvider());
		mesComCV.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((MESCommType) element).toString();
			}
		});

		mesComCV.setInput(MESCommType.values());
		if (MESCommType.values().length > 0) {
			MESCommType element = MESCommType.values()[0];
			mesComCV.setSelection(new StructuredSelection(element));
			if (mesCommunication == null) {
				mesCommunication = new Pair<MESCommType, String>(element, null);
			} else {
				mesCommunication.setFirstElement(element);
			}
		}
		
		final Text mesComText = new Text(container, SWT.BORDER);
		if (mesCommunication != null && mesCommunication.getFirstElement() == MESCommType.OPCUA && mesCommunication.getSecondElement() == null) {
			String serviceAddress = OPCUAServerRepository.getSelectedServerUri();
			mesComText.setText(serviceAddress);
			mesCommunication.setSecondElement(serviceAddress);
		} else {
			mesComText.setText("");
		}
		mesComText.setLayoutData(buttonGD.span(1,1).create());
		
		final Label esComLabel = new Label(container, SWT.NONE);
		esComLabel.setText("ES Communication: ");
		esComLabel.setLayoutData(buttonGD.span(2, 1).create());
		
		final ComboViewer esComCV = new ComboViewer(container);
		esComCV.getControl().setLayoutData(buttonGD.span(1, 1).create());
		esComCV.setContentProvider(new ArrayContentProvider());
		esComCV.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((ESCommType) element).toString();
			}
		});

		esComCV.setInput(ESCommType.values());
		if (ESCommType.values().length > 0) {
			ESCommType element = ESCommType.values()[0];
			esComCV.setSelection(new StructuredSelection(element));
			if (esCommunication == null) {
				esCommunication = new Pair<ESCommType, String>(element, null);
			} else {
				esCommunication.setFirstElement(element);
			}
		}
		
		final Text esComText = new Text(container, SWT.BORDER);
		esComText.setText("");
		esComText.setLayoutData(buttonGD.span(1,1).create());
		
		if (amsCommunication != null) {
			AMSCommType amsFirstElement = amsCommunication.getFirstElement();
			if (amsFirstElement != null) {
				amsComCV.setSelection(new StructuredSelection(amsFirstElement));
			}
			String amsSecondElement = amsCommunication.getSecondElement();
			if (amsSecondElement != null) {
				amsComText.setText(amsSecondElement);
			}
		}
		
		if (mesCommunication != null) {
			MESCommType firstElement = mesCommunication.getFirstElement();
			if (firstElement != null) {
				mesComCV.setSelection(new StructuredSelection(firstElement));
			}
			String secondElement = mesCommunication.getSecondElement();
			if (secondElement != null) {
				mesComText.setText(secondElement);
			}
		}
		
		if (esCommunication != null) {
			ESCommType firstElement = esCommunication.getFirstElement();
			if (firstElement != null) {
				esComCV.setSelection(new StructuredSelection(firstElement));
			}
			String secondElement = esCommunication.getSecondElement();
			if (secondElement != null) {
				esComText.setText(secondElement);
			}
		}
		
		//Listeners
		mesComCV.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = mesComCV.getSelection();
				if (!selection.isEmpty()) {
					MESCommType selectedElement = (MESCommType) ((IStructuredSelection) selection).getFirstElement();
					if (mesCommunication == null) {
						mesCommunication = new Pair<MESCommType, String>(selectedElement, null);
					} else {
						mesCommunication.setFirstElement(selectedElement);
					}
				}
				validate();
			}
		});
		
		mesComText.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				if (mesCommunication == null) {
					mesCommunication = new Pair<MESCommType, String>(null, mesComText.getText());
				} else {
					mesCommunication.setSecondElement(mesComText.getText());
				}
				validate();
			}
		});
		
		esComCV.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = esComCV.getSelection();
				if (!selection.isEmpty()) {
					ESCommType selectedElement = (ESCommType) ((IStructuredSelection) selection).getFirstElement();
					if (esCommunication == null) {
						esCommunication = new Pair<ESCommType, String>(selectedElement, null);
					} else {
						esCommunication.setFirstElement(selectedElement);
					}
				}
				validate();
			}
		});
		
		esComText.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				if (esCommunication == null) {
					esCommunication = new Pair<ESCommType, String>(null, esComText.getText());
				} else {
					esCommunication.setSecondElement(esComText.getText());
				}
				validate();
			}
		});
		
		amsComCV.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = amsComCV.getSelection();
				if (!selection.isEmpty()) {
					AMSCommType selectedElement = (AMSCommType) ((IStructuredSelection) selection).getFirstElement();
					if (amsCommunication == null) {
						amsCommunication = new Pair<AMSCommType, String>(selectedElement, null);
					} else {
						amsCommunication.setFirstElement(selectedElement);
					}
				}
				validate();
			}
		});
		
		amsComText.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				if (amsCommunication == null) {
					amsCommunication = new Pair<AMSCommType, String>(null, amsComText.getText());
				} else {
					amsCommunication.setSecondElement(amsComText.getText());
				}
				validate();
			}
		});
	}

	private void createDefaultIDComposites(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(GridLayoutFactory.fillDefaults().equalWidth(true).numColumns(2).create());
		container.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		final Label defaultResourceConfigurationIDLabel = new Label(container, SWT.NONE);
		defaultResourceConfigurationIDLabel.setText("Default resource configuration ID: ");
		defaultResourceConfigurationIDLabel.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());

		defaultResourceConfigurationIDComboViewer = new ComboViewer(container);
		defaultResourceConfigurationIDComboViewer.setContentProvider(new ArrayContentProvider());
		defaultResourceConfigurationIDComboViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((ResourceConfiguration) element).getName();
			}
		});
		defaultResourceConfigurationIDComboViewer.getControl().setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		if (resource == null) {
			defaultResourceConfigurationIDComboViewer.getControl().setEnabled(false);
		} else {
			List<ResourceConfiguration> resourceConfigurations = resource.getResourceConfigurations();
			defaultResourceConfigurationIDComboViewer.setInput(resourceConfigurations);
			if (defaultResourceConfigurationID != null) {
				ResourceConfiguration defaultResourceConfiguration = null;
				for (ResourceConfiguration rConf : resourceConfigurations) {
					if (rConf.getName().equals(defaultResourceConfigurationID)) {
						defaultResourceConfiguration = rConf;
						break;
					}
				}
				defaultResourceConfigurationIDComboViewer.setSelection(new StructuredSelection(defaultResourceConfiguration));
			}
		}
		
		
		final Label defaultProductConfigurationIDLabel = new Label(container, SWT.NONE);
		defaultProductConfigurationIDLabel.setText("Default product configuration ID: ");
		defaultProductConfigurationIDLabel.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());

		Composite productComposite = new Composite(container, SWT.NONE);
		productComposite.setLayout(GridLayoutFactory.fillDefaults().equalWidth(false).numColumns(2).create());
		productComposite.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		
		final Text defaultProductConfigurationIDText = new Text(productComposite, SWT.BORDER | SWT.READ_ONLY);
		defaultProductConfigurationIDText.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		defaultProductConfigurationIDText.setText("");
		if (defaultInputProductQuantities != null && !defaultInputProductQuantities.isEmpty()) {
			defaultProductConfigurationIDText.setText(Arrays.toString(defaultInputProductQuantities.toArray()));
		}
		defaultProductConfigurationIDText.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		
		final Button addProductsButton = new Button(productComposite, SWT.PUSH);
		addProductsButton.setText("Add");
		addProductsButton.setLayoutData(GridDataFactory.swtDefaults().grab(false, false).create());
		
		final Label seeTypeLabel = new Label(container, SWT.NONE);
		seeTypeLabel.setText("SEE Type: ");
		seeTypeLabel.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());

		final Text seeTypeText = new Text(container, SWT.BORDER);
		seeTypeText.setText(seeType);
		seeTypeText.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		
		seeTypeText.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				seeType = seeTypeText.getText();
			}
		});
		
		defaultResourceConfigurationIDComboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				defaultResourceConfigurationID = ((ResourceConfiguration) ((IStructuredSelection) defaultResourceConfigurationIDComboViewer
						.getSelection()).getFirstElement()).getName();
			}
		});
		
		addProductsButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ProductConfigurationDialog dialog = new ProductConfigurationDialog(getShell());
				if (dialog.open() == Dialog.OK) {
					defaultInputProductQuantities = dialog.getInputs();
					if (defaultInputProductQuantities != null && !defaultInputProductQuantities.isEmpty()) {
						defaultProductConfigurationIDText.setText(Arrays.toString(defaultInputProductQuantities.toArray()));
					}
				}
			}
		});
	}

	private SelectionAdapter addResourceListener() {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ChooseAssetFromConfigurationDialog dialog = new ChooseAssetFromConfigurationDialog(getShell());
				if (dialog.open() == Window.OK) {
					Resource result = dialog.getResult()[0];
					if (result != null) {
						List<SEE> correspondingSEEs = SkillproService.getSkillproProvider().getSEERepo().getCorrespondingSEEs(result);
						if (correspondingSEEs.isEmpty()) {
							changeResource(result);
						} else {
							String seeNames = "";
							SEE otherSEE = null;
							if (correspondingSEEs != null && correspondingSEEs.size() > 1) {
								throw new IllegalArgumentException("Please update this method or SEE's implementation.");
							}
							for (SEE see : correspondingSEEs) {
								seeNames += "\""+ "SEE:" + see.getSeeID() + see.getResource() +"\"\n";
								if (otherSEE != null) {
									throw new IllegalArgumentException("Resource should always be controlled by 1 SEE!" +
											"This SEE violated the rule: " + see.getSeeID());
								}
								otherSEE = see;
							}
							MessageDialog alertDialog = new MessageDialog(Display.getCurrent().getActiveShell() , "VIS-Server not running!", null,
									"The resource you chose is controlled by the following SEE:\n" + seeNames +  "Are you sure you want to assign " +
											"the resource: " + result.getName() + " to this SEE?", MessageDialog.WARNING, new String[] { "Yes", "No"}, 0);
							if (alertDialog.open() == Dialog.OK) {
								otherSEE.removeResource();
								changeResource(result);
							}
						}
					}
					validate();
				}
			}
		};
	}
	
	private void changeResource(Resource resource) {
		this.resource = resource;
		resourceText.setText(resource.getName());
		defaultResourceConfigurationIDComboViewer.getControl().setEnabled(true);
		List<ResourceConfiguration> resourceConfigurations = resource.getResourceConfigurations();
		defaultResourceConfigurationIDComboViewer.setInput(resourceConfigurations);
		if (resourceConfigurations != null && !resourceConfigurations.isEmpty()) {
			defaultResourceConfigurationIDComboViewer.setSelection(new StructuredSelection(resourceConfigurations.get(0)));
		}
	}
	
	public Set<ProductQuantity> getDefaultProductQuantities() {
		return defaultInputProductQuantities;
	}
	
	public String getDefaultResourceConfigurationID() {
		return defaultResourceConfigurationID;
	}
	
	public boolean isSimulation() {
		return simulation;
	}
	
	public Pair<AMSCommType, String> getAmsCommunication() {
		return amsCommunication;
	}
	
	public Pair<MESCommType, String> getMesCommunication() {
		return mesCommunication;
	}
	
	public Resource getResource() {
		return resource;
	}
	
	public Pair<ESCommType, String> getEsCommunication() {
		return esCommunication;
	}
	
	public String getSEEType() {
		return seeType;
	}
	
	//TO OVERRIDE
	public boolean validate() {
		return true;
	}
}
