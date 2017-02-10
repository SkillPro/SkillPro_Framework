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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import skillpro.model.properties.Property;

public abstract class Skill {
	private String name;
	private List<Property> properties = new ArrayList<>();
	private String id;
	private List<Object> amlElements = new ArrayList<>();
	
	public Skill(String name) {
		this.name = name;
	}
	
	public Skill(String name, List<Property> properties) {
		this.name = name;
		this.properties = properties;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public boolean addProperty(Property property) {
		if (!properties.contains(property)) {
			return properties.add(property);
		}
		return false;
	}
	
	public List<Property> getProperties() {
		return properties;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public List<Object> getAmlElements() {
		return amlElements;
	}
	
	public boolean addAMLElement(Object object) {
		if (!amlElements.contains(object)) {
			return amlElements.add(object);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, properties);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Skill other = (Skill) obj;
		return Objects.equals(id, other.id)
				&& Objects.equals(name, other.name)
				&& Objects.equals(properties, other.properties);
	}
}
