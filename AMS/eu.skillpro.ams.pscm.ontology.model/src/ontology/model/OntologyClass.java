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

public class OntologyClass {
	private String name;
	private Set<OntologyClass> parents = new HashSet<>();
	private Set<OntologyClass> disjoints = new HashSet<>();
	private Set<OntologyClass> disjointUnions = new HashSet<>();
	
	public OntologyClass(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public Set<OntologyClass> getParents() {
		return parents;
	}
	
	public Set<OntologyClass> getDisjoints() {
		return disjoints;
	}
	
	public Set<OntologyClass> getDisjointUnions() {
		return disjointUnions;
	}
	
	public boolean addParent(OntologyClass parent) {
		if (parent != null && !parents.contains(parent)) {
			return parents.add(parent);
		}
		return false;
	}
	
	public boolean addDisjoint(OntologyClass disjoint) {
		if (disjoint != null && !disjoints.contains(disjoint)) {
			return disjoints.add(disjoint);
		}
		return false;
	}
	
	public boolean addDisjointUnion(OntologyClass disjoint) {
		if (disjoint != null && !disjointUnions.contains(disjoint)) {
			return disjointUnions.add(disjoint);
		}
		return false;
	}
}
