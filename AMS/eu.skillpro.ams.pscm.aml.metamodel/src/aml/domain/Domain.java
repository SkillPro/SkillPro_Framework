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

package aml.domain;

import java.util.ArrayList;
import java.util.List;

import aml.model.AMLObject;
import aml.model.Attribute;
import aml.model.AttributeDesignator;

public abstract class Domain extends AMLObject {
	private List<AttributeDesignator> designators = new ArrayList<>();
	
	public Domain(String name) {
		super(name);
	}
	
	public List<AttributeDesignator> getDesignators() {
		return designators;
	}
	
	public void setDesignators(List<AttributeDesignator> designators) {
		this.designators = designators;
	}
	
	public boolean addDesignator(AttributeDesignator designator) {
		if (designator != null & !designators.contains(designator)) {
			AttributeDesignator desByAttribute = getDesignatorByAttribute(designator.getAttribute());
			if (desByAttribute != null) {
				if (desByAttribute.getValue() == null || desByAttribute.getValue().isEmpty()) {
					desByAttribute.setValue(designator.getValue());
					return false;
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
	
	public boolean addAttribute(Attribute attribute) {
		for (AttributeDesignator des : designators) {
			if (des.getAttribute().equals(attribute)) {
				return false;
			}
		}
		return addDesignator(new AttributeDesignator(attribute, this));
	}
	
	public AttributeDesignator getDesignatorByAttribute(Attribute attribute) {
		for (AttributeDesignator des : designators) {
			if (des.getAttribute().equals(attribute)) {
				return des;
			}
		}
		return null;
	}
	
	public AttributeDesignator getDesignatorByName(String name) {
		AttributeDesignator result = null;
		for (AttributeDesignator des : designators) {
			if (des.getAttribute().getName().equals(name)) {
				if (result != null) {
					throw new IllegalArgumentException("Cannot ensure correctness. More than 1 attribute with the same name detected!");
				}
				result = des;
			}
		}
		return result;
	}
	
	public List<Attribute> getAttributes() {
		List<Attribute> normals = new ArrayList<>();
		for (AttributeDesignator des : designators) {
			normals.add(des.getAttribute());
		}
		return normals;
	}
	
	public String getReferencedTemplateSkillPath() {
		String value = "";
		for (AttributeDesignator des : designators) {
			if (des.getAttribute().getName().equalsIgnoreCase("RefTemplateSkill")) {
				value = des.getValue();
			} else if (des.getAttribute().getName().equalsIgnoreCase("SupportedRoleClass")) {
				value = des.getValue();
			} else if (des.getAttribute().getName().equalsIgnoreCase("SupportedRolePath")) {
				value = des.getValue();
			}
		}
		return value;
	}
}
