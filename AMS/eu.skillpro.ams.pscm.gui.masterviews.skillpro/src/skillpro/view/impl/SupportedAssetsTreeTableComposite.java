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

import masterviews.composite.abstracts.TreeTableComposite;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;

import skillpro.dialogs.AddRequirementPairDialog;
import skillpro.dialogs.ConfigurationSelectionDialog;
import skillpro.model.assets.Resource;
import skillpro.model.assets.ResourceConfiguration;
import skillpro.model.products.ProductConfiguration;
import skillpro.model.service.SkillproService;
import skillpro.model.skills.PrePostRequirement;
import skillpro.model.skills.Requirement;
import skillpro.model.skills.RequirementProductConfigType;
import skillpro.model.skills.RequirementResourceConfigType;
import skillpro.model.skills.ResourceSkill;
import skillpro.model.skills.Skill;
import skillpro.model.update.UpdateType;
import skillpro.providers.skill.SupportedAssetsContentProvider;
import skillpro.providers.skill.SupportedAssetsLabelProvider;
import eu.skillpro.ams.pscm.icons.IconActivator;

public class SupportedAssetsTreeTableComposite extends TreeTableComposite implements ISelectionListener {
	public SupportedAssetsTreeTableComposite(Composite parent, int style) {
		super(parent, style);
		addContextMenu();
	}

	@Override
	protected LabelProvider initLabelProvider() {
		return new SupportedAssetsLabelProvider();
	}

	@Override
	protected IContentProvider initContentProvider() {
		return new SupportedAssetsContentProvider();
	}
	
	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
	}
	
	private void addContextMenu() {
		MenuManager menuMgr = new MenuManager();
	    menuMgr.setRemoveAllWhenShown(true);
	    menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
	        	if (viewer.getSelection().isEmpty()) {
                    return;
                }
	        	
	        	final Object firstElement = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
	        	if (firstElement instanceof ResourceSkill) {
	        		final ResourceSkill resourceSkill = (ResourceSkill) firstElement;
	        		manager.add(new Action() {
	        			@Override
	        			public String getText() {
	        				return "Add Requirement";
	        			}
	        			
						@Override
	        			public void run() {
	        				AddRequirementPairDialog dialog = new AddRequirementPairDialog(getShell(), resourceSkill);
	        				
	        				if (dialog.open() == Window.OK) {
	        					for (Object obj : dialog.getResult()) {
	        						if (obj instanceof PrePostRequirement) {
	        							resourceSkill.addPrePostRequirement((PrePostRequirement) obj);
	        						} else {
	        							throw new IllegalArgumentException("Error! Result is of type: " + obj.getClass());
	        						}
	        					}
	        					SkillproService.getUpdateManager().notify(UpdateType.SKILL_UPDATED, null);
	        					SkillproService.getUpdateManager().notify(UpdateType.ASSET_UPDATED, null);
	        				}
	        			}
	        		});
	        		
	        		
	        		manager.add(new Separator());
	        		
	        		manager.add(new Action() {
	        			@Override
	        			public String getText() {
	        				return "Set pre-condition";
	        			}
	        			
	        			@Override
	        			public void run() {
	        				Shell shell = Display.getCurrent().getActiveShell();
	        				ConfigurationSelectionDialog dialog = new ConfigurationSelectionDialog(shell, (Resource) resourceSkill.getResource());
	        				if (dialog.open() == Dialog.OK) {
	        					resourceSkill.setPreConfiguration(dialog.getResult()[0]);
	        					SkillproService.getUpdateManager().notify(UpdateType.SKILL_UPDATED, Skill.class);
	        				}
	        			}
	        		});
	        		
	        		manager.add(new Action() {
	        			//This works, just that it won't be shown in the GUI.
	        			//Modify the LabelProvider to show the pre/post conditions of the ResourceSkill.
	        			@Override
	        			public String getText() {
	        				return "Set post-condition";
	        			}
	        			
	        			@Override
	        			public void run() {
	        				Shell shell = Display.getCurrent().getActiveShell();
	        				ConfigurationSelectionDialog dialog = new ConfigurationSelectionDialog(shell, (Resource) resourceSkill.getResource());
	        				if (dialog.open() == Dialog.OK) {
	        					resourceSkill.setPostConfiguration(dialog.getResult()[0]);
	        					SkillproService.getUpdateManager().notify(UpdateType.SKILL_UPDATED, Skill.class);
	        				}
	        			}
	        		});
	        	} else if (firstElement instanceof PrePostRequirement) {
	        		final PrePostRequirement requirementPair = (PrePostRequirement) firstElement;
	        		final ResourceSkill mainResourceSkill = requirementPair.getPreRequirement().getMainResourceSkill();
	        		manager.add(new Action() {
	        			@Override
	        			public String getText() {
	        				return "Edit Requirement";
	        			}
	        			
						@Override
	        			public void run() {
	        				AddRequirementPairDialog dialog = new AddRequirementPairDialog(getShell(), requirementPair);
	        				
	        				if (dialog.open() == Window.OK) {
	        					mainResourceSkill.getPrePostRequirements().remove(requirementPair);
	        					for (Object obj : dialog.getResult()) {
	        						if (obj instanceof PrePostRequirement) {
	        							mainResourceSkill.addPrePostRequirement((PrePostRequirement) obj);
	        						} else {
	        							throw new IllegalArgumentException("Error! Result is of type: " + obj.getClass());
	        						}
	        					}
	        					SkillproService.getUpdateManager().notify(UpdateType.SKILL_UPDATED, Skill.class);
	        				}
	        			}
	        		});
	        		
	        		manager.add(new Action() {
	        			@Override
	        			public String getText() {
	        				return "Delete Requirement";
	        			}
	        			
						@Override
	        			public void run() {
	        				if (mainResourceSkill.equals(requirementPair.getPreRequirement().getRequiredResourceSkill())) {
	        					
	        				} else {
	        					setEnabled(true);
	        					mainResourceSkill.getPrePostRequirements().remove(requirementPair);
	        					SkillproService.getUpdateManager().notify(UpdateType.SKILL_UPDATED, Skill.class);
	        				}
	        			}
	        			
	        			public boolean isEnabled() {
	        				if (mainResourceSkill.equals(requirementPair.getPreRequirement().getRequiredResourceSkill())) {
	        					return false;
	        				}
	        				return true;
	        			};
	        		});
	        	}
	        }
	    });
	    
	    Menu menu = menuMgr.createContextMenu(viewer.getControl());
	    viewer.getControl().setMenu(menu);
	}

	@Override
	protected void addColumn(Tree tree) {
		addPreProductConfigColumn(tree);
		addPostProductConfigurationColumn(tree);
		addTemplateSkillColumn(tree);
		addAssetColumn(tree);
		addPreConfigurationColumn(tree);
		addPostConfigurationColumn(tree);
	}
	
	private void addPreConfigurationColumn(final Tree tree) {
		TreeColumn column = new TreeColumn(tree, SWT.RIGHT | SWT.CHECK, 5);
		column.setAlignment(SWT.LEFT);
		column.setText("Pre-Configuration");
		column.setWidth(100);
		TreeViewerColumn treeViewerColumn = new TreeViewerColumn(viewer, column);
		
		treeViewerColumn.setLabelProvider(new PreConfigurationColumnProvider());
	}
	
	private void addPostConfigurationColumn(final Tree tree) {
		TreeColumn column = new TreeColumn(tree, SWT.RIGHT | SWT.CHECK, 6);
		column.setAlignment(SWT.LEFT);
		column.setText("Post-Configuration");
		column.setWidth(100);
		TreeViewerColumn treeViewerColumn = new TreeViewerColumn(viewer, column);
		
		treeViewerColumn.setLabelProvider(new PostConfigurationColumnProvider());
	}
	
	private void addPreProductConfigColumn(final Tree tree) {
		TreeColumn secondColumn = new TreeColumn(tree, SWT.RIGHT | SWT.CHECK, 1);
		secondColumn.setAlignment(SWT.LEFT);
		secondColumn.setText("Pre-PConf");
		secondColumn.setWidth(100);
		TreeViewerColumn treeViewerColumn = new TreeViewerColumn(viewer, secondColumn);
		
		treeViewerColumn.setLabelProvider(new PreProductConfigurationColumnProvider());
	}
	
	private void addPostProductConfigurationColumn(final Tree tree) {
		TreeColumn secondColumn = new TreeColumn(tree, SWT.RIGHT | SWT.CHECK, 2);
		secondColumn.setAlignment(SWT.LEFT);
		secondColumn.setText("Post-PConf");
		secondColumn.setWidth(100);
		TreeViewerColumn treeViewerColumn = new TreeViewerColumn(viewer, secondColumn);
		
		treeViewerColumn.setLabelProvider(new PostProductConfigurationColumnProvider());
	}
	
	private void addTemplateSkillColumn(final Tree tree) {
		TreeColumn secondColumn = new TreeColumn(tree, SWT.RIGHT | SWT.CHECK, 3);
		secondColumn.setAlignment(SWT.LEFT);
		secondColumn.setText("TemplateSkill");
		secondColumn.setWidth(100);
		TreeViewerColumn treeViewerColumn = new TreeViewerColumn(viewer, secondColumn);
		
		treeViewerColumn.setLabelProvider(new TemplateSkillColumnProvider());
	}
	
	
	
	private void addAssetColumn(final Tree tree) {
		
		TreeColumn column = new TreeColumn(tree, SWT.RIGHT | SWT.CHECK, 4);
		column.setAlignment(SWT.LEFT);
		column.setText("Asset");
		column.setWidth(100);
		TreeViewerColumn treeViewerColumn = new TreeViewerColumn(viewer, column);
		
		treeViewerColumn.setLabelProvider(new AssetColumnSupportedRoleProvider());
	}
	
	private class PreConfigurationColumnProvider extends ColumnLabelProvider {

		@Override
		public String getText(Object element) {
			if (element instanceof PrePostRequirement) {
				Requirement firstElement = ((PrePostRequirement) element).getPreRequirement();
				return resourceConfigurationToString(firstElement.getResourceConfigType(), firstElement.getRequiredResourceConfiguration());
			}
			return "";
		}

		@Override
		public Image getImage(Object element) {
			return null;
		}
	}
	
	private class PostConfigurationColumnProvider extends ColumnLabelProvider {

		@Override
		public String getText(Object element) {
			if (element instanceof PrePostRequirement) {
				Requirement secondElement = ((PrePostRequirement) element).getPostRequirement();
				return resourceConfigurationToString(secondElement.getResourceConfigType(), secondElement.getRequiredResourceConfiguration());
			}
			return "";
		}

		@Override
		public Image getImage(Object element) {
			return null;
		}
	}
	
	private static String resourceConfigurationToString(RequirementResourceConfigType requirementResourceConfigType, ResourceConfiguration resourceConfiguration) {
		switch (requirementResourceConfigType) {
		case ANY:
		case DIFFERENT_ANY:
		case SAME:
			return requirementResourceConfigType.toString();
		case SPECIFIC:
			return resourceConfiguration == null ? null : resourceConfiguration.getName();
		default:
			return "Unknown RequirementResourceConfigType";
		}
	}
	
	private class PreProductConfigurationColumnProvider extends ColumnLabelProvider {
		@Override
		public String getText(Object element) {
			if (element instanceof ResourceSkill) {
				return "";
			} else if (element instanceof PrePostRequirement) {
				Requirement firstElement = ((PrePostRequirement) element).getPreRequirement();
				return getProductConfigurationString(firstElement.getProductConfigType(), firstElement.getRequiredProductConfiguration());
			}
			return "ERROR (not a resource skill)";
		}

		@Override
		public Image getImage(Object element) {
			return null;
		}
	}
	
	private class PostProductConfigurationColumnProvider extends ColumnLabelProvider{

		@Override
		public String getText(Object element) {
			if (element instanceof ResourceSkill) {
				return "";
			} else if (element instanceof PrePostRequirement) {
				Requirement secondElement = ((PrePostRequirement) element).getPostRequirement();
				return getProductConfigurationString(secondElement.getProductConfigType(), secondElement.getRequiredProductConfiguration());	
			}
			return "ERROR (not a resource skill)";
		}

		@Override
		public Image getImage(Object element) {
			return null;
		}
	}
	
	private static String getProductConfigurationString(RequirementProductConfigType configType, ProductConfiguration productConfiguration) {
		switch (configType) {
		case ANY:
		case EMPTY:
		case SAME:
			return configType.toString();
		case SPECIFIC:
			return productConfiguration == null ? null : productConfiguration.toString();
		default:
			return "Unknown type";	
		}
	}
	
	private class AssetColumnSupportedRoleProvider extends ColumnLabelProvider {
		private Image assetIcon;

		@Override
		public String getText(Object element) {
			if (element instanceof ResourceSkill) {
				return ((ResourceSkill) element).getResource().getName();
			} else if (element instanceof PrePostRequirement) {
				Requirement firstElement = ((PrePostRequirement) element).getPreRequirement();
				return firstElement.getRequiredResourceSkill().getResource().toString();
			}
			return "ERROR (not a resource skill)";
		}

		@Override
		public Image getImage(Object element) {
			return getAssetIcon();
		}
		
		private Image getAssetIcon() {
			if (assetIcon == null) {
				assetIcon = IconActivator.getImageDescriptor("icons/asset/wp.png").createImage();
			}
			
			return assetIcon;
		}
	}
	
	private class TemplateSkillColumnProvider extends ColumnLabelProvider {
		private Image assetIcon;

		@Override
		public String getText(Object element) {
			if (element instanceof ResourceSkill) {
				return ((ResourceSkill) element).getTemplateSkill().getName();
			} else if (element instanceof PrePostRequirement) {
				Requirement firstElement = ((PrePostRequirement) element).getPreRequirement();
				return firstElement.getRequiredResourceSkill().getTemplateSkill().toString();
			}
			return "ERROR (not a resource skill)";
		}

		@Override
		public Image getImage(Object element) {
			return getAssetIcon();
		}
		
		private Image getAssetIcon() {
			if (assetIcon == null) {
				assetIcon = IconActivator.getImageDescriptor("icons/skill/ts.png").createImage();
			}
			return assetIcon;
		}
	}
}
