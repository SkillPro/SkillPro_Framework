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

package amltransformation.dialogs;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import masterviews.dialogs.MasterFileDialog;
import masterviews.util.SupportedFileType;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

import aml.amlparser.AMLParser;

public class ImportAMLDialog extends TitleAreaDialog {

	
	private static final String MISSING_INPUT_WARNING = "The following files are missing: \n";
	private static final String PARTS = "AML-Parts";
	private static final String PROJECT = "Project";
	
	private boolean consistsOfParts = true;
	private List librariesList;
	//filename, document
	private Map<String, String> librariesMap = new HashMap<>();
	private List projectsList;
	//filename, document
	private Map<String, String> projectsMap = new HashMap<>();
	//filename, references
	private Map<String, java.util.List<String>> externalReferencesFromLibraries = new HashMap<>();
	private Map<String, java.util.List<String>> externalReferencesFromProjects = new HashMap<>();
	
	//GUI
	private Group librariesGroup;
	private Button loadLibFromRemoteButton;
	private Button loadLibFromLocalButton;
	
	public ImportAMLDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	public void create() {
		super.create();
		setTitle("Import AML file");
	}
	
	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(GridDataFactory.fillDefaults().create());
		container.setLayout(GridLayoutFactory.fillDefaults().margins(5,5).equalWidth(false).numColumns(1).create());
		
		createButtonsComposite(container);
		createLibrariesComposite(container);
		createProjectsComposite(container);
		
		return area;
	}

	private void createButtonsComposite(Composite container) {
		Composite buttonsComposite = new Composite(container, SWT.NONE);
		buttonsComposite.setLayoutData(GridDataFactory.fillDefaults().grab(false, false).create());
		buttonsComposite.setLayout(GridLayoutFactory.fillDefaults().margins(5,5).equalWidth(false).numColumns(2).create());
		final Button partsButton = new Button(buttonsComposite, SWT.RADIO);
		partsButton.setText(PARTS);
		partsButton.setSelection(false);
		partsButton.setLayoutData(GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).create());
		
		final Button confButton = new Button(buttonsComposite, SWT.RADIO);
		confButton.setText(PROJECT);
		confButton.setSelection(true);
		confButton.setLayoutData(GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).create());
		
		partsButton.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				librariesGroup.setEnabled(true);
				librariesList.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
				loadLibFromLocalButton.setEnabled(true);
				loadLibFromRemoteButton.setEnabled(true);
				consistsOfParts = true;
			}
		});
		
		confButton.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				librariesGroup.setEnabled(false);
				librariesList.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_GRAY));
				loadLibFromLocalButton.setEnabled(false);
				loadLibFromRemoteButton.setEnabled(false);
				consistsOfParts = false;
				
			}
		});
	}
	
	private void createLibrariesComposite(Composite container) {
		librariesGroup = new Group(container, SWT.NONE);
		librariesGroup.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		librariesGroup.setLayout(GridLayoutFactory.fillDefaults().margins(5,5).equalWidth(false).numColumns(4).create());
		librariesGroup.setText("Libraries");
		
		librariesList = new List(librariesGroup,  SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		librariesList.setLayoutData(GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).span(4, 1).grab(true, false).create());
		
		librariesList.addKeyListener(new KeyAdapter() {
			
			@Override
			public void keyReleased(KeyEvent e) {
				int selectionIndex = librariesList.getSelectionIndex();
				if (e.keyCode == SWT.DEL && selectionIndex > -1) {
					String name = librariesList.getItem(selectionIndex);
					librariesList.remove(selectionIndex);
					librariesMap.remove(name);
					externalReferencesFromLibraries.remove(name);
				}
			}
		});
		
		loadLibFromLocalButton = new Button(librariesGroup,  SWT.PUSH);
		loadLibFromLocalButton.setLayoutData(GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).create());
		loadLibFromLocalButton.setText("Add Local Lib");
		loadLibFromLocalButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				File[] localFiles = MasterFileDialog.getLocalFilesFromFileDialog(SupportedFileType.AML);
				if (localFiles != null) {
					for (int i = 0; i < localFiles.length; i++) {
						String absolutePath = localFiles[i].getAbsolutePath();
						librariesList.add(absolutePath);
						String contentStringFromFile = AMLParser.getInstance().getContentStringFromFile(localFiles[i]);
						librariesMap.put(absolutePath, contentStringFromFile);
						java.util.List<String> references = AMLParser.getInstance().getExternalReferencesFromString(contentStringFromFile);
						java.util.List<String> actualReferences = new ArrayList<>();
						for (String ref : references) {
							String actualRef = getActualPath(getSubstringBeforeSlash(absolutePath), ref.replace("/", "\\"));
							actualReferences.add(actualRef);
						}
						externalReferencesFromLibraries.put(absolutePath, actualReferences);
						
					}
				}
			}
		});
		Label empty1 = new Label(librariesGroup, SWT.NONE);
	    empty1.setLayoutData(GridDataFactory.swtDefaults().create());
	    
	    loadLibFromRemoteButton = new Button(librariesGroup,  SWT.PUSH);
		loadLibFromRemoteButton.setLayoutData(GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).create());
		loadLibFromRemoteButton.setText("Add Remote Lib");
		loadLibFromRemoteButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ImportRemoteAMLDialog amlDialog = new ImportRemoteAMLDialog(Display.getDefault().getActiveShell());
				amlDialog.create();
				if (amlDialog.open() == Dialog.OK) {
					String amlInfo = amlDialog.getAmlInfo();
					librariesList.add(amlInfo);
					String amlContent = amlDialog.getAmlContent();
					librariesMap.put(amlInfo, amlContent);
					java.util.List<String> references = AMLParser.getInstance().getExternalReferencesFromString(amlContent);
					externalReferencesFromLibraries.put(amlInfo, references);
				}
			}
		});
		
		Label empty2 = new Label(librariesGroup, SWT.NONE);
	    empty2.setLayoutData(GridDataFactory.swtDefaults().create());
		
	    librariesGroup.setEnabled(false);
		librariesList.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_GRAY));
		loadLibFromLocalButton.setEnabled(false);
		loadLibFromRemoteButton.setEnabled(false);
		consistsOfParts = false;
	}
	
	private void createProjectsComposite(Composite container) {
		Group projectsGroup = new Group(container, SWT.NONE);
		projectsGroup.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		projectsGroup.setLayout(GridLayoutFactory.fillDefaults().margins(5,5).equalWidth(false).numColumns(4).create());
		projectsGroup.setText("Projects");
		
		projectsList = new List(projectsGroup,  SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		projectsList.setLayoutData(GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).span(4, 1).grab(true, false).create());
		
		projectsList.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
				int selectionIndex = projectsList.getSelectionIndex();
				if (e.keyCode == SWT.DEL && selectionIndex > -1) {
					String name = projectsList.getItem(selectionIndex);
					projectsList.remove(selectionIndex);
					projectsMap.remove(name);
					externalReferencesFromProjects.remove(name);
				}
				
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
			}
		});
		
		Button loadFromLocalButton = new Button(projectsGroup,  SWT.PUSH);
		loadFromLocalButton.setLayoutData(GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).create());
		loadFromLocalButton.setText("Add Local Project");
		loadFromLocalButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				File[] localFiles = MasterFileDialog.getLocalFilesFromFileDialog(SupportedFileType.AML);
				if (localFiles != null) {
					for (int i = 0; i < localFiles.length; i++) {
						String absolutePath = localFiles[i].getAbsolutePath();
						projectsList.add(absolutePath);
						String contentStringFromFile = AMLParser.getInstance().getContentStringFromFile(localFiles[i]);
						projectsMap.put(absolutePath, contentStringFromFile);
						java.util.List<String> references = AMLParser.getInstance().getExternalReferencesFromString(contentStringFromFile);
						java.util.List<String> actualReferences = new ArrayList<>();
						for (String ref : references) {
							String actualRef = getActualPath(getSubstringBeforeSlash(absolutePath), ref.replace("/", "\\"));
							actualReferences.add(actualRef);
						}
						externalReferencesFromProjects.put(absolutePath, actualReferences);
					}
					
				}
			}
		});
		Label empty1 = new Label(projectsGroup, SWT.NONE);
	    empty1.setLayoutData(GridDataFactory.swtDefaults().create());
	    
	    Button loadFromRemoteButton = new Button(projectsGroup,  SWT.PUSH);
		loadFromRemoteButton.setLayoutData(GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).create());
		loadFromRemoteButton.setText("Add Remote Project");
		loadFromRemoteButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ImportRemoteAMLDialog amlDialog = new ImportRemoteAMLDialog(Display.getDefault().getActiveShell());
				amlDialog.create();
				if (amlDialog.open() == Dialog.OK) {
					String amlInfo = amlDialog.getAmlInfo();
					projectsList.add(amlInfo);
					String amlContent = amlDialog.getAmlContent();
					projectsMap.put(amlInfo, amlContent);
					java.util.List<String> references = AMLParser.getInstance().getExternalReferencesFromString(amlContent);
					externalReferencesFromProjects.put(amlInfo, references);
				}
			}
		});
		
		Label empty2 = new Label(projectsGroup, SWT.NONE);
	    empty2.setLayoutData(GridDataFactory.swtDefaults().create());
		
		
	}
	
	private boolean checkMissingInput() {
		java.util.List<String> neededLibs = getNeededLibs();
		String neededLibsString = "";
		for (String lib : neededLibs) {
			neededLibsString = neededLibsString + lib + "\n";
		}
		
		if (!neededLibs.isEmpty()) {
			setErrorMessage(MISSING_INPUT_WARNING + neededLibsString);
			return false;
		}
		
		return true;
	}
	
	private boolean checkInput() {
		if ((consistsOfParts && librariesMap.isEmpty())
			|| (!consistsOfParts && projectsMap.isEmpty())) {
			setErrorMessage("There are missing inputs. + consistsofparts" + consistsOfParts);
			return false;
		}
		return true;
	}
	
	private java.util.List<String> getSortedLibraries() {
		return sortLibraries(new ArrayList<String>(), externalReferencesFromLibraries);
	}
	
	private java.util.List<String> sortLibraries(java.util.List<String> referencedList, Map<String, java.util.List<String>> referenceMap) {
		java.util.List<String> result = new ArrayList<>();
		
		Map<String, java.util.List<String>> updatedMap = new HashMap<>();
		for (String entry : referenceMap.keySet()) {
			java.util.List<String> references = new ArrayList<>();
			
			if (!referencedList.contains(entry)) {
				references.addAll(referenceMap.get(entry));
				for (String referenced : referencedList) {
					references.remove(referenced);
				}
				updatedMap.put(entry, references);
			}
		}
		
		for (String entry : updatedMap.keySet()) {
			if (updatedMap.get(entry).isEmpty()) {
				result.add(entry);
			}
		}
		
		
		if (!updatedMap.isEmpty()) {
			result.addAll(sortLibraries(result, updatedMap));
				
		}
		
	
		return result;
	}
	
	private java.util.List<String> getNeededLibs() {
		java.util.List<String> result = new ArrayList<>();
		for (String path : externalReferencesFromLibraries.keySet()) {
			for (String refPath : externalReferencesFromLibraries.get(path)) {
				
				if (!librariesMap.keySet().contains(refPath)) {
					result.add(refPath);
				} 
			}
		}
		
		return result;
	}
	
	private String getActualPath(String currentPath, String refPath) {
		if (refPath.startsWith("..\\")) {
			String actualPath = getSubstringBeforeSlash(currentPath);
			return getActualPath(actualPath, getSubstringAfterSlash(refPath));
		} else if (refPath.contains(":")) {
			return refPath;
		} else if (!currentPath.endsWith("\\")) {
			return currentPath + "\\" + refPath;
		} else {
			return currentPath + refPath;
			
		}
		
	}

	private String getSubstringBeforeSlash(String path) {
		int index = 0;
		//path.length() - 1 so that it won't include the last \ -> ...\Libs\ 
		for (int i = 0; i < path.length() - 1; i++) {
			if (path.charAt(i) == '/' || path.charAt(i) == '\\') {
				index = i;
			}
		}
		if (index == 0) {
			index = path.length();
		}
		//includes the / or \
		return path.substring(0, index + 1);
	}
	
	private String getSubstringAfterSlash(String path) {
		int index = 0;
		for (int i = 0; i < path.length(); i++) {
			if (path.charAt(i) == '/' || path.charAt(i) == '\\') {
				index = i;
				break;
			}
		}
		return path.substring(index + 1, path.length());
	}
	
	@Override
	protected void okPressed() {
		if (checkMissingInput() && checkInput()) {
			super.okPressed();
		}
	}
	
	public java.util.List<String> getListOfLibraries() {
		java.util.List<String> libList = new ArrayList<String>();
		for (String libPath : getSortedLibraries()) {
			libList.add(librariesMap.get(libPath));
		}
		return libList;
	}
	
	public java.util.List<String> getListOfProjects() {
		java.util.List<String> projectsList = new ArrayList<String>();
		projectsList.addAll(projectsMap.values());
		return projectsList;
	}

	public Map<String, String> getProjectsMap() {
		return projectsMap;
	}
	
	public boolean hasParts() {
		return consistsOfParts;
	}
}
