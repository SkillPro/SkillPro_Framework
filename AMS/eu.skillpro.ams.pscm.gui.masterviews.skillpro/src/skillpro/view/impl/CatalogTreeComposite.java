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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import masterviews.composite.abstracts.TreeComposite;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import skillpro.model.assets.Factory;
import skillpro.model.assets.FactoryNode;
import skillpro.model.assets.Resource;
import skillpro.model.assets.Setup;
import skillpro.model.repo.Repo;
import skillpro.model.service.SkillproService;
import skillpro.model.skills.ResourceSkill;
import skillpro.model.update.UpdateType;
import skillpro.providers.asset.AssetTreeContentProvider;
import skillpro.providers.catalog.CatalogTreeLabelProvider;
import utils.AssetTransfer;
import eu.skillpro.ams.pscm.gui.dndhelper.DragHelper;

public class CatalogTreeComposite extends TreeComposite {
	public CatalogTreeComposite(Composite parent, int style) {
		super(parent, style);
		initDragAndDrop(getViewer());
		
		addContextMenu();
	}

	@Override
	protected LabelProvider initLabelProvider() {
		return new CatalogTreeLabelProvider();
	}

	@Override
	protected IContentProvider initContentProvider() {
		return new AssetTreeContentProvider();
	}

	protected void initDragAndDrop(final TreeViewer viewer) {
		int dragOperations = DND.DROP_COPY | DND.DROP_MOVE;
		int dropOperations = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_DEFAULT;
		Transfer[] transferTypes = new Transfer[] {
				AssetTransfer.getInstance(), TextTransfer.getInstance() };
		final TreeItem[] dragSourceItem = new TreeItem[1];
		DragSourceListener dragListener = new CatalogDragListener(dragSourceItem);
		DropTargetListener dropListener = new CatalogDropListener();
		viewer.addDragSupport(dragOperations, transferTypes, dragListener);
		viewer.addDropSupport(dropOperations, transferTypes, dropListener);
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
	
	private Resource copyResource(Resource resource) {
		Resource copy = null;
		List<Setup> newSetups = new ArrayList<>();
		copy = new Resource(resource.getName(), newSetups, null);
		for (Setup config : resource.getSetups()) {
			Setup newConfig = new Setup(config.getName(), copy);
			for (ResourceSkill rs : config.getResourceSkills()) {
				ResourceSkill newResourceSkill = new ResourceSkill(rs.getName(), rs.getTemplateSkill(), copy
						, rs.getPrePostRequirements());
				newConfig.addResourceSkill(newResourceSkill);
			}
			//current configuration for the new copied workplace?
			newSetups.add(newConfig);
		}
		//catalogs don't have parents.
		
		for (Setup conf : newSetups) {
			if (conf.getName().equals(resource.getCurrentSetup().getName())) {
				((Resource) copy).setCurrentSetup(conf);
				break;
			}
		}
		copy.setLayoutable(resource.isLayoutable());
		copy.setCurrentCoordinates(resource.getCurrentCoordinates());
		copy.setSize(resource.getSize());
		
		return copy;
	}
	
	/**
	 * Drop listener
	 * 
	 */
	private final class CatalogDropListener extends DropTargetAdapter {
		@Override
		public void dragOver(DropTargetEvent event) {
			event.feedback = DND.FEEDBACK_EXPAND | DND.FEEDBACK_SCROLL;
			if (event.item != null) {
				Object[] delivery = (Object[]) DragHelper.data;
				List<FactoryNode> drops = convertDelivery(delivery);
				for (FactoryNode drop : drops) {
					if (drop instanceof Factory) {
						//because it contains root
						event.detail = DND.DROP_NONE;
						return;
					} else if (!(drop instanceof FactoryNode)) {
						event.detail = DND.DROP_NONE;
						return;
					}
				}
				if (event.item.getData() instanceof Resource) {
					event.detail = DND.DROP_NONE;
				} else if (event.item.getData() instanceof FactoryNode) {
					event.detail = DND.DROP_MOVE;
				} else {
					event.detail = DND.DROP_NONE;
				}
			}
			// doDragOver(event);
		}

		@Override
		public void drop(DropTargetEvent event) {
			Object[] delivery = (Object[]) DragHelper.data;
			// put the object on the root.
			// Unreachable code?
			List<FactoryNode> drop = convertDelivery(delivery);
			List<Resource> filteredEntries = filterDescendantEntries(getCopiedResources(drop));
			if (event.item == null) {
				
				List<FactoryNode> haveParents = filterParentlessEntries(filteredEntries);
				detachFromParents(haveParents);
				SkillproService.getSkillproProvider().refreshAssetRepo();
				
				SkillproService.getUpdateManager().notify(UpdateType.ASSET_UPDATED, FactoryNode.class);
				
			} else if (((TreeItem) event.item).getData() instanceof Resource) {
				event.detail = DND.DROP_NONE;
				return;
			} else if (((TreeItem) event.item).getData() instanceof FactoryNode) {
				FactoryNode dropTarget = (FactoryNode) event.item.getData();

				// filtering loops
				filterLoopingEntries(dropTarget, filteredEntries);
				changeParent(dropTarget, filteredEntries);
				
				SkillproService.getSkillproProvider().refreshAssetRepo();
				
				SkillproService.getUpdateManager().notify(UpdateType.ASSET_UPDATED, FactoryNode.class);
				
			} else if (((TreeItem) event.item).getData() instanceof Repo) {
				event.detail = DND.DROP_NONE;
				return;
			}
			//notify that viewer is updated!
			SkillproService.getUpdateManager().notify(UpdateType.CATALOG_UPDATED, FactoryNode.class);
			getViewer().refresh();
		}

		private void changeParent(FactoryNode dropTarget,
				List<Resource> entries) {
			for (FactoryNode entry : entries) {
				if (entry.getParent() != null) {
					FactoryNode parent = entry.getParent();
					parent.getSubNodes().remove(entry);
					entry.setParent(null);
					SkillproService.getSkillproProvider().updateFactoryNode(parent);
				}
				dropTarget.addSubNode(entry);
				SkillproService.getSkillproProvider().updateFactoryNode(entry);
			}
			SkillproService.getSkillproProvider().updateFactoryNode(dropTarget);
		}

		private List<FactoryNode> convertDelivery(Object[] delivery) {
			List<FactoryNode> result = new LinkedList<FactoryNode>();
			for (Object o : delivery) {
				result.add((FactoryNode) o);
			}
			return result;
		}
		
		private List<Resource> getCopiedResources(List<FactoryNode> nodes) {
			List<Resource> result = new LinkedList<>();
			
			for (FactoryNode node : nodes) {
				if (node instanceof Resource) {
					result.add(SkillproService.getSkillproProvider().copyResourceToCatalog((Resource) node));
				}
			}
			
			return result;
		}

		/**
		 * Calculates the selection hull. If a parent and a descendant of the
		 * same subtree are selected, the latter is removed from the list. This
		 * method edits the list in place.
		 * 
		 * @param selectedEntries
		 *            the entries selection.
		 * @return the list, filtered.
		 */
		private List<Resource> filterDescendantEntries(
				List<Resource> selectedEntries) {
			for (Iterator<Resource> iterator = selectedEntries.iterator(); iterator
					.hasNext();) {
				FactoryNode asset = iterator.next();
				if (isParentInSelection(asset, selectedEntries)) {
					iterator.remove();
				}
			}
			return selectedEntries;
		}

		private boolean isParentInSelection(FactoryNode entry,
				List<Resource> selectedEntries) {
			for (FactoryNode ancestor : entry.ascendants()) {
				if (selectedEntries.contains(ancestor)) {
					return true;
				}
			}
			return false;
		}

		/**
		 * Leaves out the entries causing a loop in the structure upon drop.
		 * This method edits the list in place!
		 * 
		 * @param dropTarget
		 *            the target
		 * @param entries
		 *            the entries to check.
		 */
		private void filterLoopingEntries(FactoryNode dropTarget,
				List<Resource> entries) {
			for (Iterator<Resource> i = entries.iterator(); i.hasNext();) {
				FactoryNode entry = i.next();
				if (dropTarget.ascendantsAndSelf().contains(entry)) {
					i.remove();
				}
			}
		}

		/**
		 * Leaves only those entries, which have a parent. Test is based on a
		 * <code>null</code> equality check.
		 * 
		 * @param delivery
		 *            the dropped entries.
		 * @return a new list of type entries which have a reference to a
		 *         parent.
		 */
		private List<FactoryNode> filterParentlessEntries(
				List<Resource> delivery) {
			List<FactoryNode> collectedEntries = new ArrayList<FactoryNode>();
			for (Object object : delivery) {
				FactoryNode entry = (FactoryNode) object;
				if (entry.getParent() != null) {
					collectedEntries.add(entry);
				}
			}
			return collectedEntries;
		}

		/**
		 * Deletes the link of the given type entries to their parents.
		 * 
		 * @param haveParents
		 *            the type entries to process.
		 */
		private void detachFromParents(List<FactoryNode> haveParents) {
			for (FactoryNode entry : haveParents) {
				FactoryNode parent = entry.getParent();
				parent.getSubNodes().remove(entry);
				entry.setParent(null);
				SkillproService.getSkillproProvider().updateFactoryNode(entry);
				SkillproService.getSkillproProvider().updateFactoryNode(parent);
			}
		}

	}
	
	public boolean checkSelection() {
		IStructuredSelection selection = getViewerSelection();
		boolean hasSelection = selection != null && !selection.isEmpty();
		return hasSelection;
	}

	public boolean isSelectionHierarchy() {
		IStructuredSelection selection = getViewerSelection();
		boolean isSelectionHierarchy = selection != null && !selection.isEmpty() 
				&& !(selection.getFirstElement() instanceof Resource) && selection.getFirstElement() instanceof FactoryNode;
		return isSelectionHierarchy;
	}
	
	public IStructuredSelection getViewerSelection() {
		return (IStructuredSelection) getTreeViewer().getSelection();
	}

	/**
	 * Drag listener
	 * 
	 */
	private final class CatalogDragListener implements DragSourceListener {
		private final TreeItem[] dragSourceItem;

		/**
		 * private constructor
		 * 
		 * @param dragSourceItem
		 *            dragsource item
		 * @param tr
		 *            tree
		 */
		private CatalogDragListener(TreeItem[] dragSourceItem) {
			this.dragSourceItem = dragSourceItem;
		}

		@Override
		public void dragStart(DragSourceEvent event) {
			event.doit = !getViewer().getSelection().isEmpty();
			DragHelper.sameObject = false;
			dragSetData(event);
		}

		@Override
		public void dragSetData(DragSourceEvent event) {
			List<FactoryNode> selectedEntries = getCopiedResourcesSelection(getTreeItemSelection(event));
			DragHelper.data = selectedEntries.toArray();
			event.data = DragHelper.data;
		}

		@Override
		public void dragFinished(DragSourceEvent event) {
			dragSourceItem[0] = null;
		}

		/**
		 * Accesses the event from the drag source event.
		 * 
		 * @param event
		 *            the event.
		 * @return a selection of tree items.
		 */
		private TreeItem[] getTreeItemSelection(DragSourceEvent event) {
			DragSource source = (DragSource) event.getSource();
			Tree widget = (Tree) source.getControl();
			return widget.getSelection();
		}

		/**
		 * Extracts the given selection (an array of {@link TreeItem}s) to a
		 * list of {@link FactoryNode}.
		 * 
		 * @param selectedItems
		 *            the selection.
		 * @return a list of type entries contained in the selection.
		 */
		private List<FactoryNode> getCopiedResourcesSelection(
				TreeItem[] selectedItems) {
			List<FactoryNode> entries = new LinkedList<FactoryNode>();
			for (TreeItem item : selectedItems) {
				if (item.getData() instanceof Resource) {
					entries.add(copyResource((Resource) item.getData()));
				}
			}
			return entries;
		}
	}
}
