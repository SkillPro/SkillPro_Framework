/**
 * 
 */
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

package eu.skillpro.ams.pscm.connector.opcua.ui.dialogs;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import skillpro.model.products.Order;
import skillpro.model.service.SkillproService;

/**
 * @author aleksa
 * 
 * @version: 11.10.2014
 *
 */
public class SendOrdersDialog extends OPCUACallDialog {
	private static final String DIALOG_TITLE = "Send orders to MES";
	private static final String ORDERS_LABEL = "Choose order to send";
	private static final String SELECTED_ORDER_LABEL = "Selected order";
	private static final String PRODUCT = "Product";
	private static final String QUANTITY = "Quantity";
	private static final String PRIORITY = "Priority";

	private Order order = null;
	private int priority = 1;
	
	/**
	 * @param parentShell
	 */
	public SendOrdersDialog(Shell parentShell) {
		super(parentShell);
	}

	protected void configureShell(Shell shell) {
	      super.configureShell(shell);
	      shell.setText(DIALOG_TITLE);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		
		Label orderLabel = new Label(area, SWT.NONE);
		orderLabel.setLayoutData(getLabelGDF().create());
		orderLabel.setText(ORDERS_LABEL);
		
		ComboViewer cv = new ComboViewer(area);
		cv.getControl().setLayoutData(getSecondGDF().create());
		cv.setContentProvider(new ArrayContentProvider());
		cv.setLabelProvider(new OrderContentProvider());
		
		Label separator = new Label(area, SWT.SEPARATOR|SWT.HORIZONTAL);
		separator.setLayoutData(getViewerGDF().copy().hint(SWT.DEFAULT, SWT.DEFAULT).create());
		
		Label orderNrLabel = new Label(area, SWT.NONE);
		orderNrLabel.setText(SELECTED_ORDER_LABEL);
		orderNrLabel.setLayoutData(getLabelGDF().create());
		
		final Text orderNrText = new Text(area, SWT.NONE);
		orderNrText.setText("");
		orderNrText.setLayoutData(getSecondGDF().create());
		orderNrText.setEditable(false);
		
		Label orderProductLabel = new Label(area, SWT.NONE);
		orderProductLabel.setText(PRODUCT);
		orderProductLabel.setLayoutData(getLabelGDF().create());
		
		final Text orderProductText = new Text(area, SWT.NONE);
		orderProductText.setText("");
		orderProductText.setLayoutData(getSecondGDF().create());
		orderProductText.setEditable(false);
		
		Label orderQuantityLabel = new Label(area, SWT.NONE);
		orderQuantityLabel.setText(QUANTITY);
		orderQuantityLabel.setLayoutData(getLabelGDF().create());
		
		final Text orderQuantityText = new Text(area, SWT.NONE);
		orderQuantityText.setText("");
		orderQuantityText.setLayoutData(getSecondGDF().create());
		orderQuantityText.setEditable(false);
		
		Label orderPriorityLabel = new Label(area, SWT.NONE);
		orderPriorityLabel.setText(PRIORITY);
		orderPriorityLabel.setLayoutData(getLabelGDF().create());
		
		final Text orderPriorityText = new Text(area, SWT.NONE);
		orderPriorityText.setText(String.valueOf(priority));
		orderPriorityText.setLayoutData(getSecondGDF().create());
		orderPriorityText.setEditable(false);
		
		cv.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				order = (Order) ((IStructuredSelection)event.getSelection()).getFirstElement();
				updateControls();
			}

			private void updateControls() {
				orderNrText.setText(order.getOrderID());
				orderProductText.setText(order.getProductQuantity().getProduct().getName());
				orderQuantityText.setText(String.valueOf(order.getProductQuantity().getQuantity()));
			}
		});
		
		cv.setInput(SkillproService.getSkillproProvider().getOrderRepo().getEntities());
		
		return area;
	}
		
	@Override
	protected boolean isResizable() {
		return true;
	}
	

	@Override
	protected void okPressed() {
		if (order != null) {
			super.okPressed();
		} else {
			MessageDialog.openWarning(getShell(), "SEE not registered.", "Cancel instead!");
		}
	}
	
	public Order getOrder() {
		return order;
	}


	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}


	private class OrderContentProvider extends LabelProvider{
		@Override
		public String getText(Object element) {
			if (element instanceof Order) {
				Order order = (Order) element;
				return order.getProductQuantity().getProduct() + ": " + order.getProductQuantity().getQuantity() 
						+ " (" + order.getOrderID() + ")";
			}
			return super.getText(element);
		}
	}
}
