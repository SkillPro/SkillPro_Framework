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

import aml.model.Hierarchy;
import aml.model.InterfaceDesignator;
import aml.model.InternalLink;


public class SystemUnit extends Domain {
	private List<InterfaceDesignator> interfaceDesignators = new ArrayList<>();
	private List<Hierarchy<InternalElement>> ieHierarchies = new ArrayList<>();
	private List<SystemUnit> children = new ArrayList<>();
	private SystemUnit referencedSystemUnit;
	private List<Role> supportedRoles = new ArrayList<>();
	private List<InternalLink> internalLinks = new ArrayList<>();
	
	public SystemUnit(String name, SystemUnit referencedSystemUnit) {
		super(name);
		this.referencedSystemUnit = referencedSystemUnit;
	}
	
	public boolean addIEHierarchy(Hierarchy<InternalElement> hie) {
		if (hie != null && !ieHierarchies.contains(hie)) {
			ieHierarchies.add(hie);
			return true;
		}
		
		return false;
	}
	
	public void setReferencedSystemUnit(SystemUnit referencedSystemUnit) {
		this.referencedSystemUnit = referencedSystemUnit;
		if (referencedSystemUnit != null) {
			referencedSystemUnit.addChild(this);
		}
	}
	
	public boolean addChild(SystemUnit child) {
		if (child != null && !children.contains(child)) {
			children.add(child);
			return true;
		}
		return false;
	}
	
	public boolean addSupportedRole(Role sup) {
		if (sup != null && !supportedRoles.contains(sup)) {
			supportedRoles.add(sup);
			return true;
		}
		return false;
	}
	
	public boolean addInternalLink(InternalLink link) {
		if (link != null && !internalLinks.contains(link)) {
			internalLinks.add(link);
			return true;
		}
		return false;
	}
	
	public boolean addInterfaceDesignator(InterfaceDesignator designator) {
		if (designator != null && !interfaceDesignators.contains(designator)) {
			interfaceDesignators.add(designator);
			return true;
		}
		return false;
	}
	
	public List<InterfaceDesignator> getInterfaceDesignators() {
		return interfaceDesignators;
	}
	
	public List<Hierarchy<InternalElement>> getIeHierarchies() {
		return ieHierarchies;
	}
	
	public SystemUnit getReferencedSystemUnit() {
		return referencedSystemUnit;
	}
	
	public List<InternalLink> getInternalLinks() {
		return internalLinks;
	}
	
	public List<SystemUnit> getChildren() {
		return children;
	}
	
	public List<Role> getSupportedRoles() {
		return supportedRoles;
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), internalLinks, referencedSystemUnit, supportedRoles);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		SystemUnit other = (SystemUnit) obj;
		return Objects.equals(internalLinks, other.internalLinks)
				&& Objects.equals(referencedSystemUnit, other.referencedSystemUnit)
				&& Objects.equals(supportedRoles, other.supportedRoles);
	}
}
