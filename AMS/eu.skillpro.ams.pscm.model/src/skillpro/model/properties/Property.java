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
import java.util.UUID;

public class Property {
	private String name;
	private PropertyType type;
	private Property parent;
	private List<Property> subProperties = new ArrayList<Property>();
	private String unit;
	private String description;
	
	private String randomID = UUID.randomUUID().toString();
	
	public Property() {
	}
	
	/**
	 * A copy constructor, creates a deep copy of the parameter. The parent field is not cloned.
	 * @param property a Property
	 */
	public Property(Property property) {
		this.name = property.name;
		this.type = property.type;
		this.unit = property.unit;
		this.parent = property.parent;
		this.subProperties = new ArrayList<Property>();
		for (Property p : property.subProperties) {
			this.subProperties.add(new Property(p));
		}
		this.description = property.description;
	}
	
	public Property(String name, PropertyType type, String unit) {
		this.name = name;
		this.type = type;
		this.unit = unit;
	}

	public String getName() {
		return name;
	}
	
	public PropertyType getType() {
		return type;
	}
	
	public String getUnit() {
		return unit;
	}
	
	public String getRandomID() {
		return randomID;
	}
	
	public List<Property> getSubProperties() {
		return subProperties;
	}
	
	public Property getParent() {
		return parent;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public void setParent(Property parent) {
		this.parent = parent;
		if (parent != null) {
			parent.addProperty(this);
		}
	}
	
	public boolean addProperty(Property property) {
		if (property != null && !subProperties.contains(property)) {
			if (subProperties.add(property)) {
				if (property.getParent() == null) {
					setParent(this);
				}
				return true;
			}
		}
		
		return false;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void setUnit(String unit) {
		this.unit = unit;
	}
	
	public void setType(PropertyType type) {
		this.type = type;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(name, type, unit);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Property other = (Property) obj;
		return Objects.equals(name, other.name)
				&& Objects.equals(type, other.type)
				&& Objects.equals(unit, other.unit);
	}
}
