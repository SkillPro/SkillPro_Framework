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
import java.util.Objects;

import aml.model.InterfaceDesignator;


public class Role extends Domain {
	private Role referencedRole;
	private List<InterfaceDesignator> interfaceDesignators = new ArrayList<>();
	private List<Role> children = new ArrayList<>();
	private String description;
	
	public Role(String name, Role referencedRole) {
		super(name);
		setReferencedRole(referencedRole);
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setReferencedRole(Role referencedRole) {
		this.referencedRole = referencedRole;
		if (referencedRole != null) {
			referencedRole.addChild(this);
		}
	}
	
	public List<Role> getChildren() {
		return children;
	}
	
	public Role getReferencedRole() {
		return referencedRole;
	}
	
	private List<InterfaceDesignator> getInheritedInterfaceDesignators() {
		List<InterfaceDesignator> designators = new ArrayList<>();
		if (referencedRole != null) {
			designators.addAll(referencedRole.getInheritedInterfaceDesignators());
			designators.addAll(referencedRole.getInterfaceDesignators());
		}
		
		return designators;
	}
	
	public List<InterfaceDesignator> getAllInterfaceDesignators() {
		List<InterfaceDesignator> designators = new ArrayList<>();
		designators.addAll(getInheritedInterfaceDesignators());
		designators.addAll(getInterfaceDesignators());
		
		return designators;
	}
	
	public List<InterfaceDesignator> getInterfaceDesignators() {
		return interfaceDesignators;
	}
	
	public boolean addInterfaceDesignator(InterfaceDesignator designator) {
		if (designator != null && !interfaceDesignators.contains(designator)) {
			return interfaceDesignators.add(designator);
		}
		return false;
	}
	
	public boolean addChild(Role role) {
		if (role != null && !children.contains(role)) {
			return children.add(role);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), interfaceDesignators, referencedRole);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Role other = (Role) obj;
		return Objects.equals(interfaceDesignators, other.interfaceDesignators)
				&& Objects.equals(referencedRole, other.referencedRole);
	}

	@Override
	public String toString() {
		return super.toString() + ": " + getName();
	}
}
