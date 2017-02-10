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

package skillpro.model.properties;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import skillpro.model.skills.Skill;

public class PropertyDesignator {
	private Property property;
	private Skill skill;
	private List<PropertyConstraint> constraints = new ArrayList<>();
	private String value;
	
	public PropertyDesignator(Property property, Skill skill, String value) {
		this.property = property;
		this.skill = skill;
		this.value = value;
	}
	
	public PropertyDesignator(Property property, Skill skill, String value, List<PropertyConstraint> constraints) {
		this(property, skill, value);
		this.constraints = constraints;
	}
	
	/**
	 * A copy constructor. All fields but the {@link property} and the
	 * {@link skill} are deeply copied.
	 * 
	 * @param pd a PropertyDesignator
	 */
	public PropertyDesignator(PropertyDesignator pd) {
		this.property = pd.property;
		this.skill = pd.skill;
		for (PropertyConstraint pc : constraints) {
			this.constraints.add(pc.clone());
		}
		this.value = pd.value;
	}

	public List<PropertyConstraint> getConstraints() {
		return constraints;
	}
	
	public void setSkill(Skill skill) {
		this.skill = skill;
	}
	
	public void setConstraints(List<PropertyConstraint> constraints) {
		this.constraints = constraints;
	}
	
	public void setProperty(Property property) {
		this.property = property;
	}
	
	public Property getProperty() {
		return property;
	}
	
	public Skill getSkill() {
		return skill;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public boolean addConstraint(PropertyConstraint constraint) {
		if (constraint != null && !constraints.contains(constraint)) {
			return constraints.add(constraint);
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(constraints, property, skill, value);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PropertyDesignator other = (PropertyDesignator) obj;
		return Objects.equals(constraints, other.constraints)
				&& Objects.equals(property, other.property)
				&& Objects.equals(skill, other.skill)
				&& Objects.equals(value, other.value);
	}
}
