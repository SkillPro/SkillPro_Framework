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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import masterviews.composite.abstracts.TableComposite;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import skillpro.dialogs.ExecutableSkillsSelectionDialog;
import skillpro.model.products.Order;
import skillpro.model.service.SkillproService;
import skillpro.model.skills.ExecutableSkill;
import skillpro.model.skills.ResourceExecutableSkill;
import skillpro.model.utils.ExecutableSkillKPIs;
import skillpro.model.utils.Pair;

public class OrderAlternativeTableComposite extends TableComposite {

	public OrderAlternativeTableComposite(Composite parent, int style) {
		super(parent, style);
		getViewer().addDoubleClickListener(new IDoubleClickListener() {
			
			@Override
			public void doubleClick(DoubleClickEvent event) {
				Object selection = ((StructuredSelection) getViewer().getSelection()).getFirstElement();
				if (selection instanceof Pair<?, ?>) {
					if (((Pair<?, ?>) selection).getSecondElement() instanceof List<?>) {
						List<?> input = (List<?>) ((Pair<?, ?>) selection).getSecondElement();
						ExecutableSkillsSelectionDialog dialog = new ExecutableSkillsSelectionDialog(getShell(),
								input, "Executable Skills contained in currently selected alternative");
						if (dialog.open() == SWT.OK) {
							//TODO
						}
						
					}
				}
			}
		});
	}

	@Override
	protected void createColumns(TableViewer viewer) {
		String[] headers = { "Name", "Configuration Total Lead Time",
				"Configuration Energy Consumption",
				"Configuration Total Quality Indicator",
				"Configuration Total CO2 Emissions",
				"Configuration Total Cost" };

		for (int i = 0; i < headers.length; i++) {
			TableViewerColumn col = new TableViewerColumn(viewer, SWT.NONE);
			col.getColumn().setText(headers[i]);
			if (i == 0) {
				col.getColumn().setWidth(100);
			} else {
				col.getColumn().setWidth(50);
			}
			
			col.getColumn().setResizable(true);
			col.getColumn().setMoveable(true);
		}

		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLinesVisible(true);
		
	}

	@Override
	protected ITableLabelProvider initLabelProvider() {
		return new AlternativeTableLabelProvider();
	}

	@Override
	protected IContentProvider initContentProvider() {
		return new AlternativeContentProvider();
	}

	@Override
	public void refreshViewer() {
		((AlternativeTableLabelProvider) getViewer().getLabelProvider()).index = 0;
		super.refreshViewer();
	}
	
	@Override
	public void setInput(Collection<?> object) {
		((AlternativeTableLabelProvider) getViewer().getLabelProvider()).index = 0;
		super.setInput(object);
	}

	private class AlternativeContentProvider implements IStructuredContentProvider {

		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		@Override
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof Collection<?>) {
				List<Object> objects = new ArrayList<>();
				for (Object obj : ((Collection<?>) inputElement)) {
					//input element looks like: 
					//Entry<Order, List<List<ExecutableSkill>>> entry = (Entry<Order, List<List<ExecutableSkill>>>) obj;
					if (obj instanceof Entry<?, ?>) {
						Object value = ((Entry<?, ?>) obj).getValue();
						if (value instanceof Collection<?>) {
							for (Object insideOfValue : ((Collection<?>) value).toArray()) {
								objects.add(new Pair<>(((Entry<?, ?>) obj).getKey(), insideOfValue));
							}
							
						}
					}
				}
				
				return objects.toArray();
			}
			return new Object[] {};
		}
		
	}
	
	private class AlternativeTableLabelProvider extends LabelProvider implements ITableLabelProvider {
		private int index = 0;
		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof Pair<?, ?> && ((Pair<?, ?>) element).getFirstElement() instanceof Order
					&& ((Pair<?, ?>) element).getSecondElement() instanceof List<?>) {
				Order order = (Order) ((Pair<?, ?>) element).getFirstElement();
				List<?> secondElementList = (List<?>) ((Pair<?, ?>) element).getSecondElement();
				double totalLeadTime = 0.0;
				if (SkillproService.getSkillproProvider().getKPIRepo().getKpiMap().isEmpty()) {
					int base = (secondElementList.size() - 2) * 30;
					totalLeadTime = base + new Random().nextInt(200);
				} else {
					for (Object obj : secondElementList) {
						if (obj instanceof ExecutableSkill) {
							ExecutableSkill exSkill = (ExecutableSkill) obj;
							double maxLeadTime = - Double.MAX_VALUE;
							for (ResourceExecutableSkill rexSkill : exSkill.getResourceExecutableSkills()) {
								ExecutableSkillKPIs executableSkillKPIs = SkillproService
										.getSkillproProvider().getKPIRepo().getKpiMap().get(rexSkill.getName());
								if (executableSkillKPIs != null) {
									maxLeadTime = Math.max(maxLeadTime, executableSkillKPIs.averageLeadTime);
								} else {
									maxLeadTime = Math.max(maxLeadTime, new Random().nextInt(30));
								}
							}
							totalLeadTime += maxLeadTime;
						} else {
							throw new IllegalArgumentException("Unexpected element: " + obj);
						}
					}
				}
				switch (columnIndex) {
				case 0:
					return order.getOrderName() + "_Alternative_" + index++;
				case 1:
					return totalLeadTime + "";
				case 2:
					return 0.0 + "";
				case 3:
					return 0.0 + "";
				case 4:
					return 0.0 + "";
				case 5:
					return 0.0 + "";
				}
			}
			return null;
		}
	}
}
