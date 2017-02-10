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

package aml.transformation.providers.local;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import aml.transformation.providers.IAMLProvider;
import aml.transformation.repo.aml.AMLInterfaceRepo;
import aml.transformation.repo.aml.AMLInternalElementRepo;
import aml.transformation.repo.aml.AMLModelRepo;
import aml.transformation.repo.aml.AMLRoleRepo;
import aml.transformation.repo.aml.AMLSystemUnitRepo;

public class LocalAMLProvider implements IAMLProvider {
	private AMLInternalElementRepo amlInternalElementRepo;
	private AMLInterfaceRepo amlInterfaceRepo;
	private AMLRoleRepo amlRoleRepo;
	private AMLSystemUnitRepo amlSystemUnitRepo;
	private Map<Class<? extends Domain>, AMLModelRepo<? extends Domain>> amlModelRepos = new HashMap<>();
	
	public LocalAMLProvider() {
	}

	@Override
	public AMLInternalElementRepo getAMLInternalElementRepo() {
		if (amlInternalElementRepo == null) {
			amlInternalElementRepo = new AMLInternalElementRepo();
		}
		return amlInternalElementRepo;
	}
	
	@Override
	public AMLInterfaceRepo getAMLInterfaceRepo() {
		if (amlInterfaceRepo == null) {
			amlInterfaceRepo = new AMLInterfaceRepo();
		}
		return amlInterfaceRepo;
	}
	
	@Override
	public AMLRoleRepo getAMLRoleRepo() {
		if (amlRoleRepo == null) {
			amlRoleRepo = new AMLRoleRepo();
		}
		return amlRoleRepo;
	}
	
	@Override
	public AMLSystemUnitRepo getAMLSystemUnitRepo() {
		if (amlSystemUnitRepo == null) {
			amlSystemUnitRepo = new AMLSystemUnitRepo();
		}
		return amlSystemUnitRepo;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <D extends Domain> AMLModelRepo<D> getAMLModelRepo(Class<D> domain) {
		AMLModelRepo<D> modelRepo;
		if (!amlModelRepos.containsKey(domain)) {
			modelRepo = new AMLModelRepo<>(domain);
			amlModelRepos.put(domain, modelRepo);
		} else {
			modelRepo = (AMLModelRepo<D>) amlModelRepos.get(domain);
		}
		return modelRepo;
	}
	
	@Override
	public Set<Class<? extends Domain>> getAllDomains() {
		Set<Class<? extends Domain>> classes = amlModelRepos.keySet();
		return classes;
	}

	@Override
	public Interface createInterface(String name, Interface referencedInterface) {
		Interface interfais = new Interface(name, referencedInterface);
		if (interfais.getReferencedInterface() == null) {
			getAMLInterfaceRepo().getEntities().add(interfais);
		}
		return interfais;
	}

	@Override
	public InternalElement createInternalElement(String id, String name) {
		InternalElement internalElement = new InternalElement(id, name);
		getAMLInternalElementRepo().getEntities().add(internalElement);
		return internalElement;
	}

	@Override
	public Role createRole(String name, Role referencedRole) {
		Role role = new Role(name, referencedRole);
		if (referencedRole == null) {
			getAMLRoleRepo().getEntities().add(role);
		}
		return role;
	}

	@Override
	public SystemUnit createSystemUnit(String name, SystemUnit referencedSystemUnit) {
		SystemUnit systemUnit = new SystemUnit(name, referencedSystemUnit);
		if (referencedSystemUnit == null) {
			getAMLSystemUnitRepo().getEntities().add(systemUnit);
		}
		return systemUnit;
	}

	@Override
	public Attribute createAttribute(String name, String attributeType,
			String unit) {
		Attribute attribute = new Attribute(name, attributeType, unit);
		return attribute;
	}

	@Override
	public AttributeDesignator createAttributeDesignator(Attribute attribute,
			AMLObject amlObject) {
		AttributeDesignator designator = new AttributeDesignator(attribute, amlObject);
		return designator;
	}

	@Override
	public AttributeDesignator createAttributeDesignator(Attribute attribute,
			AMLObject amlObject, List<Constraint> constraints) {
		AttributeDesignator designator = new AttributeDesignator(attribute, amlObject, constraints);
		return designator;
	}

	@Override
	public AttributeDesignator createAttributeDesignator(Attribute attribute,
			AMLObject amlObject, List<Constraint> constraints, String value) {
		AttributeDesignator designator = new AttributeDesignator(attribute, amlObject, constraints, value);
		return designator;
	}
	
	@Override
	public InterfaceDesignator createInterfaceDesignator(String id,
			String name, Domain domain, Interface externalInterface) {
		InterfaceDesignator designator = new InterfaceDesignator(id, name, domain, externalInterface);
		return designator;
	}

	@Override
	public InternalLink createInternalLink(String name,
			InterfaceDesignator refA, InterfaceDesignator refB) {
		InternalLink internalLink = new InternalLink(name, refA, refB);
		return internalLink;
	}

	@Override
	public <D extends Domain> Hierarchy<D> createHierarchy(String name,
			D element) {
		Hierarchy<D> hierarchy = new Hierarchy<D>(name, element);
		getAMLModelRepo(element.getClass()).addToFlattenedHierarchies(hierarchy);
		return hierarchy;
	}

	@Override
	public <D extends Domain> Root<D> createRoot(String name, Class<D> domainClass) {
		Root<D> root = new Root<>(name, domainClass);
		getAMLModelRepo(domainClass).getEntities().add(root);
		return root;
	}

	@Override
	public void removeInterface(Interface interfais) {
		getAMLInterfaceRepo().getEntities().remove(interfais);
	}


	@Override
	public void removeInternalElement(InternalElement internalElement) {
		getAMLInternalElementRepo().getEntities().remove(internalElement);
	}


	@Override
	public void removeRole(Role role) {
		getAMLRoleRepo().getEntities().remove(role);
	}


	@Override
	public void removeSystemUnit(SystemUnit systemUnit) {
		getAMLSystemUnitRepo().getEntities().remove(systemUnit);
		
	}


	@Override
	public <D extends Domain> void removeRoot(Class<D> domainClass, Root<?> root) {
		getAMLModelRepo(domainClass).getEntities().remove(root);
	}


	@Override
	public void updateInterface(Interface interfais) {
		List<Interface> entities = getAMLInterfaceRepo().getEntities();
		if (interfais.getReferencedInterface() != null) {
			if (entities.contains(interfais)) {
				entities.remove(interfais);
			}
		} else {
			if (!entities.contains(interfais)) {
				entities.add(interfais);
			}
		}
	}


	@Override
	public void updateInternalElement(InternalElement internalElement) {
	}


	@Override
	public void updateRole(Role role) {
		List<Role> entities = getAMLRoleRepo().getEntities();
		if (role.getReferencedRole() != null) {
			if (entities.contains(role)) {
				entities.remove(role);
			}
		} else {
			if (!entities.contains(role)) {
				entities.add(role);
			}
		}
	}


	@Override
	public void updateSystemUnit(SystemUnit systemUnit) {
		List<SystemUnit> entities = getAMLSystemUnitRepo().getEntities();
		if (systemUnit.getReferencedSystemUnit() != null) {
			if (entities.contains(systemUnit)) {
				entities.remove(systemUnit);
			}
		} else {
			if (!entities.contains(systemUnit)) {
				entities.add(systemUnit);
			}
		}
	}

	@Override
	public void updateHierarchy(Hierarchy<?> hierarchy) {
	}
	
	@Override
	public void updateRoot(Root<?> root) {
	}
	
	private int getCompleteModelRepoSize() {
		int totalSize = 0;
		for (Class<? extends Domain> domainClass : getAllDomains()) {
			totalSize = totalSize + getAMLModelRepo(domainClass).size();
		}
		return totalSize;
	}


	@Override
	public boolean isDirty() {
		return (getAMLInterfaceRepo().size() + getAMLInternalElementRepo().size() 
				+ getCompleteModelRepoSize() + getAMLRoleRepo().size() + getAMLSystemUnitRepo().size()) > 0;
	}

	@Override
	public void wipeAllData() {
		getAMLInterfaceRepo().wipeAllData();
		getAMLInternalElementRepo().wipeAllData();
		getAMLRoleRepo().wipeAllData();
		getAMLSystemUnitRepo().wipeAllData();
		for (Class<? extends Domain> domain : getAllDomains()) {
			getAMLModelRepo(domain).wipeAllData();
		}
	}

	@Override
	public void storeInterface(Interface inter) {
		if (inter.getReferencedInterface() == null) {
			getAMLInterfaceRepo().getEntities().add(inter);
		}
	}

	@Override
	public void storeInternalElement(InternalElement internalElement) {
		getAMLInternalElementRepo().getEntities().add(internalElement);
	}

	@Override
	public void storeRole(Role role) {
		if (role.getReferencedRole() == null) {
			getAMLRoleRepo().getEntities().add(role);
		}
	}

	@Override
	public void storeSystemUnit(SystemUnit systemUnit) {
		if (systemUnit.getReferencedSystemUnit() == null) {
			getAMLSystemUnitRepo().getEntities().add(systemUnit);
		}		
	}

	@Override
	public <D extends Domain> void storeHierarchy(Hierarchy<D> hie) {
		getAMLModelRepo(hie.getElement().getClass()).addToFlattenedHierarchies(hie);
	}

	@Override
	public <D extends Domain> void storeRoot(Root<D> root) {
		getAMLModelRepo(root.getDomainClass()).getEntities().add(root);
	}
}
