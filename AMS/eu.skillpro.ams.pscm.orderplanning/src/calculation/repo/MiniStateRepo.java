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

package calculation.repo;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import skillpro.model.assets.Resource;
import skillpro.model.skills.ResourceSkill;
import skillpro.model.utils.Pair;
import calculation.model.Condition;
import calculation.model.PossibleStateTransition;
import calculation.model.SkillRequirementSingleEntity;
import calculation.model.state.MiniState;
import calculation.model.state.TransitionState;

public class MiniStateRepo {
	private final Map<MiniState, MiniState> entities = new HashMap<>();
	private final Map<PossibleStateTransition, Set<TransitionState>> possibleStateTransitionsMap = new HashMap<>();
	private final Map<ResourceSkill, Set<Set<SkillRequirementSingleEntity>>> singleEntitiesMap = new HashMap<>();
	private static final MiniStateRepo INSTANCE = new MiniStateRepo();
	
	private MiniStateRepo() {
		
	}
	
	public static MiniStateRepo getInstance() {
		return INSTANCE;
	}
	
	public Map<ResourceSkill, Set<Set<SkillRequirementSingleEntity>>> getSingleEntitiesMap() {
		return singleEntitiesMap;
	}
	
	public Map<PossibleStateTransition, Set<TransitionState>> getPossibleStateTransitionsMap() {
		return possibleStateTransitionsMap;
	}
	
	public Set<MiniState> findMiniStates(Collection<Pair<Resource, Condition>> queries) {
		Set<MiniState> miniStates = new HashSet<>();
		for (MiniState miniState : entities.keySet()) {
			boolean match = true;
			for (Pair<Resource, Condition> query : queries) {
				if (!miniState.getCurrentStateMap().get(query.getFirstElement()).equals(query.getSecondElement())) {
					match = false;
					break;
				}
			}
			if (match && !miniStates.contains(miniState)) {
				miniStates.add(miniState);
			}
			
		}
		
		return miniStates;
	}
	
	public Map<MiniState, MiniState> getEntities() {
		return entities;
	}
	
	public boolean addEntity(MiniState miniState) {
		if (miniState != null && !entities.containsKey(miniState)) {
			return entities.put(miniState, miniState) != null;
		}
		
		return false;
	}
	
	public void wipeAllData() {
		entities.clear();
		possibleStateTransitionsMap.clear();
		singleEntitiesMap.clear();
	}
}
