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

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
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

import skillpro.model.products.ProductQuantity;
import skillpro.providers.product.PQTableContentProvider;
import skillpro.providers.product.PQTableLabelProvider;

public class ProductConfigurationDialog extends SelectionDialog {
	private Set<ProductQuantity> inputs = new HashSet<>();
	
	public ProductConfigurationDialog(Shell parentShell) {
		super(parentShell);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		container.setLayout(GridLayoutFactory.fillDefaults().margins(5,5).equalWidth(false).create());
		
		Label ProductConfigurationLabel = new Label(container, SWT.NONE);
		ProductConfigurationLabel.setText("Product Configuration");
		ProductConfigurationLabel.setLayoutData(GridDataFactory.fillDefaults().span(2, 1).grab(true, false).create());

		String[] headers = { "Name", "Quantity" };
		int[] bounds = { 100, 100 };

		// input
		final TableViewer preProductQuantitiesTableViewer = createTableViewer(container, headers, bounds);
		
		preProductQuantitiesTableViewer.setInput(inputs);
		preProductQuantitiesTableViewer.getControl().setLayoutData(GridDataFactory.fillDefaults().span(2, 1).grab(true, true)
				.hint(0, 200).create());

		// ADD Buttons
		Composite inputButtonsArea = new Composite(container, SWT.NONE);
		inputButtonsArea.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).create());

		Button addPreProductQuantityButton = new Button(inputButtonsArea, SWT.PUSH);
		addPreProductQuantityButton.setText("Add");
		Button deleteProductQuantityButton = new Button(inputButtonsArea, SWT.PUSH);
		deleteProductQuantityButton.setText("Delete");
		
		addPreProductQuantityButton.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				AddInputOutputDialog dialog = new AddInputOutputDialog(getShell(), ProductDialogType.INPUT_DIALOG, inputs);
				if (dialog.open() == Window.OK) {
					for (ProductQuantity pq : dialog.getResult()) {
						inputs.add(pq);
					}
					preProductQuantitiesTableViewer.refresh();
				}
				
			}
		});

		deleteProductQuantityButton.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				deleteSelected(preProductQuantitiesTableViewer);
				preProductQuantitiesTableViewer.refresh();
				
			}
		});
		return area;
	}
	
	private void deleteSelected(TableViewer viewer) {
		Collection<?> input = (Collection<?>) viewer.getInput();
		for (TableItem tableItem : viewer.getTable().getSelection()) {
			input.remove(tableItem.getData());
		}
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
		return new HashSet<>(inputs);
	}
}