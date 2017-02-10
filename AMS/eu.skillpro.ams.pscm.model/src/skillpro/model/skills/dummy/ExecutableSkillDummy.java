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

package skillpro.model.skills.dummy;

import java.util.ArrayList;
import java.util.List;

import skillpro.model.skills.Skill;

public class ExecutableSkillDummy extends Skill {
	
	private ResourceExecutableSkillDummy mainDummy;
	private List<ResourceExecutableSkillDummy> dummies = new ArrayList<>();

	private ResourceExecutableSkillDummy producer;
	
	public ExecutableSkillDummy() {
		super("");
	}
	
	public ExecutableSkillDummy(String name) {
		super(name);
		setId(name);
	}
	
	public boolean addDummy(ResourceExecutableSkillDummy skill) {
		if (skill != null && !dummies.contains(skill)) {
			return dummies.add(skill);
		}
		
		return false;
	}
	
	public List<ResourceExecutableSkillDummy> getDummies() {
		return dummies;
	}

	public ResourceExecutableSkillDummy add(String resourceExecutableSkill, ResourceDummy resource, String templateSkill) {
		ResourceExecutableSkillDummy result = new ResourceExecutableSkillDummy(resourceExecutableSkill, resource, templateSkill);
		addDummy(result);
		return result;
	}
	
	public ResourceExecutableSkillDummy getMainDummy() {
		return mainDummy;
	}
	
	public void setMainDummy(ResourceExecutableSkillDummy mainDummy) {
		this.mainDummy = mainDummy;
	}
	
	public void setProducer(ResourceExecutableSkillDummy producer) {
		this.producer = producer;
	}
	
	public ResourceExecutableSkillDummy getProducer() {
		return producer;
	}

}
