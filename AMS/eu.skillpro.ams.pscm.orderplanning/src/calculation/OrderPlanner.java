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

package calculation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import skillpro.model.assets.Resource;
import skillpro.model.assets.ResourceConfiguration;
import skillpro.model.products.Order;
import skillpro.model.products.ProductConfiguration;
import skillpro.model.service.SkillproService;
import skillpro.model.skills.ExecutableSkill;
import skillpro.model.skills.PrePostRequirement;
import skillpro.model.skills.ProductionSkill;
import skillpro.model.skills.Requirement;
import skillpro.model.skills.ResourceExecutableSkill;
import skillpro.model.skills.ResourceSkill;
import skillpro.model.skills.SkillSynchronizationType;
import skillpro.model.utils.Pair;
import calculation.model.Condition;
import calculation.model.SkillRequirementSingleEntity;
import calculation.model.path.ResourceSkillPath;
import calculation.model.path.ResourceSkillPathContent;
import calculation.model.path.SkillStep;
import calculation.model.path.StatePath;
import calculation.model.state.MiniState;
import calculation.util.ProductionPath;
import calculation.util.ProductionSkillNode;
import calculation.util.ProductionSkillTree;
import calculation.util.Utility;

public class OrderPlanner {
	private final static OrderPlanner INSTANCE = new OrderPlanner();
	
	private OrderPlanner() {
		
	}
	
	public static OrderPlanner getInstance() {
		return INSTANCE;
	}
	
	// this is the main method that we'll use to generate ExecutableSkills
	public static Pair<List<ExecutableSkill>, Map<Order, Set<List<ExecutableSkill>>>> generatePossibleExecutableSkills(List<Order> orders, MiniState currentState) {
		currentState.exploreAndUpdateStates();
		Map<Order, Set<List<ExecutableSkill>>> allPossibilities = new HashMap<>();
		System.out.println("=============================================");
		System.out.println("===================GENERATING EXSKILLS=================");
		System.out.println("=============================================");
		List<ExecutableSkill> allExecutableSkills = new ArrayList<>();
		long before = System.currentTimeMillis();
		for (Order order : orders) {
			ProductionSkillTree orderTree = INSTANCE.createProductionTree(order);

			boolean foundProductionPaths = false;
			Set<List<ExecutableSkill>> executableSkillsPerOrder = new HashSet<>();
			for (ProductionPath possiblePath : orderTree.getPossibleProductionPaths()) {
				foundProductionPaths = true;
				// currentState = a time where no executableSkills have been
				// executed yet.
				for (Pair<Map<ProductionSkill, MiniState>, ResourceSkillPath> pathPair : INSTANCE.extractResourceSkillPath(currentState, possiblePath.getProductionNodes())) {
					Pair<Set<ExecutableSkill>, Set<List<ExecutableSkill>>> createdExecutableSkills = INSTANCE.createExecutableSkills(currentState, pathPair, possiblePath.getProductionNodes());
					for (ExecutableSkill exSkill : createdExecutableSkills.getFirstElement()) {
						if (!allExecutableSkills.contains(exSkill)) {
							allExecutableSkills.add(exSkill);
						}
					}
					Set<List<ExecutableSkill>> createdOrderedExecutableSkills = createdExecutableSkills.getSecondElement();
					executableSkillsPerOrder.addAll(createdOrderedExecutableSkills);
				}
			}
			allPossibilities.put(order, executableSkillsPerOrder);

			if (!foundProductionPaths) {
				//add looped skills
				for (ResourceSkillPath loopedPath : currentState.getLoopedStatePath()) {
					for (ExecutableSkill exSkill : INSTANCE.createExecutableSkills(currentState, loopedPath)) {
						if (!allExecutableSkills.contains(exSkill)) {
							allExecutableSkills.add(exSkill);
						}
					}
				}
			}
		}
		System.err.println("After Generating: " + (System.currentTimeMillis() - before));
		
		for (ExecutableSkill exSkill : allExecutableSkills) {
			int id = 0;
			for (ExecutableSkill existingExSkill : allExecutableSkills) {
				if (existingExSkill.getName().equals(exSkill.getName())) {
					id++;
					if (existingExSkill.getId().equals(exSkill.getId())) {
						break;
					}
				}
			}
			exSkill.setId(exSkill.getId() + "_" + id);
		}
		return new Pair<>(allExecutableSkills, allPossibilities);
	}
	
	private Pair<Set<ExecutableSkill>, Set<List<ExecutableSkill>>> createExecutableSkills(MiniState currentState,
			Pair<Map<ProductionSkill, MiniState>, ResourceSkillPath> executionPathPair,
			List<ProductionSkillNode> productionNodes) {
		Set<List<ExecutableSkill>> orderedExecutableSkills = new HashSet<>();
		Set<ExecutableSkill> exSkills = new HashSet<>();

		MiniState realPreviousState = currentState;
		
		Iterator<ProductionSkillNode> iterator = productionNodes.iterator();
		
		ProductionSkill currentProductionSkill = iterator.next().getProductionSkill();
		MiniState currentCriticalState = executionPathPair.getFirstElement().get(currentProductionSkill);
		
		List<ExecutableSkill> executablePath = new ArrayList<>();
		
		for (SkillStep step : executionPathPair.getSecondElement().getSteps()) {
			ResourceSkillPathContent pathContent = step.getSecondElement();
			MiniState realNextState = step.getFirstElement();
			ResourceSkill mainResourceSkill = pathContent.getMainResourceSkill();
			Set<ResourceExecutableSkill> resourceExecutableSkills = new HashSet<>();
			for (SkillRequirementSingleEntity single : pathContent.getSinglesList()) {
				ResourceSkill contentResourceSkill = single.getContent();
				Resource contentResource = contentResourceSkill.getResource();
				Condition previousStateMap = realPreviousState
						.getCurrentStateMap().get(contentResource);
				Condition nextStateMap = realNextState
						.getCurrentStateMap().get(contentResource);
				ResourceConfiguration preRConf = previousStateMap.getFirstElement();
				ResourceConfiguration postRConf = nextStateMap.getFirstElement();
				ProductConfiguration prePConf = previousStateMap.getSecondElement();
				ProductConfiguration postPConf = nextStateMap.getSecondElement();
				//syncType
				SkillSynchronizationType syncType = null;
				for (PrePostRequirement requirement : mainResourceSkill.getPrePostRequirements()) {
					Requirement preRequirement = requirement.getPreRequirement();
					if (preRequirement.getRequiredResourceSkill().equals(contentResourceSkill)) {
						syncType = preRequirement.getSyncType();
						break;
					}
				}
				if (realNextState.equals(currentCriticalState)) {
					ResourceExecutableSkill rexSkill;
					if (SkillproService.getSkillproProvider().getResourceSkillRepo().getPossibleResourceSkills(currentProductionSkill).contains(contentResourceSkill)) {
						rexSkill = createResourceExecutableSkill(currentProductionSkill, 
								contentResourceSkill, preRConf, postRConf, prePConf, postPConf, syncType);
					} else {
						rexSkill = createResourceExecutableSkill(contentResourceSkill,
								preRConf, postRConf, prePConf, postPConf, syncType);
					}
					resourceExecutableSkills.add(rexSkill);
					if (iterator.hasNext()) {
						currentProductionSkill = iterator.next().getProductionSkill();
						currentCriticalState = executionPathPair.getFirstElement().get(currentProductionSkill);
					} else {
						currentProductionSkill = null;
						currentCriticalState = null;
					}
				} else {
					ResourceExecutableSkill rexSkill = createResourceExecutableSkill(contentResourceSkill,
							preRConf, postRConf, prePConf, postPConf, syncType);
					resourceExecutableSkills.add(rexSkill);
				}
			}
			for (ResourceSkillPath loopExecutionPath : realNextState.getLoopedStatePath()) {
				for (ExecutableSkill exSkill : INSTANCE.createExecutableSkills(realNextState, loopExecutionPath)) {
					if (!exSkills.contains(exSkill)) {
						exSkills.add(exSkill);
					}
				}
			}
			realPreviousState = realNextState;
			
			if (!resourceExecutableSkills.isEmpty()) {
				String name = "EX_" + mainResourceSkill;
				name = name.replace(" ", "_");
				ExecutableSkill exSkill = new ExecutableSkill(name,
						null, resourceExecutableSkills);
				executablePath.add(exSkill);
				exSkill.setId(exSkill.getName());
				exSkills.add(exSkill);
			}
			
		}
		
		orderedExecutableSkills.add(executablePath);
		return new Pair<Set<ExecutableSkill>, Set<List<ExecutableSkill>>>(exSkills, orderedExecutableSkills);
	}
	
	private Set<ExecutableSkill> createExecutableSkills(MiniState currentState, ResourceSkillPath executionPath) {
		Set<ExecutableSkill> exSkills = new HashSet<>();

		MiniState realPreviousState = currentState;
		
		for (SkillStep step : executionPath.getSteps()) {
			ResourceSkillPathContent pathContent = step.getSecondElement();
			MiniState realNextState = step.getFirstElement();
			ResourceSkill mainResourceSkill = pathContent.getMainResourceSkill();
			Set<ResourceExecutableSkill> resourceExecutableSkills = new HashSet<>();
			for (SkillRequirementSingleEntity single : pathContent.getSinglesList()) {
				ResourceSkill contentResourceSkill = single.getContent();
				Resource contentResource = contentResourceSkill.getResource();
				Condition previousStateMap = realPreviousState
						.getCurrentStateMap().get(contentResource);
				Condition nextStateMap = realNextState
						.getCurrentStateMap().get(contentResource);
				ResourceConfiguration preRConf = previousStateMap.getFirstElement();
				ResourceConfiguration postRConf = nextStateMap.getFirstElement();
				ProductConfiguration prePConf = previousStateMap.getSecondElement();
				ProductConfiguration postPConf = nextStateMap.getSecondElement();
				//sync type
				SkillSynchronizationType syncType = null;
				for (PrePostRequirement requirement : mainResourceSkill.getPrePostRequirements()) {
					Requirement preRequirement = requirement.getPreRequirement();
					if (preRequirement.getRequiredResourceSkill().equals(contentResourceSkill)) {
						syncType = preRequirement.getSyncType();
						break;
					}
				}
				ResourceExecutableSkill rexSkill = createResourceExecutableSkill(contentResourceSkill,
						preRConf, postRConf, prePConf, postPConf, syncType);
				resourceExecutableSkills.add(rexSkill);
			}
			realPreviousState = realNextState;
			if (!resourceExecutableSkills.isEmpty()) {
				String name = "EX_" + mainResourceSkill;
				name = name.replace(" ", "_");
				ExecutableSkill exSkill = new ExecutableSkill(name,
						null, resourceExecutableSkills);
				exSkill.setId(exSkill.getName());
				exSkills.add(exSkill);
			}
			
		}
		
		return exSkills;
	}
	
	private ResourceExecutableSkill createResourceExecutableSkill(ResourceSkill resourceSkill,
			ResourceConfiguration preRConf, ResourceConfiguration postRConf,
			ProductConfiguration prePConf, ProductConfiguration postPConf, SkillSynchronizationType syncType) {
		//no property designators for now
		String name = "REX_" + resourceSkill.getName();
		name = name.replace(" ", "_");
		return new ResourceExecutableSkill(name,
				resourceSkill, preRConf, postRConf, prePConf, postPConf,
				resourceSkill.getEmptyAndFilledDesignators(), 0, 0, syncType);
	}
	
	private ResourceExecutableSkill createResourceExecutableSkill(ProductionSkill productionSkill,
			ResourceSkill resourceSkill, ResourceConfiguration preRConf, ResourceConfiguration postRConf,
			ProductConfiguration prePConf, ProductConfiguration postPConf, SkillSynchronizationType syncType) {
		//no property designators for now
		String name = "REX_" + resourceSkill.getName();
		name = name.replace(" ", "_");
		return new ResourceExecutableSkill(name,
				resourceSkill, preRConf, postRConf, prePConf, postPConf,
				productionSkill.getEmptyAndFilledDesignators(), 0, 0, syncType);
	}
	
	private ProductionSkillTree createProductionTree(Order order) {
		return new ProductionSkillTree(order.getProductQuantity().getProduct());
	}
	
	private Set<Pair<Map<ProductionSkill, MiniState>, ResourceSkillPath>> extractResourceSkillPath(MiniState initialState, 
			List<ProductionSkillNode> orderedProductionSkillNodes) {
		Set<Pair<Map<ProductionSkill, MiniState>, ResourceSkillPath>> resourceSkillPaths = new HashSet<>();
		Map<MiniState, Set<Pair<Map<ProductionSkill, MiniState>, List<MiniState>>>> currentResourceSkillPathsMapping = new HashMap<>();
		currentResourceSkillPathsMapping.put(initialState, new HashSet<Pair<Map<ProductionSkill, MiniState>, List<MiniState>>>());
		for (ProductionSkillNode psn : orderedProductionSkillNodes) {
			Map<MiniState, Set<Pair<Map<ProductionSkill, MiniState>, List<MiniState>>>> nextResourceSkillPathsMapping = new HashMap<>();
			for (Entry<MiniState, Set<Pair<Map<ProductionSkill, MiniState>, List<MiniState>>>> entry : currentResourceSkillPathsMapping.entrySet()) {
				MiniState currentState = entry.getKey();
				Set<List<MiniState>> allPossibleSteps = getAllPossibleSteps(currentState, psn.getProductionSkill());
				for (List<MiniState> orderedSteps : allPossibleSteps) {
					MiniState lastState = orderedSteps.get(orderedSteps.size() - 1);
					if (lastState.equals(currentState)) {
						throw new IllegalArgumentException("Loops detected!");
					}
					Set<Pair<Map<ProductionSkill, MiniState>, List<MiniState>>> nextResourceSkillPaths = nextResourceSkillPathsMapping
							.get(lastState);
					if (nextResourceSkillPaths == null) {
						nextResourceSkillPaths =  new HashSet<>();
						nextResourceSkillPathsMapping.put(lastState, nextResourceSkillPaths);
					}
					
					if (entry.getValue().isEmpty()) {
						Map<ProductionSkill, MiniState> criticalProductionPointMap = new HashMap<>();
						criticalProductionPointMap.put(psn.getProductionSkill(), lastState);
						nextResourceSkillPaths.add(new Pair<Map<ProductionSkill, MiniState>, List<MiniState>>(criticalProductionPointMap,
								new ArrayList<MiniState>(orderedSteps)));
					} else {
						for (Pair<Map<ProductionSkill, MiniState>, List<MiniState>> previousPathPair : entry.getValue()) {
							List<MiniState> newPath = new ArrayList<>(previousPathPair.getSecondElement());
							Map<ProductionSkill, MiniState> criticalProductionPointMap = new HashMap<>(previousPathPair.getFirstElement());
							criticalProductionPointMap.put(psn.getProductionSkill(), lastState);
							newPath.addAll(orderedSteps);
							nextResourceSkillPaths.add(new Pair<>(criticalProductionPointMap, newPath));
						}
					}
					
					
				}
			}
			currentResourceSkillPathsMapping = nextResourceSkillPathsMapping;
		}
		for (Entry<MiniState, Set<Pair<Map<ProductionSkill, MiniState>, List<MiniState>>>> entry : currentResourceSkillPathsMapping.entrySet()) {
			for (Pair<Map<ProductionSkill, MiniState>, List<MiniState>> pathPair : entry.getValue()) {
				for (ResourceSkillPath resourceSkillPath : Utility.convertToResourceSkillPath(initialState, pathPair.getSecondElement())) {
					resourceSkillPaths.add(new Pair<>(pathPair.getFirstElement(), resourceSkillPath));
				}
			}
		}
		
		return resourceSkillPaths;
	}
	
	private Set<List<MiniState>> getAllPossibleSteps(MiniState currentState, ProductionSkill productionSkill) {
		Set<List<MiniState>> allPossibleSteps = new HashSet<>();
		
		for (StatePath statePath : currentState.findStatePath(productionSkill)) {
			allPossibleSteps.add(statePath.getSteps());
		}
		return allPossibleSteps;
	}
}
