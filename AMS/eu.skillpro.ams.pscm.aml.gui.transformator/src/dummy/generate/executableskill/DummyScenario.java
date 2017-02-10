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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.transform.TransformerException;

import nu.xom.Document;
import nu.xom.ParsingException;
import skillpro.model.skills.dummy.Condition;
import skillpro.model.skills.dummy.ExecutableSkillDummy;
import skillpro.model.skills.dummy.ResourceExecutableSkillDummy;
import aml.amlparser.AMLExporter;
import aml.amlparser.AMLParser;
import aml.domain.Interface;
import aml.domain.InternalElement;
import aml.domain.Role;
import aml.domain.SystemUnit;
import aml.model.Hierarchy;
import aml.model.Root;
import aml.skillpro.mapping.TransformationMappingParser;
import aml.skillpro.transformation.adapters.template.TransformableAdapterTemplate;
import aml.skillpro.transformer.ReverseTransformer;
import aml.transformation.service.AMLTransformationService;

public abstract class DummyScenario{
	
	protected List<ExecutableSkillDummy> executableSkillList = null;
	
	public abstract String getName();
	
	public abstract String getSuffix();
	
	public abstract List<String> getGoalSkills();
	
	public static List<DummyScenario> getAllScenarios() {
		return Arrays.asList(
				FullOrderScenario.getInstance(),
				OnlyChocolateOrderScenario.getInstance(),
				KR6OnlyOrderScenario.getInstance(),
				UR5OnlyOrderScenario.getInstance(),
				DDEScenarioA.getInstance(),
				DDEScenarioB.getInstance()
		);
	}
	
	public static void main(String[] args) {
		List<DummyScenario> scenarios = DDEScenario.getAllScenarios();
//		Set<String> templateSkills = new HashSet<>();
//		for (DummyScenario s : scenarios) {
//			for (ExecutableSkillDummy e : s.getScenarioSkills()) {
//				for (ResourceExecutableSkillDummy re : e.getDummies()) {
//					templateSkills.add(re.getTemplateSkill());
//				}
//			}
//		}
//		for (String skill : templateSkills) {
//			System.out.println(skill);
//		}
//		for (DummyScenario s : scenarios) {
//			s.check();
//		}
		try {
			System.out.println("Creating html skill overview ...");
			new DummyScenarioHtmlReport(scenarios).save();
		} catch (FileNotFoundException e) {
			System.out.println("Couldn't save the overview file.");
		}
//		new DummyScenarioPlanner(DDEScenarioA.getInstance()).print();
	}
	
	@SuppressWarnings("unchecked")
	public static void doExport(List<ExecutableSkillDummy> toExport, String roleLibPath, String transformationMappingPath, String exportedFilePath) {
		List<String> amlLibs = new ArrayList<>();
		amlLibs.add(AMLParser.getInstance().getContentStringFromFile(roleLibPath));
		AMLParser.getInstance().wipeData();
		for (String input : amlLibs) {
			AMLParser.getInstance().parseAMLLibrariesFromString(input);
		}
		Set<Role> roles = new HashSet<>();
		Set<Hierarchy<InternalElement>> hierarchies = new HashSet<>();
		List<Hierarchy<Role>> roleHierarchies = new ArrayList<>();
		List<Hierarchy<Interface>> interfaceHierarchies = new ArrayList<>();
		Set<Object> parsedObjects = AMLParser.getInstance().getParsedObjects();
		for (Object obj : parsedObjects) {
			if (obj instanceof Role) {
				roles.add((Role) obj);
			} else if (obj instanceof Hierarchy<?>) {
				if (((Hierarchy<?>) obj).getElement() instanceof InternalElement) {
					hierarchies.add((Hierarchy<InternalElement>) obj);
				} else if (((Hierarchy<?>) obj).getElement() instanceof Role) {
					roleHierarchies.add((Hierarchy<Role>) obj);
				} else if (((Hierarchy<?>) obj).getElement() instanceof Interface) {
					interfaceHierarchies.add((Hierarchy<Interface>) obj);
				}
			}
		}
		parseTransformationMapping(transformationMappingPath, roles, hierarchies);
		// ExecutableSkillDummy dummy = createBoxFoldingExecutableSkill();
		reverseTransform(toExport, roleHierarchies, interfaceHierarchies);
		// add reverse transformed object
		Root<InternalElement> defaultRoot = new Root<InternalElement>("Configuration", InternalElement.class);
		parsedObjects.add(defaultRoot);
		Collection<Object> objects = AMLTransformationService.getTransformationProvider().getTransformationRepo().getReverseTransformedObjectsMap().values();
		for (Object obj : objects) {
			if (obj instanceof Interface) {
				parsedObjects.add((Interface) obj);
			} else if (obj instanceof Role) {
				parsedObjects.add((Role) obj);
			} else if (obj instanceof InternalElement) {
				parsedObjects.add((InternalElement) obj);
			} else if (obj instanceof SystemUnit) {
				parsedObjects.add((SystemUnit) obj);
			} else if (obj instanceof Root<?>) {
				parsedObjects.add((Root<?>) obj);
			} else if (obj instanceof Hierarchy<?>) {
				Hierarchy<?> hie = (Hierarchy<?>) obj;
				if (hie.getElement() instanceof InternalElement && hie.getParent() == null) {
					defaultRoot.addChild((Hierarchy<InternalElement>) hie);
				}
				parsedObjects.add(hie);
			} else {
				// do nothing
			}
		}
		Document amlDoc = AMLExporter.getExportedAsDoc(parsedObjects, true);
		// amlDoc = AMLExporter.getInternalElementsOnly(amlDoc);
		try {
			System.out.println("SNIPPET: " + AMLExporter.getExportedAsString(amlDoc).replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", ""));
			AMLExporter.saveFile(exportedFilePath, AMLExporter.getInstanceHierarchiesOnly(amlDoc));
		} catch (TransformerException | IOException e) {
			// todo Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected abstract List<ExecutableSkillDummy> getScenarioSkills();
	
	private static void parseTransformationMapping(String transformationMappingPath, Set<Role> roles, Set<Hierarchy<InternalElement>> hierarchies) {
		try {
			System.out.println("Roles: " + roles);
			System.out.println("Hierarchies: " + hierarchies);
			TransformationMappingParser.loadTransformationMapping(transformationMappingPath, roles, hierarchies);
		} catch (ParsingException | IOException e) {
			e.printStackTrace();
			AMLParser.getInstance().wipeData();
			AMLTransformationService.getTransformationProvider().wipeAllData();
			throw new IllegalArgumentException("Transformation didn't function correctly");
		}
	}
	
	private static void reverseTransform(List<ExecutableSkillDummy> executableSkills, List<Hierarchy<Role>> roleHierarchies, List<Hierarchy<Interface>> interfaceHierarchies) {
		// Set role and interface hierarchies before performing reverse
		// transformation
		TransformableAdapterTemplate.setCurrentRoleHierarchies(roleHierarchies);
		TransformableAdapterTemplate.setCurrentInterfaceHierarchies(interfaceHierarchies);
		// reverse transform SEE
		ReverseTransformer.getInstance().reverseTransformExecutableSkillDummies(executableSkills);
		// REVERT
		TransformableAdapterTemplate.revertEverything();
	}
	
	protected List<String> addIDSuffixToGoalskills(List<String> skills) {
		String suffix = "-" + getSuffix();
		for (int i = 0; i < skills.size(); i++) {
			skills.set(i, skills.get(i) + suffix);
		}
		return skills;
	}
	
	protected List<ExecutableSkillDummy> addIDSuffixToSkills(List<ExecutableSkillDummy> skills) {
		String suffix = "-" + getSuffix();
		for (ExecutableSkillDummy e : skills) {
			e.setId(e.getId() + suffix);
			e.setName(e.getName() + suffix);
			for (ResourceExecutableSkillDummy re : e.getDummies()) {
				re.setId(re.getId() + suffix);
				re.setName(re.getName() + suffix);
			}
		}
		return skills;
	}
	
	protected void check() {
		checkIDs(getScenarioSkills());
		checkConditions(getScenarioSkills());
		if (new DummyScenarioPlanner(this).reachedGoalSkill()) {
			System.out.println("Reached a GoalSkill.");
		} else {
			System.out.println("Couldn't reach a GoalSkill.");
		}
	}
	
	protected void checkIDs(List<ExecutableSkillDummy> list) {
		System.out.println("\n\n" + getName() + ": Finding duplicate ids ...");
		Set<String> ids = new HashSet<>();
		Set<String> names = new HashSet<>();
		Set<String> idsREx = new HashSet<>();
		Set<String> namesREx = new HashSet<>();
		for (ExecutableSkillDummy e : list) {
			if (ids.contains(e.getId())) {
				System.out.println("\tDuplicate id: " + e.getId());
			} else {
				ids.add(e.getId());
			}
			if (names.contains(e.getName())) {
				System.out.println("\tDuplicate name: " + e.getName());
			} else {
				names.add(e.getName());
			}
			
			for (ResourceExecutableSkillDummy re : e.getDummies()) {
				if (idsREx.contains(re.getId())) {
//					System.out.println("Duplicate REx id: " + e.getId() + " => " + re.getId());
				} else {
					idsREx.add(re.getId());
				}
				if (namesREx.contains(re.getName())) {
					System.out.println("\tDuplicate REx name: " + e.getId() + " => " + re.getName());
				} else {
					namesREx.add(re.getName());
				}
			}
		}
	}
	
	protected void checkConditions(List<ExecutableSkillDummy> list) {
		Set<Condition> preConditions = new HashSet<>();
		Set<Condition> postConditions = new HashSet<>();
		
		for (ExecutableSkillDummy e : list) {
			for (ResourceExecutableSkillDummy re : e.getDummies()) {
				Condition pre = re.getPreCondition();
				Condition post = re.getPostCondition();
				if (pre == null) {
					System.out.println("null Pre-Condition: " + re.getName());
				}
				if (post == null) {
					System.out.println("null Post-Condition: " + re.getName());
				}
				preConditions.add(pre);
				postConditions.add(post);
			}
		}
		
		Set<Condition> preNoPost = new HashSet<>(preConditions);
		preNoPost.removeAll(postConditions);
		
		Set<Condition> postNoPre = new HashSet<>(postConditions);
		postNoPre.removeAll(postConditions);
		
		System.out.println(getName() + ": Checking " + preConditions.size() + " pre-conditions and " + postConditions.size() + " post-conditions ...");
		
		if (!preNoPost.isEmpty()) {
			System.out.println("\t" + preNoPost.size() + " unmatched pre-conditions!");
			for (Condition pre : preNoPost) {
				System.out.println("\t" + pre.getFirstElement() + "\t" + pre.getSecondElement());
			}
		}
		if (!postNoPre.isEmpty()) {
			System.out.println("\t" + preNoPost.size() + " unmatched pre-conditions!");
			for (Condition post : postNoPre) {
				System.out.println("\t" + post.getFirstElement() + "\t" + post.getSecondElement());
			}
		}
		System.out.println("\tdone");
	}
}