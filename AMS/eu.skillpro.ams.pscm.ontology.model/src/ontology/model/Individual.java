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

package ontology.model;

import java.util.HashSet;
import java.util.Set;

import ontology.model.property.DataPropertyDesignator;
import ontology.model.property.ObjectPropertyDesignator;

public class Individual {
	private String name;
	private Set<OntologyClass> types = new HashSet<>();
	private Set<DataPropertyDesignator> dataPropertyDesignators = new HashSet<>();
	private Set<ObjectPropertyDesignator> objectPropertyDesignators = new HashSet<>();
	
	public Individual(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public Set<OntologyClass> getTypes() {
		return types;
	}
	
	public boolean addType(OntologyClass type) {
		if (type != null && !types.contains(type)) {
			return types.add(type);
		}
		
		return false;
	}
	
	public boolean addDataPropertyDesignator(DataPropertyDesignator des) {
		if (des != null && !dataPropertyDesignators.contains(des)) {
			return dataPropertyDesignators.add(des);
		}
		
		return false;
	}
	
	public boolean addObjectPropertyDesignator(ObjectPropertyDesignator des) {
		if (des != null && !objectPropertyDesignators.contains(des)) {
			return objectPropertyDesignators.add(des);
		}
		
		return false;
	}
	
	public Set<DataPropertyDesignator> getDataPropertyDesignators() {
		return dataPropertyDesignators;
	}
	
	public Set<ObjectPropertyDesignator> getObjectPropertyDesignators() {
		return objectPropertyDesignators;
	}
}

