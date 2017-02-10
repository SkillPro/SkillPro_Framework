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
import java.util.Map;

import skillpro.model.assets.Resource;
import skillpro.model.assets.ResourceConfiguration;
import skillpro.model.products.ProductConfiguration;
import skillpro.model.skills.ResourceExecutableSkill;
import calculation.model.Condition;

public class TempState extends State {
	public TempState(State previousState, ResourceExecutableSkill executedSkill) {
		super();
		currentStateMap = changeState(previousState, executedSkill);
		
	}

	private Map<Resource, Condition> changeState(State previousState,
			ResourceExecutableSkill executedSkill) {
		Map<Resource, Condition> newStateMap = new HashMap<>(previousState.currentStateMap);
		
		Resource resource = executedSkill.getResource();
		ProductConfiguration postProductConfiguration = executedSkill.getPostProductConfiguration();
		ResourceConfiguration postResourceConfiguration = executedSkill.getPostResourceConfiguration();
		newStateMap.put(resource, new Condition(postResourceConfiguration, postProductConfiguration));
		return newStateMap;
	}
}
