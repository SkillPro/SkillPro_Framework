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

package skillpro.product.views;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import masterviews.dialogs.EditAllNameSelectionDialog;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.part.ViewPart;

import skillpro.model.products.Product;
import skillpro.model.products.ProductQuantity;
import skillpro.model.service.SkillproService;
import skillpro.model.skills.ProductionSkill;
import skillpro.model.update.Updatable;
import skillpro.model.update.UpdateType;
import skillpro.product.dialogs.AddTransportSkillToProductDialog;
import skillpro.product.dialogs.CreateProductDialog;
import skillpro.view.impl.ProductTreeComposite;
import eu.skillpro.ams.pscm.icons.IconActivator;

public class ProductTreeView extends ViewPart implements Updatable {
	public static final String ID = ProductTreeView.class.getName();
	private ProductTreeComposite productTreeComposite;
	
	@Override
	public void createPartControl(Composite parent) {
		createViewer(parent);
		getSite().setSelectionProvider(productTreeComposite.getTreeViewer());
		SkillproService.getUpdateManager().registerUpdatable(this, Product.class);
	}

	private void createViewer(Composite parent) {
		productTreeComposite =  new ProductTreeComposite(parent, SWT.NULL) {
			@Override
			protected void addCoolbarItems(Composite parent) {
				ToolBar coolToolBar = new ToolBar(parent, SWT.VERTICAL);
				createToolItem(coolToolBar, SWT.VERTICAL, "a*", IconActivator.getImageDescriptor("icons/asset/add.png").createImage(), "Add a new Product", addProductSelectionListener());
				createToolItem(coolToolBar, SWT.VERTICAL, "a*", IconActivator.getImageDescriptor("icons/asset/remove.png").createImage(), "Delete selected Product", deleteProductSelectionListener());
			}

			@Override
			protected List<Action> createExtraContextActions(Object selection) {
				List<Action> actions = new ArrayList<>();
				if (selection instanceof ProductQuantity) {
					actions.add(addTransportSkill((ProductQuantity) selection));
				}
				return actions;
			}
		};
		productTreeComposite.setLayoutData(GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).create());
		productTreeComposite.setInput(SkillproService.getSkillproProvider().getProductRepo());
		productTreeComposite.getViewer().addDoubleClickListener(createDoubleClickListener());
		productTreeComposite.getViewer().getTree().addKeyListener(new KeyAdapter() {
			
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.keyCode == SWT.DEL) {
					deleteViewerSelections();
				}
				
			}
		});
	}
	
	private IDoubleClickListener createDoubleClickListener() {
		return new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				Object selection = ((StructuredSelection) productTreeComposite.getViewer().getSelection()).getFirstElement();
				if (selection instanceof Product) {
					CreateProductDialog dialog = new CreateProductDialog(getSite().getShell(), (Product) selection);
					if (dialog.open() == Dialog.OK) {
						dialog.updateProduct();
						SkillproService.getUpdateManager().notify(UpdateType.PRODUCT_UPDATED, null);
					}
				} else if (selection instanceof ProductionSkill) {
					CreateProductDialog dialog = new CreateProductDialog(getSite().getShell(), (ProductionSkill) selection);
					if (dialog.open() == Dialog.OK) {
						dialog.updateProductionSkill();
						SkillproService.getUpdateManager().notify(UpdateType.PRODUCT_UPDATED, null);
					}
				} else if (selection instanceof ProductQuantity) {
					EditAllNameSelectionDialog changeNameDialog = new EditAllNameSelectionDialog(getSite().getShell(),
							((ProductQuantity) selection).getProduct().getName());
					changeNameDialog.open();
					if (changeNameDialog.getReturnCode() == Dialog.OK) {
						((ProductQuantity) selection).getProduct().setName((String) changeNameDialog.getName());
						SkillproService.getUpdateManager().notify(UpdateType.SKILL_UPDATED, null);
						SkillproService.getUpdateManager().notify(UpdateType.PRODUCT_UPDATED, null);
					}
				}
			}
		};
	}

	@Override
	public void setFocus() {
	}

	@Override
	public void update(UpdateType type) {
		switch (type) {
		case NEW_DATA_IMPORTED:
			productTreeComposite.disposeAllItems();
			productTreeComposite.setInput(SkillproService.getSkillproProvider().getProductRepo());
			productTreeComposite.getTreeViewer().refresh();
		case PRODUCT_CREATED:
			productTreeComposite.getTreeViewer().refresh();
		case PRODUCT_UPDATED:
			productTreeComposite.getTreeViewer().refresh();
		case PRODUCT_DELETED:
			productTreeComposite.getTreeViewer().refresh();
		case SKILL_CREATED:
			productTreeComposite.getTreeViewer().refresh();
		default:
			break;
		}
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
	
	private SelectionListener addProductSelectionListener() {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				CreateProductDialog dialog = new CreateProductDialog(getViewSite().getShell());
				
				if (dialog.open() == Dialog.OK) {
					if (dialog.isProduct()) {
						Product newProduct = dialog.getCreatedProduct();
						SkillproService.getSkillproProvider().getProductRepo().add(newProduct);
						SkillproService.getUpdateManager().notify(UpdateType.PRODUCT_CREATED, null);
					} else {
						SkillproService.getSkillproProvider().getProductionSkillRepo().add(dialog.getCreatedProductionSkill());
						SkillproService.getUpdateManager().notify(UpdateType.PRODUCT_CREATED, null);
					}
				}
			}
		};
	}
	
	private SelectionListener deleteProductSelectionListener() {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				deleteViewerSelections();
			}
		};
	}
	
	private void deleteViewerSelections() {
		Object[] selections = productTreeComposite.getViewerSelection().toArray();
		boolean single = selections.length == 1;
		if (!single) {
			boolean confirm =  MessageDialog.openConfirm(getViewSite().getShell(),
					"Confirm", "Are you sure you want to delete the selected " + selections.length + " elements?");
			if (!confirm) {
				return;
			}
		}
		for (Object selectedElement : selections) {
			delete(selectedElement, single);
		}				
	}
	
	private void delete(Object selectedElement, boolean singleConfirm) {
		if (selectedElement instanceof Product) {
			Product product = (Product) selectedElement;
			boolean confirm = singleConfirm ? MessageDialog.openConfirm(getViewSite().getShell(),
					"Confirm", "Are you sure you want to delete \"" + product.getName() + "\"?") : true;
			if (confirm) {
				deleteProduct(product);
			}
		} else if (selectedElement instanceof ProductionSkill) {
			ProductionSkill productionSkill = (ProductionSkill) selectedElement;
			boolean confirm =  singleConfirm ? MessageDialog.openConfirm(getViewSite().getShell(),
					"Confirm", "Are you sure you want to delete \"" + productionSkill.getName() + "\"?") : true;
			if (confirm) {
				SkillproService.getSkillproProvider().getProductionSkillRepo().remove(productionSkill);
				SkillproService.getUpdateManager().notify(UpdateType.PRODUCT_DELETED, null);
				SkillproService.getUpdateManager().notify(UpdateType.SKILL_UPDATED, null);
			}
		}
	}
	
	private void deleteProduct(Product product) {
		for (Iterator<ProductionSkill> i = SkillproService.getSkillproProvider().getProductionSkillRepo().iterator(); i.hasNext(); ){
			ProductionSkill pSkill = i.next();
			for (ProductQuantity outputPQ : pSkill.getOutputConfiguration().getProductQuantities()) {
				if (outputPQ.getProduct().equals(product)) {
					i.remove();
					break;
				}
			}
		}
		SkillproService.getSkillproProvider().removeProduct(product);
		SkillproService.getUpdateManager().notify(UpdateType.PRODUCT_DELETED, null);
	}
	
	private Action addTransportSkill(final ProductQuantity productQuantity) {
		return new Action("Add Transport Skill") {
			@Override
			public void run() {
				AddTransportSkillToProductDialog dialog = new AddTransportSkillToProductDialog(getSite().getShell(), productQuantity);
				if (dialog.open() == Dialog.OK) {
					SkillproService.getSkillproProvider().getProductionSkillRepo().add(dialog.getCreatedProductionSkill());
					SkillproService.getUpdateManager().notify(UpdateType.SKILL_CREATED, null);
				}
			}
		};
	}
}
