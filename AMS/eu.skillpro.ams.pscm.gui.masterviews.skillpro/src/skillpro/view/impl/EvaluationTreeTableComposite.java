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

import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Math.max;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

import masterviews.composite.abstracts.TreeTableComposite;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;

import skillpro.model.service.SkillproService;
import skillpro.model.skills.ExecutableSkill;
import skillpro.model.skills.ResourceExecutableSkill;
import skillpro.model.utils.ExecutableSkillKPIs;
import skillpro.providers.evaluation.EvaluationContentProvider;
import skillpro.providers.skill.ExecutableSkillLabelProvider;

public class EvaluationTreeTableComposite extends TreeTableComposite implements ISelectionListener {
	public EvaluationTreeTableComposite(Composite parent, int style) {
		super(parent, style);
	}

	@Override
	protected LabelProvider initLabelProvider() {
		return new ExecutableSkillLabelProvider();
	}

	@Override
	protected IContentProvider initContentProvider() {
		return new EvaluationContentProvider();
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		Object currentSelection = ((IStructuredSelection) selection)
				.getFirstElement();
		if (currentSelection instanceof ExecutableSkill) {
			initInput(currentSelection);
		} else if (currentSelection instanceof Entry<?, ?>) {
			Object firstElement = ((Entry<?, ?>) currentSelection).getKey();
			if (firstElement instanceof ExecutableSkill) {
				initInput(firstElement);
			}
		}
		viewer.refresh();
	}
	
	private void initInput(Object object) {
		viewer.setInput(Arrays.asList(object));
	}
	
	@Override
	protected void addColumn(Tree tree) {
		addDurationColumn(tree, "Duration (PEZ)");
		
		int columnIndex = 1;

		addKPIColumn(tree, columnIndex++, "Main production time (HNZ)", new KPIColumnProvider() {
			@Override
			protected double getValue(ExecutableSkillKPIs kpi) {
				return kpi.averageMainProductiveTime;
			}
		});
		addKPIColumn(tree, columnIndex++, "Busy time (BLZ)", new KPIColumnProvider() {
			@Override
			protected double getValue(ExecutableSkillKPIs kpi) {
				return kpi.averageExecutionTime;
			}
		});
		addKPIColumn(tree, columnIndex++, "Usage level (N=BLZ/HNZ)", new KPIColumnProvider() {
			@Override
			protected double getValue(ExecutableSkillKPIs kpi) {
				return kpi.averageExecutionTime / kpi.averageMainProductiveTime;
			}
		});
		addKPIColumn(tree, columnIndex++, "Process level (P = HNZ/DLZ)", new EmptyColumnProvider());
	}
	
	private void addDurationColumn(final Tree tree, String title) {
		TreeColumn column = new TreeColumn(tree, SWT.RIGHT | SWT.CHECK, 1);
		column.setAlignment(SWT.LEFT);
		column.setText(title);
		column.setWidth(100);
		TreeViewerColumn treeViewerColumn = new TreeViewerColumn(viewer, column);
		
		treeViewerColumn.setLabelProvider(new DurationColumnProvider());
	}
	
	private void addKPIColumn(Tree tree, int columnIndex, String title, ColumnLabelProvider labelProvider) {
		TreeColumn column = new TreeColumn(tree, SWT.RIGHT | SWT.CHECK, columnIndex);
		column.setAlignment(SWT.RIGHT);
		column.setText(title);
		column.setWidth(75);
		TreeViewerColumn treeViewerColumn = new TreeViewerColumn(viewer, column);
		
		treeViewerColumn.setLabelProvider(labelProvider);
	}
	
	private class DurationColumnProvider extends ColumnLabelProvider {
		@Override
		public String getText(Object element) {
			if (element instanceof ExecutableSkill) {
				int maxDuration = Integer.MIN_VALUE;
				for (ResourceExecutableSkill rexSkill : ((ExecutableSkill) element).getResourceExecutableSkills()) {
					maxDuration = Math.max(maxDuration, rexSkill.getDuration());
				}
				return maxDuration == Integer.MIN_VALUE ? "-" :  String.valueOf(maxDuration);
			} else if (element instanceof ResourceExecutableSkill) {
				return String.valueOf(((ResourceExecutableSkill) element).getDuration());
			} else {
				throw new IllegalArgumentException("EvaluationTreeTable: unknown element");
			}
		}

		@Override
		public Image getImage(Object element) {
			return null;
		}
	}
	
	private static class EmptyColumnProvider extends ColumnLabelProvider{
		@Override
		public String getText(Object element) {
			return "-";
		}
	}
	
	private static abstract class KPIColumnProvider extends ColumnLabelProvider{
		protected abstract double getValue(ExecutableSkillKPIs kpi);
		private final DecimalFormat decimalFormat = new DecimalFormat("##.#");
		
		@Override
		public String getText(Object element) {
			Map<String, ExecutableSkillKPIs> kpiMap = SkillproService.getSkillproProvider().getKPIRepo().getKpiMap();
			if (element instanceof ExecutableSkill) {
				double maxValue = NEGATIVE_INFINITY;
				for (ResourceExecutableSkill rexSkill : ((ExecutableSkill) element).getResourceExecutableSkills()) {
					ExecutableSkillKPIs kpi = kpiMap.get(rexSkill.getId());
					if (kpi != null) {
						double recentTime = getValue(kpi);
						maxValue = max(maxValue, recentTime);
					}
				}
				return maxValue == NEGATIVE_INFINITY ? "-" :  decimalFormat.format(maxValue);
			} else if (element instanceof ResourceExecutableSkill) {
				ExecutableSkillKPIs kpi = kpiMap.get(((ResourceExecutableSkill) element).getId());
				if (kpi == null) {
					return "-";
				} else {
					return decimalFormat.format(getValue(kpi));
				}
			} else {
				throw new IllegalArgumentException("EvaluationTreeTable: unknown element");
			}
		}

		@Override
		public Image getImage(Object element) {
			return null;
		}
	}
}
