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
import java.util.UUID;

import aml.model.InterfaceDesignator;
import aml.model.InternalLink;


public class InternalElement extends Domain {
	private String id;
	private List<Role> supportedRoles = new ArrayList<>();
	private Role requiredRole;
	private List<InterfaceDesignator> interfaceDesignators = new ArrayList<>();
	private SystemUnit systemUnit;
	private List<InternalLink> internalLinks = new ArrayList<>();
	private InternalElement referencedInternalElement;
	
	public InternalElement(String id, String name) {
		super(name);
		this.id = id;
	}
	
	public InternalElement(String id, String name, Role requiredRole) {
		super(name);
		this.id = id;
		setRequiredRole(requiredRole);
		
	}
	
	public InternalElement getReferencedInternalElement() {
		return referencedInternalElement;
	}
	
	public void setReferencedInternalElement(
			InternalElement referencedInternalElement) {
		this.referencedInternalElement = referencedInternalElement;
	}
	
	public String getId() {
		return id;
	}
	
	public List<Role> getSupportedRoles() {
		return supportedRoles;
	}
	
	public Role getRequiredRole() {
		return requiredRole;
	}
	
	public void setRequiredRole(Role requiredRole) {
		this.requiredRole = requiredRole;
		List<InterfaceDesignator> allRoleInterfaceDesignators = requiredRole.getAllInterfaceDesignators();
		if (allRoleInterfaceDesignators != null) {
			for (InterfaceDesignator des : allRoleInterfaceDesignators) {
				//add inherited interface designators only when it hasn't existed yet
				if (getInterfaceDesignatorByName(des.getName()) == null) {
					interfaceDesignators.add(new InterfaceDesignator(UUID.randomUUID().toString()
							, des.getName(), this, des.getBaseInterface()));
				}
			}
		}
	}
	
	public SystemUnit getSystemUnit() {
		return systemUnit;
	}

	public void setSystemUnit(SystemUnit systemUnit) {
		this.systemUnit = systemUnit;
	}
	
	public List<InterfaceDesignator> getInterfaceDesignators() {
		return interfaceDesignators;
	}
	
	public InterfaceDesignator getInterfaceDesignatorByName(String name) {
		for (InterfaceDesignator des : interfaceDesignators) {
			if (des.getName().equalsIgnoreCase(name)) {
				return des;
			}
		}
		return null;
	}
	
	public boolean addInterfaceDesignator(InterfaceDesignator designator) {
		if (designator != null && !interfaceDesignators.contains(designator)) {
			interfaceDesignators.add(designator);
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
	
	public List<InternalLink> getInternalLinks() {
		return internalLinks;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(id, internalLinks, requiredRole, supportedRoles, systemUnit);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		InternalElement other = (InternalElement) obj;
		return Objects.equals(id, other.id)
				&& Objects.equals(internalLinks, other.internalLinks)
				&& Objects.equals(requiredRole, other.requiredRole)
				&& Objects.equals(supportedRoles, other.supportedRoles)
				&& Objects.equals(systemUnit, other.systemUnit);
	}
}
