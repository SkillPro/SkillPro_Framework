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
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import masterviews.dialogs.EditAllNameSelectionDialog;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.part.ViewPart;

import skillpro.dialogs.CreateAssetDialog;
import skillpro.dialogs.EditAssetDialog;
import skillpro.dialogs.ManageResourceConfigurationTypesDialog;
import skillpro.dialogs.ManageResourceConfigurationsDialog;
import skillpro.dialogs.ProductConfigurationDialog;
import skillpro.dialogs.TemplateSkillSelectionDialog;
import skillpro.model.assets.Factory;
import skillpro.model.assets.FactoryNode;
import skillpro.model.assets.Resource;
import skillpro.model.assets.ResourceConfiguration;
import skillpro.model.assets.ResourceConfigurationType;
import skillpro.model.assets.Setup;
import skillpro.model.assets.Tool;
import skillpro.model.products.ProductConfiguration;
import skillpro.model.service.SkillproService;
import skillpro.model.skills.PrePostRequirement;
import skillpro.model.skills.RequirementSkillType;
import skillpro.model.skills.ResourceSkill;
import skillpro.model.skills.Skill;
import skillpro.model.skills.TemplateSkill;
import skillpro.model.update.Updatable;
import skillpro.model.update.UpdateType;
import skillpro.view.impl.AssetTreeComposite;
import eu.skillpro.ams.pscm.icons.IconActivator;

public class AssetTreeView extends ViewPart implements Updatable {
	public static final String ID = AssetTreeView.class.getName();

	private AssetTreeComposite assetTreeComposite;

	@Override
	public void createPartControl(Composite parent) {
		createViewer(parent);
		getSite().setSelectionProvider(assetTreeComposite.getTreeViewer());
		SkillproService.getUpdateManager().registerUpdatable(this, FactoryNode.class);
	}

	private void createViewer(Composite parent) {
		assetTreeComposite =  new AssetTreeComposite(parent, SWT.NULL) {
			@Override
			protected void addCoolbarItems(Composite parent) {
				ToolBar coolToolBar = new ToolBar(parent, SWT.VERTICAL);
				createToolItem(coolToolBar, SWT.VERTICAL, "A*",
						IconActivator.getImageDescriptor("icons/asset/add.png").createImage(),
						"Creates a new asset with the help of wizard", createAssetListener(this));

				createToolItem(coolToolBar, SWT.VERTICAL, "D*",
						IconActivator.getImageDescriptor("icons/asset/remove.png").createImage(),
						"Delete selected element", deleteSelectionListener());
			}
			
			@Override
			protected List<Action> createExtraContextActions(Object selection) {
				List<Action> extraActions = new ArrayList<>();
				if (selection instanceof Resource) {
					Resource selectedResource = (Resource) selection;
					extraActions.add(addNewResourceConfigurationSkillAction(selectedResource));
					extraActions.add(createManageResourceConfigurationTypesAction(selectedResource));
					extraActions.add(createManageResourceConfigurationsAction(selectedResource));
					extraActions.add(separatorAction());
					extraActions.add(setCurrentProductConfigurationAction(selectedResource));
				} else if (selection instanceof ResourceConfiguration) {
					ResourceConfiguration selectedResourceConfiguration = (ResourceConfiguration) selection;
					extraActions.add(setCurrentResourceConfigurationSkillAction(selectedResourceConfiguration));
					extraActions.add(deleteResourceConfigurationAction(selectedResourceConfiguration));
				} else if (selection instanceof ResourceSkill) {
					ResourceSkill selectedResourceSkill = (ResourceSkill) selection;
					extraActions.add(setTemplateSkillAction(selectedResourceSkill));
					extraActions.add(deleteResourceSkillAction(selectedResourceSkill));
				}
				return extraActions;
			}

		};
		assetTreeComposite.setLayoutData(GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).create());
		assetTreeComposite.setInput(SkillproService.getSkillproProvider().getAssetRepo());
		assetTreeComposite.getViewer().addDoubleClickListener(createAssetDoubleClickListener());
		assetTreeComposite.getViewer().getTree().addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.keyCode == SWT.DEL) {
					deleteViewerSelections();
				}
				
			}
		});
	}

	private IDoubleClickListener createAssetDoubleClickListener() {
		return new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
				Object selection = ((StructuredSelection) assetTreeComposite.getViewer().getSelection()).getFirstElement();
				Shell shell = Display.getCurrent().getActiveShell();
				if (selection instanceof FactoryNode) {
					FactoryNode selectedFactoryNode = (FactoryNode) selection;
					SkillproService.getSkillproProvider().removeFactoryNodeAndDependencies(selectedFactoryNode);
					EditAssetDialog editAssetDialog  = new EditAssetDialog(shell, selectedFactoryNode);
					editAssetDialog.open();
					if (editAssetDialog.getReturnCode() == Dialog.OK) {
						selectedFactoryNode.setName((String) editAssetDialog.getResult()[0]);
						selectedFactoryNode.setLayoutable(!editAssetDialog.isLogical());
						if (editAssetDialog.isLogical()) {
							selectedFactoryNode.setWidth(-1);
							selectedFactoryNode.setLength(-1);
							selectedFactoryNode.setHeight(-1);
						} else {
							if (selectedFactoryNode.getWidth() < 0)
								selectedFactoryNode.setWidth(100);
							if (selectedFactoryNode.getHeight() < 0)
								selectedFactoryNode.setHeight(100);
							if (selectedFactoryNode.getLength() < 0)
								selectedFactoryNode.setLength(100);
						}
						SkillproService.getUpdateManager().notify(UpdateType.ASSET_UPDATED, FactoryNode.class);
					}
					SkillproService.getSkillproProvider().addFactoryNodeAndDependencies(selectedFactoryNode);
				} else if (selection instanceof Tool) {
					Tool tool = (Tool) selection;
					EditAllNameSelectionDialog changeNameDialog = new EditAllNameSelectionDialog(shell,
							tool.getName());
					changeNameDialog.open();
					if (changeNameDialog.getReturnCode() == Dialog.OK) {
						tool.setName((String) changeNameDialog.getName());
						SkillproService.getUpdateManager().notify(UpdateType.TOOL_UPDATED, Tool.class);
					}
				} else if (selection instanceof Setup) {
					Setup setup = (Setup) selection;
					//cos the repo stored it in a set
					SkillproService.getSkillproProvider().getSetupRepo().remove(setup);
					EditAllNameSelectionDialog changeNameDialog = new EditAllNameSelectionDialog(shell,
							setup.getName());
					changeNameDialog.open();
					if (changeNameDialog.getReturnCode() == Dialog.OK) {
						setup.setName((String) changeNameDialog.getName());
						SkillproService.getUpdateManager().notify(UpdateType.ASSET_UPDATED, FactoryNode.class);
					}
					//re-adding
					SkillproService.getSkillproProvider().getSetupRepo().getEntities().add(setup);
				} else if (selection instanceof Skill) {
					Skill skill = (Skill) selection;
					//cos the repo stored it in a set
					SkillproService.getSkillproProvider().removeSkillAndDependencies(skill);
					EditAllNameSelectionDialog changeNameDialog = new EditAllNameSelectionDialog(shell, skill.getName());
					changeNameDialog.open();
					if (changeNameDialog.getReturnCode() == Dialog.OK) {
						skill.setName((String) changeNameDialog.getName());
						SkillproService.getUpdateManager().notify(UpdateType.SKILL_UPDATED, null);
						SkillproService.getUpdateManager().notify(UpdateType.ASSET_UPDATED, FactoryNode.class);
					}
					//re-adding
					SkillproService.getSkillproProvider().addSkillAndDependencies(skill);
				} else if (selection instanceof ResourceConfiguration) {
					ResourceConfiguration resourceConfiguration = (ResourceConfiguration) selection;
					EditAllNameSelectionDialog changeNameDialog = new EditAllNameSelectionDialog(shell, resourceConfiguration.getName());
					changeNameDialog.open();
					if (changeNameDialog.getReturnCode() == Dialog.OK) {
						resourceConfiguration.setName((String) changeNameDialog.getName());
						SkillproService.getUpdateManager().notify(UpdateType.SKILL_UPDATED, Skill.class);
						SkillproService.getUpdateManager().notify(UpdateType.ASSET_UPDATED, FactoryNode.class);
					}
				}
			}
		};
	}

	private SelectionListener createAssetListener(final AssetTreeComposite parent) {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				CreateAssetDialog dialog = new CreateAssetDialog(parent.getShell());
				if (dialog.open() == Dialog.OK) {
					createAsset(dialog);
					SkillproService.getUpdateManager().notify(UpdateType.ASSET_CREATED, FactoryNode.class);
				}
			}
		};
	}
	
	private Action createManageResourceConfigurationTypesAction(final Resource resource) {
		return new Action() {
			@Override
			public String getText() {
				return "Manage resource configuration types";
			}
			
			@Override
			public void run() {
				Shell shell = Display.getCurrent().getActiveShell();
				ManageResourceConfigurationTypesDialog dialog = new ManageResourceConfigurationTypesDialog(shell, resource);
				
				if (dialog.open() == Window.OK) {
					resource.getResourceConfigurationTypes().clear();
					for (Object obj : dialog.getResult()) {
						if (obj instanceof ResourceConfigurationType) {
							resource.addResourceConfigurationType((ResourceConfigurationType) obj);
						} else {
							throw new IllegalArgumentException("ManageResourceConfigurationDialog returned "
										+ "an unknown object: " + obj);
						}
					}
					SkillproService.getUpdateManager().notify(UpdateType.ASSET_UPDATED, FactoryNode.class);
				}
			}
		};
	}
	
	private Action createManageResourceConfigurationsAction(final Resource resource) {
		return new Action() {
			
			@Override
			public String getText() {
				return "Manage resource configurations";
			}
			
			@Override
			public void run() {
				Shell shell = Display.getCurrent().getActiveShell();
				ManageResourceConfigurationsDialog dialog = new ManageResourceConfigurationsDialog(shell, resource);
				
				if (dialog.open() == Window.OK) {
					resource.getResourceConfigurationTypes().clear();
					for (Object obj : dialog.getResult()) {
						if (obj instanceof ResourceConfiguration) {
							resource.addResourceConfiguration((ResourceConfiguration) obj);
						} else {
							throw new IllegalArgumentException("ManageResourceConfigurationDialog returned an unknown object: " + obj);
						}
					}
					SkillproService.getUpdateManager().notify(UpdateType.ASSET_UPDATED, FactoryNode.class);
				}
			}
		};
	}

	private Action setCurrentProductConfigurationAction(final Resource resource) {
		return new Action() {

			@Override
			public String getText() {
				return "Set current Product Configuration";
			}
			
			@Override
			public void run() {
				if (resource != null) {
					Shell shell = Display.getCurrent().getActiveShell();
					ProductConfigurationDialog pConfDialog = new ProductConfigurationDialog(shell);
					pConfDialog.setTitle("Write a name for the resource configuration");
					if (pConfDialog.open() == Dialog.OK) {
						ProductConfiguration pConf = new ProductConfiguration(UUID.randomUUID().toString(), pConfDialog.getInputs());
						if (pConf.getProductQuantities().isEmpty()) {
							resource.setCurrentProductConfiguration(null);
						} else {
							resource.setCurrentProductConfiguration(pConf);
						}
						SkillproService.getUpdateManager().notify(UpdateType.ASSET_UPDATED, FactoryNode.class);
					}
					
				}
			}
		};
	}
	
	private Action addNewResourceConfigurationSkillAction(final Resource resource) {
		return new Action() {

			@Override
			public String getText() {
				return "Add resource configuration";
			}
			
			@Override
			public void run() {
				if (resource != null) {
					Shell shell = Display.getCurrent().getActiveShell();
					EditAllNameSelectionDialog nameDialog = new EditAllNameSelectionDialog(shell);
					nameDialog.setTitle("Write a name for the resource configuration");
					if (nameDialog.open() == Dialog.OK) {
						ResourceConfiguration conf = new ResourceConfiguration(UUID.randomUUID().toString(),
								nameDialog.getName(), resource);
						resource.addResourceConfiguration(conf);
						if (resource.getCurrentResourceConfiguration() == null) {
							//should it be done this way?
							resource.setCurrentResourceConfiguration(conf);
						}
						SkillproService.getUpdateManager().notify(UpdateType.ASSET_UPDATED, FactoryNode.class);
					}
					
				}
			}
		};
	}

	
	private Action setCurrentResourceConfigurationSkillAction(final ResourceConfiguration resourceConfiguration) {
		return new Action() {

			@Override
			public String getText() {
				return "Set as current resource configuration";
			}
			
			@Override
			public void run() {
				if (resourceConfiguration != null) {
					resourceConfiguration.getResource().setCurrentResourceConfiguration(resourceConfiguration);
					SkillproService.getUpdateManager().notify(UpdateType.ASSET_UPDATED, FactoryNode.class);
				}
			}
		};
	}
	
	private Action deleteResourceConfigurationAction(final ResourceConfiguration resourceConfiguration) {
		return new Action() {

			@Override
			public String getText() {
				return "Delete Resource Configuration";
			}
			
			@Override
			public void run() {
				if (resourceConfiguration != null) {
					deleteResourceConfiguration(resourceConfiguration);
				}
			}
		};
	}
	
	private void deleteResourceConfiguration(ResourceConfiguration toDelete) {
		if (toDelete.getResource().getCurrentResourceConfiguration() != null 
				&& toDelete.getResource().getCurrentResourceConfiguration()
				.equals(toDelete)) {
			toDelete.getResource().setCurrentResourceConfiguration(null);
			for (ResourceConfiguration conf : toDelete.getResource().getResourceConfigurations()) {
				if (!conf.equals(toDelete)) {
					toDelete.getResource().setCurrentResourceConfiguration(conf);
					break;
				}
			}
		}
		toDelete.getResource().getResourceConfigurations().remove(toDelete);
		toDelete.setResource(null);
		SkillproService.getUpdateManager().notify(UpdateType.ASSET_UPDATED, FactoryNode.class);
	}
	
	private Action deleteResourceSkillAction(final ResourceSkill resourceSkill) {
		return new Action() {

			@Override
			public String getText() {
				return "Delete Resource Skill";
			}
			
			@Override
			public void run() {
				if (resourceSkill != null) {
					uprootResourceSkill(resourceSkill);
				}
			}
		};
	}
	
	private void uprootResourceSkill(ResourceSkill resourceSkill) {
		for (Setup setup : resourceSkill.getResource().getSetups()) {
			setup.getResourceSkills().remove(resourceSkill);
		}
		deleteResourceSkill(resourceSkill);
		SkillproService.getUpdateManager().notify(UpdateType.ASSET_UPDATED, FactoryNode.class);
	}
	
	private void deleteResourceSkill(ResourceSkill rSkill) {
		for (ResourceSkill otherRSkill : SkillproService.getSkillproProvider().getResourceSkillRepo()) {
			if (!otherRSkill.equals(rSkill)) {
				for (Iterator<PrePostRequirement> iter = otherRSkill
						.getPrePostRequirements().iterator(); iter.hasNext();) {
					PrePostRequirement req = iter.next();
					ResourceSkill requiredResourceSkill = req.getPreRequirement().getRequiredResourceSkill();
					if (requiredResourceSkill == null || req.getPostRequirement().getSkillType() != RequirementSkillType.SAME) {
						throw new IllegalArgumentException("Time to update this method");
					}
					if (requiredResourceSkill.equals(rSkill)) {
						iter.remove();
					}
				}
			}
		}
		rSkill.getPrePostRequirements().clear();
		SkillproService.getSkillproProvider().removeSkill(rSkill);
		SkillproService.getUpdateManager().notify(UpdateType.SKILL_UPDATED, null);
	}
	
	private Action setTemplateSkillAction(final ResourceSkill resourceSkill) {
		return new Action() {
			@Override
			public String getText() {
				return "Set Template Skill";
			}
			
			@Override
			public void run() {
				if (resourceSkill != null) {
					TemplateSkillSelectionDialog dialog = new TemplateSkillSelectionDialog(getViewSite().getShell(),
    						SkillproService.getSkillproProvider().getTemplateSkillRepo().getEntities());
    				
    				if (dialog.open() == Window.OK) {
    					TemplateSkill tSkill = (TemplateSkill) dialog.getResult()[0];
    					resourceSkill.setTemplateSkill(tSkill);
    				}
					SkillproService.getUpdateManager().notify(UpdateType.ASSET_UPDATED, FactoryNode.class);
					SkillproService.getUpdateManager().notify(UpdateType.SKILL_UPDATED, null);
				}
			}
		};
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

	private void deleteAsset(FactoryNode toDelete, boolean singleConfirm) {
		boolean confirm = singleConfirm ? MessageDialog.openConfirm(getViewSite().getShell(),
				"Confirm", "Are you sure you want to delete \"" + toDelete.getName() + "\"?") : true;
		if (confirm) {
			if (toDelete instanceof Resource) {
				//delete skills and configurations
				List<Setup> setups = ((Resource) toDelete).getSetups();
				for (Setup conf : setups) {
					for (ResourceSkill rSkill : conf.getResourceSkills()) {
						deleteResourceSkill(rSkill);
					}
					SkillproService.getSkillproProvider().getSetupRepo().remove(conf);
				}
			} else {
				List<FactoryNode> subNodesCopy = new ArrayList<>(toDelete.getSubNodes());
				for (FactoryNode child : subNodesCopy) {
					deleteAsset(child, false);
				}
			}
			FactoryNode parent = toDelete.getParent();
			if (parent != null) {
				//detach from parent
				parent.getSubNodes().remove(toDelete);
				toDelete.setParent(null);
				toDelete.setFactory(null);
			}
			SkillproService.getSkillproProvider().removeFactoryNode(toDelete);
			SkillproService.getUpdateManager().notify(UpdateType.ASSET_DELETED, null);
		}
		
	}

	private SelectionListener deleteSelectionListener() {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				deleteViewerSelections();
			}
		};
	}
	
	private void deleteViewerSelections() {
		Object[] selections = assetTreeComposite.getViewerSelection().toArray();
		boolean single = selections.length == 1;
		if (!single) {
			boolean confirm =  MessageDialog.openConfirm(getViewSite().getShell(),
					"Confirm", "Are you sure you want to delete the selected " + selections.length + " elements?");
			if (!confirm) {
				return;
			}
		}
		for (Object selectedElement : selections) {
			delete(selectedElement, single);
		}				
	}
	
	private void delete(Object selectedElement, boolean singleConfirm) {
		if (selectedElement instanceof FactoryNode) {
			deleteAsset((FactoryNode) selectedElement, singleConfirm);
		} else if (selectedElement instanceof ResourceSkill) {
			uprootResourceSkill((ResourceSkill) selectedElement);
		} else if (selectedElement instanceof ResourceConfiguration) {
			deleteResourceConfiguration((ResourceConfiguration) selectedElement);
		}
	}

	@Override
	public void setFocus() {
	}

	@Override
	public void update(UpdateType type) {
		switch (type) {
		case NEW_DATA_IMPORTED:
			for (Control child : assetTreeComposite.getTreeViewer().getTree().getChildren()) {
				child.dispose();
			}
			assetTreeComposite.setInput(SkillproService.getSkillproProvider().getAssetRepo());
			assetTreeComposite.getTreeViewer().refresh();
			break;
		case ASSET_CREATED:
			assetTreeComposite.getTreeViewer().refresh();
			break;
		case ASSET_UPDATED:
			assetTreeComposite.getTreeViewer().refresh();
			break;
		case ASSET_DELETED:
			assetTreeComposite.getTreeViewer().refresh();
			break;
		default:
			break;
		}
	}
}
