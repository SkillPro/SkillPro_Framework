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

package skillpro.model.repo.resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import skillpro.model.products.Order;
import skillpro.model.repo.Repo;
import skillpro.model.skills.ExecutableSkill;
import skillpro.model.skills.Skill;

public class SkillRepo extends Repo<Skill> {
	private Set<ExecutableSkill> executableSkills = new HashSet<>();
	
	private Map<ExecutableSkill, Boolean> exSkillsSelectionMap = new HashMap<>();
	
	private Map<Order, Set<List<ExecutableSkill>>> possibleOrderedExSkillsBasedOnOrder = new HashMap<>();
	
	@Override
	public List<Skill> getEntities() {
		List<Skill> entities = new ArrayList<>();
		return entities;
	}
	
	public Map<ExecutableSkill, Boolean> getExSkillsSelectionMap() {
		return exSkillsSelectionMap;
	}
	
	public Map<Order, Set<List<ExecutableSkill>>> getPossibleOrderedExSkillsBasedOnOrder() {
		return possibleOrderedExSkillsBasedOnOrder;
	}
	
	public Set<ExecutableSkill> getExecutableSkills() {
		return executableSkills;
	}
	
	@Override
	public int size() {
		return getEntities().size();
	}
	
	@Override
	public void wipeAllData() {
		executableSkills.clear();
		possibleOrderedExSkillsBasedOnOrder.clear();
		exSkillsSelectionMap.clear();
	}
}
