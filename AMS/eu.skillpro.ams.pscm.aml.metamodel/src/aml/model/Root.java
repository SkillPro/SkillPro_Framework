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


public class Root<D extends Domain> {
	private String name;
	private List<Hierarchy<D>> children;
	private Class<D> domainClass;
	
	public Root(String name, Class<D> domainClass) {
		this.name = name;
		this.domainClass = domainClass;
		children = new ArrayList<>();
	}
	
	public List<Hierarchy<D>> getChildren() {
		return children;
	}
	
	public String getName() {
		return name;
	}
	
	public Class<D> getDomainClass() {
		return domainClass;
	}
	
	public boolean addChild(Hierarchy<D> child) {
		if (child != null && !children.contains(child)) {
			children.add(child);
			return true;
		}
		return false;
	}
	
	public boolean containsExactHierarchy(Hierarchy<?> child) {
		for (Hierarchy<D> candidate : children) {
			if (candidate == child) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public String toString() {
		return domainClass.getSimpleName() + ": " + name;
	}

	@Override
	public int hashCode() {
		return Objects.hash(children, domainClass);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Root<?> other = (Root<?>) obj;
		return Objects.equals(children, other.children)
				&& Objects.equals(domainClass, other.domainClass)
				&& Objects.equals(name, other.name);
	}
}
