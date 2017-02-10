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

package aml.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Attribute {
	private String name;
	private String description;
	private Attribute parent;
	private List<Attribute> subAttributes = new ArrayList<>();
	private List<AttributeDesignator> designators = new ArrayList<>();
	private AttributeType attributeType;
	private String unit;
	
	public Attribute(String name, AttributeType attributeType, String unit) {
		this.name = name;
		this.attributeType = attributeType;
		if (unit.equals("")) {
			this.unit = null;
		} else {
			this.unit = unit;
		}
	}
	
	public Attribute(String name, String attributeType, String unit) {
		this.name = name;
		this.attributeType = convertStringToAttributeType(attributeType);
		this.unit = unit;
	}
	
	private AttributeType convertStringToAttributeType(String attributeType) {
		AttributeType result = null;
		if (attributeType == null) {
			//FIXME logical?
			return AttributeType.STRING;
		}
		//double here too?
		String attributeTypeWithoutXS = attributeType.replace("xs:", "");
		if (attributeTypeWithoutXS.toLowerCase().contains("float") || attributeTypeWithoutXS.equalsIgnoreCase(AttributeType.DOUBLE.toString())) {
			result = AttributeType.DOUBLE;
		} else if (attributeTypeWithoutXS.equalsIgnoreCase(AttributeType.INTEGER.toString())) {
			result = AttributeType.INTEGER;
		} else if (attributeTypeWithoutXS.equalsIgnoreCase(AttributeType.STRING.toString())) {
			result = AttributeType.STRING;
		} else if (attributeTypeWithoutXS.equalsIgnoreCase(AttributeType.BOOLEAN.toString())) {
			result = AttributeType.BOOLEAN;
		} else if (attributeTypeWithoutXS.equalsIgnoreCase(AttributeType.COMPLEX_TYPE.toString())) {
			result = AttributeType.COMPLEX_TYPE;
		}
		return result;
	}

	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public List<Attribute> getSubAttributes() {
		return subAttributes;
	}
	
	public Attribute getParent() {
		return parent;
	}
	
	public void setParent(Attribute parent) {
		this.parent = parent;
		if (parent != null) {
			parent.addAttribute(this);
		}
	}
	
	public boolean addAttribute(Attribute attribute) {
		if (attribute != null && !subAttributes.contains(attribute)) {
			if (subAttributes.add(attribute)) {
				if (attribute.getParent() == null) {
					attribute.setParent(this);
				}
				return true;
			}
		}
		
		return false;
	}
	
	public AttributeType getAttributeType() {
		return attributeType;
	}
	
	public String getUnit() {
		return unit;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public List<AttributeDesignator> getDesignators() {
		return designators;
	}
	
	public boolean addDesignator(AttributeDesignator designator) {
		if (designator != null & !designators.contains(designator)) {
			AttributeDesignator desByAttribute = getDesignatorByAttribute(designator.getAttribute());
			if (desByAttribute != null) {
				if (desByAttribute.getValue() == null || desByAttribute.getValue().isEmpty()) {
					desByAttribute.setValue(designator.getValue());
				}
				
				if (desByAttribute.getConstraints().isEmpty()) {
					desByAttribute.getConstraints().addAll(designator.getConstraints());
					
				}
			} else {
				designators.add(designator);
			}
			return true;
		}
		return false;
	}
	
	public AttributeDesignator getDesignatorByAttribute(Attribute attribute) {
		for (AttributeDesignator des : designators) {
			if (des.getAttribute().equals(attribute)) {
				return des;
			}
		}
		return null;
	}

	@Override
	public int hashCode() {
		return Objects.hash(attributeType, name, parent, unit);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Attribute other = (Attribute) obj;
		return Objects.equals(attributeType, other.attributeType)
				&& Objects.equals(name, other.name)
				&& Objects.equals(parent, other.parent)
				&& Objects.equals(unit, other.unit);
	}
	
	@Override
	public String toString() {
		return "Attribute[name=\"" + name + "\", type=\"" + attributeType + "\", unit=\"" + unit +  "\", parent=\"" + parent + "\"]";
	}
}
