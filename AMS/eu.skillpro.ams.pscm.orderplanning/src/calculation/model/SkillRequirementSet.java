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

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class SkillRequirementSet {
	private Set<SkillRequirementCombination> combinationSet = new HashSet<>();
	
	public Set<SkillRequirementCombination> getCombinationSet() {
		return combinationSet;
	}
	
	public boolean addCombination(SkillRequirementCombination combination) {
		if (combination != null && !combinationSet.contains(combination)) {
			return combinationSet.add(combination);
		}
		
		return false;
	}
	
	@Override
	public String toString() {
		
		StringBuffer sb = new StringBuffer();
		for (SkillRequirementCombination combi : combinationSet) {
			sb.append(combi);
			sb.append(" + ");
		}
		if (combinationSet.size() >= 3) {
			sb.delete(sb.length() - 3, sb.length());
		}
		
		return sb.toString();
	}

	@Override
	public int hashCode() {
		return Objects.hash(combinationSet);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SkillRequirementSet other = (SkillRequirementSet) obj;
		return Objects.equals(combinationSet, other.combinationSet);
	}
}
