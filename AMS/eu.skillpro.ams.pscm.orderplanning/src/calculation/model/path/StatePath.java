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

public class StatePath {
	private Set<MiniState> relevantStates =  new HashSet<>();

	private List<MiniState> steps =  new ArrayList<>();
	private List<List<MiniState>> nextStatePaths = new ArrayList<>();

	private ResourceSkillPathContent lastStepContent;
	
	public StatePath(StatePath toClone) {
		steps.addAll(toClone.steps);
		relevantStates.addAll(toClone.relevantStates);
		lastStepContent = toClone.lastStepContent;
		nextStatePaths.addAll(toClone.nextStatePaths);
	}
	
	public StatePath(SkillStep nextStep) {
		addNextStep(nextStep);
	}
	
	public List<MiniState> getSteps() {
		List<MiniState> steps = new ArrayList<>(this.steps);
		for (List<MiniState> nextPath : nextStatePaths) {
			steps.addAll(nextPath);
		}
		return steps;
	}
	
	public ResourceSkillPathContent getLastStepContent() {
		return lastStepContent;
	}
	
	public boolean addNextStep(SkillStep nextStep) {
		if (!relevantStates.contains(nextStep.getFirstElement())) {
			relevantStates.add(nextStep.getFirstElement());
			steps.add(nextStep.getFirstElement());
			lastStepContent = nextStep.getSecondElement();
			return true;
		} else {
			return false;
		}
	}
	
	//WARNING! This method will permanently affect the steps of the state
	//Use at your own risk!
	public boolean addNextSteps(StatePath nextStatePath) {
		for (MiniState nextStep : nextStatePath.relevantStates) {
			if (!relevantStates.contains(nextStep)) {
				relevantStates.add(nextStep);
			} else {
				return false;
			}
		}
		nextStatePaths.add(nextStatePath.steps);
		nextStatePaths.addAll(nextStatePath.nextStatePaths);
		lastStepContent = nextStatePath.getLastStepContent();
		return true;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(steps, nextStatePaths);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StatePath other = (StatePath) obj;
		return Objects.equals(steps, other.steps)
				&& Objects.equals(nextStatePaths, other.nextStatePaths);
	}

	@Override
	public String toString() {
		return Arrays.toString(steps.toArray());
	}
}
