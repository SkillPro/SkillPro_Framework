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

package calculation.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import skillpro.model.assets.Resource;
import calculation.model.state.MiniState;
import calculation.model.state.TransitionState;

public class PossibleStateTransition {
	private Set<SkillRequirementSingleEntity> singleEntities = new HashSet<>();
	private TransitionState transitionState;
	
	public PossibleStateTransition(Set<SkillRequirementSingleEntity> singleEntities, MiniState state) {
		this.singleEntities.addAll(singleEntities);
		Map<Resource, Condition> currentStateMap = new HashMap<>();
		for (SkillRequirementSingleEntity singleEntity : singleEntities) {
			Resource resource = singleEntity.getContent().getResource();
			Condition currentStateOfResource = state
					.getCurrentStateMap().get(resource);
			if (currentStateOfResource == null) {
				throw new IllegalArgumentException("This should never ever happen!" +
						" Current state does not have an entry of this Resource: " + resource);
			}
			currentStateMap.put(resource, currentStateOfResource);
		}
		transitionState = new TransitionState(currentStateMap);
	}

	public TransitionState getTransitionState() {
		return transitionState;
	}
	
	public Set<SkillRequirementSingleEntity> getSingleEntities() {
		return singleEntities;
	}

	@Override
	public int hashCode() {
		return Objects.hash(singleEntities, transitionState);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PossibleStateTransition other = (PossibleStateTransition) obj;
		return Objects.equals(singleEntities, other.singleEntities)
				&& Objects.equals(transitionState, other.transitionState);
	}
}
