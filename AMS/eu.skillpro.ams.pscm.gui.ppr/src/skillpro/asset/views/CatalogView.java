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

package skillpro.asset.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.part.ViewPart;

import skillpro.model.assets.FactoryNode;
import skillpro.model.assets.Resource;
import skillpro.model.assets.Setup;
import skillpro.model.service.SkillproService;
import skillpro.model.skills.ResourceSkill;
import skillpro.model.update.Updatable;
import skillpro.model.update.UpdateType;
import skillpro.transformator.actions.SkillproReverseTransformAction;
import skillpro.view.impl.CatalogTreeComposite;
import eu.skillpro.ams.pscm.icons.IconActivator;

public class CatalogView extends ViewPart implements Updatable {
	public static final String ID = CatalogView.class.getName();
	
	private CatalogTreeComposite catalogTreeComposite;
	
	@Override
	public void createPartControl(Composite parent) {
		createViewer(parent);
		getSite().setSelectionProvider(catalogTreeComposite.getTreeViewer());
		SkillproService.getUpdateManager().registerUpdatable(this, FactoryNode.class);
	}
	
	private void createViewer(Composite parent) {
		catalogTreeComposite =  new CatalogTreeComposite(parent, SWT.NULL) {
			@Override
			protected void addCoolbarItems(Composite parent) {
				ToolBar coolToolBar = new ToolBar(parent, SWT.VERTICAL);
				createToolItem(coolToolBar, SWT.VERTICAL, "D*", IconActivator.getImageDescriptor("icons/asset/remove.png").createImage(), "Delete selected element", deleteSelectionListener());
			}
			
			@Override
			protected List<Action> createExtraContextActions(Object selection) {
				List<Action> extraActions = new ArrayList<>();
				List<Resource> resources = new ArrayList<>();
				if (selection instanceof Resource) {
					resources.add((Resource) selection);
					extraActions.add(createReverseTransformResourcesAction(resources));
					
				} 
				
				return extraActions;
			}
			
		};
		catalogTreeComposite.setLayoutData(GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).create());
		catalogTreeComposite.setInput(SkillproService.getSkillproProvider().getCatalogRepo().getEntities());
	}
	
	private Action createReverseTransformResourcesAction(final List<Resource> resources) {
		return new Action() {
			@Override
			public String getText() {
				return "Reverse transform resource(s)";
			}
			
			@Override
			public void run() {
				if (!resources.isEmpty()) {
					SkillproReverseTransformAction reverseTransformer = new SkillproReverseTransformAction();
					reverseTransformer.reverseTransformResources(resources);
				}
			}
		};
	}
	
	private SelectionListener deleteSelectionListener() {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				//get selected
				Object selectedElement = catalogTreeComposite.getViewerSelection().getFirstElement();
				if (selectedElement != null) {
					//is empty?
					if (selectedElement instanceof Resource) {
						Resource resource = (Resource) selectedElement;
						if (!resource.getSubNodes().isEmpty()) {
							MessageDialog.openWarning(getViewSite().getShell(), "Warning", "Objects with children can not be deleted! Remove children first!"); 
							return;
						}
						boolean confirm = MessageDialog.openConfirm(getViewSite().getShell(), "Confirm", "Are you sure you want to delete \"" + resource.getName() + "\"?");
						if (confirm) {
							List<Setup> setups = ((Resource) resource).getSetups();
							for (Setup conf : setups) {
								for (ResourceSkill rs : conf.getResourceSkills()) {
									SkillproService.getSkillproProvider().removeSkill(rs);
								}
								SkillproService.getSkillproProvider().getSetupRepo().remove(conf);
							}
							SkillproService.getSkillproProvider().getCatalogRepo().remove(resource);
							SkillproService.getUpdateManager().notify(UpdateType.ASSET_DELETED, null);
						}
					} else {
						MessageDialog.openError(getViewSite().getShell(), "Error!", "Selected element cannot be deleted.");
					}

				}
			}
		};
	}
	
	@Override
	public void setFocus() {
	}

	@Override
	public void update(UpdateType type) {
		switch (type) {
		case NEW_DATA_IMPORTED:
			catalogTreeComposite.getTreeViewer().refresh();
			break;
		case CATALOG_UPDATED:
			catalogTreeComposite.getTreeViewer().refresh();
			break;
		case ASSET_CREATED:
			catalogTreeComposite.getTreeViewer().refresh();
			break;
		case ASSET_UPDATED:
			catalogTreeComposite.getTreeViewer().refresh();
			break;
		case ASSET_DELETED:
			catalogTreeComposite.getTreeViewer().refresh();
			break;
		default:
			break;
		}
	}
}
