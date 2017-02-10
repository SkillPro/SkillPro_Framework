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

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

import skillpro.model.skills.ResourceSkill;
import calculation.model.SkillRequirementSingleEntity;

public class ResourceSkillPathContent {
	private ResourceSkill mainResourceSkill;
	private Set<SkillRequirementSingleEntity> singlesList;
	
	public ResourceSkillPathContent(ResourceSkill mainResourceSkill, Set<SkillRequirementSingleEntity> singlesList) {
		this.mainResourceSkill = mainResourceSkill;
		this.singlesList = singlesList;
	}
	
	public ResourceSkill getMainResourceSkill() {
		return mainResourceSkill;
	}
	
	public Set<SkillRequirementSingleEntity> getSinglesList() {
		return singlesList;
	}

	@Override
	public int hashCode() {
		return Objects.hash(mainResourceSkill, singlesList);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ResourceSkillPathContent other = (ResourceSkillPathContent) obj;
		return Objects.equals(mainResourceSkill, other.mainResourceSkill)
				&& Objects.equals(singlesList, other.singlesList);
	}


	@Override
	public String toString() {
		return "[ Main: " + mainResourceSkill.getName() + ", singles: " + Arrays.toString(singlesList.toArray()) + " ]";
	}
}
