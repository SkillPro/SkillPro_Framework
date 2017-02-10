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

package skillpro.vc.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.dialogs.SelectionDialog;

import skillpro.dialogs.CreateAssetDialog;
import skillpro.dialogs.EditAssetDialog;
import skillpro.model.assets.Factory;
import skillpro.model.assets.FactoryNode;
import skillpro.model.assets.Resource;
import skillpro.model.assets.Setup;
import skillpro.model.service.SkillproService;
import skillpro.model.update.UpdateType;
import skillpro.model.utils.Pair;
import skillpro.vc.client.gen.datacontract.Asset;
import skillpro.vc.ui.providers.MappingContentProvider;
import skillpro.vc.ui.providers.MappingLabelProvider;
import eu.skillpro.ams.pscm.vc.client.Activator;

public class VISMappingDialog extends SelectionDialog {
	private TreeViewer viewer;

	public VISMappingDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		container.setLayout(GridLayoutFactory.fillDefaults().margins(5,5).equalWidth(false).numColumns(1).create());

		createMappingTableComposite(container);
		viewer.setInput(refreshInput());
		return area;
	}

	private void createMappingTableComposite(Composite container) {
		Composite tableComposite = new Composite(container, SWT.BORDER);
		tableComposite.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).create());
		tableComposite.setLayoutData(GridDataFactory.swtDefaults()
				.align(SWT.FILL, SWT.FILL).grab(true, true).create());
		Composite leftToolbar = new Composite(tableComposite, SWT.NONE);
		leftToolbar.setLayoutData(GridDataFactory.swtDefaults()
				.align(SWT.BEGINNING, SWT.FILL).hint(25, SWT.DEFAULT)
				.grab(false, false).create());
		leftToolbar.setLayout(GridLayoutFactory.fillDefaults().margins(1, 1)
				.create());
		ToolBar coolToolBar = new ToolBar(leftToolbar, SWT.VERTICAL);

		// the default cool bar items.
		createToolItem(coolToolBar, SWT.VERTICAL, "R", Activator
				.getImageDescriptor("icons/refresh.png").createImage(),
				"Refreshes the Viewer", refreshSelectionListener());
		createToolItem(coolToolBar, SWT.VERTICAL, "+", Activator
				.getImageDescriptor("icons/expandall.png").createImage(),
				"Expands the branches in viewer", expandSelectionListener());
		createToolItem(coolToolBar, SWT.VERTICAL, "-", Activator
				.getImageDescriptor("icons/collapseall.png").createImage(),
				"Collapses the branches in viewer", collapseSelectionListener());
		//		addCoolbarItems(leftToolbar);
		Composite mainView = new Composite(tableComposite, SWT.NONE);
		mainView.setLayout(GridLayoutFactory.fillDefaults().margins(1, 0)
				.numColumns(2).create());
		mainView.setLayoutData(GridDataFactory.swtDefaults()
				.align(SWT.FILL, SWT.FILL).grab(true, true).create());
		createTreeViewer(mainView, SWT.MULTI);
		viewer.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
				Pair<?, ?> pair = (Pair<?, ?>) ((StructuredSelection) viewer.getSelection()).getFirstElement();
				FactoryNode selectedFactoryNode = (FactoryNode) pair.getFirstElement();
				Asset selectedAsset = (Asset) pair.getSecondElement();
				Shell shell = Display.getCurrent().getActiveShell();
				if (selectedFactoryNode != null) {
					EditAssetDialog editAssetDialog  = new EditAssetDialog(shell, ((FactoryNode) selectedFactoryNode));
					editAssetDialog.open();
					if (editAssetDialog.getReturnCode() == Dialog.OK) {
						((FactoryNode) selectedFactoryNode).setName((String) editAssetDialog.getResult()[0]);
						((FactoryNode) selectedFactoryNode).setLayoutable(!editAssetDialog.isLogical());
						if(editAssetDialog.isLogical()) {
							((FactoryNode) selectedFactoryNode).setWidth(-1);
							((FactoryNode) selectedFactoryNode).setLength(-1);
							((FactoryNode) selectedFactoryNode).setHeight(-1);
						} else {
							if(((FactoryNode) selectedFactoryNode).getWidth()==-1)
								((FactoryNode) selectedFactoryNode).setWidth(100);
							if(((FactoryNode) selectedFactoryNode).getHeight()==-1)
								((FactoryNode) selectedFactoryNode).setHeight(100);
							if(((FactoryNode) selectedFactoryNode).getLength()==-1)
								((FactoryNode) selectedFactoryNode).setLength(100);
						}
						SkillproService.getSkillproProvider();
						SkillproService.getUpdateManager().notify(UpdateType.ASSET_UPDATED, FactoryNode.class);
					}
					viewer.setInput(refreshInput());
					viewer.refresh();
				} else if (selectedAsset != null && selectedFactoryNode == null) {
					CreateAssetDialog createAssetDialog =  new CreateAssetDialog(shell, selectedAsset.getName(), false);
					createAssetDialog.open();
					if (createAssetDialog.getReturnCode() == Dialog.OK) {
						createAsset(createAssetDialog);
						viewer.setInput(refreshInput());
					}
				}
			}
		});
	}
	
	protected List<?> refreshInput() {
		return new ArrayList<>();
	}
	
	private void createAsset(CreateAssetDialog dialog) {
		FactoryNode node = null;
		if (dialog.isFactory()) {
			node = new Factory(dialog.getResult()[0]);
		} else if (dialog.isFactoryNode()) {
			node = new FactoryNode(dialog.getResult()[0],!dialog.isLogical());
		} else if (dialog.isResource()) {
			node = new Resource(dialog.getResult()[0], new ArrayList<Setup>(), null);
		}
		SkillproService.getSkillproProvider().getAssetRepo().add(node);
	}

	/**
	 * Creates Menu and Button that manages the visibility of the table columns
	 * 
	 * @return the HeaderMenu
	 */
	private SelectionListener refreshSelectionListener() {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				viewer.refresh();
			}
		};
	}



	protected ToolItem createToolItem(ToolBar bar, int style, String text,
			Image image, String tooltip, SelectionListener listener) {
		if (image != null && (text == null && tooltip == null)) {
			throw new IllegalArgumentException(
					"image only items require a tool tip");
		}
		ToolItem ti = new ToolItem(bar, style);
		if (image != null) {
			ti.setImage(image);
		} else {
			if (text != null) {
				ti.setText(text);
			}
		}
		if (tooltip != null) {
			ti.setToolTipText(tooltip);
		}
		if (listener != null) {
			ti.addSelectionListener(listener);
		}
		return ti;
	}

	/**
	 * Returns the tree viewer.
	 * 
	 * @return the tree viewer.
	 */
	public TreeViewer getTreeViewer() {
		return viewer;
	}

	public IStructuredSelection getViewerSelection() {
		return (IStructuredSelection) getTreeViewer().getSelection();
	}

	/**
	 * Create the tree viewer for this composite
	 */
	private void createTreeViewer(Composite parent, int style) {
		if (style == SWT.SINGLE) {
			viewer = initializeSingleFilteredTreeViewer(parent);
		} else {
			viewer = initializeFilteredTreeViewer(parent);
		}
		viewer.setContentProvider(initContentProvider());
		viewer.setLabelProvider(initLabelProvider());
		viewer.setAutoExpandLevel(Integer.MAX_VALUE);
		final Tree tree = viewer.getTree();
		tree.setLayoutData(GridDataFactory.fillDefaults().span(2, 1)
				.grab(true, true).create());

		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);
		TreeColumn firstColumn = new TreeColumn(tree, SWT.LEFT, 0);
		firstColumn.setAlignment(SWT.LEFT);
		firstColumn.setText("Name");
		firstColumn.setWidth(200);
		addVISObjectColumn(tree);

	}

	private void addVISObjectColumn(final Tree tree) {

		TreeColumn column = new TreeColumn(tree, SWT.RIGHT | SWT.CHECK, 1);
		column.setAlignment(SWT.LEFT);
		column.setText("VIS Object");
		column.setWidth(150);
		TreeViewerColumn treeViewerColumn = new TreeViewerColumn(viewer, column);

		treeViewerColumn.setLabelProvider(new VISColumnProvider());
	}

	// initializes the filtered tree viewer.
	private TreeViewer initializeFilteredTreeViewer(Composite parent) {
		PatternFilter filter = new PatternFilter();
		FilteredTree tree = new FilteredTree(parent, SWT.FULL_SELECTION | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER, filter, true);
		return tree.getViewer();
	}


	private TreeViewer initializeSingleFilteredTreeViewer(Composite parent) {
		PatternFilter filter = new PatternFilter();
		FilteredTree tree = new FilteredTree(parent, SWT.FULL_SELECTION | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER, filter, true);
		return tree.getViewer();
	}

	/* SELECTION LISTENERS */
	private SelectionListener expandSelectionListener() {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				viewer.expandAll();
			}
		};
	}

	private SelectionListener collapseSelectionListener() {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				viewer.collapseAll();

			}
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
		//TODO
		return new MappingLabelProvider();
	}

	protected IContentProvider initContentProvider() {
		//TODO
		return new MappingContentProvider();
	}


	private class VISColumnProvider extends ColumnLabelProvider {


		@Override
		public String getText(Object element) {

			if (element instanceof Pair<?, ?>) {
				String text = "";
				Asset secondElement = (Asset) ((Pair<?, ?>) element).getSecondElement();
				if (secondElement != null) {
					text = secondElement.getName();
				} else {

				}
				return text;

			} else {
				return "What pair is this";
			}
		}

		@Override
		public Image getImage(Object element) {
			return null;
		}

		@Override
		public Color getBackground(Object element) {
			Pair<?, ?> pair = (Pair<?, ?>) element;
			if (pair.getFirstElement() == null || pair.getSecondElement() == null) {
				return Display.getDefault().getSystemColor(SWT.COLOR_YELLOW);
			}
			return super.getBackground(element);
		}
	}


}
