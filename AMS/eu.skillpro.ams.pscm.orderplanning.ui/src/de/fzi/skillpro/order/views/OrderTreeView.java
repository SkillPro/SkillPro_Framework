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

package de.fzi.skillpro.order.views;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.part.ViewPart;

import skillpro.model.products.Order;
import skillpro.model.service.SkillproService;
import skillpro.model.update.Updatable;
import skillpro.model.update.UpdateType;
import skillpro.view.impl.OrderTreeComposite;
import de.fzi.skillpro.order.dialogs.CreateNewOrderDialog;
import eu.skillpro.ams.pscm.gui.masterviews.Activator;


public class OrderTreeView extends ViewPart implements Updatable {
	public final static String ID = OrderTreeView.class.getName();
	private OrderTreeComposite orderTreeComposite;

	@Override
	public void createPartControl(Composite parent) {
		orderTreeComposite = new OrderTreeComposite(parent, SWT.NONE) {
			@Override
			protected void addCoolbarItems(Composite parent) {
				super.addCoolbarItems(parent);
				ToolBar coolToolBar = new ToolBar(parent, SWT.VERTICAL);
				createToolItem(coolToolBar, SWT.VERTICAL, "a*", Activator.getImageDescriptor("icons/attribute_add.png").createImage(), "Add a new order", addNewOrderListener());
				createToolItem(coolToolBar, SWT.VERTICAL, "d*", Activator.getImageDescriptor("icons/cross.png").createImage(), "Delete selected order", deleteSelectedOrderListener());
			}
		};
		getSite().setSelectionProvider(orderTreeComposite.getTreeViewer());
		orderTreeComposite.setInput(SkillproService.getSkillproProvider().getOrderRepo().getEntities());
		SkillproService.getUpdateManager().registerUpdatable(this, Order.class);
	}

	@Override
	public void setFocus() {
	}

	@Override
	public void update(UpdateType type) {
		switch (type) {
		case ORDER_CREATED:
			orderTreeComposite.getTreeViewer().refresh();
			break;
		case ORDER_DELETED:
			orderTreeComposite.getTreeViewer().refresh();
			break;
		case NEW_DATA_IMPORTED:
			orderTreeComposite.getTreeViewer().refresh();
			break;
		case EXECUTABLE_SKILLS_GENERATED:
			orderTreeComposite.getTreeViewer().refresh();
			break;	
		default:
			break;
		}
	}

	private SelectionListener addNewOrderListener() {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				CreateNewOrderDialog dialog = new CreateNewOrderDialog(getSite().getShell());
				if (dialog.open() == IDialogConstants.OK_ID) {
					SkillproService.getSkillproProvider().getOrderRepo().add(dialog.getCreatedOrder());
					SkillproService.getUpdateManager().notify(UpdateType.ORDER_CREATED, null);
				}
			}
		};
	}
	
	private SelectionListener deleteSelectedOrderListener() {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Object selectedElement = ((IStructuredSelection) orderTreeComposite.getTreeViewer()
						.getSelection()).getFirstElement();
				if (selectedElement instanceof Order) {
					Order toDelete = (Order) selectedElement;
					SkillproService.getSkillproProvider().getOrderRepo().remove(toDelete);
					SkillproService.getUpdateManager().notify(UpdateType.ORDER_DELETED, null);
				}
			}
		};
	}
}
