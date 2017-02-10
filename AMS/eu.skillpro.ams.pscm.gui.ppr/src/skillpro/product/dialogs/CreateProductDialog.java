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

package skillpro.product.dialogs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.SelectionDialog;

import skillpro.dialogs.AddInputOutputDialog;
import skillpro.dialogs.ProductDialogType;
import skillpro.model.assets.Factory;
import skillpro.model.assets.FactoryNode;
import skillpro.model.products.Product;
import skillpro.model.products.ProductQuantity;
import skillpro.model.products.Supplier;
import skillpro.model.products.Supply;
import skillpro.model.service.SkillproService;
import skillpro.model.skills.ProductionSkill;
import skillpro.model.skills.TemplateSkill;
import skillpro.providers.product.PQTableContentProvider;
import skillpro.providers.product.PQTableLabelProvider;

public class CreateProductDialog extends SelectionDialog {
	private static final String TITLE = "Create a new Product or Production Skill";

	private final Set<ProductQuantity> inputs = new HashSet<>();
	private final Set<ProductQuantity> outputs = new HashSet<>();
	private String productionSkillName;
	private TemplateSkill templateSkill;

	private String productName;
	private boolean disposable;
	private boolean purchasable;
	private String supplyName;
	private String supplierName;
	private Factory factory;

	private TableViewer inputTableViewer;
	private TableViewer outputTableViewer;
	private Button addInputButton;
	private Button deleteInputButton;
	private Button addOutputButton;
	private Button deleteOutputButton;
	private boolean isProduct = true;
	private boolean modifyDialog = false;

	// Used when the dialog is a modify Dialog
	private ProductionSkill productionSkill; 
	private Product product;

	public CreateProductDialog(Shell parentShell) {
		super(parentShell);
		setTitle(TITLE);
	}

	public CreateProductDialog(Shell parentShell, Product product) {
		super(parentShell);
		this.product = product;
		setTitle("Modify Production Skill");

		isProduct = true;
		modifyDialog = true;
	}
	
	public CreateProductDialog(Shell parentShell, ProductionSkill skill) {
		super(parentShell);
		this.productionSkill = skill;
		setTitle("Modify Production Skill");

		isProduct = false;
		modifyDialog = true;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(GridDataFactory.fillDefaults()
				.grab(true, false).create());
		container.setLayout(GridLayoutFactory.fillDefaults().margins(5, 5)
				.equalWidth(false).create());

		GridDataFactory gDFactory = GridDataFactory.swtDefaults()
				.align(SWT.FILL, SWT.FILL).grab(true, false).span(2, 1);

		final Button productButton = new Button(container, SWT.RADIO);
		productButton.setText("Product");
		productButton.setSelection(isProduct);

		final Button productionSkillButton = new Button(container, SWT.RADIO);
		productionSkillButton.setText("Production Skill");
		productionSkillButton.setSelection(!isProduct);

		if (modifyDialog) {
			productButton.setEnabled(false);
			productionSkillButton.setEnabled(false);
			if (productionSkill != null) {
				productionSkillButton.setSelection(true);
			} else if (product != null) {
				productButton.setSelection(true);
			}
		}

		final Label sep = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
		sep.setLayoutData(gDFactory.create());

		final Composite pageComposite = new Composite(container, SWT.NONE);
		pageComposite.setLayoutData(gDFactory.create());
		final StackLayout layout = new StackLayout();
		pageComposite.setLayout(layout);
		final Composite productionSkillPage = createProductionSkillPage(pageComposite);
		final Composite productPage = createProductPage(pageComposite);

		layout.topControl = isProduct ? productPage: productionSkillPage;
		pageComposite.layout();
		// createProjectsComposite(container);
		SelectionListener listener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.getSource() instanceof Button) {
					Button source = (Button) e.getSource();

					if (source == productButton) {
						isProduct = true;
						layout.topControl = productPage;

					} else if (source == productionSkillButton) {
						isProduct = false;
						layout.topControl = productionSkillPage;
					}
					pageComposite.layout();
					validate();
				}
			}
		};

		productButton.addSelectionListener(listener);
		productionSkillButton.addSelectionListener(listener);

		return area;
	}

	private Composite createProductionSkillPage(Composite parent) {
		GridLayoutFactory gridLayoutSingle = GridLayoutFactory.swtDefaults();
		GridLayoutFactory gridLayoutDouble = GridLayoutFactory.swtDefaults().numColumns(2);
		GridDataFactory gd = GridDataFactory.fillDefaults().align(SWT.FILL,
				SWT.CENTER);
		GridDataFactory gdGrab = gd.copy().grab(true, false);

		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(gridLayoutSingle.create());

		Composite topArea = new Composite(container, SWT.NONE);
		topArea.setLayoutData(gdGrab.create());
		topArea.setLayout(gridLayoutDouble.create());

		final Label productionSkillLabel = new Label(topArea, SWT.NONE);
		productionSkillLabel.setText("Name");

		final Text productionSkillText = new Text(topArea, SWT.BORDER);
		productionSkillText.setText("");
		productionSkillText.setLayoutData(gdGrab.create());

		final Label templateSkillLabel = new Label(topArea, SWT.NONE);
		templateSkillLabel.setText("Template Skill");

		final ComboViewer templateSkillCV = new ComboViewer(topArea);
		templateSkillCV.getControl().setLayoutData(gdGrab.create());
		templateSkillCV.setContentProvider(new ArrayContentProvider());
		templateSkillCV.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((TemplateSkill) element).getName();
			}
		});

		templateSkillCV.setInput(SkillproService.getSkillproProvider().getTemplateSkillRepo().getEntities());
		// tables
		Composite tablesArea = new Composite(container, SWT.NONE);
		tablesArea.setLayoutData(gdGrab.create());
		tablesArea.setLayout(gridLayoutDouble.create());

		final Label inputsLabel = new Label(tablesArea, SWT.NONE);
		inputsLabel.setText("Input products:");
		inputsLabel.setLayoutData(gdGrab.create());

		final Label outputsLabel = new Label(tablesArea, SWT.NONE);
		outputsLabel.setText("Output products:");
		outputsLabel.setLayoutData(gdGrab.create());

		String[] headers = { "Name", "Quantity" };
		int[] bounds = { 150, 100 };
		// input
		inputTableViewer = createTableViewer(tablesArea, headers, bounds);
		inputTableViewer.setInput(inputs);
		// output
		outputTableViewer = createTableViewer(tablesArea, headers, bounds);
		outputTableViewer.setInput(outputs);
		// ADD Buttons
		Composite inputButtonsArea = new Composite(tablesArea, SWT.NONE);
		inputButtonsArea.setLayout(gridLayoutDouble.create());

		addInputButton = new Button(inputButtonsArea, SWT.PUSH);
		addInputButton.setText("Add");
		deleteInputButton = new Button(inputButtonsArea, SWT.PUSH);
		deleteInputButton.setText("Delete");

		Composite outputButtonsArea = new Composite(tablesArea, SWT.NONE);
		outputButtonsArea.setLayout(gridLayoutDouble.create());

		addOutputButton = new Button(outputButtonsArea, SWT.PUSH);
		addOutputButton.setText("Add");
		deleteOutputButton = new Button(outputButtonsArea, SWT.PUSH);
		deleteOutputButton.setText("Delete");

		if (modifyDialog && !isProduct) {
			this.productionSkillName = productionSkill.getName();
			for (ProductQuantity input : productionSkill.getInputConfiguration().getProductQuantities()) {
				inputs.add(input);
			}
			for (ProductQuantity output : productionSkill.getOutputConfiguration().getProductQuantities()) {
				outputs.add(output);
			}
			this.templateSkill = productionSkill.getTemplateSkill();
			productionSkillText.setText(productionSkillName);

			templateSkillCV.setSelection(new StructuredSelection(productionSkill.getTemplateSkill()));
			inputTableViewer.setInput(inputs);
			outputTableViewer.setInput(outputs);
		}
		// ADD LISTENERS
		templateSkillCV.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				templateSkill = (TemplateSkill) ((IStructuredSelection) templateSkillCV
						.getSelection()).getFirstElement();
				validate();
			}
		});
		
		addInputButton.addSelectionListener(addProductQuantitySelectionListener(inputs));
		addOutputButton.addSelectionListener(addProductQuantitySelectionListener(outputs));

		deleteInputButton.addSelectionListener(deleteProductQuantityListener());
		deleteOutputButton.addSelectionListener(deleteProductQuantityListener());

		productionSkillText.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				productionSkillName = productionSkillText.getText();
				validate();
			}
		});
		return container;
	}

	private TableViewer createTableViewer(Composite container,
			String[] headers, int[] bounds) {
		TableViewer tableViewer = new TableViewer(container);
		for (int i = 0; i < headers.length; i++) {
			TableViewerColumn col = new TableViewerColumn(tableViewer, SWT.NONE);
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

	public Set<ProductQuantity> getInputs() {
		return inputs;
	}

	public Set<ProductQuantity> getOutputs() {
		return outputs;
	}

	public ProductionSkill getCreatedProductionSkill() {
		return new ProductionSkill(productionSkillName, templateSkill, inputs, outputs);
	}

	private void deleteSelected(TableViewer viewer) {
		Collection<?> input = (Collection<?>) viewer.getInput();
		for (TableItem tableItem : viewer.getTable().getSelection()) {
			input.remove(tableItem.getData());
		}
	}

	// LISTENERS
	private SelectionListener addProductQuantitySelectionListener(final Collection<ProductQuantity> existingProductQuantities) {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button source = (Button) e.getSource();
				if (source == addInputButton) {
					AddInputOutputDialog dialog = new AddInputOutputDialog(source.getShell(), ProductDialogType.INPUT_DIALOG, existingProductQuantities);
					if (dialog.open() == Window.OK) {
						for (ProductQuantity pq : dialog.getResult()) {
							inputs.add(pq);
						}
						inputTableViewer.refresh();
					}
				} else if (source == addOutputButton) {
					AddInputOutputDialog dialog = new AddInputOutputDialog(source.getShell(), ProductDialogType.OUTPUT_DIALOG, existingProductQuantities);
					if (dialog.open() == Window.OK) {
						for (ProductQuantity pq : dialog.getResult()) {
							outputs.add(pq);
						}
						outputTableViewer.refresh();
					}
				}
				validate();
			}
		};
	}

	private SelectionListener deleteProductQuantityListener() {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button source = (Button) e.getSource();

				if (source == deleteInputButton) {
					deleteSelected(inputTableViewer);
					inputTableViewer.refresh();
				} else if (source == deleteOutputButton) {
					deleteSelected(outputTableViewer);
					outputTableViewer.refresh();
				}
				validate();
			}
		};
	}


	private Composite createProductPage(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();

		container.setLayout(layout);
		layout.numColumns = 2;

		GridDataFactory gd = GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER);
		GridDataFactory gdInput = gd.copy().grab(true, false);

		final Label productLabel = new Label(container, SWT.NONE);
		productLabel.setText("Name");

		final Text productText = new Text(container, SWT.BORDER);
		productText.setText("");
		productText.setLayoutData(gdInput.create());

		final Label supplyLabel = new Label(container, SWT.NONE);
		supplyLabel.setText("Supply");

		//FIXME Supply and Supplier are both not used for now
		final Text supplyText = new Text(container, SWT.BORDER);
		supplyText.setText("");
		supplyText.setLayoutData(gdInput.create());
		supplyText.setEnabled(false);

		final Label supplierLabel = new Label(container, SWT.NONE);
		supplierLabel.setText("Supplier");

		final Text supplierText = new Text(container, SWT.BORDER);
		supplierText.setText("");
		supplierText.setLayoutData(gdInput.create());
		supplierText.setEnabled(false);

		final Label factoryLabel = new Label(container, SWT.NONE);
		factoryLabel.setText("Factory");

		final ComboViewer factoryCV = new ComboViewer(container);
		factoryCV.getControl().setLayoutData(gdInput.create());
		factoryCV.setContentProvider(new ArrayContentProvider());
		factoryCV.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((Factory)element).getName();
			}
		});
		List<Factory> factories = new ArrayList<>();
		for (FactoryNode fn : SkillproService.getSkillproProvider().getAssetRepo()) {
			if (fn instanceof Factory) {
				factories.add((Factory) fn);
			}
		}
		factoryCV.setInput(factories);
		if (factories.size() > 0) {
			factory = factories.get(0);
			factoryCV.setSelection(new StructuredSelection(factory));
		}
		factoryCV.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				factory = (Factory) ((IStructuredSelection) factoryCV.getSelection()).getFirstElement();
			}
		});

		final Button disposableButton = new Button(container, SWT.CHECK);
		disposableButton.setText("Disposable");
		disposableButton.setSelection(false);
		disposableButton.setEnabled(true);

		final Button purchasableButton = new Button(container, SWT.CHECK);
		purchasableButton.setText("Purchasable");
		purchasableButton.setSelection(false);
		purchasableButton.setEnabled(true);

		if (modifyDialog && isProduct) {
			productName = product.getName();
			supplyName = product.getSupply() != null ? product.getSupply().getName() : "";
			supplierName = product.getSupply() != null && product.getSupply().getSupplier() != null ? product.getSupply().getSupplier().getName() : "";
			factory = product.getFactory();
			disposable = product.isDisposable();
			purchasable = product.isPurchasable();
			productText.setText(productName);
			supplyText.setText(supplyName);
			supplierText.setText(supplierName);
			factoryCV.setSelection(new StructuredSelection(factory));
			disposableButton.setSelection(disposable);
			purchasableButton.setSelection(purchasable);
		}
		
		//Listeners
		ModifyListener modifyHandler = new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				Text source = (Text) e.getSource();
				if (source == productText) {
					productName = source.getText();
				} else if (source == supplyText) {
					supplyName = source.getText();
				} else if (source == supplierText) {
					supplierName = source.getText();
				}
				validate();

			}
		};
		productText.addModifyListener(modifyHandler);
		supplyText.addModifyListener(modifyHandler);
		supplierText.addModifyListener(modifyHandler);

		SelectionListener selectionHandler = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Object source = e.getSource();
				if (source == disposableButton) {
					disposable = disposableButton.getSelection();
				} else if (source == purchasableButton) {
					purchasable = purchasableButton.getSelection();
				}
			}
		};
		disposableButton.addSelectionListener(selectionHandler);
		purchasableButton.addSelectionListener(selectionHandler);
		return container;
	}

	public Product getCreatedProduct() {
		Supplier supplier = null;
		Supply supply = null;
		if (supplierName != null && !supplierName.isEmpty()) {
			supplier = new Supplier(supplierName);
			if (supplyName != null && !supplyName.isEmpty()) {
				supply = new Supply(supplyName, supplier);
			}
		}
		return new Product(productName, supply, factory, disposable, purchasable);
	}

	public boolean isProduct() {
		return isProduct;
	}
	
	public void updateProduct() {
		product.setName(productName);
		Supply supply = product.getSupply();
		if (supply != null) {
			supply.setName(supplyName);
			if (supply.getSupplier() != null) {
				supply.getSupplier().setName(supplierName);
			}
		}
		product.setDisposable(disposable);
		product.setPurchasable(purchasable);
	}

	public void updateProductionSkill() {
		productionSkill.setName(productionSkillName);
		productionSkill.setTemplateSkill(templateSkill);
		Set<ProductQuantity> skillInputs = productionSkill.getInputConfiguration().getProductQuantities(); 

		skillInputs.clear();
		for (ProductQuantity input : inputs) {
			skillInputs.add(input);
		}

		Set<ProductQuantity> skillOutputs = productionSkill.getOutputConfiguration().getProductQuantities(); 
		skillOutputs.clear();
		for (ProductQuantity output : outputs) {
			skillOutputs.add(output);
		}
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		validate();
	}
	
	private void validate() {
		boolean valid;
		if (isProduct) {
			valid = productName != null && !productName.isEmpty() && factory != null;
		} else {
			valid = productionSkillName != null && !productionSkillName.isEmpty() && templateSkill != null && !inputs.isEmpty() && !outputs.isEmpty();
		}
		getOkButton().setEnabled(valid);
	}
}
