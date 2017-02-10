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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.SelectionDialog;

import skillpro.model.products.Product;
import skillpro.model.products.ProductQuantity;
import skillpro.model.service.SkillproService;

public class AddInputOutputDialog extends SelectionDialog {
	private Product product;
	private int quantity;
	private Collection<ProductQuantity> existingProductQuantities;

	public AddInputOutputDialog(Shell parentShell, ProductDialogType type, Collection<ProductQuantity> existingProductQuantities) {
		super(parentShell);
		if (type == ProductDialogType.INPUT_DIALOG) {
			setTitle("Input product");
		} else {
			setTitle("Output product");
		}
		this.quantity = 1;
		this.existingProductQuantities = existingProductQuantities;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		container.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).margins(5,5).equalWidth(false).create());

		createButtonsComposite(container);		
		return area;
	}

	private void createButtonsComposite(Composite container) {
		GridDataFactory gdf = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).span(2,1);

		final Label productLabel = new Label(container, SWT.NONE);
		productLabel.setText("Product: ");
		productLabel.setLayoutData(gdf.span(1,2).create());

		Set<Product> products = new HashSet<>();
		products.addAll(SkillproService.getSkillproProvider().getProductRepo().getEntities());
		for (ProductQuantity prodQuantity : existingProductQuantities) {
			products.remove(prodQuantity.getProduct());
		}
		final ComboViewer productComboViewer = new ComboViewer(container);
		productComboViewer.getControl().setLayoutData(gdf.span(1, 2).create());
		productComboViewer.setContentProvider(new ArrayContentProvider());
		productComboViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((Product) element).getName();
			}
		});
		productComboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				product = (Product) ((IStructuredSelection) productComboViewer.getSelection()).getFirstElement();
				validate();
			}
		});
		productComboViewer.setInput(products);

		final Label quantityLabel = new Label(container, SWT.NONE);
		quantityLabel.setText("Quantity: ");
		quantityLabel.setLayoutData(gdf.span(1,1).create());

		final Text quantityText = new Text(container, SWT.BORDER);
		quantityText.setText(String.valueOf(quantity));
		quantityText.setLayoutData(gdf.span(1,1).create());

		final Label errorLabel = new Label(container, SWT.NONE);
		errorLabel.setText(getMessage());
		errorLabel.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_RED));
		errorLabel.setLayoutData(GridDataFactory.fillDefaults().span(2, 1).create());
		quantityText.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				quantity = -1;
				try {
					quantity = Integer.parseInt(quantityText.getText());
				} catch (NumberFormatException ex) {
				}
				if (quantity <= 0) {
					setMessage("Quantity has to be a positive integer");
				} else {
					setMessage("");
				}
				errorLabel.setText(getMessage());
				validate();
			}
		});
	}

	@Override
	protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
		Button button = super.createButton(parent, id, label, defaultButton);
		if (id == IDialogConstants.OK_ID) {
			button.setEnabled(false);
		}
		return button;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		validate();
	}
	
	private void validate() {
		boolean valid = product != null && quantity > 0;
		getOkButton().setEnabled(valid);
	}
	
	@Override
	protected void okPressed() {
		ProductQuantity productQuantity = new ProductQuantity(product, quantity);
		setSelectionResult(new ProductQuantity[]{productQuantity});
		super.okPressed();
	}
	
	@Override
	public ProductQuantity[] getResult() {
		return new ProductQuantity[] { new ProductQuantity(product, quantity) };
	}
}