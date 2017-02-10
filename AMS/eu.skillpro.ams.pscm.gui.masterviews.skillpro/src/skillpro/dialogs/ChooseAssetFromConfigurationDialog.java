/**
 * 
 */
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

package skillpro.dialogs;

import masterviews.composite.abstracts.TreeComposite;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.SelectionDialog;

import skillpro.model.assets.Resource;
import skillpro.model.service.SkillproService;
import skillpro.providers.asset.AssetOnlyTreeContentProvider;
import skillpro.providers.asset.AssetTreeLabelProvider;

/**
 * @author aleksa
 * 
 * @version: 04.10.2014
 * 
 */
public class ChooseAssetFromConfigurationDialog extends SelectionDialog {
	private static final String DIALOG_TITLE = "Choose a Resource";
	private TreeViewer treeViewer;
	private Resource selectedResource;

	public ChooseAssetFromConfigurationDialog(Shell parentShell) {
		super(parentShell);
		setTitle(DIALOG_TITLE);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		area.setLayoutData(GridDataFactory.fillDefaults().hint(500, 500).create());
		Composite container = new Composite(area, SWT.BORDER);
		container.setLayoutData(GridDataFactory.fillDefaults().hint(480, 480).grab(true, true).align(SWT.FILL, SWT.FILL).create());
		container.setLayout(GridLayoutFactory.fillDefaults().equalWidth(false).numColumns(1).create());

		TreeComposite assetComposite = new TreeComposite(container, SWT.SINGLE) {

			@Override
			protected LabelProvider initLabelProvider() {
				return new AssetTreeLabelProvider();
			}

			@Override
			protected IContentProvider initContentProvider() {
				return new AssetOnlyTreeContentProvider();
			}
			
		};
		assetComposite.setLayoutData(GridDataFactory.fillDefaults().hint(460, 460).create());
		assetComposite.setInput(SkillproService.getSkillproProvider().getAssetRepo().getAllAssignedResources());
		
		treeViewer = assetComposite.getTreeViewer();
		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				selectedResource = (Resource) ((IStructuredSelection) event.getSelection()).getFirstElement();
			}
		});
		treeViewer.addDoubleClickListener(new IDoubleClickListener() {
			
			@Override
			public void doubleClick(DoubleClickEvent event) {
				if (selectedResource != null) {
					okPressed();
				}
			}
		});
		
		return area;
	}
	
	@Override
	public Resource[] getResult() {
		return new Resource[] { selectedResource };
	}
}
