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

package skillpro.model.skills;

import java.util.Objects;

/** 
 * A pair of matching requirements, one pre-requirement and one post-requirement.
 */
public class PrePostRequirement {
	private Requirement preRequirement;
	private Requirement postRequirement;
	
	public PrePostRequirement(Requirement preRequirement, Requirement postRequirement) {
		setPreRequirement(preRequirement);
		setPostRequirement(postRequirement);
		if (postRequirement.getSkillType() != RequirementSkillType.SAME) {
			throw new IllegalArgumentException("Different ResourceSkills detected between both Requirements");
		}
	}
	
	public void setPreRequirement(Requirement preRequirement) {
		Objects.requireNonNull(preRequirement);
		this.preRequirement = preRequirement;
	}
	
	public void setPostRequirement(Requirement postRequirement) {
		Objects.requireNonNull(postRequirement);
		this.postRequirement = postRequirement;
	}

	public int hashCode() {
		return Objects.hash(preRequirement, postRequirement);
	}
	
	public Requirement getPreRequirement() {
		return preRequirement;
	}
	
	public Requirement getPostRequirement() {
		return postRequirement;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PrePostRequirement other = (PrePostRequirement) obj;
		return Objects.equals(preRequirement, other.preRequirement) && Objects.equals(postRequirement, other.postRequirement);
	}
	
	@Override
	public String toString() {
		return "(" + preRequirement + "; " + postRequirement + ")";
	}
}
