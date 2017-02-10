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

package calculation.model.state;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import skillpro.model.assets.Resource;
import skillpro.model.assets.ResourceConfiguration;
import skillpro.model.products.ProductConfiguration;
import skillpro.model.skills.Requirement;
import calculation.model.Condition;

public abstract class State {
	//the current state of the resources
	protected Map<Resource, Condition> currentStateMap = new HashMap<>();
	
	public Map<Resource, Condition> getCurrentStateMap() {
		return currentStateMap;
	}
	
	protected final boolean fulfillsPreRequirement(Requirement preRequirement, Condition configuration) {
		boolean fulfillsPreRequirementRConf = fulfillsPreRequirement(preRequirement, configuration.getFirstElement());
		boolean fulfillsPreRequirementPConf = fulfillsPreRequirement(preRequirement, configuration.getSecondElement());
		return fulfillsPreRequirementRConf && fulfillsPreRequirementPConf;
	}
	
	private boolean fulfillsPreRequirement(Requirement preRequirement, ResourceConfiguration resourceConfiguration) {
		ResourceConfiguration requiredResourceConfiguration = preRequirement.getRequiredResourceConfiguration();
		
		switch (preRequirement.getResourceConfigType()) {
		case ANY:
			Resource requiredResource = null;
			if (preRequirement.getRequiredResourceSkill() != null) {
				requiredResource = preRequirement.getRequiredResourceSkill().getResource();
			} else if (requiredResourceConfiguration != null) {
				requiredResource = requiredResourceConfiguration.getResource();
			} else {
				throw new IllegalArgumentException("Cannot extract mainResource from Requirement: " + preRequirement);
			}
			return requiredResource.equals(resourceConfiguration.getResource());
		case SPECIFIC: 
			return requiredResourceConfiguration != null && requiredResourceConfiguration.equals(resourceConfiguration);
		default:
			throw new IllegalArgumentException("Error checking PreRequirement: " + preRequirement);
		}
	}
	
	private boolean fulfillsPreRequirement(Requirement preRequirement, ProductConfiguration productConfiguration) {
		ProductConfiguration requiredProductConfiguration = preRequirement.getRequiredProductConfiguration();
		
		switch (preRequirement.getProductConfigType()) {
		case ANY:
			return true;
		case SPECIFIC: 
			//TODO change equals of ProductConfiguration / remove ProductQuantity and replace it with a Map
			return requiredProductConfiguration != null && requiredProductConfiguration.equals(productConfiguration);
		case EMPTY:
			return productConfiguration == null;
		default:
			throw new IllegalArgumentException("Error checking PreRequirement: " + preRequirement);
		}
	}
	
	@Override
	public String toString() {
		return "CurrentStateMap: " + currentStateMap.toString();
	}

	@Override
	public int hashCode() {
		return Objects.hash(currentStateMap);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		State other = (State) obj;
		return Objects.equals(currentStateMap, other.currentStateMap);
	}
}
