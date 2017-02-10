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
import java.util.Collection;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

public abstract class TableComposite extends Composite {
	protected TableViewer viewer;

	/**
	 * Constructs a new {@link TableComposite}.
	 * 
	 * @param parent
	 *            this {@link TableComposite} will be draw based on this
	 *            {@link Composite}
	 * @param style
	 *            not used.
	 */
	public TableComposite(Composite parent, int style) {
		super(parent, SWT.NONE);
		if (style == SWT.MULTI) {
			GridLayoutFactory singleLayoutFactory = GridLayoutFactory.fillDefaults().extendedMargins(1, 1, 1, 1).spacing(1, 1).numColumns(1);
			GridLayoutFactory tripleLayoutFactory = GridLayoutFactory.fillDefaults().extendedMargins(1, 1, 1, 1).spacing(1, 1).numColumns(3);
			
			this.setLayout(tripleLayoutFactory.create());
			Composite leftToolbar = new Composite(this, SWT.NONE);
			leftToolbar.setLayoutData(GridDataFactory.swtDefaults()
					.align(SWT.CENTER, SWT.FILL).hint(SWT.DEFAULT, SWT.DEFAULT)
					.grab(false, false).create());
			leftToolbar.setLayout(singleLayoutFactory.create());
			
			addCoolbarItems(leftToolbar);
			
			Label separator = new Label(this, SWT.SEPARATOR|SWT.VERTICAL);
			separator.setLayoutData(GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.FILL).grab(false, true).create());
					
			Composite mainView = new Composite(this, SWT.NONE);
			mainView.setLayout(singleLayoutFactory.create());
			mainView.setLayoutData(GridDataFactory.swtDefaults()
					.align(SWT.FILL, SWT.FILL).grab(true, true).create());
			mainView.setLayoutData(GridDataFactory.swtDefaults()
					.align(SWT.FILL, SWT.FILL).grab(true, true).create());
			createTableViewer(mainView);
		} else {
			GridLayoutFactory layoutFactory = GridLayoutFactory.fillDefaults().extendedMargins(1, 1, 1, 1).spacing(1, 1);
			this.setLayout(layoutFactory.create());
			this.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
			Composite mainView = new Composite(this, SWT.NONE);
			mainView.setLayout(layoutFactory.create());
			mainView.setLayoutData(GridDataFactory.swtDefaults()
					.align(SWT.FILL, SWT.FILL).grab(true, true).create());
			createTableViewer(mainView);
		}
	}
	
	protected void addCoolbarItems(Composite parent) {
		
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
	 * Create the Table viewer for this composite
	 */
	public void createTableViewer(Composite parent) {
		viewer = initializeTableViewer(parent);
		viewer.setContentProvider(initContentProvider());
		viewer.setLabelProvider(initLabelProvider());
		initInput();
		viewer.getTable().setLayoutData(GridDataFactory.fillDefaults().span(2, 1)
				.grab(true, true).create());
		//set default toString() comparator, override this if needed
		viewer.setComparator(new ViewerComparator());
	}

	// initializes the filtered Table viewer.
	private TableViewer initializeTableViewer(Composite parent) {
		TableViewer tableViewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.FULL_SELECTION);
		createColumns(tableViewer);
		return tableViewer;
	}
	
	/**
	 * Initialize the columns of the table viewer.
	 * @param viewer the table viewer.
	 */
	protected abstract void createColumns(TableViewer viewer);

	// initializes the input that will be used by the Table viewer.
	//should be changed when needed.
	protected void initInput() {
		viewer.setInput(new ArrayList<>());
	}


	/**
	 * Sets the input for the Table viewer.
	 * 
	 * @param object
	 *            is a list of objects that will be used as the input for the
	 *            Table viewer.
	 */
	public void setInput(Collection<?> object) {
		viewer.setInput(object);
	}
	
	public void refreshViewer() {
		viewer.refresh(true);
	}
	
	public TableViewer getViewer() {
		return viewer;
	}
	
	/**
	 * Returns the very first label provider that will be used by this
	 * {@link TableComposite}.
	 * 
	 * @return the very first label provider that will be used by this
	 *         {@link TableComposite}.
	 */
	protected abstract ITableLabelProvider initLabelProvider();

	/**
	 * Returns the very first content provider that will be used by this
	 * {@link TableComposite}
	 * 
	 * @return the very first content provider that will be used by this
	 *         {@link TableComposite}
	 */
	protected abstract IContentProvider initContentProvider();

	public void disposeAllItems() {
		for (TableItem child : getViewer().getTable().getItems()) {
			child.dispose();
		}
	}
}
