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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import skillpro.model.properties.Property;
import skillpro.model.properties.PropertyConstraint;
import skillpro.model.properties.PropertyConstraintNominal;
import skillpro.model.properties.PropertyConstraintOrdinal;
import skillpro.model.properties.PropertyDesignator;
import skillpro.model.repo.Repo;
import skillpro.model.skills.ProductionSkill;
import skillpro.model.skills.ResourceSkill;
import skillpro.model.skills.TemplateSkill;

public class ResourceSkillRepo extends Repo<ResourceSkill>{

	public Set<ResourceSkill> getPossibleResourceSkills(ProductionSkill productionSkill) {
		Set<ResourceSkill> possibleResourceSkills = new HashSet<>();
		for (ResourceSkill resourceSkill : list) {
			if (resourceSkill.getTemplateSkill() == null) {
				throw new IllegalArgumentException("Not possible, ResourceSkill doesn't have a TemplateSkill: "
						+ resourceSkill.getId());
			}
			if (isMatchable(productionSkill, resourceSkill)) {
				possibleResourceSkills.add(resourceSkill);
			}
		}
	
		// check properties
		Iterator<ResourceSkill> iterator = possibleResourceSkills.iterator();
		while (iterator.hasNext()) {
			ResourceSkill currentResourceSkill = iterator.next();
			for (PropertyDesignator prodPropDes : productionSkill.getEmptyAndFilledDesignators()) {
				if (!isFulfilled(prodPropDes, currentResourceSkill)) {
					iterator.remove();
					break;
				}
			}
		}
		return possibleResourceSkills;
	}
	
	public static boolean isMatchable(ProductionSkill productionSkill, ResourceSkill resourceSkill) {
		return isMatchable(productionSkill.getTemplateSkill(), resourceSkill.getTemplateSkill());
	}
	
	private static boolean isMatchable(TemplateSkill productionTemplate, TemplateSkill resourceTemplate) {
		return isDescendantOf(resourceTemplate, productionTemplate);
	}
	
	private static boolean isDescendantOf(TemplateSkill possibleChild, TemplateSkill ancestor) {
		if (ancestor.equals(possibleChild)) {
			return true;
		}
		for (TemplateSkill child : ancestor.getChildren()) {
			return isDescendantOf(possibleChild, child);
		}
		return false;
	}
	
	public static boolean isFulfilled(PropertyDesignator designator, ResourceSkill skill) {
		boolean fulfilled = true;
		Property property = designator.getProperty();
		PropertyDesignator resourceDesignator = skill.getPropertyDesignator(property);
		if (resourceDesignator == null) {
			return false;
		} else {
			List<PropertyConstraint> constraints = resourceDesignator
					.getConstraints();
			for (PropertyConstraint constraint : constraints) {
				if (constraint instanceof PropertyConstraintNominal) {
					PropertyConstraintNominal nominalConstraint = (PropertyConstraintNominal) constraint;
					if (!nominalConstraint.getValues().contains(
							designator.getValue())) {
						fulfilled = false;
					}
				} else if (constraint instanceof PropertyConstraintOrdinal) {
					PropertyConstraintOrdinal ordinalConstraint = (PropertyConstraintOrdinal) constraint;
					double minValue = ordinalConstraint.getMinValue();
					double maxValue = ordinalConstraint.getMaxValue();
					double value = Double.parseDouble(designator.getValue());
					if (!((minValue <= value) && (maxValue >= value))) {
						fulfilled = false;
					}
				}
			}
		}
		return fulfilled;
	}
	
	public List<ResourceSkill> getCorrespondingResourceSkills(TemplateSkill templateSkill) {
		List<TemplateSkill> flatten = flattenChildrenAndThis(templateSkill);
		List<ResourceSkill> correspondingResourceSkills = new ArrayList<>();
		for (ResourceSkill res : list) {
			for (TemplateSkill childOrThis : flatten) {
				if (res.getTemplateSkill().equals(childOrThis)) {
					if (!correspondingResourceSkills.contains(res)) {
						correspondingResourceSkills.add(res);
					}
					break;
				}
			}
		}
		return correspondingResourceSkills;
	}
	
	private List<TemplateSkill> flattenChildrenAndThis(TemplateSkill templateSkill) {
		List<TemplateSkill> flatten = new ArrayList<>();
		
		flatten.add(templateSkill);
		List<TemplateSkill> children = templateSkill.getChildren();
		if (children != null && !children.isEmpty()) {
			for (TemplateSkill temp : children) {
				flatten.addAll(flattenChildrenAndThis(temp));
			}
		}
		
		return flatten;
	}
}
