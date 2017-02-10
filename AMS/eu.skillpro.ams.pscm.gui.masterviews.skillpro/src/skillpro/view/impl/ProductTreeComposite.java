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
import java.util.List;

import masterviews.composite.abstracts.TreeComposite;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;

import skillpro.providers.product.ProductContentProvider;
import skillpro.providers.product.ProductLabelProvider;

public class ProductTreeComposite extends TreeComposite {
	public ProductTreeComposite(Composite parent, int style) {
		super(parent, style);
		addContextMenu();
	}

	public IStructuredSelection getViewerSelection() {
		return (IStructuredSelection) getTreeViewer().getSelection();
	}

	@Override
	protected LabelProvider initLabelProvider() {
		return new ProductLabelProvider();
	}

	@Override
	protected IContentProvider initContentProvider() {
		return new ProductContentProvider();
	}

	private void addContextMenu() {
		MenuManager menuMgr = new MenuManager();
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				if (getViewer().getSelection().isEmpty()) {
					return;
				}

				final Object firstElement = ((IStructuredSelection) getViewer().getSelection()).getFirstElement();
				for (Action action : createExtraContextActions(firstElement)) {
					manager.add(action);
				}
			}
		});

		Menu menu = menuMgr.createContextMenu(getViewer().getControl());
		getViewer().getControl().setMenu(menu);
	}
	
	//please overwrite this if you want to create extra context menu items
	protected List<Action> createExtraContextActions(final Object selection) {
		return new ArrayList<>();
	}
}
