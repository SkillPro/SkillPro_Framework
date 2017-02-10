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
import java.util.Set;

import skillpro.model.assets.ResourceConfigurationType;
import skillpro.model.properties.Property;
import transformation.interfaces.ITransformable;
import aml.domain.Role;
import aml.model.AttributeDesignator;
import aml.model.Hierarchy;
import aml.skillpro.transformation.adapters.template.TransformableAdapterTemplate;
import aml.skillpro.transformation.interfaces.IPropertyTransformable;
import aml.skillpro.transformation.interfaces.IResourceConfigurationTypeTransformable;

public class ResourceConfigurationTypeAdapter extends TransformableAdapterTemplate implements IResourceConfigurationTypeTransformable {
	private ResourceConfigurationType resourceConfigurationType;
	
	public ResourceConfigurationTypeAdapter() {
		this(new ResourceConfigurationType());
	}
	
	public ResourceConfigurationTypeAdapter(ResourceConfigurationType resourceConfigurationType) {
		this.resourceConfigurationType = resourceConfigurationType;
	}
	
	@Override
	public String getTransformableName() {
		return resourceConfigurationType.getName();
	}

	@Override
	public String getTransformableID() {
		return null;
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
	public Object getElement() {
		return resourceConfigurationType;
	}

	@Override
	public ITransformable transform(Role object, Set<Object> context) {
		if (!context.contains(object)) {
			return null;
		}
		//returns a transformed element if it has already been transformed before.
		if (transformedObjectsMap.containsKey(object)) {
			return transformedObjectsMap.get(object);
		}
		
		resourceConfigurationType.setName(object.getName());
		//properties
		for (AttributeDesignator des : object.getDesignators()) {
			//transform will put this transformed prop into transformed objects map as well.
			IPropertyTransformable transformedProp = new PropertyAdapter().transform(des.getAttribute());
			resourceConfigurationType.addProperty((Property) transformedProp.getElement());
		}
		
		transformedObjectsMap.put(object, this);
		return this;
	}

	@Override
	public Object reverseTransform() {
		//start ritual
		if (reverseTransformedObjectsMap.containsKey(this)) {
			return reverseTransformedObjectsMap.get(this);
		}
		//end ritual
		Hierarchy<Role> parentHie = resourceConfigurationTypeHierarchy;
		
		Role role = null;
		//find role
		for (Object obj : interfaceTransformablesMapping.keySet()) {
			if (obj instanceof Role) {
				if (((Role) obj).getName().equals(getTransformableName())) {
					role = (Role) obj;
				}
			}
		}
		if (role == null) {
			Role referencedRole = parentHie.getElement();
			role = new Role(getTransformableName(), referencedRole);
		}
		
		for (IPropertyTransformable propTrans : getTransformableProperties()) {
			role.addAttribute(propTrans.reverseTransform());
		}
		
		Hierarchy<Role> roleHie = new Hierarchy<Role>(role.getName(), role);
		roleHie.setParent(parentHie);
		//find template skill then set it as parent of roleHie
		
		//start ritual
		reverseTransformedObjectsMap.put(this, roleHie);
		return roleHie;
		//end ritual
	}

	@Override
	public List<IPropertyTransformable> getTransformableProperties() {
		List<IPropertyTransformable> transformables = new ArrayList<>();
		
		for (Property prop : resourceConfigurationType.getProperties()) {
			transformables.add(new PropertyAdapter(prop));
		}
		
		return transformables;
	}
}
