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

package amltransformation.composites;

import java.util.ArrayList;
import java.util.List;

import masterviews.composite.abstracts.TreeTableComposite;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.dialogs.SelectionDialog;

import transformation.interfaces.ITransformable;
import aml.domain.Role;
import aml.model.Hierarchy;
import aml.skillpro.transformation.interfaces.IFactoryNodeTransformable;
import aml.skillpro.transformation.interfaces.IFactoryTransformable;
import aml.skillpro.transformation.interfaces.IProductTransformable;
import aml.skillpro.transformation.interfaces.IProductionSkillTransformable;
import aml.skillpro.transformation.interfaces.IResourceSkillTransformable;
import aml.skillpro.transformation.interfaces.IResourceTransformable;
import aml.skillpro.transformation.interfaces.ITemplateSkillTransformable;
import aml.transformation.service.AMLTransformationService;
import amltransformation.dialogs.SetTransformableDialog;
import amltransformation.providers.aml.DomainContentProvider;
import amltransformation.providers.aml.DomainLabelProvider;

public class RoleTreeTableComposite extends TreeTableComposite {
	private TreeColumn secondColumn;

	public RoleTreeTableComposite(Composite parent, int style) {
		super(parent, SWT.NONE);
		//menu
		addContextMenu();
	}
	/**
	 * Returns the tree viewer.
	 * @return the tree viewer.
	 */
	public TreeViewer getTreeViewer() {
		return viewer;
	}

	/**
	 * Adds to the tree a context menu for showing and 
	 * setting images for its elements. The menu is shown only
	 */
	private void addContextMenu() {
		MenuManager menuMgr = new MenuManager();
	    menuMgr.setRemoveAllWhenShown(true);
	    menuMgr.addMenuListener(new IMenuListener() {
	        public void menuAboutToShow(IMenuManager manager) {
	        	if (viewer.getSelection().isEmpty()) {
                    return;
                }
	        	
	        	final Role selection = (Role) ((IStructuredSelection) viewer.getSelection()).getFirstElement();
	        	if (selection instanceof Role) {
	        		
	        		
	        		manager.add(new Action() {
	        			
	        			@Override
	        			public String getText() {
	        				return "Assign transformable for current element";
	        			}
	        			
	        			@SuppressWarnings("unchecked")
						@Override
	        			public void run() {
	        				if (selection instanceof Role) {
	        					SetTransformableDialog dialog = new SetTransformableDialog(getShell());
	        					dialog.open();
	        					if (dialog.getReturnCode() != SelectionDialog.CANCEL) {
	        						AMLTransformationService.getTransformationProvider().putTransformable(selection
	        								, (Class<? extends ITransformable>) dialog.getSelectedTransformable());
	        						viewer.refresh();
	        					}
	        				}
	        			}
	        		});
	        		manager.add(new Action() {
	        			@Override
	        			public String getText() {
	        				return "Assign transformable for current element and children";
	        			}
	        			
	        			@SuppressWarnings("unchecked")
						@Override
	        			public void run() {
	        				if (selection instanceof Role) {
	        					SetTransformableDialog dialog = new SetTransformableDialog(getShell());
	        					dialog.open();
	        					if (dialog.getReturnCode() != SelectionDialog.CANCEL) {
	        						AMLTransformationService.getTransformationProvider().putTransformable(selection
	        								, (Class<? extends ITransformable>) dialog.getSelectedTransformable());
	        						List<Role> children = getAllFlattenedChildrenRoles(selection);
	        						for (Role role : children) {
	        							AMLTransformationService.getTransformationProvider().putTransformable(role
		        								, (Class<? extends ITransformable>) dialog.getSelectedTransformable());
	        						}
	        						viewer.refresh();
	        					}
	        				}
	        			}
	        			
	        		});
	        		manager.add(new Separator());
	        		manager.add(new Action() {
	        			
	        			@Override
	        			public String getText() {
	        				return "Assign transformables automatically";
	        			}
	        			
	        			@Override
	        			public void run() {
	        				List<Hierarchy<Role>> allHierarchies = AMLTransformationService.getAMLProvider().getAMLModelRepo(Role.class).getFlattenedHierarchies();
	        				for (Hierarchy<?> hie : allHierarchies) {
	        					String name = hie.getName().toLowerCase();
        						Role role = (Role) hie.getElement();
	        					if (name.contains("templateskill")) {
	        						AMLTransformationService.getTransformationProvider().putTransformable(role
	        								, ITemplateSkillTransformable.class);
	        						setTransformablesRoleRecursive(role, ITemplateSkillTransformable.class);
	        					} else if (name.contains("productionskill")) {
	        						AMLTransformationService.getTransformationProvider().putTransformable(role
	        								, IProductionSkillTransformable.class);
	        						setTransformablesRoleRecursive(role, IProductionSkillTransformable.class);
	        					} else if (name.contains("resourceskill")) {
	        						AMLTransformationService.getTransformationProvider().putTransformable(role
	        								, IResourceSkillTransformable.class);
	        						setTransformablesRoleRecursive(role, IResourceSkillTransformable.class);
	        					} else if (name.contains("skillproresource")) {
	        						AMLTransformationService.getTransformationProvider().putTransformable(role
	        								, IResourceTransformable.class);
	        						setTransformablesRoleRecursive(role, IResourceTransformable.class);
	        					} else if (name.contains("skillproproduct")) {
	        						AMLTransformationService.getTransformationProvider().putTransformable(role
	        								, IProductTransformable.class);
	        						setTransformablesRoleRecursive(role, IProductTransformable.class);
	        					} else if (name.contains("resourcestructure")) {
	        						AMLTransformationService.getTransformationProvider().putTransformable(role
	        								, IFactoryNodeTransformable.class);
	        						setTransformablesRoleRecursive(role, IFactoryNodeTransformable.class);
	        					} else if (name.contains("enterprise")) {
	        						AMLTransformationService.getTransformationProvider().putTransformable(role
	        								, IFactoryTransformable.class);
	        						setTransformablesRoleRecursive(role, IFactoryTransformable.class);
	        					}
	        				}
	        				viewer.refresh();
	        			}
	        		});
	        		manager.add(new Separator());
	        		manager.add(new Action() {
	        			@Override
	        			public String getText() {
	        				return "Unassign transformable";
	        			}
	        			
	        			@Override
	        			public void run() {
	        				if (selection instanceof Role) {
	        					AMLTransformationService.getTransformationProvider().getTransformationRepo().getInterfaceTransformablesMapping().remove(selection);
	        					viewer.refresh();
	        				}
	        			}
	        		});
	        		manager.add(new Action() {
	        			@Override
	        			public String getText() {
	        				return "Unassign all";
	        			}
	        			
	        			@Override
	        			public void run() {
	        				AMLTransformationService.getTransformationProvider().getTransformationRepo().getInterfaceTransformablesMapping().clear();
	        				viewer.refresh();
	        			}
	        		});
	        	}
	        }
	    });
	    Menu menu = menuMgr.createContextMenu(viewer.getControl());
	    viewer.getControl().setMenu(menu);
	}
	
	private void setTransformablesRoleRecursive(Role role, Class<? extends ITransformable> clz) {
		for (Role child : role.getChildren()) {
			AMLTransformationService.getTransformationProvider().putTransformable(child, clz);
			if (child.getChildren() != null && !child.getChildren().isEmpty()) {
				setTransformablesRoleRecursive(child, clz);
			}
		}
	}
	
	private List<Role> getAllFlattenedChildrenRoles(Role role) {
		List<Role> roles = new ArrayList<>();
		for (Role child : role.getChildren()) {
			roles.add(child);
			if (child.getChildren() != null && !child.getChildren().isEmpty()) {
				roles.addAll(getAllFlattenedChildrenRoles(child));
			}
		}
		return roles;
	}

	protected void addColumn(final Tree tree) {
		secondColumn = new TreeColumn(tree, SWT.RIGHT | SWT.CHECK, 1);
		secondColumn.setAlignment(SWT.LEFT);
		secondColumn.setText("Transformable");
		secondColumn.setWidth(150);
		TreeViewerColumn treeViewerColumn = new TreeViewerColumn(viewer, secondColumn);
		
		treeViewerColumn.setLabelProvider(new TransformableColumnProvider());
	}

	// initializes the input that will be used by the tree viewer.
	@SuppressWarnings("unused")
	private void initInput() {
		viewer.setInput(new ArrayList<>());
	}

	protected LabelProvider initLabelProvider() {
		return new DomainLabelProvider();
	}

	protected IContentProvider initContentProvider() {
		return new DomainContentProvider();
	}
	
	
	private class TransformableColumnProvider extends ColumnLabelProvider {
		@Override
		public String getText(Object element) {
			String text = "";
			Class<? extends ITransformable> transClass = AMLTransformationService.getTransformationProvider().getTransformationRepo().getInterfaceTransformablesMapping().get(element);
			if (transClass != null) {
				text = transClass.getSimpleName();
			}
			return text;
		}

		@Override
		public Image getImage(Object element) {
			return null;
		}
	}
}
