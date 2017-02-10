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

import java.util.HashSet;
import java.util.Set;

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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.SelectionDialog;

import skillpro.model.products.ProductQuantity;
import skillpro.model.service.SkillproService;
import skillpro.model.skills.ProductionSkill;
import skillpro.model.skills.TemplateSkill;

public class AddTransportSkillToProductDialog extends SelectionDialog {
	private String name;
	private TemplateSkill skill;
	private final ProductQuantity productQuantity;

	public AddTransportSkillToProductDialog(Shell parentShell, ProductQuantity productQuantity) {
		super(parentShell);
		name = "Transport " + productQuantity.getProduct().getName();
		this.productQuantity = productQuantity;
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

	private void createContainerItems(Composite container,
			GridDataFactory gdGrab) {
		final Label nameLabel = new Label(container, SWT.NONE);
		nameLabel.setText("Name");
		final Text nameText = new Text(container, SWT.BORDER);
		nameText.setLayoutData(gdGrab.create());
		nameText.setText(name);

		final Label templateSkillLabel = new Label(container, SWT.NONE);
		templateSkillLabel.setText("Template Skill");

		final ComboViewer templateSkillCV = new ComboViewer(container);
		templateSkillCV.getControl().setLayoutData(gdGrab.create());
		templateSkillCV.setContentProvider(new ArrayContentProvider());
		templateSkillCV.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((TemplateSkill) element).getName();
			}
		});

		templateSkillCV.setInput(SkillproService.getSkillproProvider().getTemplateSkillRepo().getEntities());

		// LISTENERS
		templateSkillCV.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				skill = (TemplateSkill) ((IStructuredSelection) templateSkillCV
						.getSelection()).getFirstElement();
				validate();
			}
		});

		nameText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				name = nameText.getText();
				validate();
			}
		});
	}

	private void validate() {
		if (name != null && !name.isEmpty() && skill != null) {
			getOkButton().setEnabled(true);
		} else {
			getOkButton().setEnabled(false);
		}

	}

	public ProductionSkill getCreatedProductionSkill() {
		Set<ProductQuantity> transportedQuantities = new HashSet<>();
		transportedQuantities.add(productQuantity);
		return new ProductionSkill(name, skill, transportedQuantities, transportedQuantities);
	}
}
