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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import calculation.model.path.StatePath;

public class GhostState extends State {
	private MiniState referencedState;
	private Map<MiniState, Set<StatePath>> nextPossibleStates = new HashMap<>();
	
	public GhostState() {
		
	}
	
	public GhostState(MiniState referencedState) {
		this.referencedState = referencedState;
		for (Entry<MiniState, Set<StatePath>> entry : referencedState.getPossibleNextStatePaths().entrySet()) {
			nextPossibleStates.put(entry.getKey(), new HashSet<>(entry.getValue()));
		}
	}
	
	public GhostState(Map<MiniState, Set<StatePath>> nextPossibleStates) {
		this.nextPossibleStates.putAll(nextPossibleStates);
	}
	
	public Map<MiniState, Set<StatePath>> getNextPossibleStates() {
		return nextPossibleStates;
	}
	
	
	public MiniState getReferencedState() {
		return referencedState;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((referencedState == null) ? 0 : referencedState.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		GhostState other = (GhostState) obj;
		if (referencedState == null) {
			if (other.referencedState != null)
				return false;
		} else if (!referencedState.equals(other.referencedState))
			return false;
		return true;
	}
}

