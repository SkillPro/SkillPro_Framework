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

import java.util.Map.Entry;

import masterviews.composite.abstracts.TreeTableComposite;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import skillpro.model.skills.ExecutableSkill;
import skillpro.model.skills.ResourceExecutableSkill;
import skillpro.providers.skill.ExecutableSkillSelectionContentProvider;
import skillpro.providers.skill.ExecutableSkillSelectionLabelProvider;
import eu.skillpro.ams.pscm.icons.IconActivator;

public class ExecutableSkillSelectionTreeTableComposite extends TreeTableComposite {
	public ExecutableSkillSelectionTreeTableComposite(Composite parent, int style) {
		super(parent, style);
	}

	@Override
	protected LabelProvider initLabelProvider() {
		return new ExecutableSkillSelectionLabelProvider();
	}

	@Override
	protected IContentProvider initContentProvider() {
		return new ExecutableSkillSelectionContentProvider();
	}

	@Override
	protected void addColumn(Tree tree) {
		addExecutableSkillColumn(tree);
		addSelectionColumn(tree);
		
	}
	
	@Override
	protected void initColumn(Tree tree) {
	}
	
	private void addExecutableSkillColumn(final Tree tree) {
		TreeColumn secondColumn = new TreeColumn(tree, SWT.LEFT | SWT.CHECK, 0);
		secondColumn.setAlignment(SWT.LEFT);
		secondColumn.setText("ExecutableSkill");
		secondColumn.setWidth(150);
		TreeViewerColumn treeViewerColumn = new TreeViewerColumn(viewer, secondColumn);
		
		treeViewerColumn.setLabelProvider(new ExecutableSkillColumnProvider());
	}
	
	private void addSelectionColumn(final Tree tree) {
		TreeColumn secondColumn = new TreeColumn(tree, SWT.CENTER | SWT.CHECK, 1);
		secondColumn.setAlignment(SWT.CENTER);
		secondColumn.setText("Selection");
		secondColumn.setWidth(50);
		TreeViewerColumn treeViewerColumn = new TreeViewerColumn(viewer, secondColumn);
		
		treeViewerColumn.setLabelProvider(new SelectionColumnProvider());
		treeViewerColumn.setEditingSupport(new SelectionEditingSupport(treeViewerColumn.getViewer()));
	}
	
	private class SelectionEditingSupport extends EditingSupport {
		public SelectionEditingSupport(ColumnViewer columnViewer) {
			super(columnViewer);
		}
		
		@Override
		protected CellEditor getCellEditor(Object element) {
			return new CheckboxCellEditor(null, SWT.CHECK | SWT.READ_ONLY);
		}

		@Override
		protected boolean canEdit(Object element) {
			if (element instanceof ResourceExecutableSkill) {
				return false;
			}
			return true;
		}

		@Override
		protected Object getValue(Object element) {
			if (element instanceof Entry<?, ?>) {
				return ((Entry<?, ?>) element).getValue();
			}
			return null;
		}

		@SuppressWarnings({ "unchecked" })
		@Override
		protected void setValue(Object element, Object value) {
			if (value == null) {
				return;
			}
			if (element instanceof Entry<?, ?>) {
				((Entry<?, Boolean>) element).setValue((Boolean) value);
				viewer.update(element, null);
			}
		}
	}
	
	private class ExecutableSkillColumnProvider extends ColumnLabelProvider {
		private Image exSkillIcon;
		private Image rexSkillIcon;
		
		@Override
		public String getText(Object element) {
			if (element instanceof Entry<?, ?>) {
				if (((Entry<?, ?>) element).getKey() instanceof ExecutableSkill) {
					ExecutableSkill executableSkill = (ExecutableSkill) ((Entry<?, ?>) element).getKey();
					return executableSkill.getName();
				}
				
			} else if (element instanceof ResourceExecutableSkill) {
				return ((ResourceExecutableSkill) element).toString();
			}
			return "ERROR (not an executable skill)";
		}
		@Override
		public Image getImage(Object element) {
			if (element instanceof Entry<?, ?>) {
				if (!(((Entry<?, ?>) element).getKey() instanceof ExecutableSkill)) {
					throw new IllegalArgumentException("Not an ExSkill");
				}
				return getExecutableSkillIcon();
			} else if (element instanceof ResourceExecutableSkill) {
				return getResourceExecutableSkillIcon();
			}
			return null;
		}
		
		private Image getExecutableSkillIcon() {
			if (exSkillIcon == null) {
				exSkillIcon = IconActivator.getImageDescriptor("icons/skill/exSkill.png").createImage();
			}

			return exSkillIcon;
		}

		private Image getResourceExecutableSkillIcon() {
			if (rexSkillIcon == null) {
				rexSkillIcon = IconActivator.getImageDescriptor("icons/skill/rexSkill.png").createImage();
			}

			return rexSkillIcon;
		}
	}
	
	private class SelectionColumnProvider extends ColumnLabelProvider {
		@Override
		public String getText(Object element) {
			return "";
		}

		@Override
		public Image getImage(Object element) {
			if (element instanceof Entry<?, ?>) {
				if (((Entry<?, ?>) element).getValue() instanceof Boolean ) {
					Boolean value = (Boolean) ((Entry<?, ?>) element).getValue();
					if (value) {
						return IconActivator.getImageDescriptor("icons/commons/checked.png").createImage();
					} else {
						return IconActivator.getImageDescriptor("icons/commons/unchecked.png").createImage();
					}
					
				}
			}
			return null;
		}
	}
}
