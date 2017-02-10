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

package dummy.generate.executableskill;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.transform.TransformerException;

import masterviews.dialogs.MasterFileDialog;
import masterviews.util.SupportedFileType;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SelectionDialog;

import skillpro.model.skills.dummy.Condition;
import skillpro.model.skills.dummy.ConditionConfiguration;
import skillpro.model.skills.dummy.ConditionProduct;
import skillpro.model.skills.dummy.ExecutableSkillDummy;
import skillpro.model.skills.dummy.ResourceDummy;
import skillpro.model.skills.dummy.ResourceExecutableSkillDummy;
import skillpro.model.utils.Pair;

public class DummyExecutableSkillsDialog extends SelectionDialog {

	//dialog attributes
	private final Map<ResourceDummy, Condition> currentConditionMapping = new HashMap<>();
	private DummyScenario chosenExSkillScenario = null;

	//inputs
	private final List<Pair<ResourceDummy, Condition>> resourceConditionInput = new ArrayList<>();
	private final List<ResourceExecutableSkillDummy> rexInput = new ArrayList<>();
	private final List<ExecutableSkillDummy> exInput = new ArrayList<>();
	
	//
	private String roleLibFilepath;
	private String transformationMappingPath;
	private Button exportAMLButton;
	private TableViewer currentResourceConditionTableViewer;
	private TableViewer possibleRExSkillTableViewer;
	private TableViewer possibleExSkillTableViewer;
	private Button testButton;
	private Button setPostRExButton;
	private Button setPostExButton;
	
	public DummyExecutableSkillsDialog(Shell parentShell) {
		super(parentShell);
		initResourceConditionMapping();
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		container.setLayout(GridLayoutFactory.fillDefaults().margins(5, 5).equalWidth(false).create());
		
		createExecutableSkillsExportComposite(container);
		createLowerComposite(container);
		
		return area;
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		validate();
	}
	
	private void createExecutableSkillsExportComposite(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		group.setLayout(GridLayoutFactory.fillDefaults().equalWidth(false).margins(4, 3).numColumns(2).create());
		group.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		group.setText("Scenario List");
		
		final ListViewer availableScenarioListViewer = new ListViewer(group);
		availableScenarioListViewer.getControl().setLayoutData(GridDataFactory.fillDefaults()
				.hint(500, 20).grab(true, true).create());
		availableScenarioListViewer.setContentProvider(new ArrayContentProvider());
		availableScenarioListViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((DummyScenario) element).getName();
			}
		});
		
		List<DummyScenario> scenarios = DummyScenario.getAllScenarios();
		
		availableScenarioListViewer.setInput(scenarios);
		if (scenarios.size() > 0) {
			chosenExSkillScenario = scenarios.get(0);
			availableScenarioListViewer.setSelection(new StructuredSelection(chosenExSkillScenario));
		}
		Composite buttonComposite = new Composite(group, SWT.NONE);
		buttonComposite.setLayout(GridLayoutFactory.fillDefaults().equalWidth(true).create());
		buttonComposite.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		
		createGroupRoleLibSelection(buttonComposite);
		createGroupDefaultMappingSelection(buttonComposite);
		
		exportAMLButton = new Button(group, SWT.PUSH);
		exportAMLButton.setText("Export AML");
		exportAMLButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean success = true;
				try {
					String filename = MasterFileDialog.saveFile(SupportedFileType.AML);
					DummyScenario.doExport(chosenExSkillScenario.getScenarioSkills(), roleLibFilepath, transformationMappingPath, filename);
				} catch (TransformerException e1) {
					success = false;
				}
				MessageBox messageBox = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.ICON_INFORMATION | SWT.YES);
				messageBox.setText("AML Export");
				if (success) {
					messageBox.setMessage("Scenario: " + chosenExSkillScenario.getName() + " has been successfully exported");
				} else {
					messageBox.setMessage("An error occured while exporting scenario " + chosenExSkillScenario.getName() + ".");
				}
			}
		});

		final Text goalskillText = new Text(group, SWT.BORDER | SWT.READ_ONLY);
		GridData textGridData = new GridData();
		textGridData.horizontalSpan = 2;
		textGridData.minimumWidth = 400;
		textGridData.widthHint = 400;
		goalskillText.setLayoutData(textGridData);

		availableScenarioListViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) availableScenarioListViewer.getSelection();
				chosenExSkillScenario = (DummyScenario) selection.getFirstElement();
				String goalSkills = "";
				for (String s : chosenExSkillScenario.getGoalSkills()) {
					if (!goalSkills.isEmpty()) {
						goalSkills += ",";
					}
					goalSkills += s;
				}
				goalskillText.setText(goalSkills);
			}
		});
	}
	
	private void createGroupRoleLibSelection(Composite parent) {
		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.verticalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		
		Group groupContext = new Group(parent, SWT.NONE);
		groupContext.setLayout(GridLayoutFactory.fillDefaults().numColumns(3).margins(4, 3).create());

		groupContext.setLayoutData(gridData);
		
		Button defaultOption = new Button(groupContext, SWT.RADIO);
		defaultOption.setLayoutData(GridDataFactory.fillDefaults().span(1, 1).create());
		defaultOption.setText("Default");
		
		final Text defaultText = new Text(groupContext, SWT.BORDER | SWT.READ_ONLY);
		defaultText.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).span(2, 1).create());
		defaultText.setText("Please fill this manually in code");
		roleLibFilepath = defaultText.getText();
		
		Button loadOption = new Button(groupContext, SWT.RADIO);
		loadOption.setLayoutData(GridDataFactory.fillDefaults().create());
		loadOption.setText("Load Role Lib");
		
		final Text loadText = new Text(groupContext, SWT.BORDER | SWT.READ_ONLY);
		loadText.setBackground(getShell().getDisplay().getSystemColor(SWT.COLOR_WHITE));
		loadText.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		
		final Button loadButton = new Button(groupContext, SWT.PUSH);
		loadButton.setText("...");
		loadButton.setLayoutData(GridDataFactory.fillDefaults().create());
		
		loadButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				roleLibFilepath = MasterFileDialog.getFilenameFromFileDialog(SupportedFileType.AML);
				loadText.setText(roleLibFilepath);
				validate();
			}
		});
		
		defaultOption.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				loadText.setEnabled(false);
				loadButton.setEnabled(false);
				roleLibFilepath = defaultText.getText();
				validate();
			}
		});
		
		loadOption.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				loadText.setEnabled(true);
				loadButton.setEnabled(true);
				roleLibFilepath = loadText.getText();
				validate();
			}
		});
		
		loadOption.setSelection(false);
		defaultOption.setSelection(true);
		loadText.setEnabled(false);
		loadButton.setEnabled(false);
	}
	
	private void createGroupDefaultMappingSelection(Composite parent) {
		Group groupContext = new Group(parent, SWT.NONE);
		
		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.verticalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupContext.setLayoutData(gridData);
		
		groupContext.setLayout(GridLayoutFactory.fillDefaults().numColumns(3).margins(4, 3).create());

		
		Button defaultOption = new Button(groupContext, SWT.RADIO);
		defaultOption.setLayoutData(GridDataFactory.fillDefaults().span(1, 1).create());
		defaultOption.setText("Default Mapping");
		
		final Text defaultText = new Text(groupContext, SWT.BORDER | SWT.READ_ONLY);
		defaultText.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).span(2, 1).create());
		defaultText.setText("DefaultMapping.xml");
		transformationMappingPath = defaultText.getText();
		
		Button loadOption = new Button(groupContext, SWT.RADIO);
		loadOption.setLayoutData(GridDataFactory.fillDefaults().create());
		loadOption.setText("Load Mapping");
		
		final Text loadText = new Text(groupContext, SWT.BORDER | SWT.READ_ONLY);
		loadText.setBackground(getShell().getDisplay().getSystemColor(
				SWT.COLOR_WHITE));
		loadText.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		
		final Button loadButton = new Button(groupContext, SWT.PUSH);
		loadButton.setText("...");
		loadButton.setLayoutData(GridDataFactory.fillDefaults().create());
		
		
		loadButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				transformationMappingPath = MasterFileDialog.getFilenameFromFileDialog(SupportedFileType.XML);
				loadText.setText(transformationMappingPath);
				validate();
			}
		});
		
		defaultOption.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				transformationMappingPath = defaultText.getText();
				loadText.setEnabled(false);
				loadButton.setEnabled(false);
				validate();
			}
		});
		
		loadOption.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				transformationMappingPath = loadText.getText();
				loadText.setEnabled(true);
				loadButton.setEnabled(true);
				validate();
			}
		});
		loadOption.setSelection(false);
		defaultOption.setSelection(true);
		loadText.setEnabled(false);
		loadButton.setEnabled(false);
	}
	
	private void createLowerComposite(Composite parent) {
		Composite context = new Composite(parent, SWT.NONE);
		context.setLayout(GridLayoutFactory.fillDefaults().equalWidth(false).margins(5, 5).create());
		context.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		createStatusComposite(context);
		
		createTestingComposite(context);
		
		currentResourceConditionTableViewer.setContentProvider(new ArrayContentProvider());
		currentResourceConditionTableViewer.setLabelProvider(new ResourceConditionTableLabelProvider());
		
		possibleRExSkillTableViewer.setContentProvider(new ArrayContentProvider());
		possibleRExSkillTableViewer.setLabelProvider(new ResourceExecutableSkillTableLabelProvider());
		
		possibleExSkillTableViewer.setContentProvider(new ArrayContentProvider());
		possibleExSkillTableViewer.setLabelProvider(new ExecutableSkillTableLabelProvider());
		
		refreshResourceConditionInput();
		
		currentResourceConditionTableViewer.setInput(resourceConditionInput);
		possibleRExSkillTableViewer.setInput(rexInput);
		possibleExSkillTableViewer.setInput(exInput);
	}
	
	private void refreshResourceConditionInput() {
		resourceConditionInput.clear();
		for (Entry<ResourceDummy, Condition> entry : currentConditionMapping.entrySet()) {
			resourceConditionInput.add(new Pair<>(entry.getKey(), entry.getValue()));
		}
		currentResourceConditionTableViewer.refresh();
	}
	
	private void createStatusComposite(Composite parent) {
		Composite context = new Composite(parent, SWT.NONE);
		context.setLayout(GridLayoutFactory.fillDefaults().equalWidth(false).numColumns(2).create());
		context.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		
		Label label = new Label(context, SWT.NONE);
		label.setLayoutData(GridDataFactory.fillDefaults().span(2, 1).create());
		label.setText("Current Resource Conditions");
		
		currentResourceConditionTableViewer = createTableViewer(createResourceConditionTable(context));
		
		testButton = new Button(context, SWT.PUSH);
		testButton.setText("PUSH TO TEST");
		testButton.setToolTipText("Shows which ResourceExecutableSkills and ExecutableSkills are available based on the current ResourceCondition");
		testButton.setLayoutData(GridDataFactory.fillDefaults().create());
		testButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				rexInput.clear();
				exInput.clear();
				
				List<ExecutableSkillDummy> scenario = chosenExSkillScenario.getScenarioSkills();
				Pair<List<ResourceExecutableSkillDummy>, List<ExecutableSkillDummy>> possibleSkills = testPossibleSkills(scenario);
				rexInput.addAll(possibleSkills.getFirstElement());
				exInput.addAll(possibleSkills.getSecondElement());
				
				refreshEverything();
			}
		});
		
		testButton.setEnabled(false);
	}
	
	private void createTestingComposite(Composite parent) {
		Composite context = new Composite(parent, SWT.NONE);
		context.setLayout(GridLayoutFactory.fillDefaults().equalWidth(true).numColumns(2).create());
		context.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		
		createRExSkillTestingComposite(context);
		createExSkillTestingComposite(context);
	}
	
	private void createRExSkillTestingComposite(Composite parent) {
		Composite context = new Composite(parent, SWT.NONE);
		context.setLayout(GridLayoutFactory.fillDefaults().equalWidth(true).create());
		context.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		
		Label label = new Label(context, SWT.NONE);
		label.setLayoutData(GridDataFactory.fillDefaults().span(1, 1).create());
		label.setText("Possible Resource Executable Skills");
		
		possibleRExSkillTableViewer = createTableViewer(createPossibleResourceExecutableSkillTable(context));
		
		setPostRExButton = new Button(context, SWT.PUSH);
		setPostRExButton.setText("Set Selected Post-Condition as Current");
		setPostRExButton.setToolTipText("Sets the post condition of the current ResourceExecutableSkill as the current ResourceCondition");
		setPostRExButton.setLayoutData(GridDataFactory.fillDefaults().create());
		setPostRExButton.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				ResourceExecutableSkillDummy rex = (ResourceExecutableSkillDummy) ((IStructuredSelection) possibleRExSkillTableViewer
						.getSelection()).getFirstElement();
				currentConditionMapping.put(rex.getResource(), rex.getPostCondition());
				rexInput.clear();
				exInput.clear();
				refreshEverything();
			}
		});
		setPostRExButton.setEnabled(false);
	}

	private void createExSkillTestingComposite(Composite parent) {
		Composite context = new Composite(parent, SWT.NONE);
		context.setLayout(GridLayoutFactory.fillDefaults().equalWidth(true).create());
		context.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		
		Label label = new Label(context, SWT.NONE);
		label.setLayoutData(GridDataFactory.fillDefaults().span(1, 1).create());
		label.setText("Possible Executable Skills");
		
		possibleExSkillTableViewer = createTableViewer(createPossibleExecutableSkillTable(context));

		setPostExButton = new Button(context, SWT.PUSH);
		setPostExButton.setText("Set Selected Post-Conditions as Current");
		setPostExButton.setToolTipText("Sets the post conditions of the current ExecutableSkill as the current ResourceCondition");
		setPostExButton.setLayoutData(GridDataFactory.fillDefaults().create());
		setPostExButton.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				ExecutableSkillDummy ex = (ExecutableSkillDummy) ((IStructuredSelection) possibleExSkillTableViewer
						.getSelection()).getFirstElement();
				//FIXME this will be a problem if there are multiple ResourceExecutableSkills of the same Resource
				//We won't know which one is supposed to be the last one executed.
				//Should check slack I guess?
				for (ResourceExecutableSkillDummy rex : ex.getDummies()) {
					currentConditionMapping.put(rex.getResource(), rex.getPostCondition());
				}
				rexInput.clear();
				exInput.clear();
				refreshEverything();
			}
		});
		setPostExButton.setEnabled(false);
	}
	
	private Table createResourceConditionTable(Composite composite) {
		int style = SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | 
				SWT.FULL_SELECTION | SWT.HIDE_SELECTION;
		Table table = new Table(composite, style);

		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		
		TableColumn column1 = new TableColumn(table, SWT.LEFT, 0);
		column1.setText("Resource");
		column1.setWidth(200);

		TableColumn column2 = new TableColumn(table, SWT.LEFT, 1);
		column2.setText("Pre-Configuration");
		column2.setWidth(200);
		
		TableColumn column3 = new TableColumn(table, SWT.LEFT, 2);
		column3.setText("Pre-Product");
		column3.setWidth(200);
		
		return table;
	}
	
	private Table createPossibleResourceExecutableSkillTable(Composite composite) {
		int style = SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | 
				SWT.FULL_SELECTION | SWT.HIDE_SELECTION;
		Table table = new Table(composite, style);

		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		
		TableColumn column1 = new TableColumn(table, SWT.LEFT, 0);
		column1.setText("Resource");
		column1.setWidth(100);

		TableColumn column2 = new TableColumn(table, SWT.LEFT, 1);
		column2.setText("ResourceExecutableSkill");
		column2.setWidth(160);
		
		TableColumn column3 = new TableColumn(table, SWT.LEFT, 2);
		column3.setText("Post-Configuration");
		column3.setWidth(130);
		
		TableColumn column4 = new TableColumn(table, SWT.LEFT, 3);
		column4.setText("Post-Product");
		column4.setWidth(90);
		
		return table;
	}
	
	private Table createPossibleExecutableSkillTable(Composite composite) {
		int style = SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | 
				SWT.FULL_SELECTION | SWT.HIDE_SELECTION;
		Table table = new Table(composite, style);

		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		
		TableColumn column1 = new TableColumn(table, SWT.LEFT, 0);
		column1.setText("ExecutableSkill");
		column1.setWidth(200);

		TableColumn column2 = new TableColumn(table, SWT.LEFT, 1);
		column2.setText("ResourceExecutableSkills");
		column2.setWidth(200);
		
		return table;
	}
	
	private TableViewer createTableViewer(Table table) {
		TableViewer tableViewer = new TableViewer(table);
		tableViewer.setUseHashlookup(true);
		tableViewer.getControl().setLayoutData(GridDataFactory.fillDefaults().hint(500, 160).grab(true, true).create());
		
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				validate();
			}
		});
		return tableViewer;
	}
	
	private void refreshEverything() {
		refreshResourceConditionInput();
		possibleExSkillTableViewer.refresh();
		possibleRExSkillTableViewer.refresh();
	}
	
	private void initResourceConditionMapping() {
		Condition defaultHumanWP1Condition = new Condition(ConditionConfiguration.NEUTRAL, ConditionProduct.BOX_UNFOLDED.toString());
		Condition defaultHumanWP2Condition = new Condition(ConditionConfiguration.NEUTRAL, ConditionProduct.BOX_UNFOLDED.toString());
		Condition defaultHumanWP3Condition = new Condition(ConditionConfiguration.NEUTRAL, ConditionProduct.BOX_UNFOLDED.toString());
		Condition defaultUR5Condition = new Condition(ConditionConfiguration.NEUTRAL, ConditionProduct.EMPTY.toString());
		Condition defaultUR5GripperCondition = new Condition(ConditionConfiguration.NEUTRAL, ConditionProduct.EMPTY.toString());
		Condition defaultKR6Condition = new Condition(ConditionConfiguration.NEUTRAL, ConditionProduct.EMPTY.toString());
		Condition defaultKR6GripperCondition = new Condition(ConditionConfiguration.NEUTRAL, ConditionProduct.EMPTY.toString());
		Condition defaultMobilePlatformCondition = new Condition(ConditionConfiguration.NEUTRAL, ConditionProduct.EMPTY.toString());
		Condition defaultSimulationCondition = new Condition(ConditionConfiguration.NEUTRAL, ConditionProduct.EMPTY.toString());
		
		currentConditionMapping.put(HMIScenario.HUMAN_WP1, defaultHumanWP1Condition);
		currentConditionMapping.put(HMIScenario.HUMAN_WP2, defaultHumanWP2Condition);
		currentConditionMapping.put(HMIScenario.HUMAN_WP3, defaultHumanWP3Condition);
		currentConditionMapping.put(HMIScenario.UR5, defaultUR5Condition);
		currentConditionMapping.put(HMIScenario.UR5_GRIPPER, defaultUR5GripperCondition);
		currentConditionMapping.put(HMIScenario.KR6, defaultKR6Condition);
		currentConditionMapping.put(HMIScenario.KR6_GRIPPER, defaultKR6GripperCondition);
		currentConditionMapping.put(HMIScenario.MOBILE_PLATFORM, defaultMobilePlatformCondition);
		currentConditionMapping.put(HMIScenario.SIMULATION, defaultSimulationCondition);
	}

	private Pair<List<ResourceExecutableSkillDummy>, List<ExecutableSkillDummy>> testPossibleSkills(List<ExecutableSkillDummy> dummyScenarios) {
		List<ResourceExecutableSkillDummy> possibleRexDummies = new ArrayList<>();
		List<ExecutableSkillDummy> possibleExDummies = new ArrayList<>();
		Pair<List<ResourceExecutableSkillDummy>, List<ExecutableSkillDummy>> possibleSkills = new Pair<>(possibleRexDummies, possibleExDummies);

		for (ExecutableSkillDummy exDummy : dummyScenarios) {
			boolean allTrue = true;
			for (ResourceExecutableSkillDummy rexDummy : exDummy.getDummies()) {
				ResourceDummy rexResource = rexDummy.getResource();
				Condition pair = currentConditionMapping.get(rexResource);
				System.out.println("pair: " + pair);
				if (pair.equals(rexDummy.getPreCondition())) {
					possibleRexDummies.add(rexDummy);
				} else {
					allTrue = false;
				}
			}
			if (allTrue) {
				possibleExDummies.add(exDummy);
			}
		}
		return possibleSkills;
	}
	
	private void validate() {
		if (roleLibFilepath != null && roleLibFilepath.endsWith(".aml") && transformationMappingPath != null
				&& transformationMappingPath.endsWith(".xml") && chosenExSkillScenario != null) {
			
			exportAMLButton.setEnabled(true);
		} else {
			exportAMLButton.setEnabled(false);
		}
		
		if (chosenExSkillScenario != null) {
			testButton.setEnabled(true);
		} else {
			testButton.setEnabled(false);
		}
		
		if (possibleRExSkillTableViewer.getSelection() != null
				&& ((IStructuredSelection) possibleRExSkillTableViewer.getSelection()).getFirstElement() != null) {
			setPostRExButton.setEnabled(true);
		} else {
			setPostRExButton.setEnabled(false);
		}
		
		if (possibleExSkillTableViewer.getSelection() != null
				&& ((IStructuredSelection) possibleExSkillTableViewer.getSelection()).getFirstElement() != null) {
			setPostExButton.setEnabled(true);
		} else {
			setPostExButton.setEnabled(false);
		}
	}
	
	private class ResourceConditionTableLabelProvider extends LabelProvider implements ITableLabelProvider{
		public String getColumnText(Object element, int columnIndex) {
			@SuppressWarnings("unchecked")
			Pair<ResourceDummy, Condition> dummy = (Pair<ResourceDummy, Condition>) element;
			switch (columnIndex) {
			case 0:
				return dummy.getFirstElement().getResourceId();
			case 1:
				return dummy.getSecondElement().getFirstElement().toString();
			case 2:
				return dummy.getSecondElement().getSecondElement().toString();
			default:
				return "";
			}
		}
		
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	}
	
	private class ResourceExecutableSkillTableLabelProvider extends LabelProvider implements ITableLabelProvider{
		public String getColumnText(Object element, int columnIndex) {
			ResourceExecutableSkillDummy rex = (ResourceExecutableSkillDummy) element;
			switch (columnIndex) {
			case 0:
				return rex.getResource().getResourceId();
			case 1:
				return rex.getName();
			case 2:
				return rex.getPostCondition().getFirstElement().toString();
			case 3:
				return rex.getPostCondition().getSecondElement().toString();
			default:
				return "";
			}
		}
		
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	}
	
	private class ExecutableSkillTableLabelProvider extends LabelProvider implements ITableLabelProvider{
		public String getColumnText(Object element, int columnIndex) {
			ExecutableSkillDummy ex = (ExecutableSkillDummy) element;
			switch (columnIndex) {
			case 0:
				return ex.getName();
			case 1:
				return Arrays.toString(ex.getDummies().toArray());
			default:
				return "";
			}
		}
		
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	}
}
