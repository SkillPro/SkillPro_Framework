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

package aml.skillpro.transformation.adapters;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import skillpro.model.assets.Resource;
import skillpro.model.assets.Setup;
import skillpro.model.skills.ResourceSkill;
import transformation.interfaces.ITransformable;
import aml.domain.InternalElement;
import aml.domain.Role;
import aml.model.Attribute;
import aml.model.AttributeDesignator;
import aml.model.AttributeType;
import aml.model.Hierarchy;
import aml.skillpro.transformation.adapters.template.TransformableAdapterTemplate;
import aml.skillpro.transformation.interfaces.IFactoryNodeTransformable;
import aml.skillpro.transformation.interfaces.IResourceSkillTransformable;
import aml.skillpro.transformation.interfaces.IResourceTransformable;
import aml.skillpro.transformation.interfaces.ISetupTransformable;

public class SetupAdapter extends TransformableAdapterTemplate implements ISetupTransformable {
	private Setup setup;
	
	public SetupAdapter() {
		this(new Setup());
	}
	
	public SetupAdapter(Setup setup) {
		this.setup = setup;
	}
	
	@Override
	public String getTransformableName() {
		return setup.getName();
	}

	@Override
	public String getTransformableID() {
		return setup.getId();
	}

	@Override
	public List<ITransformable> getTransformableChildren() {
		return null;
	}

	@Override
	public ITransformable getTransformableParent() {
		return null;
	}
	
	@Override
	public ITransformable transform(Hierarchy<InternalElement> object,
			Set<Object> context) throws InstantiationException, IllegalAccessException {
		//ritual start
		if (!context.contains(object)) {
			return null;
		}
		
		//returns a transformed element if it has already been transformed before.
		InternalElement ie = object.getElement();
		if (transformedObjectsMap.containsKey(ie)) {
			return (ITransformable) transformedObjectsMap.get(ie);
		}
		//ritual end
		
		setup.setName(ie.getName());
		setup.setId(ie.getId());
		
		//owner
		Resource owner = null;
		Hierarchy<InternalElement> parent = object.getParent();
		if (parent != null && context.contains(parent)) {
			Class<? extends ITransformable> transClass = adapterTransformablesMapping.get(parent.getElement());
			ITransformable transformedOwner = ((IFactoryNodeTransformable) transClass.newInstance())
					.transform(parent, context);
			owner = (Resource) transformedOwner.getElement();
		}
		
		if (owner == null) {
			throw new IllegalArgumentException("This Setup: " + setup + ", has to have an owner!");
		}
		owner.addSetup(setup);
		
		//resource skills
		for (Hierarchy<InternalElement> child : object.getChildren()) {
			if (child != null && context.contains(child)) {
				Class<? extends ITransformable> transClass = adapterTransformablesMapping.get(child.getElement());
				ITransformable transformedResourceSkill = ((IResourceSkillTransformable) transClass.newInstance())
						.transform(child, context);
				setup.addResourceSkill((ResourceSkill) transformedResourceSkill.getElement());
			}
		}
		//set current setup
		//I hope this attribute will stay unchanged forever
		Attribute currentConfAtt = new Attribute("Current Setup", AttributeType.BOOLEAN, "");
		AttributeDesignator currentConfDes = ie.getDesignatorByAttribute(currentConfAtt);
		if (currentConfDes != null) {
			if (currentConfDes.getValue().equalsIgnoreCase("true")) {
				owner.setCurrentSetup(setup);
			}
		}
		
		//ritual start
		transformedObjectsMap.put(ie, this);
		return this;
		//ritual end
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Object reverseTransform() {
		//start ritual
		if (reverseTransformedObjectsMap.containsKey(this)) {
			return reverseTransformedObjectsMap.get(this);
		}
		//end ritual
		
		//parent
		Hierarchy<InternalElement> resourceHie = (Hierarchy<InternalElement>) getTransformableResource().reverseTransform();
		
		//find role
		Role requiredRole = (Role) inversedPivotAdapterMapping.get(this.getClass());
		if (requiredRole == null) {
			throw new IllegalArgumentException("RequiredRole is null for this adapter: " + this.getClass().getSimpleName());
		}
		
		InternalElement internalElement = new InternalElement(getTransformableID(), getTransformableName());
		internalElement.setRequiredRole(requiredRole);
		
		if (setup.equals(setup.getResource().getCurrentSetup())) {
			Attribute currentSetupAtt = new Attribute("Current Setup", AttributeType.BOOLEAN, "");
			addAttribute(internalElement, currentSetupAtt, "true");
		}
		
		//parent
		Hierarchy<InternalElement> objectHie = new Hierarchy<InternalElement>(internalElement.getName(), internalElement);
		objectHie.setParent(resourceHie);
		
		//add ResourceSkills as children
		for (IResourceSkillTransformable trans : getTransformableResourceSkills()) {
			Hierarchy<InternalElement> resourceSkillHie = (Hierarchy<InternalElement>) trans.reverseTransform();
			InternalElement actualResourceSkill = new InternalElement(UUID.randomUUID().toString(), resourceSkillHie.getName());
			actualResourceSkill.setReferencedInternalElement(resourceSkillHie.getElement());
			Hierarchy<InternalElement> actualResourceSkillHierarchy = new Hierarchy<InternalElement>(actualResourceSkill.getName(), actualResourceSkill);
			actualResourceSkillHierarchy.setParent(objectHie);
		}
		
		//start ritual
		reverseTransformedObjectsMap.put(this, objectHie);
		return objectHie;
		//end ritual
	}
	
	@Override
	public Setup getElement() {
		return setup;
	}

	@Override
	public List<IResourceSkillTransformable> getTransformableResourceSkills() {
		List<IResourceSkillTransformable> transformables = new ArrayList<>();
		for (ResourceSkill skill : setup.getResourceSkills()) {
			transformables.add(new ResourceSkillAdapter(skill));
		}
		
		return transformables;
	}
	
	@Override
	public IResourceTransformable getTransformableResource() {
		if (setup.getResource() == null) {
			throw new IllegalArgumentException("Resource is null for this setup: " + getTransformableName());
		}
		return new ResourceAdapter(setup.getResource());
	}

	@Override
	public int hashCode() {
		return Objects.hash(setup);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SetupAdapter other = (SetupAdapter) obj;
		return Objects.equals(setup, other.setup);
	}
}
