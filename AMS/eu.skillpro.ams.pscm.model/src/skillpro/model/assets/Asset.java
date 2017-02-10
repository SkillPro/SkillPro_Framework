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

package skillpro.model.assets;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import skillpro.model.costs.Cost;

public class Asset {
	private String name;
	private List<Cost> costs = new ArrayList<>();
	private List<Object> amlElements = new ArrayList<>();
	
	public Asset(String name) {
		this.name = name;
	}
	
	public Asset(String name, List<Cost> costs) {
		this.name = name;
		this.costs = costs;
	}
	
	public List<Object> getAmlElements() {
		return amlElements;
	}
	
	public boolean addAMLElement(Object object) {
		if (object != null && !amlElements.contains(object)) {
			return amlElements.add(object);
		}
		return false;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public List<Cost> getCosts() {
		return costs;
	}
	
	public boolean addCosts(Cost cost) {
		if (cost != null && !costs.contains(cost)) {
			costs.add(cost);
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Asset other = (Asset) obj;
		return Objects.equals(name, other.name);
	}
}
