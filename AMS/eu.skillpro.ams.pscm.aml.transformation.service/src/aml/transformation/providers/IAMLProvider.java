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

package aml.transformation.providers;

import java.util.Collection;
import java.util.List;

import aml.domain.Domain;
import aml.domain.Interface;
import aml.domain.InternalElement;
import aml.domain.Role;
import aml.domain.SystemUnit;
import aml.model.AMLObject;
import aml.model.Attribute;
import aml.model.AttributeDesignator;
import aml.model.Constraint;
import aml.model.Hierarchy;
import aml.model.InterfaceDesignator;
import aml.model.InternalLink;
import aml.model.Root;
import aml.transformation.repo.aml.AMLInterfaceRepo;
import aml.transformation.repo.aml.AMLInternalElementRepo;
import aml.transformation.repo.aml.AMLModelRepo;
import aml.transformation.repo.aml.AMLRoleRepo;
import aml.transformation.repo.aml.AMLSystemUnitRepo;

public interface IAMLProvider {
	//read
	public AMLInternalElementRepo getAMLInternalElementRepo();
	public AMLInterfaceRepo getAMLInterfaceRepo();
	public AMLRoleRepo getAMLRoleRepo();
	public AMLSystemUnitRepo getAMLSystemUnitRepo();
	public <D extends Domain> AMLModelRepo<D> getAMLModelRepo(Class<D> domain);
	
	public Collection<Class<? extends Domain>> getAllDomains();

	//create
	public Interface createInterface(String name, Interface referencedInterface);
	public void storeInterface(Interface inter);
	public InternalElement createInternalElement(String id, String name);
	public void storeInternalElement(InternalElement internalElement);
	public Role createRole(String name, Role referencedRole);
	public void storeRole(Role role);
	public SystemUnit createSystemUnit(String name, SystemUnit referencedSystemUnit);
	public void storeSystemUnit(SystemUnit systemUnit);

	public Attribute createAttribute(String name, String attributeType,
			String unit);
	public AttributeDesignator createAttributeDesignator(Attribute attribute,
			AMLObject amlObject);
	public AttributeDesignator createAttributeDesignator(Attribute attribute,
			AMLObject amlObject, List<Constraint> constraints);
	public AttributeDesignator createAttributeDesignator(Attribute attribute,
			AMLObject amlObject, List<Constraint> constraints, String value);
	public InterfaceDesignator createInterfaceDesignator(String id,
			String name, Domain domain, Interface externalInterface);
	public InternalLink createInternalLink(String name,
			InterfaceDesignator refA, InterfaceDesignator refB);
	
	public <D extends Domain> Hierarchy<D> createHierarchy(String name,
			D element);
	public <D extends Domain> void storeHierarchy(Hierarchy<D> hie);
	public <D extends Domain> Root<D> createRoot(String name, Class<D> domainClass);
	public <D extends Domain> void storeRoot(Root<D> root);
	//remove
	public void removeInterface(Interface interfais);
	public void removeInternalElement(InternalElement internalElement);
	public void removeRole(Role role);
	public void removeSystemUnit(SystemUnit systemUnit);

	public <D extends Domain> void removeRoot(Class<D> domainClass, Root<?> root);

	//update
	public void updateInterface(Interface interfais);
	public void updateInternalElement(InternalElement internalElement);
	public void updateRole(Role role);
	public void updateSystemUnit(SystemUnit systemUnit);
	
	public void updateHierarchy(Hierarchy<?> hierarchy);
	public void updateRoot(Root<?> root);
	
	//etc
	public boolean isDirty();
	public void wipeAllData();
}
