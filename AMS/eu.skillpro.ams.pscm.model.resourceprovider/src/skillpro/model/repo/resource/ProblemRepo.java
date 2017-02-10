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

import java.util.Arrays;

import skillpro.model.repo.Repo;
import skillpro.model.service.SkillproService;
import skillpro.model.skills.ProductionSkill;
import skillpro.model.skills.ResourceSkill;

public class ProblemRepo extends Repo<String>{
	private final static String noRequirement = "The ResourceSkill: [%s] with the Resource = \"%s\" does not have any Requirements.";
	private final static String noResourceSkillTemplateSkill = "The ProductionSkill: [%s] with the TemplateSkill = \"%s\" does not have a corresponding" + " ResourceSkill.";
	private final static String noResourceSkill = "The ProductionSkill: [%s] with the following Properties = [%s] does not have a corresponding ResourceSkill.";
	
	public boolean checkForProblems() {
		wipeAllData();
		boolean requirementSearched = false;
		for (ProductionSkill pSkill : SkillproService.getSkillproProvider().getProductionSkillRepo()) {
			boolean foundTemplateSkill = false;
			for (ResourceSkill rSkill : SkillproService.getSkillproProvider().getResourceSkillRepo()) {
				if (!requirementSearched && rSkill.getPrePostRequirements().isEmpty()) {
					list.add(String.format(noRequirement, rSkill.getName(), rSkill.getResource().getName()));
				}
				if (rSkill.getTemplateSkill().equals(pSkill.getTemplateSkill())) {
					foundTemplateSkill = true;
					break;
				}
			}
			requirementSearched = true;
			if (!foundTemplateSkill){
				list.add(String.format(noResourceSkillTemplateSkill, pSkill.getName(), pSkill.getTemplateSkill().getName()));
			}else if (SkillproService.getSkillproProvider().getResourceSkillRepo().getPossibleResourceSkills(pSkill).isEmpty()){
				list.add(String.format(noResourceSkill, pSkill.getName(), Arrays.toString(pSkill.getPropertyDesignators().toArray())));
			}
		}
		return !isEmpty();
	}
}