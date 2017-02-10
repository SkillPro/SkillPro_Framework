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

package ontology.model.property;

import java.util.HashSet;
import java.util.Set;

public class ObjectProperty extends Property {
	private Set<ObjectProperty> parents = new HashSet<>();
	private PropertyCardinalityCharacteristic cardinalityCharacteristic = PropertyCardinalityCharacteristic.NONE;
	private PropertyLogicalCharacteristic logicalCharacteristic = PropertyLogicalCharacteristic.NONE;
	private PropertyReflexiveCharacteristic reflexiveCharacteristic = PropertyReflexiveCharacteristic.NONE;
	
	public ObjectProperty(String name) {
		super(name);
	}
	
	public void setCardinalityCharacteristic(
			PropertyCardinalityCharacteristic cardinalityCharacteristic) {
		this.cardinalityCharacteristic = cardinalityCharacteristic;
	}
	
	public void setLogicalCharacteristic(
			PropertyLogicalCharacteristic logicalCharacteristic) {
		this.logicalCharacteristic = logicalCharacteristic;
	}
	
	public void setReflexiveCharacteristic(
			PropertyReflexiveCharacteristic reflexiveCharacteristic) {
		this.reflexiveCharacteristic = reflexiveCharacteristic;
	}

	public PropertyCardinalityCharacteristic getCardinalityCharacteristic() {
		return cardinalityCharacteristic;
	}
	
	public PropertyLogicalCharacteristic getLogicalCharacteristic() {
		return logicalCharacteristic;
	}
	
	public PropertyReflexiveCharacteristic getReflexiveCharacteristic() {
		return reflexiveCharacteristic;
	}
	
	public Set<ObjectProperty> getParents() {
		return parents;
	}
	
	public boolean addParent(ObjectProperty parent) {
		if (parent != null && !parents.contains(parent)) {
			return parents.add(parent);
		}
		return false;
	}
}
