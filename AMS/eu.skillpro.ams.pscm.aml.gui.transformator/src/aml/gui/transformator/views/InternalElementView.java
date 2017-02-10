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

package aml.gui.transformator.views;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.part.ViewPart;

import skillpro.model.service.SkillproService;
import skillpro.model.update.Updatable;
import skillpro.model.update.UpdateType;
import skillpro.transformator.actions.SkillproClearAllAction;
import skillpro.transformator.actions.SkillproReverseTransformAction;
import skillpro.transformator.actions.SkillproTransformAction;
import aml.domain.Domain;
import aml.domain.InternalElement;
import aml.model.Hierarchy;
import aml.model.Root;
import aml.transformation.service.AMLTransformationService;
import amltransformation.composites.InternalTreeTableComposite;
import eu.skillpro.ams.pscm.gui.masterviews.Activator;

public class InternalElementView extends ViewPart implements Updatable {
	public static final String ID = InternalElementView.class.getName();
	private InternalTreeTableComposite internalTreeTableComposite;
	
	@Override
	public void createPartControl(Composite parent) {
		createViewer(parent);
		SkillproService.getUpdateManager().registerUpdatable(this, Domain.class);

		IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();
		toolbarManager.add(new SkillproTransformAction());
		toolbarManager.add(new SkillproClearAllAction());
		toolbarManager.add(new SkillproReverseTransformAction());
		
		getSite().setSelectionProvider(internalTreeTableComposite.getTreeViewer());
	}
	
	private void createViewer(Composite parent) {
		internalTreeTableComposite =  new InternalTreeTableComposite(parent, SWT.NULL) {
			@Override
			protected void addCoolbarItems(Composite parent) {
				super.addCoolbarItems(parent);
				ToolBar coolToolBar = new ToolBar(parent, SWT.VERTICAL);
				createToolItem(coolToolBar, SWT.VERTICAL, "D*", 
						Activator.getImageDescriptor("icons/cross.png").createImage(), 
						"Delete selected element", deleteSelectionListener());
			}
			
		};
		internalTreeTableComposite.setLayoutData(GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL)
				.grab(true, false).create());
		internalTreeTableComposite.setInput(AMLTransformationService.getAMLProvider()
				.getAMLModelRepo(InternalElement.class));
	}

	@Override
	public void setFocus() {
	}
	
	private void deleteHierarchy(Hierarchy<?> toDelete) {
		List<Hierarchy<InternalElement>> flattenedHierarchies = AMLTransformationService.getAMLProvider()
				.getAMLModelRepo(InternalElement.class).getFlattenedHierarchies();
		for (Hierarchy<?> hie : flattenHierarchy(toDelete)) {
			Iterator<Hierarchy<InternalElement>> iterator = flattenedHierarchies.iterator();
			while (iterator.hasNext()) {
				if (iterator.next().getActualElement().equals(hie.getActualElement())) {
					iterator.remove();
				}
			}
			if (hie.getElement() instanceof InternalElement) {
				InternalElement element = (InternalElement) hie.getElement();
				AMLTransformationService.getAMLProvider().removeInternalElement(element);
				Hierarchy<?> parent = hie.getParent();
				if (parent != null) {
					parent.getChildren().remove(hie);
				} else {
					for (Root<InternalElement> root : AMLTransformationService.getAMLProvider().getAMLModelRepo(InternalElement.class).getEntities()) {
						if (root.containsExactHierarchy(hie)) {
							root.getChildren().remove(hie);
						}
					}
				}
				hie.setParent(null);
				AMLTransformationService.getTransformationProvider().getTransformationRepo().getAdapterTransformablesMapping().remove(element);
			} else {
				throw new IllegalArgumentException("Not a Hierarchy of InternalElement");
			}
		}
	}
	
	private List<Hierarchy<?>> flattenHierarchy(Hierarchy<?> hie) {
		List<Hierarchy<?>> flattenedChildren = new ArrayList<>();
		
		flattenedChildren.add(hie);
		for (Hierarchy<?> child : hie.getChildren()) {
			flattenedChildren.addAll(flattenHierarchy(child));
		}
		
		return flattenedChildren;
	}
	
	private SelectionListener deleteSelectionListener() {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				//get selected element
				Object selectedElement = internalTreeTableComposite.getViewerSelection().getFirstElement();
				if (selectedElement != null) {
					//is empty?
					if (selectedElement instanceof Hierarchy<?>) {
						deleteHierarchy((Hierarchy<?>) selectedElement);
						SkillproService.getUpdateManager().notify(UpdateType.AML_DOMAIN_DELETED, null);
					} else {
						if (selectedElement instanceof Root<?>) {
							deleteRoot((Root<?>) selectedElement);
							SkillproService.getUpdateManager().notify(UpdateType.AML_DOMAIN_DELETED, null);
						}
					}
				}
			}

			private void deleteRoot(Root<?> root) {
				List<Hierarchy<?>> toDelete = new ArrayList<>();
				toDelete.addAll(root.getChildren());
				for (Hierarchy<?> rootHie : toDelete) {
					deleteHierarchy((Hierarchy<?>) rootHie);
				}
				AMLTransformationService.getAMLProvider().removeRoot(InternalElement.class, root);
			}
		};
	}
	
	@Override
	public void update(UpdateType type) {
		switch (type) {
		case NEW_DATA_IMPORTED:
			internalTreeTableComposite.disposeAllItems();
			internalTreeTableComposite.setInput(AMLTransformationService.getAMLProvider().getAMLModelRepo(InternalElement.class));
			internalTreeTableComposite.getTreeViewer().refresh();
		case AML_DOMAIN_CREATED:
			internalTreeTableComposite.getTreeViewer().refresh();
		case AML_DOMAIN_UPDATED:
			internalTreeTableComposite.getTreeViewer().refresh();
		case AML_DOMAIN_DELETED:
			internalTreeTableComposite.getTreeViewer().refresh();
		default:
			break;
		}
	}
}
