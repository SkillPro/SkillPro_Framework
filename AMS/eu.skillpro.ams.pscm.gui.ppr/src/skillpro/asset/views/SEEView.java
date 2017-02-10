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

package skillpro.asset.views;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import masterviews.composite.abstracts.TreeTableComposite;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.part.ViewPart;

import skillpro.ams.util.AMSServiceUtility;
import skillpro.asset.views.dialogs.CreateSEEDialog;
import skillpro.asset.views.dialogs.RegisterSEEDialog;
import skillpro.dialogs.ChooseAssetFromConfigurationDialog;
import skillpro.model.assets.Resource;
import skillpro.model.assets.SEE;
import skillpro.model.assets.SEEState;
import skillpro.model.service.SkillproService;
import skillpro.model.update.Updatable;
import skillpro.model.update.UpdateType;
import skillpro.vc.csclient.VCClient;
import eu.skillpro.ams.pscm.connector.opcua.Activator;
import eu.skillpro.ams.pscm.connector.opcua.OPCUAServerRepository;
import eu.skillpro.ams.pscm.connector.opcua.SkillProOPCUAException;
import eu.skillpro.ams.pscm.icons.IconActivator;

/**
 * @author Kiril Aleksandrov
 * 
 * @version: 03.10.2014
 * 
 */
public class SEEView extends ViewPart implements Updatable {

	public static final String ID = SEEView.class.getName();

	private TreeTableComposite seeTreeComposite;

	public SEEView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		seeTreeComposite = new TreeTableComposite(parent, SWT.NONE, true) {
			@Override
			protected void createTreeViewer(Composite parent, int style) {
				super.createTreeViewer(parent, style);
				firstColumn.setText("Configuration ID");
				addContextMenu(viewer);
			}
			
			@Override
			protected void addCoolbarItems(Composite parent) {
				ToolBar coolToolBar = new ToolBar(parent, SWT.VERTICAL);
				createToolItem(coolToolBar, SWT.VERTICAL, "A*", IconActivator.getImageDescriptor("icons/asset/add.png").createImage(), "Creates a new asset with the help of wizard", createSEEListener(this));
				createToolItem(coolToolBar, SWT.VERTICAL, "D*", IconActivator.getImageDescriptor("icons/asset/remove.png").createImage(), "Delete selected element", deleteSelectionListener());
			}
			

			private void addContextMenu(final TreeViewer viewer) {
				MenuManager menuMgr = new MenuManager();
				menuMgr.setRemoveAllWhenShown(true);
				menuMgr.addMenuListener(new IMenuListener() {
					public void menuAboutToShow(IMenuManager manager) {
						if (viewer.getSelection().isEmpty()) {
							return;
						}
						final Object firstElement = ((IStructuredSelection) viewer
								.getSelection()).getFirstElement();
						if (firstElement instanceof SEE) {
							final SEE see = (SEE) firstElement;
							createMenuForSEE(manager, see);
						} else {
							// if selected element is not root 
							// (hence child of SEE)
							if (firstElement instanceof SEEResource) { 
								return;
							}
						}
					}

					private void createMenuForSEE(IMenuManager manager,
							final SEE see) {

						// check if see already assigned to workplace ->
						// if not assign menu, if so register menu
						if (see.getResource() == null) {
							// assign SEE to Resource menu
							assignResourceToSEE(manager, see);
						} else {
							Resource resource = see.getResource();
							// register assigned SEE to OPC-UA menu
							assignResourceToSEE(manager, see);
							registerResourceToSEE(manager, see, resource);
							unregisterResourceFromOPCUA(manager, see, resource);
							seeTreeComposite.getTreeViewer().refresh();
						}
					}

					private void assignResourceToSEE(IMenuManager manager,
							final SEE see) {
						manager.add(new Action("Assign from catalogue") {
							@Override
							public void run() {
							}
						});
						manager.add(new Action("Assign from configuration") {
							@Override
							public void run() {
								ChooseAssetFromConfigurationDialog dialog = new ChooseAssetFromConfigurationDialog(getShell());
								if (dialog.open() == Window.OK) {
									Resource resource = dialog.getResult()[0];
									if (resource == null) {
										return;
									}
									see.addNotRegisteredResource(resource);
								}
								SkillproService.getUpdateManager().notify(UpdateType.SEE_ADDED, SEE.class);
							}
						});
					}

					private void registerResourceToSEE(IMenuManager manager,
							final SEE see, final Resource resource) {
						manager.add(new Action("Register SEE") {
							@Override
							public void run() {
								RegisterSEEDialog dialog = new RegisterSEEDialog(getShell(), see, resource);
								if (dialog.open() == Dialog.OK) {
									see.setMESNodeID(dialog.getNodeID());
									see.setOpcUAAddress(Activator.getDefault()
											.getCurrentUAaddress());
									see.setSEEState(SEEState.REGISTERED);
									resource.setResponsibleSEE(see);
									// updates the SEE on the AMS server
									updateSeeOnServer(see);
									if (see.isSimulation()) {
										VCClient.getInstance().registerSEEToVIS(see);
									}
									SkillproService.getUpdateManager().notify(UpdateType.SEE_ADDED, SEE.class);
								}
							}

							private boolean updateSeeOnServer(SEE see) {
								String serviceName = "updateSEE";
								String parameters = "?configurationId=" + see.getSeeID();
								parameters += "&runtimeId=" + see.getMESNodeID();
								parameters += "&opcuaAddress=" + see.getOpcUAAddress();
								parameters += "&simulation=" + see.isSimulation();
								
								HttpGet request = new HttpGet(
										AMSServiceUtility.serviceAddress
												+ serviceName + parameters);
								request.setHeader("Content-type", "application/json");

								HttpClient client = HttpClientBuilder.create().build();;
								HttpResponse response;
								try {
									response = client.execute(request);
									System.out.println("Response status: " + response.getStatusLine().getStatusCode());
									return true;
								} catch (IOException e) {
									e.printStackTrace();
									MessageDialog.openError(getShell(),
											"AMS service error", e.getMessage());
									return false;
								}
							}
						});
					}

					private void unregisterResourceFromOPCUA(IMenuManager manager,
							final SEE see, final Resource resource) {
						manager.add(new Action("Unregister SEE") {
							@Override
							public void run() {
								try {
									OPCUAServerRepository.deregisterSEE(see.getMESNodeID(),
											Activator.getDefault().getCurrentUAaddress());
									see.setSEEState(SEEState.NOT_REGISTERED);
									resource.setResponsibleSEE(see);
								} catch (SkillProOPCUAException e) {
									e.printStackTrace();
									MessageDialog.openWarning(getShell(),
											"OPC-UA connection error",
											e.getMessage());
								}
							}
						});
					}
				});

				Menu menu = menuMgr.createContextMenu(viewer.getControl());
				viewer.getControl().setMenu(menu);
			}

			@Override
			protected LabelProvider initLabelProvider() {
				return new SEETreeLabelProvider();
			}

			@Override
			protected IContentProvider initContentProvider() {
				return new SEETreeContentProvider();
			}

			@Override
			protected void addColumn(Tree tree) {
				TreeColumn column1 = new TreeColumn(tree, SWT.LEFT, 1);
				column1.setAlignment(SWT.LEFT);
				column1.setText("Asset type(s)");
				column1.setWidth(300);
				TreeViewerColumn treeViewerColumn1 = new TreeViewerColumn(
						viewer, column1);
				treeViewerColumn1
						.setLabelProvider(new AssetColumnProvider());

				TreeColumn column2 = new TreeColumn(tree, SWT.LEFT, 2);
				column2.setAlignment(SWT.LEFT);
				column2.setText("Node ID (runtime)");
				column2.setWidth(100);
				TreeViewerColumn treeViewerColumn2 = new TreeViewerColumn(
						viewer, column2);
				treeViewerColumn2
						.setLabelProvider(new RuntimeNodeColumnProvider());
				
				TreeColumn column3 = new TreeColumn(tree, SWT.LEFT , 3);
				column3.setAlignment(SWT.LEFT);
				column3.setText("Simulation");
				column3.setWidth(50);
				TreeViewerColumn treeViewerColumn3 = new TreeViewerColumn(
						viewer, column3);
				treeViewerColumn3
						.setLabelProvider(new SimulationLabelProvider());
				
				TreeColumn column4 = new TreeColumn(tree, SWT.LEFT, 4);
				column4.setAlignment(SWT.LEFT);
				column4.setText("OPC-UA address");
				column4.setWidth(100);
				TreeViewerColumn treeViewerColumn4 = new TreeViewerColumn(
						viewer, column4);
				treeViewerColumn4
						.setLabelProvider(new OPCServerLabelProvider());
			}
		};
		
		seeTreeComposite.getTreeViewer().addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				if (selection.getFirstElement() instanceof SEE) {
					SEE see = (SEE) selection.getFirstElement();
					see.setSimulation(!see.isSimulation());
					seeTreeComposite.getTreeViewer().refresh();
				}
			}
		});
		seeTreeComposite.setInput(SkillproService.getSkillproProvider()
				.getSEERepo().getEntities());
		SkillproService.getUpdateManager().registerUpdatable(this, SEE.class);
	}

	@Override
	public void setFocus() {
		seeTreeComposite.setFocus();
	}
	
	private SelectionListener createSEEListener(final TreeTableComposite parent) {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				CreateSEEDialog dialog = new CreateSEEDialog(parent.getShell());
				if (dialog.open() == Dialog.OK) {
					SkillproService.getSkillproProvider().getSEERepo().add(dialog.getSEE());
					SkillproService.getUpdateManager().notify(UpdateType.SEE_ADDED, null);
				}
			}
		};
	}
	
	private SelectionListener deleteSelectionListener() {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				//get selected
				IStructuredSelection selection = (IStructuredSelection) seeTreeComposite.getTreeViewer().getSelection();
				Object selectedElement = selection.getFirstElement();
				if (selectedElement != null) {
					//is empty?
					if (selectedElement instanceof SEE) {
						SEE see = (SEE) selectedElement;
						SkillproService.getSkillproProvider().removeSEE(see);					
						SkillproService.getUpdateManager().notify(UpdateType.SEE_ADDED, null);
					}
				}
			}
		};
	}

	@Override
	public void update(UpdateType type) {
		switch (type) {
		case SEE_IMPORTED:
			seeTreeComposite.getTreeViewer().refresh();
		case NEW_DATA_IMPORTED:
			seeTreeComposite.disposeAllItems();
			seeTreeComposite.setInput(SkillproService.getSkillproProvider().getSEERepo().getEntities());
			seeTreeComposite.getTreeViewer().refresh();
			break;
		case SEE_ADDED:
			seeTreeComposite.getTreeViewer().refresh();
			break;
		default:
			break;
		}
	}
	private class SEETreeLabelProvider extends LabelProvider implements
			ITableLabelProvider, ITableColorProvider {

		private Image seeRegisteredImage = IconActivator.getImageDescriptor(
				"icons/see/bundeled.png").createImage();
		private Image seeNotRegisteredImage = IconActivator.getImageDescriptor(
				"icons/see/notregistered.png").createImage();
		private Image seeBundledImage = IconActivator.getImageDescriptor(
				"icons/see/registered.png").createImage();

		@Override
		public Color getForeground(Object element, int columnIndex) {
			return null;
		}

		@Override
		public Color getBackground(Object element, int columnIndex) {
			return null;
		}

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			if (columnIndex == 0) {
				SEE see;
				
				if (element instanceof SEE) {
					see = (SEE) element;
				} else if (element instanceof SEEResource) {
					see = ((SEEResource) element).getSee();
				} else {
					return null;
				}
				switch (see.getSEEState()) {
				case NOT_REGISTERED:
					return seeNotRegisteredImage;
				case REGISTERED:
					return seeRegisteredImage;
				case BUNDLED:
					return seeBundledImage;
				default:
					return null;
				}
			} else
				return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			switch (columnIndex) {
			case 0:
				if (element instanceof SEEResource) {
					return ((SEEResource) element).getResource().getName();
				} else if (element instanceof SEE) {
					return ((SEE) element).getSeeID();
				}
			case 1:
				if (element instanceof SEEResource) {
					return ((SEEResource) element).getSee().getSEEState().name();
				} else if (element instanceof SEE) {
					return ((SEE) element).getResource().getName();
				}
			default:
				return null;
			}
		}
	}

	private class AssetColumnProvider extends ColumnLabelProvider {
		@Override
		public String getText(Object element) {
			SEE see = null;
			if (element instanceof SEE) {
				see = (SEE) element;
			} else if (element instanceof SEEResource) {
				see = ((SEEResource) element).getSee();
			} else {
				return null;
			}
			Resource resource = see.getResource();
			return resource != null ? resource.getName() : "ERROR, Resource is null";
		}

		@Override
		public Image getImage(Object element) {
			return null;
		}

	}

	private class RuntimeNodeColumnProvider extends
			ColumnLabelProvider {
		@Override
		public String getText(Object element) {
			if (element instanceof SEE) {
				return ((SEE) element).getMESNodeID();
			} else if (element instanceof SEEResource) {
				return null;
			}
			return null;
		}

		@Override
		public Image getImage(Object element) {
			return null;
		}
	}

	private class OPCServerLabelProvider extends ColumnLabelProvider {
		@Override
		public String getText(Object element) {
			if (element instanceof SEE) {
				return ((SEE) element).getOpcUAAddress();
			} else if (element instanceof SEEResource) {
				return null;
			}
			return null;
		}

		@Override
		public Image getImage(Object element) {
			return null;
		}
	}
	
	private class SimulationLabelProvider extends ColumnLabelProvider {
		@Override
		public String getText(Object element) {
			if (element instanceof SEE) {
				return ((SEE) element).isSimulation()? "Yes" : "No";
			}
			return null;
		}
	}

	private class SEETreeContentProvider extends ArrayContentProvider implements
			ITreeContentProvider {
		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		@Override
		public Object[] getElements(Object inputElement) {
			return super.getElements(inputElement);
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof SEE) {
				List<SEEResource> result = new ArrayList<SEEResource>();
				result.add(new SEEResource((SEE) parentElement));
				return result.toArray();
			} else {
				return null;
			}
		}

		@Override
		public Object getParent(Object element) {
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			if (element instanceof SEE) {
				return ((SEE) element).getResource() != null;
			} else {
				return false;
			}
		}
	}

	private class SEEResource {
		private SEE see;
		private Resource resource;

		public SEEResource(SEE see) {
			this.see = see;
			this.resource = see.getResource();
		}

		public SEE getSee() {
			return see;
		}

		public Resource getResource() {
			return resource;
		}
	}
}
