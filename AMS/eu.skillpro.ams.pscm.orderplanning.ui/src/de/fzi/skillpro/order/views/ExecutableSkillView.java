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

package de.fzi.skillpro.order.views;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import skillpro.model.service.SkillproService;
import skillpro.model.skills.ExecutableSkill;
import skillpro.model.skills.Skill;
import skillpro.model.update.Updatable;
import skillpro.model.update.UpdateType;
import skillpro.view.impl.ExecutableSkillSelectionTreeTableComposite;
import eu.skillpro.ams.ontology.reasoning.ClassMembershipRenderer;
import eu.skillpro.ams.ontology.reasoning.InferencesRenderer;
import eu.skillpro.ams.ontology.reasoning.ReasoningResults;
import eu.skillpro.ams.ontology.reasoning.renderer.ConformityRenderer;
import eu.skillpro.ams.ontology.util.OntologyUtil;
import eu.skillpro.ams.pscm.connector.opcua.Activator;
import eu.skillpro.ams.pscm.connector.opcua.OPCUAServerRepository;
import eu.skillpro.ams.pscm.icons.IconActivator;
import eu.skillpro.ams.pscm.ontology.transformer.SkillproToOntologyTransformer;

public class ExecutableSkillView extends ViewPart implements Updatable {
	public final static String ID = ExecutableSkillView.class.getName();
	private ExecutableSkillSelectionTreeTableComposite executableSkillTreeComposite;
	
	@Override
	public void createPartControl(Composite parent) {
		executableSkillTreeComposite = new ExecutableSkillSelectionTreeTableComposite(parent, SWT.NONE) {
			@Override
			protected void addCoolbarItems(Composite parent) {
				super.addCoolbarItems(parent);
				ToolBar coolToolBar = new ToolBar(parent, SWT.VERTICAL);
				createToolItem(coolToolBar, SWT.VERTICAL, "E*",
						IconActivator.getImageDescriptor("icons/commons/export_wiz.png").createImage(),
						"Exports current state into a .owl data", createExportOntologyListener(this));
				createToolItem(coolToolBar, SWT.VERTICAL, "C*", IconActivator.getImageDescriptor("icons/asset/remove.png")
						.createImage(), "Clear ExecutableSkills", clearSelectionListener());
			}
			
			@Override
			protected SelectionListener refreshSelectionListener() {
				return new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						refreshViewer();
					}
				};
			}
			
			private SelectionListener clearSelectionListener() {
				return new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						SkillproService.getSkillproProvider().getSkillRepo().getExecutableSkills().clear();
						SkillproService.getSkillproProvider().getSkillRepo().getExSkillsSelectionMap().clear();
						executableSkillTreeComposite.getTreeViewer().refresh();
					}
				};
			}
		};
		SkillproService.getUpdateManager().registerUpdatable(this, Skill.class);
		getSite().setSelectionProvider(executableSkillTreeComposite.getTreeViewer());
		initInput();
	}
	
	
	private void refreshViewer() {
		for (Control child : executableSkillTreeComposite.getTreeViewer().getTree().getChildren()) {
			child.dispose();
		}
		SkillproService.getSkillproProvider().getKPIRepo().wipeAllData();
		if (OPCUAServerRepository.testConnection(Activator.getDefault().getCurrentUAaddress())) {
			SkillproService.getSkillproProvider().getKPIRepo().getKpiMap().putAll(OPCUAServerRepository
					.getKPIData(Activator.getDefault().getCurrentUAaddress()));
		}
		executableSkillTreeComposite.getTreeViewer().refresh();
		SkillproService.getUpdateManager().notify(UpdateType.KPI_UPDATED, null);
	}
	
	private void initInput() {
		Map<ExecutableSkill, Boolean> input = SkillproService.getSkillproProvider().getSkillRepo().getExSkillsSelectionMap();
		for (ExecutableSkill exSkill : SkillproService.getSkillproProvider().getSkillRepo().getExecutableSkills()) {
			input.put(exSkill, true);
		}
		executableSkillTreeComposite.setInput(input.entrySet());
	}

	@Override
	public void setFocus() {
		executableSkillTreeComposite.getTreeViewer().getControl().setFocus();
	}
	
	@Override
	public void update(UpdateType type) {
		switch (type) {
		case NEW_DATA_IMPORTED:
			executableSkillTreeComposite.disposeAllItems();
			initInput();
			executableSkillTreeComposite.getTreeViewer().refresh();
			break;
		case EXECUTABLE_SKILLS_GENERATED:
			initInput();
			refreshViewer();
			break;
		case SKILL_UPDATED:
			executableSkillTreeComposite.getTreeViewer().refresh();
			break;
		case SKILL_DELETED:
			executableSkillTreeComposite.getTreeViewer().refresh();
			break;
		case ASSET_CREATED:
			executableSkillTreeComposite.getTreeViewer().refresh();
			break;
		case ASSET_DELETED:
			executableSkillTreeComposite.getTreeViewer().refresh();
		case ASSET_UPDATED:
			executableSkillTreeComposite.getTreeViewer().refresh();
		case CONFIGURATION_UPDATED:
			executableSkillTreeComposite.getTreeViewer().refresh();
		default:
			break;
		}
	}
	
	private SelectionListener createExportOntologyListener(final ExecutableSkillSelectionTreeTableComposite parent) {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				SkillproService.getOntologyProvider().wipeAllData();
				SkillproToOntologyTransformer.getInstance().transform();
				String defaultFilepath = "SkillProDefault.owl";
				try {
					OntologyUtil.loadAndExport(defaultFilepath);
				} catch (OWLOntologyCreationException e1) {
					e1.printStackTrace();
				}
				interpretOntology(defaultFilepath);
				SkillproService.getUpdateManager().notify(UpdateType.EXECUTABLE_SKILLS_TESTED, null);
			}

			private void interpretOntology(String filepath) {
				ClassMembershipRenderer defaultRenderer = new ConformityRenderer();
				InferencesRenderer inferencesRenderer = new InferencesRenderer(defaultRenderer);
				ReasoningResults result = OntologyUtil.getInstance().interpretOntology(inferencesRenderer);
				MessageBox dialog = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.ICON_QUESTION | SWT.YES| SWT.NO);
				dialog.setText("Executable Skills Test Results");
				String message = "";
				if (result.getResults().isEmpty()) {
					message = "No problems detected";
				} else {
					int i = 1;
					for (Entry<OWLNamedIndividual, List<String>> entry : result.getResults().entrySet()) {
						message = "- Message " + i + ": " + entry.getKey().getIRI().toString().replace("XXX", " ") + ", that" +
								" was sorted into class ";
						for (String value : entry.getValue()) {
							message = message + value + "\n";
						}
					}
				}
				dialog.setMessage(message);
				dialog.open();
			}
			
		};
	}
}
