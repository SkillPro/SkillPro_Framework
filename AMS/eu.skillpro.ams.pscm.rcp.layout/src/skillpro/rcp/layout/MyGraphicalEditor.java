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

package skillpro.rcp.layout;

import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.Viewport;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.parts.ScrollableThumbnail;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.KeyHandler;
import org.eclipse.gef.KeyStroke;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.MouseWheelHandler;
import org.eclipse.gef.MouseWheelZoomHandler;
import org.eclipse.gef.SnapToGrid;
import org.eclipse.gef.editparts.ScalableRootEditPart;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.gef.ui.actions.ZoomInAction;
import org.eclipse.gef.ui.actions.ZoomOutAction;
import org.eclipse.gef.ui.parts.ContentOutlinePage;
import org.eclipse.gef.ui.parts.GraphicalEditor;
import org.eclipse.gef.ui.parts.TreeViewer;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import skillpro.asset.views.AssetTreeView;
import skillpro.model.assets.FactoryNode;
import skillpro.model.assets.Resource;
import skillpro.rcp.layout.actions.CopyNodeAction;
import skillpro.rcp.layout.actions.PasteNodeAction;
import skillpro.rcp.layout.actions.Turn90DegreeAction;
import skillpro.rcp.layout.model.EditorNode;
import skillpro.rcp.layout.model.GEFNode;
import skillpro.rcp.layout.model.GEFWorkingPlace;
import skillpro.rcp.layout.model.Room;
import skillpro.rcp.layout.part.AppEditPartFactory;
import skillpro.rcp.layout.part.tree.AppTreeEditPartFactory;

//GraphicalEditorWithPalette
public class MyGraphicalEditor extends GraphicalEditor {
	public static final String ID = MyGraphicalEditor.class.getName();

	private EditorNode model;
	private KeyHandler keyHandler;

	protected class OutlinePage extends ContentOutlinePage {
		private SashForm sash;

		private ScrollableThumbnail thumbnail;
		private DisposeListener disposeListener;

		public OutlinePage() {
			super(new TreeViewer());
		}

		@Override
		public void createControl(Composite parent) {
			sash = new SashForm(parent, SWT.VERTICAL);

			getViewer().createControl(sash);

			getViewer().setEditDomain(getEditDomain());
			getViewer().setEditPartFactory(new AppTreeEditPartFactory());
			getViewer().setContents(model);

			getSelectionSynchronizer().addViewer(getViewer());

			IActionBars bars = getSite().getActionBars();
			ActionRegistry ar = getActionRegistry();
			bars.setGlobalActionHandler(ActionFactory.UNDO.getId(),
					ar.getAction(ActionFactory.UNDO.getId()));
			bars.setGlobalActionHandler(ActionFactory.REDO.getId(),
					ar.getAction(ActionFactory.REDO.getId()));
			bars.setGlobalActionHandler(ActionFactory.DELETE.getId(),
					ar.getAction(ActionFactory.DELETE.getId()));
			bars.setGlobalActionHandler(ActionFactory.COPY.getId(),
					ar.getAction(ActionFactory.COPY.getId()));
			bars.setGlobalActionHandler(ActionFactory.PASTE.getId(),
					ar.getAction(ActionFactory.PASTE.getId()));

			Canvas canvas = new Canvas(sash, SWT.BORDER);
			LightweightSystem lws = new LightweightSystem(canvas);

			thumbnail = new ScrollableThumbnail(
					(Viewport) ((ScalableRootEditPart) getGraphicalViewer()
							.getRootEditPart()).getFigure());
			thumbnail.setSource(((ScalableRootEditPart) getGraphicalViewer()
					.getRootEditPart())
					.getLayer(LayerConstants.PRINTABLE_LAYERS));

			lws.setContents(thumbnail);

			disposeListener = new DisposeListener() {
				@Override
				public void widgetDisposed(DisposeEvent e) {
					if (thumbnail != null) {
						thumbnail.deactivate();
						thumbnail = null;
					}
				}
			};
			getGraphicalViewer().getControl().addDisposeListener(
					disposeListener);
		}

		@Override
		public Control getControl() {
			return sash;
		}

		@Override
		public void dispose() {
			getSelectionSynchronizer().removeViewer(getViewer());
			if (getGraphicalViewer().getControl() != null
					&& !getGraphicalViewer().getControl().isDisposed())
				getGraphicalViewer().getControl().removeDisposeListener(
						disposeListener);

			super.dispose();
		}
	}

	public MyGraphicalEditor() {
		setEditDomain(new DefaultEditDomain(this));
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	public EditorNode createEditor() {
		EditorNode editor = new EditorNode();
		editor.setName("Factory layout");
		return editor;
	}

	@Override
	protected void initializeGraphicalViewer() {
		GraphicalViewer viewer = getGraphicalViewer();
		model = createEditor();
		viewer.setContents(model);
		getSite()
				.getWorkbenchWindow()
				.getSelectionService()
				.addSelectionListener("skillpro.asset.views.AssetTreeView",
						this);
	}

	@Override
	protected void configureGraphicalViewer() {
		double[] zoomLevels;

		super.configureGraphicalViewer();
		GraphicalViewer viewer = getGraphicalViewer();
		viewer.setEditPartFactory(new AppEditPartFactory());

		ScalableRootEditPart rootEditPart = new ScalableRootEditPart();
		viewer.setRootEditPart(rootEditPart);

		ZoomManager manager = rootEditPart.getZoomManager();
		getActionRegistry().registerAction(new ZoomInAction(manager));
		getActionRegistry().registerAction(new ZoomOutAction(manager));

		zoomLevels = new double[] { 0.25, 0.5, 0.75, 1.0, 1.5, 2.0, 2.5, 3.0,
				4.0, 5.0, 10.0, 20.0 };
		manager.setZoomLevels(zoomLevels);
		ArrayList<String> zoomContributions = new ArrayList<String>();
		zoomContributions.add(ZoomManager.FIT_ALL);
		zoomContributions.add(ZoomManager.FIT_HEIGHT);
		zoomContributions.add(ZoomManager.FIT_WIDTH);
		manager.setZoomLevelContributions(zoomContributions);

		keyHandler = new KeyHandler();

		keyHandler.put(KeyStroke.getPressed(SWT.DEL, 127, 0),
				getActionRegistry().getAction(ActionFactory.DELETE.getId()));

		keyHandler.put(KeyStroke.getPressed('+', SWT.KEYPAD_ADD, 0),
				getActionRegistry().getAction(GEFActionConstants.ZOOM_IN));

		keyHandler.put(KeyStroke.getPressed('-', SWT.KEYPAD_SUBTRACT, 0),
				getActionRegistry().getAction(GEFActionConstants.ZOOM_OUT));

		viewer.setProperty(MouseWheelHandler.KeyGenerator.getKey(SWT.NONE),
				MouseWheelZoomHandler.SINGLETON);

		viewer.setKeyHandler(keyHandler);

		ContextMenuProvider provider = new AppContextMenuProvider(viewer,
				getActionRegistry());
		viewer.setContextMenu(provider);
		getGraphicalViewer()
				.setProperty(SnapToGrid.PROPERTY_GRID_ENABLED, true);

		getGraphicalViewer()
				.setProperty(SnapToGrid.PROPERTY_GRID_VISIBLE, true);
		getGraphicalViewer().setProperty(SnapToGrid.PROPERTY_GRID_SPACING,
				new Dimension(10, 10));
	}

	@Override
	@SuppressWarnings("unchecked")
	public void createActions() {
		super.createActions();

		ActionRegistry registry = getActionRegistry();
		IAction action = new CopyNodeAction(this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new PasteNodeAction(this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new Turn90DegreeAction(this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());
	}

	public EditorNode getModel() {
		return model;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class type) {
		if (type == ZoomManager.class)
			return ((ScalableRootEditPart) getGraphicalViewer()
					.getRootEditPart()).getZoomManager();
		if (type == IContentOutlinePage.class) {
			return new OutlinePage();
		}
		return super.getAdapter(type);
	}

	private void createGEFNode(FactoryNode child, GEFNode parent) {
		if (child instanceof Resource) {
			GEFWorkingPlace wp = new GEFWorkingPlace((Resource) child);
			parent.addChild(wp);
		} else {
			GEFNode parentNode = parent;
			if (child.isLayoutable()) {
				Room room = new Room(child);
				parent.addChild(room);
				parentNode = room;
			}
			for (FactoryNode childNode : child.getSubNodes()) {
				createGEFNode(childNode, parentNode);
			}
		}
	}

	public void showNodes(FactoryNode root) {
		createGEFNode(root, model);
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (part instanceof AssetTreeView) {
			ITreeSelection treeSelection = (ITreeSelection) selection;
			if (treeSelection.getFirstElement() instanceof FactoryNode) {

				FactoryNode factoryNode = (FactoryNode) treeSelection
						.getFirstElement();
				FactoryNode root = factoryNode;
				while ((root.getParent() != null)
						&& !(root.isLayoutable() && !(root instanceof Resource))) {
					root = root.getParent();
				}
				model.removeAllChildren();
				showNodes(root);
			}
		}
	}
}
