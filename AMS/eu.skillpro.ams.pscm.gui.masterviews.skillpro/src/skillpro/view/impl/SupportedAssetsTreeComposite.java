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
import java.util.List;

import masterviews.composite.abstracts.TreeComposite;
import masterviews.dialogs.EditAllNameSelectionDialog;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;

import skillpro.dialogs.ConfigurationSelectionDialog;
import skillpro.dialogs.TemplateSkillSelectionDialog;
import skillpro.model.assets.Factory;
import skillpro.model.assets.FactoryNode;
import skillpro.model.assets.Resource;
import skillpro.model.assets.Setup;
import skillpro.model.service.SkillproService;
import skillpro.model.skills.ResourceSkill;
import skillpro.model.skills.TemplateSkill;
import skillpro.model.update.UpdateType;
import skillpro.providers.skill.SupportedAssetsContentProvider;
import skillpro.providers.skill.SupportedAssetsLabelProvider;

public class SupportedAssetsTreeComposite extends TreeComposite implements ISelectionListener {
	public SupportedAssetsTreeComposite(Composite parent, int style) {
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
		Object currentSelection = ((IStructuredSelection) selection)
				.getFirstElement();
		TemplateSkill temp = null;
		if (currentSelection instanceof TemplateSkill) {
			temp = (TemplateSkill) currentSelection;
			
		}
		
		initInput(temp);
		getViewer().refresh();
		
	}
	
	private void initInput(TemplateSkill temp) {
		if (temp != null) {
			getTreeViewer().setInput(SkillproService.getSkillproProvider()
					.getResourceSkillRepo().getCorrespondingResourceSkills(temp));
		}

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
	        	if (firstElement instanceof ResourceSkill) {
	        		final ResourceSkill resourceSkill = (ResourceSkill) firstElement;
	        		//FIXME these are not needed anymore right?
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
	        				}
	        			}
	        		});
	        		
	        		manager.add(new Action() {
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
	        				}
	        			}
	        		});
	        		
	        	} else if (firstElement instanceof FactoryNode) {
	        		final FactoryNode selection = (FactoryNode) firstElement;
	        		manager.add(copyAction(selection));
	        	} else if (firstElement instanceof Setup) {
	        		final Setup conf = (Setup) firstElement;
	        		manager.add(new Action() {
	        			
	        			@Override
	        			public String getText() {
	        				return "Set as current configuration";
	        			}
	        			
	        			@Override
	        			public void run() {
	        				conf.getResource().setCurrentSetup(conf);
	        				SkillproService.getUpdateManager().notify(UpdateType.ASSET_UPDATED, FactoryNode.class);
	        			}
					});
	        		
	        		manager.add(new Action() {
	        			@Override
	        			public String getText() {
	        				return "Add resource skill";
	        			}
	        			
	        			@Override
	        			public void run() {
	        				TemplateSkillSelectionDialog dialog = new TemplateSkillSelectionDialog(getShell(),
	        						SkillproService.getSkillproProvider().getTemplateSkillRepo().getEntities());
	        				
	        				if (dialog.open() == Window.OK) {
	        					TemplateSkill tSkill = (TemplateSkill) dialog.getResult()[0];
	        					Resource resource = conf.getResource();
	        					ResourceSkill rSkill = SkillproService.getSkillproProvider().createResourceSkill(tSkill.getName(), tSkill, resource);
	        					
	        					if (!conf.getResourceSkills().contains(rSkill)) {
	        						conf.addResourceSkill(rSkill);
	        					}
	        					SkillproService.getUpdateManager().notify(UpdateType.ASSET_UPDATED, FactoryNode.class);
	        				}
	        			}
					});

	        	}
	        }

			private Action copyAction(final FactoryNode selection) {
				return new Action() {
					
					@Override
					public String getText() {
						return "Copy";
					}
					
					@Override
					public void run() {
						EditAllNameSelectionDialog editNameDialog = new EditAllNameSelectionDialog(getShell(), selection.getName());
						FactoryNode copy = null;
						if (editNameDialog.open() == Window.OK) {
							if (selection instanceof Factory) {
								copy = SkillproService.getSkillproProvider().createFactory((Factory) selection);
								
							} else if (selection instanceof Resource) {
								Resource resource = (Resource) selection;
								List<Setup> newSetups = new ArrayList<>();
								for (Setup config : resource.getSetups()) {
									Setup newConfig = SkillproService.getSkillproProvider().createSetup(config.getName(), resource);
									for (ResourceSkill rs : config.getResourceSkills()) {
										newConfig.addResourceSkill(SkillproService.getSkillproProvider().createResourceSkill(rs.getName(), rs.getTemplateSkill(), resource));
									}
									//current configuration for the new copied workplace?
//									if (workplace.getCurrentSetup().equals(config));
									newSetups.add(newConfig);
								}
								copy = SkillproService.getSkillproProvider().createResource(editNameDialog.getName(),
										newSetups, resource.getParent());
							} else {
								copy = SkillproService.getSkillproProvider().createFactoryNode(editNameDialog.getName(), selection.getParent());
							}
							copy.setLayoutable(selection.isLayoutable());
							copy.setName(editNameDialog.getName());
							copy.setCurrentCoordinates(selection.getCurrentCoordinates());
							copy.setSize(selection.getSize());
							
							SkillproService.getUpdateManager().notify(UpdateType.ASSET_CREATED, FactoryNode.class);
						}
						
					}
				};
			}
	    });
	    
	    Menu menu = menuMgr.createContextMenu(getViewer().getControl());
	    getViewer().getControl().setMenu(menu);
	}
}
