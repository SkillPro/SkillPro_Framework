/**
 * 
 */
/*****************************************************************************
 *
 * Copyright 2012-2016 SkillPro Consortium
 *
 * Author: PDE, FZI, pde@fzi.de
 *
 * Date of creation: 2012-2016
 *
 * Module: AMS Server
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

package eu.skillpro.ams.service.to.assets;

import java.util.ArrayList;
import java.util.List;

import skillpro.model.properties.Property;
import skillpro.model.properties.PropertyConstraint;
import skillpro.model.properties.PropertyDesignator;

/**
 * @author caliqi
 * @date 25.09.2014
 * 
 */
public class AttributeTO {
	private String id;
	private String name;
	private String type;
	private String unit;
	private String description;
	private String value;
	private List<AttributeConstraintTO> attributeConstraints = new ArrayList<AttributeConstraintTO>();

	/**
	 * 
	 */
	public AttributeTO(String id, String name, String value) {
		this.id = id;
		this.name = name;
		this.value = value;
	}

	/**
	 * @param property
	 * @param propertyDesignator
	 */
	public AttributeTO(Property property, PropertyDesignator propertyDesignator) {
		this.id = property.getRandomID();
		this.name = property.getName();
		this.type = property.getType().toString();
		this.unit = property.getUnit();
		this.description = property.getDescription();

		if (propertyDesignator != null) {
			this.value = propertyDesignator.getValue();
			for (PropertyConstraint pc : propertyDesignator.getConstraints()) {
				attributeConstraints.add(new AttributeConstraintTO(pc));
			}
		}
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the unit
	 */
	public String getUnit() {
		return unit;
	}

	/**
	 * @param unit
	 *            the unit to set
	 */
	public void setUnit(String unit) {
		this.unit = unit;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * @return the attributeConstraints
	 */
	public List<AttributeConstraintTO> getAttributeConstraints() {
		return attributeConstraints;
	}

	/**
	 * @param attributeConstraints
	 *            the attributeConstraints to set
	 */
	public void setAttributeConstraints(
			List<AttributeConstraintTO> attributeConstraints) {
		this.attributeConstraints = attributeConstraints;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "AttributeTOv2 [id=" + id + ", name=" + name + ", type=" + type
				+ ", unit=" + unit + ", description=" + description
				+ ", value=" + value + ", attributeConstraints="
				+ attributeConstraints + "]";
	}
}
