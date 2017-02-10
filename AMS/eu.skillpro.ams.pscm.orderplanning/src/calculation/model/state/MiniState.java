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

package calculation.model.state;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import skillpro.model.assets.Resource;
import skillpro.model.assets.ResourceConfiguration;
import skillpro.model.assets.Setup;
import skillpro.model.products.ProductConfiguration;
import skillpro.model.properties.PropertyDesignator;
import skillpro.model.service.SkillproService;
import skillpro.model.skills.PrePostRequirement;
import skillpro.model.skills.ProductionSkill;
import skillpro.model.skills.Requirement;
import skillpro.model.skills.RequirementProductConfigType;
import skillpro.model.skills.RequirementResourceConfigType;
import skillpro.model.skills.RequirementSkillType;
import skillpro.model.skills.ResourceExecutableSkill;
import skillpro.model.skills.ResourceSkill;
import calculation.StateExplorer;
import calculation.model.Condition;
import calculation.model.PossibleStateTransition;
import calculation.model.SkillRequirementSet;
import calculation.model.SkillRequirementSingleEntity;
import calculation.model.path.ResourceSkillPath;
import calculation.model.path.ResourceSkillPathContent;
import calculation.model.path.SkillStep;
import calculation.model.path.StatePath;
import calculation.repo.MiniStateRepo;
import calculation.util.Utility;

public class MiniState extends State {
	//possible transitions from this State to next States
	private Map<MiniState, Set<ResourceSkillPathContent>> directReachableStatesMap = new HashMap<>();
	private Map<MiniState, Set<StatePath>> possibleNextStatePaths = new HashMap<>();
	private Map<ResourceSkill, Set<StatePath>> pathsByEndSkillMap = new HashMap<>();
	private Set<ResourceSkillPath> loopedStatePaths = new HashSet<>();
	public MiniState() {
		
	}
	
	//constructor for cloning
	public MiniState(State toClone) {
		this.currentStateMap = new HashMap<>(toClone.currentStateMap);
	}
	
	public MiniState(State previousState, TransitionState transitionState) {
		this.currentStateMap = new HashMap<>(previousState.currentStateMap);
		this.currentStateMap.putAll(transitionState.getCurrentStateMap());
	}
	
	public void exploreAndUpdateStates() {
		long before = System.currentTimeMillis();
		exploreNextPossibleStates();
		System.err.println("After Exploring: " + (System.currentTimeMillis() - before));
		System.out.println("MiniStateRepoSize: " + MiniStateRepo.getInstance().getEntities().size());
		before = System.currentTimeMillis();
		StateExplorer.exploreAndUpdateStates();
		System.err.println("After updating: " + (System.currentTimeMillis() - before));
	}
	
	private void exploreNextPossibleStates() {
		Set<MiniState> foundMiniStates = new HashSet<>();
		
		for (Entry<Resource, Condition> entry : currentStateMap.entrySet()) {
			Resource resource = entry.getKey();
			for (Setup setup : resource.getSetups()) {
				for (ResourceSkill rSkill : setup.getResourceSkills()) {
					
					Set<Set<SkillRequirementSingleEntity>> cleanedEntities = MiniStateRepo.getInstance().getSingleEntitiesMap().get(rSkill);
					if (cleanedEntities == null) {
						SkillRequirementSet requirementSet = new SkillRequirementSet();
						if (!resource.equals(rSkill.getResource())) {
							throw new IllegalArgumentException("Resource does not match with the Resource of this ResourceSkill: "
									+ rSkill);
						}
						for (PrePostRequirement pairRequirement : rSkill.getPrePostRequirements()) {
							requirementSet.addCombination(Utility.extractResourceSkillsRequirementFromRequirement(pairRequirement));
						}
						Set<Set<SkillRequirementSingleEntity>> flattenedEntities = Utility.flattenRequirementSet(requirementSet);
						
						cleanedEntities = cleanSinglesList(flattenedEntities);
						if (cleanedEntities.isEmpty()) {
							throw new IllegalArgumentException("An unknown error has occurred, please submit a bug report.");
						}
						MiniStateRepo.getInstance().getSingleEntitiesMap().put(rSkill, cleanedEntities);
					}
					for (Set<SkillRequirementSingleEntity> singleEntities : cleanedEntities) {
						PossibleStateTransition possibleStateTransition = new PossibleStateTransition(singleEntities, this);
						Set<TransitionState> transitionStates = MiniStateRepo.getInstance().getPossibleStateTransitionsMap().get(possibleStateTransition);
						if (transitionStates == null && possibleToExecute(singleEntities)) {
							transitionStates = getChangedStates(possibleStateTransition);
							//stateMap's key set and nextPossibleStates should be equal to one another
							MiniStateRepo.getInstance().getPossibleStateTransitionsMap()
									.put(possibleStateTransition, transitionStates);
						}
						if (transitionStates != null) {
							ResourceSkillPathContent nextStep = new ResourceSkillPathContent(rSkill, singleEntities);
							for (TransitionState transitionState : transitionStates) {
								MiniState nextState = new MiniState(this, transitionState);
								MiniState nextStateCandidate = MiniStateRepo.getInstance().getEntities().get(nextState);
								if (nextStateCandidate != null) {
									nextState = nextStateCandidate;
								}
								SkillStep nextSkillStep = new SkillStep(nextState, nextStep);
								
								if (this.equals(nextState)) {
									loopedStatePaths.add(new ResourceSkillPath(nextSkillStep));
								} else {
									Set<StatePath> existingStatePaths = possibleNextStatePaths.get(nextState);
									if (existingStatePaths == null) {
										existingStatePaths = new HashSet<>();
										possibleNextStatePaths.put(nextState, existingStatePaths);
									}
									Set<ResourceSkillPathContent> existingDirectStep = directReachableStatesMap.get(nextState);
									if (existingDirectStep == null) {
										existingDirectStep = new HashSet<>();
										directReachableStatesMap.put(nextState, existingDirectStep);
									}
									ResourceSkill mainResourceSkill = nextSkillStep.getSecondElement().getMainResourceSkill();
									Set<StatePath> pathsByEndSkill = pathsByEndSkillMap.get(mainResourceSkill);
									if (pathsByEndSkill == null) {
										pathsByEndSkill = new HashSet<>();
										pathsByEndSkillMap.put(mainResourceSkill, pathsByEndSkill);
									}
									StatePath directStatePath = new StatePath(nextSkillStep);
									pathsByEndSkill.add(directStatePath);
									existingStatePaths.add(directStatePath);
									existingDirectStep.add(nextSkillStep.getSecondElement());
								}
								foundMiniStates.add(nextState);
							}
						}
					}
					
				}
			}
		}
		Set<MiniState> newlyAddedStates = new HashSet<>();
		for (MiniState nextState : foundMiniStates) {
			if (!MiniStateRepo.getInstance().getEntities().containsKey(nextState)) {
				MiniStateRepo.getInstance().getEntities().put(nextState, nextState);
				newlyAddedStates.add(nextState);
			}
		}
		for (MiniState addedState : newlyAddedStates) {
			addedState.exploreNextPossibleStates();
			
		}
	}
	
	public Set<StatePath> findStatePath(ProductionSkill productionSkill) {
		Set<StatePath> foundStatePaths = new HashSet<>();
		Set<ResourceSkill> possibleResourceSkills = SkillproService.getSkillproProvider()
				.getResourceSkillRepo().getPossibleResourceSkills(productionSkill);
		for (ResourceSkill rSkill : possibleResourceSkills) {
			//supposed to be the last entry in the ResourceSkillPath
			Set<StatePath> foundPaths = pathsByEndSkillMap.get(rSkill);
			if (foundPaths != null) {
				foundStatePaths.addAll(foundPaths);
			}
		}
		
		return foundStatePaths;
	}
	
	public Map<MiniState, Set<ResourceSkillPathContent>> getDirectReachableStatesMap() {
		return directReachableStatesMap;
	}
	
	private boolean possibleToExecute(Set<SkillRequirementSingleEntity> singlesList) {
		for (SkillRequirementSingleEntity single : singlesList) {
			if (!possibleToExecute(single)) {
				return false;
			}
		}
		
		return true;
	}
	
	private boolean possibleToExecute(SkillRequirementSingleEntity single) {
		return possibleToExecute(new PrePostRequirement (single.getPreRequirement(), single.getPostRequirement()),
				single.getContent(), this);
	}
	
	private Set<TransitionState> getChangedStates(PossibleStateTransition possibleStateTransition) {
		Set<SkillRequirementSingleEntity> singlesEntities = new HashSet<>(possibleStateTransition.getSingleEntities());
		Set<State> realCurrentStates = new HashSet<>();
		realCurrentStates.add(new TransitionState(possibleStateTransition.getTransitionState().getCurrentStateMap()));
		if (singlesEntities.isEmpty()) {
			throw new IllegalArgumentException("No singles, shouldn't be possible");
		}
		for (SkillRequirementSingleEntity single : singlesEntities) {
			Requirement preRequirement = single.getPreRequirement();
			Requirement postRequirement = single.getPostRequirement();
			ResourceSkill resourceSkill = single.getContent();
			
			//PropertyDesignators are not important for this stage.
			Set<State> nextStates = new HashSet<>(realCurrentStates);
			for (State currentState : realCurrentStates) {
				Set<ResourceExecutableSkill> createdRExSkills = createResourceExecutableSkills(resourceSkill,
						preRequirement, postRequirement);
				if (!createdRExSkills.isEmpty()) {
					nextStates.remove(currentState);
					for (ResourceExecutableSkill rexSkill : createdRExSkills) {
						nextStates.add(new TempState(currentState, rexSkill));
					}
				} else {
					throw new IllegalArgumentException("Cannot go to next state");
				}
				
			}
			realCurrentStates = nextStates;
		}
		
		//last state will be converted back into a MiniState and will be returned
		Set<TransitionState> changedStates = new HashSet<>();
		for (State currentState : realCurrentStates) {
			TransitionState nextState = new TransitionState(currentState.getCurrentStateMap());
			changedStates.add(nextState);
		}
		
		return changedStates;
	}
	
	private Set<Set<SkillRequirementSingleEntity>> cleanSinglesList(Set<Set<SkillRequirementSingleEntity>> listOfSinglesList) {
		Set<Set<SkillRequirementSingleEntity>> cleanedSingles = new HashSet<>();
		
		for (Set<SkillRequirementSingleEntity> rawCombi : listOfSinglesList) {
			Set<Resource> resourcesInCombiList = new HashSet<>();
			boolean illegalDuplicates = false;
			for (SkillRequirementSingleEntity single : rawCombi) {
				//content of a Single should never be null
				Resource resource = single.getContent().getResource();
				if (resource == null) {
					throw new IllegalArgumentException("Resource is null! Not possible!");
				} else if (resourcesInCombiList.contains(resource)) {
					illegalDuplicates = true;
					break;
				} else {
					resourcesInCombiList.add(resource);
				}
			}
			
			if (!illegalDuplicates) {
				cleanedSingles.add(rawCombi);
			} else {
				throw new IllegalArgumentException("Illegal duplicates found!");
			}
		}
		
		return cleanedSingles;
	}
	
	public Map<MiniState, Set<StatePath>> getPossibleNextStatePaths() {
		return possibleNextStatePaths;
	}
	
	private Set<ResourceExecutableSkill> createResourceExecutableSkills(ResourceSkill resourceSkill,
			Requirement preRequirement, Requirement postRequirement) {
		//no property designators for now
		ResourceConfiguration postResourceConfiguration = postRequirement.getRequiredResourceConfiguration();
		ProductConfiguration postProductConfiguration = postRequirement.getRequiredProductConfiguration();
		
		Condition currentResourceStatePair = this.getCurrentStateMap().get(resourceSkill.getResource());
		if (postRequirement.getResourceConfigType() == RequirementResourceConfigType.SAME) {
			if (preRequirement.getResourceConfigType() == RequirementResourceConfigType.ANY) {
				postResourceConfiguration = currentResourceStatePair.getFirstElement();
			} else {
				postResourceConfiguration = preRequirement.getRequiredResourceConfiguration();
			}
		}
		if (postRequirement.getProductConfigType() == RequirementProductConfigType.SAME) {
			if (preRequirement.getProductConfigType() == RequirementProductConfigType.ANY) {
				postProductConfiguration = currentResourceStatePair.getSecondElement();
			} else {
				postProductConfiguration = preRequirement.getRequiredProductConfiguration();
			}
		} else if (postRequirement.getProductConfigType() == RequirementProductConfigType.ANY) {
			throw new IllegalArgumentException("ANY post product configuration should not exist, yet.");
		}

		Set<ResourceConfiguration> possiblePostResourceConfigurations = new HashSet<>();
		if (postResourceConfiguration == null) {
			if (postRequirement.getResourceConfigType() == RequirementResourceConfigType.ANY) {
				Resource resource = resourceSkill.getResource();
				for (ResourceConfiguration rConf : resource.getResourceConfigurations()) {
					possiblePostResourceConfigurations.add(rConf);
				}
			} else if (postRequirement.getResourceConfigType() == RequirementResourceConfigType.DIFFERENT_ANY) {
				Resource resource = resourceSkill.getResource();
				for (ResourceConfiguration rConf : resource.getResourceConfigurations()) {
					if (!rConf.equals(currentResourceStatePair.getFirstElement())) {
						possiblePostResourceConfigurations.add(rConf);
					}
				}
			}
		} else {
			if (postRequirement.getResourceConfigType() == RequirementResourceConfigType.ANY) {
				throw new IllegalArgumentException("RConf is not null but RConfType is ANY!!");
			}
			possiblePostResourceConfigurations.add(postResourceConfiguration);
		}
		Set<ResourceExecutableSkill> rexSkills = new HashSet<>();
		for (ResourceConfiguration possibleResourceConfiguration : possiblePostResourceConfigurations) {
			ResourceExecutableSkill resourceExecutableSkill = new ResourceExecutableSkill("REX: " + resourceSkill.getName(),
					resourceSkill, preRequirement, possibleResourceConfiguration, postProductConfiguration,
					new ArrayList<PropertyDesignator>(), 0, 0, preRequirement.getSyncType());
			rexSkills.add(resourceExecutableSkill);
		}
		return rexSkills;
	}
	
	private boolean possibleToExecute(PrePostRequirement prePostRequirement, ResourceSkill sourceResourceSkill,
			State currentState) {
		Requirement preRequirement = prePostRequirement.getPreRequirement();
		Requirement postRequirement = prePostRequirement.getPostRequirement();
		if (postRequirement.getSkillType() != RequirementSkillType.SAME) {
			throw new IllegalArgumentException("Different ResourceSkills between the pre- and postRequirements!");
		}
		//should be the same ResourceSkill
		Condition confPair = currentState.currentStateMap.get(sourceResourceSkill.getResource());
		
		//checking whether product configuration is correct will be done in another method?
		return fulfillsPreRequirement(preRequirement, confPair);
	}
	
	public Map<ResourceSkill, Set<StatePath>> getPathsByEndSkillMap() {
		return pathsByEndSkillMap;
	}
	
	public Set<ResourceSkillPath> getLoopedStatePath() {
		return loopedStatePaths;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("CurrentStateMap: " + currentStateMap.toString());
		return sb.toString();
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		return true;
	}
}
