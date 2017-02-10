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

package masterviews.composite.abstracts;


import java.util.ArrayList;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;

import eu.skillpro.ams.pscm.gui.masterviews.Activator;

public abstract class TreeTableComposite extends Composite {
	protected TreeViewer viewer;
	protected TreeColumn firstColumn;
	protected Menu headerMenu;
	
	public TreeTableComposite(Composite parent, int style) {
		this(parent,style,true);
	}
	
	public TreeTableComposite(Composite parent, int style, boolean visibleHeaders) {
		super(parent, SWT.NONE);

		GridLayoutFactory singleLayoutFactory = GridLayoutFactory.fillDefaults().extendedMargins(1, 1, 1, 1).spacing(1, 1).numColumns(1);
		GridLayoutFactory doubleLayoutFactory = GridLayoutFactory.fillDefaults().extendedMargins(1, 1, 1, 1).spacing(1, 1).numColumns(2);
		GridLayoutFactory trippleLayoutFactory = GridLayoutFactory.fillDefaults().extendedMargins(1, 1, 1, 1).spacing(1, 1).numColumns(3);
		
		this.setLayout(trippleLayoutFactory.create());
		
		createHeaderMenu();
		
		Composite leftToolbar = new Composite(this, SWT.NONE);
		leftToolbar.setLayoutData(GridDataFactory.swtDefaults()
				.align(SWT.BEGINNING, SWT.FILL).hint(25, SWT.DEFAULT)
				.grab(false, false).create());
		leftToolbar.setLayout(singleLayoutFactory.create());
		ToolBar coolToolBar = new ToolBar(leftToolbar, SWT.VERTICAL);

		// the default cool bar items.
		createToolItem(coolToolBar, SWT.VERTICAL, "R", Activator.getImageDescriptor("icons/refresh.png").createImage(), "Refreshes the Viewer", refreshSelectionListener());
		createToolItem(coolToolBar, SWT.VERTICAL, "+", Activator
				.getImageDescriptor("icons/expandall.png").createImage(),
				"Expands the branches in viewer", expandSelectionListener());
		createToolItem(coolToolBar, SWT.VERTICAL, "-", Activator
				.getImageDescriptor("icons/collapseall.png").createImage(),
				"Collapses the branches in viewer", collapseSelectionListener());
		addCoolbarItems(leftToolbar);
		
		Label separator = new Label(this, SWT.SEPARATOR|SWT.VERTICAL);
		separator.setLayoutData(GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.FILL).grab(false, true).create());
		
		Composite mainView = new Composite(this, SWT.NONE);
		mainView.setLayout(doubleLayoutFactory.create());
		mainView.setLayoutData(GridDataFactory.swtDefaults()
				.align(SWT.FILL, SWT.FILL).grab(true, true).create());
		createTreeViewer(mainView, style);
		viewer.getTree().setHeaderVisible(visibleHeaders);
	}
	
	// add buttons
	protected void addCoolbarItems(Composite parent) {
		
	}

	/**
	 * Creates Menu and Button that manages the visibility of the table columns
	 * 
	 * @return the HeaderMenu
	 */
	private void createHeaderMenu() {
		headerMenu = new Menu(this);
	}

	protected SelectionListener refreshSelectionListener() {
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

	/**
	 * Create the tree viewer for this composite
	 */
	protected void createTreeViewer(Composite parent, int style) {
		if (style == SWT.SINGLE) {
			viewer = initializeSingleFilteredTreeViewer(parent);
		} else {
			viewer = initializeFilteredTreeViewer(parent);
		}
		viewer.setContentProvider(initContentProvider());
		viewer.setLabelProvider(initLabelProvider());
		viewer.setAutoExpandLevel(Integer.MAX_VALUE);
		//set default toString() comparator, override this if needed
		viewer.setComparator(new ViewerComparator());
		
		final Tree tree = viewer.getTree();
		tree.setLayoutData(GridDataFactory.fillDefaults().span(2, 1)
				.grab(true, true).create());
		
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);
		initColumn(tree);
		addColumn(tree);
		
	}
	
	protected void initColumn(Tree tree) {
		firstColumn = new TreeColumn(tree, SWT.LEFT, 0);
		firstColumn.setAlignment(SWT.LEFT);
		firstColumn.setText("Name");
		firstColumn.setWidth(250);
	}
	
	
	protected abstract void addColumn(Tree tree);

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

	// initializes the input that will be used by the tree viewer.
	@SuppressWarnings("unused")
	private void initInput() {
		viewer.setInput(new ArrayList<>());
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



	/**
	 * Returns the very first label provider that will be used by this
	 * {@link TableComposite}.
	 * 
	 * @return the very first label provider that will be used by this
	 *         {@link TableComposite}.
	 */
	protected abstract LabelProvider initLabelProvider();

	/**
	 * Returns the very first content provider that will be used by this
	 * {@link TableComposite}
	 * 
	 * @return the very first content provider that will be used by this
	 *         {@link TableComposite}
	 */
	protected abstract IContentProvider initContentProvider();
	
	public void disposeAllItems() {
		for (TreeItem child : getTreeViewer().getTree().getItems()) {
			child.dispose();
		}
	}
}
