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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import skillpro.model.skills.ResourceSkill;
import calculation.model.SkillRequirementSingleEntity;
import calculation.model.path.StatePath;
import calculation.model.state.GhostState;
import calculation.model.state.MiniState;
import calculation.repo.MiniStateRepo;
import calculation.util.Utility;

public class StateExplorer {
	private static final StateExplorer INSTANCE = new StateExplorer();
	
	private StateExplorer() {
		
	}
	
	public static void exploreAndUpdateStates() {
		List<MiniState> miniStates = new ArrayList<>(MiniStateRepo.getInstance().getEntities().values());
		
		//uncomment to know the actual unexplored "size"
//		int originalTotal = 0;
//		int originalLoop = 0;
//		for (MiniState miniState : miniStates) {
//			originalTotal += miniState.getPossibleNextStatePaths().size();
//			originalLoop += miniState.getLoopedStatePath().size();
//		}
//		System.err.println("ORIGINAL TOTAL: " + originalTotal + ", Loop: " + originalLoop);
		
		//initialize toAddStatesMap
		Map<MiniState, GhostState> toAddStates = new HashMap<>();
		Map<MiniState, Map<MiniState, Set<StatePath>>> newlyAddedPossiblePaths = new HashMap<>();
		for (MiniState miniState : miniStates) {
			newlyAddedPossiblePaths.put(miniState, new HashMap<>(miniState.getPossibleNextStatePaths()));
			GhostState ghostState = new GhostState(miniState);
			toAddStates.put(miniState, ghostState);
		}
		
		//end initializing
		//important for the exploring, so it can't be replaced
		for (int i = 0; i < miniStates.size(); i++) {
			//important to go through each states once
			boolean addedSomething = false;
			for (MiniState currentState : miniStates) {
				
				//the newly found paths will be stored here
				Map<MiniState, Set<StatePath>> toAddPathsMap = new HashMap<>();
				//goes through all the currently available possible next states
				//and checks whether these states have access to new states or not
				Map<MiniState, Set<StatePath>> currentNewPossibleStatesMap = newlyAddedPossiblePaths.get(currentState);
				for (Entry<MiniState, Set<StatePath>> nextPossibleEntry : newlyAddedPossiblePaths.get(currentState).entrySet()) {
					MiniState nextPossibleState = nextPossibleEntry.getKey();
					Set<StatePath> roadsToNextPossibleState = nextPossibleEntry.getValue();
					Map<MiniState, Set<StatePath>> beyondPossibleStatesMap = toAddStates
							.get(nextPossibleState).getNextPossibleStates();
					for (Entry<MiniState, Set<StatePath>> beyondNextEntry : beyondPossibleStatesMap.entrySet()) {
						MiniState beyondState = beyondNextEntry.getKey();
						Set<StatePath> beyondPaths = beyondNextEntry.getValue();
						if (beyondState.equals(currentState)) {
							for (StatePath connectedPath : INSTANCE.connectPaths(roadsToNextPossibleState,
									nextPossibleState, beyondPaths)) {
								currentState.getLoopedStatePath().addAll(Utility.convertToResourceSkillPath(currentState,
										connectedPath));
							}
						} else {
							Set<StatePath> allPathsLeadingToBeyondState = currentState.getPossibleNextStatePaths().get(beyondState);
							if (allPathsLeadingToBeyondState == null) {
								allPathsLeadingToBeyondState = new HashSet<>();
								toAddPathsMap.put(beyondState, allPathsLeadingToBeyondState);
							}
							allPathsLeadingToBeyondState.addAll(INSTANCE.connectPaths(roadsToNextPossibleState,
									nextPossibleState, beyondPaths));
						}
					}
				}
				
				currentNewPossibleStatesMap.clear();
				for (Entry<MiniState, Set<StatePath>> toAdd : toAddPathsMap.entrySet()) {
					Set<StatePath> toAddPaths = new HashSet<>(toAdd.getValue());
					MiniState toAddState = toAdd.getKey();
					if (!currentState.getPossibleNextStatePaths().containsKey(toAddState)) {
						currentNewPossibleStatesMap.put(toAddState, toAddPaths);
					}
					for (StatePath statePath : toAddPaths) {
						for (SkillRequirementSingleEntity singleEntity : statePath.getLastStepContent().getSinglesList()) {
							ResourceSkill rSkillSingle = singleEntity.getContent();
							Set<StatePath> pathsByEndSkill = currentState.getPathsByEndSkillMap().get(rSkillSingle);
							if (pathsByEndSkill == null) {
								pathsByEndSkill = new HashSet<>();
								currentState.getPathsByEndSkillMap().put(rSkillSingle, pathsByEndSkill);
							}
							pathsByEndSkill.add(statePath);
						}
						
					}
					currentState.getPossibleNextStatePaths().put(toAddState, toAddPaths);
					addedSomething = true;
				}
			}
			
			if (!addedSomething) {
				break;
			}
		}
		//uncomment to know the actual explored "size"
//		int total = 0;
//		int loop = 0;
//		for (MiniState miniState : miniStates) {
//			total += miniState.getPossibleNextStatePaths().size();
//			loop += miniState.getLoopedStatePath().size();
//		}
//		System.err.println("TOTAL: " + total + ", Loop: " + loop);
	}
	
	private Set<StatePath> connectPaths(Set<StatePath> visitedPaths,
			MiniState possibleState, Set<StatePath> pathsBeyond) {
		if (visitedPaths.isEmpty() || pathsBeyond.isEmpty()) {
			throw new IllegalArgumentException("At least one of the paths is empty");
		}
		Set<StatePath> combinedPaths = new HashSet<>();
		for (StatePath visitedPath : visitedPaths) {
			for (StatePath pathBeyond : pathsBeyond) {
				StatePath pathToConnect = new StatePath(visitedPath);
				if (pathToConnect.addNextSteps(pathBeyond)) {
					combinedPaths.add(pathToConnect);
				}
			}
		}
		return combinedPaths;
	}
}

