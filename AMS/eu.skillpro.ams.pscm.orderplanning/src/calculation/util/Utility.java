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

package calculation.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import skillpro.model.products.Product;
import skillpro.model.products.ProductQuantity;
import skillpro.model.service.SkillproService;
import skillpro.model.skills.PrePostRequirement;
import skillpro.model.skills.ProductionSkill;
import skillpro.model.skills.Requirement;
import skillpro.model.skills.RequirementSkillType;
import skillpro.model.skills.ResourceSkill;
import skillpro.model.skills.TemplateSkill;
import calculation.model.SkillRequirementCombination;
import calculation.model.SkillRequirementDisjunctEntity;
import calculation.model.SkillRequirementSet;
import calculation.model.SkillRequirementSingleEntity;
import calculation.model.path.ResourceSkillPath;
import calculation.model.path.ResourceSkillPathContent;
import calculation.model.path.SkillStep;
import calculation.model.path.StatePath;
import calculation.model.state.MiniState;

public class Utility {
	//this means we'll get alternative production skills
	//should be used with CAUTION
	public static final List<ProductionSkill> getProductionSkillsRelatedToOutput(Product outputProduct) {
		List<ProductionSkill> relatedProductionSkills = new ArrayList<>();
		for (ProductionSkill skill : SkillproService.getSkillproProvider().getProductionSkillRepo()) {
			// skip transport skills to avoid infinite loops
			if (skill.getInputConfiguration().getProductQuantities().equals(skill.getOutputConfiguration()
					.getProductQuantities())) {
				continue;
			}
			
			for (ProductQuantity productQuantity : skill.getOutputConfiguration().getProductQuantities()) {
				if (productQuantity.getProduct().equals(outputProduct)) {
					if (!relatedProductionSkills.contains(skill)) {
						relatedProductionSkills.add(skill);
					}
				}
			}
		}
		
		return relatedProductionSkills;
	}

	public static SkillRequirementCombination extractResourceSkillsRequirementFromRequirement(PrePostRequirement pairRequirement) {
		Requirement preRequirement = pairRequirement.getPreRequirement();
		Requirement postRequirement = pairRequirement.getPostRequirement();
		SkillRequirementDisjunctEntity disjunctEntity = new SkillRequirementDisjunctEntity(preRequirement, postRequirement);
		
		TemplateSkill requiredTemplateSkill = preRequirement.getRequiredTemplateSkill();
		if (preRequirement.getSkillType() == RequirementSkillType.TEMPLATE_SKILL && requiredTemplateSkill != null) {
			for (ResourceSkill rSkill : SkillproService.getSkillproProvider().getResourceSkillRepo()) {
				//adds an alternative
				SkillRequirementSet disjunctiveRequirementSet = new SkillRequirementSet();
				disjunctEntity.addRequirementSet(disjunctiveRequirementSet);
				
				if (rSkill.getTemplateSkill().equals(requiredTemplateSkill)) {
					for (PrePostRequirement secondLayerPair : preRequirement.getRequiredResourceSkill()
							.getPrePostRequirements()) {
						ResourceSkill requiredResourceSkill = secondLayerPair.getPreRequirement().getRequiredResourceSkill();
						if (requiredResourceSkill.equals(rSkill)) {
							disjunctiveRequirementSet.addCombination(new SkillRequirementSingleEntity(secondLayerPair.getPreRequirement(),
									secondLayerPair.getPostRequirement(), rSkill));
						} else {
							disjunctiveRequirementSet.addCombination(extractResourceSkillsRequirementFromRequirement(secondLayerPair));
						}
					}
				}
			}
			
		} else if (preRequirement.getSkillType() == RequirementSkillType.RESOURCE_SKILL && preRequirement.getRequiredResourceSkill() != null) {
			
			SkillRequirementSet disjunctiveRequirementSet = new SkillRequirementSet();
			disjunctEntity.addRequirementSet(disjunctiveRequirementSet);
			
			for (PrePostRequirement secondLayerPair : preRequirement.getRequiredResourceSkill()
					.getPrePostRequirements()) {
				ResourceSkill requiredResourceSkill = secondLayerPair.getPreRequirement().getRequiredResourceSkill();
				if (requiredResourceSkill.equals(preRequirement.getRequiredResourceSkill())) {
					disjunctiveRequirementSet.addCombination(new SkillRequirementSingleEntity(preRequirement,
							postRequirement, requiredResourceSkill));
				} else {
					disjunctiveRequirementSet.addCombination(extractResourceSkillsRequirementFromRequirement(secondLayerPair));
				}
			}
				
		} else {
			throw new IllegalArgumentException("Error processing PreRequirement: " + preRequirement);
		}
		return disjunctEntity;
	}
	
	private static <D> Set<Set<D>> combineDistinctTwoCollections(Set<Set<D>> first, Set<Set<D>> second) {
		Set<Set<D>> combinedList = new HashSet<>();
		for (Set<D> secondContent : second) {
			for (Set<D> firstContent : first) {
				Set<D> combinedContent = new HashSet<>(firstContent);
				for (D realSecondContent : secondContent) {
					if (!combinedContent.contains(realSecondContent)) {
						combinedContent.add(realSecondContent);
					}
				}
				if (!combinedList.contains(combinedContent)) {
					combinedList.add(combinedContent);
				}
			}
		}
		return combinedList;
	}
	
	private static Set<Set<SkillRequirementSingleEntity>> flattenSkillReqCombination(SkillRequirementCombination combination) {
		Set<Set<SkillRequirementSingleEntity>> flattenedSkillReq = new HashSet<>();
		if (combination instanceof SkillRequirementSingleEntity) {
			Set<SkillRequirementSingleEntity> entityAsSet = new HashSet<>();
			entityAsSet.add((SkillRequirementSingleEntity) combination);
			flattenedSkillReq.add(entityAsSet);
		} else if (combination instanceof SkillRequirementDisjunctEntity) {
			for (SkillRequirementSet disjunctSet : ((SkillRequirementDisjunctEntity) combination).getDisjunctSkillRequirementSet()) {
				flattenedSkillReq.addAll(flattenRequirementSet(disjunctSet));
			}
		} else {
			throw new IllegalArgumentException("Error! Not possible");
		}
		
		return flattenedSkillReq;
	}

	public static Set<Set<SkillRequirementSingleEntity>> flattenRequirementSet(SkillRequirementSet requirementSet) {
		Set<Set<SkillRequirementSingleEntity>> flattenedSkillReq = new HashSet<>();
		for (SkillRequirementCombination combi : requirementSet.getCombinationSet()) {
			if (flattenedSkillReq.isEmpty()) {
				flattenedSkillReq.addAll(flattenSkillReqCombination(combi));
			} else {
				flattenedSkillReq = combineDistinctTwoCollections(flattenedSkillReq, flattenSkillReqCombination(combi));
			}
		}
		
		return flattenedSkillReq;
	}
	
	public static Set<ResourceSkillPath> convertToResourceSkillPath(MiniState firstState, StatePath statePath) {
		return convertToResourceSkillPath(firstState, statePath.getSteps());
	}
	
	public static Set<ResourceSkillPath> convertToResourceSkillPath(MiniState firstState, List<MiniState> states) {
		Set<List<SkillStep>> pathContents = new HashSet<>();
		MiniState currentState = firstState;
		for (MiniState nextState : states) {
			Set<ResourceSkillPathContent> possibleContents = currentState.getDirectReachableStatesMap().get(nextState);
			if (possibleContents == null) {
				throw new IllegalArgumentException("Cannot convert to ResourceSkillPath!");
			}
			if (pathContents.isEmpty()) {
				for (ResourceSkillPathContent possibleContent : possibleContents) {
					List<SkillStep> contentList = new ArrayList<>();
					contentList.add(new SkillStep(nextState, possibleContent));
					pathContents.add(contentList);
				}
			} else {
				Set<List<SkillStep>> newContents = new HashSet<>();
				for (List<SkillStep> pathContent : pathContents) {
					for (ResourceSkillPathContent possibleContent : possibleContents) {
						List<SkillStep> newPathContent = new ArrayList<>(pathContent);
						newPathContent.add(new SkillStep(nextState, possibleContent));
						newContents.add(newPathContent);
					}
				}
				pathContents = newContents;
			}
			currentState = nextState;
		}
		Set<ResourceSkillPath> resourceSkillPaths = new HashSet<>();
		for (List<SkillStep> pathContent : pathContents) {
			resourceSkillPaths.add(new ResourceSkillPath(pathContent));
		}
		return resourceSkillPaths;
	}
}
