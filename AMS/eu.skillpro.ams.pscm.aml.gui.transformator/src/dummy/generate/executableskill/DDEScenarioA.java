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

package dummy.generate.executableskill;

import java.util.Arrays;
import java.util.List;

import skillpro.model.skills.dummy.ExecutableSkillDummy;

public class DDEScenarioA extends DDEScenario{
	
	private static final DDEScenarioA INSTANCE = new DDEScenarioA();
	
	public static DDEScenarioA getInstance() {
		return INSTANCE;
	}
	
	public String getName() {
		return "DDE scenario A";
	}
	
	@Override
	public String getSuffix() {
		return "DDEa";
	}
	
	@Override
	public List<String> getGoalSkills() {
		return addIDSuffixToGoalskills(Arrays.asList("ID_QualityControl"));
	}
	
	@Override
	protected List<ExecutableSkillDummy> getScenarioSkills() {
		return getScenarioSkills("DDEa.plantuml", "DDEa properties.csv");
	}
}
