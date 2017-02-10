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
import java.util.Set;

import masterviews.composite.abstracts.TreeTableComposite;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.dialogs.SelectionDialog;

import transformation.interfaces.ITransformable;
import aml.domain.InternalElement;
import aml.domain.Role;
import aml.model.AttributeDesignator;
import aml.model.Hierarchy;
import aml.model.Root;
import aml.skillpro.transformation.util.TransformationUtil;
import aml.transformation.service.AMLTransformationService;
import amltransformation.dialogs.SetElementTransformableDialog;
import amltransformation.providers.aml.DomainContentProvider;
import amltransformation.providers.aml.DomainLabelProvider;
import eu.skillpro.ams.pscm.gui.masterviews.Activator;

public class InternalTreeTableComposite extends TreeTableComposite {
	private TreeColumn secondColumn;

	public InternalTreeTableComposite(Composite parent, int style) {
		super(parent, SWT.NONE);
		//menu
		addContextMenu();
	}
	// add buttons
	@Override
	protected void addCoolbarItems(Composite parent) {
		ToolBar coolToolBar = new ToolBar(parent, SWT.VERTICAL);
		createToolItem(coolToolBar, SWT.VERTICAL, "columns", Activator
				.getImageDescriptor("icons/common_tab.gif").createImage(),
				"Configure columns visibility", changeColumnsVisibilityListener());
	}
	
	private void createMenuItem(Menu parent, final TreeColumn column) {
		final MenuItem itemName = new MenuItem(parent, SWT.CHECK);
		itemName.setText(column.getText());
		itemName.setSelection(column.getResizable());
		itemName.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (itemName.getSelection()) {
					column.setWidth(150);
					column.setResizable(true);
				} else {
					column.setWidth(0);
					column.setResizable(false);
				}
			}
		});
	}

	public IStructuredSelection getViewerSelection() {
		return (IStructuredSelection) getTreeViewer().getSelection();
	}
	
	/**
	 * Adds to the tree a context menu for showing and 
	 * setting images for its elements. The menu is shown only
	 */
	private void addContextMenu() {
		MenuManager menuMgr = new MenuManager();
	    menuMgr.setRemoveAllWhenShown(true);
	    menuMgr.addMenuListener(new IMenuListener() {
	        @SuppressWarnings("unchecked")
			public void menuAboutToShow(IMenuManager manager) {
	        	if (viewer.getSelection().isEmpty()) {
                    return;
                }
	        	
	        	if (((IStructuredSelection) viewer.getSelection()).getFirstElement() instanceof Hierarchy<?>) {
	        		final Hierarchy<InternalElement> selection = (Hierarchy<InternalElement>) ((IStructuredSelection) viewer.getSelection()).getFirstElement();
	        		manager.add(new Action() {
	        			@Override
	        			public String getText() {
	        				return "Assign for current element";
	        			}
	        			
	        			@Override
	        			public void run() {
	        				if (selection instanceof Hierarchy<?>) {
	        					Role role = selection.getElement().getRequiredRole();
	        					if (role != null) {
	        						Class<? extends ITransformable> trans = AMLTransformationService.getTransformationProvider().getTransformationRepo().getInterfaceTransformablesMapping().get(role);
	        						if (trans != null) {
	        							SetElementTransformableDialog dialog = new SetElementTransformableDialog(getShell(), trans);
	        							dialog.open();
	        							if (dialog.getReturnCode() != SelectionDialog.CANCEL) {
	        								AMLTransformationService.getTransformationProvider().putElementTransformable(selection.getElement(),
	        										(Class<? extends ITransformable>) dialog.getSelectedTransformable());
	        								viewer.refresh();
	        							}
	        							
	        						} else {
	        							MessageDialog.openInformation(getShell(), "Unassigned Role", role.getName() + " has to be assigned to a transformable first!");
	        						}
	        						
	        					} else {
	        						MessageDialog.openInformation(getShell(), "No Role", selection.getName() + " has no role!");
	        					}
	        				}
	        			}
	        		});
	        		manager.add(new Action() {
	        			@Override
	        			public String getText() {
	        				return "Assign for current element and children";
	        			}
	        			
	        			@Override
	        			public void run() {
	        				if (selection instanceof Hierarchy<?>) {
	        					Role role = selection.getElement().getRequiredRole();
	        					if (role != null) {
	        						Class<? extends ITransformable> trans = AMLTransformationService.getTransformationProvider().getTransformationRepo().getInterfaceTransformablesMapping().get(role);
	        						if (trans != null) {
	        							SetElementTransformableDialog dialog = new SetElementTransformableDialog(getShell(), trans);
	        							dialog.open();
	        							if (dialog.getReturnCode() != SelectionDialog.CANCEL) {
	        								Class<? extends ITransformable> selectedTransformable = (Class<? extends ITransformable>) dialog.getSelectedTransformable();
											AMLTransformationService.getTransformationProvider().putElementTransformable(selection.getElement(),
													selectedTransformable);
	        								List<Hierarchy<InternalElement>> children = getFlattenedHierarchyChildren(selection);
	        								for (Hierarchy<?> child : children) {
	        									Class<? extends ITransformable> childTrans = AMLTransformationService.getTransformationProvider().getTransformationRepo().getInterfaceTransformablesMapping()
    											.get(((InternalElement) child.getElement()).getRequiredRole());
	        									if (selection.getElement().getClass().isAssignableFrom(child.getElement().getClass())
	        											&& TransformationUtil.getAllModelsImplementingTransformable(childTrans).contains(selectedTransformable)) {
	        										AMLTransformationService.getTransformationProvider().putElementTransformable((InternalElement) child.getElement(),
	        												selectedTransformable);
	        									}
	        								}
	        								viewer.refresh();
	        							}
	        							
	        						} else {
	        							MessageDialog.openInformation(getShell(), "Unassigned Role", role.getName() + " has to be assigned to a transformable first!");
	        						}
	        						
	        					} else {
	        						MessageDialog.openInformation(getShell(), "No Role", selection.getName() + " has no role!");
	        					}
	        				}
	        			}
	        		});
	        		manager.add(new Separator());
	        		manager.add(new Action() {
	        			@Override
	        			public String getText() {
	        				return "Assign automatically";
	        			}
	        			
	        			@Override
	        			public void run() {
	        				List<Hierarchy<InternalElement>> allHierarchies = AMLTransformationService.getAMLProvider().getAMLModelRepo(InternalElement.class).getFlattenedHierarchies();
	        				for (Hierarchy<InternalElement> hie : allHierarchies) {
	        					String name = hie.getName().toLowerCase();
	        					if (!name.contains("dummy")) {
	        						Role role = ((InternalElement) hie.getElement()).getRequiredRole();
	        						if (role != null) {
	        							Class<? extends ITransformable> trans = AMLTransformationService.getTransformationProvider().getTransformationRepo().getInterfaceTransformablesMapping().get(role);
	        							if (trans != null) {
	        								Set<Class<?>> transformables = TransformationUtil.getAllModelsImplementingTransformable(trans);
	        								if (transformables.size() == 1) {
	        									AMLTransformationService.getTransformationProvider().putElementTransformable((InternalElement) hie.getElement(),
	        											(Class<? extends ITransformable>) transformables.iterator().next());
	        								}
	        								viewer.refresh();
	        								
	        							}
	        						}
	        					}
	        					
	        				}
	        			}
	        		});
	        		manager.add(new Separator());
	        		manager.add(new Action() {
	        			@Override
	        			public String getText() {
	        				return "Unassign";
	        			}
	        			
	        			@Override
	        			public void run() {
	        				if (selection instanceof Hierarchy<?>) {
	        					AMLTransformationService.getTransformationProvider().getTransformationRepo().getAdapterTransformablesMapping().remove(selection.getElement());
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
	        				AMLTransformationService.getTransformationProvider().getTransformationRepo().getAdapterTransformablesMapping().clear();
	        				viewer.refresh();
	        			}
	        		});
	        		
	        	}
	        }
	    });
	    
	    Menu menu = menuMgr.createContextMenu(viewer.getControl());
	    viewer.getControl().setMenu(menu);
	}
	
	public List<Hierarchy<InternalElement>> getFlattenedHierarchyChildren(Hierarchy<InternalElement> hie) {
		List<Hierarchy<InternalElement>> hierarchies = new ArrayList<>();
		if (hie == null) {
			for (Root<InternalElement> root : AMLTransformationService.getAMLProvider().getAMLModelRepo(InternalElement.class).getEntities()) {
				for (Hierarchy<InternalElement> child : root.getChildren()) {
					hierarchies.addAll(getFlattenedHierarchyChildren(child));
				}
			}
		} else {
			for (Hierarchy<InternalElement> child : hie.getChildren()) {
				hierarchies.add(child);
				if (child.getChildren() != null && !child.getChildren().isEmpty()) {
					hierarchies.addAll(getFlattenedHierarchyChildren(child));
				}
			}
		}
		return hierarchies;
	}
	
	@Override
	protected void addColumn(Tree tree) {
		createMenuItem(headerMenu, firstColumn);
		addConfigurableColumn(tree);
		addRoleColumn(tree);
		addSupportedRolesColumn(tree);
		addAttributesColumn(tree);
	}

	private void addConfigurableColumn(final Tree tree) {
		secondColumn = new TreeColumn(tree, SWT.RIGHT | SWT.CHECK, 1);
		secondColumn.setAlignment(SWT.LEFT);
		secondColumn.setText("Transformable");
		secondColumn.setWidth(150);
		TreeViewerColumn treeViewerColumn = new TreeViewerColumn(viewer, secondColumn);
		
		treeViewerColumn.setLabelProvider(new TransformableColumnProvider());
		createMenuItem(headerMenu, secondColumn);
	}
	
	private void addRoleColumn(final Tree tree) {
		
		TreeColumn column = new TreeColumn(tree, SWT.RIGHT | SWT.CHECK, 2);
		column.setAlignment(SWT.LEFT);
		column.setText("Assigned Roles");
		column.setWidth(150);
		TreeViewerColumn treeViewerColumn = new TreeViewerColumn(viewer, column);
		
		treeViewerColumn.setLabelProvider(new TransformableColumnRoleProvider());
		createMenuItem(headerMenu, column);
	}
	
	private void addSupportedRolesColumn(final Tree tree) {
		
		TreeColumn column = new TreeColumn(tree, SWT.RIGHT | SWT.CHECK, 3);
		column.setAlignment(SWT.LEFT);
		column.setText("Supported Roles");
		column.setWidth(150);
		TreeViewerColumn treeViewerColumn = new TreeViewerColumn(viewer, column);
		
		treeViewerColumn.setLabelProvider(new TransformableColumnSupportedRoleProvider());
		createMenuItem(headerMenu, column);
	}
	
	private void addAttributesColumn(final Tree tree) {
		TreeColumn column = new TreeColumn(tree, SWT.RIGHT | SWT.CHECK, 4);
		column.setAlignment(SWT.LEFT);
		column.setText("Attributes");
		column.setWidth(250);
		TreeViewerColumn treeViewerColumn = new TreeViewerColumn(viewer, column);
		
		treeViewerColumn.setLabelProvider(new TransformableColumnAttributeProvider());
		createMenuItem(headerMenu, column);
	}


	// initializes the input that will be used by the tree viewer.
	@SuppressWarnings("unused")
	private void initInput() {
		viewer.setInput(new ArrayList<>());
	}

	
	private SelectionListener changeColumnsVisibilityListener() {
		return new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				headerMenu.setVisible(true);
			};
		};
	}
	
	/**
	 * Sets the input for the tree viewer.
	 * 
	 * @param object
	 *            is a list of objects that will be used as the input for the
	 *            tree viewer.
	 */
	public void setInput(Object object) {
		viewer.setInput(object);
	}

	protected LabelProvider initLabelProvider() {
		return new DomainLabelProvider();
	}

	protected IContentProvider initContentProvider() {
		return new DomainContentProvider();
	}
	
	private class TransformableColumnProvider extends ColumnLabelProvider {
		@SuppressWarnings("unchecked")
		@Override
		public String getText(Object element) {
			if (element instanceof Hierarchy<?>) {
				String text = "";
				Class<? extends ITransformable> transClass = AMLTransformationService.getTransformationProvider()
						.getTransformationRepo().getAdapterTransformablesMapping()
						.get(((Hierarchy<InternalElement>) element).getElement());
				if (transClass != null) {
					text = transClass.getSimpleName();
				}
				return text;
				
			} else {
				return "Not transformable";
			}
		}

		@Override
		public Image getImage(Object element) {
			return null;
		}
	}
	
	private class TransformableColumnRoleProvider extends ColumnLabelProvider {
		@Override
		public String getText(Object element) {
			if (element instanceof Hierarchy<?>) {
				String text = "";
				InternalElement ie = (InternalElement) ((Hierarchy<?>) element).getElement();
				Role requiredRole = ie.getRequiredRole();
				if (requiredRole != null) {
					text = requiredRole.getName();
				}
				return text;
				
			} else {
				return "No Roles.";
			}
		}

		@Override
		public Image getImage(Object element) {
			return null;
		}
	}
	
	private class TransformableColumnSupportedRoleProvider extends ColumnLabelProvider {
		@Override
		public String getText(Object element) {
			if (element instanceof Hierarchy<?>) {
				InternalElement ie = (InternalElement) ((Hierarchy<?>) element).getElement();
				List<Role> supportedRoles = ie.getSupportedRoles();
				StringBuffer sb = new StringBuffer();
				for (Role sup : supportedRoles) {
					sb.append(sup.getName());
					sb.append(", ");
				}
				//delete the last ", "
				if (sb.toString().endsWith(", ")) {
					sb.delete(sb.length() - 2, sb.length() - 1);
				}
				return sb.toString();
				
			} else {
				return "No Supported Roles.";
			}
		}

		@Override
		public Image getImage(Object element) {
			return null;
		}
	}
	
	private class TransformableColumnAttributeProvider extends ColumnLabelProvider {
		@Override
		public String getText(Object element) {
			if (element instanceof Hierarchy<?>) {
				InternalElement ie = (InternalElement) ((Hierarchy<?>) element).getElement();
				List<AttributeDesignator> designators = ie.getDesignators();
				StringBuffer sb = new StringBuffer();
				
				for (AttributeDesignator ad:designators) {
					sb.append(ad.getAttribute().getName());
					if (ad.getValue() != null && !ad.getValue().isEmpty()) {
						sb.append(": " + ad.getValue());
					}
					sb.append("; ");
				}
				//delete the last ", "
				if (sb.toString().endsWith(", ")) {
					sb.delete(sb.length() - 2, sb.length() - 1);
				}
				return sb.toString();
				
			} else {
				return "No Attributes.";
			}
		}

		@Override
		public Image getImage(Object element) {
			return null;
		}
	}
}
