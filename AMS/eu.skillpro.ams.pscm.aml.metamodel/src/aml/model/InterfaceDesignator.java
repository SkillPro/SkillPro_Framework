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

import aml.domain.Domain;
import aml.domain.Interface;

/**
 * This class represents an ExternalInterface in AML
 *
 * @author Otono Kakuei
 *
 */
public class InterfaceDesignator extends AMLObject {
	private String id;
	private Domain domain;
	private Interface baseInterface;
	private List<AttributeDesignator> attributeDesignators = new ArrayList<>();
	
	public InterfaceDesignator(String id, String name, Domain domain, Interface baseInterface) {
		super(name);
		this.id = id;
		this.domain = domain;
		this.baseInterface = baseInterface;
	}
	
	public Domain getDomain() {
		return domain;
	}
	
	public Interface getBaseInterface() {
		return baseInterface;
	}
	
	public String getId() {
		return id;
	}
	
	public List<AttributeDesignator> getDesignators() {
		return attributeDesignators;
	}
	
	public void setDesignators(List<AttributeDesignator> attributeDesignators) {
		this.attributeDesignators = attributeDesignators;
	}
	
	public boolean addDesignator(AttributeDesignator designator) {
		if (designator != null & !attributeDesignators.contains(designator)) {
			attributeDesignators.add(designator);
			return true;
		}
		return false;
	}

	public boolean addAttribute(Attribute attribute) {
		for (AttributeDesignator des : attributeDesignators) {
			if (des.getAttribute().equals(attribute)) {
				return false;
			}
		}
		return addDesignator(new AttributeDesignator(attribute, this));
	}
	
	public AttributeDesignator getDesignatorByAttribute(Attribute attribute) {
		for (AttributeDesignator des : attributeDesignators) {
			if (des.getAttribute().equals(attribute)) {
				return des;
			}
		}
		return null;
	}
	
	public AttributeDesignator getDesignatorByName(String name) {
		for (AttributeDesignator des : attributeDesignators) {
			if (des.getAttribute().getName().equalsIgnoreCase(name)) {
				return des;
			}
		}
		return null;
	}

	@Override
	public int hashCode() {
		return Objects.hash(baseInterface, id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		InterfaceDesignator other = (InterfaceDesignator) obj;
		return Objects.equals(baseInterface, other.baseInterface)
				&& Objects.equals(id, other.id);
	}
}
