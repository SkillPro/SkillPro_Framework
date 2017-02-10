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

package calculation.model.path;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import calculation.model.state.MiniState;

public class ResourceSkillPath {
	private List<SkillStep> steps = new ArrayList<>();
	private Set<MiniState> relevantStates =  new HashSet<>();
	
	public ResourceSkillPath(ResourceSkillPath toClone) {
		steps.addAll(toClone.steps);
		relevantStates.addAll(toClone.relevantStates);
	}
	
	public ResourceSkillPath(SkillStep nextStep) {
		addNextStep(nextStep);
	}
	public ResourceSkillPath(List<SkillStep> path) {
		this.steps.addAll(path);
		for (SkillStep pair : steps) {
			if (!relevantStates.contains(pair.getFirstElement())) {
				relevantStates.add(pair.getFirstElement());
			} else {
				throw new IllegalArgumentException("A path should consists of unique states");
			}
		}
	}
	
	public ResourceSkillPath(ResourceSkillPath previousPath, SkillStep nextStep) {
		this(previousPath.steps);
		addNextStep(nextStep);
	}
	
	public ResourceSkillPath(ResourceSkillPath previousPath, List<SkillStep> nextSteps) {
		this(previousPath.steps);
		addNextSteps(nextSteps);
	}
	
	public List<SkillStep> getSteps() {
		return steps;
	}
	
	public boolean addNextSteps(List<SkillStep> nextSteps) {
		Set<MiniState> copyOfRelevantStates = new HashSet<>(relevantStates);
		List<SkillStep> copyOfSteps = new ArrayList<>(steps);
		for (SkillStep nextStep : nextSteps) {
			if (!addNextStep(nextStep)) {
				relevantStates.clear();
				relevantStates.addAll(copyOfRelevantStates);
				steps.clear();
				steps.addAll(copyOfSteps);
				return false;
			}
		}
		
		return true;
	}
	
	public boolean addNextStep(SkillStep nextStep) {
		if (!relevantStates.contains(nextStep.getFirstElement())) {
			relevantStates.add(nextStep.getFirstElement());
			steps.add(nextStep);
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(steps);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ResourceSkillPath other = (ResourceSkillPath) obj;
		return Objects.equals(steps, other.steps);
	}

	@Override
	public String toString() {
		return Arrays.toString(steps.toArray());
	}
}
