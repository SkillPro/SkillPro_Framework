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

package de.fzi.skillpro.order.dialogs;

import java.util.Calendar;

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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.SelectionDialog;

import skillpro.model.products.Order;
import skillpro.model.products.Product;
import skillpro.model.products.ProductQuantity;
import skillpro.model.service.SkillproService;

public class CreateNewOrderDialog extends SelectionDialog {
	private static final String TITLE = "Add a new Order";
	private final int DEFAULT_QUANTITY = 1;
	
	private String id;
	private Product product;
	private int quantity;
	private Calendar startDate;
	private Calendar deadline;
	private String name;
	
	
	public CreateNewOrderDialog(Shell parentShell) {
		super(parentShell);
		setTitle(TITLE);
		startDate = Calendar.getInstance();
		startDate.set(Calendar.MONTH, startDate.get(Calendar.MONTH) + 1);
		deadline = Calendar.getInstance();
		deadline.set(Calendar.MONTH, deadline.get(Calendar.MONTH) + 1);
		quantity = DEFAULT_QUANTITY;
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		validate();
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		
		Composite container = new Composite(area, SWT.NONE);
		GridLayoutFactory gridLayoutDouble = GridLayoutFactory.swtDefaults().numColumns(2);
		
		GridDataFactory gd = GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER);
		GridDataFactory gdGrab = gd.copy().grab(true, false);
		
		container.setLayoutData(gdGrab.create());
		container.setLayout(gridLayoutDouble.create());
		createContainerItems(container, gdGrab);
		
		return area;
	}
	
	
	public Order getCreatedOrder() {
		return new Order(id, name, new ProductQuantity(product, quantity), startDate, deadline);
	}
	
	private void createContainerItems(Composite container, GridDataFactory gdGrab) {
		final Label orderLabel = new Label(container, SWT.NONE);
		orderLabel.setText("ID");
		final Text orderText = new Text(container, SWT.BORDER);
		orderText.setLayoutData(gdGrab.create());
		
		final Label nameLabel = new Label(container, SWT.NONE);
		nameLabel.setText("Name");
		final Text nameText = new Text(container, SWT.BORDER);
		nameText.setLayoutData(gdGrab.create());
		
		final Label productLabel = new Label(container, SWT.NONE);
		productLabel.setText("Product");
		final ComboViewer productCV = new ComboViewer(container);
		productCV.getControl().setLayoutData(gdGrab.create());
		productCV.setContentProvider(new ArrayContentProvider());
		productCV.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((Product)element).getName();
			}
		});
		productCV.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				product = (Product) ((IStructuredSelection)productCV.getSelection()).getFirstElement();
				validate();
			}
		});
		productCV.setInput(SkillproService.getSkillproProvider().getProductRepo().getEntities());
		
		final Label quantityLabel = new Label(container, SWT.NONE);
		quantityLabel.setText("Quantity");
		final Text quantityText = new Text(container, SWT.BORDER);
		quantityText.setText(DEFAULT_QUANTITY + "");
		quantityText.setLayoutData(gdGrab.create());

		final Label startDateLabel = new Label(container, SWT.NONE);
		startDateLabel.setText("Start Date");
		final DateTime startDateTime = new DateTime (container, SWT.DATE | SWT.MEDIUM);
		startDateTime.setLayoutData(gdGrab.create());


		final Label endDateLabel = new Label(container, SWT.NONE);
		endDateLabel.setText("End Date");
		final DateTime endDateTime = new DateTime (container, SWT.DATE | SWT.MEDIUM);
		endDateTime.setLayoutData(gdGrab.create());
		// Listeners
		SelectionListener dateListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DateTime source = (DateTime) e.getSource();
				if (source == startDateTime) {
					startDate.set(Calendar.DAY_OF_MONTH, startDateTime.getDay());
					startDate.set(Calendar.MONTH, startDateTime.getMonth() + 1);
					startDate.set(Calendar.YEAR, startDateTime.getYear());
				} else if (source == endDateTime) {
					deadline.set(Calendar.DAY_OF_MONTH, endDateTime.getDay());
					deadline.set(Calendar.MONTH, endDateTime.getMonth() + 1);
					deadline.set(Calendar.YEAR, endDateTime.getYear());
				}
				validate();
			}
		};
		
		ModifyListener modifyListener =  new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				Text source = (Text) e.getSource();
				if (source == quantityText) {
					try {
						quantity = Integer.parseInt(quantityText.getText());
					} catch (NumberFormatException ex) {
						quantityText.setText("");
					}
				} else if (source == orderText) {
					id = orderText.getText();
				}
				validate();
			}
		};
		// ADD LISTENERS
		orderText.addModifyListener(modifyListener);
		quantityText.addModifyListener(modifyListener);
		nameText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				name = nameText.getText();
				validate();
			}
		});
		startDateTime.addSelectionListener(dateListener);
		endDateTime.addSelectionListener(dateListener);
	}
	
	private void validate() {
		if (id != null && !id.isEmpty() && name != null && !name.isEmpty()
				&& product != null && quantity > 0 && (startDate.compareTo(deadline) <= 0)) {
			getOkButton().setEnabled(true);
		} else {
			getOkButton().setEnabled(false);
		}
	}
}
