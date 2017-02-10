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

import masterviews.composite.abstracts.TreeComposite;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import skillpro.providers.order.OrderTreeContentProvider;
import skillpro.providers.order.OrderTreeLabelProvider;


public class OrderTreeComposite extends TreeComposite {

	public OrderTreeComposite(Composite parent, int style) {
		super(parent, style);
	}

	@Override
	protected LabelProvider initLabelProvider() {
		return new OrderTreeLabelProvider();
	}

	@Override
	protected IContentProvider initContentProvider() {
		return new OrderTreeContentProvider();
	}
	
	@Override
	public void createTreeViewer(Composite parent, int style) {
		super.createTreeViewer(parent, style);
		String[] headers = { "Order ID", "Qty.", "Product", "StartDate", "EndDate" };
		int[] bounds = {100,40,100,70,70};
		Tree tree = getViewer().getTree();
		tree.setHeaderVisible(true);
		for (int i = 0; i < headers.length; i++) {
			TreeColumn column = new TreeColumn(tree, SWT.LEFT, i);
			column.setAlignment(SWT.LEFT);
			column.setText(headers[i]);
			column.setWidth(bounds[i]);
		}
	}
}
