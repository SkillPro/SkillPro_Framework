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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.dialogs.SelectionDialog;

import skillpro.model.assets.Resource;
import skillpro.model.assets.ResourceConfiguration;
import skillpro.model.assets.Setup;
import skillpro.model.products.ProductConfiguration;
import skillpro.model.products.ProductQuantity;
import skillpro.model.service.SkillproService;
import skillpro.model.skills.PrePostRequirement;
import skillpro.model.skills.Requirement;
import skillpro.model.skills.RequirementProductConfigType;
import skillpro.model.skills.RequirementResourceConfigType;
import skillpro.model.skills.RequirementSkillType;
import skillpro.model.skills.ResourceSkill;
import skillpro.model.skills.SkillSynchronizationType;
import skillpro.providers.product.PQTableContentProvider;
import skillpro.providers.product.PQTableLabelProvider;

public class AddRequirementPairDialog extends SelectionDialog {
	private final static String TITLE = "Add a new Requirement";
	
	private ResourceSkill mainResourceSkill;
	private ResourceSkill requiredResourceSkill;
	private boolean isRequiredRSkillEditable = true;
	private RequirementProductConfigType preProductConfigType = RequirementProductConfigType.SPECIFIC;
	private RequirementProductConfigType postProductConfigType = RequirementProductConfigType.SAME;
	private RequirementResourceConfigType preResourceConfigType = RequirementResourceConfigType.SPECIFIC;
	private RequirementResourceConfigType postResourceConfigType = RequirementResourceConfigType.SPECIFIC;
	private ResourceConfiguration preResourceConfiguration;
	private ResourceConfiguration postResourceConfiguration;
	private ProductConfiguration preProductConfiguration;
	private ProductConfiguration postProductConfiguration;
	private SkillSynchronizationType syncType;

	private Resource selectedResource;

	private Set<ProductQuantity> preProductQuantities = new HashSet<>();
	private Set<ProductQuantity> postProductQuantities = new HashSet<>();
	private ComboViewer preResourceConfigCV;
	private ComboViewer postResourceConfigCV;
	
	public AddRequirementPairDialog(Shell parentShell, ResourceSkill resourceSkill) {
		super(parentShell);
		setTitle(TITLE);
		boolean found = false;
		for (PrePostRequirement pair : resourceSkill.getPrePostRequirements()) {
			if (resourceSkill.equals(pair.getPreRequirement().getRequiredResourceSkill())) {
				found = true;
			}
		}
		
		if (!found) {
			isRequiredRSkillEditable = false;
			requiredResourceSkill = resourceSkill;
			selectedResource = requiredResourceSkill.getResource();
		}
		this.mainResourceSkill = resourceSkill;
	}
	
	public AddRequirementPairDialog(Shell parentShell, PrePostRequirement requirementPair) {
		super(parentShell);
		setTitle(TITLE);
		Requirement preRequirement = requirementPair.getPreRequirement();
		Requirement postRequirement = requirementPair.getPostRequirement();
		
		requiredResourceSkill = preRequirement.getRequiredResourceSkill();
		this.mainResourceSkill = preRequirement.getMainResourceSkill();
		if (requiredResourceSkill.equals(mainResourceSkill)) {
			isRequiredRSkillEditable = false;
		}
		selectedResource = requiredResourceSkill.getResource();
		
		preProductConfiguration = preRequirement.getRequiredProductConfiguration();
		preResourceConfiguration = preRequirement.getRequiredResourceConfiguration();
		preResourceConfigType = preRequirement.getResourceConfigType();
		preProductConfigType = preRequirement.getProductConfigType();
		
		postProductConfiguration = postRequirement.getRequiredProductConfiguration();
		postResourceConfiguration = postRequirement.getRequiredResourceConfiguration();
		postResourceConfigType = postRequirement.getResourceConfigType();
		postProductConfigType = postRequirement.getProductConfigType();
		syncType = preRequirement.getSyncType();
		if (syncType != postRequirement.getSyncType()) {
			throw new IllegalArgumentException("Sync type is not the same between pre and post requirements!");
		}
	}
	
	@Override
	protected boolean isResizable() {
		return false;
	}
	
	@Override
	public PrePostRequirement[] getResult() {
		if (getReturnCode() == CANCEL) {
			return null;
		} else {
			if (preProductConfigType == RequirementProductConfigType.SPECIFIC) {
				preProductConfiguration = new ProductConfiguration(UUID.randomUUID().toString(), preProductQuantities);
			} else {
				preProductConfiguration = null;
			}
			
			if (postProductConfigType == RequirementProductConfigType.SPECIFIC) {
				postProductConfiguration = new ProductConfiguration(UUID.randomUUID().toString(), postProductQuantities);
			} else {
				postProductConfiguration = null;
			}
			
			Requirement preRequirement = new Requirement(RequirementSkillType.RESOURCE_SKILL,
					preResourceConfigType, preProductConfigType,
					preResourceConfiguration, preProductConfiguration, requiredResourceSkill, mainResourceSkill, syncType);
			//if same
			if (postResourceConfigType == RequirementResourceConfigType.SAME) {
				postResourceConfiguration = preResourceConfiguration;
			}
			if (postProductConfigType == RequirementProductConfigType.SAME) {
				if (preProductConfiguration != null) {
					postProductConfiguration = new ProductConfiguration(UUID.randomUUID().toString(),
							preProductConfiguration.getProductQuantities());
				}
			}
			Requirement postRequirement = new Requirement(RequirementSkillType.SAME,
					postResourceConfigType, postProductConfigType,
					postResourceConfiguration, postProductConfiguration, requiredResourceSkill, mainResourceSkill, syncType);
			//returns a pair of requirements
			PrePostRequirement result = new PrePostRequirement(preRequirement, postRequirement);
			return new PrePostRequirement[]{ result };
		}
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		container.setLayout(GridLayoutFactory.fillDefaults().margins(5,5).equalWidth(false).create());
		
		createMainComposite(container);
		
		return area;
	}

	private void createMainComposite(Composite container) {
		createResourceSkillComposite(container);
		Label separator = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		createSkillSyncComposite(container);
		Label separator2 = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator2.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		createRequirementsComposite(container);
	}
	
	private void createResourceSkillComposite(Composite parent) {
		GridDataFactory gdGrab =  GridDataFactory.fillDefaults().align(SWT.FILL,
				SWT.CENTER).copy().grab(true, false);
		Composite resourceSkillComposite = new Composite(parent, SWT.NONE);
		resourceSkillComposite.setLayout(GridLayoutFactory.fillDefaults().equalWidth(false).numColumns(2).margins(3, 4).create());
		resourceSkillComposite.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		
		Label resourceLabel = new Label(resourceSkillComposite, SWT.NONE);
		resourceLabel.setText("Resource");
		resourceLabel.setLayoutData(GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).grab(false, false).create());
		
		final ComboViewer resourceCV = new ComboViewer(resourceSkillComposite);
		resourceCV.getControl().setLayoutData(gdGrab.create());
		resourceCV.setContentProvider(new ArrayContentProvider());
		resourceCV.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((Resource) element).getName();
			}
		});
		List<Resource> resourceInput = new ArrayList<>(SkillproService.getSkillproProvider()
				.getAssetRepo().getAllAssignedResources());
		for (PrePostRequirement pair : mainResourceSkill.getPrePostRequirements()) {
			//FIXME assuming that every pairs have a resource skill
			Resource existingResource = pair.getPreRequirement().getRequiredResourceSkill().getResource();
			if (selectedResource == null || !selectedResource.equals(existingResource)) {
				resourceInput.remove(existingResource);
			}
		}
		resourceCV.setInput(resourceInput);
		if (requiredResourceSkill != null) {
			resourceCV.setSelection(new StructuredSelection(selectedResource));
		} else if (resourceInput.size() > 0) {
				resourceCV.setSelection(new StructuredSelection(resourceInput.get(0)));
				selectedResource = resourceInput.get(0);
		}
		resourceCV.getControl().setEnabled(isRequiredRSkillEditable);
		
		Label resourceSkillLabel = new Label(resourceSkillComposite, SWT.NONE);
		resourceSkillLabel.setText("ResourceSkill");
		resourceSkillLabel.setLayoutData(GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).grab(false, false).create());
		
		final ComboViewer resourceSkillCV = new ComboViewer(resourceSkillComposite);
		resourceSkillCV.getControl().setLayoutData(gdGrab.create());
		resourceSkillCV.setContentProvider(new ArrayContentProvider());
		resourceSkillCV.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((ResourceSkill) element).getName();
			}
		});
		
		final List<ResourceSkill> input = new ArrayList<>();
		for (Setup setup : selectedResource.getSetups()) {
			input.addAll(setup.getResourceSkills());
		}
		if (requiredResourceSkill != null) {
			resourceSkillCV.setSelection(new StructuredSelection(requiredResourceSkill));
		} else {
			resourceSkillCV.setSelection(new StructuredSelection(input.get(0)));
			requiredResourceSkill = input.get(0);
		}
		resourceSkillCV.setInput(input);
		if (requiredResourceSkill != null) {
			resourceSkillCV.setSelection(new StructuredSelection(requiredResourceSkill));
		} else if (input.size() > 0) {
			resourceSkillCV.setSelection(new StructuredSelection(input.get(0)));
			requiredResourceSkill = input.get(0);
		}
		resourceSkillCV.getControl().setEnabled(isRequiredRSkillEditable);
		resourceSkillCV.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				requiredResourceSkill = (ResourceSkill) ((IStructuredSelection) resourceSkillCV
						.getSelection()).getFirstElement();
				validate();
			}
		});
		
		resourceCV.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				selectedResource = (Resource) ((IStructuredSelection) resourceCV
						.getSelection()).getFirstElement();
				List<ResourceConfiguration> resourceConfigInput = selectedResource != null ? new ArrayList<>(selectedResource
						.getResourceConfigurations()) : new ArrayList<ResourceConfiguration>();
				preResourceConfigCV.setInput(resourceConfigInput);

				if (resourceConfigInput.size() > 0) {
					preResourceConfigCV.setSelection(new StructuredSelection(resourceConfigInput.get(0)));
					preResourceConfiguration = resourceConfigInput.get(0);
				}
				
				postResourceConfigCV.setInput(resourceConfigInput);

				if (resourceConfigInput.size() > 0) {
					postResourceConfigCV.setSelection(new StructuredSelection(resourceConfigInput.get(0)));
					postResourceConfiguration = resourceConfigInput.get(0);
				}
				input.clear();
				if (selectedResource != null) {
					for (Setup setup : selectedResource.getSetups()) {
						input.addAll(setup.getResourceSkills());
					}
				}
				resourceSkillCV.refresh();
				resourceSkillCV.setSelection(new StructuredSelection(input.get(0)));
				requiredResourceSkill = input.get(0);
				validate();
			}
		});
	}
	
	private void createSkillSyncComposite(Composite parent) {
		GridDataFactory gdGrab =  GridDataFactory.fillDefaults().align(SWT.FILL,
				SWT.CENTER).copy().grab(true, false);
		Composite skillSyncComposite = new Composite(parent, SWT.NONE);
		skillSyncComposite.setLayout(GridLayoutFactory.fillDefaults().equalWidth(false).numColumns(2).margins(3, 4).create());
		skillSyncComposite.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		
		
		Label skillSyncLabel = new Label(skillSyncComposite, SWT.NONE);
		skillSyncLabel.setText("Skill Sync Type");
		skillSyncLabel.setLayoutData(GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).grab(false, false).create());
		
		final ComboViewer skillSyncCV = new ComboViewer(skillSyncComposite);
		skillSyncCV.getControl().setLayoutData(gdGrab.create());
		skillSyncCV.setContentProvider(new ArrayContentProvider());
		skillSyncCV.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((SkillSynchronizationType) element).getName();
			}
		});
		
		final List<SkillSynchronizationType> input = new ArrayList<>();
		for (SkillSynchronizationType type : SkillSynchronizationType.values()) {
			input.add(type);
		}
		skillSyncCV.setInput(input);

		if (syncType != null) {
			skillSyncCV.setSelection(new StructuredSelection(syncType));
		} else {
			skillSyncCV.setSelection(new StructuredSelection(SkillSynchronizationType.NONE));
			syncType = SkillSynchronizationType.NONE;
		}
		skillSyncCV.refresh();
		skillSyncCV.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				syncType = (SkillSynchronizationType) ((IStructuredSelection) skillSyncCV
						.getSelection()).getFirstElement();
				validate();
			}
		});
		
		
	}
	
	
	private void createRequirementsComposite(Composite parent) {
		Composite requirementsComposite = new Composite(parent, SWT.NONE);
		requirementsComposite.setLayout(GridLayoutFactory.fillDefaults().numColumns(3).margins(3, 4).create());
		requirementsComposite.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		
		createPreRequirementComposite(requirementsComposite);
		Label separator = new Label(requirementsComposite, SWT.SEPARATOR | SWT.VERTICAL);
		separator.setLayoutData(GridDataFactory.fillDefaults().grab(false, true).create());
		createPostRequirementComposite(requirementsComposite);
	}
	
	private void createPreRequirementComposite(Composite parent) {
		GridDataFactory gdGrab =  GridDataFactory.fillDefaults().align(SWT.FILL,
				SWT.CENTER).copy().grab(true, false);
		Composite preRequirementComposite = new Composite(parent, SWT.NONE);
		preRequirementComposite.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).margins(1, 1).create());
		preRequirementComposite.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		//center label
		Label preRequirementLabel = new Label(preRequirementComposite, SWT.NONE);
		preRequirementLabel.setText("Pre-Requirement");
		preRequirementLabel.setLayoutData(GridDataFactory.fillDefaults().span(2, 1).align(SWT.CENTER, SWT.CENTER)
				.grab(true, false).create());
		//ResourceConfigurationType
		Label resourceConfigTypeLabel = new Label(preRequirementComposite, SWT.NONE);
		resourceConfigTypeLabel.setText("Resource Config Type");
		resourceConfigTypeLabel.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		
		final ComboViewer resourceConfigTypeCV = new ComboViewer(preRequirementComposite);
		resourceConfigTypeCV.getControl().setLayoutData(gdGrab.create());
		resourceConfigTypeCV.setContentProvider(new ArrayContentProvider());
		resourceConfigTypeCV.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((RequirementResourceConfigType) element).toString();
			}
		});
		List<RequirementResourceConfigType> resourceConfigurationTypeInput = Arrays.asList(RequirementResourceConfigType.values());
		resourceConfigurationTypeInput = new ArrayList<>(resourceConfigurationTypeInput);
		for (Iterator<RequirementResourceConfigType> iter = resourceConfigurationTypeInput.iterator();
				iter.hasNext();) {
			RequirementResourceConfigType type = iter.next();
			if (type.toString().equalsIgnoreCase("different_any")
					|| type.toString().equalsIgnoreCase("same")) {
				iter.remove();
			}
		}
		resourceConfigTypeCV.setInput(resourceConfigurationTypeInput);

		if (preResourceConfigType != null) {
			resourceConfigTypeCV.setSelection(new StructuredSelection(preResourceConfigType));
		} else if (resourceConfigurationTypeInput.size() > 0) {
			resourceConfigTypeCV.setSelection(new StructuredSelection(resourceConfigurationTypeInput.get(0)));
			preResourceConfigType = resourceConfigurationTypeInput.get(0);
		}
		
		//ResourceConfiguration
		Label resourceConfigurationLabel = new Label(preRequirementComposite, SWT.NONE);
		resourceConfigurationLabel.setText("Resource Configuration");
		resourceConfigurationLabel.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		
		preResourceConfigCV = new ComboViewer(preRequirementComposite);
		preResourceConfigCV.getControl().setLayoutData(gdGrab.create());
		preResourceConfigCV.setContentProvider(new ArrayContentProvider());
		preResourceConfigCV.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((ResourceConfiguration) element).getName();
			}
		});
		List<ResourceConfiguration> resourceConfigInput = requiredResourceSkill != null ? new ArrayList<>(requiredResourceSkill
				.getResource().getResourceConfigurations()) : new ArrayList<ResourceConfiguration>();
		preResourceConfigCV.setInput(resourceConfigInput);

		if (preResourceConfiguration != null) {
			preResourceConfigCV.setSelection(new StructuredSelection(preResourceConfiguration));
		} else if (resourceConfigInput.size() > 0) {
			preResourceConfigCV.setSelection(new StructuredSelection(resourceConfigInput.get(0)));
			preResourceConfiguration = resourceConfigInput.get(0);
		}
		if (preResourceConfigType != RequirementResourceConfigType.SPECIFIC) {
			preResourceConfigCV.getControl().setEnabled(false);
		}
		
		//ProductConfigurationType
		Label productConfigurationTypeLabel = new Label(preRequirementComposite, SWT.NONE);
		productConfigurationTypeLabel.setText("Product Config Type");
		productConfigurationTypeLabel.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		
		final ComboViewer productConfigTypeCV = new ComboViewer(preRequirementComposite);
		productConfigTypeCV.getControl().setLayoutData(gdGrab.create());
		productConfigTypeCV.setContentProvider(new ArrayContentProvider());
		productConfigTypeCV.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((RequirementProductConfigType) element).toString();
			}
		});
		List<RequirementProductConfigType> productConfigTypeInput = Arrays.asList(RequirementProductConfigType.values());
		productConfigTypeInput = new ArrayList<>(productConfigTypeInput);
		for (Iterator<RequirementProductConfigType> iter = productConfigTypeInput.iterator();
				iter.hasNext();) {
			RequirementProductConfigType type = iter.next();
			if (type.toString().equalsIgnoreCase("same")) {
				iter.remove();
			}
		}
		productConfigTypeCV.setInput(productConfigTypeInput);
		if (preProductConfigType != null) {
			productConfigTypeCV.setSelection(new StructuredSelection(preProductConfigType));
		} else if (productConfigTypeInput.size() > 0) {
			productConfigTypeCV.setSelection(new StructuredSelection(productConfigTypeInput.get(0)));
			preProductConfigType = productConfigTypeInput.get(0);
		}
		
		//ProductConfiguration
		Label ProductConfigurationLabel = new Label(preRequirementComposite, SWT.NONE);
		ProductConfigurationLabel.setText("Product Configuration");
		ProductConfigurationLabel.setLayoutData(GridDataFactory.fillDefaults().span(2, 1).grab(true, false).create());
		

		String[] headers = { "Name", "Quantity" };
		int[] bounds = { 100, 100 };

		// input
		if (preProductConfiguration!= null) {
			preProductQuantities.addAll(preProductConfiguration.getProductQuantities());
		}
		final TableViewer preProductQuantitiesTableViewer = createTableViewer(preRequirementComposite, headers, bounds);
		
		preProductQuantitiesTableViewer.setInput(preProductQuantities);
		preProductQuantitiesTableViewer.getControl().setLayoutData(GridDataFactory.fillDefaults().span(2, 1).grab(true, true)
				.hint(0, 200).create());


		// ADD Buttons
		Composite inputButtonsArea = new Composite(preRequirementComposite, SWT.NONE);
		inputButtonsArea.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).create());

		Button addPreProductQuantityButton = new Button(inputButtonsArea, SWT.PUSH);
		addPreProductQuantityButton.setText("Add");
		Button deleteProductQuantityButton = new Button(inputButtonsArea, SWT.PUSH);
		deleteProductQuantityButton.setText("Delete");

		if (preProductConfigType != RequirementProductConfigType.SPECIFIC) {
			preProductQuantitiesTableViewer.getControl().setEnabled(false);
		}
		
		//Listeners!!
		resourceConfigTypeCV.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				preResourceConfigType = (RequirementResourceConfigType) ((IStructuredSelection) resourceConfigTypeCV
						.getSelection()).getFirstElement();
				if (preResourceConfigType == RequirementResourceConfigType.SPECIFIC) {
					preResourceConfigCV.getControl().setEnabled(true);
					preResourceConfiguration = (ResourceConfiguration) ((IStructuredSelection) preResourceConfigCV
							.getSelection()).getFirstElement();
				} else {
					preResourceConfigCV.getControl().setEnabled(false);
					preResourceConfiguration = null;
				}
				validate();
			}
		});
		
		preResourceConfigCV.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				preResourceConfiguration = (ResourceConfiguration) ((IStructuredSelection) preResourceConfigCV
						.getSelection()).getFirstElement();
				validate();
			}
		});
		
		productConfigTypeCV.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				preProductConfigType = (RequirementProductConfigType) ((IStructuredSelection) productConfigTypeCV
						.getSelection()).getFirstElement();
				if (preProductConfigType == RequirementProductConfigType.SPECIFIC) {
					preProductQuantitiesTableViewer.getControl().setEnabled(true);
				} else {
					preProductQuantitiesTableViewer.getControl().setEnabled(false);
				}
				validate();
			}
		});
		
		addPreProductQuantityButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				AddInputOutputDialog dialog = new AddInputOutputDialog(getShell(), ProductDialogType.INPUT_DIALOG, preProductQuantities);
				if (dialog.open() == Window.OK) {
					for (ProductQuantity pq : dialog.getResult()) {
						preProductQuantities.add(pq);
					}
					preProductQuantitiesTableViewer.refresh();
					validate();
				}
				
			}
		});

		deleteProductQuantityButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				deleteSelected(preProductQuantitiesTableViewer);
				preProductQuantitiesTableViewer.refresh();
				validate();
				
			}
		});
		
	}
	
	private void createPostRequirementComposite(Composite parent) {
		GridDataFactory gdGrab =  GridDataFactory.fillDefaults().align(SWT.FILL,
				SWT.CENTER).copy().grab(true, false);
		Composite postRequirementComposite = new Composite(parent, SWT.NONE);
		postRequirementComposite.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).margins(1, 1).create());
		postRequirementComposite.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		//center label
		Label postRequirementLabel = new Label(postRequirementComposite, SWT.NONE);
		postRequirementLabel.setText("Post-Requirement");
		postRequirementLabel.setLayoutData(GridDataFactory.fillDefaults().span(2, 1).align(SWT.CENTER, SWT.CENTER)
				.grab(true, false).create());
		//ResourceConfigurationType
		Label resourceConfigTypeLabel = new Label(postRequirementComposite, SWT.NONE);
		resourceConfigTypeLabel.setText("Resource Config Type");
		resourceConfigTypeLabel.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		
		final ComboViewer resourceConfigTypeCV = new ComboViewer(postRequirementComposite);
		resourceConfigTypeCV.getControl().setLayoutData(gdGrab.create());
		resourceConfigTypeCV.setContentProvider(new ArrayContentProvider());
		resourceConfigTypeCV.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((RequirementResourceConfigType) element).toString();
			}
		});
		List<RequirementResourceConfigType> resourceConfigurationTypeInput = Arrays.asList(RequirementResourceConfigType.values());
		resourceConfigTypeCV.setInput(resourceConfigurationTypeInput);
		
		if (postResourceConfigType != null) {
			resourceConfigTypeCV.setSelection(new StructuredSelection(postResourceConfigType));
		} else if (resourceConfigurationTypeInput.size() > 0) {
			resourceConfigTypeCV.setSelection(new StructuredSelection(resourceConfigurationTypeInput.get(0)));
			postResourceConfigType = resourceConfigurationTypeInput.get(0);
		}
		
		//ResourceConfiguration
		Label resourceConfigurationLabel = new Label(postRequirementComposite, SWT.NONE);
		resourceConfigurationLabel.setText("Resource Configuration");
		resourceConfigurationLabel.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		
		postResourceConfigCV = new ComboViewer(postRequirementComposite);
		postResourceConfigCV.getControl().setLayoutData(gdGrab.create());
		postResourceConfigCV.setContentProvider(new ArrayContentProvider());
		postResourceConfigCV.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((ResourceConfiguration) element).getName();
			}
		});
		List<ResourceConfiguration> resourceConfigInput = requiredResourceSkill != null ? new ArrayList<>(requiredResourceSkill
				.getResource().getResourceConfigurations()) : new ArrayList<ResourceConfiguration>();
		postResourceConfigCV.setInput(resourceConfigInput);
		if (postResourceConfiguration != null) {
			postResourceConfigCV.setSelection(new StructuredSelection(postResourceConfiguration));
			
		} else if (resourceConfigInput.size() > 0) {
			postResourceConfigCV.setSelection(new StructuredSelection(resourceConfigInput.get(0)));
			postResourceConfiguration = resourceConfigInput.get(0);
		}
		if (postResourceConfigType != RequirementResourceConfigType.SPECIFIC) {
			postResourceConfigCV.getControl().setEnabled(false);
		}
		
		//ProductConfigurationType
		Label productConfigurationTypeLabel = new Label(postRequirementComposite, SWT.NONE);
		productConfigurationTypeLabel.setText("Product Config Type");
		productConfigurationTypeLabel.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		
		final ComboViewer productConfigTypeCV = new ComboViewer(postRequirementComposite);
		productConfigTypeCV.getControl().setLayoutData(gdGrab.create());
		productConfigTypeCV.setContentProvider(new ArrayContentProvider());
		productConfigTypeCV.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((RequirementProductConfigType) element).toString();
			}
		});
		List<RequirementProductConfigType> productConfigTypeInput = Arrays.asList(RequirementProductConfigType.values());
		productConfigTypeInput = new ArrayList<>(productConfigTypeInput);
		for (Iterator<RequirementProductConfigType> iter = productConfigTypeInput.iterator();
				iter.hasNext();) {
			RequirementProductConfigType type = iter.next();
			if (type.toString().equalsIgnoreCase("any")) {
				iter.remove();
			}
		}
		productConfigTypeCV.setInput(productConfigTypeInput);
		
		if (postProductConfigType != null) {
			productConfigTypeCV.setSelection(new StructuredSelection(postProductConfigType));
		} else if (productConfigTypeInput.size() > 0) {
			productConfigTypeCV.setSelection(new StructuredSelection(productConfigTypeInput.get(0)));
			postProductConfigType = productConfigTypeInput.get(0);
		}
		
		//ProductConfiguration
		Label ProductConfigurationLabel = new Label(postRequirementComposite, SWT.NONE);
		ProductConfigurationLabel.setText("Product Configuration");
		ProductConfigurationLabel.setLayoutData(GridDataFactory.fillDefaults().span(2, 1).grab(true, false).create());
		

		String[] headers = { "Name", "Quantity" };
		int[] bounds = { 100, 100 };

		// input
		if (postProductConfiguration!= null) {
			postProductQuantities.addAll(postProductConfiguration.getProductQuantities());
		}
		final TableViewer postProductQuantitiesTableViewer = createTableViewer(postRequirementComposite, headers, bounds);
		
		postProductQuantitiesTableViewer.setInput(postProductQuantities);
		postProductQuantitiesTableViewer.getControl().setLayoutData(GridDataFactory.fillDefaults().span(2, 1).grab(true, true)
				.hint(0, 200).create());


		// ADD Buttons
		Composite inputButtonsArea = new Composite(postRequirementComposite, SWT.NONE);
		inputButtonsArea.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).create());

		final Button addPostProductQuantityButton = new Button(inputButtonsArea, SWT.PUSH);
		addPostProductQuantityButton.setText("Add");
		final Button deletePostProductQuantityButton = new Button(inputButtonsArea, SWT.PUSH);
		deletePostProductQuantityButton.setText("Delete");

		if (postProductConfigType != RequirementProductConfigType.SPECIFIC) {
			postProductQuantitiesTableViewer.getControl().setEnabled(false);
		}
		
		//Listeners!!
		resourceConfigTypeCV.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				postResourceConfigType = (RequirementResourceConfigType) ((IStructuredSelection) resourceConfigTypeCV
						.getSelection()).getFirstElement();
				if (postResourceConfigType == RequirementResourceConfigType.SPECIFIC) {
					postResourceConfigCV.getControl().setEnabled(true);
					postResourceConfiguration = (ResourceConfiguration) ((IStructuredSelection) postResourceConfigCV
							.getSelection()).getFirstElement();
				} else {
					if (postResourceConfigType == RequirementResourceConfigType.SAME) {
						if (preResourceConfiguration != null) {
							postResourceConfigCV.setSelection(new StructuredSelection(preResourceConfiguration));
						}
					}
					postResourceConfiguration = null;
					postResourceConfigCV.getControl().setEnabled(false);
					
				}
				validate();
			}
		});
		
		postResourceConfigCV.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				postResourceConfiguration = (ResourceConfiguration) ((IStructuredSelection) postResourceConfigCV
						.getSelection()).getFirstElement();
				validate();
			}
		});
		
		productConfigTypeCV.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				postProductConfigType = (RequirementProductConfigType) ((IStructuredSelection) productConfigTypeCV
						.getSelection()).getFirstElement();
				if (postProductConfigType == RequirementProductConfigType.SPECIFIC) {
					postProductQuantitiesTableViewer.getControl().setEnabled(true);
				} else {
					postProductQuantitiesTableViewer.getControl().setEnabled(false);
				}
				validate();
			}
		});
		
		addPostProductQuantityButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				AddInputOutputDialog dialog = new AddInputOutputDialog(getShell(), ProductDialogType.INPUT_DIALOG, postProductQuantities);
				if (dialog.open() == Window.OK) {
					for (ProductQuantity pq : dialog.getResult()) {
						postProductQuantities.add(pq);
					}
					postProductQuantitiesTableViewer.refresh();
					validate();
				}
				
			}
		});

		deletePostProductQuantityButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				deleteSelected(postProductQuantitiesTableViewer);
				postProductQuantitiesTableViewer.refresh();
				validate();
				
			}
		});
	}
	
	private void validate() {
		boolean valid = true;
		//TODO implement this method!
		getOkButton().setEnabled(valid);
	}
	
	private TableViewer createTableViewer(Composite container,
			String[] headers, int[] bounds) {
		TableViewer tableViewer = new TableViewer(container);
		for (int i = 0; i < headers.length; i++) {
			TableViewerColumn col = new TableViewerColumn(tableViewer, SWT.V_SCROLL);
			col.getColumn().setText(headers[i]);
			col.getColumn().setWidth(bounds[i]);
			col.getColumn().setResizable(true);
		}
		Table table = tableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		GridDataFactory gdInput = GridDataFactory.fillDefaults()
				.align(SWT.FILL, SWT.FILL).grab(true, true)
				.hint(SWT.DEFAULT, 6 * table.getItemHeight());
		table.setLayoutData(gdInput.create());
		tableViewer.setLabelProvider(new PQTableLabelProvider());
		tableViewer.setContentProvider(new PQTableContentProvider());
		return tableViewer;
	}
	
	private void deleteSelected(TableViewer viewer) {
		Collection<?> input = (Collection<?>) viewer.getInput();
		for (TableItem tableItem : viewer.getTable().getSelection()) {
			input.remove(tableItem.getData());
		}
	}
}